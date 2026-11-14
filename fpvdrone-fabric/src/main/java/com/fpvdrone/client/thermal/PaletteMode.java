package com.fpvdrone.client.thermal;

/** 1.1.8: thermal camera colour palettes (lang keys fpvdrone.thermal.palette.*), cycled in this order. */
public enum PaletteMode {
   WHITE_HOT,
   BLACK_HOT,
   IRONBOW;

   public PaletteMode next() {
      PaletteMode[] values = values();
      return values[(this.ordinal() + 1) % values.length];
   }
}
