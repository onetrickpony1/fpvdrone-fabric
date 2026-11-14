package com.fpvdrone.client.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ReconPixelPainter {
   private static final int v5suI = 24;
   private static final ThreadLocal<FlatTintGetter> ibzfxic = ThreadLocal.withInitial(FlatTintGetter::new);
   private static final ThreadLocal<MutableBlockPos> wuhB = ThreadLocal.withInitial(MutableBlockPos::new);

   private ReconPixelPainter() {
   }

   public static void v5suI(ReconStore.Rdl0X5jeod var0) {
      if (var0 != null && var0.rsDd50tfV && !var0.v5suI()) {
         int var1 = Integer.MIN_VALUE;

         for (int var2 = 0; var2 < 256; var2++) {
            int var3 = var0.wuhB[var2];
            if (var3 != var1) {
               var1 = var3;
               BlockMapColors.v5suI(Block.stateById(var3));
            }
         }
      }
   }

   public static void v5suI(ReconStore.Rdl0X5jeod var0, int var1, int var2, int[] var3) {
      Registry var4 = v5suI();
      int var5 = var1 << 4;
      int var6 = var2 << 4;
      FlatTintGetter var7 = var4 == null ? null : ibzfxic.get().v5suI(var4, var0.dOfFwvo, var5, var6);
      MutableBlockPos var8 = wuhB.get();

      for (int var9 = 0; var9 < 256; var9++) {
         BlockState var10 = Block.stateById(var0.wuhB[var9]);
         var8.set(var5 + (var9 & 15), var0.ibzfxic[var9], var6 + (var9 >> 4));
         int var11 = BlockMapColors.v5suI(var10, var7, var8);
         if ((var11 >>> 24 & 0xFF) < 24) {
            var11 = BlockMapColors.ibzfxic(var10, null, null);
         }

         if ((var11 >>> 24 & 0xFF) < 24) {
            var11 = -16777216;
         }

         var3[var9] = var11;
      }
   }

   private static Registry<Biome> v5suI() {
      ClientLevel var0 = Minecraft.getInstance().level;
      return var0 == null ? null : var0.registryAccess().lookupOrThrow(Registries.BIOME);
   }
}
