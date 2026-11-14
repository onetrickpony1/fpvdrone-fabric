package com.fpvdrone.client;

import com.fpvdrone.sync.RemoteSmoother;
import com.fpvdrone.sync.RemoteSmoothed;
import com.fpvdrone.sync.RemoteSmoothedSimple;

import com.fpvdrone.sync.SmoothingMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Client side of 1.1.8's long-range sync: steps the smoothers of remote aircraft every frame. */
public final class RemoteSmoothingTicker {
   private static long v5suI = 0L;

   private RemoteSmoothingTicker() {
   }

   /** End of each client tick (Forge ClientTickEvent END). */
   public static void clientTick() {
      {
         Minecraft var1 = Minecraft.getInstance();
         if (var1.level != null && !var1.isPaused()) {
            RemoteSmoothingTicker.KKeMvpuBgEY var2 = v5suI(var1);

            for (RemoteSmoothed var4 : var2.v5suI) {
               if (!v5suI(var4)) {
                  var4.a_();
               }
            }

            for (RemoteSmoothedSimple var6 : var2.ibzfxic) {
               if (!v5suI(var6)) {
                  var6.iAKNPAj();
               }
            }
         }
      }
   }

   /** Start of each rendered frame, after the mod's other render-tick work (Forge RenderTickEvent START, LOW). */
   public static void renderTick() {
      {
         Minecraft var1 = Minecraft.getInstance();
         long var2 = System.nanoTime();
         double var4 = v5suI == 0L ? 0.0 : (var2 - v5suI) / 1.0E9;
         v5suI = var2;
         if (var1.level != null && !var1.isPaused()) {
            RemoteSmoothingTicker.KKeMvpuBgEY var6 = v5suI(var1);

            for (RemoteSmoothed var8 : var6.v5suI) {
               if (!v5suI(var8)) {
                  RemoteSmoother var9 = var8.cXHvq();
                  if (var9.v5suI()) {
                     if (!var8.hOKMYHOaC()) {
                        var9.ibzfxic();
                     } else {
                        var9.v5suI(var8.iAKNPAj());
                        var9.ibzfxic(var8.qlv6NKWnH());
                        if (var9.v5suI(var4, var8.rxYnaa())) {
                           var8.v5suI(var9.wuhB(), true);
                        }
                     }
                  }
               }
            }

            for (RemoteSmoothedSimple var11 : var6.ibzfxic) {
               if (!v5suI(var11)) {
                  var11.ibzfxic(var4);
               }
            }
         }
      }
   }

   private static boolean v5suI(Object var0) {
      return var0 instanceof Entity var1 && var1.isRemoved();
   }

   private static RemoteSmoothingTicker.KKeMvpuBgEY v5suI(Minecraft var0) {
      RemoteSmoothingTicker.KKeMvpuBgEY var1 = new RemoteSmoothingTicker.KKeMvpuBgEY();

      for (Entity var3 : var0.level.entitiesForRendering()) {
         if (!var3.isRemoved()) {
            if (var3 instanceof RemoteSmoothed var4 && var4.b69s() == SmoothingMode.REMOTE) {
               var1.v5suI.add(var4);
            } else if (var3 instanceof RemoteSmoothedSimple var5) {
               var1.ibzfxic.add(var5);
            }
         }
      }

      return var1;
   }

   private static final class KKeMvpuBgEY {
      final List<RemoteSmoothed> v5suI = new ArrayList<>();
      final List<RemoteSmoothedSimple> ibzfxic = new ArrayList<>();
   }
}
