package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MultiPlayerGameMode.class})
public class PlayerInteractionMixin {
    @Inject(
        method = {"attack"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockAttack(Player player, Entity target, CallbackInfo ci) {
        if (FPVMod.getActiveDroneId(player.getUUID()).isPresent()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"startDestroyBlock"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && FPVMod.getActiveDroneId(mc.player.getUUID()).isPresent()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = {"continueDestroyBlock"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockContinueBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && FPVMod.getActiveDroneId(mc.player.getUUID()).isPresent()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = {"useItemOn"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        if (FPVMod.getActiveDroneId(player.getUUID()).isPresent()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(
        method = {"interact"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockInteract(Player player, Entity target, net.minecraft.world.phys.EntityHitResult hit, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (FPVMod.getActiveDroneId(player.getUUID()).isPresent()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(
        method = {"useItem"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (FPVMod.getActiveDroneId(player.getUUID()).isPresent()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
