package com.fpvdrone.network;

import com.fpvdrone.client.particle.FpvParticles;
import com.fpvdrone.network.compat.NetworkContext;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * Server to client: play a projectile's detonation particles (its {@code "particles"} list in
 * {@code data/fpvdrone/projectiles/<id>.json}) at a point, oriented along the impact direction (1.1.8).
 */
public class ProjectileBurstPacket {
    private final String projectileId;
    private final double x;
    private final double y;
    private final double z;
    private final float dirX;
    private final float dirY;
    private final float dirZ;

    public ProjectileBurstPacket(String projectileId, Vec3 pos, Vec3 direction) {
        this.projectileId = projectileId == null ? "" : projectileId;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        Vec3 dir = direction != null && direction.lengthSqr() > 1.0E-8 ? direction.normalize() : new Vec3(0.0, 1.0, 0.0);
        this.dirX = (float)dir.x;
        this.dirY = (float)dir.y;
        this.dirZ = (float)dir.z;
    }

    public ProjectileBurstPacket(String projectileId, Vec3 pos) {
        this(projectileId, pos, new Vec3(0.0, 1.0, 0.0));
    }

    private ProjectileBurstPacket(String projectileId, double x, double y, double z, float dirX, float dirY, float dirZ) {
        this.projectileId = projectileId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dirX = dirX;
        this.dirY = dirY;
        this.dirZ = dirZ;
    }

    public static void encode(ProjectileBurstPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.projectileId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.dirX);
        buf.writeFloat(msg.dirY);
        buf.writeFloat(msg.dirZ);
    }

    public static ProjectileBurstPacket decode(FriendlyByteBuf buf) {
        return new ProjectileBurstPacket(buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(ProjectileBurstPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get()
            .enqueueWork(
                () -> FpvParticles.spawnProjectileBurst(msg.projectileId, new Vec3(msg.x, msg.y, msg.z), null, new Vec3(msg.dirX, msg.dirY, msg.dirZ))
            );
        ctx.get().setPacketHandled(true);
    }
}
