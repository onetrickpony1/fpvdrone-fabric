package com.fpvdrone.render;

import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.LoiteringMunitionEntity;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 data/Record_DI7qSmAxI: one frame of OSD input, handed to the renderer of the aircraft's OSD profile.
 *
 * @param feed            whose video this is (a drone or a loitering munition)
 * @param frozen          no signal: the last frame is shown and only warnings update
 * @param markerX         target marker offset from the reticle centre, in window pixels
 * @param markerOnScreen  the target marker is inside the frame
 * @param gimbalObserve   the gimbal-observe control scheme (compass ring and tilt scale are drawn)
 * @param gimbalYaw       the gimbal heading used by the compass ring
 * @param fullReticle     draw the side lines of the reticle too
 */
public record OsdFrame(
   Feed feed,
   UUID sourceId,
   Minecraft mc,
   GuiGraphicsExtractor graphics,
   Player player,
   int width,
   int height,
   boolean frozen,
   @Nullable DroneEntity drone,
   @Nullable LoiteringMunitionEntity munition,
   float markerX,
   float markerY,
   boolean markerOnScreen,
   boolean gimbalObserve,
   float gimbalYaw,
   boolean fullReticle
) {
   public OsdFrame {
      Objects.requireNonNull(feed, "feed");
      Objects.requireNonNull(sourceId, "sourceId");
   }

   public static OsdFrame drone(UUID id, Minecraft mc, GuiGraphicsExtractor g, Player p, int w, int h, boolean frozen, @Nullable DroneEntity drone) {
      return new OsdFrame(Feed.DRONE, id, mc, g, p, w, h, frozen, drone, null, 0.0F, 0.0F, true, false, 0.0F, true);
   }

   public static OsdFrame munition(UUID id, Minecraft mc, GuiGraphicsExtractor g, Player p, int w, int h, boolean frozen, @Nullable LoiteringMunitionEntity m) {
      return new OsdFrame(Feed.MUNITION, id, mc, g, p, w, h, frozen, null, m, 0.0F, 0.0F, true, false, 0.0F, true);
   }

   /** Click-designate view: a target marker, and the side lines unless the reticle is reduced. */
   public static OsdFrame designate(
      UUID id, Minecraft mc, GuiGraphicsExtractor g, Player p, int w, int h, boolean frozen, @Nullable LoiteringMunitionEntity m,
      float markerX, float markerY, boolean markerOnScreen, boolean fullReticle
   ) {
      return new OsdFrame(Feed.MUNITION, id, mc, g, p, w, h, frozen, null, m, markerX, markerY, markerOnScreen, false, 0.0F, fullReticle);
   }

   /** Gimbal-observe view: target marker plus the compass ring at the gimbal heading. */
   public static OsdFrame observe(
      UUID id, Minecraft mc, GuiGraphicsExtractor g, Player p, int w, int h, @Nullable LoiteringMunitionEntity m,
      float markerX, float markerY, boolean markerOnScreen, float gimbalYaw
   ) {
      return new OsdFrame(Feed.MUNITION, id, mc, g, p, w, h, false, null, m, markerX, markerY, markerOnScreen, true, gimbalYaw, true);
   }

   /** 1.1.8 Record_DI7qSmAxI.KKeMvpuBgEY. */
   public enum Feed {
      DRONE,
      MUNITION
   }
}
