package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;

/** 1.1.8 ThermalPass: one step of the thermal post-processing chain, reading {@code in} and writing {@code out}. */
public interface ThermalPass {
   String getName();

   void apply(RenderTarget in, RenderTarget out, PostProcessContext context);

   default void cleanup() {
   }
}
