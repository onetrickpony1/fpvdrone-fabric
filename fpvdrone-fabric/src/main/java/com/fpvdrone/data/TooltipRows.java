package com.fpvdrone.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class TooltipRows {
   public static final String v5suI = "tooltip.fpvdrone.required";
   public static final String ibzfxic = "tooltip.fpvdrone.optional";
   public static final String wuhB = "tooltip.fpvdrone.installed";
   private static final String dOfFwvo = "tooltip.fpvdrone.more";

   private TooltipRows() {
   }

   public static List<TooltipRows.KKeMvpuBgEY> v5suI(String var0) {
      return v5suI(DroneDefinitions.ibzfxic(var0).r6s7FL3(), var0);
   }

   static List<TooltipRows.KKeMvpuBgEY> v5suI(TooltipSpec var0, String var1) {
      ArrayList var2 = new ArrayList();
      if (!var0.v5suI()) {
         for (TooltipSpec.Section var4 : var0.sections()) {
            String var5 = !var4.labelKey().isEmpty() ? var4.labelKey() : v5suI(var4.source());

            for (String var7 : v5suI(var4.source(), var1)) {
               var2.add(TooltipRows.KKeMvpuBgEY.v5suI(var7, var5));
            }

            for (String var9 : var4.items()) {
               var2.add(TooltipRows.KKeMvpuBgEY.v5suI(var9, var5));
            }
         }
      } else {
         var2.addAll(ibzfxic(var1));
      }

      return v5suI(v5suI(var2), var0.maxRows());
   }

   private static String v5suI(TooltipSource var0) {
      return switch (var0) {
         case v5suI -> "tooltip.fpvdrone.required";
         case ibzfxic, wuhB, dOfFwvo, rsDd50tfV -> "tooltip.fpvdrone.optional";
      };
   }

   private static List<TooltipRows.KKeMvpuBgEY> ibzfxic(String var0) {
      ArrayList var1 = new ArrayList();
      v5suI(var1, wuhB(var0), "tooltip.fpvdrone.required");
      v5suI(var1, dOfFwvo(var0), "tooltip.fpvdrone.optional");
      ArrayList var2 = new ArrayList<>(rsDd50tfV(var0));

      for (String var4 : uSUb(var0)) {
         if (!var2.contains(var4)) {
            var2.add(var4);
         }
      }

      v5suI(var1, var2, "tooltip.fpvdrone.optional");
      return var1;
   }

   private static List<String> v5suI(TooltipSource var0, String var1) {
      return switch (var0) {
         case v5suI -> wuhB(var1);
         case ibzfxic -> dOfFwvo(var1);
         case wuhB -> rsDd50tfV(var1);
         case dOfFwvo -> uSUb(var1);
         case rsDd50tfV -> List.of();
      };
   }

   private static List<String> wuhB(String var0) {
      ArrayList var1 = new ArrayList();

      for (AttachableEntry var3 : DroneDefinitions.ibzfxic(var0).n9lX8A()) {
         var1.add(BatteryDefinitions.ibzfxic(var3.v5suI()).ibzfxic());
      }

      return var1;
   }

   private static List<String> dOfFwvo(String var0) {
      ArrayList var1 = new ArrayList();

      for (AttachableEntry var3 : DroneDefinitions.ibzfxic(var0).wBXn()) {
         var1.add(ProjectileDefinitions.getOrDefault(var3.v5suI()).itemId());
      }

      return var1;
   }

   private static List<String> rsDd50tfV(String var0) {
      DroneDefinition var1 = DroneDefinitions.ibzfxic(var0);
      if (!var1.jGhI2()) {
         return List.of();
      }

      ArrayList var2 = new ArrayList();

      for (PayloadStation var4 : var1.uKpZYj().stations()) {
         String var5 = var4.v5suI(var1.uKpZYj().projectile());
         var2.add(ProjectileDefinitions.getOrDefault(var5).itemId());
      }

      return var2;
   }

   private static List<String> uSUb(String var0) {
      DroneDefinition var1 = DroneDefinitions.ibzfxic(var0);
      if (!var1.wEJx3Itb()) {
         return List.of();
      }

      ArrayList var2 = new ArrayList();

      for (PayloadStation var4 : var1.jt9oI60ST().rsDd50tfV()) {
         String var5 = var4.v5suI(var1.jt9oI60ST().dOfFwvo());
         var2.add(ProjectileDefinitions.getOrDefault(var5).itemId());
      }

      return var2;
   }

   private static void v5suI(List<TooltipRows.KKeMvpuBgEY> var0, List<String> var1, String var2) {
      for (String var4 : var1) {
         var0.add(TooltipRows.KKeMvpuBgEY.v5suI(var4, var2));
      }
   }

   private static List<TooltipRows.KKeMvpuBgEY> v5suI(List<TooltipRows.KKeMvpuBgEY> var0) {
      ArrayList var1 = new ArrayList();
      LinkedHashSet var2 = new LinkedHashSet();

      for (TooltipRows.KKeMvpuBgEY var4 : var0) {
         if (var2.add(var4.ibzfxic())) {
            var1.add(var4);
         }
      }

      return var1;
   }

   private static List<TooltipRows.KKeMvpuBgEY> v5suI(List<TooltipRows.KKeMvpuBgEY> var0, int var1) {
      if (var0.size() <= var1) {
         return var0;
      }

      int var2 = Math.max(0, var1 - 1);
      ArrayList var3 = new ArrayList(var0.subList(0, var2));
      var3.add(TooltipRows.KKeMvpuBgEY.v5suI(var0.size() - var2));
      return var3;
   }

   public static final class KKeMvpuBgEY {
      private final String v5suI;
      private final String ibzfxic;
      private final int wuhB;

      public KKeMvpuBgEY(String var1, String var2, int var3) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
      }

      public static TooltipRows.KKeMvpuBgEY v5suI(String var0, String var1) {
         return new TooltipRows.KKeMvpuBgEY(var0, var1, 0);
      }

      public static TooltipRows.KKeMvpuBgEY v5suI(int var0) {
         return new TooltipRows.KKeMvpuBgEY("", "tooltip.fpvdrone.more", var0);
      }

      public boolean v5suI() {
         return this.v5suI == null || this.v5suI.isEmpty();
      }

      public String ibzfxic() {
         return this.v5suI;
      }

      public String wuhB() {
         return this.ibzfxic;
      }

      public int dOfFwvo() {
         return this.wuhB;
      }
   }
}
