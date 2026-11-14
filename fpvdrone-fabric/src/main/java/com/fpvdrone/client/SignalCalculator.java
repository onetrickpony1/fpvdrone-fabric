package com.fpvdrone.client;

import com.fpvdrone.FpvConfig;
import com.fpvdrone.data.DroneDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SignalCalculator {
   private static final double v5suI = 4.0;
   private static final float ibzfxic = 2.0F;
   private static Level cachedLevel = null;
   private static Vec3 dOfFwvo = null;
   private static Vec3 cachedPilotPos = null;
   private static float cachedSignal = 1.0F;
   private static float smoothedSignal = 1.0F;
   private static long cachedTick = -1L;
   private static final int bAcdvq9 = 20;
   private static final float c1jHYPb4 = 0.01F;
   private static int zeroSignalCounter = 0;
   private static float cachedObstaclePenalty = 0.0F;
   private static long obstacleCacheTick = -1L;
   private static final int b69s = 40;
   private static int cachedGroundY = -1;
   private static BlockPos rxYnaa = null;
   private static long groundCacheTick = -1L;
   private static final int iAKNPAj = 60;

   public static void setLevel(Level var0) {
      cachedLevel = var0;
   }

   public static double calculateDistance(Vec3 var0, Vec3 var1) {
      if (var0 != null && var1 != null) {
         double var2 = var0.x - var1.x;
         double var4 = var0.y - var1.y;
         double var6 = var0.z - var1.z;
         double var8 = Math.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
         return Double.isNaN(var8) ? 0.0 : var8;
      } else {
         return 0.0;
      }
   }

   public static float v5suI(Vec3 var0, Vec3 var1, Level var2, DroneDefinition.Signal var3) {
      if (var0 != null && var1 != null && var2 != null) {
         long var4 = var2.getGameTime();
         if (cachedTick >= 0L && var4 - cachedTick < 20L && dOfFwvo != null && cachedPilotPos != null) {
            boolean var6 = dOfFwvo.distanceToSqr(var0) >= 9.0 || cachedPilotPos.distanceToSqr(var1) >= 9.0;
            if (!var6 && cachedSignal > 0.01F) {
               return cachedSignal;
            }
         }

         cachedLevel = var2;
         dOfFwvo = var0;
         cachedPilotPos = var1;
         cachedTick = var4;
         double var21 = calculateDistance(var0, var1);
         double var8 = var3.maxRange();
         float var10 = 1.0F - (float)Math.min(1.0, var21 / var8);
         float var11;
         if (obstacleCacheTick >= 0L && var4 - obstacleCacheTick < 40L) {
            var11 = cachedObstaclePenalty;
         } else {
            var11 = calculateObstaclePenalty(var0, var1, var2);
            cachedObstaclePenalty = var11;
            obstacleCacheTick = var4;
         }

         float var12 = 1.0F / (1.0F + var11 * 0.03F * var3.obstaclePenaltyMultiplier());
         float var13 = (float)Math.min(var0.y, var1.y);
         float var14 = Math.max(0.0F, (var13 - var3.heightBaseline()) * var3.heightBonusPerBlock() * 0.01F);
         float var15 = 1.0F + var14;
         boolean var16 = var11 < 1.0F;
         float var17 = var16 ? 1.1F : 1.0F;
         float var18 = v5suI(var0, var2, var21, var4, var3);
         float var19 = 1.0F - var18 / 100.0F;
         float var20 = var10 * var12 * var15 * var17 * var19;
         var20 = Math.max(0.0F, Math.min(1.0F, var20));
         smoothedSignal = smoothedSignal + (var20 - smoothedSignal) * FpvConfig.getSignalSmoothing();
         if (var20 <= 0.01F) {
            zeroSignalCounter++;
         } else {
            zeroSignalCounter = 0;
         }

         if (zeroSignalCounter >= FpvConfig.getZeroSignalFramesForDisconnect()) {
            cachedSignal = 0.0F;
         } else {
            cachedSignal = Math.max(0.01F, smoothedSignal);
         }

         return cachedSignal;
      } else {
         return 1.0F;
      }
   }

   private static float v5suI(Vec3 var0, Level var1, double var2, long var4, DroneDefinition.Signal var6) {
      float var7 = var6.tubes();
      if (var2 < var7) {
         return 0.0F;
      }

      BlockPos var8 = new BlockPos((int)var0.x, (int)var0.y, (int)var0.z);
      int var9;
      if (rxYnaa != null
         && groundCacheTick >= 0L
         && var4 - groundCacheTick < 60L
         && Math.abs(rxYnaa.getX() - var8.getX()) < 8
         && Math.abs(rxYnaa.getZ() - var8.getZ()) < 8) {
         var9 = cachedGroundY;
      } else {
         var9 = findGroundLevel(var1, var8);
         cachedGroundY = var9;
         rxYnaa = var8;
         groundCacheTick = var4;
      }

      float var10 = (float)(var0.y - var9);
      float var11 = var6.mounts();
      if (var10 >= var11) {
         return 0.0F;
      }

      float var12 = (float)Math.min(1.0, (var2 - var7) / 150.0);
      float var13 = 1.0F - var10 / var11;
      var13 *= var13;
      return var6.projectile() * var12 * var13;
   }

   private static int findGroundLevel(Level var0, BlockPos var1) {
      MutableBlockPos var2 = new MutableBlockPos(var1.getX(), var1.getY(), var1.getZ());
      int var3 = Math.max(var0.getMinY(), var1.getY() - 64);

      for (int var4 = var1.getY(); var4 > var3; var4 -= 2) {
         var2.setY(var4);
         if (!var0.isLoaded(var2)) {
            return var1.getY() - 5;
         }

         BlockState var5 = var0.getBlockState(var2);
         if (var5.isSolidRender()) {
            return var4;
         }
      }

      return var3;
   }

   private static float calculateObstaclePenalty(Vec3 var0, Vec3 var1, Level var2) {
      float var3 = 0.0F;
      Vec3 var4 = var1.subtract(var0).normalize();
      double var5 = var0.distanceTo(var1);
      int var7 = (int)(var5 / 4.0);
      byte var8 = 50;
      int var9 = var7 > var8 ? var7 / var8 : 1;

      for (int var10 = 1; var10 < var7; var10 += var9) {
         Vec3 var11 = var0.add(var4.scale(var10 * 4.0));
         BlockPos var12 = new BlockPos((int)var11.x, (int)var11.y, (int)var11.z);
         if (!var2.isLoaded(var12)) {
            var3 += 2.0F;
         } else {
            BlockState var13 = var2.getBlockState(var12);
            float var14 = BlockAttenuationRegistry.getAttenuation(var13);
            if (var14 > 0.0F) {
               var3 += var14;
            }
         }
      }

      return var3;
   }

   public static float v5suI(double var0, DroneDefinition.Signal var2) {
      double var3 = var2.freezeStartDistance();
      double var5 = var2.maxRange();
      if (var0 < var3) {
         return 1.0F;
      }

      if (var0 >= var5) {
         return 0.0F;
      }

      double var7 = var5 - var3;
      float var9 = (float)(1.0 - (var0 - var3) / var7);
      return Math.max(0.0F, Math.min(1.0F, var9));
   }

   public static void invalidateCache() {
      cachedTick = -1L;
      dOfFwvo = null;
      cachedPilotPos = null;
      cachedSignal = 1.0F;
      smoothedSignal = 1.0F;
      zeroSignalCounter = 0;
      obstacleCacheTick = -1L;
      cachedObstaclePenalty = 0.0F;
      groundCacheTick = -1L;
      rxYnaa = null;
      cachedGroundY = -1;
   }
}
