package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import com.fpvdrone.physics.PhysicsState;
import com.fpvdrone.physics.PhysicsUnits;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** 1.1.8 client-only helpers the common entity code calls (D0qGQZeK). */
public final class ClientHooks {
   /** Opens the route planner for a fixed-wing aircraft or a loaded ramp; false for anything else. */
   public static boolean v5suI(Entity var0) {
      // 1.1.8-PENDING: LoiteringMunitionEntity / RampEntity open the tactical map (not ported yet).
      return false;
   }

   public static void v5suI() {
      Minecraft.getInstance().getSoundManager().stop(Identifier.fromNamespaceAndPath("fpvdrone", "fpv_connect"), SoundSource.PLAYERS);
   }

   public static boolean v5suI(UUID var0) {
      Minecraft var1 = Minecraft.getInstance();
      return var1.player != null && FPVMod.getActiveDroneId(var1.player.getUUID()).map(var1x -> var1x.equals(var0)).orElse(false);
   }

   public static Vec3 ibzfxic(UUID var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.player != null && FPVMod.getActiveDroneId(var1.player.getUUID()).map(var1x -> var1x.equals(var0)).orElse(false)) {
         PhysicsState var2 = FPVMod.getPhysicsState(var1.player.getUUID());
         if (var2 != null) {
            return PhysicsUnits.toMinecraftVelocity(var2.getVelocity());
         }
      }

      return null;
   }
}
