package com.fpvdrone.client.interact;

import com.fpvdrone.FPVMod;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.data.PlacedInteractZone;
import com.fpvdrone.data.NearestBoxHit;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.interact.ClientInteractHandler;
import com.fpvdrone.interact.InteractContent;
import com.fpvdrone.interact.InteractContents;
import com.fpvdrone.interact.InteractOption;
import com.fpvdrone.network.InteractActionPacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.render.SmoothedValue;
import java.util.List;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 (MR7Qyh): the interact (hover) menu. Every frame, on foot and with no screen open, the player's look ray is
 * tested against the interact zones placed on the models of nearby aircraft (4 blocks reach). The zone looked at gives a
 * menu (title and options from its content); it fades in after two frames on the same zone and fades with distance
 * (full up to 2 blocks, gone at 4). The mouse wheel picks an option, the interact key (F by default, or its controller
 * button) runs it, and options with a click count need that many left clicks in a row instead.
 */
public final class InteractMenu {
   private static final double REACH = 4.0;
   private static final double SEARCH_INFLATE = 3.5;
   private static final long FADE_OUT_DELAY_NANOS = 150_000_000L;
   private static final float MAX_ALPHA = 0.85F;
   private static final double FULL_ALPHA_DISTANCE = 2.0;
   private static final int FRAMES_BEFORE_SHOWN = 2;
   private static @Nullable Menu current = null;
   private static @Nullable Menu shown = null;
   private static final SmoothedValue ALPHA = new SmoothedValue(0.0F, 8.0F, 0.0F, MAX_ALPHA);
   private static long lastSeenNanos = 0L;
   private static int lastEntityId = -1;
   private static @Nullable String lastZoneId = null;
   private static int framesOnZone = 0;
   private static int clickEntityId = -1;
   private static @Nullable String clickZoneId = null;
   private static @Nullable String clickOptionId = null;
   private static int clickCount = 0;

   private InteractMenu() {
   }

   /** The menu under the crosshair this frame (null when none). */
   public static @Nullable Menu current() {
      return current;
   }

   /** The menu being drawn (it lingers while fading out). */
   public static @Nullable Menu shown() {
      return shown;
   }

   /** The menu's alpha this frame; clears the shown menu once it has faded out. */
   public static float alpha() {
      float alpha = ALPHA.update();
      if (shown != null && ALPHA.target() <= 0.0F && alpha <= 0.01F) {
         shown = null;
         ALPHA.snapTo(0.0F);
         alpha = 0.0F;
      }

      return alpha;
   }

   private static float alphaForDistance(double distance) {
      if (distance <= FULL_ALPHA_DISTANCE) {
         return MAX_ALPHA;
      }

      double range = REACH - FULL_ALPHA_DISTANCE;
      double t = range > 0.0 ? (distance - FULL_ALPHA_DISTANCE) / range : 1.0;
      return MAX_ALPHA * (1.0F - (float)Mth.clamp(t, 0.0, 1.0));
   }

   /** Mouse wheel: moves the selection (only when there is more than one option). */
   public static void scroll(int step) {
      if (current != null && current.options().size() > 1) {
         int count = current.options().size();
         int selected = Mth.clamp(current.selected() + step, 0, count - 1);
         current = current.withSelected(selected);
      }
   }

   /** True when the wheel belongs to the menu (1.1.8 CameraManager checks this before the zoom). */
   public static boolean onMouseScroll(double delta) {
      if (current != null && current.options().size() > 1) {
         if (delta != 0.0) {
            scroll(delta > 0.0 ? -1 : 1);
         }

         return true;
      }

      return false;
   }

   /** The interact key: runs the selected option when it is available. */
   public static void activate() {
      Menu menu = current;
      if (menu == null || menu.options().isEmpty()) {
         return;
      }

      InteractOption option = menu.options().get(menu.selected());
      if (!option.available()) {
         return;
      }

      resetClicks();
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         mc.player.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
      }

      Entity entity = mc.level != null ? mc.level.getEntity(menu.entityId()) : null;
      InteractZone zone = entity != null ? InteractContents.zoneOf(entity, menu.zoneId()) : null;
      if (zone == null || !(InteractContents.get(zone.dOfFwvo()) instanceof ClientInteractHandler handler && handler.handleOnClient(entity, zone, option.id()))) {
         ModNetwork.CHANNEL.sendToServer(new InteractActionPacket(menu.entityId(), menu.zoneId(), option.id()));
      }
   }

   /**
    * Left click (1.1.8 InputEvent.MouseButton.Pre). Returns true when the click was taken by an option that needs
    * repeated clicks.
    */
   public static boolean onLeftClick() {
      if (current != null && !current.options().isEmpty()) {
         InteractOption option = current.options().get(current.selected());
         if (option.holdClicks() > 0) {
            countClick(option);
            return true;
         }
      }

      return false;
   }

   private static void countClick(InteractOption option) {
      Menu menu = current;
      if (menu == null) {
         return;
      }

      boolean same = menu.entityId() == clickEntityId && menu.zoneId().equals(clickZoneId) && option.id().equals(clickOptionId);
      clickCount = same ? clickCount + 1 : 1;
      clickEntityId = menu.entityId();
      clickZoneId = menu.zoneId();
      clickOptionId = option.id();
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         mc.player.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
      }

      if (mc.level != null) {
         mc.level.playLocalSound(menu.hit().x, menu.hit().y, menu.hit().z, SoundEvents.ARMOR_STAND_HIT, SoundSource.PLAYERS, 0.7F, 1.1F, false);
      }

      if (clickCount >= option.holdClicks()) {
         resetClicks();
         activate();
      }
   }

   private static void resetClicks() {
      clickEntityId = -1;
      clickZoneId = null;
      clickOptionId = null;
      clickCount = 0;
   }

   /** Once per frame, before the world renders (1.1.8 RenderTickEvent START, low priority). */
   public static void onFrame() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.player == null || mc.isPaused()) {
         clearAll();
         return;
      }

      // 1.1.8 FPVMod.isFlyingAnything: flying a drone or a loitering munition.
      if (mc.gui.screen() != null || FPVMod.isFlyingAnything(mc.player.getUUID())) {
         clearAll();
         return;
      }

      float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      Vec3 eye = mc.player.getEyePosition(partialTick);
      Vec3 end = eye.add(mc.player.getViewVector(partialTick).scale(REACH));
      AABB search = new AABB(eye, end).inflate(SEARCH_INFLATE);
      String zoneId = null;
      int entityId = -1;
      Vec3 hit = null;
      double bestSqr = Double.MAX_VALUE;

      for (Entity entity : mc.level.getEntities(mc.player, search, InteractContents::hasZones)) {
         List<PlacedInteractZone> placed = placedZones(entity, partialTick);
         if (!placed.isEmpty()) {
            // 1.1.8 Record_LjdslnKBxL (NearestBoxHit, shared with the main session's port): nearest zone the ray hits.
            NearestBoxHit.KKeMvpuBgEY pick = NearestBoxHit.v5suI(placed, eye, end);
            if (pick != null && pick.wuhB() < bestSqr) {
               bestSqr = pick.wuhB();
               zoneId = pick.v5suI();
               entityId = entity.getId();
               hit = pick.ibzfxic();
            }
         }
      }

      Entity entity = zoneId != null ? mc.level.getEntity(entityId) : null;
      InteractZone zone = entity != null ? InteractContents.zoneOf(entity, zoneId) : null;
      if (zone == null) {
         current = null;
         lostTarget();
         return;
      }

      InteractContent content = InteractContents.get(zone.dOfFwvo());
      Component title = content.title(entity, zone, mc.player);
      List<InteractOption> options = content.options(entity, zone, mc.player);
      int selected = current != null && current.entityId() == entityId && zone.v5suI().equals(current.zoneId())
         ? Math.min(current.selected(), Math.max(0, options.size() - 1))
         : 0;
      current = new Menu(entityId, zone.v5suI(), hit, title, options, selected, Math.sqrt(bestSqr));
      lastSeenNanos = System.nanoTime();
      boolean sameZone = entityId == lastEntityId && zone.v5suI().equals(lastZoneId);
      framesOnZone = sameZone ? framesOnZone + 1 : 1;
      lastEntityId = entityId;
      lastZoneId = zone.v5suI();
      boolean alreadyShowing = ALPHA.target() > 0.0F;
      if (alreadyShowing || framesOnZone >= FRAMES_BEFORE_SHOWN) {
         shown = current;
         ALPHA.setTarget(alphaForDistance(current.distance()));
      }
   }

   private static List<PlacedInteractZone> placedZones(Entity entity, float partialTick) {
      if (entity instanceof com.fpvdrone.entity.LoiteringMunitionEntity munition) {
         return munition.dOfFwvo(partialTick);
      }

      if (entity instanceof com.fpvdrone.entity.RampEntity ramp) {
         return ramp.wuhB(partialTick);
      }

      return entity instanceof DroneEntity drone ? drone.uSUb(partialTick) : List.of();
   }

   private static void lostTarget() {
      framesOnZone = 0;
      lastEntityId = -1;
      lastZoneId = null;
      if (System.nanoTime() - lastSeenNanos >= FADE_OUT_DELAY_NANOS) {
         ALPHA.setTarget(0.0F);
         resetClicks();
      }
   }

   private static void clearAll() {
      current = null;
      shown = null;
      ALPHA.snapTo(0.0F);
      framesOnZone = 0;
      lastEntityId = -1;
      lastZoneId = null;
      resetClicks();
   }

   /** 1.1.8 MR7Qyh.KKeMvpuBgEY: one frame's menu. */
   public record Menu(int entityId, String zoneId, Vec3 hit, Component title, List<InteractOption> options, int selected, double distance) {
      public Menu withSelected(int index) {
         return new Menu(this.entityId, this.zoneId, this.hit, this.title, this.options, index, this.distance);
      }
   }
}
