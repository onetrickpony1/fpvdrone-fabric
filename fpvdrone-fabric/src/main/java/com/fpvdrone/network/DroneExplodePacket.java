package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.data.ShrapnelStats;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.ShrapnelExplosion;
import com.fpvdrone.network.compat.NetworkContext;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.PacketDistributor.TargetPoint;
import com.fpvdrone.util.ParticleSendRange;
import java.util.UUID;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Client to server: the pilot's armed drone hit something hard enough to detonate. 1.1.8 sends the impact direction
 * (for the particle burst) and takes power, fragments and damage numbers from the drone's projectile on the server,
 * ignoring the client's values; the old IV vehicle id is gone (shaped-charge damage is part of the vehicle pass).
 */
public class DroneExplodePacket {
    private final UUID droneId;
    private final float explosionPower;
    private final int fragmentCount;
    private final float impactX;
    private final float impactY;
    private final float impactZ;

    public DroneExplodePacket(UUID droneId, float explosionPower, int fragmentCount) {
        this(droneId, explosionPower, fragmentCount, 0.0F, 1.0F, 0.0F);
    }

    // 1.1.8-PENDING: the client callback in FPVClientMod still passes the IV vehicle it hit; 1.1.8 sends the impact
    // direction instead (the 6-argument constructor). The vehicle id is no longer needed and is dropped.
    @Deprecated
    public DroneExplodePacket(UUID droneId, float explosionPower, int fragmentCount, @Nullable UUID vehicleHitId) {
        this(droneId, explosionPower, fragmentCount);
    }

    public DroneExplodePacket(UUID droneId, float explosionPower, int fragmentCount, float impactX, float impactY, float impactZ) {
        this.droneId = droneId;
        this.explosionPower = explosionPower;
        this.fragmentCount = fragmentCount;
        this.impactX = impactX;
        this.impactY = impactY;
        this.impactZ = impactZ;
    }

    public static void encode(DroneExplodePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeFloat(msg.explosionPower);
        buf.writeVarInt(msg.fragmentCount);
        buf.writeFloat(msg.impactX);
        buf.writeFloat(msg.impactY);
        buf.writeFloat(msg.impactZ);
    }

    public static DroneExplodePacket decode(FriendlyByteBuf buf) {
        UUID droneId = buf.readUUID();
        float power = buf.readFloat();
        int frags = buf.readVarInt();
        return new DroneExplodePacket(droneId, power, frags, buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(DroneExplodePacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.level();
                if (level.getEntity(msg.droneId) instanceof DroneEntity drone) {
                    UUID serverDroneId = FPVMod.getServerActiveDroneId(player.getUUID());
                    if (serverDroneId != null && serverDroneId.equals(msg.droneId)) {
                        // 1.1.8-PENDING: ProjectileDefinitions.get(drone's equipped projectile id): its ExplosionPower,
                        // FragmentCount, Stats and id. Until then every armed drone carries the bundled rpg7.
                        String projectileId = "rpg7";
                        float power = 4.0F;
                        int fragments = 128;
                        ShrapnelStats stats = ShrapnelStats.RPG7;
                        Vec3 pos = new Vec3(drone.getX(), drone.getY(), drone.getZ());
                        // Forge's ExplosionEvent.Detonate (posted so other mods could react) has no Fabric counterpart.
                        ShrapnelExplosion.detonate(level, pos, power, fragments, player, true, null, stats, projectileId, false);
                        Vec3 impact = new Vec3(msg.impactX, msg.impactY, msg.impactZ);
                        ModNetwork.CHANNEL
                            .send(
                                PacketDistributor.NEAR.with(() -> new TargetPoint(pos.x, pos.y, pos.z, ParticleSendRange.get(), level.dimension())),
                                new ProjectileBurstPacket(projectileId, pos, impact)
                            );
                        drone.setBeingControlled(false);
                        drone.setMotorsArmed(false);
                        FPVMod.onDroneDestroyedServer(msg.droneId);
                        drone.releaseChunkLoading();
                        drone.discard();
                        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DroneDestroyedPacket(msg.droneId));
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
