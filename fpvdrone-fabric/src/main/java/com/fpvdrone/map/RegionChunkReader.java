package com.fpvdrone.map;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class RegionChunkReader {
   /** 26.3: block and biome codecs come from a PalettedContainerFactory, rebuilt when the biome registry changes. */
   private static Registry<Biome> ibzfxic;
   private static PalettedContainerFactory wuhB;
   private final PalettedContainer<BlockState>[] dOfFwvo;
   private final PalettedContainerRO<Holder<Biome>>[] rsDd50tfV;
   private final Holder<Biome> uSUb;
   private final int jGhI2;
   private final int wEJx3Itb;
   private final int bAcdvq9;

   private RegionChunkReader(PalettedContainer<BlockState>[] var1, PalettedContainerRO<Holder<Biome>>[] var2, Holder<Biome> var3, int var4, int var5, int var6) {
      this.dOfFwvo = var1;
      this.rsDd50tfV = var2;
      this.uSUb = var3;
      this.jGhI2 = var4;
      this.wEJx3Itb = var5;
      this.bAcdvq9 = var6;
   }

   @Nullable
   public static RegionChunkReader v5suI(ServerLevel var0, CompoundTag var1) {
      ChunkStatus var2 = var1.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
      if (var2 != ChunkStatus.FULL) {
         return null;
      }

      ListTag var3 = var1.getListOrEmpty("sections");
      if (var3.isEmpty()) {
         return null;
      }

      Registry var4 = var0.registryAccess().lookupOrThrow(Registries.BIOME);
      PalettedContainerFactory var5 = v5suI(var0, var4);
      int var6 = var0.getSectionsCount();
      int var7 = var0.getMinSectionY();
      PalettedContainer[] var8 = new PalettedContainer[var6];
      PalettedContainerRO[] var9 = new PalettedContainerRO[var6];

      for (int var10 = 0; var10 < var3.size(); var10++) {
         CompoundTag var11 = var3.getCompoundOrEmpty(var10);
         int var12 = var11.getByteOr("Y", (byte)0) - var7;
         if (var12 >= 0 && var12 < var6) {
            if (var11.getCompound("block_states").isPresent()) {
               var8[var12] = (PalettedContainer)var5.blockStatesContainerCodec().parse(NbtOps.INSTANCE, var11.getCompoundOrEmpty("block_states")).result().orElse(null);
            }

            if (var11.getCompound("biomes").isPresent()) {
               var9[var12] = (PalettedContainerRO)var5.biomeContainerCodec().parse(NbtOps.INSTANCE, var11.getCompoundOrEmpty("biomes")).result().orElse(null);
            }
         }
      }

      return new RegionChunkReader(var8, var9, var5.defaultBiome(), var7, var0.getMinY(), var0.getMinY() + var0.getHeight());
   }

   private static PalettedContainerFactory v5suI(ServerLevel var1, Registry<Biome> var0) {
      if (var0 != ibzfxic || wuhB == null) {
         ibzfxic = var0;
         wuhB = PalettedContainerFactory.create(var1.registryAccess());
      }

      return wuhB;
   }

   public BlockState v5suI(int var1, int var2, int var3) {
      int var4 = (var2 >> 4) - this.jGhI2;
      return var4 >= 0 && var4 < this.dOfFwvo.length && this.dOfFwvo[var4] != null
         ? (BlockState)this.dOfFwvo[var4].get(var1 & 15, var2 & 15, var3 & 15)
         : Blocks.AIR.defaultBlockState();
   }

   public Holder<Biome> ibzfxic(int var1, int var2, int var3) {
      int var4 = QuartPos.fromBlock(this.wEJx3Itb);
      int var5 = var4 + QuartPos.fromBlock(this.bAcdvq9 - this.wEJx3Itb) - 1;
      int var6 = Mth.clamp(var2, var4, var5);
      int var7 = (QuartPos.toBlock(var6) >> 4) - this.jGhI2;
      return var7 >= 0 && var7 < this.rsDd50tfV.length && this.rsDd50tfV[var7] != null
         ? (Holder)this.rsDd50tfV[var7].get(var1 & 3, var6 & 3, var3 & 3)
         : this.uSUb;
   }

   public int v5suI() {
      return this.wEJx3Itb;
   }

   public int ibzfxic() {
      return this.bAcdvq9;
   }
}
