package com.fpvdrone.client;

import com.fpvdrone.entity.DroppedBombEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * 1.1.8 G2pNpnR5J: client-side prediction of the pilot's own drops. When the pilot presses drop, the client spawns a
 * local stand-in bomb at once (negative entity id, never sent to the server) and asks the server to drop the real one.
 * When the real bomb arrives, it is matched to the stand-in by (release game tick, station) and takes over its position,
 * velocity and nose, so the pilot sees no delay and no jump.
 * <p>
 * (Not named after the 1.1.8 class: its simple name G2pNpnR5J also appears as an unrelated private class in
 * ProjectileDefinition, so the porting script must not rename it.)
 */
public final class PredictedBombs {
   private static final Map<Long, DroppedBombEntity> v5suI = new ConcurrentHashMap<>();
   private static final AtomicInteger ibzfxic = new AtomicInteger(-1000000);

   private PredictedBombs() {
   }

   /** Adds a locally predicted bomb to the client level. */
   public static void v5suI(DroppedBombEntity var0) {
      if (var0.level() instanceof ClientLevel var1) {
         int var3 = ibzfxic.decrementAndGet();
         var0.setId(var3);
         var1.addEntity(var0);
         ibzfxic(var0);
      }
   }

   private static long v5suI(long var0, int var2) {
      return var0 << 8 | var2 & 255L;
   }

   public static void ibzfxic(DroppedBombEntity var0) {
      v5suI.put(v5suI(var0.vmpmU(), var0.v8ZN()), var0);
   }

   /** Called when the server's bomb is created on this client: adopt the matching stand-in if it is ours. */
   public static void wuhB(DroppedBombEntity var0) {
      if (!var0.zTT3nw6()) {
         Minecraft var1 = Minecraft.getInstance();
         if (var1.player != null && var1.player.getUUID().equals(var0.jGhI2())) {
            DroppedBombEntity var2 = v5suI.remove(v5suI(var0.vmpmU(), var0.v8ZN()));
            if (var2 != null && !var2.isRemoved()) {
               var0.v5suI(var2);
            }
         }
      }
   }
}
