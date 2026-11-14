package com.fpvdrone.mixin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 1.1.8 PhysicsState$EntityHitCallback (a misnamed accessor): reads a saved chunk's NBT, upgraded by the data fixer,
 * so the tactical map can draw chunks that are not loaded.
 */
@Mixin(ChunkMap.class)
public interface ChunkMapReadInvoker {
   @Invoker("readChunk")
   CompletableFuture<Optional<CompoundTag>> fpvdrone$readChunk(ChunkPos pos);
}
