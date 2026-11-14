package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.IVDamageHelper;
import com.fpvdrone.entity.ShrapnelExplosion;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.NetworkContext;

public class DroneExplodePacket {
    private final UUID droneId;
    private final float explosionPower;
    private final int fragmentCount;
    @Nullable
    private final UUID vehicleHitId;

    public DroneExplodePacket(UUID droneId, float explosionPower, int fragmentCount) {
        this(droneId, explosionPower, fragmentCount, null);
    }

    public DroneExplodePacket(UUID droneId, float explosionPower, int fragmentCount, @Nullable UUID vehicleHitId) {
        this.droneId = droneId;
        this.explosionPower = explosionPower;
        this.fragmentCount = fragmentCount;
        this.vehicleHitId = vehicleHitId;
    }

    public static void encode(DroneExplodePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeFloat(msg.explosionPower);
        buf.writeVarInt(msg.fragmentCount);
        boolean hasVehicle = msg.vehicleHitId != null;
        buf.writeBoolean(hasVehicle);
        if (hasVehicle) {
            buf.writeUUID(msg.vehicleHitId);
        }
    }

    public static DroneExplodePacket decode(FriendlyByteBuf buf) {
        UUID droneId = buf.readUUID();
        float power = buf.readFloat();
        int frags = buf.readVarInt();
        UUID vehicleHitId = buf.readBoolean() ? buf.readUUID() : null;
        return new DroneExplodePacket(droneId, power, frags, vehicleHitId);
    }

    public static void handle(DroneExplodePacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.level();
                if (level.getEntity(msg.droneId) instanceof DroneEntity drone) {
                    UUID serverDroneId = FPVMod.getServerActiveDroneId(player.getUUID());
                    if (serverDroneId != null && serverDroneId.equals(msg.droneId)) {
                        float power = FpvConfig.getKamikazeExplosionPower();
                        int fragments = FpvConfig.getShrapnelCount();
                        double x = drone.getX();
                        double y = drone.getY();
                        double z = drone.getZ();
                        Vec3 pos = new Vec3(x, y, z);
                        // Forge-only Immersive Vehicles explosion events have no Fabric counterpart.
                        if (msg.vehicleHitId != null) {
                            Entity vehicleEntity = level.getEntity(msg.vehicleHitId);
                            if (vehicleEntity != null && vehicleEntity.isAlive() && IVDamageHelper.isIVEntity(vehicleEntity)) {
                                float shapedChargeDamage = 250.0F;
                                IVDamageHelper.dealRawHpDamage(vehicleEntity, shapedChargeDamage);
                            }
                        }

                        ShrapnelExplosion.detonate(level, pos, power, fragments, player, true);
                        drone.setBeingControlled(false);
                        drone.setMotorsArmed(false);
                        FPVMod.onDroneDestroyedServer(msg.droneId);
                        drone.releaseChunkLoading();
                        drone.discard();
                        float blockPower = power * FpvConfig.getExplosionBlockPower();
                        if (blockPower > 0.01F) {
                            FPVMod.suppressVanillaTntEntityDamage(true);

                            try {
                                level.explode(null, x, y, z, blockPower, ExplosionInteraction.TNT);
                            } finally {
                                FPVMod.suppressVanillaTntEntityDamage(false);
                            }
                        }

                        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DroneDestroyedPacket(msg.droneId));
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
