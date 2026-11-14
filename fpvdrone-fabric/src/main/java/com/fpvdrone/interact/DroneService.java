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

      // 1.1.8-PENDING (munition slice): if (entity instanceof RampEntity) add
      //   new InteractOption(REMOVE_FROM_RAMP, translatable("message.fpvdrone.interact.remove_from_ramp"), true, REMOVE_FROM_RAMP_CLICKS);
      // if (entity instanceof LoiteringMunitionEntity m && m.vmpmU()) (crashed) add
      //   new InteractOption(RETRIEVE_WRECK, translatable("message.fpvdrone.interact.pickup_drone"), true, RETRIEVE_WRECK_CLICKS).
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
         // 1.1.8-PENDING (munition slice): REMOVE_FROM_RAMP -> entity instanceof RampEntity r && r.uSUb(player);
         //   RETRIEVE_WRECK -> entity instanceof LoiteringMunitionEntity m && m.v5suI(player).
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
      // 1.1.8-PENDING (munition slice): adapters for LoiteringMunitionEntity and RampEntity (only a ramp with an aircraft
      // on it, r.rsDd50tfV()). 1.1.8 maps them as:
      //   munition m: podSlotFree = m.uKpZYj() && m.uQdlyiwB().jt9oI60ST().uSUb().isEmpty(); fuel = m.we1GYY();
      //     missingFuel = m.pxmzA(); usesFuel = m.sTrE(); canInstallPod = podSlotFree && !m.l2IWOAGO();
      //     canUninstallPod = podSlotFree && m.l2IWOAGO(); refuel = m.ibzfxic(p); drainFuel = m.wuhB(p);
      //     installPod = m.dOfFwvo(p); uninstallPod = m.rsDd50tfV(p).
      //   ramp r: podSlotFree = r.c1jHYPb4() && DroneDefinitions.ibzfxic(r.dOfFwvo()).jt9oI60ST().uSUb().isEmpty();
      //     fuel = r.v8ZN(); missingFuel = r.zTT3nw6(); usesFuel = r.vmpmU(); canInstallPod = podSlotFree && !r.wEJx3Itb();
      //     canUninstallPod = podSlotFree && r.wEJx3Itb(); refuel = r.dOfFwvo(p); drainFuel = r.rsDd50tfV(p);
      //     installPod = r.v5suI(p); uninstallPod = r.ibzfxic(p).
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
