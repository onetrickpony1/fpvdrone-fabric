package com.fpvdrone.interact;

import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.data.PayloadStation;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.registry.ModItems;
import java.util.List;
import org.jspecify.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * 1.1.8 (EIXvKj), content "mountPointLoadout": one interact zone per payload station (the zone's Bone names the
 * station). The title shows what is loaded ("Empty" otherwise); the option loads the station's projectile from the
 * player's inventory or unloads it. Works on drones with a bomb bay, loitering munitions and ramps with a rocket pod.
 */
public final class MountPointLoadout implements InteractContent, InteractExecutor {
   private static final String LOAD = "load";
   private static final String UNLOAD = "unload";

   @Override
   public Component title(Entity entity, InteractZone zone, Player player) {
      Stations stations = stationsOf(entity);
      if (stations == null) {
         return Component.empty();
      }

      int index = stations.indexOf(zone.ibzfxic());
      if (index < 0) {
         return Component.empty();
      }

      if (!stations.isLoaded(index)) {
         return Component.translatable("message.fpvdrone.interact.empty");
      }

      ProjectileDefinition projectile = ProjectileDefinitions.find(stations.projectileId(index)).orElse(null);
      return projectile != null ? Component.translatable(projectile.displayName()) : Component.translatable("message.fpvdrone.interact.loaded_unknown");
   }

   @Override
   public List<InteractOption> options(Entity entity, InteractZone zone, Player player) {
      Stations stations = stationsOf(entity);
      if (stations == null) {
         return List.of();
      }

      int index = stations.indexOf(zone.ibzfxic());
      if (index < 0) {
         return List.of();
      }

      if (stations.isLoaded(index)) {
         return List.of(new InteractOption(UNLOAD, Component.translatable("message.fpvdrone.interact.unload")));
      }

      Item item = ModItems.projectileItemFor(stations.projectileId(index));
      boolean available = item != null && ModItems.findInInventory(player, item) != null;
      return List.of(new InteractOption(LOAD, Component.translatable("message.fpvdrone.interact.load"), available));
   }

   @Override
   public boolean execute(ServerPlayer player, Entity entity, InteractZone zone, String optionId) {
      Stations stations = stationsOf(entity);
      if (stations == null) {
         return false;
      }

      int index = stations.indexOf(zone.ibzfxic());
      if (index < 0) {
         return false;
      }

      return switch (optionId) {
         case LOAD -> !stations.isLoaded(index) && stations.load(index, player);
         case UNLOAD -> stations.isLoaded(index) && stations.unload(index, player);
         default -> false;
      };
   }

   private static @Nullable Stations stationsOf(Entity entity) {
      // 1.1.8-PENDING (munition slice): LoiteringMunitionEntity and RampEntity adapters. 1.1.8:
      //   LoiteringMunitionEntity m -> stations: m.uQdlyiwB() (its definition) .wEJx3Itb() ? .jt9oI60ST().rsDd50tfV() : [];
      //     projectile: station.v5suI(def.jt9oI60ST().dOfFwvo()); isLoaded: m.ibzfxic(i); load: m.ibzfxic(i, player);
      //     unload: m.v5suI(i, player), true.
      //   RampEntity r (definition DroneDefinitions.ibzfxic(r.dOfFwvo())) -> same stations and projectile;
      //     isLoaded: r.ibzfxic(i); load: r.v5suI(i, player); unload: r.ibzfxic(i, player), true.
      //   They come first, before the DroneEntity check below.
      return entity instanceof DroneEntity drone ? droneStations(drone) : null;
   }

   private static Stations droneStations(DroneEntity drone) {
      return new Stations() {
         @Override
         List<PayloadStation> stations() {
            DroneDefinition definition = drone.oEuCBB();
            return definition.jGhI2() ? definition.uKpZYj().stations() : List.of();
         }

         @Override
         String projectileId(int index) {
            return this.stations().get(index).v5suI(drone.oEuCBB().uKpZYj().projectile());
         }

         @Override
         boolean isLoaded(int index) {
            return drone.ibzfxic(index);
         }

         @Override
         boolean load(int index, ServerPlayer player) {
            if (drone.ibzfxic(index)) {
               return false;
            }

            drone.v5suI(index, player);
            return drone.ibzfxic(index);
         }

         @Override
         boolean unload(int index, ServerPlayer player) {
            drone.wuhB(index);
            return true;
         }
      };
   }

   /** 1.1.8 EIXvKj.KKeMvpuBgEY: the payload stations of one carrier. */
   abstract static class Stations {
      abstract List<PayloadStation> stations();

      /** Station index by its id (the interact zone's Bone), or -1. */
      int indexOf(String stationId) {
         List<PayloadStation> list = this.stations();
         for (int i = 0; i < list.size(); i++) {
            if (list.get(i).ibzfxic().equals(stationId)) {
               return i;
            }
         }

         return -1;
      }

      /** The projectile id this station takes (its own or the carrier's default). */
      abstract String projectileId(int index);

      abstract boolean isLoaded(int index);

      abstract boolean load(int index, ServerPlayer player);

      abstract boolean unload(int index, ServerPlayer player);
   }
}
