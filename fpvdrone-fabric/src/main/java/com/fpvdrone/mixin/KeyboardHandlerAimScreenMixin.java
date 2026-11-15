package com.fpvdrone.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 1.1.8 KeyboardHandlerMixin: with the Lancet aim screen open, the game-input keys (F3 debug combinations and the
 * like) still work, as if no screen were open. 26.3: the "handlesGameInput = screen() == null" read in keyPress.
 */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerAimScreenMixin {
   @Redirect(
      method = "keyPress",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;", ordinal = 2),
      require = 0
   )
   private Screen fpvdrone$aimScreenCountsAsNoScreen(Gui gui) {
      Screen screen = gui.screen();
      return screen instanceof com.fpvdrone.gui.screen.AimScreen ? null : screen;
   }
}
