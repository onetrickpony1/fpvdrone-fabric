package com.fpvdrone.block;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 client-side list of loaded EW jammers (Record_TRHK7MZuHe). Each jammer is a ring (torus) around the block:
 * centre circle {@code ringRadius} blocks out, tube {@code tubeRadius} thick (vertically scaled by {@code verticalScale}).
 * Inside the tube the signal factor falls from 1 at the tube's edge to 0 over the inner half (as in 1.1.8: the area
 * right next to the jammer, inside the ring, is not jammed).
 */
public final class JammerZones {
    private static final Map<BlockPos, Zone> ZONES = new ConcurrentHashMap<>();

    private JammerZones() {
    }

    public static void add(BlockPos pos, double ringRadius, double tubeRadius, double verticalScale, double yOffset) {
        Vec3 centre = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5 + yOffset, pos.getZ() + 0.5);
        ZONES.put(pos.immutable(), new Zone(centre, ringRadius, tubeRadius, verticalScale));
    }

    public static void remove(BlockPos pos) {
        ZONES.remove(pos);
    }

    public static void clear() {
        ZONES.clear();
    }

    public static Collection<Zone> zones() {
        return ZONES.values();
    }

    /** Signal multiplier at a point: 1 = no jamming, 0 = fully jammed (lowest over all jammers). */
    public static float signalFactor(Vec3 pos) {
        if (ZONES.isEmpty()) {
            return 1.0F;
        }

        float factor = 1.0F;
        for (Zone zone : ZONES.values()) {
            double dx = pos.x - zone.centre.x;
            double dy = pos.y - zone.centre.y;
            double dz = pos.z - zone.centre.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            double radial = (horizontal - zone.ringRadius) / zone.tubeRadius;
            double verticalRadius = zone.tubeRadius * (zone.verticalScale <= 1.0E-6 ? 1.0 : zone.verticalScale);
            double vertical = dy / verticalRadius;
            double d = Math.sqrt(radial * radial + vertical * vertical);
            if (!(d >= 1.0) && !(zone.tubeRadius <= 1.0E-6)) {
                float f = (float)Math.max(0.0, d - 0.5);
                if (f < factor) {
                    factor = f;
                }
            }
        }

        return factor;
    }

    public record Zone(Vec3 centre, double ringRadius, double tubeRadius, double verticalScale) {
    }
}
