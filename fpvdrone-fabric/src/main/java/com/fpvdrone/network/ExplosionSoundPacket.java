package com.fpvdrone.network;

import com.fpvdrone.client.audio.ExplosionAudioEngine;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import com.fpvdrone.network.compat.NetworkContext;

/** Server to client: an explosion went off. 1.1.8 adds the projectile id, which picks its sound profile. */
public class ExplosionSoundPacket {
    private final Vec3 center;
    private final boolean indoors;
    private final byte closeVariant;
    private final byte distantVariant;
    private final float power;
    private final String projectileId;

    public ExplosionSoundPacket(Vec3 center, boolean indoors, byte closeVariant, byte distantVariant, float power, String projectileId) {
        this.center = center;
        this.indoors = indoors;
        this.closeVariant = closeVariant;
        this.distantVariant = distantVariant;
        this.power = power;
        this.projectileId = projectileId == null ? "" : projectileId;
    }

    public static void encode(ExplosionSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.center.x);
        buf.writeDouble(msg.center.y);
        buf.writeDouble(msg.center.z);
        buf.writeBoolean(msg.indoors);
        buf.writeByte(msg.closeVariant);
        buf.writeByte(msg.distantVariant);
        buf.writeFloat(msg.power);
        buf.writeUtf(msg.projectileId);
    }

    public static ExplosionSoundPacket decode(FriendlyByteBuf buf) {
        Vec3 center = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        boolean indoors = buf.readBoolean();
        byte closeVariant = buf.readByte();
        byte distantVariant = buf.readByte();
        float power = buf.readFloat();
        String projectileId = buf.readUtf();
        return new ExplosionSoundPacket(center, indoors, closeVariant, distantVariant, power, projectileId);
    }

    public static void handle(ExplosionSoundPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get()
            .enqueueWork(
                () -> ExplosionAudioEngine.get()
                    .playExplosion(msg.center, msg.indoors, msg.closeVariant, msg.distantVariant, msg.power, msg.projectileId)
            );
        ctx.get().setPacketHandled(true);
    }
}
