package com.fpvdrone.data;

import com.fpvdrone.data.DefinitionFiles;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

public final class ParticleSpec {
   private final String ibzfxic;
   private final ParticleSpec.KKeMvpuBgEY wuhB;
   private final ParticleSpec.Rdl0X5jeod dOfFwvo;
   private final ParticleSpec.Ol3TEDSog rsDd50tfV;
   private final boolean uSUb;
   private final boolean jGhI2;
   private final boolean wEJx3Itb;
   private final float bAcdvq9;
   private final boolean c1jHYPb4;
   private final boolean v8ZN;
   private final int zTT3nw6;
   private final float vmpmU;
   private final int b69s;
   private final int cXHvq;
   private final int rxYnaa;
   private final float r6s7FL3;
   private final float iAKNPAj;
   private final int qlv6NKWnH;
   private final int hOKMYHOaC;
   private final float iS1JnZov;
   private final float lfyfLwuw;
   private final int wyNDWDB;
   private final int iRWc2Pi;
   private final float tpzIHm;
   @Nullable
   private final String eJDY;
   private final List<String> zHBAQgZP;
   private final List<Integer> we1GYY;
   private final boolean pxmzA;
   private final int iElAbEZe;
   private final int sTrE;
   private final List<Integer> ifpFOCaAs;
   private final List<Integer> uQdlyiwB;
   private final boolean ivDR;
   @Nullable
   private final float[] n9lX8A;
   @Nullable
   private final float[] wBXn;
   @Nullable
   private final float[] mM7DsB9AR;
   @Nullable
   private final float[] uKpZYj;
   @Nullable
   private final float[] l2IWOAGO;
   @Nullable
   private final float[] xYttSy;
   @Nullable
   private final float[] kNoG;
   @Nullable
   private final float[] fvsAOg;
   private final boolean lFt1ckP;
   private final boolean qU1QioDiC;
   public static final int v5suI = -1;

   public ParticleSpec(
      String var1,
      ParticleSpec.KKeMvpuBgEY var2,
      ParticleSpec.Rdl0X5jeod var3,
      ParticleSpec.Ol3TEDSog var4,
      boolean var5,
      boolean var6,
      boolean var7,
      float var8,
      boolean var9,
      boolean var10,
      int var11,
      float var12,
      int var13,
      int var14,
      int var15,
      float var16,
      float var17,
      int var18,
      int var19,
      float var20,
      float var21,
      int var22,
      int var23,
      float var24,
      @Nullable String var25,
      List<String> var26,
      List<Integer> var27,
      boolean var28,
      int var29,
      int var30,
      List<Integer> var31,
      List<Integer> var32,
      boolean var33,
      @Nullable float[] var34,
      @Nullable float[] var35,
      @Nullable float[] var36,
      @Nullable float[] var37,
      @Nullable float[] var38,
      @Nullable float[] var39,
      @Nullable float[] var40,
      @Nullable float[] var41,
      boolean var42,
      boolean var43
   ) {
      this.ibzfxic = var1;
      this.wuhB = var2;
      this.dOfFwvo = var3;
      this.rsDd50tfV = var4;
      this.uSUb = var5;
      this.jGhI2 = var6;
      this.wEJx3Itb = var7;
      this.bAcdvq9 = var8;
      this.c1jHYPb4 = var9;
      this.v8ZN = var10;
      this.zTT3nw6 = var11;
      this.vmpmU = var12;
      this.b69s = var13;
      this.cXHvq = var14;
      this.rxYnaa = var15;
      this.r6s7FL3 = var16;
      this.iAKNPAj = var17;
      this.qlv6NKWnH = var18;
      this.hOKMYHOaC = var19;
      this.iS1JnZov = var20;
      this.lfyfLwuw = var21;
      this.wyNDWDB = var22;
      this.iRWc2Pi = var23;
      this.tpzIHm = var24;
      this.eJDY = var25;
      this.zHBAQgZP = var26;
      this.we1GYY = var27;
      this.pxmzA = var28;
      this.iElAbEZe = var29;
      this.sTrE = var30;
      this.ifpFOCaAs = var31;
      this.uQdlyiwB = var32;
      this.ivDR = var33;
      this.n9lX8A = var34;
      this.wBXn = var35;
      this.mM7DsB9AR = var36;
      this.uKpZYj = var37;
      this.l2IWOAGO = var38;
      this.xYttSy = var39;
      this.kNoG = var40;
      this.fvsAOg = var41;
      this.lFt1ckP = var42;
      this.qU1QioDiC = var43;
   }

   public static ParticleSpec v5suI(String var0, List<String> var1) {
      return new ParticleSpec(
         var0,
         ParticleSpec.KKeMvpuBgEY.rsDd50tfV,
         ParticleSpec.Rdl0X5jeod.dOfFwvo,
         ParticleSpec.Ol3TEDSog.rsDd50tfV,
         false,
         false,
         false,
         0.0F,
         false,
         true,
         1,
         0.0F,
         600,
         100,
         0,
         1.0F,
         1.0F,
         0,
         100,
         0.75F,
         0.0F,
         0,
         0,
         0.2F,
         null,
         var1,
         List.of(),
         true,
         16777215,
         -1,
         List.of(),
         List.of(),
         false,
         new float[]{0.0F, 0.02F, 0.0F},
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         false,
         false
      );
   }

   public static List<ParticleSpec> v5suI(JsonObject var0, String var1) {
      ArrayList var2 = new ArrayList();
      if (var0.has(var1) && var0.get(var1).isJsonArray()) {
         int var3 = 0;

         for (JsonElement var5 : var0.getAsJsonArray(var1)) {
            if (var5.isJsonObject()) {
               var2.add(ibzfxic(var5.getAsJsonObject(), var1 + "[" + var3++ + "]"));
            }
         }
      }

      return List.copyOf(var2);
   }

   public static ParticleSpec ibzfxic(JsonObject var0, String var1) {
      ParticleSpec.KKeMvpuBgEY var2 = ParticleSpec.KKeMvpuBgEY.v5suI(DefinitionFiles.v5suI(var0, "type", "generic"));
      return new ParticleSpec(
         var1,
         var2,
         ParticleSpec.Rdl0X5jeod.v5suI(DefinitionFiles.v5suI(var0, "spawningOrientation", "world")),
         ParticleSpec.Ol3TEDSog.v5suI(DefinitionFiles.v5suI(var0, "renderingOrientation", "player")),
         DefinitionFiles.v5suI(var0, "spawnEveryTick", false),
         DefinitionFiles.v5suI(var0, "isBright", var2 == ParticleSpec.KKeMvpuBgEY.ibzfxic),
         DefinitionFiles.v5suI(var0, "isBlended", false),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "daytimeReductionFactor", 0.0F), 0.0F, 1.0F),
         DefinitionFiles.v5suI(var0, "stopsOnGround", false),
         DefinitionFiles.v5suI(var0, "ignoreCollision", false),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "quantity", 1), 1, 512),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "distance", 0.0F), 0.0F, 64.0F),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "duration", 0), 0, 12000),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "durationRandomness", 0), 0, 12000),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "movementDuration", 0), 0, 12000),
         var0.has("transparency") ? DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "transparency", 1.0F), 0.0F, 1.0F) : -1.0F,
         var0.has("toTransparency") ? DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "toTransparency", 0.0F), 0.0F, 1.0F) : -1.0F,
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "fadeInTransparencyTime", 0), 0, 12000),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "fadeOutTransparencyTime", 0), 0, 12000),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "scale", 0.0F), 0.0F, 1024.0F),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "toScale", 0.0F), 0.0F, 1024.0F),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "fadeInScaleTime", 0), 0, 12000),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "fadeOutScaleTime", 0), 0, 12000),
         DefinitionFiles.v5suI(DefinitionFiles.v5suI(var0, "hitboxSize", 0.2F), 0.01F, 4.0F),
         var0.has("texture") ? DefinitionFiles.v5suI(var0, "texture", (String)null) : null,
         DefinitionFiles.ibzfxic(var0, "textureList"),
         DefinitionFiles.wuhB(var0, "textureDelays"),
         DefinitionFiles.v5suI(var0, "randomTexture", false),
         DefinitionFiles.ibzfxic(var0, "color", 16777215),
         DefinitionFiles.ibzfxic(var0, "toColor", -1),
         v5suI(var0),
         DefinitionFiles.wuhB(var0, "colorDelays"),
         DefinitionFiles.v5suI(var0, "randomColor", false),
         DefinitionFiles.v5suI(var0, "pos"),
         DefinitionFiles.v5suI(var0, "rotationVelocity"),
         DefinitionFiles.v5suI(var0, "spreadRandomness"),
         DefinitionFiles.v5suI(var0, "relativeInheritedVelocityFactor"),
         DefinitionFiles.v5suI(var0, "initialVelocity"),
         DefinitionFiles.v5suI(var0, "movementVelocity"),
         DefinitionFiles.v5suI(var0, "relativeMovementVelocity"),
         DefinitionFiles.v5suI(var0, "terminalVelocity"),
         DefinitionFiles.v5suI(var0, "useBlockColor", false),
         DefinitionFiles.v5suI(var0, "getBlockPropertiesFromGround", false)
      );
   }

   private static List<Integer> v5suI(JsonObject var0) {
      ArrayList var1 = new ArrayList();
      if (var0.has("colorList") && var0.get("colorList").isJsonArray()) {
         for (JsonElement var3 : var0.getAsJsonArray("colorList")) {
            try {
               String var4 = var3.getAsString().trim();
               if (var4.startsWith("#")) {
                  var4 = var4.substring(1);
               } else if (var4.startsWith("0x") || var4.startsWith("0X")) {
                  var4 = var4.substring(2);
               }

               var1.add(Integer.parseInt(var4, 16) & 16777215);
            } catch (Exception var5) {
            }
         }
      }

      return var1;
   }

   public String v5suI() {
      return this.ibzfxic;
   }

   public ParticleSpec.KKeMvpuBgEY ibzfxic() {
      return this.wuhB;
   }

   public ParticleSpec.Rdl0X5jeod wuhB() {
      return this.dOfFwvo;
   }

   public ParticleSpec.Ol3TEDSog dOfFwvo() {
      return this.rsDd50tfV;
   }

   public boolean rsDd50tfV() {
      return this.uSUb;
   }

   public boolean uSUb() {
      return this.jGhI2;
   }

   public boolean jGhI2() {
      return this.wEJx3Itb;
   }

   public float wEJx3Itb() {
      return this.bAcdvq9;
   }

   public boolean bAcdvq9() {
      return this.c1jHYPb4;
   }

   public boolean c1jHYPb4() {
      return this.v8ZN;
   }

   public int v8ZN() {
      return this.zTT3nw6;
   }

   public float zTT3nw6() {
      return this.vmpmU;
   }

   public int vmpmU() {
      return this.b69s;
   }

   public int b69s() {
      return this.cXHvq;
   }

   public int cXHvq() {
      return this.rxYnaa;
   }

   public float rxYnaa() {
      return this.r6s7FL3;
   }

   public float r6s7FL3() {
      return this.iAKNPAj;
   }

   public int iAKNPAj() {
      return this.qlv6NKWnH;
   }

   public int qlv6NKWnH() {
      return this.hOKMYHOaC;
   }

   public float hOKMYHOaC() {
      return this.iS1JnZov;
   }

   public float iS1JnZov() {
      return this.lfyfLwuw;
   }

   public int lfyfLwuw() {
      return this.wyNDWDB;
   }

   public int wyNDWDB() {
      return this.iRWc2Pi;
   }

   public float iRWc2Pi() {
      return this.tpzIHm;
   }

   @Nullable
   public String tpzIHm() {
      return this.eJDY;
   }

   public List<String> eJDY() {
      return this.zHBAQgZP;
   }

   public List<Integer> zHBAQgZP() {
      return this.we1GYY;
   }

   public boolean we1GYY() {
      return this.pxmzA;
   }

   public int pxmzA() {
      return this.iElAbEZe;
   }

   public int iElAbEZe() {
      return this.sTrE;
   }

   public List<Integer> sTrE() {
      return this.ifpFOCaAs;
   }

   public List<Integer> ifpFOCaAs() {
      return this.uQdlyiwB;
   }

   public boolean uQdlyiwB() {
      return this.ivDR;
   }

   @Nullable
   public float[] ivDR() {
      return this.n9lX8A;
   }

   @Nullable
   public float[] n9lX8A() {
      return this.wBXn;
   }

   @Nullable
   public float[] wBXn() {
      return this.mM7DsB9AR;
   }

   @Nullable
   public float[] mM7DsB9AR() {
      return this.uKpZYj;
   }

   @Nullable
   public float[] uKpZYj() {
      return this.l2IWOAGO;
   }

   @Nullable
   public float[] l2IWOAGO() {
      return this.xYttSy;
   }

   @Nullable
   public float[] xYttSy() {
      return this.kNoG;
   }

   @Nullable
   public float[] kNoG() {
      return this.fvsAOg;
   }

   public boolean fvsAOg() {
      return this.lFt1ckP;
   }

   public boolean lFt1ckP() {
      return this.qU1QioDiC;
   }

   public enum KKeMvpuBgEY {
      v5suI,
      ibzfxic,
      wuhB,
      dOfFwvo,
      rsDd50tfV;

      public static ParticleSpec.KKeMvpuBgEY v5suI(String var0) {
         if (var0 == null) {
            return rsDd50tfV;
         }

         try {
            return valueOf(var0.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            return rsDd50tfV;
         }
      }
   }

   public enum Ol3TEDSog {
      v5suI,
      ibzfxic,
      wuhB,
      dOfFwvo,
      rsDd50tfV;

      public static ParticleSpec.Ol3TEDSog v5suI(String var0) {
         if (var0 == null) {
            return ibzfxic;
         }

         try {
            return valueOf(var0.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            return ibzfxic;
         }
      }
   }

   public enum Rdl0X5jeod {
      v5suI,
      ibzfxic,
      wuhB,
      dOfFwvo;

      public static ParticleSpec.Rdl0X5jeod v5suI(String var0) {
         if (var0 == null) {
            return ibzfxic;
         }

         try {
            return valueOf(var0.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            return ibzfxic;
         }
      }
   }
}
