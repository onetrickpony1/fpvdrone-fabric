package com.fpvdrone.interact;

import com.fpvdrone.data.AttachableEntry;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.entity.DroneEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 1.1.8 (MQyrDg), content "droneAttachService" (the quads and the Mavic): attach / detach the battery, link / unlink the
 * remote and the goggles (not on a GPS drone), fit / take off the hanging projectile, load / unload the bomb bay one
 * station at a time, and pick the drone up (disarmed, not flown, not crashed or burning).
 * <p>
 * The DroneEntity methods keep their decompiled names; the item finders take a {@link Player} and the actions a
 * {@link ServerPlayer}, hence the casts.
 */
public final class DroneAttachService implements InteractContent, InteractExecutor {
   private static final String ATTACH_BATTERY = "attachBattery";
   private static final String DETACH_BATTERY = "detachBattery";
   private static final String ATTACH_REMOTE = "attachRemote";
   private static final String DETACH_REMOTE = "detachRemote";
   private static final String ATTACH_GOGGLES = "attachGoggles";
   private static final String DETACH_GOGGLES = "detachGoggles";
   private static final String ATTACH_PROJECTILE = "attachProjectile";
   private static final String DETACH_PROJECTILE = "detachProjectile";
   private static final String LOAD_BOMB = "loadBomb";
   private static final String UNLOAD_BOMB = "unloadBomb";
   private static final String PICKUP_DRONE = "pickupDrone";

   @Override
   public Component title(Entity entity, InteractZone zone, Player player) {
      return Component.empty();
   }

   @Override
   public List<InteractOption> options(Entity entity, InteractZone zone, Player player) {
      if (!(entity instanceof DroneEntity drone) || player == null) {
         return List.of();
      }

      List<InteractOption> out = new ArrayList<>(6);
      if (drone.idXga()) {
         out.add(new InteractOption(DETACH_BATTERY, Component.translatable("message.fpvdrone.interact.detach_battery")));
      } else {
         out.add(new InteractOption(ATTACH_BATTERY, Component.translatable("message.fpvdrone.interact.attach_battery"), drone.ibzfxic(player)));
      }

      if (drone.vmpmU(player)) {
         out.add(new InteractOption(DETACH_REMOTE, Component.translatable("message.fpvdrone.interact.detach_remote")));
      } else {
         out.add(new InteractOption(ATTACH_REMOTE, Component.translatable("message.fpvdrone.interact.attach_remote"), drone.bAcdvq9(player)));
      }

      if (!drone.xogV5KBhg()) {
         if (drone.cXHvq(player)) {
            out.add(new InteractOption(DETACH_GOGGLES, Component.translatable("message.fpvdrone.interact.detach_goggles")));
         } else {
            out.add(new InteractOption(ATTACH_GOGGLES, Component.translatable("message.fpvdrone.interact.attach_goggles"), drone.v8ZN(player)));
         }
      }

      if (drone.iKXOcc()) {
         out.add(new InteractOption(DETACH_PROJECTILE, Component.translatable("message.fpvdrone.interact.detach_projectile")));
      } else if (!drone.oEuCBB().wBXn().isEmpty()) {
         AttachableEntry held = drone.wuhB(player);
         boolean available = held != null;
         AttachableEntry shown = held != null ? held : drone.oEuCBB().wBXn().get(0);
         out.add(
            new InteractOption(
               ATTACH_PROJECTILE,
               Component.translatable("message.fpvdrone.interact.attach_projectile_named", Component.translatable(shown.ibzfxic())),
               available
            )
         );
      }

      if (drone.mM7DsB9AR()) {
         int loaded = drone.kNoG();
         int stations = drone.uKpZYj();
         if (loaded < stations) {
            out.add(
               new InteractOption(
                  LOAD_BOMB,
                  Component.translatable("message.fpvdrone.interact.attach_projectile_named_count", drone.i7EP1lKl(), loaded, stations),
                  drone.jGhI2(player)
               )
            );
         }

         if (loaded > 0) {
            out.add(
               new InteractOption(
                  UNLOAD_BOMB, Component.translatable("message.fpvdrone.interact.detach_projectile_named_count", drone.i7EP1lKl(), loaded, stations)
               )
            );
         }
      }

      boolean canPickUp = !drone.areMotorsArmed() && !drone.isBeingControlled() && !drone.dOfFwvo() && !drone.rsDd50tfV();
      out.add(new InteractOption(PICKUP_DRONE, Component.translatable("message.fpvdrone.interact.pickup_drone"), canPickUp));
      return out;
   }

   @Override
   public boolean execute(ServerPlayer player, Entity entity, InteractZone zone, String optionId) {
      if (!(entity instanceof DroneEntity drone)) {
         return false;
      }

      Player asPlayer = player;
      return switch (optionId) {
         case ATTACH_BATTERY -> {
            ItemStack stack = drone.v5suI(asPlayer);
            yield stack != null && drone.v5suI(player, stack);
         }
         case DETACH_BATTERY -> drone.v5suI(player);
         case ATTACH_REMOTE -> {
            ItemStack stack = drone.wEJx3Itb(asPlayer);
            yield stack != null && drone.dOfFwvo(player, stack);
         }
         case DETACH_REMOTE -> drone.dOfFwvo(player);
         case ATTACH_GOGGLES -> {
            ItemStack stack = drone.c1jHYPb4(asPlayer);
            yield stack != null && drone.rsDd50tfV(player, stack);
         }
         case DETACH_GOGGLES -> drone.rsDd50tfV(player);
         case ATTACH_PROJECTILE -> {
            ItemStack stack = drone.dOfFwvo(asPlayer);
            yield stack != null && drone.ibzfxic(player, stack);
         }
         case DETACH_PROJECTILE -> drone.ibzfxic(player);
         case LOAD_BOMB -> {
            ItemStack stack = drone.uSUb(asPlayer);
            yield stack != null && drone.wuhB(player, stack);
         }
         case UNLOAD_BOMB -> drone.wuhB(player);
         case PICKUP_DRONE -> drone.rxYnaa(player);
         default -> false;
      };
   }
}
