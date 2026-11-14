package com.fpvdrone.entity;

import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.data.ShrapnelStats;
import com.fpvdrone.data.SoundProfile;
import com.fpvdrone.data.SurfaceMaterial;
import com.fpvdrone.network.ExplosionSoundPacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.ShrapnelDecalPacket;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.PacketDistributor.TargetPoint;
import com.fpvdrone.util.ParticleSendRange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The mod's own explosion: shrapnel rays, a blast wave with an instant-kill core, doors forced open, block damage and
 * the explosion sound. 1.1.8 takes every number from the warhead's {@link ShrapnelStats} (its projectile file) instead
 * of the config, adds ground-debris decals where fragments strike, damages other drones, holds the chunks for the
 * dead, and breaks blocks itself when {@code explosionBreaksBlocks} is on.
 */
public class ShrapnelExplosion {
    private static final int MAX_DECALS = 48;
    private static final int MAX_DECALS_PER_FACE = 3;
    private static final double DECAL_CHANCE = 0.5;
    private static final double DECAL_FACE_INSET = 0.38;
    private static final double SHAPED_CHARGE_RANGE = 2.0;
    /** Keeps chunks loaded briefly after a detonation so the dead (and their drops) are not lost on unload (1.1.8). */
    public static final TicketType DEATH_HOLD = Registry.register(
        BuiltInRegistries.TICKET_TYPE,
        Identifier.fromNamespaceAndPath("fpvdrone", "death_hold"),
        new TicketType(40L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION)
    );
    private static final Map<Integer, Vec3[]> SPIRALS = new ConcurrentHashMap<>();

    /** Registers the chunk ticket type; call once from mod init. */
    public static void init() {
    }

    /** Holds the chunks around a point for two seconds (also used by loitering munitions before they detonate). */
    public static void holdChunks(ServerLevel level, Vec3 center, int radiusChunks) {
        level.getChunkSource().addTicketWithRadius(DEATH_HOLD, ChunkPos.containing(BlockPos.containing(center)), radiusChunks);
    }

    public static void detonate(ServerLevel level, Vec3 center, float power, int fragmentCount, Entity source, boolean customSound) {
        detonate(level, center, power, fragmentCount, source, customSound, null, ShrapnelStats.DEFAULT, null, false);
    }

    public static void detonate(
        ServerLevel level, Vec3 center, float power, int fragmentCount, Entity source, boolean customSound, @Nullable UUID excludeSoundPlayer
    ) {
        detonate(level, center, power, fragmentCount, source, customSound, excludeSoundPlayer, ShrapnelStats.DEFAULT, null, false);
    }

    public static void detonate(
        ServerLevel level, Vec3 center, float power, int fragmentCount, Entity source, boolean customSound, @Nullable UUID excludeSoundPlayer,
        ShrapnelStats stats
    ) {
        detonate(level, center, power, fragmentCount, source, customSound, excludeSoundPlayer, stats, null, false);
    }

    public static void detonate(
        ServerLevel level, Vec3 center, float power, int fragmentCount, Entity source, boolean customSound, @Nullable UUID excludeSoundPlayer,
        ShrapnelStats stats, @Nullable String projectileId
    ) {
        detonate(level, center, power, fragmentCount, source, customSound, excludeSoundPlayer, stats, projectileId, false);
    }

    /**
     * @param stats        the warhead's damage numbers
     * @param projectileId the projectile file id (sound profile, shaped-charge damage); null for a plain drone crash
     * @param hitsOwnPilot true when the pilot is not spared (1.1.8 passes true for bombs dropped from a bay)
     */
    public static void detonate(
        ServerLevel level,
        Vec3 center,
        float power,
        int fragmentCount,
        Entity source,
        boolean customSound,
        @Nullable UUID excludeSoundPlayer,
        ShrapnelStats stats,
        @Nullable String projectileId,
        boolean hitsOwnPilot
    ) {
        double outerRadius = stats.outerRadius(power);
        holdChunks(level, center, (int)Math.ceil(outerRadius / 16.0) + 2);
        double radius = stats.shrapnelRadius(power);
        UUID pilotUUID;
        if (source instanceof ServerPlayer sp) {
            pilotUUID = sp.getUUID();
        } else if (source instanceof DroneEntity drone) {
            pilotUUID = FPVMod.findPilotForDrone(drone.getUUID());
        } else {
            pilotUUID = null;
        }

        UUID sparedPilot = hitsOwnPilot ? null : pilotUUID;
        UUID ownDroneId;
        if (source instanceof DroneEntity drone) {
            ownDroneId = drone.getUUID();
        } else if (pilotUUID != null) {
            ownDroneId = FPVMod.getServerActiveDroneId(pilotUUID);
        } else {
            ownDroneId = null;
        }

        Map<LivingEntity, Float> damageMap = new HashMap<>();
        List<ShrapnelDecalPacket.Hit> decals = new ArrayList<>();
        Map<FaceKey, Integer> decalsPerFace = new HashMap<>();
        double blastRadius = stats.blastRadius();
        AABB shrapnelZone = new AABB(center, center).inflate(radius);

        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, shrapnelZone, e -> isTarget(e, ownDroneId, sparedPilot))) {
            AABB box = living.getBoundingBox();
            double dist = distanceToBox(center, box);
            if (!(dist > radius)) {
                Vec3 targetCenter = box.getCenter();
                ShrapnelClip.Result los = ShrapnelClip.trace(level, center, targetCenter);
                if (los.damageMultiplier() == 0.0) {
                    double blockDist = los.end().distanceTo(center);
                    double entityDist = center.distanceTo(targetCenter);
                    if (blockDist < entityDist - 0.1) {
                        continue;
                    }
                }

                double losMultiplier = los.damageMultiplier() > 0.0 ? los.damageMultiplier() : 1.0;
                float damage = (float)(shrapnelDamage(dist, radius, power, stats) * losMultiplier);
                if (!(damage < 0.5F)) {
                    damageMap.merge(living, damage, Float::sum);
                }
            }
        }

        List<LivingEntity> rayCandidates = level.getEntitiesOfClass(
            LivingEntity.class, new AABB(center, center).inflate(radius + 2.0), e -> isTarget(e, ownDroneId, sparedPilot)
        );

        for (int i = 0; i < fragmentCount; i++) {
            Vec3 dir = goldenSpiralDirection(i, fragmentCount);
            Vec3 end = center.add(dir.scale(radius));
            ShrapnelClip.Result clipResult = ShrapnelClip.trace(level, center, end);
            Vec3 effectiveEnd = clipResult.end();
            double effectiveRange = effectiveEnd.distanceTo(center);
            BlockHitResult blockHit = clipResult.blockHit();
            if (blockHit != null && decals.size() < MAX_DECALS) {
                BlockPos hitPos = blockHit.getBlockPos();
                boolean insideStartBlock = hitPos.equals(BlockPos.containing(center));
                Direction face;
                Vec3 at;
                if (insideStartBlock) {
                    face = faceAgainst(dir);
                    at = faceCenter(hitPos, face);
                } else {
                    face = blockHit.getDirection();
                    at = blockHit.getLocation();
                }

                FaceKey key = new FaceKey(hitPos, face);
                int onFace = decalsPerFace.getOrDefault(key, 0);
                if (onFace < MAX_DECALS_PER_FACE && level.getRandom().nextDouble() < DECAL_CHANCE) {
                    decalsPerFace.put(key, onFace + 1);
                    SurfaceMaterial material = SurfaceMaterial.of(level.getBlockState(hitPos));
                    at = clampIntoFace(hitPos, face, at, DECAL_FACE_INSET);
                    decals.add(new ShrapnelDecalPacket.Hit(at.x, at.y, at.z, face, material));
                }
            }

            AABB rayBounds = new AABB(center, effectiveEnd).inflate(1.0);

            for (LivingEntity candidate : rayCandidates) {
                if (!damageMap.containsKey(candidate) && candidate.getBoundingBox().intersects(rayBounds)) {
                    AABB inflatedBox = candidate.getBoundingBox().inflate(0.4);
                    Optional<Vec3> optHit = inflatedBox.clip(center, effectiveEnd);
                    double dist;
                    if (optHit.isPresent()) {
                        dist = optHit.get().distanceTo(center);
                    } else {
                        if (!inflatedBox.contains(center)) {
                            continue;
                        }

                        dist = 0.0;
                    }

                    if (!(dist > effectiveRange)) {
                        float damage = (float)(shrapnelDamage(dist, radius, power, stats) * clipResult.damageMultiplier());
                        if (!(damage < 0.5F)) {
                            damageMap.putIfAbsent(candidate, damage);
                        }
                    }
                }
            }
        }

        ServerPlayer sourcePlayer = source instanceof ServerPlayer sp ? sp : null;
        DamageSource dmgSource = level.damageSources().explosion(sourcePlayer, sourcePlayer);
        float blastDamage = stats.blastDamage();
        double killZone = stats.killZone();

        for (LivingEntity target : level.getEntitiesOfClass(
            LivingEntity.class, new AABB(center, center).inflate(blastRadius), e -> isTarget(e, ownDroneId, sparedPilot)
        )) {
            AABB box = target.getBoundingBox();
            double dist = distanceToBox(center, box);
            if (!(dist > blastRadius) && ShrapnelClip.checkBlastLos(level, center, box.getCenter())) {
                float waveDamage;
                if (dist <= killZone) {
                    waveDamage = 1000.0F;
                } else {
                    if (blastRadius <= killZone) {
                        continue;
                    }

                    double t = (dist - killZone) / (blastRadius - killZone);
                    t = Math.min(1.0, Math.max(0.0, t));
                    waveDamage = (float)(blastDamage * (1.0 - t) * (1.0 - t) * (1.0 - t));
                }

                if (!(waveDamage < 0.5F)) {
                    if (waveDamage >= 1000.0F) {
                        damageMap.put(target, 1000.0F);
                    } else {
                        damageMap.merge(target, waveDamage, Math::max);
                    }
                }
            }
        }

        for (Entry<LivingEntity, Float> entry : damageMap.entrySet()) {
            float damage = entry.getValue() >= 1000.0F ? 1000.0F : Math.min(entry.getValue(), stats.damageCap());
            applyDamageToEntity(entry.getKey(), damage, dmgSource, sourcePlayer, center, power);
        }

        // Keep the chunk of every mob that died loaded for a moment, so its death animation and drops finish.
        for (LivingEntity hit : damageMap.keySet()) {
            if (hit.isDeadOrDying() && !(hit instanceof ServerPlayer) && !(hit instanceof PilotBodyEntity)) {
                level.getChunkSource().addTicketWithRadius(DEATH_HOLD, ChunkPos.containing(hit.blockPosition()), 2);
            }
        }

        Map<Entity, Float> nonLivingDamage = new HashMap<>();

        for (Entity nle : level.getEntities(
            source,
            shrapnelZone,
            e -> e.isAlive() && !(e instanceof LivingEntity) && !(e instanceof DroneEntity) && !IVDamageHelper.isIVEntity(e)
            // 1.1.8-PENDING: also skip airframes with damage zones (LoiteringMunitionEntity, RampEntity); they get
            // their own pass below once ported.
        )) {
            AABB box = nle.getBoundingBox();
            double dist = distanceToBox(center, box);
            if (!(dist > radius)) {
                Vec3 targetCenter = box.getCenter();
                ShrapnelClip.Result los = ShrapnelClip.trace(level, center, targetCenter);
                double blockDist = los.end().distanceTo(center);
                double entityDist = center.distanceTo(targetCenter);
                if (los.damageMultiplier() != 0.0 || !(blockDist < entityDist)) {
                    double losMultiplier = los.damageMultiplier() > 0.0 ? los.damageMultiplier() : 1.0;
                    float damage = (float)(shrapnelDamage(dist, radius, power, stats) * losMultiplier);
                    if (!(damage < 0.5F)) {
                        nonLivingDamage.merge(nle, damage, Float::sum);
                    }
                }
            }
        }

        for (Entry<Entity, Float> entry : nonLivingDamage.entrySet()) {
            Entity target = entry.getKey();
            float damage = Math.min(entry.getValue(), stats.damageCap());
            target.hurtServer(level, dmgSource, damage);
            Vec3 knockback = target.position().subtract(center).normalize().scale(power * 0.5);
            target.setDeltaMovement(target.getDeltaMovement().add(knockback));
            target.syncVelocity = true;
        }

        // 1.1.8-PENDING: damage-zone pass for airframes with aimed hitboxes (LoiteringMunitionEntity, RampEntity):
        // nearest hitbox point, combinedDamage(...), capped by stats.damageCap(), applied through the airframe.

        if (IVDamageHelper.isIVAvailable()) {
            float shapedCharge = shapedChargeDamage(projectileId);

            for (Entity vehicle : IVDamageHelper.findIVEntities(level, new AABB(center, center).inflate(outerRadius))) {
                if (vehicle != source) {
                    AABB box = vehicle.getBoundingBox();
                    double dist = distanceToBox(center, box);
                    Vec3 nearest = nearestPoint(center, box);
                    float damage = combinedDamage(level, center, nearest, dist, radius, blastRadius, killZone, power, blastDamage, stats);
                    damage = damage >= 1000.0F ? 1000.0F : Math.min(damage, stats.damageCap());
                    if (shapedCharge > 0.0F && damage < 1000.0F && dist <= SHAPED_CHARGE_RANGE) {
                        damage += shapedCharge;
                    }

                    if (!(damage < 0.5F)) {
                        IVDamageHelper.dealRawHpDamage(vehicle, damage);
                    }
                }
            }
        }

        // SuperbWarfare vehicle damage (1.1.8) is Forge-only and has no Fabric counterpart.

        if (stats.opensDoors()) {
            openDoorsInRadius(level, center, blastRadius);
        }

        if (!decals.isEmpty()) {
            ModNetwork.CHANNEL
                .send(
                    PacketDistributor.NEAR.with(() -> new TargetPoint(center.x, center.y, center.z, ParticleSendRange.get(), level.dimension())),
                    new ShrapnelDecalPacket(decals)
                );
        }

        if (customSound) {
            boolean indoors = isIndoors(level, center);
            byte closeVariant = (byte)level.getRandom().nextInt(2);
            byte distantVariant = (byte)level.getRandom().nextInt(2);
            sendExplosionSoundPacket(level, center, indoors, closeVariant, distantVariant, power, excludeSoundPlayer, projectileId);
        }

        if (FpvConfig.isExplosionBreaksBlocks() && stats.blockPower() > 0.01F) {
            float blockPower = power * stats.blockPower();
            // Blocks only: the vanilla explosion's entity damage is skipped (ServerExplosionMixin), the passes above did it.
            FPVMod.suppressVanillaTntEntityDamage(true);

            try {
                level.explode(source, null, null, center.x, center.y, center.z, blockPower, false, ExplosionInteraction.TNT);
            } finally {
                FPVMod.suppressVanillaTntEntityDamage(false);
            }
        }
    }

    private static boolean isTarget(LivingEntity e, @Nullable UUID ownDroneId, @Nullable UUID sparedPilot) {
        return e.isAlive() && !isOwnDrone(e, ownDroneId) && !isOwnPilot(e, sparedPilot) && !isFlyingPilot(e) && !ridesArmouredVehicle(e);
    }

    /** Shrapnel and blast for one point, the larger of the two (used for vehicles and damage-zone airframes). */
    private static float combinedDamage(
        ServerLevel level,
        Vec3 center,
        Vec3 point,
        double dist,
        double radius,
        double blastRadius,
        double killZone,
        float power,
        float blastDamage,
        ShrapnelStats stats
    ) {
        float damage = 0.0F;
        if (dist <= radius) {
            ShrapnelClip.Result los = ShrapnelClip.trace(level, center, point);
            boolean blocked = los.damageMultiplier() == 0.0 && los.end().distanceTo(center) < dist - 0.1;
            if (!blocked) {
                double losMultiplier = los.damageMultiplier() > 0.0 ? los.damageMultiplier() : 1.0;
                damage = (float)(shrapnelDamage(dist, radius, power, stats) * losMultiplier);
            }
        }

        if (dist <= blastRadius && ShrapnelClip.checkBlastLos(level, center, point)) {
            float wave;
            if (dist <= killZone) {
                wave = 1000.0F;
            } else if (blastRadius <= killZone) {
                wave = 0.0F;
            } else {
                double t = (dist - killZone) / (blastRadius - killZone);
                t = Math.min(1.0, Math.max(0.0, t));
                wave = (float)(blastDamage * (1.0 - t) * (1.0 - t) * (1.0 - t));
            }

            damage = Math.max(damage, wave);
        }

        return damage;
    }

    /** The projectile's shaped-charge damage; 0 with no projectile (1.1.8). */
    private static float shapedChargeDamage(@Nullable String projectileId) {
        return projectileId == null ? 0.0F : ProjectileDefinitions.find(projectileId).map(ProjectileDefinition::shapedChargeDamage).orElse(0.0F);
    }

    private static float shrapnelDamage(double dist, double radius, float power, ShrapnelStats stats) {
        float peak = stats.peakDamage(power);
        double lethal = stats.lethalRange();
        if (dist <= lethal) {
            return peak;
        } else if (radius <= lethal) {
            return 0.0F;
        } else {
            double t = (dist - lethal) / (radius - lethal);
            t = Math.min(1.0, Math.max(0.0, t));
            return (float)(peak * (1.0 - t) * (1.0 - t));
        }
    }

    private static double distanceToBox(Vec3 p, AABB box) {
        return p.distanceTo(nearestPoint(p, box));
    }

    private static Vec3 nearestPoint(Vec3 p, AABB box) {
        return new Vec3(
            Math.max(box.minX, Math.min(p.x, box.maxX)), Math.max(box.minY, Math.min(p.y, box.maxY)), Math.max(box.minZ, Math.min(p.z, box.maxZ))
        );
    }

    private static void applyDamageToEntity(LivingEntity target, float damage, DamageSource dmgSource, ServerPlayer sourcePlayer, Vec3 center, float power) {
        if (sourcePlayer != null) {
            target.setLastHurtByPlayer(sourcePlayer, 100);
        }

        if (target instanceof ServerPlayer sp) {
            if (sp.isInvulnerableTo(sp.level(), dmgSource)) {
                return;
            }

            if (sp.getAbilities().invulnerable) {
                return;
            }

            float absorbed = Math.min(sp.getAbsorptionAmount(), damage);
            float afterAbsorb = damage - absorbed;
            float armorValue = sp.getArmorValue();
            float toughness = (float)sp.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            afterAbsorb = CombatRules.getDamageAfterAbsorb(sp, afterAbsorb, dmgSource, armorValue, toughness);
            float protLevel = EnchantmentHelper.getDamageProtection(sp.level(), sp, dmgSource);
            if (protLevel > 0) {
                afterAbsorb = CombatRules.getDamageAfterMagicAbsorb(afterAbsorb, protLevel);
            }

            float finalDamage = Math.max(afterAbsorb, 0.0F);
            if (!savedByWornArmour(sp, finalDamage)) {
                sp.setAbsorptionAmount(sp.getAbsorptionAmount() - absorbed);
                float newHealth = sp.getHealth() - finalDamage;
                sp.setHealth(newHealth);
                sp.animateHurt(0.0F);
                sp.setInvulnerableTime(20);
                if (newHealth <= 0.0F) {
                    sp.die(dmgSource);
                }
            }
        } else if (!savedByWornArmour(target, damage)) {
            target.hurtServer((ServerLevel)target.level(), dmgSource, damage);
        }

        Vec3 knockback = target.position().subtract(center).normalize().scale(power * 0.5);
        target.setDeltaMovement(target.getDeltaMovement().add(knockback));
        target.syncVelocity = true;
    }

    // 1.1.8-PENDING: wearable "opener" chest armour (data/fpvdrone/armor/opener.json) can absorb a lethal hit; its
    // check lives with the armour items. Until then nothing is saved.
    private static boolean savedByWornArmour(LivingEntity entity, float damage) {
        return false;
    }

    private static void openDoorsInRadius(ServerLevel level, Vec3 center, double radius) {
        BlockPos origin = BlockPos.containing(center);
        int r = (int)Math.ceil(radius);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (!(dx * dx + dy * dy + dz * dz > radius * radius)) {
                        BlockPos pos = origin.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() instanceof DoorBlock) {
                            if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                                && state.hasProperty(BlockStateProperties.OPEN)
                                && !(Boolean)state.getValue(BlockStateProperties.OPEN)) {
                                ((DoorBlock)state.getBlock()).setOpen(null, level, state, pos, true);
                            }
                        } else if (state.getBlock() instanceof TrapDoorBlock) {
                            if (state.hasProperty(BlockStateProperties.OPEN) && !(Boolean)state.getValue(BlockStateProperties.OPEN)) {
                                level.setBlock(pos, (BlockState)state.setValue(BlockStateProperties.OPEN, true), 10);
                            }
                        } else if (state.getBlock() instanceof FenceGateBlock
                            && state.hasProperty(BlockStateProperties.OPEN)
                            && !(Boolean)state.getValue(BlockStateProperties.OPEN)) {
                            level.setBlock(pos, (BlockState)state.setValue(BlockStateProperties.OPEN, true), 10);
                        }
                    }
                }
            }
        }
    }

    /** The face of the start block that a ray leaving in this direction would have entered through. */
    private static Direction faceAgainst(Vec3 dir) {
        double ax = Math.abs(dir.x);
        double ay = Math.abs(dir.y);
        double az = Math.abs(dir.z);
        if (ax >= ay && ax >= az) {
            return dir.x > 0.0 ? Direction.WEST : Direction.EAST;
        } else if (ay >= ax && ay >= az) {
            return dir.y > 0.0 ? Direction.DOWN : Direction.UP;
        } else {
            return dir.z > 0.0 ? Direction.NORTH : Direction.SOUTH;
        }
    }

    /** Keeps a decal inside the face so it never hangs over the block's edge. */
    private static Vec3 clampIntoFace(BlockPos pos, Direction face, Vec3 at, double inset) {
        double lo = inset;
        double hi = 1.0 - inset;
        double x = at.x;
        double y = at.y;
        double z = at.z;
        Direction.Axis axis = face.getAxis();
        if (axis != Direction.Axis.X) {
            x = pos.getX() + Math.min(Math.max(at.x - pos.getX(), lo), hi);
        }

        if (axis != Direction.Axis.Y) {
            y = pos.getY() + Math.min(Math.max(at.y - pos.getY(), lo), hi);
        }

        if (axis != Direction.Axis.Z) {
            z = pos.getZ() + Math.min(Math.max(at.z - pos.getZ(), lo), hi);
        }

        return new Vec3(x, y, z);
    }

    private static Vec3 faceCenter(BlockPos pos, Direction face) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        switch (face) {
            case WEST -> x = pos.getX();
            case EAST -> x = pos.getX() + 1.0;
            case DOWN -> y = pos.getY();
            case UP -> y = pos.getY() + 1.0;
            case NORTH -> z = pos.getZ();
            case SOUTH -> z = pos.getZ() + 1.0;
        }

        return new Vec3(x, y, z);
    }

    private static Vec3 goldenSpiralDirection(int index, int total) {
        return SPIRALS.computeIfAbsent(total, ShrapnelExplosion::buildSpiral)[index];
    }

    private static Vec3[] buildSpiral(int total) {
        Vec3[] dirs = new Vec3[total];

        for (int i = 0; i < total; i++) {
            double phi = Math.acos(1.0 - 2.0 * (i + 0.5) / total);
            double theta = (Math.PI * 2) * i / ((1.0 + Math.sqrt(5.0)) / 2.0);
            dirs[i] = new Vec3(Math.sin(phi) * Math.cos(theta), Math.sin(phi) * Math.sin(theta), Math.cos(phi));
        }

        return dirs;
    }

    private static boolean isIndoors(ServerLevel level, Vec3 center) {
        BlockPos origin = BlockPos.containing(center);
        BlockPos[] samples = new BlockPos[]{origin, origin.north(), origin.south(), origin.east(), origin.west()};
        int ceilingCount = 0;

        for (BlockPos sample : samples) {
            for (int dy = 1; dy <= 16; dy++) {
                BlockPos above = sample.above(dy);
                BlockState state = level.getBlockState(above);
                if (!state.isAir() && !state.propagatesSkylightDown()) {
                    ceilingCount++;
                    break;
                }
            }
        }

        return ceilingCount >= 4;
    }

    private static void sendExplosionSoundPacket(
        ServerLevel level,
        Vec3 center,
        boolean indoors,
        byte closeVariant,
        byte distantVariant,
        float power,
        @Nullable UUID excludePlayerId,
        @Nullable String projectileId
    ) {
        ExplosionSoundPacket packet = new ExplosionSoundPacket(center, indoors, closeVariant, distantVariant, power, projectileId);
        double range = explosionSoundRange(projectileId);
        if (excludePlayerId == null) {
            ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new TargetPoint(center.x, center.y, center.z, range, level.dimension())), packet);
        } else {
            double maxDistSq = range * range;

            for (ServerPlayer sp : level.players()) {
                if (!sp.getUUID().equals(excludePlayerId) && sp.distanceToSqr(center.x, center.y, center.z) <= maxDistSq) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), packet);
                }
            }
        }
    }

    /** Players within the projectile's explosion sound range get the explosion (1.1.8; 1.1.4 sent to 150 blocks). */
    private static double explosionSoundRange(@Nullable String projectileId) {
        return ProjectileDefinitions.find(projectileId).map(p -> (double)p.explosionSound().maxRange()).orElse((double)SoundProfile.BOMBLET.maxRange());
    }

    private static boolean isOwnPilot(LivingEntity entity, @Nullable UUID pilotUUID) {
        return pilotUUID != null && entity instanceof ServerPlayer sp && sp.getUUID().equals(pilotUUID);
    }

    /** Players flying a drone are spared; their parked body (PilotBodyEntity) takes the hit instead. */
    private static boolean isFlyingPilot(LivingEntity entity) {
        return entity instanceof ServerPlayer sp && FPVMod.hasActiveDrone(sp.getUUID());
    }

    /** Passengers of an armoured vehicle are protected by it; the vehicle takes the damage in its own pass. */
    private static boolean ridesArmouredVehicle(LivingEntity entity) {
        Entity vehicle = entity.getVehicle();
        return vehicle != null && IVDamageHelper.isIVEntity(vehicle);
    }

    private static boolean isOwnDrone(Entity entity, @Nullable UUID ownDroneId) {
        return ownDroneId != null && entity instanceof DroneEntity drone && drone.getUUID().equals(ownDroneId);
    }

    private record FaceKey(BlockPos pos, Direction face) {
    }
}
