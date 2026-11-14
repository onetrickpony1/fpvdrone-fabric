package com.fpvdrone.client.map;

public final class MapShading {
   public static final int v5suI = Integer.MIN_VALUE;
   private static final float ibzfxic = 0.2F;
   private static final float wuhB = 0.5F;
   private static final float dOfFwvo = 0.6666667F;

   private MapShading() {
   }

   public static int[] v5suI(int[] var0, int[] var1, int var2, int var3, float var4, float var5, float var6) {
      int[] var7 = new int[var3 * var3];

      for (int var8 = 0; var8 < var3; var8++) {
         for (int var9 = 0; var9 < var3; var9++) {
            int var10 = var0[var8 * var3 + var9];
            if ((var10 >>> 24 & 0xFF) == 0) {
               var7[var8 * var3 + var9] = 0;
            } else {
               int var11 = var1[(var8 + 1) * var2 + var9 + 1];
               int var12 = var1[var8 * var2 + var9 + 1];
               int var13 = var1[var8 * var2 + var9];
               if (var11 == Integer.MIN_VALUE) {
                  var11 = 63;
               }

               if (var12 == Integer.MIN_VALUE) {
                  var12 = var11;
               }

               if (var13 == Integer.MIN_VALUE) {
                  var13 = var11;
               }

               var7[var8 * var3 + var9] = v5suI(var10, var11, var12, var13, var4, var5, var6);
            }
         }
      }

      return var7;
   }

   public static int v5suI(int var0, int var1, int var2, int var3, float var4, float var5, float var6) {
      float var7 = 0.5F + v5suI(var1, var2, var3);
      float var8 = v5suI(var1);
      int var9 = var0 >> 16 & 0xFF;
      int var10 = var0 >> 8 & 0xFF;
      int var11 = var0 & 0xFF;
      int var12 = ibzfxic((int)(var9 * (var4 * 0.2F + var7) * var8));
      int var13 = ibzfxic((int)(var10 * (var5 * 0.2F + var7) * var8));
      int var14 = ibzfxic((int)(var11 * (var6 * 0.2F + var7) * var8));
      return v5suI(255, var12, var13, var14);
   }

   private static float v5suI(int var0, int var1, int var2) {
      int var3 = var0 - var1;
      int var4 = var0 - var2;
      float var5 = 0.0F;
      float var6 = -var3;
      if (var6 < 1.0F) {
         if (var3 == 1 && var4 == 1) {
            var5 = 1.0F;
         } else {
            float var7 = var3 - var4;
            float var8 = 1.0F - var6;
            float var9 = (float)Math.sqrt(var7 * var7 + 1.0F + var6 * var6);
            var5 = (float)(var8 / var9 / Math.sqrt(2.0));
         }
      }

      if (var5 == 1.0F) {
         return 0.6666667F;
      } else {
         return var5 > 0.0F ? (float)Math.ceil(var5 * 10.0F) / 10.0F * 0.6666667F * 0.88388F : 0.0F;
      }
   }

   private static float v5suI(int var0) {
      float var1 = var0 / 63.0F;
      return var1 > 1.0F ? 1.0F : Math.max(0.9F, var1);
   }

   private static int ibzfxic(int var0) {
      return var0 < 0 ? 0 : Math.min(var0, 255);
   }

   private static int v5suI(int var0, int var1, int var2, int var3) {
      return var0 << 24 | var3 << 16 | var2 << 8 | var1;
   }
}
