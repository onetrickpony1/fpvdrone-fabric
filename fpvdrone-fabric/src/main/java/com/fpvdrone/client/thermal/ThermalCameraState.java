package com.fpvdrone.client.thermal;

/**
 * 1.1.8 (O0UbPo3YWm): whether the thermal camera view is on, its palette, and whether its depth-of-field
 * blur is on (on by default). The picture itself is made by {@link ThermalRenderHook}.
 */
public class ThermalCameraState {
   private static final ThermalCameraState INSTANCE = new ThermalCameraState();
   private boolean enabled = false;
   private PaletteMode palette = PaletteMode.WHITE_HOT;
   private boolean depthOfField = true;

   ThermalCameraState() {
   }

   public static ThermalCameraState getInstance() {
      return INSTANCE;
   }

   public void toggle() {
      this.enabled = !this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void disable() {
      this.enabled = false;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public PaletteMode getPalette() {
      return this.palette;
   }

   /** Port addition (1.1.8 has no way to change the palette in game; used by the in-game test). */
   public void setPalette(PaletteMode palette) {
      this.palette = palette;
   }

   public void nextPalette() {
      this.palette = this.palette.next();
   }

   public boolean isDepthOfField() {
      return this.depthOfField;
   }

   public void setDepthOfField(boolean on) {
      this.depthOfField = on;
   }

   public void toggleDepthOfField() {
      this.depthOfField = !this.depthOfField;
   }
}
