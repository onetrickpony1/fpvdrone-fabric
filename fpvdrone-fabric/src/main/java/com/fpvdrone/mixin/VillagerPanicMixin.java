package com.fpvdrone.mixin;

import com.fpvdrone.client.VillagerPanicHandler;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Start of every villager tick: let villagers panic near drones (replaces Forge's LivingTickEvent hook). */
@Mixin(Villager.class)
public abstract class VillagerPanicMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void fpvdrone$panicNearDrones(CallbackInfo ci) {
        VillagerPanicHandler.onVillagerTick((Villager)(Object)this);
    }
}
