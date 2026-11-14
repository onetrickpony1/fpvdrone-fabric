package com.fpvdrone.entity;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.MoverType;
import net.minecraft.core.BlockPos;
import com.fpvdrone.FPVMod;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PilotBodyEntity extends LivingEntity {
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(
        PilotBodyEntity.class, FpvDataSerializers.OPTIONAL_UUID
    );
    private static final EntityDataAccessor<String> DATA_OWNER_NAME = SynchedEntityData.defineId(PilotBodyEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_SLIM_MODEL = SynchedEntityData.defineId(PilotBodyEntity.class, EntityDataSerializers.BOOLEAN);
    private int orphanCheckTicks = 0;
    private int ownerUpdateTicks;
    private static final int ORPHAN_CHECK_DELAY = 100;

    public PilotBodyEntity(EntityType<? extends PilotBodyEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_OWNER_NAME, "");
        builder.define(DATA_SLIM_MODEL, false);
    }

    public void setOwner(Player player) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
        this.entityData.set(DATA_OWNER_NAME, player.getGameProfile().name());
        this.setCustomName(Component.literal(player.getGameProfile().name()));
        this.setCustomNameVisible(true);

        for (int i = 0; i < 4; i++) {
            EquipmentSlot slot = EquipmentSlot.values()[i + 2];
            this.setItemSlot(slot, player.getItemBySlot(slot).copy());
        }

        this.setItemSlot(EquipmentSlot.MAINHAND, player.getMainHandItem().copy());
        this.setItemSlot(EquipmentSlot.OFFHAND, player.getOffhandItem().copy());
        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot());
        this.yRotO = player.yRotO;
        this.xRotO = player.xRotO;
        this.yHeadRot = player.yHeadRot;
        this.yHeadRotO = player.yHeadRotO;
        this.yBodyRot = player.yBodyRot;
        this.yBodyRotO = player.yBodyRotO;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return (UUID)((Optional)this.entityData.get(DATA_OWNER_UUID)).orElse(null);
    }

    public String getOwnerName() {
        return (String)this.entityData.get(DATA_OWNER_NAME);
    }

    public boolean isSlimModel() {
        return (Boolean)this.entityData.get(DATA_SLIM_MODEL);
    }

    public void setSlimModel(boolean slim) {
        this.entityData.set(DATA_SLIM_MODEL, slim);
    }

    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            // 1.1.8: the body keeps its own chunks loaded, is remembered for the exit, and tells its owner where it is.
            PilotBodyManager.tickChunkLoading(this);
            PilotBodyManager.rememberPosition(this);
            this.ownerUpdateTicks++;
            if (this.ownerUpdateTicks >= 20) {
                this.ownerUpdateTicks = 0;
                PilotBodyManager.sendPositionToOwner(this);
            }

            this.orphanCheckTicks++;
            if (this.orphanCheckTicks >= 100) {
                this.orphanCheckTicks = 0;
                UUID ownerUUID = this.getOwnerUUID();
                if (ownerUUID != null) {
                    PilotBodyEntity tracked = PilotBodyManager.getPilotBody(ownerUUID);
                    if (tracked == null || tracked != this) {
                        this.discard();
                    }
                }
            }
        }
    }

    /** 1.1.8: the body falls and slides like a mob (1.1.4 froze it in place); a body with no gravity behaves as vanilla. */
    @Override
    public void travel(Vec3 input) {
        if (!this.level().isClientSide() && !this.isNoGravity()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 motion = this.getDeltaMovement();
            double friction = 0.91;
            if (this.onGround()) {
                BlockPos below = this.getBlockPosBelowThatAffectsMyMovement();
                friction = this.level().getBlockState(below).getBlock().getFriction() * 0.91F;
            }

            this.setDeltaMovement(motion.x * friction, (motion.y - 0.08) * 0.98, motion.z * friction);
            this.calculateEntityAnimation(false);
        } else {
            super.travel(input);
        }
    }

    public boolean hurtServer(ServerLevel damageLevel, DamageSource source, float amount) {
        UUID ownerUUID = this.getOwnerUUID();
        if (ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
            if (owner != null && owner.getAbilities().invulnerable) {
                return false;
            }
        }

        boolean result = super.hurtServer(damageLevel, source, amount);
        if (this.isDeadOrDying() && ownerUUID != null) {
            FPVMod.setArmed(ownerUUID, false);
        }

        return result;
    }

    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public boolean isPushable() {
        UUID ownerUUID = this.getOwnerUUID();
        if (ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
            if (owner != null && owner.getAbilities().invulnerable) {
                return false;
            }
        }

        return false;
    }

    public boolean canBeCollidedWith(net.minecraft.world.entity.Entity other) {
        UUID ownerUUID = this.getOwnerUUID();
        if (ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
            if (owner != null && owner.getAbilities().invulnerable) {
                return false;
            }
        }

        return super.canBeCollidedWith(other);
    }

    protected void pushEntities() {
    }

    public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        UUID ownerUUID = this.getOwnerUUID();
        if (ownerUUID != null) {
            tag.store("OwnerUUID", net.minecraft.core.UUIDUtil.CODEC, ownerUUID);
        }

        tag.putString("OwnerName", this.getOwnerName());
        tag.putBoolean("SlimModel", this.isSlimModel());
    }

    public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_OWNER_UUID, tag.read("OwnerUUID", net.minecraft.core.UUIDUtil.CODEC));
        this.entityData.set(DATA_OWNER_NAME, tag.getStringOr("OwnerName", ""));
        this.entityData.set(DATA_SLIM_MODEL, tag.getBooleanOr("SlimModel", false));
    }

    public boolean isCustomNameVisible() {
        return true;
    }
}
