package com.fpvdrone.physics;

import com.fpvdrone.entity.MunitionCollision;

import com.fpvdrone.data.BoneBox;
import com.fpvdrone.data.OrientedBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BoxTerrainCollision {
   private static final double v5suI = 0.08;
   private static final double ibzfxic = 0.45;
   private static final double wuhB = 0.85;
   private static final double dOfFwvo = 0.04;
   private static final double rsDd50tfV = 0.05;
   private static final double uSUb = 0.3;
   private static final double jGhI2 = 0.995;
   private static final int wEJx3Itb = 12;
   private static final double bAcdvq9 = 0.35;
   private static final double c1jHYPb4 = 0.01;
   private static final float v8ZN = 1.0F;
   private static final double zTT3nw6 = Math.toRadians(1.0);
   private static final double vmpmU = 85.0;
   private static final double b69s = 0.004;
   private static final double cXHvq = 0.2;
   private static final int rxYnaa = 12;
   private static final double r6s7FL3 = 0.02;
   private static final double iAKNPAj = 60.0;
   private static final double qlv6NKWnH = 60.0;
   private static final double hOKMYHOaC = 80.0;
   private static final double iS1JnZov = 0.88;
   private static final double lfyfLwuw = 9.0;
   private static final double wyNDWDB = 0.35;
   private static final int iRWc2Pi = 24;
   private static final double tpzIHm = 0.1;
   private static final double eJDY = 2.0;
   private static final Logger zHBAQgZP = LoggerFactory.getLogger("FPVDrone/FP1Solver");
   private static int we1GYY = 0;

   private BoxTerrainCollision() {
   }

   public static void v5suI(Level var0, List<BoneBox> var1, BoxTerrainCollision.Rdl0X5jeod var2) {
      if (var1.isEmpty()) {
         var2.b69s = true;
      } else {
         double var3 = var2.v5suI;
         double var5 = var2.ibzfxic;
         double var7 = var2.wuhB;
         double var9 = var2.dOfFwvo;
         double var11 = var2.rsDd50tfV;
         double var13 = var2.uSUb;
         double var15 = var2.jGhI2;
         double var17 = var2.wEJx3Itb;
         double var19 = var2.bAcdvq9;
         v5suI(var1, var2);
         var2.wEJx3Itb = var2.wEJx3Itb - var2.hOKMYHOaC;
         if (var2.wEJx3Itb < -var2.iS1JnZov) {
            var2.wEJx3Itb = -var2.iS1JnZov;
         }

         double var21 = 0.0;
         double var23 = 0.0;
         if (var2.eJDY > 1.0E-6) {
            double var25 = Math.max(Math.abs(var2.dOfFwvo), Math.abs(var2.rsDd50tfV));
            if (var25 > 60.0) {
               double var27 = Mth.clamp((var25 - 60.0) / 20.0, 0.0, 1.0);
               double var29 = var2.eJDY * var27;
               var21 = -Math.signum(var2.dOfFwvo) * Math.min(var29, Math.abs(var2.dOfFwvo));
               var23 = -Math.signum(var2.rsDd50tfV) * Math.min(var29, Math.abs(var2.rsDd50tfV));
            }
         }

         double var63 = dOfFwvo(var0, var1, var2);
         double var64 = Math.sqrt(var2.jGhI2 * var2.jGhI2 + var2.wEJx3Itb * var2.wEJx3Itb + var2.bAcdvq9 * var2.bAcdvq9);
         double var65 = Math.max(Math.abs(var2.c1jHYPb4), Math.max(Math.abs(var21), Math.abs(var23)));
         double var31 = Math.toRadians(var65) * var63;
         int var33 = Mth.clamp((int)Math.ceil(Math.max(var64, var31) / 0.2), 1, 12);
         double var34 = var2.jGhI2 / var33;
         double var36 = var2.wEJx3Itb / var33;
         double var38 = var2.bAcdvq9 / var33;
         double var40 = var2.c1jHYPb4 / var33;
         double var42 = var21 / var33;
         double var44 = var23 / var33;
         BoxTerrainCollision.KKeMvpuBgEY var46 = new BoxTerrainCollision.KKeMvpuBgEY();

         for (int var47 = 0; var47 < var33; var47++) {
            var2.v5suI += var34;
            var2.ibzfxic += var36;
            var2.wuhB += var38;
            var2.uSUb += var40;
            var2.dOfFwvo = Mth.clamp(var2.dOfFwvo + var42, -85.0, 85.0);
            var2.rsDd50tfV = Mth.clamp(var2.rsDd50tfV + var44, -85.0, 85.0);
            v5suI(var0, var1, var2, var46);
         }

         if (var46.rsDd50tfV) {
            boolean var66 = ibzfxic(var0, var1, var2, var46);
            if (!var66 && rsDd50tfV(var0, var1, var2)) {
               double var48 = var2.v5suI;
               double var50 = var2.ibzfxic;
               double var52 = var2.wuhB;
               double var54 = var2.dOfFwvo;
               double var56 = var2.rsDd50tfV;
               double var58 = var2.uSUb;
               var2.v5suI = var3;
               var2.ibzfxic = var5;
               var2.wuhB = var7;
               var2.dOfFwvo = var9;
               var2.rsDd50tfV = var11;
               var2.uSUb = var13;
               if (rsDd50tfV(var0, var1, var2)) {
                  var2.v5suI = var48;
                  var2.ibzfxic = var50;
                  var2.wuhB = var52;
                  var2.dOfFwvo = var54;
                  var2.rsDd50tfV = var56;
                  var2.uSUb = var58;
                  if (!uSUb(var0, var1, var2) && rsDd50tfV(var0, var1, var2)) {
                  }
               }
            }
         }

         if (var46.rsDd50tfV) {
            double var67 = Math.sqrt(var46.v5suI * var46.v5suI + var46.ibzfxic * var46.ibzfxic + var46.wuhB * var46.wuhB);
            if (var67 > 1.0E-6) {
               double var49 = var46.v5suI / var67;
               double var51 = var46.ibzfxic / var67;
               double var53 = var46.wuhB / var67;
               double var55 = var2.jGhI2 * var49 + var2.wEJx3Itb * var51 + var2.bAcdvq9 * var53;
               if (var55 < 0.0) {
                  var2.jGhI2 -= var49 * var55;
                  var2.wEJx3Itb -= var51 * var55;
                  var2.bAcdvq9 -= var53 * var55;
               }
            }

            var2.v8ZN = var46.dOfFwvo > 0.02;
            if (var2.v8ZN) {
               var2.jGhI2 = var2.jGhI2 * var2.cXHvq;
               var2.bAcdvq9 = var2.bAcdvq9 * var2.cXHvq;
               double var69 = Math.abs(var2.v5suI - var3)
                  + Math.abs(var2.ibzfxic - var5)
                  + Math.abs(var2.wuhB - var7)
                  + 0.02 * (Math.abs(var2.dOfFwvo - var9) + Math.abs(var2.rsDd50tfV - var11) + Math.abs(Mth.wrapDegrees(var2.uSUb - var13)));
               if (Math.sqrt(var15 * var15 + var19 * var19) < var2.lfyfLwuw && Math.abs(var17) < 0.05 && Math.abs(var2.c1jHYPb4) < 0.3 && var69 < 0.004) {
                  double var70 = var2.v5suI;
                  double var71 = var2.ibzfxic;
                  double var72 = var2.wuhB;
                  double var57 = var2.dOfFwvo;
                  double var59 = var2.rsDd50tfV;
                  double var61 = var2.uSUb;
                  var2.v5suI = var3;
                  var2.ibzfxic = var5;
                  var2.wuhB = var7;
                  var2.dOfFwvo = var9;
                  var2.rsDd50tfV = var11;
                  var2.uSUb = var13;
                  if (rsDd50tfV(var0, var1, var2)) {
                     var2.v5suI = var70;
                     var2.ibzfxic = var71;
                     var2.wuhB = var72;
                     var2.dOfFwvo = var57;
                     var2.rsDd50tfV = var59;
                     var2.uSUb = var61;
                  } else {
                     var2.jGhI2 = 0.0;
                     var2.wEJx3Itb = 0.0;
                     var2.bAcdvq9 = 0.0;
                     var2.c1jHYPb4 = 0.0;
                  }
               }
            }
         } else {
            var2.jGhI2 *= 0.995;
            var2.bAcdvq9 *= 0.995;
            var2.v8ZN = false;
         }

         var2.vmpmU = var46.uSUb;
         var2.zTT3nw6 = var46.rsDd50tfV;
         var2.c1jHYPb4 = var2.c1jHYPb4 * var2.wyNDWDB;
         if (var46.uSUb) {
            var2.c1jHYPb4 *= 0.35;
         }

         var2.c1jHYPb4 = Mth.clamp(var2.c1jHYPb4, -var2.iRWc2Pi, var2.iRWc2Pi);
         if (Math.abs(var2.c1jHYPb4) < 0.02) {
            var2.c1jHYPb4 = 0.0;
         }

         double var68 = Math.abs(var2.v5suI - var3)
            + Math.abs(var2.ibzfxic - var5)
            + Math.abs(var2.wuhB - var7)
            + 0.02 * (Math.abs(var2.dOfFwvo - var9) + Math.abs(var2.rsDd50tfV - var11) + Math.abs(Mth.wrapDegrees(var2.uSUb - var13)));
         var2.b69s = var46.rsDd50tfV && var68 < 0.004 && Math.abs(var2.jGhI2) < 0.01 && Math.abs(var2.bAcdvq9) < 0.01 && Math.abs(var2.c1jHYPb4) < 0.05;
         if (++we1GYY % 30 == 0) {
         }
      }
   }

   private static void v5suI(List<BoneBox> var0, BoxTerrainCollision.Rdl0X5jeod var1) {
      Vector3f var2 = MunitionCollision.v5suI();
      var1.qlv6NKWnH = v5suI(var0, var1, var2);
      Quaternionf var3 = MunitionCollision.v5suI((float)var1.uSUb, -((float)var1.dOfFwvo), (float)var1.rsDd50tfV);
      Quaternionf var4 = MunitionCollision.v5suI((float)var1.uSUb, -((float)(var1.dOfFwvo + 1.0)), (float)var1.rsDd50tfV);
      Quaternionf var5 = MunitionCollision.v5suI((float)var1.uSUb, -((float)var1.dOfFwvo), (float)(var1.rsDd50tfV + 1.0));
      Quaternionf var6 = MunitionCollision.v5suI((float)(var1.uSUb + 1.0), -((float)var1.dOfFwvo), (float)var1.rsDd50tfV);
      double var7 = 0.0;

      for (BoneBox var10 : var0) {
         var7 += var10.dOfFwvo();
      }

      if (var7 <= 0.0) {
         var7 = 1.0;
      }

      double var23 = 1.0 / (zTT3nw6 * zTT3nw6);
      double var11 = 0.0;
      double var13 = 0.0;
      double var15 = 0.0;

      for (BoneBox var18 : var0) {
         double var19 = var18.dOfFwvo() / var7;
         Vector3f var21 = new Vector3f(var18.v5suI()).add(var2).sub(var1.qlv6NKWnH);
         Vector3f var22 = var3.transform(var21, new Vector3f());
         var11 += var19 * var4.transform(var21, new Vector3f()).sub(var22).lengthSquared() * var23;
         var13 += var19 * var5.transform(var21, new Vector3f()).sub(var22).lengthSquared() * var23;
         var15 += var19 * var6.transform(var21, new Vector3f()).sub(var22).lengthSquared() * var23;
      }

      var1.rxYnaa = Math.min(1.0 / Math.max(var11, 0.02), 60.0);
      var1.r6s7FL3 = Math.min(1.0 / Math.max(var13, 0.02), 60.0);
      var1.iAKNPAj = Math.min(1.0 / Math.max(var15, 0.02), 60.0);
   }

   private static Vector3f v5suI(List<BoneBox> var0, BoxTerrainCollision.Rdl0X5jeod var1, Vector3f var2) {
      if (var1.tpzIHm != null) {
         return new Vector3f(var1.tpzIHm).add(var2);
      }

      double var3 = 0.0;
      double var5 = 0.0;
      double var7 = 0.0;
      double var9 = 0.0;

      for (BoneBox var12 : var0) {
         double var13 = var12.dOfFwvo();
         var5 += var13 * var12.v5suI().x;
         var7 += var13 * var12.v5suI().y;
         var9 += var13 * var12.v5suI().z;
         var3 += var13;
      }

      return var3 <= 0.0 ? new Vector3f(var2) : new Vector3f((float)(var5 / var3), (float)(var7 / var3), (float)(var9 / var3)).add(var2);
   }

   private static double dOfFwvo(Level var0, List<BoneBox> var1, BoxTerrainCollision.Rdl0X5jeod var2) {
      var var3 = MunitionCollision.v5suI(var1, var2.v5suI, var2.ibzfxic, var2.wuhB, (float)var2.uSUb, (float)var2.dOfFwvo, (float)var2.rsDd50tfV);
      if (var3.isEmpty()) {
         return 1.0;
      }

      double var4 = 0.0;
      double var6 = 0.0;

      for (OrientedBox var9 : var3) {
         var4 += var9.jGhI2().x;
         var6 += var9.jGhI2().z;
      }

      var4 /= var3.size();
      var6 /= var3.size();
      double var22 = 1.0;

      for (OrientedBox var11 : var3) {
         for (Vec3 var15 : var11.rsDd50tfV()) {
            double var16 = var15.x - var4;
            double var18 = var15.z - var6;
            var22 = Math.max(var22, var16 * var16 + var18 * var18);
         }
      }

      return Math.sqrt(var22);
   }

   private static void v5suI(Level var0, List<BoneBox> var1, BoxTerrainCollision.Rdl0X5jeod var2, BoxTerrainCollision.KKeMvpuBgEY var3) {
      double var4 = 1.0;
      double var6 = 1.0 / zTT3nw6;

      for (int var8 = 0; var8 < 12; var8++) {
         var var9 = MunitionCollision.v5suI(var1, var2.v5suI, var2.ibzfxic, var2.wuhB, (float)var2.uSUb, (float)var2.dOfFwvo, (float)var2.rsDd50tfV);
         var var10 = v5suI(var0, var9);
         if (var10.isEmpty()) {
            break;
         }

         var3.rsDd50tfV = true;
         Quaternionf var11 = MunitionCollision.v5suI((float)var2.uSUb, -((float)var2.dOfFwvo), (float)var2.rsDd50tfV);
         Quaternionf var12 = var11.conjugate(new Quaternionf());
         Quaternionf var13 = MunitionCollision.v5suI((float)var2.uSUb, -((float)(var2.dOfFwvo + 1.0)), (float)var2.rsDd50tfV);
         Quaternionf var14 = MunitionCollision.v5suI((float)var2.uSUb, -((float)var2.dOfFwvo), (float)(var2.rsDd50tfV + 1.0));
         Quaternionf var15 = MunitionCollision.v5suI((float)(var2.uSUb + 1.0), -((float)var2.dOfFwvo), (float)var2.rsDd50tfV);
         Vector3f var16 = var11.transform(var2.qlv6NKWnH, new Vector3f());
         Vector3f var17 = new Vector3f((float)var2.v5suI, (float)var2.ibzfxic, (float)var2.wuhB).add(var16);
         Vector3f var18 = var13.transform(var2.qlv6NKWnH, new Vector3f()).sub(var16);
         Vector3f var19 = var14.transform(var2.qlv6NKWnH, new Vector3f()).sub(var16);
         Vector3f var20 = var15.transform(var2.qlv6NKWnH, new Vector3f()).sub(var16);

         for (BoxTerrainCollision.Ol3TEDSog var22 : var10) {
            if (Math.abs(var22.v5suI.y) < 0.5) {
               var3.uSUb = true;
            }

            Vector3f var23 = var12.transform(
               new Vector3f((float)(var22.wuhB.x - var17.x), (float)(var22.wuhB.y - var17.y), (float)(var22.wuhB.z - var17.z))
            );
            Vector3f var24 = var11.transform(var23, new Vector3f());
            Vector3f var25 = var13.transform(var23, new Vector3f()).sub(var24);
            Vector3f var26 = var14.transform(var23, new Vector3f()).sub(var24);
            Vector3f var27 = var15.transform(var23, new Vector3f()).sub(var24);
            double var28 = (var22.v5suI.x * var25.x + var22.v5suI.y * var25.y + var22.v5suI.z * var25.z) * var6;
            double var30 = (var22.v5suI.x * var26.x + var22.v5suI.y * var26.y + var22.v5suI.z * var26.z) * var6;
            double var32 = (var22.v5suI.x * var27.x + var22.v5suI.y * var27.y + var22.v5suI.z * var27.z) * var6;
            double var34 = 1.0 + var2.rxYnaa * var28 * var28 + var2.r6s7FL3 * var30 * var30 + var2.iAKNPAj * var32 * var32;
            if (!(var34 < 1.0E-9)) {
               double var36 = 0.35 * Math.max(var22.ibzfxic - 0.01, 0.0) / var34;
               if (!(var36 <= 0.0)) {
                  double var38 = var22.v5suI.x * var36;
                  double var40 = var22.v5suI.y * var36;
                  double var42 = var22.v5suI.z * var36;
                  var2.v5suI += var38;
                  var2.ibzfxic += var40;
                  var2.wuhB += var42;
                  var17.add((float)var38, (float)var40, (float)var42);
                  var3.v5suI += var38;
                  var3.ibzfxic += var40;
                  var3.wuhB += var42;
                  var3.dOfFwvo += var40;
                  double var44 = Math.toDegrees(var2.rxYnaa * var28 * var36);
                  double var46 = Math.toDegrees(var2.r6s7FL3 * var30 * var36);
                  double var48 = Math.toDegrees(var2.iAKNPAj * var32 * var36);
                  var2.dOfFwvo = Mth.clamp(var2.dOfFwvo + var44, -85.0, 85.0);
                  var2.rsDd50tfV = Mth.clamp(var2.rsDd50tfV + var46, -85.0, 85.0);
                  var2.uSUb += var48;
                  double var50 = var44 * var4;
                  double var52 = var46 * var4;
                  double var54 = var48 * var4;
                  var2.v5suI = var2.v5suI - (var18.x * var50 + var19.x * var52 + var20.x * var54);
                  var2.ibzfxic = var2.ibzfxic - (var18.y * var50 + var19.y * var52 + var20.y * var54);
                  var2.wuhB = var2.wuhB - (var18.z * var50 + var19.z * var52 + var20.z * var54);
               }
            }
         }
      }
   }

   private static boolean ibzfxic(Level var0, List<BoneBox> var1, BoxTerrainCollision.Rdl0X5jeod var2, BoxTerrainCollision.KKeMvpuBgEY var3) {
      for (int var4 = 0; var4 < 24; var4++) {
         var var5 = MunitionCollision.v5suI(var1, var2.v5suI, var2.ibzfxic, var2.wuhB, (float)var2.uSUb, (float)var2.dOfFwvo, (float)var2.rsDd50tfV);
         var var6 = v5suI(var0, var5);
         if (var6.isEmpty()) {
            return true;
         }

         for (BoxTerrainCollision.Ol3TEDSog var8 : var6) {
            double var9 = Math.max(var8.ibzfxic - 0.01, 0.0);
            if (!(var9 <= 0.0)) {
               var2.v5suI = var2.v5suI + var8.v5suI.x * var9;
               var2.ibzfxic = var2.ibzfxic + var8.v5suI.y * var9;
               var2.wuhB = var2.wuhB + var8.v5suI.z * var9;
               var3.v5suI = var3.v5suI + var8.v5suI.x * var9;
               var3.ibzfxic = var3.ibzfxic + var8.v5suI.y * var9;
               var3.wuhB = var3.wuhB + var8.v5suI.z * var9;
               if (var8.v5suI.y > 0.5) {
                  var3.dOfFwvo = var3.dOfFwvo + var8.v5suI.y * var9;
               }

               if (Math.abs(var8.v5suI.y) < 0.5) {
                  var3.uSUb = true;
               }
            }
         }
      }

      return false;
   }

   private static boolean rsDd50tfV(Level var0, List<BoneBox> var1, BoxTerrainCollision.Rdl0X5jeod var2) {
      var var3 = MunitionCollision.v5suI(var1, var2.v5suI, var2.ibzfxic, var2.wuhB, (float)var2.uSUb, (float)var2.dOfFwvo, (float)var2.rsDd50tfV);

      for (BoxTerrainCollision.Ol3TEDSog var5 : v5suI(var0, var3)) {
         if (var5.ibzfxic > 0.01) {
            return true;
         }
      }

      return false;
   }

   public static boolean ibzfxic(Level var0, List<BoneBox> var1, BoxTerrainCollision.Rdl0X5jeod var2) {
      return rsDd50tfV(var0, var1, var2);
   }

   public static boolean wuhB(Level var0, List<BoneBox> var1, BoxTerrainCollision.Rdl0X5jeod var2) {
      if (var1.isEmpty()) {
         return true;
      }

      if (!rsDd50tfV(var0, var1, var2)) {
         return true;
      }

      BoxTerrainCollision.KKeMvpuBgEY var3 = new BoxTerrainCollision.KKeMvpuBgEY();
      return ibzfxic(var0, var1, var2, var3) ? true : uSUb(var0, var1, var2);
   }

   private static boolean uSUb(Level var0, List<BoneBox> var1, BoxTerrainCollision.Rdl0X5jeod var2) {
      double var3 = var2.ibzfxic;

      for (double var5 = 0.0; var5 <= 2.0; var5 += 0.1) {
         if (!rsDd50tfV(var0, var1, var2)) {
            return true;
         }

         var2.ibzfxic += 0.1;
      }

      if (!rsDd50tfV(var0, var1, var2)) {
         return true;
      }

      var2.ibzfxic = var3;
      return false;
   }

   public static boolean v5suI(Level var0, List<BoneBox> var1, double var2, double var4, double var6, float var8, float var9, float var10) {
      var var11 = MunitionCollision.v5suI(var1, var2, var4, var6, var8, var9, var10);

      for (BoxTerrainCollision.Ol3TEDSog var13 : v5suI(var0, var11)) {
         if (Math.abs(var13.v5suI.y) < 0.5) {
            return true;
         }
      }

      return false;
   }

   private static List<BoxTerrainCollision.Ol3TEDSog> v5suI(Level var0, List<OrientedBox> var1) {
      var var2 = new ArrayList();
      MutableBlockPos var3 = new MutableBlockPos();

      for (OrientedBox var5 : var1) {
         AABB var6 = var5.uSUb();
         int var7 = Mth.floor(var6.minX);
         int var8 = Mth.floor(var6.maxX);
         int var9 = Mth.floor(var6.minY);
         int var10 = Mth.floor(var6.maxY);
         int var11 = Mth.floor(var6.minZ);
         int var12 = Mth.floor(var6.maxZ);

         for (int var13 = var7; var13 <= var8; var13++) {
            for (int var14 = var9; var14 <= var10; var14++) {
               for (int var15 = var11; var15 <= var12; var15++) {
                  var3.set(var13, var14, var15);
                  BlockState var16 = var0.getBlockState(var3);
                  if (!var16.isAir()) {
                     VoxelShape var17 = var16.getCollisionShape(var0, var3);
                     if (!var17.isEmpty()) {
                        for (AABB var19 : var17.toAabbs()) {
                           AABB var20 = var19.move(var13, var14, var15);
                           OrientedBox var21 = OrientedBox.v5suI(var20);
                           Vector3f var22 = var5.v5suI(var21);
                           if (var22 != null) {
                              double var23 = var22.length();
                              if (!(var23 < 1.0E-5)) {
                                 Vector3f var25 = new Vector3f(var22).div((float)var23);
                                 Vec3 var26 = new Vec3(var25.x, var25.y, var25.z);
                                 int var27 = var13;
                                 int var28 = var14;
                                 int var29 = var15;
                                 double var30 = Math.abs(var26.x);
                                 double var32 = Math.abs(var26.y);
                                 double var34 = Math.abs(var26.z);
                                 if (var32 >= var30 && var32 >= var34) {
                                    var28 += var26.y > 0.0 ? 1 : -1;
                                 } else if (var30 >= var34) {
                                    var27 += var26.x > 0.0 ? 1 : -1;
                                 } else {
                                    var29 += var26.z > 0.0 ? 1 : -1;
                                 }

                                 var3.set(var27, var28, var29);
                                 BlockState var36 = var0.getBlockState(var3);
                                 if (var36.isAir() || var36.getCollisionShape(var0, var3).isEmpty()) {
                                    Vec3 var37 = var5.ibzfxic(var20.getCenter());
                                    var2.add(new BoxTerrainCollision.Ol3TEDSog(var26, var23, var37));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return var2;
   }

   private static final class KKeMvpuBgEY {
      double v5suI;
      double ibzfxic;
      double wuhB;
      double dOfFwvo;
      boolean rsDd50tfV;
      boolean uSUb;
   }

   private static final class Ol3TEDSog {
      private final Vec3 v5suI;
      private final double ibzfxic;
      private final Vec3 wuhB;

      private Ol3TEDSog(Vec3 var1, double var2, Vec3 var4) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var4;
      }


      public Vec3 v5suI() {

         return this.v5suI;

      }

   
      public double ibzfxic() {
         return this.ibzfxic;
      }

      public Vec3 wuhB() {
         return this.wuhB;
      }
   }

   public static final class Rdl0X5jeod {
      public double v5suI;
      public double ibzfxic;
      public double wuhB;
      public double dOfFwvo;
      public double rsDd50tfV;
      public double uSUb;
      public double jGhI2;
      public double wEJx3Itb;
      public double bAcdvq9;
      public double c1jHYPb4;
      public boolean v8ZN;
      public boolean zTT3nw6;
      public boolean vmpmU;
      public boolean b69s;
      public double cXHvq = 0.85;
      public double rxYnaa = 1.0;
      public double r6s7FL3 = 1.0;
      public double iAKNPAj = 1.0;
      public Vector3f qlv6NKWnH = new Vector3f();
      public double hOKMYHOaC = 0.08;
      public double iS1JnZov = 0.45;
      public double lfyfLwuw = 0.04;
      public double wyNDWDB = 0.88;
      public double iRWc2Pi = 9.0;
      public Vector3f tpzIHm = null;
      public double eJDY = 0.0;

      public Rdl0X5jeod(double var1, double var3, double var5, double var7, double var9, float var11, double var12, double var14, double var16, double var18) {
         this.v5suI = var1;
         this.ibzfxic = var3;
         this.wuhB = var5;
         this.dOfFwvo = var7;
         this.rsDd50tfV = var9;
         this.uSUb = var11;
         this.jGhI2 = var12;
         this.wEJx3Itb = var14;
         this.bAcdvq9 = var16;
         this.c1jHYPb4 = var18;
      }
   }
}
