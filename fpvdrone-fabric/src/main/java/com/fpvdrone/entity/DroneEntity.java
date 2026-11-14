package com.fpvdrone.entity;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.BatteryData;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.DroneConfig;
import com.fpvdrone.client.osd.OsdLayout;
import com.fpvdrone.item.BatteryItem;
import com.fpvdrone.item.BetaFlightItem;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.item.RemoteControlItem;
import com.fpvdrone.item.Rpg7Item;
import com.fpvdrone.network.DroneDamagePacket;
import com.fpvdrone.network.DroneDestroyedPacket;
import com.fpvdrone.network.DroneHitKickPacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.physics.PhysicsState;
import com.fpvdrone.physics.PhysicsUnits;
import com.fpvdrone.registry.ModItems;
import com.fpvdrone.registry.ModSounds;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.ServerHolder;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DroneEntity extends Mob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final String NBT_OWNER_UUID = "OwnerUUID";
    private static final String NBT_IS_CONTROLLED = "IsBeingControlled";
    private static final String NBT_ROLL = "DroneRoll";
    private static final String NBT_MOTORS_ARMED = "MotorsArmed";
    private static final String NBT_CONFIG = "DroneConfig";
    private static final String NBT_ADDED_WEIGHT = "AddedWeight";
    private static final String NBT_BATTERY = "BatteryData";
    private static final String NBT_HAS_BATTERY = "HasBattery";
    private static final String NBT_OSD_LAYOUT = "OsdLayout";
    private static final String NBT_HAS_RPG = "HasRpg";
    private static final String NBT_KIRPICHIKI_SHOWN = "KirpichikiShown";
    private static final String NBT_IS_THERMAL = "IsThermal";
    public static final float BASE_WEIGHT = 580.0F;
    private static final float DRONE_MASS = 1.05F;
    private static final float DRONE_AREA = 0.08F;
    private static final float DRAG_COEFFICIENT = 1.1F;
    private static final EntityDataAccessor<Boolean> DATA_CONTROLLED = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_ROLL = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_MOTORS_ARMED = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_BATTERY = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_ADDED_WEIGHT = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(DroneEntity.class, FpvDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_IN_FPV = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FALLING = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_FALL_TYPE = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<CompoundTag> DATA_OSD_LAYOUT = SynchedEntityData.defineId(DroneEntity.class, FpvDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Byte> DATA_VIDEO_CHANNEL = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_HAS_RPG = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_THROTTLE = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_KIRPICHIKI_SHOWN = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_USED_MAH = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_THERMAL = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
    // Replace Forge's IEntityAdditionalSpawnData: config and battery reach clients as synced tags.
    private static final EntityDataAccessor<CompoundTag> DATA_CONFIG = SynchedEntityData.defineId(DroneEntity.class, FpvDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<CompoundTag> DATA_BATTERY = SynchedEntityData.defineId(DroneEntity.class, FpvDataSerializers.COMPOUND_TAG);
    /** Explosions that break blocks but leave entity damage to the shrapnel system (Forge used ExplosionEvent.Detonate). */
    private static final ExplosionDamageCalculator BLOCKS_ONLY = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return 0.0F;
        }
    };
    public static final int MIN_CHANNEL = 1;
    public static final int MAX_CHANNEL = 8;
    private float fallYawRate;
    private float fallPitchRate;
    private float fallRollRate;
    private float fallThrustFactor;
    private float fallDragMultiplier;
    private boolean flipPhaseComplete;
    private float flipAccumulated;
    private int fallingGraceTicks;
    private static final int FALLING_GRACE_TICKS = 5;
    private boolean signalLossFall = false;
    private ChunkPos lastForcedChunk = null;
    private DroneConfig config = DroneConfig.defaults();
    private BatteryData battery = BatteryData.defaults();
    private float serverUsedMah = 0.0F;
    private int spawnGraceTicks = 0;
    private int motorsArmedTicks = 0;
    private double kirpichikiAnchorX;
    private double kirpichikiAnchorY;
    private double kirpichikiAnchorZ;
    private boolean kirpichikiAnchored = false;
    private int kirpichikiLiftoffTick = -1;
    private float prevDroneRoll = 0.0F;
    private Quaternionf remoteOrientation = new Quaternionf();
    private Quaternionf remotePrevOrientation = new Quaternionf();
    public double lerpTargetX;
    public double lerpTargetY;
    public double lerpTargetZ;
    public double lerpStartX;
    public double lerpStartY;
    public double lerpStartZ;
    public int lerpSteps = 0;
    private float propAngle = 0.0F;
    private float prevPropAngle = 0.0F;

    public DroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CONTROLLED, false);
        builder.define(DATA_ROLL, 0.0F);
        builder.define(DATA_MOTORS_ARMED, false);
        builder.define(DATA_HAS_BATTERY, false);
        builder.define(DATA_ADDED_WEIGHT, 0.0F);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_IN_FPV, false);
        builder.define(DATA_FALLING, false);
        builder.define(DATA_FALL_TYPE, (byte)0);
        builder.define(DATA_OSD_LAYOUT, new CompoundTag());
        builder.define(DATA_VIDEO_CHANNEL, (byte)1);
        builder.define(DATA_HAS_RPG, false);
        builder.define(DATA_THROTTLE, (byte)0);
        builder.define(DATA_KIRPICHIKI_SHOWN, false);
        builder.define(DATA_USED_MAH, 0.0F);
        builder.define(DATA_IS_THERMAL, false);
        builder.define(DATA_CONFIG, new CompoundTag());
        builder.define(DATA_BATTERY, new CompoundTag());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (this.level().isClientSide()) {
            if (DATA_CONFIG.equals(accessor)) {
                CompoundTag tag = this.entityData.get(DATA_CONFIG);
                if (!tag.isEmpty()) {
                    this.config = DroneConfig.fromNbt(tag);
                }
            } else if (DATA_BATTERY.equals(accessor)) {
                CompoundTag tag = this.entityData.get(DATA_BATTERY);
                if (!tag.isEmpty()) {
                    this.battery = BatteryData.fromNbt(tag);
                }
            }
        }
    }

    public void checkDespawn() {
    }

    public static Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    public void tick() {
        double savedXOld = this.xOld;
        double savedYOld = this.yOld;
        double savedZOld = this.zOld;
        boolean shouldRestore = this.level().isClientSide() && this.isBeingControlled();
        super.tick();
        if (shouldRestore) {
            this.xOld = savedXOld;
            this.yOld = savedYOld;
            this.zOld = savedZOld;
        }

        if (this.level().isClientSide()) {
            this.prevDroneRoll = this.getDroneRoll();
            this.prevPropAngle = this.propAngle;
            this.propAngle = this.propAngle + this.getPropSpinDelta();
            float TWO_PI = (float) (Math.PI * 2);
            if (this.propAngle > TWO_PI) {
                this.propAngle -= TWO_PI;
                this.prevPropAngle -= TWO_PI;
            }
        }

        if (this.level().isClientSide() && this.lerpSteps > 0) {
            Minecraft mc = Minecraft.getInstance();
            boolean isLocalDrone = mc.player != null && FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(this.getUUID())).orElse(false);
            if (!isLocalDrone) {
                this.lerpSteps--;
                double alpha = 1.0 - this.lerpSteps / 3.0;
                double newX = this.lerpStartX + (this.lerpTargetX - this.lerpStartX) * alpha;
                double newY = this.lerpStartY + (this.lerpTargetY - this.lerpStartY) * alpha;
                double newZ = this.lerpStartZ + (this.lerpTargetZ - this.lerpStartZ) * alpha;
                this.xOld = this.getX();
                this.yOld = this.getY();
                this.zOld = this.getZ();
                this.setPos(newX, newY, newZ);
            }
        }

        if (this.level().isClientSide()) {
            if (this.areMotorsArmed() || this.motorsArmedTicks > 0) {
                this.motorsArmedTicks++;
            }

            if (!this.kirpichikiAnchored) {
                this.kirpichikiAnchorX = this.getX();
                this.kirpichikiAnchorY = this.getY();
                this.kirpichikiAnchorZ = this.getZ();
            }

            if (this.areMotorsArmed() && !this.kirpichikiAnchored) {
                this.kirpichikiAnchored = true;
            }

            if (this.kirpichikiAnchored && this.kirpichikiLiftoffTick < 0 && !this.onGround()) {
                this.kirpichikiLiftoffTick = this.motorsArmedTicks;
            }
        }

        if (!this.level().isClientSide() && this.areMotorsArmed() && !(Boolean)this.entityData.get(DATA_KIRPICHIKI_SHOWN)) {
            this.entityData.set(DATA_KIRPICHIKI_SHOWN, true);
        }

        if (!this.level().isClientSide() && (this.isInWater() || this.isInLava())) {
            this.hurtServer((ServerLevel)this.level(), this.damageSources().drown(), Float.MAX_VALUE);
        }

        if (!this.level().isClientSide() && this.isBeingControlled()) {
            this.updateChunkLoading();
        }

        if (!this.level().isClientSide() && FPVMod.consumeDroneCleanup(this.getUUID())) {
            this.setInFpv(false);
            this.setBeingControlled(false);
            this.setMotorsArmed(false);
        }

        if (!this.level().isClientSide() && this.hasBattery()) {
            float dt = 0.05F;
            float currentAmps = this.areMotorsArmed() ? 25.0F : 1.5F;
            float mahThisTick = currentAmps * dt * 1000.0F / 3600.0F;
            this.serverUsedMah += mahThisTick;
            if (this.tickCount % 20 == 0) {
                this.entityData.set(DATA_USED_MAH, this.serverUsedMah);
            }
        }

        if (!this.level().isClientSide() && !this.isBeingControlled() && !this.isFalling() && this.hasBattery()) {
            this.updateChunkLoading();
        }

        if (!this.level().isClientSide() && this.isFalling()) {
            float dt = 0.05F;
            if (this.fallingGraceTicks > 0) {
                this.fallingGraceTicks--;
            }

            if (this.getFallType() == FallType.FLIP_AND_DROP && !this.flipPhaseComplete) {
                float pitchDelta = this.fallPitchRate * dt;
                this.flipAccumulated += pitchDelta;
                if (this.flipAccumulated >= 180.0F) {
                    this.flipAccumulated = 180.0F;
                    this.flipPhaseComplete = true;
                    this.fallYawRate = 0.0F;
                    this.fallPitchRate = 0.0F;
                    this.fallRollRate = 0.0F;
                }
            }

            this.setYRot(this.getYRot() + this.fallYawRate * dt);
            this.setXRot(this.getXRot() + this.fallPitchRate * dt);
            this.setDroneRoll(this.getDroneRoll() + this.fallRollRate * dt);
            Vec3 mcVel = this.getDeltaMovement();
            Vector3f vel = PhysicsUnits.toPhysicsVelocity(mcVel);
            float speed = vel.length();
            float dragFactor = 0.0539F;
            dragFactor *= this.fallDragMultiplier;
            if (speed > 1.0E-4F) {
                float dragAccel = speed * speed * dragFactor / 1.05F;
                vel.x = vel.x + -vel.x / speed * dragAccel * dt;
                vel.y = vel.y + -vel.y / speed * dragAccel * dt;
                vel.z = vel.z + -vel.z / speed * dragAccel * dt;
            }

            vel.y -= 9.80665F * dt;
            Vec3 newMcVel = PhysicsUnits.toMinecraftVelocity(vel);
            this.setDeltaMovement(newMcVel);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.updateChunkLoading();
            if ((this.onGround() || this.isInWater() || this.isInLava()) && this.fallingGraceTicks <= 0) {
                float impactSpeed = vel.length();
                this.handleGroundImpact(impactSpeed);
            } else if (this.getY() < this.level().getMinY() - 64) {
                this.handleGroundImpact(0.0F);
            }
        } else {
            if (!this.isBeingControlled()) {
                if (this.spawnGraceTicks > 0) {
                    ChunkAccess chunk = this.level().getChunkSource().getChunk(this.blockPosition().getX() >> 4, this.blockPosition().getZ() >> 4, false);
                    if (chunk != null && chunk.getPersistedStatus() == ChunkStatus.FULL) {
                        this.spawnGraceTicks--;
                        this.setDeltaMovement(Vec3.ZERO);
                        return;
                    }

                    this.setDeltaMovement(Vec3.ZERO);
                    return;
                }

                if (!this.level().isClientSide()) {
                    BlockPos feet = this.blockPosition();
                    if (this.level().getBlockState(feet).isSuffocating(this.level(), feet)) {
                        for (int dy = 1; dy <= 10; dy++) {
                            BlockPos above = feet.above(dy);
                            if (!this.level().getBlockState(above).isSuffocating(this.level(), above)) {
                                this.setPos(this.getX(), above.getY(), this.getZ());
                                this.setDeltaMovement(Vec3.ZERO);
                                break;
                            }
                        }
                    } else {
                        BlockPos below = feet.below();
                        BlockState belowState = this.level().getBlockState(below);
                        if (!belowState.isAir()) {
                            VoxelShape shape = belowState.getCollisionShape(this.level(), below);
                            if (!shape.isEmpty()) {
                                double surfaceY = below.getY() + shape.max(Axis.Y);
                                if (this.getY() < surfaceY) {
                                    this.setPos(this.getX(), surfaceY, this.getZ());
                                    this.setDeltaMovement(Vec3.ZERO);
                                }
                            }
                        }
                    }
                }

                if (this.hasBattery() && !this.isFalling() && this.isInFpv()) {
                    this.setDeltaMovement(Vec3.ZERO);
                    return;
                }

                Vec3 mcVelx = this.getDeltaMovement();
                Vector3f velx = PhysicsUnits.toPhysicsVelocity(mcVelx);
                float vx = velx.x;
                float vy = velx.y;
                float vz = velx.z;
                float speedx = velx.length();
                float dtx = 0.05F;
                float dragFactorx = 0.0539F;
                float dragForce = speedx * speedx * dragFactorx;
                float dragAccel = speedx > 1.0E-4F ? dragForce / 1.05F : 0.0F;
                if (speedx > 1.0E-4F) {
                    vx += -vx / speedx * dragAccel * dtx;
                    vy += -vy / speedx * dragAccel * dtx;
                    vz += -vz / speedx * dragAccel * dtx;
                }

                vy -= 9.80665F * dtx;
                if (this.onGround()) {
                    vy = Math.max(0.0F, vy);
                    vx *= 0.4F;
                    vz *= 0.4F;
                    if (Math.abs(vx) < 0.05F) {
                        vx = 0.0F;
                    }

                    if (Math.abs(vz) < 0.05F) {
                        vz = 0.0F;
                    }

                    float levelSpeed = 12.0F;
                    float t = Math.min(1.0F, levelSpeed * dtx);
                    this.setXRot(this.getXRot() * (1.0F - t));
                    this.setDroneRoll(this.getDroneRoll() * (1.0F - t));
                }

                velx.set(vx, vy, vz);
                Vec3 newMcVel = PhysicsUnits.toMinecraftVelocity(velx);
                this.setDeltaMovement(newMcVel);
                this.move(MoverType.SELF, this.getDeltaMovement());
            }
        }
    }

    private void updateChunkLoading() {
        if (this.level() instanceof ServerLevel serverLevel) {
            ChunkPos var3 = ChunkPos.containing(this.blockPosition());
            if (this.lastForcedChunk != null && !this.lastForcedChunk.equals(var3)) {
                DroneChunkTickets.forceChunk(serverLevel, this.lastForcedChunk.x(), this.lastForcedChunk.z(), false);
            }

            if (this.lastForcedChunk == null || !this.lastForcedChunk.equals(var3)) {
                DroneChunkTickets.forceChunk(serverLevel, var3.x(), var3.z(), true);
                this.lastForcedChunk = var3;
            }
        }
    }

    public void releaseChunkLoading() {
        if (this.lastForcedChunk != null && this.level() instanceof ServerLevel serverLevel) {
            DroneChunkTickets.forceChunk(serverLevel, this.lastForcedChunk.x(), this.lastForcedChunk.z(), false);
            this.lastForcedChunk = null;
        }
    }

    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    protected void registerGoals() {
    }

    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // The idle/fly animations in drone.animation.json are empty; props are spun by the renderer.
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public boolean isPushable() {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z, DamageSource source, float damage, boolean comesFromEffect) {
    }

    protected void pushEntities() {
    }

    public boolean isInWall() {
        return false;
    }

    public boolean isAffectedByPotions() {
        return false;
    }

    public boolean canBeLeashed() {
        return false;
    }

    public boolean canBeCollidedWith() {
        return true;
    }

    public boolean isPickable() {
        return true;
    }

    public void setOwner(@Nullable UUID owner) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(owner));
    }

    public Optional<UUID> getOwner() {
        return this.entityData.get(DATA_OWNER_UUID);
    }

    public void setInFpv(boolean inFpv) {
        this.entityData.set(DATA_IN_FPV, inFpv);
    }

    public boolean isInFpv() {
        return (Boolean)this.entityData.get(DATA_IN_FPV);
    }

    public boolean isFalling() {
        return (Boolean)this.entityData.get(DATA_FALLING);
    }

    public void setFalling(boolean falling) {
        this.entityData.set(DATA_FALLING, falling);
    }

    public FallType getFallType() {
        byte ordinal = (Byte)this.entityData.get(DATA_FALL_TYPE);
        FallType[] values = FallType.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FallType.DEAD_DROP;
    }

    public void setFallType(FallType type) {
        this.entityData.set(DATA_FALL_TYPE, (byte)type.ordinal());
    }

    public void setSignalLossFall(boolean value) {
        this.signalLossFall = value;
    }

    public void enterFallingState(FallType type, float[] angularVelocity) {
        this.setFalling(true);
        this.setFallType(type);
        this.fallYawRate = angularVelocity[0];
        this.fallPitchRate = angularVelocity[1];
        this.fallRollRate = angularVelocity[2];
        this.fallThrustFactor = type.getThrustFactor();
        this.fallDragMultiplier = type.getDragMultiplier();
        this.flipPhaseComplete = false;
        this.flipAccumulated = 0.0F;
        this.fallingGraceTicks = 5;
    }

    public void ensureAboveGround() {
        if (!this.level().isClientSide()) {
            BlockPos feet = this.blockPosition();
            if (this.level().getBlockState(feet).isSuffocating(this.level(), feet)) {
                for (int dy = 1; dy <= 10; dy++) {
                    BlockPos above = feet.above(dy);
                    if (!this.level().getBlockState(above).isSuffocating(this.level(), above)) {
                        this.setPos(this.getX(), above.getY(), this.getZ());
                        this.setDeltaMovement(Vec3.ZERO);
                        return;
                    }
                }
            } else {
                BlockPos below = feet.below();
                BlockState belowState = this.level().getBlockState(below);
                if (!belowState.isAir()) {
                    VoxelShape shape = belowState.getCollisionShape(this.level(), below);
                    if (!shape.isEmpty()) {
                        double surfaceY = below.getY() + shape.max(Axis.Y);
                        if (this.getY() < surfaceY) {
                            this.setPos(this.getX(), surfaceY, this.getZ());
                            this.setDeltaMovement(Vec3.ZERO);
                        }
                    }
                }
            }
        }
    }

    public void setBeingControlled(boolean controlled) {
        boolean wasControlled = this.isBeingControlled();
        this.entityData.set(DATA_CONTROLLED, controlled);
        if (wasControlled && !controlled && !this.isFalling()) {
            this.releaseChunkLoading();
        }
    }

    public boolean isBeingControlled() {
        return (Boolean)this.entityData.get(DATA_CONTROLLED);
    }

    public void setDroneRoll(float roll) {
        this.entityData.set(DATA_ROLL, roll);
    }

    public float getDroneRoll() {
        return (Float)this.entityData.get(DATA_ROLL);
    }

    public float getDroneRoll(float partialTick) {
        return this.prevDroneRoll + (this.getDroneRoll() - this.prevDroneRoll) * partialTick;
    }

    public void setRemoteOrientation(Quaternionf quat) {
        this.remotePrevOrientation.set(this.remoteOrientation);
        this.remoteOrientation.set(quat);
    }

    public Quaternionf getRemoteOrientation(float partialTick) {
        return new Quaternionf(this.remotePrevOrientation).slerp(this.remoteOrientation, partialTick);
    }

    public float getPropAngle(float partialTick) {
        return this.prevPropAngle + (this.propAngle - this.prevPropAngle) * partialTick;
    }

    private float getPropSpinDelta() {
        float throttle;
        if (this.areMotorsArmed() && this.isBeingControlled()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(this.getUUID())).orElse(false)) {
                PhysicsState physics = FPVMod.getPhysicsState(mc.player.getUUID());
                if (physics == null || !physics.isMotorsArmed()) {
                    throttle = 0.7F;
                } else if (physics.isThrottleCut()) {
                    throttle = 0.0F;
                } else {
                    float raw = ControllerReader.getThrottle();
                    throttle = Mth.clamp((raw + 1.0F) / 2.0F, 0.0F, 1.0F);
                }
            } else {
                throttle = 0.7F;
            }
        } else if (this.areMotorsArmed()) {
            throttle = 0.15F;
        } else {
            throttle = 0.0F;
        }

        return throttle * 6.0F;
    }

    public void setMotorsArmed(boolean armed) {
        this.entityData.set(DATA_MOTORS_ARMED, armed);
    }

    public boolean areMotorsArmed() {
        return (Boolean)this.entityData.get(DATA_MOTORS_ARMED);
    }

    public int getMotorsArmedTicks() {
        return this.motorsArmedTicks;
    }

    public double getKirpichikiAnchorX() {
        return this.kirpichikiAnchorX;
    }

    public double getKirpichikiAnchorY() {
        return this.kirpichikiAnchorY;
    }

    public double getKirpichikiAnchorZ() {
        return this.kirpichikiAnchorZ;
    }

    public boolean isKirpichikiAnchored() {
        return this.kirpichikiAnchored;
    }

    public boolean isKirpichikiEligible() {
        return !(Boolean)this.entityData.get(DATA_KIRPICHIKI_SHOWN);
    }

    public int getKirpichikiLiftoffTick() {
        return this.kirpichikiLiftoffTick;
    }

    public DroneConfig getConfig() {
        return this.config;
    }

    public void setConfig(DroneConfig config) {
        this.config = config != null ? config : DroneConfig.defaults();
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_CONFIG, this.config.toNbt());
        }
    }

    public float getAddedWeight() {
        return (Float)this.entityData.get(DATA_ADDED_WEIGHT);
    }

    public void setAddedWeight(float weight) {
        this.entityData.set(DATA_ADDED_WEIGHT, Math.max(0.0F, weight));
    }

    public void addWeight(float grams) {
        float current = this.getAddedWeight();
        this.setAddedWeight(current + grams);
    }

    // Client side only (called inside an isClientSide() check).
    private void openBetaFlightScreen() {
        OsdLayout layout = this.getOsdLayout();
        com.fpvdrone.gui.GuiEvents.openOsdBuilder(this, layout);
    }

    public BatteryData getBattery() {
        return this.battery;
    }

    public void setBattery(BatteryData battery) {
        this.battery = battery != null ? battery : BatteryData.defaults();
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_BATTERY, this.battery.toNbt());
        }

        this.serverUsedMah = 0.0F;
        this.entityData.set(DATA_USED_MAH, 0.0F);
    }

    public float getSyncedUsedMah() {
        return (Float)this.entityData.get(DATA_USED_MAH);
    }

    public void setServerUsedMah(float usedMah) {
        this.serverUsedMah = Math.max(0.0F, usedMah);
        this.entityData.set(DATA_USED_MAH, this.serverUsedMah);
    }

    public boolean hasBattery() {
        return (Boolean)this.entityData.get(DATA_HAS_BATTERY);
    }

    public void setHasBattery(boolean value) {
        this.entityData.set(DATA_HAS_BATTERY, value);
    }

    public boolean hasRpg() {
        return (Boolean)this.entityData.get(DATA_HAS_RPG);
    }

    public void setHasRpg(boolean value) {
        this.entityData.set(DATA_HAS_RPG, value);
    }

    public boolean isThermal() {
        return (Boolean)this.entityData.get(DATA_IS_THERMAL);
    }

    public void setThermal(boolean value) {
        this.entityData.set(DATA_IS_THERMAL, value);
    }

    public float getSyncedThrottle() {
        return ((Byte)this.entityData.get(DATA_THROTTLE) & 255) / 255.0F;
    }

    public void setSyncedThrottle(float throttle) {
        this.entityData.set(DATA_THROTTLE, (byte)(Mth.clamp(throttle, 0.0F, 1.0F) * 255.0F));
    }

    public OsdLayout getOsdLayout() {
        CompoundTag tag = this.entityData.get(DATA_OSD_LAYOUT);
        return tag != null && !tag.isEmpty() ? OsdLayout.fromNbt(tag) : OsdLayout.defaults();
    }

    public void setOsdLayout(OsdLayout layout) {
        this.entityData.set(DATA_OSD_LAYOUT, layout != null ? layout.toNbt() : new CompoundTag());
    }

    public int getVideoChannel() {
        return (Byte)this.entityData.get(DATA_VIDEO_CHANNEL);
    }

    public void setVideoChannel(int channel) {
        this.entityData.set(DATA_VIDEO_CHANNEL, (byte)Math.max(1, Math.min(8, channel)));
    }

    public String getVideoChannelName() {
        return "R" + this.getVideoChannel();
    }

    public void assignUniqueChannel(UUID ownerId) {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            HashSet var10 = new HashSet();
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
            if (player != null) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.getItem() instanceof RemoteControlItem) {
                        RemoteControlItem.getLinkedDroneId(stack).filter(idx -> !idx.equals(this.getUUID())).ifPresent(var10::add);
                    } else if (stack.getItem() instanceof FpvGogglesItem) {
                        Map<Integer, UUID> goggleDrones = FpvGogglesItem.getLinkedDrones(stack);

                        for (UUID id : goggleDrones.values()) {
                            if (!id.equals(this.getUUID())) {
                                var10.add(id);
                            }
                        }
                    }
                }

                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.getItem() instanceof FpvGogglesItem) {
                    Map<Integer, UUID> goggleDrones = FpvGogglesItem.getLinkedDrones(helmet);

                    for (UUID idx : goggleDrones.values()) {
                        if (!idx.equals(this.getUUID())) {
                            var10.add(idx);
                        }
                    }
                }
            }

            Set<Integer> taken = new HashSet<>();

            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof DroneEntity other && var10.contains(other.getUUID()) && !other.isRemoved() && other.isAlive()) {
                    taken.add(other.getVideoChannel());
                }
            }

            if (taken.contains(this.getVideoChannel())) {
                for (int ch = 1; ch <= 8; ch++) {
                    if (!taken.contains(ch)) {
                        this.setVideoChannel(ch);
                        return;
                    }
                }
            }
        }
    }

    public float getTotalWeight() {
        float rpgWeight = this.hasRpg() ? 1000.0F : 0.0F;
        return 580.0F + rpgWeight + this.battery.getWeightGrams();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        this.getOwner().ifPresent(uuid -> tag.store("OwnerUUID", net.minecraft.core.UUIDUtil.CODEC, uuid));
        tag.putBoolean("IsBeingControlled", this.isBeingControlled());
        tag.putFloat("DroneRoll", this.getDroneRoll());
        tag.putBoolean("MotorsArmed", this.areMotorsArmed());
        tag.store("DroneConfig", CompoundTag.CODEC, this.config.toNbt());
        tag.putFloat("AddedWeight", this.getAddedWeight());
        tag.store("BatteryData", CompoundTag.CODEC, this.battery.toNbt());
        tag.putBoolean("HasBattery", this.hasBattery());
        tag.putFloat("ServerUsedMah", this.serverUsedMah);
        tag.putBoolean("HasRpg", this.hasRpg());
        tag.putBoolean("IsThermal", this.isThermal());
        tag.putBoolean("KirpichikiShown", (Boolean)this.entityData.get(DATA_KIRPICHIKI_SHOWN));
        CompoundTag osdTag = this.entityData.get(DATA_OSD_LAYOUT);
        if (osdTag != null && !osdTag.isEmpty()) {
            tag.store("OsdLayout", CompoundTag.CODEC, osdTag);
        }

        tag.putByte("VideoChannel", (byte)this.getVideoChannel());
        if (this.isFalling()) {
            tag.putBoolean("IsFalling", true);
            tag.putByte("FallTypeId", (byte)this.getFallType().ordinal());
            tag.putFloat("FallYawRate", this.fallYawRate);
            tag.putFloat("FallPitchRate", this.fallPitchRate);
            tag.putFloat("FallRollRate", this.fallRollRate);
            tag.putFloat("FallThrustFactor", this.fallThrustFactor);
            tag.putFloat("FallDragMultiplier", this.fallDragMultiplier);
            tag.putBoolean("FlipPhaseComplete", this.flipPhaseComplete);
            tag.putFloat("FlipAccumulated", this.flipAccumulated);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        tag.read("OwnerUUID", net.minecraft.core.UUIDUtil.CODEC).ifPresent(this::setOwner);

        this.setDroneRoll(tag.getFloatOr("DroneRoll", 0.0F));

        this.setMotorsArmed(tag.getBooleanOr("MotorsArmed", false));

        tag.read("DroneConfig", CompoundTag.CODEC).ifPresent(c -> this.config = DroneConfig.fromNbt(c));
        this.entityData.set(DATA_CONFIG, this.config.toNbt());

        this.setAddedWeight(tag.getFloatOr("AddedWeight", 0.0F));

        tag.read("BatteryData", CompoundTag.CODEC).ifPresent(c -> this.battery = BatteryData.fromNbt(c));
        this.entityData.set(DATA_BATTERY, this.battery.toNbt());

        this.setHasBattery(tag.getBooleanOr("HasBattery", false));

        this.serverUsedMah = tag.getFloatOr("ServerUsedMah", 0.0F);
        this.entityData.set(DATA_USED_MAH, this.serverUsedMah);

        this.setHasRpg(tag.getBooleanOr("HasRpg", false));

        this.setThermal(tag.getBooleanOr("IsThermal", false));

        this.entityData.set(DATA_KIRPICHIKI_SHOWN, tag.getBooleanOr("KirpichikiShown", true));

        tag.read("OsdLayout", CompoundTag.CODEC).ifPresent(c -> this.setOsdLayout(OsdLayout.fromNbt(c)));

        this.setVideoChannel(tag.getByteOr("VideoChannel", (byte)1));

        if (tag.getBooleanOr("IsFalling", false)) {
            this.setFalling(true);
            {
                int ordinal = tag.getByteOr("FallTypeId", (byte)0);
                FallType[] types = FallType.values();
                if (ordinal >= 0 && ordinal < types.length) {
                    this.setFallType(types[ordinal]);
                }
            }

            this.fallYawRate = tag.getFloatOr("FallYawRate", 0.0F);
            this.fallPitchRate = tag.getFloatOr("FallPitchRate", 0.0F);
            this.fallRollRate = tag.getFloatOr("FallRollRate", 0.0F);
            this.fallThrustFactor = tag.getFloatOr("FallThrustFactor", 0.0F);
            this.fallDragMultiplier = tag.getFloatOr("FallDragMultiplier", 0.0F);
            this.flipPhaseComplete = tag.getBooleanOr("FlipPhaseComplete", false);
            this.flipAccumulated = tag.getFloatOr("FlipAccumulated", 0.0F);
            this.fallingGraceTicks = 5;
        }

        this.setBeingControlled(false);
        this.setInFpv(false);
        this.setMotorsArmed(false);
        this.spawnGraceTicks = 10;
        DroneLinkManager.linkDrone(this.getUUID(), this.getOwner().orElse(this.getUUID()));
    }

    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(this.isThermal() ? (ItemLike)ModItems.THERMAL_DRONE.get() : (ItemLike)ModItems.DRONE.get());
        CompoundTag tag = new CompoundTag();
        if (this.hasBattery()) {
            tag.putBoolean("HasBattery", true);
            BatteryData data = this.getBattery();
            int remaining = data.getCapacityMah() - (int)this.serverUsedMah;
            if (remaining < 0) {
                remaining = 0;
            }

            data.setRemainingMah(remaining);
            tag.put("BatteryData", data.toNbt());
        }

        if (this.hasRpg()) {
            tag.putBoolean("HasRpg", true);
        }

        if (!tag.isEmpty()) {
            com.fpvdrone.util.ItemData.edit(stack, t -> t.merge(tag));
        }

        return stack;
    }

    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && this.hasRpg()) {
            if (!this.level().isClientSide()) {
                this.setHasRpg(false);
                ItemStack rpgStack = new ItemStack((ItemLike)ModItems.RPG7.get());
                if (!player.getInventory().add(rpgStack)) {
                    player.drop(rpgStack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }

                player.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.rpg_detached").withStyle(Style.EMPTY.withColor(16749622)));
            }

            return InteractionResult.SUCCESS;
        } else if (player.isShiftKeyDown() && this.hasBattery()) {
            if (this.level().isClientSide()) {
                Minecraft.getInstance().getSoundManager().stop(Identifier.fromNamespaceAndPath("fpvdrone", "fpv_connect"), SoundSource.PLAYERS);
            } else {
                BatteryData data = this.getBattery();
                int remaining = data.getCapacityMah() - (int)this.serverUsedMah;
                if (remaining < 0) {
                    remaining = 0;
                }

                data.setRemainingMah(remaining);
                ItemStack batteryStack = new ItemStack((ItemLike)ModItems.BATTERY.get());
                BatteryItem.setBatteryData(batteryStack, data);
                this.setHasBattery(false);
                this.setBattery(BatteryData.defaults());
                this.releaseChunkLoading();
                if (!player.getInventory().add(batteryStack)) {
                    player.drop(batteryStack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }

                this.level().playSound(null, this.blockPosition(), (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
                player.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.battery_detached").withStyle(Style.EMPTY.withColor(16749622)));
            }

            return InteractionResult.SUCCESS;
        } else if (heldItem.getItem() instanceof RemoteControlItem) {
            if (!this.level().isClientSide()) {
                Optional<UUID> currentLink = RemoteControlItem.getLinkedDroneId(heldItem);
                if (currentLink.isPresent()) {
                    DroneLinkManager.unlinkDrone(currentLink.get());
                }

                UUID droneId = this.getUUID();
                this.setOwner(player.getUUID());
                this.setVideoChannel(1);
                this.assignUniqueChannel(player.getUUID());
                RemoteControlItem.setLinkedDroneId(heldItem, droneId);
                RemoteControlItem.setChannel(heldItem, this.getVideoChannel());
                DroneLinkManager.linkDrone(droneId, player.getUUID());
                player.sendOverlayMessage(
                    Component.translatable("item.fpvdrone.remote_control.link_success", new Object[]{this.getVideoChannelName()})
                        .withStyle(Style.EMPTY.withColor(16749622))
                );
            }

            return InteractionResult.SUCCESS;
        } else if (heldItem.getItem() instanceof FpvGogglesItem) {
            if (!this.level().isClientSide()) {
                UUID droneId = this.getUUID();
                this.setOwner(player.getUUID());
                this.setVideoChannel(1);
                this.assignUniqueChannel(player.getUUID());
                FpvGogglesItem.linkDroneOnChannel(heldItem, this.getVideoChannel(), droneId);
                DroneLinkManager.linkDrone(droneId, player.getUUID());
                player.sendOverlayMessage(
                    Component.translatable("item.fpvdrone.fpv_goggles.link_success", new Object[]{this.getVideoChannelName()})
                        .withStyle(Style.EMPTY.withColor(16749622))
                );
            }

            return InteractionResult.SUCCESS;
        } else if (heldItem.getItem() instanceof BetaFlightItem) {
            if (this.level().isClientSide()) {
                this.openBetaFlightScreen();
            }

            return InteractionResult.SUCCESS;
        } else if (heldItem.getItem() instanceof Rpg7Item) {
            if (!this.level().isClientSide()) {
                if (this.hasRpg()) {
                    player.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.rpg_already_attached").withStyle(Style.EMPTY.withColor(16749622)));
                } else {
                    this.setHasRpg(true);
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }

                    player.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.rpg_attached").withStyle(Style.EMPTY.withColor(16749622)));
                }
            }

            return InteractionResult.SUCCESS;
        } else if (heldItem.getItem() instanceof BatteryItem) {
            if (this.level().isClientSide()) {
                Minecraft.getInstance().getSoundManager().stop(Identifier.fromNamespaceAndPath("fpvdrone", "fpv_connect"), SoundSource.PLAYERS);
            } else if (this.hasBattery()) {
                player.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.battery_already_attached").withStyle(Style.EMPTY.withColor(16749622)));
            } else {
                BatteryData datax = BatteryItem.getBatteryData(heldItem);
                this.setBattery(datax);
                if (!datax.isFullCharge()) {
                    this.serverUsedMah = datax.getCapacityMah() - datax.getRemainingMah();
                    this.entityData.set(DATA_USED_MAH, this.serverUsedMah);
                }

                this.setHasBattery(true);
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }

                this.level().playSound(null, this.blockPosition(), (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
                player.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.battery_attached").withStyle(Style.EMPTY.withColor(16749622)));
            }

            return InteractionResult.SUCCESS;
        } else {
            if (!this.level().isClientSide()) {
                this.pickupDrone(player);
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (!this.level().isClientSide()) {
            System.out
                .println(
                    "[DroneEntity] hurt() called: source="
                        + source.getMsgId()
                        + ", amount="
                        + amount
                        + ", isInvulnerable="
                        + this.isInvulnerable()
                        + ", invulnerableTime="
                        + this.getInvulnerableTime()
                );
        }

        if (source.getDirectEntity() instanceof Player player && source.getDirectEntity() == source.getEntity() && !player.isPassenger()) {
            boolean isDronePilot = player.getUUID().equals(FPVMod.findPilotForDrone(this.getUUID()));
            if (!this.isInFpv() && !this.isBeingControlled() && !isDronePilot) {
                if (!this.level().isClientSide()) {
                    Optional<UUID> owner = this.getOwner();
                    if (owner.isEmpty() || owner.get().equals(player.getUUID())) {
                        this.pickupDrone(player);
                    }
                }

                return false;
            }
        }

        if (this.isInvulnerable()) {
            this.setInvulnerableTime(0);
        }

        if (this.isFalling()) {
            return false;
        } else {
            this.setInvulnerableTime(0);
            this.lastHurt = 0.0F;
            if (!this.level().isClientSide() && this.hasRpg()) {
                this.detonateRpg();
                return false;
            } else {
                boolean willBeLethal = !this.level().isClientSide() && this.getHealth() - amount <= 0.0F;
                if (willBeLethal) {
                    this.setHealth(amount + 1.0F);
                }

                boolean result = super.hurtServer(serverLevel, source, amount);
                if (result && !this.level().isClientSide() && !willBeLethal) {
                    Vec3 damagePos = source.getSourcePosition();
                    if (damagePos != null) {
                        Vec3 dir = this.position().subtract(damagePos).normalize();
                        float knockbackStrength = Math.min(amount * 0.6F, 3.0F);
                        Vec3 kick = dir.scale(knockbackStrength);
                        Vec3 currentVel = this.getDeltaMovement();
                        this.setDeltaMovement(currentVel.add(kick.x * 0.05, kick.y * 0.05, kick.z * 0.05));
                        float kickYaw = (float)(
                                dir.x * Math.cos(Math.toRadians(this.getYRot())) + dir.z * Math.sin(Math.toRadians(this.getYRot()))
                            )
                            * amount
                            * 40.0F;
                        float kickPitch = (float)dir.y * amount * -30.0F;
                        float kickRoll = (float)(
                                -dir.x * Math.sin(Math.toRadians(this.getYRot())) + dir.z * Math.cos(Math.toRadians(this.getYRot()))
                            )
                            * amount
                            * 35.0F;
                        this.setYRot(this.getYRot() + kickYaw * 0.05F);
                        this.setXRot(this.getXRot() + kickPitch * 0.05F);
                        this.setDroneRoll(this.getDroneRoll() + kickRoll * 0.05F);
                        UUID droneId = this.getUUID();
                        UUID pilotUUID = FPVMod.findPilotForDrone(droneId);
                        if (pilotUUID != null) {
                            MinecraftServer server = ServerHolder.server();
                            if (server != null) {
                                ServerPlayer pilot = server.getPlayerList().getPlayer(pilotUUID);
                                if (pilot != null) {
                                    Vector3f impulse = PhysicsUnits.toPhysicsVelocity(kick);
                                    ModNetwork.CHANNEL
                                        .send(
                                            PacketDistributor.PLAYER.with(() -> pilot), new DroneHitKickPacket(droneId, impulse, kickYaw, kickPitch, kickRoll)
                                        );
                                }
                            }
                        }
                    }
                }

                if (result && !this.level().isClientSide() && willBeLethal) {
                    this.setHealth(1.0F);
                    this.deathTime = 0;
                    this.dead = false;
                    Vec3 mcVel = this.getDeltaMovement();
                    Vector3f vel = PhysicsUnits.toPhysicsVelocity(mcVel);
                    float droneSpeed = vel.length();
                    Vec3 damagePos = source.getSourcePosition();
                    boolean hitFromAbove = false;
                    if (damagePos != null) {
                        hitFromAbove = damagePos.y > this.getY() + this.getBbHeight();
                    }

                    FallType fallType = FallType.determine(source, amount, droneSpeed, hitFromAbove);
                    float[] angularVelocity = fallType.randomizeAngularVelocity(new Random(this.getRandom().nextLong()));
                    Vec3 currentVel = this.getDeltaMovement();
                    Vector3f impulse = PhysicsUnits.toPhysicsVelocity(currentVel);
                    if (damagePos != null) {
                        Vec3 dir = this.position().subtract(damagePos).normalize();
                        impulse.add((float)dir.x * 5.0F, (float)dir.y * 5.0F, (float)dir.z * 5.0F);
                    } else {
                        impulse.add(0.0F, -2.0F, 0.0F);
                    }

                    this.enterFallingState(fallType, angularVelocity);
                    this.setBeingControlled(false);
                    UUID droneId = this.getUUID();
                    UUID pilotUUID = FPVMod.findPilotForDrone(droneId);
                    if (pilotUUID != null) {
                        MinecraftServer server = ServerHolder.server();
                        if (server != null) {
                            ServerPlayer pilot = server.getPlayerList().getPlayer(pilotUUID);
                            if (pilot != null) {
                                ModNetwork.CHANNEL
                                    .send(PacketDistributor.PLAYER.with(() -> pilot), new DroneDamagePacket(droneId, fallType, impulse, angularVelocity));
                            }
                        }
                    }
                }

                return result;
            }
        }
    }

    /** Client-side filter for server position updates (was a lerpTo override). */
    public boolean acceptServerPosition(@Nullable PositionPath position) {
        if (this.isBeingControlled()) {
            return false;
        }

        return position == null || this.position().distanceToSqr(position.endPosition()) > 1.0;
    }

    public void travel(Vec3 travelVector) {
    }

    @Override
    protected void tickHeadTurn(float yBodyRotT) {
    }

    public void detonateRpg() {
        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel)this.level();
            Vec3 pos = this.position();
            Vec3 mcVel = this.getDeltaMovement();
            Vector3f vel = PhysicsUnits.toPhysicsVelocity(mcVel);
            float speed = vel.length();
            float power = Math.min(2.5F + speed * 0.12F, 6.0F);
            int fragments = Math.min(20 + (int)(speed * 2.0F), 80);
            UUID droneId = this.getUUID();
            UUID pilotUUID = FPVMod.findPilotForDrone(droneId);
            UUID excludeSoundFor = this.signalLossFall ? pilotUUID : null;
            ShrapnelExplosion.detonate(serverLevel, pos, power, fragments, this, true, excludeSoundFor);
            if (pilotUUID != null) {
                MinecraftServer server = ServerHolder.server();
                if (server != null) {
                    ServerPlayer pilot = server.getPlayerList().getPlayer(pilotUUID);
                    if (pilot != null) {
                        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> pilot), new DroneDestroyedPacket(droneId));
                    }
                }
            }

            this.setBeingControlled(false);
            this.setMotorsArmed(false);
            FPVMod.onDroneDestroyedServer(droneId);
            this.releaseChunkLoading();
            this.discard();
            float blockPower = power * FpvConfig.getExplosionBlockPower();
            if (blockPower > 0.01F) {
                ServerPlayer pilot = pilotUUID != null ? serverLevel.getServer().getPlayerList().getPlayer(pilotUUID) : null;
                FPVMod.suppressVanillaTntEntityDamage(true);

                try {
                    serverLevel.explode(pilot, null, BLOCKS_ONLY, pos, blockPower, false, ExplosionInteraction.TNT);
                } finally {
                    FPVMod.suppressVanillaTntEntityDamage(false);
                }
            }
        }
    }

    private void handleGroundImpact(float impactSpeed) {
        if (!this.level().isClientSide()) {
            if (this.hasRpg()) {
                this.detonateRpg();
            } else {
                ServerLevel serverLevel = (ServerLevel)this.level();
                Vec3 pos = this.position();
                float explosionPower = 0.0F;
                UUID droneId = this.getUUID();
                UUID pilotUUID = FPVMod.findPilotForDrone(droneId);
                UUID excludeSoundFor = this.signalLossFall ? pilotUUID : null;
                if (impactSpeed >= 20.0F) {
                    explosionPower = 4.0F;
                    ShrapnelExplosion.detonate(serverLevel, pos, 4.0F, 40, this, true, excludeSoundFor);
                    serverLevel.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                } else if (impactSpeed > 10.0F) {
                    explosionPower = 2.5F;
                    ShrapnelExplosion.detonate(serverLevel, pos, 2.5F, 20, this, true, excludeSoundFor);
                } else if (impactSpeed >= 3.0F) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 5, 0.2, 0.2, 0.2, 0.01);
                }

                if (pilotUUID != null) {
                    MinecraftServer server = ServerHolder.server();
                    if (server != null) {
                        ServerPlayer pilot = server.getPlayerList().getPlayer(pilotUUID);
                        if (pilot != null) {
                            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> pilot), new DroneDestroyedPacket(droneId));
                        }
                    }
                }

                FPVMod.onDroneDestroyedServer(droneId);
                this.releaseChunkLoading();
                this.discard();
                float blockPower = explosionPower * FpvConfig.getExplosionBlockPower();
                if (blockPower > 0.01F) {
                    ServerPlayer pilot = pilotUUID != null ? serverLevel.getServer().getPlayerList().getPlayer(pilotUUID) : null;
                    FPVMod.suppressVanillaTntEntityDamage(true);

                    try {
                        serverLevel.explode(pilot, null, BLOCKS_ONLY, pos, blockPower, false, ExplosionInteraction.TNT);
                    } finally {
                        FPVMod.suppressVanillaTntEntityDamage(false);
                    }
                }
            }
        }
    }

    public void dropAsItem() {
        if (!this.level().isClientSide()) {
            UUID droneId = this.getUUID();
            UUID pilotUUID = FPVMod.findPilotForDrone(droneId);
            if (pilotUUID != null) {
                MinecraftServer server = ServerHolder.server();
                if (server != null) {
                    ServerPlayer pilot = server.getPlayerList().getPlayer(pilotUUID);
                    if (pilot != null) {
                        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> pilot), new DroneDestroyedPacket(droneId));
                    }
                }
            }

            FPVMod.onDroneDestroyedServer(droneId);
            this.spawnAtLocation((ServerLevel)this.level(), this.toItemStack());
            this.releaseChunkLoading();
            this.discard();
        }
    }

    private void pickupDrone(Player player) {
        UUID droneId = this.getUUID();
        UUID pilotUUID = FPVMod.findPilotForDrone(droneId);
        if (pilotUUID != null) {
            MinecraftServer server = ServerHolder.server();
            if (server != null) {
                ServerPlayer pilot = server.getPlayerList().getPlayer(pilotUUID);
                if (pilot != null) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> pilot), new DroneDestroyedPacket(droneId));
                }
            }
        }

        FPVMod.onDroneDestroyedServer(droneId);
        ItemStack droneItem = this.toItemStack();
        if (!player.getInventory().add(droneItem)) {
            player.drop(droneItem, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }

        this.unlinkAllRemotes(player);
        this.releaseChunkLoading();
        this.discard();
    }

    private void unlinkAllRemotes(Player player) {
        UUID droneId = this.getUUID();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof RemoteControlItem) {
                Optional<UUID> linkedId = RemoteControlItem.getLinkedDroneId(stack);
                if (linkedId.isPresent() && linkedId.get().equals(droneId)) {
                    RemoteControlItem.clearLink(stack);
                }
            }

            if (stack.getItem() instanceof FpvGogglesItem) {
                Optional<UUID> linkedId = FpvGogglesItem.getLinkedDroneId(stack);
                if (linkedId.isPresent() && linkedId.get().equals(droneId)) {
                    FpvGogglesItem.clearLink(stack);
                }
            }
        }

        DroneLinkManager.unlinkDrone(droneId);
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource damageSource) {
    }
}
