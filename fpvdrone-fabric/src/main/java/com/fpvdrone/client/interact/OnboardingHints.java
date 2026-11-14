package com.fpvdrone.client.interact;

import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.DroneAction;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.DjiControllerItem;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.item.RemoteControlItem;
import com.fpvdrone.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 1.1.8 (NmTu0N1om): first-steps hints for the aircraft the player looks at ("Attach a battery and link a remote +
 * goggles to fly", "Put on the linked goggles to fly", payload tips, ramp hints), with item icons.
 * <p>
 * 1.1.8 ships them switched off: the class has a constant {@code false} switch and its frame and HUD handlers are empty
 * (the compiler removed the code behind the switch), so no hint ever shows. This port keeps that: {@link #ENABLED} is
 * false and nothing is registered; what survived (choosing the hint, its text, drawing one row) is ported so the hints
 * can be wired up if a later version turns them on.
 */
public final class OnboardingHints {
   /** 1.1.8 NmTu0N1om.v5suI: false, so the hints never show. */
   public static final boolean ENABLED = false;
   static final int ROW_HEIGHT = 13;
   static final int PADDING = 4;
   static final int ICON = 16;
   static final int ICON_GAP = 2;
   static final int ORANGE = -27594;
   static final int GREY = -1865626420;

   private OnboardingHints() {
   }

   /** 1.1.8 NmTu0N1om.KKeMvpuBgEY. */
   public enum Kind {
      NONE,
      CONNECT,
      SETUP_CONTROLLER,
      WEAR_GOGGLES,
      PAYLOAD_TIP_BOMBBAY,
      PAYLOAD_TIP_PROJECTILE,
      RAMP_NEEDS_DRONE,
      RAMP_NEEDS_FUEL,
      RAMP_NEEDS_ROUTE
   }

   /** 1.1.8 NmTu0N1om.Ol3TEDSog: the hint, its item icons, and the item name some hints quote. */
   public record Hint(Kind kind, List<ItemStack> icons, @Nullable Component itemName) {
      public static final Hint NONE = new Hint(Kind.NONE, List.of(), null);

      Hint(Kind kind, List<ItemStack> icons) {
         this(kind, icons, null);
      }
   }

   /** 1.1.8 v5suI(Player, DroneEntity): the next thing the player needs to do to fly this drone. */
   public static Hint hintFor(Player player, DroneEntity drone) {
      boolean gps = drone.xogV5KBhg();
      UUID droneId = drone.getUUID();
      boolean hasBattery = drone.idXga();
      boolean linkedController = gps
         ? carriesLinked(player, DjiControllerItem.class, DjiControllerItem::getLinkedDroneId, droneId)
         : carriesLinked(player, RemoteControlItem.class, RemoteControlItem::getLinkedDroneId, droneId);
      boolean linkedGoggles = gps || carriesLinked(player, FpvGogglesItem.class, FpvGogglesItem::getLinkedDroneId, droneId);
      if (hasBattery && linkedController && linkedGoggles) {
         boolean controllerNotSetUp = ControllerReader.getInstance().isPhysicalControllerConnected()
            && DroneAction.ARM.getChannel(ControllerReader.getConfig()) < 0;
         if (controllerNotSetUp) {
            return new Hint(Kind.SETUP_CONTROLLER, List.of());
         }

         if (drone.mM7DsB9AR() && !drone.xYttSy()) {
            Item bomb = ModItems.projectileItemFor(drone.oEuCBB().uKpZYj().projectile());
            if (bomb != null) {
               ItemStack icon = new ItemStack(bomb);
               return new Hint(Kind.PAYLOAD_TIP_BOMBBAY, List.of(icon), icon.getHoverName());
            }
         }

         if (!drone.oEuCBB().wBXn().isEmpty() && drone.i263D9().isEmpty()) {
            Item payload = ModItems.projectileItemFor(drone.oEuCBB().wBXn().get(0).v5suI());
            if (payload != null) {
               ItemStack icon = new ItemStack(payload);
               return new Hint(Kind.PAYLOAD_TIP_PROJECTILE, List.of(icon), icon.getHoverName());
            }
         }

         if (!gps && !(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof FpvGogglesItem)) {
            return new Hint(Kind.WEAR_GOGGLES, List.of(new ItemStack(ModItems.FPV_GOGGLES.get())));
         }

         return Hint.NONE;
      }

      List<ItemStack> missing = new ArrayList<>(3);
      if (!hasBattery) {
         missing.add(new ItemStack(gps ? ModItems.BATTERY_DJI.get() : ModItems.BATTERY.get()));
      }

      if (!linkedController) {
         missing.add(new ItemStack(gps ? ModItems.DJI_CONTROLLER.get() : ModItems.REMOTE_CONTROL.get()));
      }

      if (!linkedGoggles) {
         missing.add(new ItemStack(ModItems.FPV_GOGGLES.get()));
      }

      return new Hint(Kind.CONNECT, missing);
   }

   // 1.1.8-PENDING (munition slice): hintFor(LoiteringMunitionEntity m): !m.eJDY() -> NONE;
   //   m.sTrE() && !m.iElAbEZe() -> RAMP_NEEDS_FUEL [fuel can]; !m.rsDd50tfV() -> RAMP_NEEDS_ROUTE [BetaFlight]; else NONE.
   // hintFor(RampEntity r): !r.rsDd50tfV() -> RAMP_NEEDS_DRONE; r.vmpmU() && r.v8ZN() <= 0.001 -> RAMP_NEEDS_FUEL [fuel can];
   //   r.wuhB() == null -> RAMP_NEEDS_ROUTE [BetaFlight]; else NONE.

   private static boolean carriesLinked(Player player, Class<? extends Item> type, Function<ItemStack, Optional<UUID>> linkOf, UUID droneId) {
      Inventory inventory = player.getInventory();
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack stack = inventory.getItem(i);
         if (!stack.isEmpty() && type.isInstance(stack.getItem()) && linkOf.apply(stack).map(droneId::equals).orElse(false)) {
            return true;
         }
      }

      return false;
   }

   /** 1.1.8 v5suI(KKeMvpuBgEY, boolean, Component): the hint's text. */
   public static Component message(Kind kind, boolean quad, @Nullable Component itemName) {
      return switch (kind) {
         case CONNECT -> Component.translatable(quad ? "fpvdrone.onboarding.connect_quad" : "fpvdrone.onboarding.connect_fpv");
         case SETUP_CONTROLLER -> Component.translatable("fpvdrone.onboarding.setup_controller");
         case WEAR_GOGGLES -> Component.translatable("fpvdrone.onboarding.wear_goggles");
         case PAYLOAD_TIP_BOMBBAY -> Component.translatable("fpvdrone.onboarding.payload_tip_bombbay", itemName);
         case PAYLOAD_TIP_PROJECTILE -> Component.translatable("fpvdrone.onboarding.payload_tip_projectile", itemName);
         case RAMP_NEEDS_DRONE -> Component.translatable("fpvdrone.onboarding.ramp_needs_drone");
         case RAMP_NEEDS_FUEL -> Component.translatable("fpvdrone.onboarding.ramp_needs_fuel");
         case RAMP_NEEDS_ROUTE -> Component.translatable("fpvdrone.onboarding.ramp_needs_route");
         case NONE -> Component.empty();
      };
   }

   /** 1.1.8 v5suI(GuiGraphics, Font, ...): one hint row (icons then text) on a faded background; returns its height. */
   static int drawRow(GuiGraphicsExtractor graphics, Font font, List<ItemStack> icons, Component text, int x, int y, int color, int background, int alpha) {
      if (color >>> 24 < 4) {
         return ROW_HEIGHT;
      }

      int iconsWidth = icons.isEmpty() ? 0 : icons.size() * ICON + (icons.size() - 1) * ICON_GAP + PADDING;
      int textWidth = font.width(text);
      int height = icons.isEmpty() ? 9 : ICON;
      int bg = withAlpha(background, alpha);
      if (bg >>> 24 > 0) {
         graphics.fill(x - PADDING, y - 2, x + iconsWidth + textWidth + PADDING, y + height + 1, bg);
      }

      // 1.1.8 tinted the icons with the row's alpha (setColor); 26.3's GUI items have no tint, so they are drawn as is.
      int ix = x;
      for (ItemStack icon : icons) {
         graphics.item(icon, ix, y);
         ix += ICON + ICON_GAP;
      }

      graphics.text(font, text, x + iconsWidth, y + (height - 9) / 2, color, true);
      return height;
   }

   private static int withAlpha(int argb, int alpha) {
      int a = (argb >>> 24) * alpha / 255;
      return a << 24 | argb & 0xFFFFFF;
   }
}
