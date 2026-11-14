package com.fpvdrone.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class DroneDefinition {
   /** Default optical zoom steps (the camera zoom state lives on the client). */
   public static final float[] DEFAULT_ZOOM_LEVELS = new float[]{1.0F, 3.0F, 7.0F};

   private final String wEJx3Itb;
   private final String bAcdvq9;
   private final DroneKind c1jHYPb4;
   private final float v8ZN;
   private final DroneDefinition.Physics zTT3nw6;
   private final DroneDefinition.Rates vmpmU;
   private final DroneDefinition.Render b69s;
   private final String cXHvq;
   private final String rxYnaa;
   private final float r6s7FL3;
   private final boolean iAKNPAj;
   private final String qlv6NKWnH;
   private final List<AttachableEntry> hOKMYHOaC;
   private final List<AttachableEntry> iS1JnZov;
   private final DroneDefinition.Mount lfyfLwuw;
   private final DroneDefinition.BombBay wyNDWDB;
   private final DroneDefinition.Camera iRWc2Pi;
   private final DroneDefinition.DamageBox tpzIHm;
   private final DroneDefinition.Hitbox eJDY;
   private final int zHBAQgZP;
   private final int we1GYY;
   private final DroneDefinition.DamageZones pxmzA;
   private final DistanceSound iElAbEZe;
   private final DroneDefinition.LoiterFlight sTrE;
   private final DroneDefinition.Collision ifpFOCaAs;
   private final DroneDefinition.Signal uQdlyiwB;
   private final DroneDefinition.Engine ivDR;
   private final Guidance n9lX8A;
   private final DroneRole wBXn;
   private final DroneDefinition.RocketPod mM7DsB9AR;
   private final DroneDefinition.Sync uKpZYj;
   private final List<InteractZone> l2IWOAGO;
   private final List<DroneDefinition.DamageSource> xYttSy;
   private final float kNoG;
   private final String fvsAOg;
   private final TooltipSpec lFt1ckP;
   public static final float CODEC = -1.0F;
   public static final List<DroneDefinition.DamageSource> ibzfxic = List.of(
      new DroneDefinition.DamageSource("tacz:bullet", null, 50.0F, null),
      new DroneDefinition.DamageSource("tacz:bullet_void", null, 50.0F, null),
      new DroneDefinition.DamageSource("tacz:bullet_ignore_armor", null, 70.0F, null),
      new DroneDefinition.DamageSource("tacz:bullet_void_ignore_armor", null, 70.0F, null)
   );
   public static final int wuhB = 6000;
   public static final int dOfFwvo = -1;
   public static final String rsDd50tfV = "geo/fpv_7d.geo.json";
   public static final String uSUb = "textures/entity/fpv_7d.png";
   private static final MapCodec<Integer> qU1QioDiC = Codec.FLOAT
      .optionalFieldOf("ItemLifetimeSeconds")
      .xmap(
         var0 -> var0.<Integer>map(var0x -> var0x <= 0.0F ? -1 : Math.round(var0x * 20.0F)).orElse(6000),
         var0 -> var0 <= 0 ? Optional.of(0.0F) : Optional.of(var0.intValue() / 20.0F)
      );
   private static final MapCodec<DroneDefinition.C9q0TgLlvZ> zQOhlhTZx = RecordCodecBuilder.mapCodec(
      var0 -> var0.group(
            Codec.STRING.fieldOf("DisplayName").forGetter(DroneDefinition.C9q0TgLlvZ::v5suI),
            DroneKind.wuhB.optionalFieldOf("Kind", DroneKind.v5suI).forGetter(DroneDefinition.C9q0TgLlvZ::ibzfxic),
            Codec.floatRange(0.0F, 1000000.0F).optionalFieldOf("BaseWeightGrams", 580.0F).forGetter(DroneDefinition.C9q0TgLlvZ::wuhB),
            DroneDefinition.Physics.ibzfxic.optionalFieldOf("Physics", DroneDefinition.Physics.v5suI).forGetter(DroneDefinition.C9q0TgLlvZ::dOfFwvo),
            DroneDefinition.Rates.ibzfxic.optionalFieldOf("Rates", DroneDefinition.Rates.v5suI).forGetter(DroneDefinition.C9q0TgLlvZ::rsDd50tfV),
            DroneDefinition.Render.ibzfxic.optionalFieldOf("Render", DroneDefinition.Render.v5suI).forGetter(DroneDefinition.C9q0TgLlvZ::uSUb),
            Codec.STRING.optionalFieldOf("Model", "geo/fpv_7d.geo.json").forGetter(DroneDefinition.C9q0TgLlvZ::jGhI2),
            Codec.STRING.optionalFieldOf("Texture", "textures/entity/fpv_7d.png").forGetter(DroneDefinition.C9q0TgLlvZ::wEJx3Itb),
            Codec.FLOAT.optionalFieldOf("ModelYawDegrees", 0.0F).forGetter(DroneDefinition.C9q0TgLlvZ::bAcdvq9),
            Codec.BOOL.optionalFieldOf("FlightMode3d", false).forGetter(DroneDefinition.C9q0TgLlvZ::c1jHYPb4),
            Codec.STRING.optionalFieldOf("DroneName", "KINDER").forGetter(DroneDefinition.C9q0TgLlvZ::v8ZN),
            DroneDefinition.DamageBox.ibzfxic.optionalFieldOf("DamageBox").forGetter(var0x -> Optional.ofNullable(var0x.zTT3nw6())),
            DroneDefinition.Hitbox.ibzfxic.optionalFieldOf("Hitbox").forGetter(var0x -> Optional.ofNullable(var0x.vmpmU())),
            qU1QioDiC.forGetter(DroneDefinition.C9q0TgLlvZ::b69s),
            Codec.intRange(0, 1000).optionalFieldOf("HitsToDowned", 0).forGetter(DroneDefinition.C9q0TgLlvZ::cXHvq),
            DroneDefinition.DamageZones.v5suI.optionalFieldOf("DamageZones").forGetter(var0x -> Optional.ofNullable(var0x.rxYnaa()))
         )
         .apply(
            var0,
            (var0x, var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var13, var14, var15) -> new DroneDefinition.C9q0TgLlvZ(
               var0x,
               var1,
               var2,
               var3,
               var4,
               var5,
               var6,
               var7,
               var8,
               var9,
               var10,
               (DroneDefinition.DamageBox)var11.orElse(null),
               (DroneDefinition.Hitbox)var12.orElse(null),
               var13,
               var14,
               (DroneDefinition.DamageZones)var15.orElse(null)
            )
         )
   );
   private static final MapCodec<DroneDefinition.Qz5WADR> lvk8rWUGD = RecordCodecBuilder.mapCodec(
      var0 -> var0.group(
            AttachableEntry.v5suI.listOf().optionalFieldOf("Batteries", List.of()).forGetter(DroneDefinition.Qz5WADR::v5suI),
            AttachableEntry.v5suI.listOf().optionalFieldOf("Projectiles", List.of()).forGetter(DroneDefinition.Qz5WADR::ibzfxic),
            DroneDefinition.Mount.v5suI.optionalFieldOf("Mount").forGetter(var0x -> Optional.ofNullable(var0x.wuhB())),
            DroneDefinition.BombBay.v5suI.optionalFieldOf("BombBay").forGetter(var0x -> Optional.ofNullable(var0x.dOfFwvo())),
            DroneDefinition.Camera.v5suI.optionalFieldOf("Camera").forGetter(var0x -> Optional.ofNullable(var0x.rsDd50tfV())),
            DistanceSound.wuhB.optionalFieldOf("Sounds").forGetter(var0x -> Optional.ofNullable(var0x.uSUb())),
            DroneDefinition.LoiterFlight.ibzfxic.optionalFieldOf("LoiterFlight").forGetter(var0x -> Optional.ofNullable(var0x.jGhI2())),
            DroneDefinition.Collision.v5suI.optionalFieldOf("Collision").forGetter(var0x -> Optional.ofNullable(var0x.wEJx3Itb())),
            DroneDefinition.Signal.ibzfxic.optionalFieldOf("Signal").forGetter(var0x -> Optional.ofNullable(var0x.bAcdvq9())),
            DroneDefinition.Engine.ibzfxic.optionalFieldOf("Engine").forGetter(var0x -> Optional.ofNullable(var0x.c1jHYPb4())),
            Guidance.dOfFwvo.optionalFieldOf("Guidance", Guidance.ibzfxic).forGetter(DroneDefinition.Qz5WADR::v8ZN),
            DroneRole.wuhB.optionalFieldOf("Role", DroneRole.v5suI).forGetter(DroneDefinition.Qz5WADR::zTT3nw6),
            DroneDefinition.RocketPod.v5suI.optionalFieldOf("RocketPod").forGetter(var0x -> Optional.ofNullable(var0x.vmpmU())),
            DroneDefinition.Sync.rsDd50tfV.optionalFieldOf("Sync").forGetter(var0x -> Optional.ofNullable(var0x.b69s())),
            InteractZone.ibzfxic.listOf().optionalFieldOf("InteractZones", List.of()).forGetter(DroneDefinition.Qz5WADR::cXHvq),
            DroneDefinition.DamageSource.v5suI.listOf().optionalFieldOf("DamageSources", ibzfxic).forGetter(DroneDefinition.Qz5WADR::rxYnaa)
         )
         .apply(
            var0,
            (var0x, var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var13, var14, var15) -> new DroneDefinition.Qz5WADR(
               var0x,
               var1,
               (DroneDefinition.Mount)var2.orElse(null),
               (DroneDefinition.BombBay)var3.orElse(null),
               (DroneDefinition.Camera)var4.orElse(null),
               (DistanceSound)var5.orElse(null),
               (DroneDefinition.LoiterFlight)var6.orElse(null),
               (DroneDefinition.Collision)var7.orElse(null),
               (DroneDefinition.Signal)var8.orElse(null),
               (DroneDefinition.Engine)var9.orElse(null),
               var10,
               var11,
               (DroneDefinition.RocketPod)var12.orElse(null),
               (DroneDefinition.Sync)var13.orElse(null),
               var14,
               var15
            )
         )
   );
   private static final MapCodec<Float> pyHY5 = Codec.floatRange(-1.0F, 1000000.0F).optionalFieldOf("VehicleDamage", -1.0F);
   private static final MapCodec<DroneDefinition.Ifr7Wzf3> idXga = RecordCodecBuilder.mapCodec(
      var0 -> var0.group(
            Codec.STRING.optionalFieldOf("Item", "").forGetter(DroneDefinition.Ifr7Wzf3::v5suI),
            TooltipSpec.CODEC.optionalFieldOf("Tooltip").forGetter(var0x -> Optional.ofNullable(var0x.ibzfxic()))
         )
         .apply(var0, (var0x, var1) -> new DroneDefinition.Ifr7Wzf3(var0x, (TooltipSpec)var1.orElse(null)))
   );
   public static final Codec<DroneDefinition> jGhI2 = Codec.mapPair(Codec.mapPair(Codec.mapPair(zQOhlhTZx, lvk8rWUGD), pyHY5), idXga)
      .codec()
      .xmap(
         var0 -> {
            DroneDefinition.C9q0TgLlvZ var1 = (DroneDefinition.C9q0TgLlvZ)((Pair)((Pair)var0.getFirst()).getFirst()).getFirst();
            DroneDefinition.Qz5WADR var2 = (DroneDefinition.Qz5WADR)((Pair)((Pair)var0.getFirst()).getFirst()).getSecond();
            float var3 = (Float)((Pair)var0.getFirst()).getSecond();
            DroneDefinition.Ifr7Wzf3 var4 = (DroneDefinition.Ifr7Wzf3)var0.getSecond();
            List var5 = var2.v5suI().isEmpty() ? List.of(new AttachableEntry("default", "item.fpvdrone.battery")) : var2.v5suI();
            return new DroneDefinition(
               "",
               var1.v5suI(),
               var1.ibzfxic(),
               var1.wuhB(),
               var1.dOfFwvo(),
               var1.rsDd50tfV(),
               var1.uSUb(),
               var1.jGhI2(),
               var1.wEJx3Itb(),
               var1.bAcdvq9(),
               var1.c1jHYPb4(),
               var1.v8ZN(),
               var5,
               var2.ibzfxic(),
               var2.wuhB(),
               var2.dOfFwvo(),
               var2.rsDd50tfV(),
               var1.zTT3nw6(),
               var1.vmpmU(),
               var1.b69s(),
               var1.cXHvq(),
               var1.rxYnaa(),
               var2.uSUb(),
               var2.jGhI2(),
               var2.wEJx3Itb(),
               var2.bAcdvq9(),
               var2.c1jHYPb4(),
               var2.v8ZN(),
               var2.zTT3nw6(),
               var2.vmpmU(),
               var2.b69s(),
               var2.cXHvq(),
               var2.rxYnaa(),
               var3,
               var4.v5suI(),
               var4.ibzfxic()
            );
         },
         var0 -> Pair.of(
            Pair.of(
               Pair.of(
                  new DroneDefinition.C9q0TgLlvZ(
                     var0.iRWc2Pi(),
                     var0.tpzIHm(),
                     var0.eJDY(),
                     var0.zHBAQgZP(),
                     var0.we1GYY(),
                     var0.pxmzA(),
                     var0.iElAbEZe(),
                     var0.sTrE(),
                     var0.ifpFOCaAs(),
                     var0.uQdlyiwB(),
                     var0.ivDR(),
                     var0.xYttSy(),
                     var0.kNoG(),
                     var0.fvsAOg(),
                     var0.lFt1ckP(),
                     var0.qU1QioDiC()
                  ),
                  new DroneDefinition.Qz5WADR(
                     var0.n9lX8A(),
                     var0.wBXn(),
                     var0.mM7DsB9AR(),
                     var0.uKpZYj(),
                     var0.l2IWOAGO(),
                     var0.zQOhlhTZx(),
                     var0.lvk8rWUGD(),
                     var0.pyHY5(),
                     var0.idXga(),
                     var0.i263D9(),
                     var0.rOIp0p7(),
                     var0.iKXOcc(),
                     var0.jt9oI60ST(),
                     var0.oEuCBB(),
                     var0.uj1PcjLg(),
                     var0.zgQgCA9()
                  )
               ),
               var0.qV33e()
            ),
            new DroneDefinition.Ifr7Wzf3(var0.xogV5KBhg(), var0.rYlh())
         )
      );

   public DroneDefinition(
      String var1,
      String var2,
      DroneKind var3,
      float var4,
      DroneDefinition.Physics var5,
      DroneDefinition.Rates var6,
      DroneDefinition.Render var7,
      String var8,
      String var9,
      float var10,
      boolean var11,
      String var12,
      List<AttachableEntry> var13,
      List<AttachableEntry> var14,
      DroneDefinition.Mount var15,
      DroneDefinition.BombBay var16,
      DroneDefinition.Camera var17,
      DroneDefinition.DamageBox var18,
      DroneDefinition.Hitbox var19,
      int var20,
      int var21,
      DroneDefinition.DamageZones var22,
      DistanceSound var23,
      DroneDefinition.LoiterFlight var24,
      DroneDefinition.Collision var25,
      DroneDefinition.Signal var26,
      DroneDefinition.Engine var27,
      Guidance var28,
      DroneRole var29,
      DroneDefinition.RocketPod var30,
      DroneDefinition.Sync var31,
      List<InteractZone> var32,
      List<DroneDefinition.DamageSource> var33,
      float var34,
      String var35,
      TooltipSpec var36
   ) {
      this.wEJx3Itb = var1;
      this.bAcdvq9 = var2;
      this.c1jHYPb4 = var3;
      this.v8ZN = var4;
      this.zTT3nw6 = var5;
      this.vmpmU = var6;
      this.b69s = var7;
      this.cXHvq = var8;
      this.rxYnaa = var9;
      this.r6s7FL3 = var10;
      this.iAKNPAj = var11;
      this.qlv6NKWnH = var12;
      this.hOKMYHOaC = var13;
      this.iS1JnZov = var14;
      this.lfyfLwuw = var15;
      this.wyNDWDB = var16;
      this.iRWc2Pi = var17;
      this.tpzIHm = var18;
      this.eJDY = var19;
      this.zHBAQgZP = var20;
      this.we1GYY = var21;
      this.pxmzA = var22;
      this.iElAbEZe = var23;
      this.sTrE = var24;
      this.ifpFOCaAs = var25;
      this.uQdlyiwB = var26;
      this.ivDR = var27;
      this.n9lX8A = var28;
      this.wBXn = var29;
      this.mM7DsB9AR = var30;
      this.uKpZYj = var31;
      this.l2IWOAGO = var32;
      this.xYttSy = var33;
      this.kNoG = var34;
      this.fvsAOg = var35;
      this.lFt1ckP = var36;
   }

   public boolean v5suI() {
      return this.zHBAQgZP <= 0;
   }

   public boolean ibzfxic() {
      return this.we1GYY > 0 && this.pxmzA == null;
   }

   public boolean wuhB() {
      return this.pxmzA != null;
   }

   public float v5suI(net.minecraft.world.damagesource.DamageSource var1, float var2) {
      if (this.xYttSy.isEmpty()) {
         return var2;
      }

      String var3 = var1.typeHolder().unwrapKey().map(var0 -> var0.identifier().toString()).orElse(null);
      Entity var4 = var1.getDirectEntity();
      Entity var5 = var4 != null ? var4 : var1.getEntity();
      String var6 = var5 != null ? EntityType.getKey(var5.getType()).toString() : null;

      for (DroneDefinition.DamageSource var8 : this.xYttSy) {
         if ((var8.v5suI() != null || var8.ibzfxic() != null)
            && (var8.v5suI() == null || var8.v5suI().equals(var3))
            && (var8.ibzfxic() == null || var8.ibzfxic().equals(var6))) {
            if (var8.wuhB() != null) {
               return var8.wuhB();
            } else {
               return var8.dOfFwvo() != null ? var2 * var8.dOfFwvo() : var2;
            }
         }
      }

      return var2;
   }

   public DroneDefinition v5suI(String var1) {
      return new DroneDefinition(
         var1,
         this.bAcdvq9,
         this.c1jHYPb4,
         this.v8ZN,
         this.zTT3nw6,
         this.vmpmU,
         this.b69s,
         this.cXHvq,
         this.rxYnaa,
         this.r6s7FL3,
         this.iAKNPAj,
         this.qlv6NKWnH,
         this.hOKMYHOaC,
         this.iS1JnZov,
         this.lfyfLwuw,
         this.wyNDWDB,
         this.iRWc2Pi,
         this.tpzIHm,
         this.eJDY,
         this.zHBAQgZP,
         this.we1GYY,
         this.pxmzA,
         this.iElAbEZe,
         this.sTrE,
         this.ifpFOCaAs,
         this.uQdlyiwB,
         this.ivDR,
         this.n9lX8A,
         this.wBXn,
         this.mM7DsB9AR,
         this.uKpZYj,
         this.l2IWOAGO,
         this.xYttSy,
         this.kNoG,
         this.fvsAOg,
         this.lFt1ckP
      );
   }

   public DroneDefinition v5suI(DroneDefinition.Engine var1) {
      return new DroneDefinition(
         this.wEJx3Itb,
         this.bAcdvq9,
         this.c1jHYPb4,
         this.v8ZN,
         this.zTT3nw6,
         this.vmpmU,
         this.b69s,
         this.cXHvq,
         this.rxYnaa,
         this.r6s7FL3,
         this.iAKNPAj,
         this.qlv6NKWnH,
         this.hOKMYHOaC,
         this.iS1JnZov,
         this.lfyfLwuw,
         this.wyNDWDB,
         this.iRWc2Pi,
         this.tpzIHm,
         this.eJDY,
         this.zHBAQgZP,
         this.we1GYY,
         this.pxmzA,
         this.iElAbEZe,
         this.sTrE,
         this.ifpFOCaAs,
         this.uQdlyiwB,
         var1,
         this.n9lX8A,
         this.wBXn,
         this.mM7DsB9AR,
         this.uKpZYj,
         this.l2IWOAGO,
         this.xYttSy,
         this.kNoG,
         this.fvsAOg,
         this.lFt1ckP
      );
   }

   public DroneDefinition v5suI(DroneDefinition.RocketPod var1) {
      return new DroneDefinition(
         this.wEJx3Itb,
         this.bAcdvq9,
         this.c1jHYPb4,
         this.v8ZN,
         this.zTT3nw6,
         this.vmpmU,
         this.b69s,
         this.cXHvq,
         this.rxYnaa,
         this.r6s7FL3,
         this.iAKNPAj,
         this.qlv6NKWnH,
         this.hOKMYHOaC,
         this.iS1JnZov,
         this.lfyfLwuw,
         this.wyNDWDB,
         this.iRWc2Pi,
         this.tpzIHm,
         this.eJDY,
         this.zHBAQgZP,
         this.we1GYY,
         this.pxmzA,
         this.iElAbEZe,
         this.sTrE,
         this.ifpFOCaAs,
         this.uQdlyiwB,
         this.ivDR,
         this.n9lX8A,
         this.wBXn,
         var1,
         this.uKpZYj,
         this.l2IWOAGO,
         this.xYttSy,
         this.kNoG,
         this.fvsAOg,
         this.lFt1ckP
      );
   }

   public DroneDefinition v5suI(DroneDefinition.LoiterFlight var1) {
      return new DroneDefinition(
         this.wEJx3Itb,
         this.bAcdvq9,
         this.c1jHYPb4,
         this.v8ZN,
         this.zTT3nw6,
         this.vmpmU,
         this.b69s,
         this.cXHvq,
         this.rxYnaa,
         this.r6s7FL3,
         this.iAKNPAj,
         this.qlv6NKWnH,
         this.hOKMYHOaC,
         this.iS1JnZov,
         this.lfyfLwuw,
         this.wyNDWDB,
         this.iRWc2Pi,
         this.tpzIHm,
         this.eJDY,
         this.zHBAQgZP,
         this.we1GYY,
         this.pxmzA,
         this.iElAbEZe,
         var1,
         this.ifpFOCaAs,
         this.uQdlyiwB,
         this.ivDR,
         this.n9lX8A,
         this.wBXn,
         this.mM7DsB9AR,
         this.uKpZYj,
         this.l2IWOAGO,
         this.xYttSy,
         this.kNoG,
         this.fvsAOg,
         this.lFt1ckP
      );
   }

   public boolean dOfFwvo() {
      return this.c1jHYPb4 == DroneKind.ibzfxic;
   }

   public boolean rsDd50tfV() {
      return this.c1jHYPb4 == DroneKind.v5suI;
   }

   public boolean uSUb() {
      return this.lfyfLwuw != null;
   }

   public boolean jGhI2() {
      return this.wyNDWDB != null && !this.wyNDWDB.stations().isEmpty();
   }

   public boolean wEJx3Itb() {
      return this.mM7DsB9AR != null && !this.mM7DsB9AR.rsDd50tfV().isEmpty();
   }

   public DroneDefinition.Camera bAcdvq9() {
      return this.iRWc2Pi != null ? this.iRWc2Pi : DroneDefinition.Camera.v5suI(this.c1jHYPb4);
   }

   public List<InteractZone> c1jHYPb4() {
      LinkedHashMap var1 = new LinkedHashMap();
      if (this.wEJx3Itb()) {
         for (PayloadStation var3 : this.mM7DsB9AR.rsDd50tfV()) {
            var1.put(var3.ibzfxic(), new InteractZone(var3.ibzfxic(), var3.ibzfxic(), InteractZone.Trigger.v5suI, "mountPointLoadout", null));
         }

         for (RocketMount var7 : this.mM7DsB9AR.ibzfxic()) {
            if (var7.ibzfxic() != null && !var7.ibzfxic().isEmpty()) {
               var1.put(var7.v5suI(), new InteractZone(var7.v5suI(), var7.ibzfxic(), InteractZone.Trigger.v5suI, "rocketPodMount", null));
            }
         }
      }

      if (this.jGhI2()) {
         for (PayloadStation var8 : this.wyNDWDB.stations()) {
            var1.put(var8.ibzfxic(), new InteractZone(var8.ibzfxic(), var8.ibzfxic(), InteractZone.Trigger.v5suI, "mountPointLoadout", null));
         }
      }

      for (InteractZone var9 : this.l2IWOAGO) {
         var1.put(var9.v5suI(), var9);
      }

      return var1.isEmpty() ? List.of() : List.copyOf(var1.values());
   }

   public boolean v8ZN() {
      return this.n9lX8A.wuhB();
   }

   public boolean zTT3nw6() {
      return this.wBXn == DroneRole.ibzfxic;
   }

   public DroneDefinition.Signal vmpmU() {
      return this.uQdlyiwB != null ? this.uQdlyiwB : DroneDefinition.Signal.v5suI;
   }

   public DroneDefinition.Sync b69s() {
      return this.uKpZYj != null ? this.uKpZYj : DroneDefinition.Sync.v5suI;
   }

   public DroneDefinition.Engine cXHvq() {
      return this.ivDR != null ? this.ivDR : DroneDefinition.Engine.v5suI;
   }

   public String rxYnaa() {
      return this.fvsAOg != null && !this.fvsAOg.isEmpty() ? this.fvsAOg : "fpvdrone:" + this.wEJx3Itb;
   }

   public TooltipSpec r6s7FL3() {
      return this.lFt1ckP != null ? this.lFt1ckP : TooltipSpec.v5suI;
   }

   public DistanceSound iAKNPAj() {
      return this.iElAbEZe != null ? this.iElAbEZe : DistanceSound.v5suI;
   }

   public DroneDefinition.LoiterFlight qlv6NKWnH() {
      return this.sTrE != null ? this.sTrE : DroneDefinition.LoiterFlight.v5suI;
   }

   public String hOKMYHOaC() {
      if (this.ifpFOCaAs != null) {
         if (!this.ifpFOCaAs.ibzfxic()) {
            return "";
         }

         if (this.ifpFOCaAs.v5suI() != null && !this.ifpFOCaAs.v5suI().isEmpty()) {
            return this.ifpFOCaAs.v5suI();
         }
      }

      return ibzfxic(this.cXHvq);
   }

   static String ibzfxic(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         int var1 = var0.lastIndexOf(47);
         String var2 = var1 >= 0 ? var0.substring(0, var1) : "geo";
         String var3 = var1 >= 0 ? var0.substring(var1 + 1) : var0;
         return var2 + "/collision/" + var3;
      } else {
         return "";
      }
   }

   public DroneDefinition.DamageBox iS1JnZov() {
      return this.tpzIHm != null ? this.tpzIHm : DroneDefinition.DamageBox.v5suI;
   }

   public DroneDefinition.Hitbox lfyfLwuw() {
      return this.eJDY != null ? this.eJDY : DroneDefinition.Hitbox.v5suI;
   }

   public boolean wuhB(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         for (AttachableEntry var3 : this.iS1JnZov) {
            if (var3.v5suI().equals(var1)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public Optional<AttachTransform> dOfFwvo(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         for (AttachableEntry var3 : this.iS1JnZov) {
            if (var3.v5suI().equals(var1)) {
               return Optional.ofNullable(var3.wuhB());
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public boolean rsDd50tfV(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         for (AttachableEntry var3 : this.hOKMYHOaC) {
            if (var3.v5suI().equals(var1)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public Optional<AttachTransform> uSUb(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         for (AttachableEntry var3 : this.hOKMYHOaC) {
            if (var3.v5suI().equals(var1)) {
               return Optional.ofNullable(var3.wuhB());
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public String wyNDWDB() {
      return this.wEJx3Itb;
   }

   public String iRWc2Pi() {
      return this.bAcdvq9;
   }

   public DroneKind tpzIHm() {
      return this.c1jHYPb4;
   }

   public float eJDY() {
      return this.v8ZN;
   }

   public DroneDefinition.Physics zHBAQgZP() {
      return this.zTT3nw6;
   }

   public DroneDefinition.Rates we1GYY() {
      return this.vmpmU;
   }

   public DroneDefinition.Render pxmzA() {
      return this.b69s;
   }

   public String iElAbEZe() {
      return this.cXHvq;
   }

   public String sTrE() {
      return this.rxYnaa;
   }

   public float ifpFOCaAs() {
      return this.r6s7FL3;
   }

   public boolean uQdlyiwB() {
      return this.iAKNPAj;
   }

   public String ivDR() {
      return this.qlv6NKWnH;
   }

   public List<AttachableEntry> n9lX8A() {
      return this.hOKMYHOaC;
   }

   public List<AttachableEntry> wBXn() {
      return this.iS1JnZov;
   }

   public DroneDefinition.Mount mM7DsB9AR() {
      return this.lfyfLwuw;
   }

   public DroneDefinition.BombBay uKpZYj() {
      return this.wyNDWDB;
   }

   public DroneDefinition.Camera l2IWOAGO() {
      return this.iRWc2Pi;
   }

   public DroneDefinition.DamageBox xYttSy() {
      return this.tpzIHm;
   }

   public DroneDefinition.Hitbox kNoG() {
      return this.eJDY;
   }

   public int fvsAOg() {
      return this.zHBAQgZP;
   }

   public int lFt1ckP() {
      return this.we1GYY;
   }

   public DroneDefinition.DamageZones qU1QioDiC() {
      return this.pxmzA;
   }

   public DistanceSound zQOhlhTZx() {
      return this.iElAbEZe;
   }

   public DroneDefinition.LoiterFlight lvk8rWUGD() {
      return this.sTrE;
   }

   public DroneDefinition.Collision pyHY5() {
      return this.ifpFOCaAs;
   }

   public DroneDefinition.Signal idXga() {
      return this.uQdlyiwB;
   }

   public DroneDefinition.Engine i263D9() {
      return this.ivDR;
   }

   public Guidance rOIp0p7() {
      return this.n9lX8A;
   }

   public DroneRole iKXOcc() {
      return this.wBXn;
   }

   public DroneDefinition.RocketPod jt9oI60ST() {
      return this.mM7DsB9AR;
   }

   public DroneDefinition.Sync oEuCBB() {
      return this.uKpZYj;
   }

   public List<InteractZone> uj1PcjLg() {
      return this.l2IWOAGO;
   }

   public List<DroneDefinition.DamageSource> zgQgCA9() {
      return this.xYttSy;
   }

   public float qV33e() {
      return this.kNoG;
   }

   public String xogV5KBhg() {
      return this.fvsAOg;
   }

   public TooltipSpec rYlh() {
      return this.lFt1ckP;
   }

   public static final class BombBay {
      private final String projectile;
      private final List<PayloadStation> stations;
      public static final Codec<DroneDefinition.BombBay> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.STRING.fieldOf("Projectile").forGetter(DroneDefinition.BombBay::projectile),
               PayloadStation.v5suI.listOf().fieldOf("Stations").forGetter(DroneDefinition.BombBay::stations)
            )
            .apply(var0, DroneDefinition.BombBay::new)
      );

      public BombBay(String var1, List<PayloadStation> var2) {
         this.projectile = var1;
         this.stations = var2;
      }

      public String projectile() {
         return this.projectile;
      }

      public List<PayloadStation> stations() {
         return this.stations;
      }

   }

   private static final class C9q0TgLlvZ {
      private final String v5suI;
      private final DroneKind ibzfxic;
      private final float wuhB;
      private final DroneDefinition.Physics dOfFwvo;
      private final DroneDefinition.Rates rsDd50tfV;
      private final DroneDefinition.Render uSUb;
      private final String jGhI2;
      private final String wEJx3Itb;
      private final float bAcdvq9;
      private final boolean isReRender;
      private final String v8ZN;
      private final DroneDefinition.DamageBox zTT3nw6;
      private final DroneDefinition.Hitbox vmpmU;
      private final int b69s;
      private final int cXHvq;
      private final DroneDefinition.DamageZones rxYnaa;

      private C9q0TgLlvZ(
         String var1,
         DroneKind var2,
         float var3,
         DroneDefinition.Physics var4,
         DroneDefinition.Rates var5,
         DroneDefinition.Render var6,
         String var7,
         String var8,
         float var9,
         boolean var10,
         String var11,
         DroneDefinition.DamageBox var12,
         DroneDefinition.Hitbox var13,
         int var14,
         int var15,
         DroneDefinition.DamageZones var16
      ) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var5;
         this.uSUb = var6;
         this.jGhI2 = var7;
         this.wEJx3Itb = var8;
         this.bAcdvq9 = var9;
         this.isReRender = var10;
         this.v8ZN = var11;
         this.zTT3nw6 = var12;
         this.vmpmU = var13;
         this.b69s = var14;
         this.cXHvq = var15;
         this.rxYnaa = var16;
      }

      public boolean c1jHYPb4() {
         return this.isReRender;
      }

      public String v5suI() {
         return this.v5suI;
      }

      public DroneKind ibzfxic() {
         return this.ibzfxic;
      }

      public float wuhB() {
         return this.wuhB;
      }

      public DroneDefinition.Physics dOfFwvo() {
         return this.dOfFwvo;
      }

      public DroneDefinition.Rates rsDd50tfV() {
         return this.rsDd50tfV;
      }

      public DroneDefinition.Render uSUb() {
         return this.uSUb;
      }

      public String jGhI2() {
         return this.jGhI2;
      }

      public String wEJx3Itb() {
         return this.wEJx3Itb;
      }

      public float bAcdvq9() {
         return this.bAcdvq9;
      }

      public String v8ZN() {
         return this.v8ZN;
      }

      public DroneDefinition.DamageBox zTT3nw6() {
         return this.zTT3nw6;
      }

      public DroneDefinition.Hitbox vmpmU() {
         return this.vmpmU;
      }

      public int b69s() {
         return this.b69s;
      }

      public int cXHvq() {
         return this.cXHvq;
      }

      public DroneDefinition.DamageZones rxYnaa() {
         return this.rxYnaa;
      }

   }

   public static final class Camera {
      private final float fovDegrees;
      private final boolean showOsd;
      private final boolean gimbal;
      private final boolean thermal;
      private final DroneDefinition.OsdProfile osdProfile;
      private final DroneDefinition.GimbalLimits gimbalLimits;
      private final boolean opticalZoom;
      private final float[] zoom;
      private final boolean thermalToggle;
      private final float zoomBlur;
      private final float camFwd;
      private final float camUp;
      private final float camSide;
      private final boolean freeLook;
      private static final float rxYnaa = 1.4375F;
      private static final float r6s7FL3 = 0.25F;
      private static final float iAKNPAj = 0.0F;
      private static final Codec<float[]> qlv6NKWnH = Codec.FLOAT.listOf().xmap(var0 -> {
         float[] var1 = new float[var0.size()];

         for (int var2 = 0; var2 < var1.length; var2++) {
            var1[var2] = (Float)var0.get(var2);
         }

         return var1;
      }, var0 -> {
         ArrayList var1 = new ArrayList(var0.length);

         for (float var5 : var0) {
            var1.add(var5);
         }

         return var1;
      });
      private static final DroneDefinition.Camera hOKMYHOaC = v5suI(DroneKind.v5suI);
      public static final Codec<DroneDefinition.Camera> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.floatRange(0.0F, 180.0F).optionalFieldOf("FovDegrees", hOKMYHOaC.fovDegrees()).forGetter(DroneDefinition.Camera::fovDegrees),
               Codec.BOOL.optionalFieldOf("ShowOsd", hOKMYHOaC.showOsd()).forGetter(DroneDefinition.Camera::showOsd),
               Codec.BOOL.optionalFieldOf("Gimbal", hOKMYHOaC.gimbal()).forGetter(DroneDefinition.Camera::gimbal),
               Codec.BOOL.optionalFieldOf("Thermal", hOKMYHOaC.thermal()).forGetter(DroneDefinition.Camera::thermal),
               DroneDefinition.OsdProfile.wuhB.optionalFieldOf("OsdProfile", hOKMYHOaC.osdProfile()).forGetter(DroneDefinition.Camera::osdProfile),
               DroneDefinition.GimbalLimits.ibzfxic.optionalFieldOf("GimbalLimits", hOKMYHOaC.gimbalLimits()).forGetter(DroneDefinition.Camera::gimbalLimits),
               Codec.BOOL.optionalFieldOf("OpticalZoom", hOKMYHOaC.opticalZoom()).forGetter(DroneDefinition.Camera::opticalZoom),
               qlv6NKWnH.optionalFieldOf("Zoom", hOKMYHOaC.zoom()).forGetter(DroneDefinition.Camera::zoom),
               Codec.BOOL.optionalFieldOf("ThermalToggle", hOKMYHOaC.thermalToggle()).forGetter(DroneDefinition.Camera::thermalToggle),
               Codec.floatRange(0.0F, 16.0F).optionalFieldOf("ZoomBlur", hOKMYHOaC.zoomBlur()).forGetter(DroneDefinition.Camera::zoomBlur),
               Codec.FLOAT.optionalFieldOf("CamFwd", hOKMYHOaC.camFwd()).forGetter(DroneDefinition.Camera::camFwd),
               Codec.FLOAT.optionalFieldOf("CamUp", hOKMYHOaC.camUp()).forGetter(DroneDefinition.Camera::camUp),
               Codec.FLOAT.optionalFieldOf("CamSide", hOKMYHOaC.camSide()).forGetter(DroneDefinition.Camera::camSide),
               Codec.BOOL.optionalFieldOf("FreeLook", hOKMYHOaC.freeLook()).forGetter(DroneDefinition.Camera::freeLook)
            )
            .apply(var0, DroneDefinition.Camera::new)
      );

      public Camera(
         float var1,
         boolean var2,
         boolean var3,
         boolean var4,
         DroneDefinition.OsdProfile var5,
         DroneDefinition.GimbalLimits var6,
         boolean var7,
         float[] var8,
         boolean var9,
         float var10,
         float var11,
         float var12,
         float var13,
         boolean var14
      ) {
         if (var5 == null) {
            var5 = DroneDefinition.OsdProfile.v5suI;
         }

         if (var6 == null) {
            var6 = DroneDefinition.GimbalLimits.v5suI;
         }

         if (var8 == null || var8.length == 0) {
            var8 = DroneDefinition.DEFAULT_ZOOM_LEVELS;
         }

         var10 = Math.max(0.0F, var10);
         this.fovDegrees = var1;
         this.showOsd = var2;
         this.gimbal = var3;
         this.thermal = var4;
         this.osdProfile = var5;
         this.gimbalLimits = var6;
         this.opticalZoom = var7;
         this.zoom = var8;
         this.thermalToggle = var9;
         this.zoomBlur = var10;
         this.camFwd = var11;
         this.camUp = var12;
         this.camSide = var13;
         this.freeLook = var14;
      }

      public static DroneDefinition.Camera v5suI(DroneKind var0) {
         return switch (var0) {
            case ibzfxic -> new DroneDefinition.Camera(
               0.0F,
               false,
               true,
               false,
               DroneDefinition.OsdProfile.v5suI,
               DroneDefinition.GimbalLimits.v5suI,
               false,
               DroneDefinition.DEFAULT_ZOOM_LEVELS,
               false,
               0.0F,
               1.4375F,
               0.25F,
               0.0F,
               false
            );
            case v5suI -> new DroneDefinition.Camera(
               0.0F,
               true,
               false,
               false,
               DroneDefinition.OsdProfile.v5suI,
               DroneDefinition.GimbalLimits.v5suI,
               false,
               DroneDefinition.DEFAULT_ZOOM_LEVELS,
               false,
               0.0F,
               1.4375F,
               0.25F,
               0.0F,
               false
            );
         };
      }

      public boolean v5suI() {
         return this.fovDegrees <= 0.0F;
      }

      public float fovDegrees() {
         return this.fovDegrees;
      }

      public boolean showOsd() {
         return this.showOsd;
      }

      public boolean gimbal() {
         return this.gimbal;
      }

      public boolean thermal() {
         return this.thermal;
      }

      public DroneDefinition.OsdProfile osdProfile() {
         return this.osdProfile;
      }

      public DroneDefinition.GimbalLimits gimbalLimits() {
         return this.gimbalLimits;
      }

      public boolean opticalZoom() {
         return this.opticalZoom;
      }

      public float[] zoom() {
         return this.zoom;
      }

      public boolean thermalToggle() {
         return this.thermalToggle;
      }

      public float zoomBlur() {
         return this.zoomBlur;
      }

      public float camFwd() {
         return this.camFwd;
      }

      public float camUp() {
         return this.camUp;
      }

      public float camSide() {
         return this.camSide;
      }

      public boolean freeLook() {
         return this.freeLook;
      }

   }

   public static final class Collision {
      private final String ibzfxic;
      private final boolean wuhB;
      private final Map<String, Double> dOfFwvo;
      private final float[] rsDd50tfV;
      private static final Codec<float[]> uSUb = Codec.FLOAT
         .listOf()
         .xmap(
            var0 -> var0.size() == 3 ? new float[]{(Float)var0.get(0), (Float)var0.get(1), (Float)var0.get(2)} : null,
            var0 -> List.of(var0[0], var0[1], var0[2])
         );
      public static final Codec<DroneDefinition.Collision> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.STRING.optionalFieldOf("Model", "").forGetter(DroneDefinition.Collision::v5suI),
               Codec.BOOL.optionalFieldOf("Enabled", true).forGetter(DroneDefinition.Collision::ibzfxic),
               Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("BoneMass", Map.of()).forGetter(DroneDefinition.Collision::wuhB),
               uSUb.optionalFieldOf("CenterOfMass").forGetter(var0x -> Optional.ofNullable(var0x.dOfFwvo()))
            )
            .apply(var0, (var0x, var1, var2, var3) -> new DroneDefinition.Collision(var0x, var1, var2, (float[])var3.orElse(null)))
      );

      public Collision(String var1, boolean var2, Map<String, Double> var3, float[] var4) {
         var3 = v5suI(var3);
         var4 = var4 != null && var4.length == 3 ? new float[]{var4[0], var4[1], var4[2]} : null;
         this.ibzfxic = var1;
         this.wuhB = var2;
         this.dOfFwvo = var3;
         this.rsDd50tfV = var4;
      }

      public Collision(String var1, boolean var2) {
         this(var1, var2, Map.of(), null);
      }

      public Collision(String var1, boolean var2, Map<String, Double> var3) {
         this(var1, var2, var3, null);
      }

      private static Map<String, Double> v5suI(Map<String, Double> var0) {
         if (var0 != null && !var0.isEmpty()) {
            HashMap var1 = new HashMap();

            for (Entry<?, ?> var3 : var0.entrySet()) {
               if (var3.getValue() != null && (Double)var3.getValue() > 0.0) {
                  var1.put(((String)var3.getKey()).toLowerCase(Locale.ROOT), (Double)var3.getValue());
               }
            }

            return Map.copyOf(var1);
         } else {
            return Map.of();
         }
      }

      public String v5suI() {
         return this.ibzfxic;
      }

      public boolean ibzfxic() {
         return this.wuhB;
      }

      public Map<String, Double> wuhB() {
         return this.dOfFwvo;
      }

      public float[] dOfFwvo() {
         return this.rsDd50tfV;
      }
   }

   public enum ControlScheme {
      v5suI("stick"),
      ibzfxic("click_designate"),
      wuhB("gimbal_observe");

      private final String rsDd50tfV;
      public static final Codec<DroneDefinition.ControlScheme> dOfFwvo = Codec.STRING
         .xmap(DroneDefinition.ControlScheme::v5suI, DroneDefinition.ControlScheme::v5suI);

      ControlScheme(String var3) {
         this.rsDd50tfV = var3;
      }

      public String v5suI() {
         return this.rsDd50tfV;
      }

      public static DroneDefinition.ControlScheme v5suI(String var0) {
         if (var0 != null) {
            String var1 = var0.trim().toLowerCase(Locale.ROOT);
            if (var1.equals("designate") || var1.equals("click") || var1.equals("zala") || var1.equals("seeker")) {
               return ibzfxic;
            }

            if (var1.equals("observe") || var1.equals("recon") || var1.equals("camera_operator")) {
               return wuhB;
            }

            for (DroneDefinition.ControlScheme var5 : values()) {
               if (var5.rsDd50tfV.equals(var1)) {
                  return var5;
               }
            }
         }

         return v5suI;
      }
   }

   public static final class DamageBox {
      private final float model;
      private final float enabled;
      public static final DroneDefinition.DamageBox v5suI = new DroneDefinition.DamageBox(0.18F, 0.14F);
      public static final Codec<DroneDefinition.DamageBox> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.floatRange(0.01F, 5.0F).optionalFieldOf("HalfWidth", v5suI.model()).forGetter(DroneDefinition.DamageBox::model),
               Codec.floatRange(0.01F, 5.0F).optionalFieldOf("HalfHeight", v5suI.enabled()).forGetter(DroneDefinition.DamageBox::enabled)
            )
            .apply(var0, DroneDefinition.DamageBox::new)
      );

      public DamageBox(float var1, float var2) {
         this.model = var1;
         this.enabled = var2;
      }

      public float model() {
         return this.model;
      }

      public float enabled() {
         return this.enabled;
      }

   }

   public static final class DamageSource {
      private final String ibzfxic;
      private final String wuhB;
      private final Float dOfFwvo;
      private final Float rsDd50tfV;
      public static final Codec<DroneDefinition.DamageSource> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.STRING.optionalFieldOf("Type").forGetter(var0x -> Optional.ofNullable(var0x.v5suI())),
               Codec.STRING.optionalFieldOf("Entity").forGetter(var0x -> Optional.ofNullable(var0x.ibzfxic())),
               Codec.FLOAT.optionalFieldOf("Damage").forGetter(var0x -> Optional.ofNullable(var0x.wuhB())),
               Codec.FLOAT.optionalFieldOf("Multiplier").forGetter(var0x -> Optional.ofNullable(var0x.dOfFwvo()))
            )
            .apply(
               var0,
               (var0x, var1, var2, var3) -> new DroneDefinition.DamageSource(
                  (String)var0x.orElse(null), (String)var1.orElse(null), (Float)var2.orElse(null), (Float)var3.orElse(null)
               )
            )
      );

      public DamageSource(String var1, String var2, Float var3, Float var4) {
         this.ibzfxic = var1;
         this.wuhB = var2;
         this.dOfFwvo = var3;
         this.rsDd50tfV = var4;
      }

      public String v5suI() {
         return this.ibzfxic;
      }

      public String ibzfxic() {
         return this.wuhB;
      }

      public Float wuhB() {
         return this.dOfFwvo;
      }

      public Float dOfFwvo() {
         return this.rsDd50tfV;
      }
   }

   public static final class DamageZones {
      private final DroneDefinition.DamageZones.KKeMvpuBgEY type;
      private final DroneDefinition.DamageZones.KKeMvpuBgEY entity;
      private final String damage;
      public static final Codec<DroneDefinition.DamageZones> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               DroneDefinition.DamageZones.KKeMvpuBgEY.v5suI.fieldOf("Core").forGetter(DroneDefinition.DamageZones::type),
               DroneDefinition.DamageZones.KKeMvpuBgEY.v5suI.fieldOf("Peripheral").forGetter(DroneDefinition.DamageZones::entity),
               Codec.STRING.optionalFieldOf("WarheadId", "").forGetter(DroneDefinition.DamageZones::damage)
            )
            .apply(var0, DroneDefinition.DamageZones::new)
      );

      public DamageZones(DroneDefinition.DamageZones.KKeMvpuBgEY var1, DroneDefinition.DamageZones.KKeMvpuBgEY var2, String var3) {
         this.type = var1;
         this.entity = var2;
         this.damage = var3;
      }

      public static final class KKeMvpuBgEY {
         private final int hp;
         private final float explodeChance;
         public static final Codec<DroneDefinition.DamageZones.KKeMvpuBgEY> v5suI = RecordCodecBuilder.create(
            var0 -> var0.group(
                  Codec.intRange(1, 1000000).fieldOf("Hp").forGetter(DroneDefinition.DamageZones.KKeMvpuBgEY::hp),
                  Codec.floatRange(0.0F, 1.0F).optionalFieldOf("ExplodeChance", 0.0F).forGetter(DroneDefinition.DamageZones.KKeMvpuBgEY::explodeChance)
               )
               .apply(var0, DroneDefinition.DamageZones.KKeMvpuBgEY::new)
         );

         public KKeMvpuBgEY(int var1, float var2) {
            this.hp = var1;
            this.explodeChance = var2;
         }

         public int hp() {
            return this.hp;
         }

         public float explodeChance() {
            return this.explodeChance;
         }

      }

      public DroneDefinition.DamageZones.KKeMvpuBgEY type() {
         return this.type;
      }

      public DroneDefinition.DamageZones.KKeMvpuBgEY entity() {
         return this.entity;
      }

      public String damage() {
         return this.damage;
      }

   }

   public static final class Engine {
      private final boolean wuhB;
      private final String dOfFwvo;
      private final float rsDd50tfV;
      private final float uSUb;
      private final float jGhI2;
      private final String wEJx3Itb;
      private final List<ParticleSpec> bAcdvq9;
      public static final DroneDefinition.Engine v5suI = new DroneDefinition.Engine(true, "engine", 2.5F, 0.4F, 1.0F, "rotor", List.of(v5suI()));
      public static final Codec<DroneDefinition.Engine> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.BOOL.optionalFieldOf("Enabled", v5suI.ibzfxic()).forGetter(DroneDefinition.Engine::ibzfxic),
               Codec.STRING.optionalFieldOf("ShakeBone", v5suI.wuhB()).forGetter(DroneDefinition.Engine::wuhB),
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("ShakeAngleDeg", v5suI.dOfFwvo()).forGetter(DroneDefinition.Engine::dOfFwvo),
               Codec.floatRange(0.0F, 16.0F).optionalFieldOf("ShakePosPixels", v5suI.rsDd50tfV()).forGetter(DroneDefinition.Engine::rsDd50tfV),
               Codec.floatRange(0.0F, 20.0F).optionalFieldOf("ShakeFrequency", v5suI.uSUb()).forGetter(DroneDefinition.Engine::uSUb),
               Codec.STRING.optionalFieldOf("ExhaustBone", v5suI.jGhI2()).forGetter(DroneDefinition.Engine::jGhI2)
            )
            .apply(var0, (var0x, var1, var2, var3, var4, var5) -> new DroneDefinition.Engine(var0x, var1, var2, var3, var4, var5, List.of()))
      );

      public Engine(boolean var1, String var2, float var3, float var4, float var5, String var6, List<ParticleSpec> var7) {
         var7 = var7 == null ? List.of() : List.copyOf(var7);
         this.wuhB = var1;
         this.dOfFwvo = var2;
         this.rsDd50tfV = var3;
         this.uSUb = var4;
         this.jGhI2 = var5;
         this.wEJx3Itb = var6;
         this.bAcdvq9 = var7;
      }

      public DroneDefinition.Engine v5suI(List<ParticleSpec> var1) {
         return new DroneDefinition.Engine(this.wuhB, this.dOfFwvo, this.rsDd50tfV, this.uSUb, this.jGhI2, this.wEJx3Itb, var1);
      }

      public static ParticleSpec v5suI() {
         return new ParticleSpec(
            "fp1_exhaust",
            ParticleSpec.KKeMvpuBgEY.rsDd50tfV,
            ParticleSpec.Rdl0X5jeod.dOfFwvo,
            ParticleSpec.Ol3TEDSog.ibzfxic,
            true,
            false,
            false,
            0.0F,
            false,
            true,
            1,
            0.0F,
            5,
            0,
            6,
            0.3F,
            0.0F,
            0,
            0,
            0.1F,
            2.0F,
            0,
            0,
            0.1F,
            null,
            List.of(
               "textures/particles/big_smoke_0",
               "textures/particles/big_smoke_1",
               "textures/particles/big_smoke_2",
               "textures/particles/big_smoke_3",
               "textures/particles/big_smoke_4",
               "textures/particles/big_smoke_5",
               "textures/particles/big_smoke_6",
               "textures/particles/big_smoke_7",
               "textures/particles/big_smoke_8",
               "textures/particles/big_smoke_9",
               "textures/particles/big_smoke_10",
               "textures/particles/big_smoke_11"
            ),
            List.of(),
            true,
            4143929,
            -1,
            List.of(),
            List.of(),
            false,
            null,
            null,
            new float[]{0.8F, 0.8F, 0.8F},
            new float[]{1.0F, 1.0F, 1.0F},
            new float[]{0.0F, 4.0F, 0.0F},
            new float[]{0.0F, 1.0E-4F, 0.0F},
            null,
            null,
            false,
            false
         );
      }

      public boolean ibzfxic() {
         return this.wuhB;
      }

      public String wuhB() {
         return this.dOfFwvo;
      }

      public float dOfFwvo() {
         return this.rsDd50tfV;
      }

      public float rsDd50tfV() {
         return this.uSUb;
      }

      public float uSUb() {
         return this.jGhI2;
      }

      public String jGhI2() {
         return this.wEJx3Itb;
      }

      public List<ParticleSpec> wEJx3Itb() {
         return this.bAcdvq9;
      }
   }

   public static final class GimbalLimits {
      private final float yawLeftDeg;
      private final float enabled;
      private final float shakeBone;
      private final float shakeAngleDeg;
      public static final DroneDefinition.GimbalLimits v5suI = new DroneDefinition.GimbalLimits(180.0F, 180.0F, 90.0F, 90.0F);
      public static final Codec<DroneDefinition.GimbalLimits> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.floatRange(0.0F, 180.0F).optionalFieldOf("YawLeftDeg", v5suI.yawLeftDeg()).forGetter(DroneDefinition.GimbalLimits::yawLeftDeg),
               Codec.floatRange(0.0F, 180.0F).optionalFieldOf("YawRightDeg", v5suI.enabled()).forGetter(DroneDefinition.GimbalLimits::enabled),
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("PitchUpDeg", v5suI.shakeBone()).forGetter(DroneDefinition.GimbalLimits::shakeBone),
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("PitchDownDeg", v5suI.shakeAngleDeg()).forGetter(DroneDefinition.GimbalLimits::shakeAngleDeg)
            )
            .apply(var0, DroneDefinition.GimbalLimits::new)
      );

      public GimbalLimits(float var1, float var2, float var3, float var4) {
         this.yawLeftDeg = var1;
         this.enabled = var2;
         this.shakeBone = var3;
         this.shakeAngleDeg = var4;
      }

      public float yawLeftDeg() {
         return this.yawLeftDeg;
      }

      public float enabled() {
         return this.enabled;
      }

      public float shakeBone() {
         return this.shakeBone;
      }

      public float shakeAngleDeg() {
         return this.shakeAngleDeg;
      }

   }

   public static final class GroundLaunch {
      private final double commitSpeed;
      private final double pushImpulse;
      private final double pushCap;
      private final double hitImpulse;
      private final double rollAccel;
      private final double runway;
      private final double takeoffSpeed;
      private final double rollFriction;
      private final double centerHalfWidth;
      private final double behindDot;
      public static final DroneDefinition.GroundLaunch v5suI = new DroneDefinition.GroundLaunch(0.25, 0.05, 0.45, 0.22, 0.04, 10.0, 0.55, 0.97, 0.6, 0.6);
      public static final Codec<DroneDefinition.GroundLaunch> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.doubleRange(0.01, 10.0).optionalFieldOf("CommitSpeed", v5suI.commitSpeed()).forGetter(DroneDefinition.GroundLaunch::commitSpeed),
               Codec.doubleRange(0.001, 10.0).optionalFieldOf("PushImpulse", v5suI.pushImpulse()).forGetter(DroneDefinition.GroundLaunch::pushImpulse),
               Codec.doubleRange(0.01, 10.0).optionalFieldOf("PushCap", v5suI.pushCap()).forGetter(DroneDefinition.GroundLaunch::pushCap),
               Codec.doubleRange(0.0, 10.0).optionalFieldOf("HitImpulse", v5suI.hitImpulse()).forGetter(DroneDefinition.GroundLaunch::hitImpulse),
               Codec.doubleRange(1.0E-4, 5.0).optionalFieldOf("RollAccel", v5suI.rollAccel()).forGetter(DroneDefinition.GroundLaunch::rollAccel),
               Codec.doubleRange(0.5, 256.0).optionalFieldOf("Runway", v5suI.runway()).forGetter(DroneDefinition.GroundLaunch::runway),
               Codec.doubleRange(0.05, 10.0).optionalFieldOf("TakeoffSpeed", v5suI.takeoffSpeed()).forGetter(DroneDefinition.GroundLaunch::takeoffSpeed),
               Codec.doubleRange(0.5, 0.999).optionalFieldOf("RollFriction", v5suI.rollFriction()).forGetter(DroneDefinition.GroundLaunch::rollFriction),
               Codec.doubleRange(0.05, 8.0)
                  .optionalFieldOf("CenterHalfWidth", v5suI.centerHalfWidth())
                  .forGetter(DroneDefinition.GroundLaunch::centerHalfWidth),
               Codec.doubleRange(-1.0, 1.0).optionalFieldOf("BehindDot", v5suI.behindDot()).forGetter(DroneDefinition.GroundLaunch::behindDot)
            )
            .apply(var0, DroneDefinition.GroundLaunch::new)
      );

      public GroundLaunch(double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15, double var17, double var19) {
         this.commitSpeed = var1;
         this.pushImpulse = var3;
         this.pushCap = var5;
         this.hitImpulse = var7;
         this.rollAccel = var9;
         this.runway = var11;
         this.takeoffSpeed = var13;
         this.rollFriction = var15;
         this.centerHalfWidth = var17;
         this.behindDot = var19;
      }

      public double commitSpeed() {
         return this.commitSpeed;
      }

      public double pushImpulse() {
         return this.pushImpulse;
      }

      public double pushCap() {
         return this.pushCap;
      }

      public double hitImpulse() {
         return this.hitImpulse;
      }

      public double rollAccel() {
         return this.rollAccel;
      }

      public double runway() {
         return this.runway;
      }

      public double takeoffSpeed() {
         return this.takeoffSpeed;
      }

      public double rollFriction() {
         return this.rollFriction;
      }

      public double centerHalfWidth() {
         return this.centerHalfWidth;
      }

      public double behindDot() {
         return this.behindDot;
      }

   }

   public static final class Hitbox {
      private final float width;
      private final float height;
      public static final DroneDefinition.Hitbox v5suI = new DroneDefinition.Hitbox(0.85F, 0.55F);
      public static final Codec<DroneDefinition.Hitbox> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.floatRange(0.05F, 16.0F).optionalFieldOf("Width", v5suI.width()).forGetter(DroneDefinition.Hitbox::width),
               Codec.floatRange(0.05F, 16.0F).optionalFieldOf("Height", v5suI.height()).forGetter(DroneDefinition.Hitbox::height)
            )
            .apply(var0, DroneDefinition.Hitbox::new)
      );

      public Hitbox(float var1, float var2) {
         this.width = var1;
         this.height = var2;
      }

      public float width() {
         return this.width;
      }

      public float height() {
         return this.height;
      }

   }

   private static final class Ifr7Wzf3 {
      private final String v5suI;
      private final TooltipSpec ibzfxic;

      private Ifr7Wzf3(String var1, TooltipSpec var2) {
         this.v5suI = var1;
         this.ibzfxic = var2;
      }

      public String v5suI() {
         return this.v5suI;
      }

      public TooltipSpec ibzfxic() {
         return this.ibzfxic;
      }

   }

   public static final class LoiterFlight {
      private final float wuhB;
      private final int dOfFwvo;
      private final float rsDd50tfV;
      private final float uSUb;
      private final float jGhI2;
      private final float wEJx3Itb;
      private final float bAcdvq9;
      private final float c1jHYPb4;
      private final float v8ZN;
      private final float zTT3nw6;
      private final float vmpmU;
      private final float b69s;
      private final float cXHvq;
      private final float rxYnaa;
      private final float r6s7FL3;
      private final float iAKNPAj;
      private final float qlv6NKWnH;
      private final float hOKMYHOaC;
      private final float iS1JnZov;
      private final float lfyfLwuw;
      private final int wyNDWDB;
      private final float iRWc2Pi;
      private final float[] tpzIHm;
      private final int eJDY;
      private final String zHBAQgZP;
      private final float we1GYY;
      private final int pxmzA;
      private final float iElAbEZe;
      private final int sTrE;
      private final float ifpFOCaAs;
      private final float uQdlyiwB;
      private final float ivDR;
      private final float n9lX8A;
      private final float wBXn;
      private final float mM7DsB9AR;
      private final String uKpZYj;
      private final float l2IWOAGO;
      private final int xYttSy;
      private final DroneDefinition.GroundLaunch kNoG;
      private final DroneDefinition.Wreck fvsAOg;
      private final DroneDefinition.Manual lFt1ckP;
      private final EnergySource qU1QioDiC;
      public static final DroneDefinition.LoiterFlight v5suI = new DroneDefinition.LoiterFlight(
         15.0F,
         60,
         1.8F,
         2.6F,
         1.25F,
         2.0F,
         0.015F,
         35.0F,
         12.0F,
         50.0F,
         0.0245F,
         3.0F,
         1.2F,
         3.0F,
         35.0F,
         1.5F,
         1.5F,
         45.0F,
         2.5F,
         0.15F,
         6000,
         7.0F,
         new float[]{8.0F, 16.0F, 26.0F},
         5,
         "tm62",
         6.0F,
         80,
         3.0F,
         1,
         1.0F,
         2.0F,
         0.0F,
         0.012F,
         0.7F,
         0.0F,
         "",
         0.5F,
         25,
         DroneDefinition.GroundLaunch.v5suI,
         DroneDefinition.Wreck.v5suI,
         DroneDefinition.Manual.v5suI,
         EnergySource.v5suI
      );
      private static final Codec<float[]> zQOhlhTZx = Codec.FLOAT.listOf().xmap(var0 -> {
         float[] var1 = new float[var0.size()];

         for (int var2 = 0; var2 < var1.length; var2++) {
            var1[var2] = (Float)var0.get(var2);
         }

         return var1;
      }, var0 -> {
         ArrayList var1 = new ArrayList(var0.length);

         for (float var5 : var0) {
            var1.add(var5);
         }

         return var1;
      });
      private static final MapCodec<DroneDefinition.LoiterFlight.KKeMvpuBgEY> lvk8rWUGD = RecordCodecBuilder.mapCodec(
         var0 -> var0.group(
               Codec.floatRange(-90.0F, 90.0F).optionalFieldOf("LaunchPitch", v5suI.c1jHYPb4()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::v5suI),
               Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("BoostTicks", v5suI.v8ZN()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::ibzfxic),
               Codec.floatRange(0.0F, 100.0F).optionalFieldOf("LaunchSpeed", v5suI.zTT3nw6()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::wuhB),
               Codec.floatRange(0.0F, 100.0F).optionalFieldOf("BoostSpeed", v5suI.vmpmU()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::dOfFwvo),
               Codec.floatRange(0.0F, 100.0F).optionalFieldOf("CruiseSpeed", v5suI.b69s()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::rsDd50tfV),
               Codec.floatRange(0.0F, 100.0F).optionalFieldOf("TerminalSpeed", v5suI.cXHvq()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::uSUb),
               Codec.floatRange(0.0F, 100.0F).optionalFieldOf("PropAccel", v5suI.rxYnaa()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::jGhI2),
               Codec.floatRange(0.0F, 4096.0F).optionalFieldOf("ClimbAltAgl", v5suI.r6s7FL3()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::wEJx3Itb),
               Codec.floatRange(-90.0F, 90.0F).optionalFieldOf("ClimbPitch", v5suI.iAKNPAj()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::bAcdvq9),
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("MaxBank", v5suI.qlv6NKWnH()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::c1jHYPb4),
               Codec.floatRange(0.0F, 10.0F).optionalFieldOf("GPerTick2", v5suI.hOKMYHOaC()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::v8ZN),
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("MaxTurnRate", v5suI.iS1JnZov()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::zTT3nw6),
               Codec.floatRange(0.0F, 100.0F).optionalFieldOf("BankGain", v5suI.lfyfLwuw()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::vmpmU),
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("RollRate", v5suI.wyNDWDB()).forGetter(DroneDefinition.LoiterFlight.KKeMvpuBgEY::b69s)
            )
            .apply(var0, DroneDefinition.LoiterFlight.KKeMvpuBgEY::new)
      );
      private static final MapCodec<DroneDefinition.LoiterFlight.Ol3TEDSog> pyHY5 = RecordCodecBuilder.mapCodec(
         var0 -> var0.group(
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("MaxPitch", v5suI.iRWc2Pi()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::v5suI),
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("PitchRate", v5suI.tpzIHm()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::ibzfxic),
               Codec.floatRange(0.0F, 100.0F).optionalFieldOf("AltPitchGain", v5suI.eJDY()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::wuhB),
               Codec.floatRange(0.0F, 4096.0F).optionalFieldOf("TerminalRange", v5suI.zHBAQgZP()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::dOfFwvo),
               Codec.floatRange(0.0F, 256.0F).optionalFieldOf("Proximity", v5suI.we1GYY()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::rsDd50tfV),
               Codec.floatRange(0.0F, 100.0F).optionalFieldOf("ImpactSpeed", v5suI.pxmzA()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::uSUb),
               Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("Capacity", v5suI.iElAbEZe()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::jGhI2),
               Codec.floatRange(0.0F, 256.0F).optionalFieldOf("ObstacleClearance", v5suI.sTrE()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::wEJx3Itb),
               zQOhlhTZx.optionalFieldOf("Lookahead", v5suI.ifpFOCaAs()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::bAcdvq9),
               Codec.intRange(1, Integer.MAX_VALUE)
                  .optionalFieldOf("ObstacleInterval", v5suI.uQdlyiwB())
                  .forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::c1jHYPb4),
               Codec.STRING.optionalFieldOf("WarheadId", v5suI.ivDR()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::v8ZN),
               Codec.floatRange(0.0F, 1000.0F).optionalFieldOf("WarheadPower", v5suI.n9lX8A()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::zTT3nw6),
               Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("WarheadFragments", v5suI.wBXn()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::vmpmU),
               Codec.floatRange(0.0F, 64.0F).optionalFieldOf("FreezeLeadDistance", v5suI.mM7DsB9AR()).forGetter(DroneDefinition.LoiterFlight.Ol3TEDSog::b69s)
            )
            .apply(var0, DroneDefinition.LoiterFlight.Ol3TEDSog::new)
      );
      private static final MapCodec<DroneDefinition.LoiterFlight.Rdl0X5jeod> idXga = RecordCodecBuilder.mapCodec(
         var0 -> var0.group(
               Codec.intRange(0, 8).optionalFieldOf("ForceRadius", v5suI.uKpZYj()).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::v5suI),
               Codec.floatRange(0.05F, 20.0F).optionalFieldOf("SpeedMultiplier", v5suI.l2IWOAGO()).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::ibzfxic),
               Codec.floatRange(0.0F, 64.0F).optionalFieldOf("PenetrationDepth", v5suI.xYttSy()).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::wuhB),
               Codec.floatRange(0.0F, 1.0F).optionalFieldOf("CrashChance", v5suI.kNoG()).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::dOfFwvo),
               Codec.floatRange(1.0E-4F, 5.0F).optionalFieldOf("CrashSinkAccel", v5suI.fvsAOg()).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::rsDd50tfV),
               Codec.floatRange(0.01F, 10.0F).optionalFieldOf("CrashMaxSink", v5suI.lFt1ckP()).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::uSUb),
               Codec.floatRange(-8.0F, 8.0F).optionalFieldOf("CrashRestYOffset", v5suI.qU1QioDiC()).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::jGhI2),
               Codec.STRING.optionalFieldOf("VitalBone", v5suI.zQOhlhTZx()).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::wEJx3Itb),
               Codec.floatRange(0.0F, 1.0F)
                  .optionalFieldOf("VitalExplodeChance", v5suI.lvk8rWUGD())
                  .forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::bAcdvq9),
               Codec.intRange(1, Integer.MAX_VALUE)
                  .optionalFieldOf("PeripheralHitsToCrash", v5suI.pyHY5())
                  .forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::c1jHYPb4),
               DroneDefinition.GroundLaunch.ibzfxic
                  .optionalFieldOf("GroundLaunch", DroneDefinition.GroundLaunch.v5suI)
                  .forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::v8ZN),
               DroneDefinition.Wreck.ibzfxic.optionalFieldOf("Wreck", DroneDefinition.Wreck.v5suI).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::zTT3nw6),
               DroneDefinition.Manual.ibzfxic.optionalFieldOf("Manual", DroneDefinition.Manual.v5suI).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::vmpmU),
               EnergySource.wuhB.optionalFieldOf("EnergySource", v5suI.iKXOcc()).forGetter(DroneDefinition.LoiterFlight.Rdl0X5jeod::b69s)
            )
            .apply(var0, DroneDefinition.LoiterFlight.Rdl0X5jeod::new)
      );
      public static final Codec<DroneDefinition.LoiterFlight> ibzfxic = Codec.mapPair(lvk8rWUGD, Codec.mapPair(pyHY5, idXga))
         .codec()
         .xmap(
            var0 -> {
               DroneDefinition.LoiterFlight.KKeMvpuBgEY var1 = (DroneDefinition.LoiterFlight.KKeMvpuBgEY)var0.getFirst();
               DroneDefinition.LoiterFlight.Ol3TEDSog var2 = (DroneDefinition.LoiterFlight.Ol3TEDSog)((Pair)var0.getSecond()).getFirst();
               DroneDefinition.LoiterFlight.Rdl0X5jeod var3 = (DroneDefinition.LoiterFlight.Rdl0X5jeod)((Pair)var0.getSecond()).getSecond();
               return new DroneDefinition.LoiterFlight(
                  var1.v5suI(),
                  var1.ibzfxic(),
                  var1.wuhB(),
                  var1.dOfFwvo(),
                  var1.rsDd50tfV(),
                  var1.uSUb(),
                  var1.jGhI2(),
                  var1.wEJx3Itb(),
                  var1.bAcdvq9(),
                  var1.c1jHYPb4(),
                  var1.v8ZN(),
                  var1.zTT3nw6(),
                  var1.vmpmU(),
                  var1.b69s(),
                  var2.v5suI(),
                  var2.ibzfxic(),
                  var2.wuhB(),
                  var2.dOfFwvo(),
                  var2.rsDd50tfV(),
                  var2.uSUb(),
                  var2.jGhI2(),
                  var2.wEJx3Itb(),
                  var2.bAcdvq9(),
                  var2.c1jHYPb4(),
                  var2.v8ZN(),
                  var2.zTT3nw6(),
                  var2.vmpmU(),
                  var2.b69s(),
                  var3.v5suI(),
                  var3.ibzfxic(),
                  var3.wuhB(),
                  var3.dOfFwvo(),
                  var3.rsDd50tfV(),
                  var3.uSUb(),
                  var3.jGhI2(),
                  var3.wEJx3Itb(),
                  var3.bAcdvq9(),
                  var3.c1jHYPb4(),
                  var3.v8ZN(),
                  var3.zTT3nw6(),
                  var3.vmpmU(),
                  var3.b69s()
               );
            },
            var0 -> Pair.of(
               new DroneDefinition.LoiterFlight.KKeMvpuBgEY(
                  var0.c1jHYPb4(),
                  var0.v8ZN(),
                  var0.zTT3nw6(),
                  var0.vmpmU(),
                  var0.b69s(),
                  var0.cXHvq(),
                  var0.rxYnaa(),
                  var0.r6s7FL3(),
                  var0.iAKNPAj(),
                  var0.qlv6NKWnH(),
                  var0.hOKMYHOaC(),
                  var0.iS1JnZov(),
                  var0.lfyfLwuw(),
                  var0.wyNDWDB()
               ),
               Pair.of(
                  new DroneDefinition.LoiterFlight.Ol3TEDSog(
                     var0.iRWc2Pi(),
                     var0.tpzIHm(),
                     var0.eJDY(),
                     var0.zHBAQgZP(),
                     var0.we1GYY(),
                     var0.pxmzA(),
                     var0.iElAbEZe(),
                     var0.sTrE(),
                     var0.ifpFOCaAs(),
                     var0.uQdlyiwB(),
                     var0.ivDR(),
                     var0.n9lX8A(),
                     var0.wBXn(),
                     var0.mM7DsB9AR()
                  ),
                  new DroneDefinition.LoiterFlight.Rdl0X5jeod(
                     var0.uKpZYj(),
                     var0.l2IWOAGO(),
                     var0.xYttSy(),
                     var0.kNoG(),
                     var0.fvsAOg(),
                     var0.lFt1ckP(),
                     var0.qU1QioDiC(),
                     var0.zQOhlhTZx(),
                     var0.lvk8rWUGD(),
                     var0.pyHY5(),
                     var0.idXga(),
                     var0.i263D9(),
                     var0.rOIp0p7(),
                     var0.iKXOcc()
                  )
               )
            )
         );

      public LoiterFlight(
         float var1,
         int var2,
         float var3,
         float var4,
         float var5,
         float var6,
         float var7,
         float var8,
         float var9,
         float var10,
         float var11,
         float var12,
         float var13,
         float var14,
         float var15,
         float var16,
         float var17,
         float var18,
         float var19,
         float var20,
         int var21,
         float var22,
         float[] var23,
         int var24,
         String var25,
         float var26,
         int var27,
         float var28,
         int var29,
         float var30,
         float var31,
         float var32,
         float var33,
         float var34,
         float var35,
         String var36,
         float var37,
         int var38,
         DroneDefinition.GroundLaunch var39,
         DroneDefinition.Wreck var40,
         DroneDefinition.Manual var41,
         EnergySource var42
      ) {
         this.wuhB = var1;
         this.dOfFwvo = var2;
         this.rsDd50tfV = var3;
         this.uSUb = var4;
         this.jGhI2 = var5;
         this.wEJx3Itb = var6;
         this.bAcdvq9 = var7;
         this.c1jHYPb4 = var8;
         this.v8ZN = var9;
         this.zTT3nw6 = var10;
         this.vmpmU = var11;
         this.b69s = var12;
         this.cXHvq = var13;
         this.rxYnaa = var14;
         this.r6s7FL3 = var15;
         this.iAKNPAj = var16;
         this.qlv6NKWnH = var17;
         this.hOKMYHOaC = var18;
         this.iS1JnZov = var19;
         this.lfyfLwuw = var20;
         this.wyNDWDB = var21;
         this.iRWc2Pi = var22;
         this.tpzIHm = var23;
         this.eJDY = var24;
         this.zHBAQgZP = var25;
         this.we1GYY = var26;
         this.pxmzA = var27;
         this.iElAbEZe = var28;
         this.sTrE = var29;
         this.ifpFOCaAs = var30;
         this.uQdlyiwB = var31;
         this.ivDR = var32;
         this.n9lX8A = var33;
         this.wBXn = var34;
         this.mM7DsB9AR = var35;
         this.uKpZYj = var36;
         this.l2IWOAGO = var37;
         this.xYttSy = var38;
         this.kNoG = var39;
         this.fvsAOg = var40;
         this.lFt1ckP = var41;
         this.qU1QioDiC = var42;
      }

      public boolean v5suI() {
         return this.qU1QioDiC != EnergySource.ibzfxic;
      }

      public boolean ibzfxic() {
         return this.uKpZYj != null && !this.uKpZYj.isEmpty();
      }

      public DroneDefinition.GroundLaunch wuhB() {
         return this.kNoG != null ? this.kNoG : DroneDefinition.GroundLaunch.v5suI;
      }

      public DroneDefinition.Wreck dOfFwvo() {
         return this.fvsAOg != null ? this.fvsAOg : DroneDefinition.Wreck.v5suI;
      }

      public DroneDefinition.Manual rsDd50tfV() {
         return this.lFt1ckP != null ? this.lFt1ckP : DroneDefinition.Manual.v5suI;
      }

      public float uSUb() {
         return this.rsDd50tfV * this.ifpFOCaAs;
      }

      public float jGhI2() {
         return this.uSUb * this.ifpFOCaAs;
      }

      public float wEJx3Itb() {
         return this.jGhI2 * this.ifpFOCaAs;
      }

      public float bAcdvq9() {
         return this.wEJx3Itb * this.ifpFOCaAs;
      }

      public DroneDefinition.LoiterFlight v5suI(String var1, float var2, int var3) {
         return new DroneDefinition.LoiterFlight(
            this.wuhB,
            this.dOfFwvo,
            this.rsDd50tfV,
            this.uSUb,
            this.jGhI2,
            this.wEJx3Itb,
            this.bAcdvq9,
            this.c1jHYPb4,
            this.v8ZN,
            this.zTT3nw6,
            this.vmpmU,
            this.b69s,
            this.cXHvq,
            this.rxYnaa,
            this.r6s7FL3,
            this.iAKNPAj,
            this.qlv6NKWnH,
            this.hOKMYHOaC,
            this.iS1JnZov,
            this.lfyfLwuw,
            this.wyNDWDB,
            this.iRWc2Pi,
            this.tpzIHm,
            this.eJDY,
            var1,
            var2,
            var3,
            this.iElAbEZe,
            this.sTrE,
            this.ifpFOCaAs,
            this.uQdlyiwB,
            this.ivDR,
            this.n9lX8A,
            this.wBXn,
            this.mM7DsB9AR,
            this.uKpZYj,
            this.l2IWOAGO,
            this.xYttSy,
            this.kNoG,
            this.fvsAOg,
            this.lFt1ckP,
            this.qU1QioDiC
         );
      }

      public float c1jHYPb4() {
         return this.wuhB;
      }

      public int v8ZN() {
         return this.dOfFwvo;
      }

      public float zTT3nw6() {
         return this.rsDd50tfV;
      }

      public float vmpmU() {
         return this.uSUb;
      }

      public float b69s() {
         return this.jGhI2;
      }

      public float cXHvq() {
         return this.wEJx3Itb;
      }

      public float rxYnaa() {
         return this.bAcdvq9;
      }

      public float r6s7FL3() {
         return this.c1jHYPb4;
      }

      public float iAKNPAj() {
         return this.v8ZN;
      }

      public float qlv6NKWnH() {
         return this.zTT3nw6;
      }

      public float hOKMYHOaC() {
         return this.vmpmU;
      }

      public float iS1JnZov() {
         return this.b69s;
      }

      public float lfyfLwuw() {
         return this.cXHvq;
      }

      public float wyNDWDB() {
         return this.rxYnaa;
      }

      public float iRWc2Pi() {
         return this.r6s7FL3;
      }

      public float tpzIHm() {
         return this.iAKNPAj;
      }

      public float eJDY() {
         return this.qlv6NKWnH;
      }

      public float zHBAQgZP() {
         return this.hOKMYHOaC;
      }

      public float we1GYY() {
         return this.iS1JnZov;
      }

      public float pxmzA() {
         return this.lfyfLwuw;
      }

      public int iElAbEZe() {
         return this.wyNDWDB;
      }

      public float sTrE() {
         return this.iRWc2Pi;
      }

      public float[] ifpFOCaAs() {
         return this.tpzIHm;
      }

      public int uQdlyiwB() {
         return this.eJDY;
      }

      public String ivDR() {
         return this.zHBAQgZP;
      }

      public float n9lX8A() {
         return this.we1GYY;
      }

      public int wBXn() {
         return this.pxmzA;
      }

      public float mM7DsB9AR() {
         return this.iElAbEZe;
      }

      public int uKpZYj() {
         return this.sTrE;
      }

      public float l2IWOAGO() {
         return this.ifpFOCaAs;
      }

      public float xYttSy() {
         return this.uQdlyiwB;
      }

      public float kNoG() {
         return this.ivDR;
      }

      public float fvsAOg() {
         return this.n9lX8A;
      }

      public float lFt1ckP() {
         return this.wBXn;
      }

      public float qU1QioDiC() {
         return this.mM7DsB9AR;
      }

      public String zQOhlhTZx() {
         return this.uKpZYj;
      }

      public float lvk8rWUGD() {
         return this.l2IWOAGO;
      }

      public int pyHY5() {
         return this.xYttSy;
      }

      public DroneDefinition.GroundLaunch idXga() {
         return this.kNoG;
      }

      public DroneDefinition.Wreck i263D9() {
         return this.fvsAOg;
      }

      public DroneDefinition.Manual rOIp0p7() {
         return this.lFt1ckP;
      }

      public EnergySource iKXOcc() {
         return this.qU1QioDiC;
      }

      private static final class KKeMvpuBgEY {
         private final float v5suI;
         private final int ibzfxic;
         private final float wuhB;
         private final float dOfFwvo;
         private final float rsDd50tfV;
         private final float uSUb;
         private final float jGhI2;
         private final float wEJx3Itb;
         private final float bAcdvq9;
         private final float c1jHYPb4;
         private final float v8ZN;
         private final float zTT3nw6;
         private final float vmpmU;
         private final float b69s;

         private KKeMvpuBgEY(
            float var1,
            int var2,
            float var3,
            float var4,
            float var5,
            float var6,
            float var7,
            float var8,
            float var9,
            float var10,
            float var11,
            float var12,
            float var13,
            float var14
         ) {
            this.v5suI = var1;
            this.ibzfxic = var2;
            this.wuhB = var3;
            this.dOfFwvo = var4;
            this.rsDd50tfV = var5;
            this.uSUb = var6;
            this.jGhI2 = var7;
            this.wEJx3Itb = var8;
            this.bAcdvq9 = var9;
            this.c1jHYPb4 = var10;
            this.v8ZN = var11;
            this.zTT3nw6 = var12;
            this.vmpmU = var13;
            this.b69s = var14;
         }

         public float v5suI() {
            return this.v5suI;
         }

         public int ibzfxic() {
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

         public float c1jHYPb4() {
            return this.c1jHYPb4;
         }

         public float v8ZN() {
            return this.v8ZN;
         }

         public float zTT3nw6() {
            return this.zTT3nw6;
         }

         public float vmpmU() {
            return this.vmpmU;
         }

         public float b69s() {
            return this.b69s;
         }
      }

      private static final class Ol3TEDSog {
         private final float v5suI;
         private final float ibzfxic;
         private final float wuhB;
         private final float dOfFwvo;
         private final float rsDd50tfV;
         private final float uSUb;
         private final int jGhI2;
         private final float wEJx3Itb;
         private final float[] bAcdvq9;
         private final int c1jHYPb4;
         private final String v8ZN;
         private final float zTT3nw6;
         private final int vmpmU;
         private final float b69s;

         private Ol3TEDSog(
            float var1,
            float var2,
            float var3,
            float var4,
            float var5,
            float var6,
            int var7,
            float var8,
            float[] var9,
            int var10,
            String var11,
            float var12,
            int var13,
            float var14
         ) {
            this.v5suI = var1;
            this.ibzfxic = var2;
            this.wuhB = var3;
            this.dOfFwvo = var4;
            this.rsDd50tfV = var5;
            this.uSUb = var6;
            this.jGhI2 = var7;
            this.wEJx3Itb = var8;
            this.bAcdvq9 = var9;
            this.c1jHYPb4 = var10;
            this.v8ZN = var11;
            this.zTT3nw6 = var12;
            this.vmpmU = var13;
            this.b69s = var14;
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

         public int jGhI2() {
            return this.jGhI2;
         }

         public float wEJx3Itb() {
            return this.wEJx3Itb;
         }

         public float[] bAcdvq9() {
            return this.bAcdvq9;
         }

         public int c1jHYPb4() {
            return this.c1jHYPb4;
         }

         public String v8ZN() {
            return this.v8ZN;
         }

         public float zTT3nw6() {
            return this.zTT3nw6;
         }

         public int vmpmU() {
            return this.vmpmU;
         }

         public float b69s() {
            return this.b69s;
         }

      }

      private static final class Rdl0X5jeod {
         private final int v5suI;
         private final float ibzfxic;
         private final float wuhB;
         private final float dOfFwvo;
         private final float rsDd50tfV;
         private final float uSUb;
         private final float jGhI2;
         private final String wEJx3Itb;
         private final float bAcdvq9;
         private final int c1jHYPb4;
         private final DroneDefinition.GroundLaunch v8ZN;
         private final DroneDefinition.Wreck zTT3nw6;
         private final DroneDefinition.Manual vmpmU;
         private final EnergySource b69s;

         private Rdl0X5jeod(
            int var1,
            float var2,
            float var3,
            float var4,
            float var5,
            float var6,
            float var7,
            String var8,
            float var9,
            int var10,
            DroneDefinition.GroundLaunch var11,
            DroneDefinition.Wreck var12,
            DroneDefinition.Manual var13,
            EnergySource var14
         ) {
            this.v5suI = var1;
            this.ibzfxic = var2;
            this.wuhB = var3;
            this.dOfFwvo = var4;
            this.rsDd50tfV = var5;
            this.uSUb = var6;
            this.jGhI2 = var7;
            this.wEJx3Itb = var8;
            this.bAcdvq9 = var9;
            this.c1jHYPb4 = var10;
            this.v8ZN = var11;
            this.zTT3nw6 = var12;
            this.vmpmU = var13;
            this.b69s = var14;
         }

         public int v5suI() {
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

         public String wEJx3Itb() {
            return this.wEJx3Itb;
         }

         public float bAcdvq9() {
            return this.bAcdvq9;
         }

         public int c1jHYPb4() {
            return this.c1jHYPb4;
         }

         public DroneDefinition.GroundLaunch v8ZN() {
            return this.v8ZN;
         }

         public DroneDefinition.Wreck zTT3nw6() {
            return this.zTT3nw6;
         }

         public DroneDefinition.Manual vmpmU() {
            return this.vmpmU;
         }

         public EnergySource b69s() {
            return this.b69s;
         }

      }
   }

   public static final class Manual {
      private final float expoRoll;
      private final float expoPitch;
      private final float expoYaw;
      private final float inputSmoothing;
      private final float maxRollRate;
      private final float rollAgility;
      private final float levelReturnRate;
      private final float pitchAgility;
      private final float maxPitchRate;
      private final float speedAuthorityMin;
      private final DroneDefinition.ControlScheme controlScheme;
      private final float guidedAttackAngleDeg;
      public static final DroneDefinition.Manual v5suI = new DroneDefinition.Manual(
         0.15F, 0.1F, 0.2F, 0.5F, 5.0F, 0.35F, 1.5F, 0.35F, 4.0F, 0.8F, DroneDefinition.ControlScheme.v5suI, 45.0F
      );
      public static final Codec<DroneDefinition.Manual> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.floatRange(0.0F, 1.0F).optionalFieldOf("ExpoRoll", v5suI.expoRoll()).forGetter(DroneDefinition.Manual::expoRoll),
               Codec.floatRange(0.0F, 1.0F).optionalFieldOf("ExpoPitch", v5suI.expoPitch()).forGetter(DroneDefinition.Manual::expoPitch),
               Codec.floatRange(0.0F, 1.0F).optionalFieldOf("ExpoYaw", v5suI.expoYaw()).forGetter(DroneDefinition.Manual::expoYaw),
               Codec.floatRange(0.01F, 1.0F).optionalFieldOf("InputSmoothing", v5suI.inputSmoothing()).forGetter(DroneDefinition.Manual::inputSmoothing),
               Codec.floatRange(0.1F, 90.0F).optionalFieldOf("MaxRollRate", v5suI.maxRollRate()).forGetter(DroneDefinition.Manual::maxRollRate),
               Codec.floatRange(0.01F, 1.0F).optionalFieldOf("RollAgility", v5suI.rollAgility()).forGetter(DroneDefinition.Manual::rollAgility),
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("LevelReturnRate", v5suI.levelReturnRate()).forGetter(DroneDefinition.Manual::levelReturnRate),
               Codec.floatRange(0.01F, 1.0F).optionalFieldOf("PitchAgility", v5suI.pitchAgility()).forGetter(DroneDefinition.Manual::pitchAgility),
               Codec.floatRange(0.1F, 90.0F).optionalFieldOf("MaxPitchRate", v5suI.maxPitchRate()).forGetter(DroneDefinition.Manual::maxPitchRate),
               Codec.floatRange(0.0F, 1.0F)
                  .optionalFieldOf("SpeedAuthorityMin", v5suI.speedAuthorityMin())
                  .forGetter(DroneDefinition.Manual::speedAuthorityMin),
               DroneDefinition.ControlScheme.dOfFwvo
                  .optionalFieldOf("ControlScheme", DroneDefinition.ControlScheme.v5suI)
                  .forGetter(DroneDefinition.Manual::controlScheme),
               Codec.floatRange(1.0F, 89.0F)
                  .optionalFieldOf("GuidedAttackAngleDeg", v5suI.guidedAttackAngleDeg())
                  .forGetter(DroneDefinition.Manual::guidedAttackAngleDeg)
            )
            .apply(var0, DroneDefinition.Manual::new)
      );

      public Manual(
         float var1,
         float var2,
         float var3,
         float var4,
         float var5,
         float var6,
         float var7,
         float var8,
         float var9,
         float var10,
         DroneDefinition.ControlScheme var11,
         float var12
      ) {
         this.expoRoll = var1;
         this.expoPitch = var2;
         this.expoYaw = var3;
         this.inputSmoothing = var4;
         this.maxRollRate = var5;
         this.rollAgility = var6;
         this.levelReturnRate = var7;
         this.pitchAgility = var8;
         this.maxPitchRate = var9;
         this.speedAuthorityMin = var10;
         this.controlScheme = var11;
         this.guidedAttackAngleDeg = var12;
      }

      public DroneDefinition.ControlScheme controlScheme() {
         return this.controlScheme != null ? this.controlScheme : DroneDefinition.ControlScheme.v5suI;
      }

      public DroneDefinition.ControlScheme zTT3nw6() {
         return this.controlScheme;
      }

      public float expoRoll() {
         return this.expoRoll;
      }

      public float expoPitch() {
         return this.expoPitch;
      }

      public float expoYaw() {
         return this.expoYaw;
      }

      public float inputSmoothing() {
         return this.inputSmoothing;
      }

      public float maxRollRate() {
         return this.maxRollRate;
      }

      public float rollAgility() {
         return this.rollAgility;
      }

      public float levelReturnRate() {
         return this.levelReturnRate;
      }

      public float pitchAgility() {
         return this.pitchAgility;
      }

      public float maxPitchRate() {
         return this.maxPitchRate;
      }

      public float speedAuthorityMin() {
         return this.speedAuthorityMin;
      }

      public float guidedAttackAngleDeg() {
         return this.guidedAttackAngleDeg;
      }

   }

   public static final class Mount {
      private final String model;
      private final String texture;
      private final AttachTransform attach;
      public static final Codec<DroneDefinition.Mount> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.STRING.fieldOf("Model").forGetter(DroneDefinition.Mount::model),
               Codec.STRING.fieldOf("Texture").forGetter(DroneDefinition.Mount::texture),
               AttachTransform.v5suI.optionalFieldOf("Attach", AttachTransform.v5suI("payload_mount")).forGetter(DroneDefinition.Mount::attach)
            )
            .apply(var0, DroneDefinition.Mount::new)
      );

      public Mount(String var1, String var2, AttachTransform var3) {
         this.model = var1;
         this.texture = var2;
         this.attach = var3;
      }

      public String model() {
         return this.model;
      }

      public String texture() {
         return this.texture;
      }

      public AttachTransform attach() {
         return this.attach;
      }

   }

   public enum OsdProfile {
      v5suI("max7456"),
      ibzfxic("zala");

      private final String dOfFwvo;
      public static final Codec<DroneDefinition.OsdProfile> wuhB = Codec.STRING.xmap(DroneDefinition.OsdProfile::v5suI, DroneDefinition.OsdProfile::v5suI);

      OsdProfile(String var3) {
         this.dOfFwvo = var3;
      }

      public String v5suI() {
         return this.dOfFwvo;
      }

      public static DroneDefinition.OsdProfile v5suI(String var0) {
         if (var0 != null) {
            if ("betaflight".equalsIgnoreCase(var0.trim())) {
               return v5suI;
            }

            if ("lancet".equalsIgnoreCase(var0.trim())) {
               return ibzfxic;
            }

            for (DroneDefinition.OsdProfile var4 : values()) {
               if (var4.dOfFwvo.equalsIgnoreCase(var0.trim())) {
                  return var4;
               }
            }
         }

         return v5suI;
      }
   }

   public static final class Physics {
      private final float motorKv;
      private final float propDiameter;
      private final float propPitch;
      private final float dragCoefficient;
      private final float thrustMultiplier;
      private final float mass;
      private final float frontalArea;
      private final float bodyDragCoefficient;
      public static final DroneDefinition.Physics v5suI = new DroneDefinition.Physics(1300.0F, 9.0F, 4.5F, 1.1F, 1.0F, 1.05F, 0.08F, 1.1F);
      public static final Codec<DroneDefinition.Physics> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.floatRange(800.0F, 3000.0F).optionalFieldOf("MotorKv", v5suI.motorKv()).forGetter(DroneDefinition.Physics::motorKv),
               Codec.floatRange(3.0F, 12.0F).optionalFieldOf("PropDiameter", v5suI.propDiameter()).forGetter(DroneDefinition.Physics::propDiameter),
               Codec.floatRange(2.0F, 8.0F).optionalFieldOf("PropPitch", v5suI.propPitch()).forGetter(DroneDefinition.Physics::propPitch),
               Codec.floatRange(0.5F, 2.0F).optionalFieldOf("DragCoefficient", v5suI.dragCoefficient()).forGetter(DroneDefinition.Physics::dragCoefficient),
               Codec.floatRange(0.5F, 2.0F).optionalFieldOf("ThrustMultiplier", v5suI.thrustMultiplier()).forGetter(DroneDefinition.Physics::thrustMultiplier),
               Codec.floatRange(0.05F, 50.0F).optionalFieldOf("Mass", v5suI.mass()).forGetter(DroneDefinition.Physics::mass),
               Codec.floatRange(0.001F, 5.0F).optionalFieldOf("FrontalArea", v5suI.frontalArea()).forGetter(DroneDefinition.Physics::frontalArea),
               Codec.floatRange(0.1F, 4.0F)
                  .optionalFieldOf("BodyDragCoefficient", v5suI.bodyDragCoefficient())
                  .forGetter(DroneDefinition.Physics::bodyDragCoefficient)
            )
            .apply(var0, DroneDefinition.Physics::new)
      );

      public Physics(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
         this.motorKv = var1;
         this.propDiameter = var2;
         this.propPitch = var3;
         this.dragCoefficient = var4;
         this.thrustMultiplier = var5;
         this.mass = var6;
         this.frontalArea = var7;
         this.bodyDragCoefficient = var8;
      }

      public float motorKv() {
         return this.motorKv;
      }

      public float propDiameter() {
         return this.propDiameter;
      }

      public float propPitch() {
         return this.propPitch;
      }

      public float dragCoefficient() {
         return this.dragCoefficient;
      }

      public float thrustMultiplier() {
         return this.thrustMultiplier;
      }

      public float mass() {
         return this.mass;
      }

      public float frontalArea() {
         return this.frontalArea;
      }

      public float bodyDragCoefficient() {
         return this.bodyDragCoefficient;
      }

   }

   private static final class Qz5WADR {
      private final List<AttachableEntry> v5suI;
      private final List<AttachableEntry> ibzfxic;
      private final DroneDefinition.Mount wuhB;
      private final DroneDefinition.BombBay dOfFwvo;
      private final DroneDefinition.Camera rsDd50tfV;
      private final DistanceSound uSUb;
      private final DroneDefinition.LoiterFlight jGhI2;
      private final DroneDefinition.Collision wEJx3Itb;
      private final DroneDefinition.Signal bAcdvq9;
      private final DroneDefinition.Engine c1jHYPb4;
      private final Guidance v8ZN;
      private final DroneRole zTT3nw6;
      private final DroneDefinition.RocketPod vmpmU;
      private final DroneDefinition.Sync b69s;
      private final List<InteractZone> cXHvq;
      private final List<DroneDefinition.DamageSource> rxYnaa;

      private Qz5WADR(
         List<AttachableEntry> var1,
         List<AttachableEntry> var2,
         DroneDefinition.Mount var3,
         DroneDefinition.BombBay var4,
         DroneDefinition.Camera var5,
         DistanceSound var6,
         DroneDefinition.LoiterFlight var7,
         DroneDefinition.Collision var8,
         DroneDefinition.Signal var9,
         DroneDefinition.Engine var10,
         Guidance var11,
         DroneRole var12,
         DroneDefinition.RocketPod var13,
         DroneDefinition.Sync var14,
         List<InteractZone> var15,
         List<DroneDefinition.DamageSource> var16
      ) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var5;
         this.uSUb = var6;
         this.jGhI2 = var7;
         this.wEJx3Itb = var8;
         this.bAcdvq9 = var9;
         this.c1jHYPb4 = var10;
         this.v8ZN = var11;
         this.zTT3nw6 = var12;
         this.vmpmU = var13;
         this.b69s = var14;
         this.cXHvq = var15;
         this.rxYnaa = var16;
      }

      public List<AttachableEntry> v5suI() {
         return this.v5suI;
      }

      public List<AttachableEntry> ibzfxic() {
         return this.ibzfxic;
      }

      public DroneDefinition.Mount wuhB() {
         return this.wuhB;
      }

      public DroneDefinition.BombBay dOfFwvo() {
         return this.dOfFwvo;
      }

      public DroneDefinition.Camera rsDd50tfV() {
         return this.rsDd50tfV;
      }

      public DistanceSound uSUb() {
         return this.uSUb;
      }

      public DroneDefinition.LoiterFlight jGhI2() {
         return this.jGhI2;
      }

      public DroneDefinition.Collision wEJx3Itb() {
         return this.wEJx3Itb;
      }

      public DroneDefinition.Signal bAcdvq9() {
         return this.bAcdvq9;
      }

      public DroneDefinition.Engine c1jHYPb4() {
         return this.c1jHYPb4;
      }

      public Guidance v8ZN() {
         return this.v8ZN;
      }

      public DroneRole zTT3nw6() {
         return this.zTT3nw6;
      }

      public DroneDefinition.RocketPod vmpmU() {
         return this.vmpmU;
      }

      public DroneDefinition.Sync b69s() {
         return this.b69s;
      }

      public List<InteractZone> cXHvq() {
         return this.cXHvq;
      }

      public List<DroneDefinition.DamageSource> rxYnaa() {
         return this.rxYnaa;
      }

   }

   public static final class Rates {
      private final float[] rcRate;
      private final float[] superRate;
      private final float[] expo;
      public static final DroneDefinition.Rates v5suI = new DroneDefinition.Rates(
         new float[]{1.0F, 1.15F, 1.15F, 1.15F}, new float[]{0.7F, 0.67F, 0.67F, 0.67F}, new float[]{0.0F, 0.0F, 0.0F, 0.0F}
      );
      public static final Codec<DroneDefinition.Rates> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               v5suI(v5suI.rcRate()).optionalFieldOf("RcRate", v5suI.rcRate()).forGetter(DroneDefinition.Rates::rcRate),
               v5suI(v5suI.superRate()).optionalFieldOf("SuperRate", v5suI.superRate()).forGetter(DroneDefinition.Rates::superRate),
               v5suI(v5suI.expo()).optionalFieldOf("Expo", v5suI.expo()).forGetter(DroneDefinition.Rates::expo)
            )
            .apply(var0, DroneDefinition.Rates::new)
      );

      public Rates(float[] var1, float[] var2, float[] var3) {
         this.rcRate = var1;
         this.superRate = var2;
         this.expo = var3;
      }

      private static Codec<float[]> v5suI(float[] var0) {
         return Codec.FLOAT.listOf().xmap(var1 -> {
            float[] var2 = (float[])var0.clone();

            for (int var3 = 0; var3 < var2.length && var3 < var1.size(); var3++) {
               var2[var3] = (Float)var1.get(var3);
            }

            return var2;
         }, var0x -> {
            ArrayList var1 = new ArrayList(var0x.length);

            for (float var5 : var0x) {
               var1.add(var5);
            }

            return var1;
         });
      }

      public float[] rcRate() {
         return this.rcRate;
      }

      public float[] superRate() {
         return this.superRate;
      }

      public float[] expo() {
         return this.expo;
      }

   }

   public static final class Render {
      private final boolean kirpichiki;
      private final boolean propellerBlur;
      public static final DroneDefinition.Render v5suI = new DroneDefinition.Render(true, true);
      public static final Codec<DroneDefinition.Render> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.BOOL.optionalFieldOf("Kirpichiki", v5suI.kirpichiki()).forGetter(DroneDefinition.Render::kirpichiki),
               Codec.BOOL.optionalFieldOf("PropellerBlur", v5suI.propellerBlur()).forGetter(DroneDefinition.Render::propellerBlur)
            )
            .apply(var0, DroneDefinition.Render::new)
      );

      public Render(boolean var1, boolean var2) {
         this.kirpichiki = var1;
         this.propellerBlur = var2;
      }

      public boolean kirpichiki() {
         return this.kirpichiki;
      }

      public boolean propellerBlur() {
         return this.propellerBlur;
      }

   }

   public static final class RocketPod {
      private final String ibzfxic;
      private final List<PayloadStation> wuhB;
      private final List<RocketMount> dOfFwvo;
      private final List<ParticleSpec> rsDd50tfV;
      public static final Codec<DroneDefinition.RocketPod> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.STRING.fieldOf("Projectile").forGetter(DroneDefinition.RocketPod::dOfFwvo),
               PayloadStation.v5suI.listOf().fieldOf("Tubes").forGetter(DroneDefinition.RocketPod::rsDd50tfV),
               RocketMount.v5suI.listOf().optionalFieldOf("Mounts", List.of()).forGetter(DroneDefinition.RocketPod::uSUb)
            )
            .apply(var0, (var0x, var1, var2) -> new DroneDefinition.RocketPod(var0x, var1, var2, List.of()))
      );

      public RocketPod(String var1, List<PayloadStation> var2, List<RocketMount> var3, List<ParticleSpec> var4) {
         var3 = var3 == null ? List.of() : List.copyOf(var3);
         var4 = var4 == null ? List.of() : List.copyOf(var4);
         this.ibzfxic = var1;
         this.wuhB = var2;
         this.dOfFwvo = var3;
         this.rsDd50tfV = var4;
      }

      public Set<String> v5suI() {
         HashSet var1 = new HashSet();

         for (PayloadStation var3 : this.wuhB) {
            var1.add(var3.ibzfxic());
         }

         return var1;
      }

      public List<RocketMount> ibzfxic() {
         if (!this.dOfFwvo.isEmpty()) {
            return this.dOfFwvo;
         }

         ArrayList var1 = new ArrayList(this.wuhB.size());

         for (PayloadStation var3 : this.wuhB) {
            var1.add(var3.ibzfxic());
         }

         return List.of(new RocketMount("pod", null, var1));
      }

      public int v5suI(String var1) {
         List var2 = this.ibzfxic();

         for (int var3 = 0; var3 < var2.size(); var3++) {
            if (((RocketMount)var2.get(var3)).wuhB().contains(var1)) {
               return var3;
            }
         }

         return -1;
      }

      public DroneDefinition.RocketPod v5suI(List<ParticleSpec> var1) {
         return new DroneDefinition.RocketPod(this.ibzfxic, this.wuhB, this.dOfFwvo, var1);
      }

      public boolean wuhB() {
         return !this.rsDd50tfV.isEmpty();
      }

      public String dOfFwvo() {
         return this.ibzfxic;
      }

      public List<PayloadStation> rsDd50tfV() {
         return this.wuhB;
      }

      public List<RocketMount> uSUb() {
         return this.dOfFwvo;
      }

      public List<ParticleSpec> jGhI2() {
         return this.rsDd50tfV;
      }
   }

   public static final class Signal {
      private final int maxRange;
      private final int freezeStartDistance;
      private final float obstaclePenaltyMultiplier;
      private final float projectile;
      private final int tubes;
      private final int mounts;
      private final float heightBonusPerBlock;
      private final int heightBaseline;
      public static final DroneDefinition.Signal v5suI = new DroneDefinition.Signal(350, 100, 1.0F, 35.0F, 30, 15, 0.1F, 64);
      public static final Codec<DroneDefinition.Signal> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("MaxRange", v5suI.maxRange()).forGetter(DroneDefinition.Signal::maxRange),
               Codec.intRange(0, Integer.MAX_VALUE)
                  .optionalFieldOf("FreezeStartDistance", v5suI.freezeStartDistance())
                  .forGetter(DroneDefinition.Signal::freezeStartDistance),
               Codec.floatRange(0.0F, 10.0F)
                  .optionalFieldOf("ObstaclePenaltyMultiplier", v5suI.obstaclePenaltyMultiplier())
                  .forGetter(DroneDefinition.Signal::obstaclePenaltyMultiplier),
               Codec.floatRange(0.0F, 100.0F).optionalFieldOf("GroundProximityPenaltyMax", v5suI.projectile()).forGetter(DroneDefinition.Signal::projectile),
               Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("GroundProximityDistanceStart", v5suI.tubes()).forGetter(DroneDefinition.Signal::tubes),
               Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("GroundProximityHeightThreshold", v5suI.mounts()).forGetter(DroneDefinition.Signal::mounts),
               Codec.floatRange(0.0F, 10.0F)
                  .optionalFieldOf("HeightBonusPerBlock", v5suI.heightBonusPerBlock())
                  .forGetter(DroneDefinition.Signal::heightBonusPerBlock),
               Codec.intRange(Integer.MIN_VALUE, Integer.MAX_VALUE)
                  .optionalFieldOf("HeightBaseline", v5suI.heightBaseline())
                  .forGetter(DroneDefinition.Signal::heightBaseline)
            )
            .apply(var0, DroneDefinition.Signal::new)
      );

      public Signal(int var1, int var2, float var3, float var4, int var5, int var6, float var7, int var8) {
         this.maxRange = var1;
         this.freezeStartDistance = var2;
         this.obstaclePenaltyMultiplier = var3;
         this.projectile = var4;
         this.tubes = var5;
         this.mounts = var6;
         this.heightBonusPerBlock = var7;
         this.heightBaseline = var8;
      }

      public DroneDefinition.Signal v5suI(float var1) {
         return var1 == 1.0F
            ? this
            : new DroneDefinition.Signal(
               Math.max(1, Math.round(this.maxRange * var1)),
               Math.max(0, Math.round(this.freezeStartDistance * var1)),
               this.obstaclePenaltyMultiplier,
               this.projectile,
               this.tubes,
               this.mounts,
               this.heightBonusPerBlock,
               this.heightBaseline
            );
      }

      public int maxRange() {
         return this.maxRange;
      }

      public int freezeStartDistance() {
         return this.freezeStartDistance;
      }

      public float obstaclePenaltyMultiplier() {
         return this.obstaclePenaltyMultiplier;
      }

      public float projectile() {
         return this.projectile;
      }

      public int tubes() {
         return this.tubes;
      }

      public int mounts() {
         return this.mounts;
      }

      public float heightBonusPerBlock() {
         return this.heightBonusPerBlock;
      }

      public int heightBaseline() {
         return this.heightBaseline;
      }

   }

   public static final class Sync {
      private final int correctionTimeMs;
      private final float snapDistance;
      private final int maxPredictionMs;
      private final FragmentStream bAcdvq9;
      public static final DroneDefinition.Sync v5suI = new DroneDefinition.Sync((int)Math.round(120.0), 8.0F, (int)Math.round(500.0), FragmentStream.v5suI);
      public static final DroneDefinition.Sync ibzfxic = new DroneDefinition.Sync(v5suI.correctionTimeMs(), 24.0F, v5suI.maxPredictionMs(), v5suI.jGhI2());
      public static final DroneDefinition.Sync wuhB = new DroneDefinition.Sync(
         v5suI.correctionTimeMs(),
         v5suI.snapDistance(),
         v5suI.maxPredictionMs(),
         new FragmentStream(FragmentStream.v5suI.wuhB(), FragmentStream.v5suI.dOfFwvo(), 1.5F)
      );
      public static final DroneDefinition.Sync dOfFwvo = new DroneDefinition.Sync(v5suI.correctionTimeMs(), 12.0F, v5suI.maxPredictionMs(), wuhB.jGhI2());
      public static final Codec<DroneDefinition.Sync> rsDd50tfV = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.intRange(1, 60000).optionalFieldOf("CorrectionTimeMs", v5suI.correctionTimeMs()).forGetter(DroneDefinition.Sync::correctionTimeMs),
               Codec.floatRange(0.1F, 4096.0F).optionalFieldOf("SnapDistance", v5suI.snapDistance()).forGetter(DroneDefinition.Sync::snapDistance),
               Codec.intRange(50, 60000).optionalFieldOf("MaxPredictionMs", v5suI.maxPredictionMs()).forGetter(DroneDefinition.Sync::maxPredictionMs),
               FragmentStream.ibzfxic.forGetter(DroneDefinition.Sync::jGhI2)
            )
            .apply(var0, DroneDefinition.Sync::new)
      );

      public Sync(int var1, float var2, int var3, FragmentStream var4) {
         this.correctionTimeMs = var1;
         this.snapDistance = var2;
         this.maxPredictionMs = var3;
         this.bAcdvq9 = var4;
      }

      public double v5suI() {
         return this.correctionTimeMs / 1000.0;
      }

      public double ibzfxic() {
         return this.maxPredictionMs / 1000.0;
      }

      public Record_IukVkAm wuhB() {
         return this.bAcdvq9.v5suI();
      }

      public FragmentStream jGhI2() {
         return this.bAcdvq9;
      }

      public int correctionTimeMs() {
         return this.correctionTimeMs;
      }

      public float snapDistance() {
         return this.snapDistance;
      }

      public int maxPredictionMs() {
         return this.maxPredictionMs;
      }

   }

   public static final class Wreck {
      private final double gravity;
      private final double maxFall;
      private final double skidFriction;
      private final double staticFrictionSpeed;
      private final double invInertiaPitch;
      private final double invInertiaRoll;
      private final double invInertiaYaw;
      private final double yawDamping;
      private final double maxYawVel;
      private final double shoveSpeed;
      private final double shoveReach;
      private final double shoveYawGain;
      private final double meleeKnockSpeed;
      private final double meleeYawGain;
      private final double glideDiveRate;
      private final double glideMaxPitch;
      public static final DroneDefinition.Wreck v5suI = new DroneDefinition.Wreck(
         0.08, 0.45, 0.85, 0.04, 1.5, 0.4, 1.0, 0.88, 9.0, 0.07, 0.85, 4.0, 0.45, 9.0, 3.0, 22.0
      );
      public static final Codec<DroneDefinition.Wreck> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.doubleRange(0.0, 5.0).optionalFieldOf("Gravity", v5suI.gravity()).forGetter(DroneDefinition.Wreck::gravity),
               Codec.doubleRange(0.01, 10.0).optionalFieldOf("MaxFall", v5suI.maxFall()).forGetter(DroneDefinition.Wreck::maxFall),
               Codec.doubleRange(0.0, 0.999).optionalFieldOf("SkidFriction", v5suI.skidFriction()).forGetter(DroneDefinition.Wreck::skidFriction),
               Codec.doubleRange(0.0, 5.0)
                  .optionalFieldOf("StaticFrictionSpeed", v5suI.staticFrictionSpeed())
                  .forGetter(DroneDefinition.Wreck::staticFrictionSpeed),
               Codec.doubleRange(0.0, 100.0).optionalFieldOf("InvInertiaPitch", v5suI.invInertiaPitch()).forGetter(DroneDefinition.Wreck::invInertiaPitch),
               Codec.doubleRange(0.0, 100.0).optionalFieldOf("InvInertiaRoll", v5suI.invInertiaRoll()).forGetter(DroneDefinition.Wreck::invInertiaRoll),
               Codec.doubleRange(0.0, 100.0).optionalFieldOf("InvInertiaYaw", v5suI.invInertiaYaw()).forGetter(DroneDefinition.Wreck::invInertiaYaw),
               Codec.doubleRange(0.0, 1.0).optionalFieldOf("YawDamping", v5suI.yawDamping()).forGetter(DroneDefinition.Wreck::yawDamping),
               Codec.doubleRange(0.1, 90.0).optionalFieldOf("MaxYawVel", v5suI.maxYawVel()).forGetter(DroneDefinition.Wreck::maxYawVel),
               Codec.doubleRange(0.0, 10.0).optionalFieldOf("ShoveSpeed", v5suI.shoveSpeed()).forGetter(DroneDefinition.Wreck::shoveSpeed),
               Codec.doubleRange(0.1, 8.0).optionalFieldOf("ShoveReach", v5suI.shoveReach()).forGetter(DroneDefinition.Wreck::shoveReach),
               Codec.doubleRange(0.0, 100.0).optionalFieldOf("ShoveYawGain", v5suI.shoveYawGain()).forGetter(DroneDefinition.Wreck::shoveYawGain),
               Codec.doubleRange(0.0, 10.0).optionalFieldOf("MeleeKnockSpeed", v5suI.meleeKnockSpeed()).forGetter(DroneDefinition.Wreck::meleeKnockSpeed),
               Codec.doubleRange(0.0, 100.0).optionalFieldOf("MeleeYawGain", v5suI.meleeYawGain()).forGetter(DroneDefinition.Wreck::meleeYawGain),
               Codec.doubleRange(0.0, 90.0).optionalFieldOf("GlideDiveRate", v5suI.glideDiveRate()).forGetter(DroneDefinition.Wreck::glideDiveRate),
               Codec.doubleRange(0.0, 90.0).optionalFieldOf("GlideMaxPitch", v5suI.glideMaxPitch()).forGetter(DroneDefinition.Wreck::glideMaxPitch)
            )
            .apply(var0, DroneDefinition.Wreck::new)
      );

      public Wreck(
         double var1,
         double var3,
         double var5,
         double var7,
         double var9,
         double var11,
         double var13,
         double var15,
         double var17,
         double var19,
         double var21,
         double var23,
         double var25,
         double var27,
         double var29,
         double var31
      ) {
         this.gravity = var1;
         this.maxFall = var3;
         this.skidFriction = var5;
         this.staticFrictionSpeed = var7;
         this.invInertiaPitch = var9;
         this.invInertiaRoll = var11;
         this.invInertiaYaw = var13;
         this.yawDamping = var15;
         this.maxYawVel = var17;
         this.shoveSpeed = var19;
         this.shoveReach = var21;
         this.shoveYawGain = var23;
         this.meleeKnockSpeed = var25;
         this.meleeYawGain = var27;
         this.glideDiveRate = var29;
         this.glideMaxPitch = var31;
      }

      public double gravity() {
         return this.gravity;
      }

      public double maxFall() {
         return this.maxFall;
      }

      public double skidFriction() {
         return this.skidFriction;
      }

      public double staticFrictionSpeed() {
         return this.staticFrictionSpeed;
      }

      public double invInertiaPitch() {
         return this.invInertiaPitch;
      }

      public double invInertiaRoll() {
         return this.invInertiaRoll;
      }

      public double invInertiaYaw() {
         return this.invInertiaYaw;
      }

      public double yawDamping() {
         return this.yawDamping;
      }

      public double maxYawVel() {
         return this.maxYawVel;
      }

      public double shoveSpeed() {
         return this.shoveSpeed;
      }

      public double shoveReach() {
         return this.shoveReach;
      }

      public double shoveYawGain() {
         return this.shoveYawGain;
      }

      public double meleeKnockSpeed() {
         return this.meleeKnockSpeed;
      }

      public double meleeYawGain() {
         return this.meleeYawGain;
      }

      public double glideDiveRate() {
         return this.glideDiveRate;
      }

      public double glideMaxPitch() {
         return this.glideMaxPitch;
      }

   }
}
