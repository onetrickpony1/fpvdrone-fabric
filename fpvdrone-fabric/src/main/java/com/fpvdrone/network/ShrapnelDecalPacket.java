package com.fpvdrone.network;

import com.fpvdrone.client.particle.FpvParticles;
import com.fpvdrone.data.SurfaceMaterial;
import com.fpvdrone.network.compat.NetworkContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/** Server to client: where shrapnel fragments struck blocks, so the client lays ground-debris decals there (1.1.8). */
public class ShrapnelDecalPacket {
    private final List<Hit> hits;

    public ShrapnelDecalPacket(List<Hit> hits) {
        this.hits = hits;
    }

    public static void encode(ShrapnelDecalPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.hits.size());

        for (Hit hit : msg.hits) {
            buf.writeDouble(hit.x());
            buf.writeDouble(hit.y());
            buf.writeDouble(hit.z());
            buf.writeByte(hit.face().get3DDataValue());
            buf.writeByte(hit.material().ordinal());
        }
    }

    public static ShrapnelDecalPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Hit> hits = new ArrayList<>(count);
        SurfaceMaterial[] materials = SurfaceMaterial.values();

        for (int i = 0; i < count; i++) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            Direction face = Direction.from3DDataValue(buf.readByte() & 255);
            int m = buf.readByte() & 255;
            hits.add(new Hit(x, y, z, face, m < materials.length ? materials[m] : SurfaceMaterial.OTHER));
        }

        return new ShrapnelDecalPacket(hits);
    }

    public static void handle(ShrapnelDecalPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            for (Hit hit : msg.hits) {
                Vec3i n = hit.face().getUnitVec3i();
                FpvParticles.spawnDecal(new Vec3(hit.x(), hit.y(), hit.z()), new Vec3(n.getX(), n.getY(), n.getZ()), hit.material());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public record Hit(double x, double y, double z, Direction face, SurfaceMaterial material) {
    }
}
