package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RampDefinition {
   private final String bAcdvq9;
   private final String c1jHYPb4;
   private final String v8ZN;
   private final String zTT3nw6;
   private final AttachTransform vmpmU;
   private final Map<String, AttachTransform> b69s;
   private final double cXHvq;
   private final double rxYnaa;
   private final double r6s7FL3;
   private final List<String> iAKNPAj;
   private final List<InteractZone> qlv6NKWnH;
   private final String hOKMYHOaC;
   public static final String v5suI = "geo/ramp_drun.geo.json";
   public static final String ibzfxic = "textures/entity/ramp_drones.png";
   public static final String wuhB = "drone_mount";
   public static final AttachTransform dOfFwvo = new AttachTransform("drone_mount", 0.0F, 17.73637F, 15.38546F, 0.0F, 0.0F, 0.0F, 1.0F);
   public static final String rsDd50tfV = "*";
   public static final String uSUb = "ramp";
   public static final String jGhI2 = "rampService";
   private static final Codec<double[]> iS1JnZov = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.DOUBLE.optionalFieldOf("Up", 1.176325).forGetter(var0x -> var0x[0]),
            Codec.DOUBLE.optionalFieldOf("Rail", 1.658017).forGetter(var0x -> var0x[1]),
            Codec.DOUBLE.optionalFieldOf("Side", 0.0).forGetter(var0x -> var0x[2])
         )
         .apply(var0, (var0x, var1, var2) -> new double[]{var0x, var1, var2})
   );
   public static final Codec<RampDefinition> wEJx3Itb = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.fieldOf("DisplayName").forGetter(RampDefinition::rsDd50tfV),
            Codec.STRING.optionalFieldOf("Model", "geo/ramp_drun.geo.json").forGetter(RampDefinition::uSUb),
            Codec.STRING.optionalFieldOf("Texture", "textures/entity/ramp_drones.png").forGetter(RampDefinition::jGhI2),
            AttachTransform.v5suI.optionalFieldOf("DroneAttach", dOfFwvo).forGetter(RampDefinition::wEJx3Itb),
            Codec.unboundedMap(Codec.STRING, AttachTransform.v5suI).optionalFieldOf("DroneAttachOverrides", Map.of()).forGetter(RampDefinition::bAcdvq9),
            iS1JnZov.optionalFieldOf("Mount", new double[]{1.176325, 1.658017, 0.0})
               .forGetter(var0x -> new double[]{var0x.c1jHYPb4(), var0x.v8ZN(), var0x.zTT3nw6()}),
            Codec.STRING.listOf().optionalFieldOf("AllowedDrones", List.of("fp1")).forGetter(RampDefinition::vmpmU),
            InteractZone.ibzfxic.listOf().optionalFieldOf("InteractZones", List.of()).forGetter(RampDefinition::b69s),
            Codec.STRING.optionalFieldOf("Item", "").forGetter(RampDefinition::cXHvq)
         )
         .apply(
            var0,
            (var0x, var1, var2, var3, var4, var5, var6, var7, var8) -> new RampDefinition(
               "", var0x, var1, var2, var3, var4, var5[0], var5[1], var5[2], var6, var7, var8
            )
         )
   );

   public RampDefinition(
      String var1,
      String var2,
      String var3,
      String var4,
      AttachTransform var5,
      Map<String, AttachTransform> var6,
      double var7,
      double var9,
      double var11,
      List<String> var13,
      List<InteractZone> var14,
      String var15
   ) {
      this.bAcdvq9 = var1;
      this.c1jHYPb4 = var2;
      this.v8ZN = var3;
      this.zTT3nw6 = var4;
      this.vmpmU = var5;
      this.b69s = var6;
      this.cXHvq = var7;
      this.rxYnaa = var9;
      this.r6s7FL3 = var11;
      this.iAKNPAj = var13;
      this.qlv6NKWnH = var14;
      this.hOKMYHOaC = var15;
   }

   public boolean v5suI(String var1) {
      if (var1 == null || var1.isEmpty()) {
         return false;
      } else {
         return !this.iAKNPAj.isEmpty() && !this.iAKNPAj.contains("*") ? this.iAKNPAj.contains(var1) : true;
      }
   }

   public AttachTransform ibzfxic(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         AttachTransform var2 = this.b69s.get(var1);
         if (var2 != null) {
            return var2;
         }
      }

      return this.vmpmU;
   }

   public String v5suI() {
      return DroneDefinition.ibzfxic(this.v8ZN);
   }

   public RampDefinition wuhB(String var1) {
      return new RampDefinition(
         var1,
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
         this.hOKMYHOaC
      );
   }

   public List<InteractZone> ibzfxic() {
      LinkedHashMap var1 = new LinkedHashMap();
      var1.put("ramp", new InteractZone("ramp", "ramp", InteractZone.Trigger.v5suI, "rampService", null));

      for (InteractZone var3 : this.qlv6NKWnH) {
         var1.put(var3.v5suI(), var3);
      }

      return List.copyOf(var1.values());
   }

   public String wuhB() {
      return this.hOKMYHOaC != null && !this.hOKMYHOaC.isEmpty() ? this.hOKMYHOaC : "fpvdrone:" + this.bAcdvq9;
   }

   public String dOfFwvo() {
      return this.bAcdvq9;
   }

   public String rsDd50tfV() {
      return this.c1jHYPb4;
   }

   public String uSUb() {
      return this.v8ZN;
   }

   public String jGhI2() {
      return this.zTT3nw6;
   }

   public AttachTransform wEJx3Itb() {
      return this.vmpmU;
   }

   public Map<String, AttachTransform> bAcdvq9() {
      return this.b69s;
   }

   public double c1jHYPb4() {
      return this.cXHvq;
   }

   public double v8ZN() {
      return this.rxYnaa;
   }

   public double zTT3nw6() {
      return this.r6s7FL3;
   }

   public List<String> vmpmU() {
      return this.iAKNPAj;
   }

   public List<InteractZone> b69s() {
      return this.qlv6NKWnH;
   }

   public String cXHvq() {
      return this.hOKMYHOaC;
   }
}
