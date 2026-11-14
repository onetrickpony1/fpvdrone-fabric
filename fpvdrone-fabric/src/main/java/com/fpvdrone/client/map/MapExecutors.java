package com.fpvdrone.client.map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class MapExecutors {
   private static final int v5suI = Runtime.getRuntime().availableProcessors();
   private static final int ibzfxic = v5suI(v5suI / 3, 1, 4);
   private static final int wuhB = v5suI(v5suI / 4, 1, 3);
   private static final ExecutorService dOfFwvo = v5suI("FPVDrone-MapSample", ibzfxic);
   private static final ExecutorService rsDd50tfV = v5suI("FPVDrone-ReconPixels", wuhB);

   private MapExecutors() {
   }

   public static ExecutorService v5suI() {
      return dOfFwvo;
   }

   public static ExecutorService ibzfxic() {
      return rsDd50tfV;
   }

   public static int wuhB() {
      return wuhB;
   }

   private static int v5suI(int var0, int var1, int var2) {
      return var0 < var1 ? var1 : Math.min(var0, var2);
   }

   private static ExecutorService v5suI(String var0, int var1) {
      AtomicInteger var2 = new AtomicInteger();
      ThreadFactory var3 = var2x -> {
         Thread var3x = new Thread(var2x, var0 + "-" + var2.incrementAndGet());
         var3x.setDaemon(true);
         var3x.setPriority(Math.max(1, 3));
         return var3x;
      };
      return Executors.newFixedThreadPool(var1, var3);
   }
}
