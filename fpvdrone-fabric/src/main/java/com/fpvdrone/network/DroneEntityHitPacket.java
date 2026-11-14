package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.IVDamageHelper;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.fpvdrone.network.compat.NetworkContext;

public class DroneEntityHitPacket {
    private final UUID droneId;
    private final UUID targetId;
    private final float speed;
    private final float velX;
    private final float velY;
    private final float velZ;
    private final double posX;
    private final double posY;
    private final double posZ;
    private static final float MAX_SPEED = 60.0F;
    private static final float MAX_DAMAGE = 40.0F;
    private static final float MIN_DAMAGE_SPEED = 5.0F;
    private static final double MAX_HIT_DISTANCE_SQ = 100.0;

    public DroneEntityHitPacket(UUID droneId, UUID targetId, float speed, float velX, float velY, float velZ, double posX, double posY, double posZ) {
        this.droneId = droneId;
        this.targetId = targetId;
        this.speed = speed;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public static void encode(DroneEntityHitPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeUUID(msg.targetId);
        buf.writeFloat(msg.speed);
        buf.writeFloat(msg.velX);
        buf.writeFloat(msg.velY);
        buf.writeFloat(msg.velZ);
        buf.writeDouble(msg.posX);
        buf.writeDouble(msg.posY);
        buf.writeDouble(msg.posZ);
    }

    public static DroneEntityHitPacket decode(FriendlyByteBuf buf) {
        return new DroneEntityHitPacket(
            buf.readUUID(),
            buf.readUUID(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble()
        );
    }

    public static void handle(DroneEntityHitPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                ServerLevel level = sender.level();
                UUID serverDroneId = FPVMod.getServerActiveDroneId(sender.getUUID());
                if (serverDroneId != null && serverDroneId.equals(msg.droneId)) {
                    Entity targetEntity = level.getEntity(msg.targetId);
                    if (targetEntity != null && targetEntity.isAlive()) {
                        if (!(targetEntity instanceof DroneEntity) && !(targetEntity instanceof com.fpvdrone.entity.DroppedBombEntity)) {
                            if (!(sender.distanceToSqr(targetEntity) > 400.0)) {
                                float speed = Math.min(Math.abs(msg.speed), 60.0F);
                                if (!(speed < 5.0F)) {
                                    if (FpvConfig.getDroneCollisionDamage()) {
                                        float mass = 1.58F;
                                        float damage = Math.min((speed - 5.0F) * mass * 2.0F, 40.0F);
                                        if (targetEntity instanceof Player player) {
                                            if (!player.isCreative()) {
                                                // 1.1.8: a lethal hit on a "Can Opener" wearer sets the vest's mine off instead.
                                                if (player instanceof ServerPlayer target) {
                                                    float afterArmour = com.fpvdrone.item.OpenerCookOff.damageAfterArmour(target, damage, sender.damageSources().flyIntoWall());
                                                    if (com.fpvdrone.item.OpenerCookOff.onLethalHit(target, afterArmour)) {
                                                        return;
                                                    }
                                                }

                                                player.hurt(sender.damageSources().flyIntoWall(), damage);
                                                float knockbackStrength = Math.min((speed - 5.0F) * 0.05F + 0.3F, 1.0F);
                                                Vec3 droneVel = new Vec3(msg.velX, msg.velY, msg.velZ);
                                                if (droneVel.lengthSqr() > 0.001) {
                                                    Vec3 dir = droneVel.normalize();
                                                    player.knockback(knockbackStrength, -dir.x, -dir.z, sender.damageSources().generic(), 0.0F);
                                                    double verticalKick = dir.y * knockbackStrength * 0.25;
                                                    player.setDeltaMovement(player.getDeltaMovement().add(0.0, verticalKick, 0.0));
                                                    player.syncVelocity = true;
                                                }
                                            }
                                        } else if (targetEntity instanceof LivingEntity living) {
                                            if (com.fpvdrone.item.OpenerCookOff.onLethalHit(living, damage)) {
                                                return;
                                            }

                                            living.hurt(sender.damageSources().flyIntoWall(), damage);
                                            float knockbackStrength = Math.min((speed - 5.0F) * 0.08F + 0.3F, 1.5F);
                                            Vec3 droneVel = new Vec3(msg.velX, msg.velY, msg.velZ);
                                            if (droneVel.lengthSqr() > 0.001) {
                                                Vec3 dir = droneVel.normalize();
                                                living.knockback(knockbackStrength, -dir.x, -dir.z, sender.damageSources().generic(), 0.0F);
                                                living.syncVelocity = true;
                                            }
                                        } else {
                                            if (IVDamageHelper.isIVEntity(targetEntity)) {
                                                IVDamageHelper.dealRawHpDamage(targetEntity, damage);
                                            } else {
                                                targetEntity.hurt(sender.damageSources().flyIntoWall(), damage);
                                            }

                                            Vec3 droneVel = new Vec3(msg.velX, msg.velY, msg.velZ);
                                            if (droneVel.lengthSqr() > 0.001) {
                                                Vec3 dir = droneVel.normalize();
                                                float knockbackStrength = Math.min((speed - 5.0F) * 0.08F + 0.3F, 1.5F);
                                                targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(dir.scale(knockbackStrength)));
                                                targetEntity.syncVelocity = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
