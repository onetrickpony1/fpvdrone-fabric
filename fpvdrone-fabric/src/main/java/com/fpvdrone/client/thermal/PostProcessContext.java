package com.fpvdrone.client.thermal;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import org.joml.Matrix4f;

/** 1.1.8 PostProcessContext: per-frame values shared by the thermal passes. */
public class PostProcessContext {
   private float agcMin;
   private float agcMax = 1.0F;
   private float ambientTemp;
   private float atmosphericAbsorption = 0.003F;
   private float weatherMod = 1.0F;
   private int frameCount;
   private int width;
   private int height;
   private PaletteMode palette = PaletteMode.WHITE_HOT;
   private float fpnReduction;
   private float focusDistance = 20.0F;
   // Port additions: what 1.1.8 read straight from GL (the scene depth and how to turn it into distances).
   private GpuTextureView depth;
   private final Matrix4f invProjection = new Matrix4f();
   private boolean useDepth = true;

   public float getAgcMin() {
      return this.agcMin;
   }

   public void setAgcMin(float v) {
      this.agcMin = v;
   }

   public float getAgcMax() {
      return this.agcMax;
   }

   public void setAgcMax(float v) {
      this.agcMax = v;
   }

   public float getAmbientTemp() {
      return this.ambientTemp;
   }

   public void setAmbientTemp(float v) {
      this.ambientTemp = v;
   }

   public float getAtmosphericAbsorption() {
      return this.atmosphericAbsorption;
   }

   public void setAtmosphericAbsorption(float v) {
      this.atmosphericAbsorption = v;
   }

   public float getWeatherMod() {
      return this.weatherMod;
   }

   public void setWeatherMod(float v) {
      this.weatherMod = v;
   }

   public int getFrameCount() {
      return this.frameCount;
   }

   public void setFrameCount(int v) {
      this.frameCount = v;
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int v) {
      this.width = v;
   }

   public int getHeight() {
      return this.height;
   }

   public void setHeight(int v) {
      this.height = v;
   }

   public PaletteMode getPalette() {
      return this.palette;
   }

   public void setPalette(PaletteMode palette) {
      this.palette = palette;
   }

   public float getFpnReduction() {
      return this.fpnReduction;
   }

   public void setFpnReduction(float v) {
      this.fpnReduction = v;
   }

   public float getFocusDistance() {
      return this.focusDistance;
   }

   public void setFocusDistance(float v) {
      this.focusDistance = v;
   }

   public GpuTextureView getDepth() {
      return this.depth;
   }

   public void setDepth(GpuTextureView depth) {
      this.depth = depth;
   }

   public Matrix4f getInvProjection() {
      return this.invProjection;
   }

   public boolean isUseDepth() {
      return this.useDepth && this.depth != null;
   }

   public void setUseDepth(boolean useDepth) {
      this.useDepth = useDepth;
   }
}
