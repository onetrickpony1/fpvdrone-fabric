package com.fpvdrone.entity;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.network.ExplosionSoundPacket;
import com.fpvdrone.network.ModNetwork;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.PacketDistributor.TargetPoint;

public class ShrapnelExplosion {
    private static final double TWO_PI = Math.PI * 2;
    private static final double LETHAL_RANGE = 1.5;
    private static final float POWER_BASE_DAMAGE = 10.0F;
    private static final float POWER_SCALE_DAMAGE = 3.0F;

    public static void detonate(ServerLevel level, Vec3 center, float power, int fragmentCount, Entity source, boolean customSound) {
        detonate(level, center, power, fragmentCount, source, customSound, null);
    }

    public static void detonate(
        ServerLevel level, Vec3 center, float power, int fragmentCount, Entity source, boolean customSound, @Nullable UUID excludeSoundPlayer
    ) {
        double radius = Math.max(2.5, power * 3.0);
        UUID pilotUUID;
        if (source instanceof ServerPlayer sp) {
            pilotUUID = sp.getUUID();
        } else if (source instanceof DroneEntity drone) {
            pilotUUID = FPVMod.findPilotForDrone(drone.getUUID());
        } else {
            pilotUUID = null;
        }

        Map<LivingEntity, Float> damageMap = new HashMap<>();
        List<Vec3> rayEnds = new ArrayList<>(fragmentCount);
        List<Boolean> rayHits = new ArrayList<>(fragmentCount);
        double blastRadius = FpvConfig.getExplosionBlastRadius();
        AABB shrapnelZone = new AABB(center, center).inflate(radius);

        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, shrapnelZone, e -> e.isAlive() && !(e instanceof DroneEntity) && !isOwnPilot(e, pilotUUID))) {
            AABB box = living.getBoundingBox();
            double cx = Math.max(box.minX, Math.min(center.x, box.maxX));
            double cy = Math.max(box.minY, Math.min(center.y, box.maxY));
            double cz = Math.max(box.minZ, Math.min(center.z, box.maxZ));
            double dist = center.distanceTo(new Vec3(cx, cy, cz));
            if (!(dist > radius)) {
                Vec3 targetCenter = living.getBoundingBox().getCenter();
                ShrapnelClip.Result los = ShrapnelClip.trace(level, center, targetCenter);
                if (los.damageMultiplier() == 0.0) {
                    double blockDist = los.end().distanceTo(center);
                    double entityDist = center.distanceTo(targetCenter);
                    if (blockDist < entityDist - 0.1) {
                        continue;
                    }
                }

                double losMultiplier = los.damageMultiplier() > 0.0 ? los.damageMultiplier() : 1.0;
                float damage = (float)(calculateDamage(dist, radius, power) * losMultiplier);
                if (!(damage < 0.5F)) {
                    damageMap.merge(living, damage, Float::sum);
                }
            }
        }

        for (int i = 0; i < fragmentCount; i++) {
            Vec3 dir = goldenSpiralDirection(i, fragmentCount);
            Vec3 end = center.add(dir.scale(radius));
            ShrapnelClip.Result clipResult = ShrapnelClip.trace(level, center, end);
            Vec3 effectiveEnd = clipResult.end();
            double effectiveRange = effectiveEnd.distanceTo(center);
            boolean hitBlock = clipResult.damageMultiplier() == 0.0 || effectiveRange < radius - 0.1;
            rayEnds.add(effectiveEnd);
            rayHits.add(hitBlock);
            AABB rayBounds = new AABB(center, effectiveEnd).inflate(1.0);

            for (LivingEntity livingx : level.getEntitiesOfClass(
                LivingEntity.class, rayBounds, e -> e.isAlive() && !(e instanceof DroneEntity) && !isOwnPilot(e, pilotUUID) && !damageMap.containsKey(e)
            )) {
                if (!damageMap.containsKey(livingx)) {
                    AABB inflatedBox = livingx.getBoundingBox().inflate(0.4);
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
                        float damage = (float)(calculateDamage(dist, radius, power) * clipResult.damageMultiplier());
                        if (!(damage < 0.5F)) {
                            damageMap.putIfAbsent(livingx, damage);
                        }
                    }
                }
            }
        }

        ServerPlayer sourcePlayer = source instanceof ServerPlayer sp ? sp : null;
        DamageSource dmgSource = level.damageSources().explosion(sourcePlayer, sourcePlayer);
        float blastDamage = FpvConfig.getExplosionBlastDamage();
        double killZone = FpvConfig.getExplosionKillZone();

        for (LivingEntity target : level.getEntitiesOfClass(
            LivingEntity.class, new AABB(center, center).inflate(blastRadius), e -> e.isAlive() && !(e instanceof DroneEntity) && !isOwnPilot(e, pilotUUID)
        )) {
            AABB box = target.getBoundingBox();
            double cx = Math.max(box.minX, Math.min(center.x, box.maxX));
            double cy = Math.max(box.minY, Math.min(center.y, box.maxY));
            double cz = Math.max(box.minZ, Math.min(center.z, box.maxZ));
            double distx = center.distanceTo(new Vec3(cx, cy, cz));
            if (!(distx > blastRadius) && ShrapnelClip.checkBlastLos(level, center, box.getCenter())) {
                float waveDamage;
                if (distx <= killZone) {
                    waveDamage = 1000.0F;
                } else {
                    if (blastRadius <= killZone) {
                        continue;
                    }

                    double t = (distx - killZone) / (blastRadius - killZone);
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
            float damage = entry.getValue() >= 1000.0F ? 1000.0F : Math.min(entry.getValue(), 60.0F);
            applyDamageToEntity(entry.getKey(), damage, dmgSource, sourcePlayer, center, power);
        }

        Map<Entity, Float> nonLivingDamage = new HashMap<>();

        for (Entity nle : level.getEntities(source, shrapnelZone, e -> e.isAlive() && !(e instanceof LivingEntity) && !(e instanceof DroneEntity))) {
            AABB box = nle.getBoundingBox();
            double cx = Math.max(box.minX, Math.min(center.x, box.maxX));
            double cy = Math.max(box.minY, Math.min(center.y, box.maxY));
            double cz = Math.max(box.minZ, Math.min(center.z, box.maxZ));
            double distx = center.distanceTo(new Vec3(cx, cy, cz));
            if (!(distx > radius)) {
                Vec3 targetCenterx = box.getCenter();
                ShrapnelClip.Result losx = ShrapnelClip.trace(level, center, targetCenterx);
                double blockDist = losx.end().distanceTo(center);
                double entityDist = center.distanceTo(targetCenterx);
                if (losx.damageMultiplier() != 0.0 || !(blockDist < entityDist)) {
                    double losMultiplier = losx.damageMultiplier() > 0.0 ? losx.damageMultiplier() : 1.0;
                    float damage = (float)(calculateDamage(distx, radius, power) * losMultiplier);
                    if (!(damage < 0.5F)) {
                        nonLivingDamage.merge(nle, damage, Float::sum);
                    }
                }
            }
        }

        for (Entry<Entity, Float> entry : nonLivingDamage.entrySet()) {
            Entity targetx = entry.getKey();
            float damage = Math.min(entry.getValue(), 60.0F);
            if (IVDamageHelper.isIVEntity(targetx)) {
                IVDamageHelper.dealRawHpDamage(targetx, damage);
            } else {
                targetx.hurt(dmgSource, damage);
            }

            Vec3 knockback = targetx.position().subtract(center).normalize().scale(power * 0.5);
            targetx.setDeltaMovement(targetx.getDeltaMovement().add(knockback));
            targetx.syncVelocity = true;
        }

        if (FpvConfig.getExplosionOpensDoors()) {
            openDoorsInRadius(level, center, blastRadius);
        }


        if (FpvConfig.getExplosionBlockPower() < 0.01F) {
            spawnEffects(level, center, power);
        }

        if (customSound) {
            boolean indoors = isIndoors(level, center);
            byte closeVariant = (byte)level.getRandom().nextInt(2);
            byte distantVariant = (byte)level.getRandom().nextInt(2);
            sendExplosionSoundPacket(level, center, indoors, closeVariant, distantVariant, power, excludeSoundPlayer);
        }
    }

    private static float calculateDamage(double dist, double radius, float power) {
        float peak = 10.0F + 3.0F * power;
        if (dist <= 1.5) {
            return peak;
        } else if (radius <= 1.5) {
            return 0.0F;
        } else {
            double t = (dist - 1.5) / (radius - 1.5);
            t = Math.min(1.0, Math.max(0.0, t));
            return (float)(peak * (1.0 - t) * (1.0 - t));
        }
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
            sp.setAbsorptionAmount(sp.getAbsorptionAmount() - absorbed);
            float newHealth = sp.getHealth() - finalDamage;
            sp.setHealth(newHealth);
            sp.animateHurt(0.0F);
            sp.setInvulnerableTime(20);
            if (newHealth <= 0.0F) {
                sp.die(dmgSource);
            }
        } else {
            target.hurt(dmgSource, damage);
        }

        Vec3 knockback = target.position().subtract(center).normalize().scale(power * 0.5);
        target.setDeltaMovement(target.getDeltaMovement().add(knockback));
        target.syncVelocity = true;
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

    private static Vec3 goldenSpiralDirection(int index, int total) {
        double phi = Math.acos(1.0 - 2.0 * (index + 0.5) / total);
        double theta = (Math.PI * 2) * index / ((1.0 + Math.sqrt(5.0)) / 2.0);
        return new Vec3(Math.sin(phi) * Math.cos(theta), Math.sin(phi) * Math.sin(theta), Math.cos(phi));
    }

    private static void spawnEffects(ServerLevel level, Vec3 center, float power) {
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
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
        ServerLevel level, Vec3 center, boolean indoors, byte closeVariant, byte distantVariant, float power, @Nullable UUID excludePlayerId
    ) {
        ExplosionSoundPacket packet = new ExplosionSoundPacket(center, indoors, closeVariant, distantVariant, power);
        if (excludePlayerId == null) {
            ModNetwork.CHANNEL
                .send(PacketDistributor.NEAR.with(() -> new TargetPoint(center.x, center.y, center.z, 150.0, level.dimension())), packet);
        } else {
            double maxDistSq = 22500.0;

            for (ServerPlayer sp : level.players()) {
                if (!sp.getUUID().equals(excludePlayerId) && sp.distanceToSqr(center.x, center.y, center.z) <= maxDistSq) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), packet);
                }
            }
        }
    }

    private static boolean isOwnPilot(LivingEntity entity, @Nullable UUID pilotUUID) {
        return pilotUUID == null ? false : entity instanceof ServerPlayer sp && sp.getUUID().equals(pilotUUID);
    }
}
