package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.PilotBodyEntity;
import com.fpvdrone.entity.PilotBodyManager;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 1.1.8: "/tp &lt;who&gt; &lt;pilot&gt;" to a player flying a drone lands at his pilot body, not at the drone. */
@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {
   @Redirect(method = "teleportToEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D"))
   private static double fpvdrone$destX(Entity destination) {
      return fpvdrone$resolveDestination(destination).x;
   }

   @Redirect(method = "teleportToEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getY()D"))
   private static double fpvdrone$destY(Entity destination) {
      return fpvdrone$resolveDestination(destination).y;
   }

   @Redirect(method = "teleportToEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D"))
   private static double fpvdrone$destZ(Entity destination) {
      return fpvdrone$resolveDestination(destination).z;
   }

   @Unique
   private static Vec3 fpvdrone$resolveDestination(Entity destination) {
      if (destination instanceof Player player && !player.level().isClientSide() && FPVMod.hasActiveDrone(player.getUUID())) {
         PilotBodyEntity body = PilotBodyManager.getPilotBody(player.getUUID());
         if (body != null && body.isAlive()) {
            return body.position();
         }
      }

      return destination.position();
   }
}
