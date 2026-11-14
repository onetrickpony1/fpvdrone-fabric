package com.fpvdrone.client.renderer;

import com.fpvdrone.block.JammerZones;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 (NQDShTvAwhG): with entity hitboxes shown (F3+B), each EW jammer's zone is drawn as a white wireframe
 * doughnut: 24 rings around the tube and 16 rings around the jammer. 1.1.8 drew it after entities with its own line
 * render type; 26.3 draws debug lines as gizmos, so this is emitted from {@code DebugRenderer.emitGizmos}.
 */
public final class JammerZoneDebugRenderer {
    private static final int TUBE_RINGS = 24;
    private static final int TUBE_SEGMENTS = 24;
    private static final int AROUND_RINGS = 16;
    private static final int AROUND_SEGMENTS = 96;
    private static final int WHITE = 0xFFFFFFFF;

    private JammerZoneDebugRenderer() {
    }

    public static void emit() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || JammerZones.zones().isEmpty() || !mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
            return;
        }

        for (JammerZones.Zone zone : JammerZones.zones()) {
            emitTorus(zone.centre(), zone.ringRadius(), zone.tubeRadius(), zone.verticalScale());
        }
    }

    private static void emitTorus(Vec3 c, double ring, double tube, double vScale) {
        // Rings around the tube, at 24 angles around the jammer.
        for (int i = 0; i < TUBE_RINGS; i++) {
            double around = Math.PI * 2 * (i / (double)TUBE_RINGS);
            double cos = Math.cos(around);
            double sin = Math.sin(around);
            Vec3 prev = null;
            for (int j = 0; j <= TUBE_SEGMENTS; j++) {
                double a = Math.PI * 2 * (j / (double)TUBE_SEGMENTS);
                double r = ring + tube * Math.cos(a);
                Vec3 p = c.add(r * cos, tube * vScale * Math.sin(a), r * sin);
                if (prev != null) {
                    Gizmos.line(prev, p, WHITE);
                }

                prev = p;
            }
        }

        // Rings around the jammer, at 16 angles around the tube.
        for (int i = 0; i < AROUND_RINGS; i++) {
            double a = Math.PI * 2 * (i / (double)AROUND_RINGS);
            double r = ring + tube * Math.cos(a);
            double y = tube * vScale * Math.sin(a);
            Vec3 prev = null;
            for (int j = 0; j <= AROUND_SEGMENTS; j++) {
                double around = Math.PI * 2 * (j / (double)AROUND_SEGMENTS);
                Vec3 p = c.add(r * Math.cos(around), y, r * Math.sin(around));
                if (prev != null) {
                    Gizmos.line(prev, p, WHITE);
                }

                prev = p;
            }
        }
    }
}
