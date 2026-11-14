package com.fpvdrone.client;

import com.fpvdrone.util.YawPitch;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class AimMath {
   private AimMath() {
   }

   public static AimMath.Ol3TEDSog v5suI(
      double var0,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      double var18,
      double var20,
      double var22,
      double var24,
      double var26,
      double var28,
      int var30,
      int var31,
      double var32
   ) {
      double var34 = var24 - var0;
      double var36 = var26 - var2;
      double var38 = var28 - var4;
      double var40 = var34 * var6 + var36 * var8 + var38 * var10;
      if (var40 <= 1.0E-4) {
         return new AimMath.Ol3TEDSog(0.0F, 0.0F, true);
      }

      double var42 = var34 * var12 + var36 * var14 + var38 * var16;
      double var44 = var34 * var18 + var36 * var20 + var38 * var22;
      double var46 = var31 * 0.5 / Math.tan(Math.toRadians(var32) * 0.5);
      float var48 = (float)(var42 / var40 * var46);
      float var49 = (float)(-var44 / var40 * var46);
      return new AimMath.Ol3TEDSog(var48, var49, false);
   }

   public static AimMath.Ol3TEDSog v5suI(AimMath.Ol3TEDSog var0, int var1, int var2, int var3) {
      float var4 = Math.max(0.0F, var1 / 2.0F - var3);
      float var5 = Math.max(0.0F, var2 / 2.0F - var3);
      return new AimMath.Ol3TEDSog(Mth.clamp(var0.v5suI(), -var4, var4), Mth.clamp(var0.ibzfxic(), -var5, var5), var0.wuhB());
   }

   public static AimMath.KKeMvpuBgEY v5suI(float var0, float var1) {
      Quaternionf var2 = YawPitch.v5suI(var0, var1);
      Vector3f var3 = new Vector3f(0.0F, 0.0F, 1.0F).rotate(var2);
      Vector3f var4 = new Vector3f(0.0F, 1.0F, 0.0F).rotate(var2);
      Vector3f var5 = new Vector3f(1.0F, 0.0F, 0.0F).rotate(var2);
      return new AimMath.KKeMvpuBgEY(var3.x, var3.y, var3.z, var4.x, var4.y, var4.z, -var5.x, -var5.y, -var5.z);
   }

   public static final class KKeMvpuBgEY {
      private final float v5suI;
      private final float ibzfxic;
      private final float wuhB;
      private final float dOfFwvo;
      private final float rsDd50tfV;
      private final float uSUb;
      private final float jGhI2;
      private final float wEJx3Itb;
      private final float bAcdvq9;

      public KKeMvpuBgEY(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var5;
         this.uSUb = var6;
         this.jGhI2 = var7;
         this.wEJx3Itb = var8;
         this.bAcdvq9 = var9;
      }


      public float v5suI() {

         return this.v5suI;

      }

   
      public float ibzfxic() {
         return this.ibzfxic;
      }

      public float wuhB() {
         return this.wuhB;
      }

      public float dOfFwvo() {
         return this.dOfFwvo;
      }

      public float rsDd50tfV() {
         return this.rsDd50tfV;
      }

      public float uSUb() {
         return this.uSUb;
      }

      public float jGhI2() {
         return this.jGhI2;
      }

      public float wEJx3Itb() {
         return this.wEJx3Itb;
      }

      public float bAcdvq9() {
         return this.bAcdvq9;
      }
   }

   public static final class Ol3TEDSog {
      private final float v5suI;
      private final float ibzfxic;
      private final boolean wuhB;

      public Ol3TEDSog(float var1, float var2, boolean var3) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
      }


      public float v5suI() {

         return this.v5suI;

      }

   
      public float ibzfxic() {
         return this.ibzfxic;
      }

      public boolean wuhB() {
         return this.wuhB;
      }
   }
}
