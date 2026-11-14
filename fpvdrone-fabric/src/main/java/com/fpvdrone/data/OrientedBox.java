package com.fpvdrone.data;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class OrientedBox {
   private final Vec3 v5suI;
   private final Vector3f ibzfxic;
   private final Quaternionf wuhB;
   private static final float dOfFwvo = 1.0E-6F;

   public OrientedBox(Vec3 var1, Vector3f var2, Quaternionf var3) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
   }

   public Vector3f v5suI() {
      return this.wuhB.transform(new Vector3f(1.0F, 0.0F, 0.0F));
   }

   public Vector3f ibzfxic() {
      return this.wuhB.transform(new Vector3f(0.0F, 1.0F, 0.0F));
   }

   public Vector3f wuhB() {
      return this.wuhB.transform(new Vector3f(0.0F, 0.0F, 1.0F));
   }

   public Vector3f[] dOfFwvo() {
      return new Vector3f[]{this.v5suI(), this.ibzfxic(), this.wuhB()};
   }

   public Vec3[] rsDd50tfV() {
      Vector3f var1 = this.v5suI().mul(this.ibzfxic.x);
      Vector3f var2 = this.ibzfxic().mul(this.ibzfxic.y);
      Vector3f var3 = this.wuhB().mul(this.ibzfxic.z);
      Vec3[] var4 = new Vec3[8];
      int var5 = 0;

      for (byte var6 = -1; var6 <= 1; var6 += 2) {
         for (byte var7 = -1; var7 <= 1; var7 += 2) {
            for (byte var8 = -1; var8 <= 1; var8 += 2) {
               var4[var5++] = this.v5suI
                  .add(
                     var1.x * var6 + var2.x * var7 + var3.x * var8,
                     var1.y * var6 + var2.y * var7 + var3.y * var8,
                     var1.z * var6 + var2.z * var7 + var3.z * var8
                  );
            }
         }
      }

      return var4;
   }

   public AABB uSUb() {
      Vec3[] var1 = this.rsDd50tfV();
      double var2 = var1[0].x;
      double var4 = var1[0].y;
      double var6 = var1[0].z;
      double var8 = var2;
      double var10 = var4;
      double var12 = var6;

      for (int var14 = 1; var14 < var1.length; var14++) {
         var2 = Math.min(var2, var1[var14].x);
         var8 = Math.max(var8, var1[var14].x);
         var4 = Math.min(var4, var1[var14].y);
         var10 = Math.max(var10, var1[var14].y);
         var6 = Math.min(var6, var1[var14].z);
         var12 = Math.max(var12, var1[var14].z);
      }

      return new AABB(var2, var4, var6, var8, var10, var12);
   }

   public Vec3 v5suI(Vec3 var1, Vec3 var2) {
      Quaternionf var3 = this.wuhB.conjugate(new Quaternionf());
      Vector3f var4 = var3.transform(
         new Vector3f((float)(var1.x - this.v5suI.x), (float)(var1.y - this.v5suI.y), (float)(var1.z - this.v5suI.z))
      );
      Vector3f var5 = var3.transform(
         new Vector3f((float)(var2.x - var1.x), (float)(var2.y - var1.y), (float)(var2.z - var1.z))
      );
      float var6 = 0.0F;
      float var7 = 1.0F;
      float[] var8 = new float[]{var4.x, var4.y, var4.z};
      float[] var9 = new float[]{var5.x, var5.y, var5.z};
      float[] var10 = new float[]{this.ibzfxic.x, this.ibzfxic.y, this.ibzfxic.z};

      for (int var11 = 0; var11 < 3; var11++) {
         if (Math.abs(var9[var11]) < 1.0E-6F) {
            if (var8[var11] < -var10[var11] || var8[var11] > var10[var11]) {
               return null;
            }
         } else {
            float var12 = 1.0F / var9[var11];
            float var13 = (-var10[var11] - var8[var11]) * var12;
            float var14 = (var10[var11] - var8[var11]) * var12;
            if (var13 > var14) {
               float var15 = var13;
               var13 = var14;
               var14 = var15;
            }

            var6 = Math.max(var6, var13);
            var7 = Math.min(var7, var14);
            if (var6 > var7) {
               return null;
            }
         }
      }

      double var16 = var6;
      return var1.add((var2.x - var1.x) * var16, (var2.y - var1.y) * var16, (var2.z - var1.z) * var16);
   }

   public boolean ibzfxic(Vec3 var1, Vec3 var2) {
      return this.v5suI(var1, var2) != null;
   }

   public OrientedBox v5suI(float var1) {
      return new OrientedBox(this.v5suI, new Vector3f(this.ibzfxic.x + var1, this.ibzfxic.y + var1, this.ibzfxic.z + var1), this.wuhB);
   }

   public OrientedBox v5suI(Vec3 var1) {
      return new OrientedBox(this.v5suI.add(var1), this.ibzfxic, this.wuhB);
   }

   public Vec3 ibzfxic(Vec3 var1) {
      Quaternionf var2 = this.wuhB.conjugate(new Quaternionf());
      Vector3f var3 = var2.transform(
         new Vector3f((float)(var1.x - this.v5suI.x), (float)(var1.y - this.v5suI.y), (float)(var1.z - this.v5suI.z))
      );
      var3.x = Math.max(-this.ibzfxic.x, Math.min(this.ibzfxic.x, var3.x));
      var3.y = Math.max(-this.ibzfxic.y, Math.min(this.ibzfxic.y, var3.y));
      var3.z = Math.max(-this.ibzfxic.z, Math.min(this.ibzfxic.z, var3.z));
      Vector3f var4 = this.wuhB.transform(var3, new Vector3f());
      return this.v5suI.add(var4.x, var4.y, var4.z);
   }

   public boolean wuhB(Vec3 var1) {
      Quaternionf var2 = this.wuhB.conjugate(new Quaternionf());
      Vector3f var3 = var2.transform(
         new Vector3f((float)(var1.x - this.v5suI.x), (float)(var1.y - this.v5suI.y), (float)(var1.z - this.v5suI.z))
      );
      return Math.abs(var3.x) <= this.ibzfxic.x && Math.abs(var3.y) <= this.ibzfxic.y && Math.abs(var3.z) <= this.ibzfxic.z;
   }

   public static OrientedBox v5suI(AABB var0) {
      return new OrientedBox(
         var0.getCenter(), new Vector3f((float)(var0.getXsize() * 0.5), (float)(var0.getYsize() * 0.5), (float)(var0.getZsize() * 0.5)), new Quaternionf()
      );
   }

   public Vector3f v5suI(OrientedBox var1) {
      Vector3f[] var2 = this.dOfFwvo();
      Vector3f[] var3 = var1.dOfFwvo();
      Vector3f var4 = new Vector3f(
         (float)(var1.v5suI.x - this.v5suI.x),
         (float)(var1.v5suI.y - this.v5suI.y),
         (float)(var1.v5suI.z - this.v5suI.z)
      );
      float[] var5 = new float[]{this.ibzfxic.x, this.ibzfxic.y, this.ibzfxic.z};
      float[] var6 = new float[]{var1.ibzfxic.x, var1.ibzfxic.y, var1.ibzfxic.z};
      float var7 = Float.MAX_VALUE;
      Vector3f var8 = null;
      Vector3f[] var9 = new Vector3f[15];
      int var10 = 0;

      for (Vector3f var14 : var2) {
         var9[var10++] = new Vector3f(var14);
      }

      for (Vector3f var29 : var3) {
         var9[var10++] = new Vector3f(var29);
      }

      for (Vector3f var30 : var2) {
         for (Vector3f var18 : var3) {
            Vector3f var19 = new Vector3f(var30).cross(var18);
            var9[var10++] = var19;
         }
      }

      for (Vector3f var31 : var9) {
         float var32 = var31.length();
         if (!(var32 < 1.0E-6F)) {
            var31.div(var32);
            float var33 = var5[0] * Math.abs(var2[0].dot(var31)) + var5[1] * Math.abs(var2[1].dot(var31)) + var5[2] * Math.abs(var2[2].dot(var31));
            float var34 = var6[0] * Math.abs(var3[0].dot(var31)) + var6[1] * Math.abs(var3[1].dot(var31)) + var6[2] * Math.abs(var3[2].dot(var31));
            float var35 = Math.abs(var4.dot(var31));
            float var36 = var33 + var34 - var35;
            if (var36 <= 0.0F) {
               return null;
            }

            if (var36 < var7) {
               var7 = var36;
               var8 = new Vector3f(var31);
               if (var8.dot(var4) > 0.0F) {
                  var8.mul(-1.0F);
               }
            }
         }
      }

      return var8 == null ? null : var8.mul(var7);
   }

   public boolean ibzfxic(OrientedBox var1) {
      return this.v5suI(var1) != null;
   }

   public Vec3 jGhI2() {
      return this.v5suI;
   }

   public Vector3f wEJx3Itb() {
      return this.ibzfxic;
   }

   public Quaternionf bAcdvq9() {
      return this.wuhB;
   }
}
