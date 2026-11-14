package com.fpvdrone.client.thermal;

/**
 * 1.1.8 data.Record_IHdt50hovE: the thermal look of one block, written into the thermal atlas.
 *
 * @param baseTemp     temperature 0..1 (atlas red)
 * @param emissivity   0..1; low values reflect the surroundings (metal, glass) (atlas green)
 * @param materialType one of the MATERIAL_* values (atlas blue)
 * @param thermalMass  0..1 (atlas alpha; 0 marks an empty texel)
 */
public record ThermalProperties(float baseTemp, float emissivity, float materialType, float thermalMass) {
   public static final float MATERIAL_NORMAL = 0.0F;
   public static final float MATERIAL_GLASS = 0.25F;
   public static final float MATERIAL_METAL = 0.5F;
   public static final float MATERIAL_WATER = 0.75F;
   public static final float MATERIAL_FIRE = 1.0F;

   /** RGBA bytes packed as NativeImage ABGR (red in the low byte). */
   int encodeABGR() {
      return encodeABGR(this.baseTemp);
   }

   int encodeABGR(float temperature) {
      int r = Math.round(Math.max(0.0F, Math.min(1.0F, temperature)) * 255.0F);
      int g = Math.round(this.emissivity * 255.0F);
      int b = Math.round(this.materialType * 255.0F);
      int a = Math.round(this.thermalMass * 255.0F);
      return a << 24 | b << 16 | g << 8 | r;
   }
}
