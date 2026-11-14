package com.fpvdrone.network;

import com.fpvdrone.client.audio.ExplosionAudioEngine;
import com.fpvdrone.network.compat.NetworkContext;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/** Server to client: a projectile was fired at a point; play its launch sound profile (1.1.8). */
public class LaunchSoundPacket {
    private final Vec3 pos;
    private final String projectileId;

    public LaunchSoundPacket(Vec3 pos, String projectileId) {
        this.pos = pos;
        this.projectileId = projectileId == null ? "" : projectileId;
    }

    public static void encode(LaunchSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.pos.x);
        buf.writeDouble(msg.pos.y);
        buf.writeDouble(msg.pos.z);
        buf.writeUtf(msg.projectileId);
    }

    public static LaunchSoundPacket decode(FriendlyByteBuf buf) {
        Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new LaunchSoundPacket(pos, buf.readUtf());
    }

    public static void handle(LaunchSoundPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> ExplosionAudioEngine.get().playLaunch(msg.pos, msg.projectileId));
        ctx.get().setPacketHandled(true);
    }
}
