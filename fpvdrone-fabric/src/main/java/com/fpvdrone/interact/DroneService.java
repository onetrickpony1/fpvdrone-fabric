package com.fpvdrone.interact;

import com.fpvdrone.client.ClientHooks;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.item.FuelCanItem;
import com.fpvdrone.item.RocketPodMountItem;
import com.fpvdrone.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 1.1.8 (IkSD3C6aW), content "droneService" (the fixed-wing aircraft and the ramp): refuel / drain fuel with a fuel can,
 * connect the laptop (the BetaFlight item, or the zone's RequiredItems "connectLaptop") to plan a route, install / take
 * back the rocket pod, take an aircraft off its ramp (8 clicks) and retrieve a crashed aircraft (8 clicks).
 * "Connect laptop" is handled on the client: it opens the tactical map.
 */
public final class DroneService implements InteractContent, ClientInteractHandler, InteractExecutor {
   private static final String REFUEL = "refuel";
   private static final String DRAIN_FUEL = "drainFuel";
   private static final String CONNECT_LAPTOP = "connectLaptop";
   private static final String INSTALL_POD = "installPod";
   private static final String UNINSTALL_POD = "uninstallPod";
   private static final String REMOVE_FROM_RAMP = "removeFromRamp";
   private static final String RETRIEVE_WRECK = "retrieveWreck";
   private static final int REMOVE_FROM_RAMP_CLICKS = 8;
   private static final int RETRIEVE_WRECK_CLICKS = 8;

   @Override
   public Component title(Entity entity, InteractZone zone, Player player) {
      return Component.empty();
   }

   @Override
   public List<InteractOption> options(Entity entity, InteractZone zone, Player player) {
      Serviced serviced = servicedOf(entity);
      if (serviced == null || player == null) {
         return List.of();
      }

      List<InteractOption> out = new ArrayList<>(5);
      if (serviced.usesFuel()) {
         if (serviced.missingFuel() > 0.001) {
            out.add(new InteractOption(REFUEL, Component.translatable("message.fpvdrone.interact.refuel"), hasFuelToGive(player)));
         }

         if (serviced.fuel() > 0.001) {
            out.add(
               new InteractOption(
                  DRAIN_FUEL, Component.translatable("message.fpvdrone.interact.drain_fuel", String.format("%.1f", serviced.fuel())), hasRoomForFuel(player)
               )
            );
         }
      }

      Item laptop = requiredItem(zone, CONNECT_LAPTOP, ModItems.BETAFLIGHT.get());
      out.add(new InteractOption(CONNECT_LAPTOP, Component.translatable("message.fpvdrone.interact.connect_laptop"), hasItem(player, laptop)));
      if (serviced.canInstallPod()) {
         out.add(new InteractOption(INSTALL_POD, Component.translatable("message.fpvdrone.interact.install_pod"), hasItem(player, RocketPodMountItem.class)));
      }

      if (serviced.canUninstallPod()) {
         out.add(new InteractOption(UNINSTALL_POD, Component.translatable("message.fpvdrone.interact.uninstall_pod")));
      }

      if (entity instanceof com.fpvdrone.entity.RampEntity) {
         out.add(new InteractOption(REMOVE_FROM_RAMP, Component.translatable("message.fpvdrone.interact.remove_from_ramp"), true, REMOVE_FROM_RAMP_CLICKS));
      }

      // 1.1.8: a crashed munition can be picked up
      if (entity instanceof com.fpvdrone.entity.LoiteringMunitionEntity munition && munition.vmpmU()) {
         out.add(new InteractOption(RETRIEVE_WRECK, Component.translatable("message.fpvdrone.interact.pickup_drone"), true, RETRIEVE_WRECK_CLICKS));
      }

      return out;
   }

   @Override
   public boolean execute(ServerPlayer player, Entity entity, InteractZone zone, String optionId) {
      Serviced serviced = servicedOf(entity);
      if (serviced == null) {
         return false;
      }

      return switch (optionId) {
         case REFUEL -> serviced.refuel(player);
         case DRAIN_FUEL -> serviced.drainFuel(player);
         case INSTALL_POD -> serviced.installPod(player);
         case UNINSTALL_POD -> serviced.uninstallPod(player);
         case REMOVE_FROM_RAMP -> entity instanceof com.fpvdrone.entity.RampEntity ramp && ramp.uSUb(player);
         case RETRIEVE_WRECK -> entity instanceof com.fpvdrone.entity.LoiteringMunitionEntity munition && munition.v5suI((Player)player);
         default -> false;
      };
   }

   /** "Connect laptop" opens the route planner (tactical map) on the client instead of going to the server. */
   @Override
   public boolean handleOnClient(Entity entity, InteractZone zone, String optionId) {
      return CONNECT_LAPTOP.equals(optionId) && ClientHooks.v5suI(entity);
   }

   private static boolean hasFuelToGive(Player player) {
      Inventory inventory = player.getInventory();
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack stack = inventory.getItem(i);
         if (stack.getItem() instanceof FuelCanItem && FuelCanItem.getFuel(stack) > 1.0E-6) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasRoomForFuel(Player player) {
      Inventory inventory = player.getInventory();
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack stack = inventory.getItem(i);
         if (stack.getItem() instanceof FuelCanItem && FuelCanItem.getFuel(stack) < 19.999) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasItem(Player player, Class<? extends Item> itemClass) {
      Inventory inventory = player.getInventory();
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         if (itemClass.isInstance(inventory.getItem(i).getItem())) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasItem(Player player, Item item) {
      Inventory inventory = player.getInventory();
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         if (inventory.getItem(i).is(item)) {
            return true;
         }
      }

      return false;
   }

   /** The zone's RequiredItems entry for an option (an item id), or the default item. */
   private static Item requiredItem(InteractZone zone, String optionId, Item fallback) {
      String id = zone.uSUb().get(optionId);
      if (id == null || id.isEmpty()) {
         return fallback;
      }

      Identifier key = Identifier.tryParse(id);
      if (key == null) {
         return fallback;
      }

      Item item = BuiltInRegistries.ITEM.getValue(key);
      return item != Items.AIR ? item : fallback;
   }

   private static @Nullable Serviced servicedOf(Entity entity) {
      // 1.1.8 IkSD3C6aW: adapters for a flying munition and for a ramp with an aircraft on it
      if (entity instanceof com.fpvdrone.entity.LoiteringMunitionEntity m) {
         boolean podSlotFree = m.uKpZYj() && m.uQdlyiwB().jt9oI60ST().uSUb().isEmpty();
         return new Serviced() {
            public double fuel() { return m.we1GYY(); }
            public double missingFuel() { return m.pxmzA(); }
            public boolean usesFuel() { return m.sTrE(); }
            public boolean canInstallPod() { return podSlotFree && !m.l2IWOAGO(); }
            public boolean canUninstallPod() { return podSlotFree && m.l2IWOAGO(); }
            public boolean refuel(Player p) { return m.ibzfxic(p); }
            public boolean drainFuel(Player p) { return m.wuhB(p); }
            public boolean installPod(Player p) { return m.dOfFwvo(p); }
            public boolean uninstallPod(Player p) { return m.rsDd50tfV(p); }
         };
      }

      if (entity instanceof com.fpvdrone.entity.RampEntity r && r.rsDd50tfV()) {
         boolean podSlotFree = r.c1jHYPb4() && com.fpvdrone.data.DroneDefinitions.ibzfxic(r.dOfFwvo()).jt9oI60ST().uSUb().isEmpty();
         return new Serviced() {
            public double fuel() { return r.v8ZN(); }
            public double missingFuel() { return r.zTT3nw6(); }
            public boolean usesFuel() { return r.vmpmU(); }
            public boolean canInstallPod() { return podSlotFree && !r.wEJx3Itb(); }
            public boolean canUninstallPod() { return podSlotFree && r.wEJx3Itb(); }
            public boolean refuel(Player p) { return r.dOfFwvo(p); }
            public boolean drainFuel(Player p) { return r.rsDd50tfV(p); }
            public boolean installPod(Player p) { return r.v5suI(p); }
            public boolean uninstallPod(Player p) { return r.ibzfxic(p); }
         };
      }

      return null;
   }

   /** 1.1.8 IkSD3C6aW.KKeMvpuBgEY: the fuel tank and rocket pod slot of a fixed-wing aircraft (flying or on a ramp). */
   interface Serviced {
      double fuel();

      double missingFuel();

      boolean usesFuel();

      boolean canInstallPod();

      boolean canUninstallPod();

      boolean refuel(Player player);

      boolean drainFuel(Player player);

      boolean installPod(Player player);

      boolean uninstallPod(Player player);
   }
}
