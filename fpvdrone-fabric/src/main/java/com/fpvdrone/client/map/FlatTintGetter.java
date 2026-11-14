package com.fpvdrone.client.map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FlatTintGetter implements BlockAndTintGetter {
   private Registry<Biome> v5suI;
   private int[] ibzfxic;
   private int wuhB;
   private int dOfFwvo;

   public FlatTintGetter v5suI(Registry<Biome> var1, int[] var2, int var3, int var4) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
      this.dOfFwvo = var4;
      return this;
   }

   public int getBlockTint(BlockPos var1, ColorResolver var2) {
      Biome var3 = this.v5suI(var1.getX(), var1.getZ());
      return var3 == null ? -1 : var2.getColor(var3, var1.getX(), var1.getZ());
   }

   private Biome v5suI(int var1, int var2) {
      if (this.v5suI != null && this.ibzfxic != null) {
         int var3 = Mth.clamp(var1 - this.wuhB, 0, 15);
         int var4 = Mth.clamp(var2 - this.dOfFwvo, 0, 15);
         return (Biome)this.v5suI.byId(this.ibzfxic[var4 * 16 + var3]);
      } else {
         return null;
      }
   }

   /** 26.3: replaces getShade(Direction, boolean), which returned 1.0 for every face. */
   private static final CardinalLighting FLAT = new CardinalLighting(1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);

   public CardinalLighting cardinalLighting() {
      return FLAT;
   }

   public LevelLightEngine getLightEngine() {
      return LevelLightEngine.EMPTY;
   }

   public BlockEntity getBlockEntity(BlockPos var1) {
      return null;
   }

   public BlockState getBlockState(BlockPos var1) {
      return Blocks.AIR.defaultBlockState();
   }

   public FluidState getFluidState(BlockPos var1) {
      return Fluids.EMPTY.defaultFluidState();
   }

   public int getHeight() {
      return 16;
   }

   public int getMinY() {
      return 0;
   }
}
