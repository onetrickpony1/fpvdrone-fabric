package com.fpvdrone.client;

import com.fpvdrone.render.CameraZoom;
import com.fpvdrone.client.thermal.ThermalCameraState;

import com.fpvdrone.FPVClientMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.client.SignalManager;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.client.AimMath;
import com.fpvdrone.entity.LoiteringMunitionEntity;
import com.fpvdrone.gui.screen.AimScreen;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.MunitionControlPacket;
import com.fpvdrone.network.ExitMunitionViewPacket;
import com.fpvdrone.network.MunitionStickPacket;
import com.fpvdrone.network.EnterMunitionViewPacket;
import com.fpvdrone.render.GogglesHudOverlay;
import com.fpvdrone.video.FrozenFrameManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class MunitionClientState {
   private static int v5suI = -1;
   private static boolean ibzfxic = false;
   private static UUID wuhB = null;
   private static double dOfFwvo;
   private static double rsDd50tfV;
   private static double uSUb;
   private static float jGhI2;
   private static float wEJx3Itb;
   private static final int bAcdvq9 = 100;
   private static int c1jHYPb4 = 0;
   private static boolean v8ZN = false;
   private static double zTT3nw6;
   private static double vmpmU;
   private static double b69s;
   private static boolean cXHvq = false;
   private static String rxYnaa = null;
   private static boolean r6s7FL3 = false;
   private static double iAKNPAj;
   private static double qlv6NKWnH;
   private static double hOKMYHOaC;
   private static String iS1JnZov = null;
   private static double lfyfLwuw = -1.0;
   private static boolean wyNDWDB = false;
   private static double iRWc2Pi = -1.0;
   private static boolean tpzIHm = false;
   private static float eJDY;
   private static float zHBAQgZP;
   private static boolean we1GYY = false;
   private static double pxmzA;
   private static double iElAbEZe;
   private static double sTrE;
   private static boolean ifpFOCaAs = false;
   private static int uQdlyiwB = -1;
   private static double ivDR;
   private static double n9lX8A;
   private static double wBXn;
   private static double mM7DsB9AR;
   private static double uKpZYj;
   private static double l2IWOAGO;
   private static double xYttSy;
   private static double kNoG;
   private static double fvsAOg;
   private static int lFt1ckP = 0;
   private static final int qU1QioDiC = 2;
   private static final double zQOhlhTZx = 50.0;
   private static boolean lvk8rWUGD = false;
   private static float pyHY5;
   private static float idXga;
   private static boolean i263D9 = true;
   private static boolean rOIp0p7 = false;
   private static float iKXOcc = 0.0F;
   private static boolean jt9oI60ST = true;
   private static final float oEuCBB = 0.003F;
   private static final float uj1PcjLg = 0.012F;
   private static boolean zgQgCA9 = false;
   private static boolean qV33e = false;
   private static final float xogV5KBhg = -90.0F;
   private static final float rYlh = 90.0F;
   private static final double sZzOK7T = 0.5;
   private static double kNWIu;
   private static double wTo0I;
   private static double hXya;
   private static boolean wogVpI = false;
   private static long iY41Hgn0m = 0L;
   private static final float nwFPfQ = 18.0F;
   private static final float qz71Ga = 0.083333336F;
   private static double dZAuCP;
   private static double p9T0hGf;
   private static double i7EP1lKl;
   private static double dReHaJ;
   private static double jm7PbrhC;
   private static double jfdtPQG;
   private static boolean urdCiPZD = false;
   private static long gQiZ = 0L;
   private static final float nTdR = 30.0F;
   private static final float be9wl = 10.0F;
   private static float nL8hNS4cc = 0.0F;
   private static float e89G = 0.0F;
   private static boolean jy4YJZH = false;
   private static final float qJ6kALs = 1.5F;
   private static final float pWmAxmwe = 30.0F;
   private static long srOgYIAu = 0L;
   private static boolean kbDIB5 = false;
   private static final Map<Integer, MunitionClientState.KKeMvpuBgEY> pEnzO8R = new HashMap<>();

   public static boolean v5suI() {
      LoiteringMunitionEntity var0 = lFt1ckP();
      return var0 != null && var0.uQdlyiwB().qlv6NKWnH().rsDd50tfV().controlScheme() == DroneDefinition.ControlScheme.wuhB;
   }

   public static boolean ibzfxic() {
      LoiteringMunitionEntity var0 = lFt1ckP();
      return var0 != null && v5suI() && var0.uQdlyiwB().bAcdvq9().opticalZoom() && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
   }

   public static boolean wuhB() {
      LoiteringMunitionEntity var0 = lFt1ckP();
      return var0 != null && v5suI() && var0.uQdlyiwB().bAcdvq9().thermalToggle();
   }

   public static float[] dOfFwvo() {
      LoiteringMunitionEntity var0 = lFt1ckP();
      return var0 != null ? var0.uQdlyiwB().bAcdvq9().zoom() : CameraZoom.DEFAULT_LEVELS;
   }

   public static float rsDd50tfV() {
      LoiteringMunitionEntity var0 = lFt1ckP();
      return var0 != null ? var0.uQdlyiwB().bAcdvq9().zoomBlur() : 0.0F;
   }

   public static boolean uSUb() {
      return zgQgCA9;
   }

   public static void jGhI2() {
      zgQgCA9 = !zgQgCA9;
      if (zgQgCA9) {
         jy4YJZH = true;
         Minecraft var0 = Minecraft.getInstance();
         if (var0.level != null) {
            Camera var1 = var0.gameRenderer.mainCamera();
            Vec3 var2 = var1.position();
            AimMath.KKeMvpuBgEY var3 = AimMath.v5suI(nL8hNS4cc, e89G);
            Vec3 var4 = new Vec3(var3.v5suI(), var3.ibzfxic(), var3.wuhB());
            AimScreen.v5suI(var2, var4, lFt1ckP());
         }
      } else {
         v5suI(Minecraft.getInstance());
         iKXOcc();
         qV33e = true;
      }
   }

   private MunitionClientState() {
   }

   public static DroneDefinition.GimbalLimits wEJx3Itb() {
      LoiteringMunitionEntity var0 = lFt1ckP();
      return var0 != null ? var0.uQdlyiwB().bAcdvq9().gimbalLimits() : DroneDefinition.GimbalLimits.v5suI;
   }

   public static void v5suI(float var0, float var1) {
      if (v5suI >= 0 && !ibzfxic) {
         if (v5suI() && !zgQgCA9) {
            jy4YJZH = true;
            if (qV33e) {
               qV33e = false;
            } else {
               DroneDefinition.GimbalLimits var2 = wEJx3Itb();
               nL8hNS4cc = Mth.wrapDegrees(nL8hNS4cc + var0 * 0.012F);
               e89G = Mth.clamp(e89G + var1 * 0.012F, -var2.shakeBone(), var2.shakeAngleDeg());
            }
         } else {
            eJDY = Mth.clamp(eJDY + var0 * 0.003F, -1.0F, 1.0F);
            zHBAQgZP = Mth.clamp(zHBAQgZP + var1 * 0.003F, -1.0F, 1.0F);
         }
      }
   }

   private static void i263D9() {
      eJDY = 0.0F;
      zHBAQgZP = 0.0F;
   }

   public static AABB v5suI(Entity var0) {
      // 1.1.8 getBoundingBoxForCulling; munitions report their own (collision box + 0.5).
      return var0 instanceof LoiteringMunitionEntity munition ? munition.cullingBox() : var0.getBoundingBox();
   }

   public static void v5suI(double var0, double var2, double var4) {
      uQdlyiwB = -1;
      pxmzA = var0;
      iElAbEZe = var2;
      sTrE = var4;
      we1GYY = true;
      rOIp0p7();
   }

   public static void v5suI(Entity var0, Vec3 var1) {
      if (var0 != null && var1 != null) {
         uQdlyiwB = var0.getId();
         AABB var2 = v5suI(var0);
         double var3 = Math.max(0.001, var2.getXsize());
         double var5 = Math.max(0.001, var2.getYsize());
         ivDR = (var1.x - var2.minX) / var3 - 0.5;
         n9lX8A = (var1.y - var2.minY) / var5;
         wBXn = (var1.z - var2.minZ) / var3 - 0.5;
         mM7DsB9AR = var1.x;
         uKpZYj = var1.y;
         l2IWOAGO = var1.z;
         pxmzA = mM7DsB9AR;
         iElAbEZe = uKpZYj;
         sTrE = l2IWOAGO;
         we1GYY = true;
         xYttSy = 0.0;
         kNoG = 0.0;
         fvsAOg = 0.0;
         lFt1ckP = 0;
         rOIp0p7();
      }
   }

   private static Vec3 ibzfxic(Entity var0) {
      AABB var1 = v5suI(var0);
      double var2 = var1.getXsize();
      double var4 = var1.getYsize();
      double var6 = var1.minX + (0.5 + ivDR) * var2;
      double var8 = var1.minY + n9lX8A * var4;
      double var10 = var1.minZ + (0.5 + wBXn) * var2;
      return new Vec3(var6, var8, var10);
   }

   public static boolean bAcdvq9() {
      return we1GYY;
   }

   public static void c1jHYPb4() {
      if (we1GYY) {
         ifpFOCaAs = true;
      }
   }

   public static boolean v8ZN() {
      return ifpFOCaAs;
   }

   public static boolean zTT3nw6() {
      return uQdlyiwB >= 0;
   }

   public static double vmpmU() {
      return pxmzA;
   }

   public static double b69s() {
      return iElAbEZe;
   }

   public static double cXHvq() {
      return sTrE;
   }

   public static Vec3 v5suI(float var0) {
      if (uQdlyiwB >= 0) {
         Minecraft var1 = Minecraft.getInstance();
         Entity var2 = var1.level != null ? var1.level.getEntity(uQdlyiwB) : null;
         if (var2 != null && var2.isAlive()) {
            double var44 = Mth.lerp(var0, var2.xOld, var2.getX());
            double var45 = Mth.lerp(var0, var2.yOld, var2.getY());
            double var46 = Mth.lerp(var0, var2.zOld, var2.getZ());
            AABB var47 = v5suI(var2);
            double var48 = var47.getXsize();
            double var12 = var47.getYsize();
            double var14 = var44 + (var47.minX - var2.getX());
            double var16 = var45 + (var47.minY - var2.getY());
            double var18 = var46 + (var47.minZ - var2.getZ());
            double var20 = var14 + (0.5 + ivDR) * var48;
            double var22 = var16 + n9lX8A * var12;
            double var24 = var18 + (0.5 + wBXn) * var48;
            long var26 = System.currentTimeMillis();
            if (!urdCiPZD) {
               dZAuCP = var20;
               p9T0hGf = var22;
               i7EP1lKl = var24;
               dReHaJ = 0.0;
               jm7PbrhC = 0.0;
               jfdtPQG = 0.0;
               urdCiPZD = true;
            } else {
               float var28 = gQiZ == 0L ? 0.0F : Math.min((float)(var26 - gQiZ) / 1000.0F, 0.083333336F);
               if (var28 > 0.0F) {
                  double var29 = dZAuCP + dReHaJ * var28;
                  double var31 = p9T0hGf + jm7PbrhC * var28;
                  double var33 = i7EP1lKl + jfdtPQG * var28;
                  double var35 = var20 - var29;
                  double var37 = var22 - var31;
                  double var39 = var24 - var33;
                  float var41 = 1.0F - (float)Math.exp(-var28 * 30.0F);
                  float var42 = 1.0F - (float)Math.exp(-var28 * 10.0F);
                  dZAuCP = var29 + var35 * var41;
                  p9T0hGf = var31 + var37 * var41;
                  i7EP1lKl = var33 + var39 * var41;
                  dReHaJ += var35 * var42 / var28;
                  jm7PbrhC += var37 * var42 / var28;
                  jfdtPQG += var39 * var42 / var28;
               }
            }

            gQiZ = var26;
            return new Vec3(dZAuCP, p9T0hGf, i7EP1lKl);
         }
      }

      double var43 = pxmzA;
      double var3 = iElAbEZe;
      double var5 = sTrE;
      long var7 = System.currentTimeMillis();
      if (!wogVpI) {
         kNWIu = var43;
         wTo0I = var3;
         hXya = var5;
         wogVpI = true;
      } else {
         float var9 = iY41Hgn0m == 0L ? 0.0F : Math.min((float)(var7 - iY41Hgn0m) / 1000.0F, 0.083333336F);
         float var10 = 1.0F - (float)Math.exp(-var9 * 18.0F);
         kNWIu = kNWIu + (var43 - kNWIu) * var10;
         wTo0I = wTo0I + (var3 - wTo0I) * var10;
         hXya = hXya + (var5 - hXya) * var10;
      }

      iY41Hgn0m = var7;
      return new Vec3(kNWIu, wTo0I, hXya);
   }

   private static void rOIp0p7() {
      wogVpI = false;
      iY41Hgn0m = 0L;
      urdCiPZD = false;
      gQiZ = 0L;
   }

   public static Vec3 rxYnaa() {
      return uQdlyiwB >= 0 && urdCiPZD ? new Vec3(dReHaJ, jm7PbrhC, jfdtPQG) : Vec3.ZERO;
   }

   public static void ibzfxic(float var0, float var1) {
      if (!we1GYY && !jy4YJZH) {
         nL8hNS4cc = var0;
         e89G = var1;
      }
   }

   public static float[] r6s7FL3() {
      return new float[]{nL8hNS4cc, e89G};
   }

   public static double iAKNPAj() {
      LoiteringMunitionEntity var0 = lFt1ckP();
      if (var0 == null) {
         return 70.0;
      }

      DroneDefinition.Camera var1 = var0.uQdlyiwB().bAcdvq9();
      double var2 = var1.v5suI() ? FpvConfig.getFov() : var1.fovDegrees();
      if (ibzfxic()) {
         CameraZoom var4 = CameraZoom.getInstance();
         if (var4.isZoomed()) {
            var2 = var4.applyToFov(var2);
         }
      }

      return var2;
   }

   public static void v5suI(double var0, double var2, double var4, float var6, float var7, float var8) {
      if (!we1GYY) {
         srOgYIAu = 0L;
      } else {
         Vec3 var9 = v5suI(var6);
         double var10 = var9.x - var0;
         double var12 = var9.y - var2;
         double var14 = var9.z - var4;
         double var16 = Math.sqrt(var10 * var10 + var14 * var14);
         if (var16 < 0.5) {
            srOgYIAu = System.currentTimeMillis();
         } else {
            float var18 = (float)Math.toDegrees(Math.atan2(-var10, var14));
            float var19 = Mth.clamp((float)(-Math.toDegrees(Math.atan2(var12, Math.max(0.001, var16)))), -90.0F, 90.0F);
            float[] var20 = v5suI(var18, var19, var7, var8);
            float var21 = var20[0];
            float var22 = var20[1];
            if (!kbDIB5 && (uQdlyiwB < 0 || !zHBAQgZP())) {
               long var23 = System.currentTimeMillis();
               float var25 = srOgYIAu == 0L ? 0.0F : Math.min((float)(var23 - srOgYIAu) / 1000.0F, 0.083333336F);
               srOgYIAu = var23;
               float var26 = Math.max(30.0F, (float)iAKNPAj() * 1.5F);
               float var27 = var26 * var25;
               float var28 = Mth.wrapDegrees(var21 - nL8hNS4cc);
               nL8hNS4cc = Mth.wrapDegrees(nL8hNS4cc + Mth.clamp(var28, -var27, var27));
               float var29 = var22 - e89G;
               e89G = Mth.clamp(e89G + Mth.clamp(var29, -var27, var27), -90.0F, 90.0F);
            } else {
               kbDIB5 = false;
               nL8hNS4cc = var21;
               e89G = var22;
               srOgYIAu = System.currentTimeMillis();
            }
         }
      }
   }

   private static float[] v5suI(float var0, float var1, float var2, float var3) {
      DroneDefinition.GimbalLimits var4 = wEJx3Itb();
      if (zHBAQgZP()) {
         float var5 = Mth.clamp(Mth.wrapDegrees(var0 - var2), -var4.yawLeftDeg(), var4.enabled());
         float var6 = Mth.wrapDegrees(var2 + var5);
         float var7 = Mth.clamp(var1 - var3, -var4.shakeBone(), var4.shakeAngleDeg());
         float var8 = Mth.clamp(var3 + var7, -90.0F, 90.0F);
         return new float[]{var6, var8};
      } else {
         return new float[]{var0, Mth.clamp(var1, -var4.shakeBone(), var4.shakeAngleDeg())};
      }
   }

   public static void v5suI(float var0, float var1, boolean var2, boolean var3, float var4, boolean var5) {
      pyHY5 = var0;
      idXga = var1;
      i263D9 = var2;
      rOIp0p7 = var3;
      iKXOcc = var4;
      jt9oI60ST = var5;
      lvk8rWUGD = true;
   }

   public static boolean qlv6NKWnH() {
      return lvk8rWUGD;
   }

   public static float hOKMYHOaC() {
      return pyHY5;
   }

   public static float iS1JnZov() {
      return idXga;
   }

   public static boolean lfyfLwuw() {
      return i263D9;
   }

   public static boolean wyNDWDB() {
      return rOIp0p7;
   }

   public static float iRWc2Pi() {
      return iKXOcc;
   }

   public static boolean tpzIHm() {
      return jt9oI60ST;
   }

   private static void iKXOcc() {
      we1GYY = false;
      ifpFOCaAs = false;
      uQdlyiwB = -1;
      ivDR = 0.0;
      n9lX8A = 0.0;
      wBXn = 0.0;
      mM7DsB9AR = 0.0;
      uKpZYj = 0.0;
      l2IWOAGO = 0.0;
      xYttSy = 0.0;
      kNoG = 0.0;
      fvsAOg = 0.0;
      lFt1ckP = 0;
      lvk8rWUGD = false;
      pyHY5 = 0.0F;
      idXga = 0.0F;
      i263D9 = true;
      rOIp0p7 = false;
      jt9oI60ST = true;
      iKXOcc = 0.0F;
      kbDIB5 = false;
      zgQgCA9 = false;
      rOIp0p7();
   }

   public static void eJDY() {
      pEnzO8R.clear();
   }

   private static void jt9oI60ST() {
      if (v5suI >= 0) {
         pEnzO8R.put(
            v5suI,
            new MunitionClientState.KKeMvpuBgEY(
               we1GYY, ifpFOCaAs, uQdlyiwB, ivDR, n9lX8A, wBXn, pxmzA, iElAbEZe, sTrE, mM7DsB9AR, uKpZYj, l2IWOAGO, nL8hNS4cc, e89G, zgQgCA9
            )
         );
      }
   }

   private static void v5suI(MunitionClientState.KKeMvpuBgEY var0) {
      we1GYY = var0.v5suI();
      ifpFOCaAs = var0.ibzfxic();
      uQdlyiwB = var0.wuhB();
      ivDR = var0.dOfFwvo();
      n9lX8A = var0.rsDd50tfV();
      wBXn = var0.uSUb();
      pxmzA = var0.jGhI2();
      iElAbEZe = var0.wEJx3Itb();
      sTrE = var0.bAcdvq9();
      mM7DsB9AR = var0.c1jHYPb4();
      uKpZYj = var0.v8ZN();
      l2IWOAGO = var0.zTT3nw6();
      nL8hNS4cc = var0.vmpmU();
      e89G = var0.b69s();
      zgQgCA9 = var0.cXHvq();
      jy4YJZH = true;
      lvk8rWUGD = false;
      pyHY5 = 0.0F;
      idXga = 0.0F;
      i263D9 = true;
      rOIp0p7 = false;
      jt9oI60ST = true;
      iKXOcc = 0.0F;
      rOIp0p7();
      kbDIB5 = true;
      srOgYIAu = 0L;
   }

   private static void v5suI(Minecraft var0) {
      if (var0.gui.screen() instanceof AimScreen) {
         var0.gui.setScreen(null);
      }
   }

   public static boolean zHBAQgZP() {
      LoiteringMunitionEntity var0 = lFt1ckP();
      return var0 != null && var0.uQdlyiwB().qlv6NKWnH().rsDd50tfV().controlScheme() == DroneDefinition.ControlScheme.ibzfxic;
   }

   public static boolean we1GYY() {
      return zHBAQgZP() || v5suI();
   }

   public static boolean pxmzA() {
      return zHBAQgZP() || v5suI() && zgQgCA9;
   }

   public static boolean iElAbEZe() {
      return v5suI >= 0;
   }

   public static void sTrE() {
      v5suI = -1;
      ibzfxic = false;
      v8ZN = false;
   }

   public static boolean ifpFOCaAs() {
      return ibzfxic;
   }

   public static boolean uQdlyiwB() {
      return v8ZN;
   }

   public static UUID ivDR() {
      return wuhB;
   }

   public static boolean n9lX8A() {
      return wyNDWDB;
   }

   public static double wBXn() {
      return iAKNPAj;
   }

   public static double mM7DsB9AR() {
      return qlv6NKWnH;
   }

   public static double uKpZYj() {
      return hOKMYHOaC;
   }

   public static String l2IWOAGO() {
      return iS1JnZov;
   }

   public static double xYttSy() {
      return lfyfLwuw;
   }

   public static void v5suI(double var0) {
      iRWc2Pi = var0;
   }

   public static void kNoG() {
      wyNDWDB = false;
   }

   public static int fvsAOg() {
      return v5suI;
   }

   public static LoiteringMunitionEntity lFt1ckP() {
      if (v5suI < 0) {
         return null;
      } else {
         Minecraft var0 = Minecraft.getInstance();
         if (var0.level == null) {
            return null;
         } else {
            return var0.level.getEntity(v5suI) instanceof LoiteringMunitionEntity var2 ? var2 : null;
         }
      }
   }

   public static boolean v5suI(UUID var0) {
      Minecraft var1 = Minecraft.getInstance();
      return iElAbEZe() && var1.player != null && var0 != null && var1.player.getUUID().equals(var0);
   }

   public static void v5suI(LoiteringMunitionEntity var0) {
      v5suI(var0.getId(), var0.getX(), var0.getY(), var0.getZ());
      wuhB(var0);
   }

   public static void v5suI(int var0, double var1, double var3, double var5) {
      Minecraft var7 = Minecraft.getInstance();
      if (var7.player != null) {
         if (v8ZN) {
            if (var0 == v5suI) {
               var7.gui.setScreen(null);
               v8ZN = false;
               oEuCBB();
               return;
            }

            v8ZN = false;
            qU1QioDiC();
         }

         var7.gui.setScreen(null);
         dOfFwvo = var7.player.getX();
         rsDd50tfV = var7.player.getY();
         uSUb = var7.player.getZ();
         jGhI2 = var7.player.getYRot();
         wEJx3Itb = var7.player.getXRot();
         v5suI = var0;
         ibzfxic = false;
         wuhB = null;
         c1jHYPb4 = 100;
         zTT3nw6 = var1;
         vmpmU = var3;
         b69s = var5;
         cXHvq = true;
         r6s7FL3 = false;
         SignalManager.getInstance().reset();
         GogglesHudOverlay.resetOsdProfile();
         qV33e();
         ControllerReader.setGpsMode(false);
         i263D9();
         MunitionClientState.KKeMvpuBgEY var8 = pEnzO8R.get(var0);
         if (var8 != null) {
            v5suI(var8);
         } else {
            iKXOcc();
            jy4YJZH = false;
         }

         var7.options.setCameraType(CameraType.FIRST_PERSON);
         ModNetwork.CHANNEL.sendToServer(new EnterMunitionViewPacket(var0, dOfFwvo, rsDd50tfV, uSUb, jGhI2, wEJx3Itb));
         FPVClientMod.setPilotBody(var7.player.getUUID(), new Vec3(dOfFwvo, rsDd50tfV, uSUb), jGhI2, wEJx3Itb);
         var7.player.setPos(var1, var3, var5);
         var7.player.setOldPosAndRot();
         ibzfxic(var7);
      }
   }

   public static void qU1QioDiC() {
      v5suI(Minecraft.getInstance(), true);
   }

   public static void zQOhlhTZx() {
      wyNDWDB = false;
      if (iElAbEZe() && !ibzfxic) {
         v8ZN = true;
      } else {
         if (cXHvq && (ibzfxic || r6s7FL3)) {
            iAKNPAj = zTT3nw6;
            qlv6NKWnH = vmpmU;
            hOKMYHOaC = b69s;
            iS1JnZov = rxYnaa;
            lfyfLwuw = iRWc2Pi;
            wyNDWDB = true;
            r6s7FL3 = false;
         }

         if (iElAbEZe()) {
            qU1QioDiC();
         }
      }
   }

   public static void lvk8rWUGD() {
      if (v8ZN) {
         v8ZN = false;
         oEuCBB();
      }
   }

   private static void oEuCBB() {
      kbDIB5 = true;
      srOgYIAu = 0L;
   }

   private static boolean ibzfxic(LoiteringMunitionEntity var0) {
      if (!ibzfxic && var0.iRWc2Pi()) {
         zgQgCA9();
         return true;
      } else {
         return false;
      }
   }

   public static void pyHY5() {
      if (v5suI >= 0) {
         r6s7FL3 = true;
      }

      if (!ibzfxic) {
         if (v5suI >= 0 && wuhB != null && FrozenFrameManager.getInstance().hasFrozenFrame(wuhB)) {
            zgQgCA9();
         } else {
            v5suI(Minecraft.getInstance(), false);
         }
      }
   }

   private static void uj1PcjLg() {
      r6s7FL3 = true;
      if (wuhB != null && FrozenFrameManager.getInstance().hasFrozenFrame(wuhB)) {
         zgQgCA9();
      } else {
         qU1QioDiC();
      }
   }

   private static void zgQgCA9() {
      ibzfxic = true;
      v8ZN = false;
      SignalManager.getInstance().forceNoSignal();
      Minecraft var0 = Minecraft.getInstance();
      v5suI(var0);
      if (var0.player != null) {
         var0.player.setPos(dOfFwvo, rsDd50tfV, uSUb);
         var0.player.setOldPosAndRot();
         ibzfxic(var0);
      }
   }

   private static void v5suI(Minecraft var0, boolean var1) {
      int var2 = v5suI;
      v5suI = -1;
      ibzfxic = false;
      v8ZN = false;
      iKXOcc();
      v5suI(var0);
      if (wuhB != null) {
         FrozenFrameManager.getInstance().clearFrame(wuhB);
         wuhB = null;
      }

      SignalManager.getInstance().reset();
      GogglesHudOverlay.resetOsdProfile();
      qV33e();
      if (var0.player != null) {
         UUID var3 = var0.player.getUUID();
         Vec3 var4 = FPVClientMod.getPilotBodyPosition(var3);
         float[] var5 = FPVClientMod.getPilotBodyRotation(var3);
         Vec3 var6 = var4 != null ? var4 : new Vec3(dOfFwvo, rsDd50tfV, uSUb);
         float var7 = var5 != null ? var5[0] : jGhI2;
         float var8 = var5 != null ? var5[1] : wEJx3Itb;
         FPVClientMod.teleportPilotWhenLoaded(var0.player, var6, var7, var8, () -> wuhB(var0));
         FPVClientMod.onDroneNotFound(var3);
      }

      if (var1 && var2 >= 0 && var0.getConnection() != null) {
         ModNetwork.CHANNEL.sendToServer(new ExitMunitionViewPacket(var2));
      }
   }

   public static void idXga() {
      if (v5suI >= 0) {
         Minecraft var0 = Minecraft.getInstance();
         if (var0.level != null && var0.player != null) {
            if (var0.player.isPassenger()) {
               var0.player.stopRiding();
            }

            if (ibzfxic) {
               ibzfxic(var0);
            } else if (v8ZN) {
               ibzfxic(var0);
            } else {
               LoiteringMunitionEntity var1 = lFt1ckP();
               if (var1 == null) {
                  if (--c1jHYPb4 <= 0) {
                     uj1PcjLg();
                  } else {
                     ibzfxic(var0);
                  }
               } else if (!var1.isRemoved() && var1.isAlive()) {
                  c1jHYPb4 = 100;
                  zTT3nw6 = var1.getX();
                  vmpmU = var1.getY();
                  b69s = var1.getZ();
                  rxYnaa = var1.wEJx3Itb();
                  cXHvq = true;
                  if (!ibzfxic(var1)) {
                     wuhB(var1);
                     ibzfxic(var0);
                     boolean var2 = var0.options.getCameraType() == CameraType.FIRST_PERSON;
                     if (!pxmzA() || !var2) {
                        v5suI(var0);
                     } else if (var0.gui.screen() == null) {
                        var0.gui.setScreen(new AimScreen(var1.getId()));
                     }

                     if (pxmzA()) {
                        if (uQdlyiwB >= 0) {
                           Entity var24 = var0.level.getEntity(uQdlyiwB);
                           if (var24 != null && var24.isAlive()) {
                              Vec3 var26 = ibzfxic(var24);
                              double var27 = var26.x;
                              double var7 = var26.y;
                              double var9 = var26.z;
                              BlockHitResult var11 = var0.level
                                 .clip(new net.minecraft.world.level.ClipContext(var1.position(), new Vec3(var27, var7, var9), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null));
                              boolean var12 = var11.getType() != Type.BLOCK;
                              if (var12) {
                                 lFt1ckP = 0;
                                 double var13 = var27 - mM7DsB9AR;
                                 double var15 = var7 - uKpZYj;
                                 double var17 = var9 - l2IWOAGO;
                                 xYttSy = var13;
                                 kNoG = var15;
                                 fvsAOg = var17;
                                 if (zHBAQgZP()) {
                                    Vec3 var19 = rxYnaa();
                                    double var20 = var1.position().distanceTo(new Vec3(var27, var7, var9));
                                    double var22 = Math.max(0.05, var20 / 50.0);
                                    pxmzA = var27 + var19.x * var22;
                                    iElAbEZe = var7 + var19.y * var22;
                                    sTrE = var9 + var19.z * var22;
                                 } else {
                                    pxmzA = var27;
                                    iElAbEZe = var7;
                                    sTrE = var9;
                                 }

                                 mM7DsB9AR = var27;
                                 uKpZYj = var7;
                                 l2IWOAGO = var9;
                              } else {
                                 lFt1ckP++;
                                 if (lFt1ckP > 2) {
                                    if (v5suI()) {
                                       we1GYY = false;
                                       uQdlyiwB = -1;
                                       lFt1ckP = 0;
                                    } else {
                                       uQdlyiwB = -1;
                                    }
                                 } else {
                                    pxmzA = pxmzA + xYttSy;
                                    iElAbEZe = iElAbEZe + kNoG;
                                    sTrE = sTrE + fvsAOg;
                                 }
                              }
                           } else {
                              uQdlyiwB = -1;
                           }
                        } else if (we1GYY && v5suI()) {
                           BlockHitResult var25 = var0.level
                              .clip(new net.minecraft.world.level.ClipContext(var1.position(), new Vec3(pxmzA, iElAbEZe, sTrE), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null));
                           if (var25.getType() != Type.BLOCK) {
                              lFt1ckP = 0;
                           } else {
                              lFt1ckP++;
                              if (lFt1ckP > 2) {
                                 we1GYY = false;
                                 lFt1ckP = 0;
                              }
                           }
                        }

                        if (zHBAQgZP()) {
                           ModNetwork.CHANNEL.sendToServer(new MunitionStickPacket(var1.getId(), we1GYY, ifpFOCaAs, pxmzA, iElAbEZe, sTrE, uQdlyiwB));
                        }

                        jt9oI60ST();
                     } else if (v5suI()) {
                        jt9oI60ST();
                     } else {
                        boolean var5 = ControllerReader.getInstance().isPhysicalControllerConnected() && !FpvConfig.isForceKbm();
                        float var3;
                        float var4;
                        if (var5) {
                           var3 = ControllerReader.getPitch();
                           var4 = ControllerReader.getRoll();
                        } else {
                           if (KeyManager.MOUSE_STICK_CENTER_KEY.isDown()) {
                              i263D9();
                           }

                           var3 = zHBAQgZP;
                           var4 = eJDY;
                        }

                        ModNetwork.CHANNEL
                           .sendToServer(new MunitionControlPacket(var1.getId(), ControllerReader.getThrottle(), ControllerReader.getYaw(), var3, var4));
                     }
                  }
               } else {
                  uj1PcjLg();
               }
            }
         } else {
            qU1QioDiC();
         }
      }
   }

   public static void v5suI(RenderTarget var0) {
      if (v5suI >= 0 && var0 != null) {
         FrozenFrameManager var1 = FrozenFrameManager.getInstance();
         if (ibzfxic) {
            if (wuhB != null) {
               var1.renderFrozenFrame(wuhB, var0);
            }
         } else {
            LoiteringMunitionEntity var2 = lFt1ckP();
            if (var2 != null && var2.isAlive() && !var2.isRemoved()) {
               wuhB = var2.getUUID();
               var1.captureFrame(wuhB, var0);
            }
         }
      }
   }

   private static void wuhB(LoiteringMunitionEntity var0) {
      if (var0 != null) {
         boolean var1 = var0.uQdlyiwB().bAcdvq9().thermal();
         if (var1) {
            ThermalCameraState var2 = ThermalCameraState.getInstance();
            if (!var2.isEnabled()) {
               var2.toggle();
               tpzIHm = true;
            }
         }
      }
   }

   private static void qV33e() {
      ThermalCameraState.getInstance().disable();
      tpzIHm = false;
   }

   private static void ibzfxic(Minecraft var0) {
      if (var0.player != null) {
         FPVClientMod.enablePilotFlight(var0.player);
      }
   }

   private static void wuhB(Minecraft var0) {
      if (var0.player != null) {
         FPVClientMod.disablePilotFlight(var0.player);
      }
   }

   private static final class KKeMvpuBgEY {
      private final boolean v5suI;
      private final boolean ibzfxic;
      private final int wuhB;
      private final double dOfFwvo;
      private final double rsDd50tfV;
      private final double uSUb;
      private final double jGhI2;
      private final double wEJx3Itb;
      private final double bAcdvq9;
      private final double c1jHYPb4;
      private final double v8ZN;
      private final double zTT3nw6;
      private final float vmpmU;
      private final float b69s;
      private final boolean cXHvq;

      private KKeMvpuBgEY(
         boolean var1,
         boolean var2,
         int var3,
         double var4,
         double var6,
         double var8,
         double var10,
         double var12,
         double var14,
         double var16,
         double var18,
         double var20,
         float var22,
         float var23,
         boolean var24
      ) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var6;
         this.uSUb = var8;
         this.jGhI2 = var10;
         this.wEJx3Itb = var12;
         this.bAcdvq9 = var14;
         this.c1jHYPb4 = var16;
         this.v8ZN = var18;
         this.zTT3nw6 = var20;
         this.vmpmU = var22;
         this.b69s = var23;
         this.cXHvq = var24;
      }


      public boolean v5suI() {

         return this.v5suI;

      }

   
      public boolean ibzfxic() {
         return this.ibzfxic;
      }

      public int wuhB() {
         return this.wuhB;
      }

      public double dOfFwvo() {
         return this.dOfFwvo;
      }

      public double rsDd50tfV() {
         return this.rsDd50tfV;
      }

      public double uSUb() {
         return this.uSUb;
      }

      public double jGhI2() {
         return this.jGhI2;
      }

      public double wEJx3Itb() {
         return this.wEJx3Itb;
      }

      public double bAcdvq9() {
         return this.bAcdvq9;
      }

      public double c1jHYPb4() {
         return this.c1jHYPb4;
      }

      public double v8ZN() {
         return this.v8ZN;
      }

      public double zTT3nw6() {
         return this.zTT3nw6;
      }

      public float vmpmU() {
         return this.vmpmU;
      }

      public float b69s() {
         return this.b69s;
      }

      public boolean cXHvq() {
         return this.cXHvq;
      }
   }
}
