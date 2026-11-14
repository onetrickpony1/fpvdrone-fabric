package com.fpvdrone.entity;

import com.fpvdrone.data.GeoBoneBoxes;

import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.BoneBox;
import com.fpvdrone.data.OrientedBox;
import com.fpvdrone.entity.LoiteringMunitionEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MunitionCollision {
   public static final float v5suI = 0.0F;
   public static final float ibzfxic = 4.0F;
   public static final float wuhB = 11.0F;
   public static final float dOfFwvo = 1.0F;
   private static final float rsDd50tfV = 0.01F;
   private static final Map<String, List<BoneBox>> uSUb = new ConcurrentHashMap<>();
   private static final double jGhI2 = 0.15;
   private static final double wEJx3Itb = 2.0;
   private static final int bAcdvq9 = 4;

   private MunitionCollision() {
   }

   public static Quaternionf v5suI(float var0, float var1, float var2) {
      double var3 = Math.toRadians(var0);
      double var5 = Math.toRadians(-var1);
      double var7 = Math.cos(var5);
      Vector3f var9 = new Vector3f((float)(-Math.sin(var3) * var7), (float)Math.sin(var5), (float)(Math.cos(var3) * var7));
      var9.normalize();
      Vector3f var10 = new Vector3f(0.0F, 1.0F, 0.0F).cross(var9);
      if (var10.lengthSquared() < 1.0E-6F) {
         var10.set(1.0F, 0.0F, 0.0F);
      }

      var10.normalize();
      Vector3f var11 = new Vector3f(var9).cross(var10).normalize();
      float var12 = (float)Math.toRadians(var2 * 1.0F);
      float var13 = Mth.cos(var12);
      float var14 = Mth.sin(var12);
      Vector3f var15 = new Vector3f(var10).mul(var13).add(new Vector3f(var11).mul(var14));
      Vector3f var16 = new Vector3f(var11).mul(var13).sub(new Vector3f(var10).mul(var14));
      Matrix3f var17 = new Matrix3f();
      var17.m00(-var15.x());
      var17.m01(-var15.y());
      var17.m02(-var15.z());
      var17.m10(var16.x());
      var17.m11(var16.y());
      var17.m12(var16.z());
      var17.m20(-var9.x());
      var17.m21(-var9.y());
      var17.m22(-var9.z());
      return new Quaternionf().setFromNormalized(var17);
   }

   public static Vector3f v5suI() {
      return new Vector3f(0.0F, -0.24F, -0.6875F);
   }

   public static Vector3f v5suI(float[] var0) {
      return var0 != null && var0.length >= 3 ? new Vector3f(-var0[0] / 16.0F, var0[1] / 16.0F, var0[2] / 16.0F) : null;
   }

   public static List<BoneBox> v5suI(LoiteringMunitionEntity var0) {
      DroneDefinition var1 = var0.uQdlyiwB();
      String var2 = var1.hOKMYHOaC();
      boolean var3 = var0.vmpmU();
      var var4 = var3 ? GeoBoneBoxes.wuhB(var2) : GeoBoneBoxes.ibzfxic(var2);
      Map<String, Double> var5 = var1.pyHY5() != null ? var1.pyHY5().wuhB() : Map.of();
      if (!var4.isEmpty() && !var5.isEmpty()) {
         String var6 = var2 + "|" + var3 + "|" + var5;
         return uSUb.computeIfAbsent(var6, var2x -> v5suI(var4, var5));
      } else {
         return var4;
      }
   }

   private static List<BoneBox> v5suI(List<BoneBox> var0, Map<String, Double> var1) {
      var var2 = new ArrayList(var0.size());

      for (BoneBox var4 : var0) {
         Double var5 = (Double)var1.get(var4.rsDd50tfV());
         var2.add(var5 != null ? var4.v5suI(var5) : var4);
      }

      return List.copyOf(var2);
   }

   public static void ibzfxic() {
      uSUb.clear();
   }

   public static boolean ibzfxic(LoiteringMunitionEntity var0) {
      return GeoBoneBoxes.v5suI(var0.uQdlyiwB().hOKMYHOaC());
   }

   public static List<OrientedBox> v5suI(List<BoneBox> var0, double var1, double var3, double var5, float var7, float var8, float var9) {
      if (var0.isEmpty()) {
         return List.of();
      }

      Quaternionf var10 = v5suI(var7, -var8, var9);
      Vector3f var11 = v5suI();
      Vec3 var12 = new Vec3(var1, var3, var5);
      var var13 = new ArrayList(var0.size());

      for (BoneBox var15 : var0) {
         var13.add(var15.v5suI(var12, var10, var11));
      }

      return var13;
   }

   public static List<OrientedBox> v5suI(LoiteringMunitionEntity var0, float var1) {
      return v5suI(v5suI(var0), var0, var1);
   }

   public static List<OrientedBox> v5suI(List<BoneBox> var0, LoiteringMunitionEntity var1, float var2) {
      if (var0.isEmpty()) {
         return List.of();
      }

      float var3 = Mth.rotLerp(var2, var1.yRotO, var1.getYRot());
      float var4 = Mth.lerp(var2, var1.xRotO, var1.getXRot());
      float var5 = var1.level().isClientSide() ? var1.ibzfxic(var2) : var1.bAcdvq9();
      Quaternionf var6 = v5suI(var3, var4, var5);
      Vector3f var7 = v5suI();
      double var8 = Mth.lerp(var2, var1.xOld, var1.getX());
      double var10 = Mth.lerp(var2, var1.yOld, var1.getY());
      double var12 = Mth.lerp(var2, var1.zOld, var1.getZ());
      Vec3 var14 = new Vec3(var8, var10, var12);
      var var15 = new ArrayList(var0.size());

      for (BoneBox var17 : var0) {
         var15.add(var17.v5suI(var14, var6, var7));
      }

      return var15;
   }

   public static AABB ibzfxic(LoiteringMunitionEntity var0, float var1) {
      var var2 = v5suI(var0, var1);
      if (var2.isEmpty()) {
         return null;
      }

      AABB var3 = ((OrientedBox)var2.get(0)).uSUb();

      for (int var4 = 1; var4 < var2.size(); var4++) {
         var3 = var3.minmax(((OrientedBox)var2.get(var4)).uSUb());
      }

      return var3;
   }

   public static boolean wuhB(LoiteringMunitionEntity var0, float var1) {
      return v5suI(var0, var1, 0.0F);
   }

   public static boolean v5suI(LoiteringMunitionEntity var0, float var1, float var2) {
      var var3 = v5suI(var0, var1);
      if (var2 > 0.0F) {
         var var4 = new ArrayList(var3.size());

         for (OrientedBox var6 : var3) {
            var4.add(var6.v5suI(var2));
         }

         var3 = var4;
      }

      return v5suI(var0.level(), var3);
   }

   public static boolean ibzfxic(LoiteringMunitionEntity var0, float var1, float var2) {
      var var3 = v5suI(var0, var1);
      if (!var3.isEmpty() && !(var2 <= 0.0F)) {
         Vec3 var4 = var0.getDeltaMovement();
         double var5 = var4.length();
         Vec3 var7;
         if (var5 > 0.001) {
            var7 = var4.scale(1.0 / var5);
         } else {
            float var8 = (float)Math.toRadians(var0.getYRot());
            float var9 = (float)Math.toRadians(-var0.getXRot());
            float var10 = Mth.cos(var9);
            var7 = new Vec3(-Mth.sin(var8) * var10, Mth.sin(var9), Mth.cos(var8) * var10);
         }

         Vec3 var14 = var7.scale(var2);
         float var15 = 0.15F;
         var var16 = new ArrayList(var3.size());

         for (OrientedBox var12 : var3) {
            OrientedBox var13 = var15 > 0.0F ? var12.v5suI(var15) : var12;
            var16.add(var13.v5suI(var14));
         }

         return v5suI(var0.level(), var16);
      } else {
         return false;
      }
   }

   public static boolean v5suI(Level var0, List<OrientedBox> var1) {
      if (var1.isEmpty()) {
         return false;
      }

      MutableBlockPos var2 = new MutableBlockPos();

      for (OrientedBox var4 : var1) {
         AABB var5 = var4.uSUb();
         int var6 = Mth.floor(var5.minX);
         int var7 = Mth.floor(var5.maxX);
         int var8 = Mth.floor(var5.minY);
         int var9 = Mth.floor(var5.maxY);
         int var10 = Mth.floor(var5.minZ);
         int var11 = Mth.floor(var5.maxZ);

         for (int var12 = var6; var12 <= var7; var12++) {
            for (int var13 = var8; var13 <= var9; var13++) {
               for (int var14 = var10; var14 <= var11; var14++) {
                  var2.set(var12, var13, var14);
                  BlockState var15 = var0.getBlockState(var2);
                  if (!var15.isAir()) {
                     VoxelShape var16 = var15.getCollisionShape(var0, var2);
                     if (!var16.isEmpty()) {
                        for (AABB var18 : var16.toAabbs()) {
                           if (var4.ibzfxic(OrientedBox.v5suI(var18.move(var12, var13, var14)))) {
                              return true;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   public static double v5suI(Level var0, List<BoneBox> var1, double var2, double var4, double var6, float var8) {
      if (var1.isEmpty()) {
         return 0.0;
      }

      var var9 = v5suI(var1, var2, var4, var6, var8, 0.0F, 0.0F);
      if (var9.isEmpty()) {
         return 0.0;
      }

      double var10 = Double.POSITIVE_INFINITY;

      for (OrientedBox var13 : var9) {
         var10 = Math.min(var10, var13.uSUb().minY);
      }

      double var23 = Double.POSITIVE_INFINITY;
      double var14 = Double.NEGATIVE_INFINITY;
      double var16 = Double.POSITIVE_INFINITY;
      double var18 = Double.NEGATIVE_INFINITY;

      for (OrientedBox var21 : var9) {
         AABB var22 = var21.uSUb();
         if (var22.minY <= var10 + 0.15) {
            var23 = Math.min(var23, var22.minX);
            var14 = Math.max(var14, var22.maxX);
            var16 = Math.min(var16, var22.minZ);
            var18 = Math.max(var18, var22.maxZ);
         }
      }

      double var24 = v5suI(var0, var23, var14, var16, var18, var10);
      return Double.isNaN(var24) ? 0.0 : var24 - var10;
   }

   private static double v5suI(Level var0, double var1, double var3, double var5, double var7, double var9) {
      int var11 = Mth.floor(var1);
      int var12 = Mth.floor(var3);
      int var13 = Mth.floor(var5);
      int var14 = Mth.floor(var7);
      int var15 = Mth.floor(var9 + 2.0);
      int var16 = Mth.floor(var9) - 4;
      double var17 = var9 + 2.0 + 1.0E-4;
      double var19 = Double.NaN;
      MutableBlockPos var21 = new MutableBlockPos();

      for (int var22 = var11; var22 <= var12; var22++) {
         for (int var23 = var13; var23 <= var14; var23++) {
            for (int var24 = var15; var24 >= var16; var24--) {
               var21.set(var22, var24, var23);
               BlockState var25 = var0.getBlockState(var21);
               if (!var25.isAir()) {
                  VoxelShape var26 = var25.getCollisionShape(var0, var21);
                  if (!var26.isEmpty()) {
                     double var27 = var24 + var26.bounds().maxY;
                     if (!(var27 > var17)) {
                        if (Double.isNaN(var19) || var27 > var19) {
                           var19 = var27;
                        }
                        break;
                     }
                  }
               }
            }
         }
      }

      return var19;
   }
}
