package com.fpvdrone.data;

import com.fpvdrone.data.Detonator;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public final class ProjectileDefinition {
   private final String wuhB;
   private final String dOfFwvo;
   private final float rsDd50tfV;
   private final float uSUb;
   private final int jGhI2;
   private final float wEJx3Itb;
   private final float bAcdvq9;
   private final float c1jHYPb4;
   private final String v8ZN;
   private final ShrapnelStats zTT3nw6;
   private final String vmpmU;
   private final String b69s;
   private final ProjectileDefinition.Drop cXHvq;
   private final ProjectileDefinition.Deflect rxYnaa;
   private final ProjectileDefinition.Grenade r6s7FL3;
   private final SoundProfile iAKNPAj;
   private final ProjectileDefinition.Fuze qlv6NKWnH;
   private final ProjectileDefinition.HangSwing hOKMYHOaC;
   private final ProjectileDefinition.Launch iS1JnZov;
   private final ProjectileDefinition.Motor lfyfLwuw;
   private final SoundProfile wyNDWDB;
   private final FragmentStream iRWc2Pi;
   private final String tpzIHm;
   public static final float CODEC = 200.0F;
   private static final MapCodec<ProjectileDefinition.Gda02ntTr> eJDY = RecordCodecBuilder.mapCodec(
      var0 -> var0.group(
            Codec.STRING.fieldOf("DisplayName").forGetter(ProjectileDefinition.Gda02ntTr::v5suI),
            Codec.floatRange(0.0F, 100000.0F).optionalFieldOf("WeightGrams", 1000.0F).forGetter(ProjectileDefinition.Gda02ntTr::ibzfxic),
            Codec.floatRange(1.0F, 10.0F).optionalFieldOf("ExplosionPower", 4.0F).forGetter(ProjectileDefinition.Gda02ntTr::wuhB),
            Codec.intRange(8, 512).optionalFieldOf("FragmentCount", 128).forGetter(ProjectileDefinition.Gda02ntTr::dOfFwvo),
            Codec.floatRange(0.0F, 100.0F).optionalFieldOf("DetonationSpeed", 2.0F).forGetter(ProjectileDefinition.Gda02ntTr::rsDd50tfV),
            Codec.floatRange(0.0F, 100000.0F).optionalFieldOf("ShapedChargeDamage", 250.0F).forGetter(ProjectileDefinition.Gda02ntTr::uSUb),
            Codec.floatRange(0.0F, 1000000.0F).optionalFieldOf("VehicleDamage", 200.0F).forGetter(ProjectileDefinition.Gda02ntTr::jGhI2),
            Codec.STRING.optionalFieldOf("Behavior", "shrapnel").forGetter(ProjectileDefinition.Gda02ntTr::wEJx3Itb),
            ShrapnelStats.CODEC.fieldOf("Stats").forGetter(ProjectileDefinition.Gda02ntTr::bAcdvq9),
            Codec.STRING.optionalFieldOf("Model", "").forGetter(ProjectileDefinition.Gda02ntTr::c1jHYPb4),
            Codec.STRING.optionalFieldOf("Texture", "").forGetter(ProjectileDefinition.Gda02ntTr::v8ZN)
         )
         .apply(var0, ProjectileDefinition.Gda02ntTr::new)
   );
   private static final MapCodec<ProjectileDefinition.G2pNpnR5J> zHBAQgZP = RecordCodecBuilder.mapCodec(
      var0 -> var0.group(
            ProjectileDefinition.Drop.v5suI.optionalFieldOf("Drop").forGetter(var0x -> Optional.ofNullable(var0x.v5suI())),
            ProjectileDefinition.Deflect.v5suI.optionalFieldOf("Deflect").forGetter(var0x -> Optional.ofNullable(var0x.ibzfxic())),
            ProjectileDefinition.Grenade.v5suI.optionalFieldOf("Grenade").forGetter(var0x -> Optional.ofNullable(var0x.wuhB())),
            SoundProfile.CODEC.optionalFieldOf("Sounds").forGetter(var0x -> Optional.ofNullable(var0x.dOfFwvo())),
            ProjectileDefinition.Fuze.ibzfxic.optionalFieldOf("Fuze").forGetter(var0x -> Optional.ofNullable(var0x.rsDd50tfV())),
            ProjectileDefinition.HangSwing.ibzfxic.optionalFieldOf("HangSwing").forGetter(var0x -> Optional.ofNullable(var0x.uSUb())),
            ProjectileDefinition.Launch.ibzfxic.optionalFieldOf("Launch").forGetter(var0x -> Optional.ofNullable(var0x.jGhI2())),
            ProjectileDefinition.Motor.v5suI.optionalFieldOf("Motor").forGetter(var0x -> Optional.ofNullable(var0x.wEJx3Itb())),
            SoundProfile.CODEC.optionalFieldOf("LaunchSounds").forGetter(var0x -> Optional.ofNullable(var0x.bAcdvq9())),
            FragmentStream.wuhB.optionalFieldOf("Stream").forGetter(var0x -> Optional.ofNullable(var0x.c1jHYPb4())),
            Codec.STRING.optionalFieldOf("Item", "").forGetter(ProjectileDefinition.G2pNpnR5J::v8ZN)
         )
         .apply(
            var0,
            (var0x, var1, var2, var3, var4, var5, var6, var7, var8, var9, var10) -> new ProjectileDefinition.G2pNpnR5J(
               (ProjectileDefinition.Drop)var0x.orElse(null),
               (ProjectileDefinition.Deflect)var1.orElse(null),
               (ProjectileDefinition.Grenade)var2.orElse(null),
               (SoundProfile)var3.orElse(null),
               (ProjectileDefinition.Fuze)var4.orElse(null),
               (ProjectileDefinition.HangSwing)var5.orElse(null),
               (ProjectileDefinition.Launch)var6.orElse(null),
               (ProjectileDefinition.Motor)var7.orElse(null),
               (SoundProfile)var8.orElse(null),
               (FragmentStream)var9.orElse(null),
               var10
            )
         )
   );
   public static final Codec<ProjectileDefinition> ibzfxic = Codec.mapPair(eJDY, zHBAQgZP)
      .codec()
      .xmap(
         var0 -> new ProjectileDefinition(
            "",
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).v5suI(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).ibzfxic(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).wuhB(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).dOfFwvo(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).rsDd50tfV(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).uSUb(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).jGhI2(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).wEJx3Itb(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).bAcdvq9(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).c1jHYPb4(),
            ((ProjectileDefinition.Gda02ntTr)var0.getFirst()).v8ZN(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).v5suI(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).ibzfxic(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).wuhB(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).dOfFwvo(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).rsDd50tfV(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).uSUb(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).jGhI2(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).wEJx3Itb(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).bAcdvq9(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).c1jHYPb4(),
            ((ProjectileDefinition.G2pNpnR5J)var0.getSecond()).v8ZN()
         ),
         var0 -> Pair.of(
            new ProjectileDefinition.Gda02ntTr(
               var0.displayName(),
               var0.weightGrams(),
               var0.explosionPower(),
               var0.fragmentCount(),
               var0.detonationSpeed(),
               var0.shapedChargeDamage(),
               var0.vehicleDamage(),
               var0.behavior(),
               var0.stats(),
               var0.model(),
               var0.texture()
            ),
            new ProjectileDefinition.G2pNpnR5J(
               var0.drop(),
               var0.deflect(),
               var0.grenade(),
               var0.soundsOrNull(),
               var0.fuzeOrNull(),
               var0.hangSwingOrNull(),
               var0.launchOrNull(),
               var0.motor(),
               var0.launchSoundsOrNull(),
               var0.streamOrNull(),
               var0.itemOrEmpty()
            )
         )
      );

   public ProjectileDefinition(
      String var1,
      String var2,
      float var3,
      float var4,
      int var5,
      float var6,
      float var7,
      float var8,
      String var9,
      ShrapnelStats var10,
      String var11,
      String var12,
      ProjectileDefinition.Drop var13,
      ProjectileDefinition.Deflect var14,
      ProjectileDefinition.Grenade var15,
      SoundProfile var16,
      ProjectileDefinition.Fuze var17,
      ProjectileDefinition.HangSwing var18,
      ProjectileDefinition.Launch var19,
      ProjectileDefinition.Motor var20,
      SoundProfile var21,
      FragmentStream var22,
      String var23
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
   }

   public ProjectileDefinition withId(String var1) {
      return new ProjectileDefinition(
         var1,
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
         this.tpzIHm
      );
   }

   public String itemId() {
      return this.tpzIHm != null && !this.tpzIHm.isEmpty() ? this.tpzIHm : "fpvdrone:" + this.wuhB;
   }

   public Detonator detonator() {
      return ProjectileDefinitions.detonator(this.v8ZN);
   }

   public SoundProfile explosionSound() {
      return this.iAKNPAj != null ? this.iAKNPAj : SoundProfile.BOMBLET;
   }

   public ProjectileDefinition.Fuze fuze() {
      return this.qlv6NKWnH != null ? this.qlv6NKWnH : ProjectileDefinition.Fuze.v5suI;
   }

   public boolean droppable() {
      return this.cXHvq != null;
   }

   public float dropModelYaw() {
      return this.cXHvq != null ? this.cXHvq.modelYawDeg() : 180.0F;
   }

   public boolean deflects() {
      return this.rxYnaa != null && this.rxYnaa.enabled();
   }

   public boolean isGrenade() {
      return this.r6s7FL3 != null;
   }

   public ProjectileDefinition.HangSwing hangSwing() {
      if (this.hOKMYHOaC != null) {
         return this.hOKMYHOaC;
      } else {
         return this.isGrenade() ? ProjectileDefinition.HangSwing.v5suI : null;
      }
   }

   public boolean swings() {
      ProjectileDefinition.HangSwing var1 = this.hangSwing();
      return var1 != null && var1.enabled();
   }

   public boolean launchable() {
      return this.iS1JnZov != null;
   }

   public ProjectileDefinition.Launch launch() {
      return this.iS1JnZov != null ? this.iS1JnZov : ProjectileDefinition.Launch.v5suI;
   }

   public FragmentStream stream() {
      return this.iRWc2Pi != null ? this.iRWc2Pi : FragmentStream.v5suI;
   }

   public boolean hasMotorParticles() {
      return this.lfyfLwuw != null && !this.motorParticles().isEmpty();
   }

   public List<ParticleSpec> motorParticles() {
      return ProjectileDefinitions.motorParticles(this.wuhB);
   }

   public SoundProfile launchSound() {
      return this.wyNDWDB;
   }

   public String id() {
      return this.wuhB;
   }

   public String displayName() {
      return this.dOfFwvo;
   }

   public float weightGrams() {
      return this.rsDd50tfV;
   }

   public float explosionPower() {
      return this.uSUb;
   }

   public int fragmentCount() {
      return this.jGhI2;
   }

   public float detonationSpeed() {
      return this.wEJx3Itb;
   }

   public float shapedChargeDamage() {
      return this.bAcdvq9;
   }

   public float vehicleDamage() {
      return this.c1jHYPb4;
   }

   public String behavior() {
      return this.v8ZN;
   }

   public ShrapnelStats stats() {
      return this.zTT3nw6;
   }

   public String model() {
      return this.vmpmU;
   }

   public String texture() {
      return this.b69s;
   }

   public ProjectileDefinition.Drop drop() {
      return this.cXHvq;
   }

   public ProjectileDefinition.Deflect deflect() {
      return this.rxYnaa;
   }

   public ProjectileDefinition.Grenade grenade() {
      return this.r6s7FL3;
   }

   public SoundProfile soundsOrNull() {
      return this.iAKNPAj;
   }

   public ProjectileDefinition.Fuze fuzeOrNull() {
      return this.qlv6NKWnH;
   }

   public ProjectileDefinition.HangSwing hangSwingOrNull() {
      return this.hOKMYHOaC;
   }

   public ProjectileDefinition.Launch launchOrNull() {
      return this.iS1JnZov;
   }

   public ProjectileDefinition.Motor motor() {
      return this.lfyfLwuw;
   }

   public SoundProfile launchSoundsOrNull() {
      return this.wyNDWDB;
   }

   public FragmentStream streamOrNull() {
      return this.iRWc2Pi;
   }

   public String itemOrEmpty() {
      return this.tpzIHm;
   }

   public static final class Deflect {
      private final boolean enabled;
      private final float restitution;
      private final float maxImpulse;
      private final String requiredTool;
      private final int armDelayAfterDeflect;
      public static final Codec<ProjectileDefinition.Deflect> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.BOOL.optionalFieldOf("Enabled", true).forGetter(ProjectileDefinition.Deflect::enabled),
               Codec.floatRange(0.0F, 1.0F).optionalFieldOf("Restitution", 0.45F).forGetter(ProjectileDefinition.Deflect::restitution),
               Codec.floatRange(0.0F, 5.0F).optionalFieldOf("MaxImpulse", 0.08F).forGetter(ProjectileDefinition.Deflect::maxImpulse),
               Codec.STRING.optionalFieldOf("RequiredTool", "").forGetter(ProjectileDefinition.Deflect::requiredTool),
               Codec.intRange(0, 100).optionalFieldOf("ArmDelayAfterDeflect", 4).forGetter(ProjectileDefinition.Deflect::armDelayAfterDeflect)
            )
            .apply(var0, ProjectileDefinition.Deflect::new)
      );

      public Deflect(boolean var1, float var2, float var3, String var4, int var5) {
         this.enabled = var1;
         this.restitution = var2;
         this.maxImpulse = var3;
         this.requiredTool = var4;
         this.armDelayAfterDeflect = var5;
      }

      public boolean enabled() {
         return this.enabled;
      }

      public float restitution() {
         return this.restitution;
      }

      public float maxImpulse() {
         return this.maxImpulse;
      }

      public String requiredTool() {
         return this.requiredTool;
      }

      public int armDelayAfterDeflect() {
         return this.armDelayAfterDeflect;
      }

   }

   public static final class Drop {
      private final float mass;
      private final float dragCoefficient;
      private final float crossSection;
      private final float alignStiffness;
      private final float angularDamping;
      private final float modelYawDeg;
      public static final Codec<ProjectileDefinition.Drop> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.floatRange(0.01F, 100.0F).optionalFieldOf("Mass", 0.25F).forGetter(ProjectileDefinition.Drop::mass),
               Codec.floatRange(0.01F, 4.0F).optionalFieldOf("DragCoefficient", 0.55F).forGetter(ProjectileDefinition.Drop::dragCoefficient),
               Codec.floatRange(1.0E-4F, 5.0F).optionalFieldOf("CrossSection", 0.0013F).forGetter(ProjectileDefinition.Drop::crossSection),
               Codec.floatRange(0.0F, 50.0F).optionalFieldOf("AlignStiffness", 0.9F).forGetter(ProjectileDefinition.Drop::alignStiffness),
               Codec.floatRange(0.0F, 50.0F).optionalFieldOf("AngularDamping", 2.2F).forGetter(ProjectileDefinition.Drop::angularDamping),
               Codec.FLOAT.optionalFieldOf("ModelYawDeg", 180.0F).forGetter(ProjectileDefinition.Drop::modelYawDeg)
            )
            .apply(var0, ProjectileDefinition.Drop::new)
      );

      public Drop(float var1, float var2, float var3, float var4, float var5, float var6) {
         this.mass = var1;
         this.dragCoefficient = var2;
         this.crossSection = var3;
         this.alignStiffness = var4;
         this.angularDamping = var5;
         this.modelYawDeg = var6;
      }

      public float mass() {
         return this.mass;
      }

      public float dragCoefficient() {
         return this.dragCoefficient;
      }

      public float crossSection() {
         return this.crossSection;
      }

      public float alignStiffness() {
         return this.alignStiffness;
      }

      public float angularDamping() {
         return this.angularDamping;
      }

      public float modelYawDeg() {
         return this.modelYawDeg;
      }

   }

   public static final class Fuze {
      private final int wuhB;
      public static final ProjectileDefinition.Fuze v5suI = new ProjectileDefinition.Fuze(3);
      public static final Codec<ProjectileDefinition.Fuze> ibzfxic = Codec.floatRange(0.0F, 30.0F)
         .optionalFieldOf("ArmingDelaySeconds", v5suI.v5suI() / 20.0F)
         .xmap(var0 -> new ProjectileDefinition.Fuze(Math.round(var0 * 20.0F)), var0 -> var0.v5suI() / 20.0F)
         .codec();

      public Fuze(int var1) {
         this.wuhB = var1;
      }

      public int v5suI() {
         return this.wuhB;
      }
   
      public int wuhB() {
         return this.wuhB;
      }
   }

   private static final class G2pNpnR5J {
      private final ProjectileDefinition.Drop v5suI;
      private final ProjectileDefinition.Deflect ibzfxic;
      private final ProjectileDefinition.Grenade wuhB;
      private final SoundProfile dOfFwvo;
      private final ProjectileDefinition.Fuze rsDd50tfV;
      private final ProjectileDefinition.HangSwing uSUb;
      private final ProjectileDefinition.Launch jGhI2;
      private final ProjectileDefinition.Motor wEJx3Itb;
      private final SoundProfile bAcdvq9;
      private final FragmentStream c1jHYPb4;
      private final String v8ZN;

      private G2pNpnR5J(
         ProjectileDefinition.Drop var1,
         ProjectileDefinition.Deflect var2,
         ProjectileDefinition.Grenade var3,
         SoundProfile var4,
         ProjectileDefinition.Fuze var5,
         ProjectileDefinition.HangSwing var6,
         ProjectileDefinition.Launch var7,
         ProjectileDefinition.Motor var8,
         SoundProfile var9,
         FragmentStream var10,
         String var11
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
      }

      public ProjectileDefinition.Drop v5suI() {
         return this.v5suI;
      }

      public ProjectileDefinition.Deflect ibzfxic() {
         return this.ibzfxic;
      }

      public ProjectileDefinition.Grenade wuhB() {
         return this.wuhB;
      }

      public SoundProfile dOfFwvo() {
         return this.dOfFwvo;
      }

      public ProjectileDefinition.Fuze rsDd50tfV() {
         return this.rsDd50tfV;
      }

      public ProjectileDefinition.HangSwing uSUb() {
         return this.uSUb;
      }

      public ProjectileDefinition.Launch jGhI2() {
         return this.jGhI2;
      }

      public ProjectileDefinition.Motor wEJx3Itb() {
         return this.wEJx3Itb;
      }

      public SoundProfile bAcdvq9() {
         return this.bAcdvq9;
      }

      public FragmentStream c1jHYPb4() {
         return this.c1jHYPb4;
      }

      public String v8ZN() {
         return this.v8ZN;
      }

   }

   private static final class Gda02ntTr {
      private final String v5suI;
      private final float ibzfxic;
      private final float wuhB;
      private final int dOfFwvo;
      private final float rsDd50tfV;
      private final float uSUb;
      private final float jGhI2;
      private final String wEJx3Itb;
      private final ShrapnelStats bAcdvq9;
      private final String c1jHYPb4;
      private final String v8ZN;

      private Gda02ntTr(
         String var1, float var2, float var3, int var4, float var5, float var6, float var7, String var8, ShrapnelStats var9, String var10, String var11
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
      }

      public String v5suI() {
         return this.v5suI;
      }

      public float ibzfxic() {
         return this.ibzfxic;
      }

      public float wuhB() {
         return this.wuhB;
      }

      public int dOfFwvo() {
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

      public ShrapnelStats bAcdvq9() {
         return this.bAcdvq9;
      }

      public String c1jHYPb4() {
         return this.c1jHYPb4;
      }

      public String v8ZN() {
         return this.v8ZN;
      }

   }

   public static final class Grenade {
      private final int fuseTicks;
      private final float restitution;
      private final float minBounceSpeed;
      private final float groundFriction;
      private final float entityRestitution;
      public static final Codec<ProjectileDefinition.Grenade> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.intRange(1, 600).optionalFieldOf("FuseTicks", 80).forGetter(ProjectileDefinition.Grenade::fuseTicks),
               Codec.floatRange(0.0F, 1.0F).optionalFieldOf("Restitution", 0.35F).forGetter(ProjectileDefinition.Grenade::restitution),
               Codec.floatRange(0.0F, 2.0F).optionalFieldOf("MinBounceSpeed", 0.06F).forGetter(ProjectileDefinition.Grenade::minBounceSpeed),
               Codec.floatRange(0.0F, 1.0F).optionalFieldOf("GroundFriction", 0.78F).forGetter(ProjectileDefinition.Grenade::groundFriction),
               Codec.floatRange(0.0F, 1.0F).optionalFieldOf("EntityRestitution", 0.25F).forGetter(ProjectileDefinition.Grenade::entityRestitution)
            )
            .apply(var0, ProjectileDefinition.Grenade::new)
      );

      public Grenade(int var1, float var2, float var3, float var4, float var5) {
         this.fuseTicks = var1;
         this.restitution = var2;
         this.minBounceSpeed = var3;
         this.groundFriction = var4;
         this.entityRestitution = var5;
      }

      public int fuseTicks() {
         return this.fuseTicks;
      }

      public float restitution() {
         return this.restitution;
      }

      public float minBounceSpeed() {
         return this.minBounceSpeed;
      }

      public float groundFriction() {
         return this.groundFriction;
      }

      public float entityRestitution() {
         return this.entityRestitution;
      }

   }

   public static final class HangSwing {
      private final boolean enabled;
      private final float pivotY;
      private final float stiffness;
      private final float damping;
      private final float maxLeanDeg;
      private final float accelGain;
      private final float dragGain;
      private final boolean followYaw;
      public static final ProjectileDefinition.HangSwing v5suI = new ProjectileDefinition.HangSwing(true, 2.75F, 0.16F, 0.3F, 38.0F, 2338.6033F, 30.0F, true);
      public static final Codec<ProjectileDefinition.HangSwing> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.BOOL.optionalFieldOf("Enabled", v5suI.enabled()).forGetter(ProjectileDefinition.HangSwing::enabled),
               Codec.floatRange(-16.0F, 16.0F).optionalFieldOf("PivotY", v5suI.pivotY()).forGetter(ProjectileDefinition.HangSwing::pivotY),
               Codec.floatRange(0.0F, 2.0F).optionalFieldOf("Stiffness", v5suI.stiffness()).forGetter(ProjectileDefinition.HangSwing::stiffness),
               Codec.floatRange(0.0F, 2.0F).optionalFieldOf("Damping", v5suI.damping()).forGetter(ProjectileDefinition.HangSwing::damping),
               Codec.floatRange(0.0F, 90.0F).optionalFieldOf("MaxLeanDeg", v5suI.maxLeanDeg()).forGetter(ProjectileDefinition.HangSwing::maxLeanDeg),
               Codec.floatRange(0.0F, 100000.0F).optionalFieldOf("AccelGain", v5suI.accelGain()).forGetter(ProjectileDefinition.HangSwing::accelGain),
               Codec.floatRange(0.0F, 1000.0F).optionalFieldOf("DragGain", v5suI.dragGain()).forGetter(ProjectileDefinition.HangSwing::dragGain),
               Codec.BOOL.optionalFieldOf("FollowYaw", v5suI.followYaw()).forGetter(ProjectileDefinition.HangSwing::followYaw)
            )
            .apply(var0, ProjectileDefinition.HangSwing::new)
      );

      public HangSwing(boolean var1, float var2, float var3, float var4, float var5, float var6, float var7, boolean var8) {
         this.enabled = var1;
         this.pivotY = var2;
         this.stiffness = var3;
         this.damping = var4;
         this.maxLeanDeg = var5;
         this.accelGain = var6;
         this.dragGain = var7;
         this.followYaw = var8;
      }

      public boolean enabled() {
         return this.enabled;
      }

      public float pivotY() {
         return this.pivotY;
      }

      public float stiffness() {
         return this.stiffness;
      }

      public float damping() {
         return this.damping;
      }

      public float maxLeanDeg() {
         return this.maxLeanDeg;
      }

      public float accelGain() {
         return this.accelGain;
      }

      public float dragGain() {
         return this.dragGain;
      }

      public boolean followYaw() {
         return this.followYaw;
      }

   }

   public static final class Launch {
      private final float muzzleVelocity;
      private final float dispersionDeg;
      public static final ProjectileDefinition.Launch v5suI = new ProjectileDefinition.Launch(0.0F, 0.0F);
      public static final Codec<ProjectileDefinition.Launch> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.floatRange(0.0F, 40.0F).optionalFieldOf("MuzzleVelocity", 3.0F).forGetter(ProjectileDefinition.Launch::muzzleVelocity),
               Codec.floatRange(0.0F, 45.0F).optionalFieldOf("DispersionDeg", 0.0F).forGetter(ProjectileDefinition.Launch::dispersionDeg)
            )
            .apply(var0, ProjectileDefinition.Launch::new)
      );

      public Launch(float var1, float var2) {
         this.muzzleVelocity = var1;
         this.dispersionDeg = var2;
      }

      public float muzzleVelocity() {
         return this.muzzleVelocity;
      }

      public float dispersionDeg() {
         return this.dispersionDeg;
      }

   }

   public static final class Motor {
      private final int ibzfxic;
      public static final Codec<ProjectileDefinition.Motor> v5suI = Codec.intRange(0, 400)
         .optionalFieldOf("BurnTicks", 20)
         .xmap(ProjectileDefinition.Motor::new, ProjectileDefinition.Motor::v5suI)
         .codec();

      public Motor(int var1) {
         this.ibzfxic = var1;
      }

      public int v5suI() {
         return this.ibzfxic;
      }
   
      public int ibzfxic() {
         return this.ibzfxic;
      }
   }
}
