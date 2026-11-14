package com.fpvdrone.render;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 (ShrapnelDebugRenderer): with entity hitboxes shown (F3+B), the shrapnel rays of the explosions of the last
 * 20 seconds are drawn as white lines from the centre, with a small box where a ray hit something. 1.1.8 drew them
 * after entities with the LINES render type; 26.3 draws debug lines as gizmos (emitted from DebugRendererMixin).
 */
public final class ShrapnelDebugRenderer {
   private static final long KEEP_MS = 20000L;
   private static final double HIT_BOX = 0.15;
   private static final int WHITE = 0xFFFFFFFF;
   private static final CopyOnWriteArrayList<ShrapnelEvent> EVENTS = new CopyOnWriteArrayList<>();

   private ShrapnelDebugRenderer() {
   }

   public static void onShrapnelReceived(Vec3 center, List<Vec3> rayEnds, List<Boolean> rayHits) {
      long now = System.currentTimeMillis();
      EVENTS.removeIf(e -> now - e.time() > KEEP_MS);
      EVENTS.add(new ShrapnelEvent(center, rayEnds, rayHits, now));
   }

   /** Explosions currently kept for the debug view (for tests). */
   public static int eventCount() {
      return EVENTS.size();
   }

   public static void emit() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || EVENTS.isEmpty() || !mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
         return;
      }

      long now = System.currentTimeMillis();
      EVENTS.removeIf(e -> now - e.time() > KEEP_MS);
      for (ShrapnelEvent event : EVENTS) {
         for (int i = 0; i < event.rayEnds().size(); i++) {
            Vec3 end = event.rayEnds().get(i);
            Gizmos.line(event.center(), end, WHITE);
            if (event.rayHits().get(i)) {
               Gizmos.cuboid(new AABB(end, end).inflate(HIT_BOX), GizmoStyle.stroke(WHITE));
            }
         }
      }
   }

   private record ShrapnelEvent(Vec3 center, List<Vec3> rayEnds, List<Boolean> rayHits, long time) {
   }
}
