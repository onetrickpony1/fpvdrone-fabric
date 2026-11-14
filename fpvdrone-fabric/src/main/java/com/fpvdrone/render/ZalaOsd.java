package com.fpvdrone.render;

import com.fpvdrone.client.MunitionClientState;
import com.fpvdrone.data.DroneDefinition;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * 1.1.8 sxez6mi/Gs8VIFVhJy: the ZALA-style OSD of fixed-wing aircraft (Lancet, Orlan). A thin reticle (side lines,
 * top and bottom stems, centre cross, corner brackets) with a sub-pixel antialiased fill; in the gimbal-observe view
 * also a compass ring, a tilt scale and a target marker. F9 / F10 change its size (a debug control kept from 1.1.8).
 */
public final class ZalaOsd {
   private static final float BASE_THICKNESS = 2.25F;
   private static final int BASE_HEIGHT = 1620;
   private static final float MIN_THICKNESS = 1.5F;
   private static final float HEIGHT_FACTOR = 1.125F;
   private static final int OUTLINE = 0xFF000000;
   private static final int LINE = 0xFFFFFFFF;
   private static final int COMPASS_TEXT = 0xFFACD0F3;
   private static final String[] COMPASS_LETTERS = new String[]{"N", "E", "S", "W"};
   private static final float[] COMPASS_ANGLES = new float[]{0.0F, 90.0F, 180.0F, 270.0F};
   private static final float COMPASS_RADIUS_FACTOR = 2.1F;
   private static final float COMPASS_TEXT_SCALE = 1.5F;
   /** The first four segments are the side lines and the top/bottom stems; 4-5 the centre cross; 6+ the brackets. */
   static final int SIDE_SEGMENTS = 4;
   static final int CROSS_SEGMENTS = 2;
   static final int FIRST_BRACKET = 6;
   static float debugScale = 1.0F;
   private static boolean f9WasDown = false;
   private static boolean f10WasDown = false;

   private ZalaOsd() {
   }

   private static void debugSizeKeys(OsdFrame frame) {
      boolean f9 = InputConstants.isKeyDown(InputConstants.KEY_F9);
      boolean f10 = InputConstants.isKeyDown(InputConstants.KEY_F10);
      if (f9 && !f9WasDown) {
         debugScale += 0.05F;
         announceSize(frame.player());
      }

      if (f10 && !f10WasDown) {
         debugScale = Math.max(0.05F, debugScale - 0.05F);
         announceSize(frame.player());
      }

      f9WasDown = f9;
      f10WasDown = f10;
   }

   private static void announceSize(Player player) {
      if (player != null) {
         int percent = Math.round(debugScale * 100.0F);
         player.sendOverlayMessage(Component.literal("ZALA OSD Debug Size: " + percent + "%").withStyle(Style.EMPTY.withColor(0xACD0F3)));
      }
   }

   public static void render(OsdFrame frame) {
      debugSizeKeys(frame);
      Layout layout = layout(frame.width(), frame.height());
      GuiGraphicsExtractor g = frame.graphics();
      List<Segment> segments = layout.segments();
      float thickness = layout.thickness();
      boolean hasMarker = Math.abs(frame.markerX()) > 0.001F || Math.abs(frame.markerY()) > 0.001F;
      boolean reduced = !frame.fullReticle();
      if (frame.gimbalObserve()) {
         drawSegments(g, segments.subList(SIDE_SEGMENTS, FIRST_BRACKET), thickness);
         if (frame.markerOnScreen() && segments.size() > FIRST_BRACKET) {
            g.pose().pushMatrix();
            g.pose().translate(frame.markerX(), frame.markerY());
            drawSegments(g, segments.subList(FIRST_BRACKET, segments.size()), thickness);
            g.pose().popMatrix();
         }

         drawCompass(g, layout, frame.gimbalYaw());
         drawTiltScale(g, layout);
         drawTiltPointer(g, layout, frame);
      } else {
         int from = reduced ? SIDE_SEGMENTS : 0;
         if (hasMarker && !frame.markerOnScreen() && segments.size() > FIRST_BRACKET) {
            drawSegments(g, segments.subList(from, FIRST_BRACKET), thickness);
         } else if (hasMarker && segments.size() > FIRST_BRACKET) {
            drawSegments(g, segments.subList(from, FIRST_BRACKET), thickness);
            g.pose().pushMatrix();
            g.pose().translate(frame.markerX(), frame.markerY());
            drawSegments(g, segments.subList(FIRST_BRACKET, segments.size()), thickness);
            g.pose().popMatrix();
         } else {
            drawSegments(g, reduced ? segments.subList(SIDE_SEGMENTS, segments.size()) : segments, thickness);
         }
      }
   }

   /** Vertical tilt scale on the right: a bar with end ticks. */
   private static void drawTiltScale(GuiGraphicsExtractor g, Layout l) {
      if (!(l.scaleBottom() <= l.scaleTop())) {
         float t = l.thickness();
         float x = l.scaleX();
         float top = l.scaleTop();
         float bottom = l.scaleBottom();
         float tick = l.tickLength();
         fillF(g, x - t / 2.0F - t, top - t, x + t / 2.0F + t, bottom + t, OUTLINE);
         fillF(g, x - t / 2.0F - t, top - t / 2.0F - t, x + tick + t, top + t / 2.0F + t, OUTLINE);
         fillF(g, x - t / 2.0F - t, bottom - t / 2.0F - t, x + tick + t, bottom + t / 2.0F + t, OUTLINE);
         fillF(g, x - t / 2.0F, top, x + t / 2.0F, bottom, LINE);
         fillF(g, x - t / 2.0F, top - t / 2.0F, x + tick, top + t / 2.0F, LINE);
         fillF(g, x - t / 2.0F, bottom - t / 2.0F, x + tick, bottom + t / 2.0F, LINE);
      }
   }

   /** Triangle pointer on the tilt scale at the camera pitch, within the gimbal's limits. */
   private static void drawTiltPointer(GuiGraphicsExtractor g, Layout l, OsdFrame frame) {
      if (!(l.scaleBottom() <= l.scaleTop())) {
         float pitch = 0.0F;
         float min = -50.0F;
         float max = 90.0F;
         if (MunitionClientState.iElAbEZe()) {
            float[] view = MunitionClientState.r6s7FL3();
            if (view != null && view.length >= 2) {
               pitch = view[1];
            }

            DroneDefinition.GimbalLimits limits = MunitionClientState.wEJx3Itb();
            if (limits != null) {
               min = -limits.shakeBone();
               max = limits.shakeAngleDeg();
            }
         } else if (frame.munition() != null) {
            pitch = frame.munition().getXRot();
            DroneDefinition.GimbalLimits limits = frame.munition().uQdlyiwB().bAcdvq9().gimbalLimits();
            if (limits != null) {
               min = -limits.shakeBone();
               max = limits.shakeAngleDeg();
            }
         } else if (frame.drone() != null) {
            pitch = frame.drone().getXRot();
            DroneDefinition.GimbalLimits limits = frame.drone().oEuCBB().bAcdvq9().gimbalLimits();
            if (limits != null) {
               min = -limits.shakeBone();
               max = limits.shakeAngleDeg();
            }
         } else if (frame.player() != null) {
            pitch = frame.player().getXRot();
            min = -90.0F;
            max = 90.0F;
         }

         float range = Math.max(1.0F, max - min);
         float f = Mth.clamp((pitch - min) / range, 0.0F, 1.0F);
         float y = l.scaleTop() + f * (l.scaleBottom() - l.scaleTop());
         float t = l.thickness();
         float x = l.scaleX();
         float len = l.markerLength();
         float half = l.markerHeight() / 2.0F;
         float tipX = x + len;
         float outline = t + 2.0F * t;
         line(g, tipX, y - half, tipX, y + half, outline, OUTLINE);
         line(g, tipX, y - half, x, y, outline, OUTLINE);
         line(g, tipX, y + half, x, y, outline, OUTLINE);
         line(g, tipX, y - half, tipX, y + half, t, LINE);
         line(g, tipX, y - half, x, y, t, LINE);
         line(g, tipX, y + half, x, y, t, LINE);
      }
   }

   /** A line of the given width, drawn as a rotated rectangle. */
   private static void line(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1, float width, int color) {
      float dx = x1 - x0;
      float dy = y1 - y0;
      float length = (float)Math.hypot(dx, dy);
      float half = width / 2.0F;
      if (length <= 0.001F) {
         fillF(g, x0 - half, y0 - half, x0 + half, y0 + half, color);
      } else {
         float angle = (float)Math.atan2(dy, dx);
         g.pose().pushMatrix();
         g.pose().translate(x0, y0);
         g.pose().rotate(angle);
         int top = Math.round(-half);
         int bottom = Math.round(half);
         if (bottom <= top) {
            bottom = top + 1;
         }

         int start = -Math.round(half);
         int end = Math.max(start + 1, Math.round(length + half));
         g.fill(start, top, end, bottom, color);
         g.pose().popMatrix();
      }
   }

   /** 1.1.8 Gs8VIFVhJy.v5suI(): vertical position of the reticle centre as a fraction of the height. */
   public static float centreFraction() {
      return 0.5F;
   }

   private static void drawCompass(GuiGraphicsExtractor g, Layout l, float yaw) {
      Font font = Minecraft.getInstance().font;
      float scale = Math.max(1.0F, l.thickness());
      float heading = Mth.wrapDegrees(yaw + 180.0F);

      for (int i = 0; i < COMPASS_LETTERS.length; i++) {
         float[] p = compassPoint(l, COMPASS_ANGLES[i], heading);
         drawCompassLetter(g, font, COMPASS_LETTERS[i], p[0], p[1], scale);
      }
   }

   static float[] compassPoint(Layout l, float bearing, float heading) {
      float rel = Mth.wrapDegrees(bearing - heading);
      double rad = Math.toRadians(rel);
      float x = l.centreX() + (float)(l.compassRadius() * Math.sin(rad));
      float y = l.centreY() - (float)(l.compassRadius() * Math.cos(rad));
      return new float[]{x, y};
   }

   private static void drawCompassLetter(GuiGraphicsExtractor g, Font font, String text, float x, float y, float scale) {
      float s = scale * COMPASS_TEXT_SCALE;
      float w = font.width(text) * s;
      float h = 9.0F * s;
      int left = Math.round(x - w / 2.0F);
      int top = Math.round(y - h / 2.0F);
      g.pose().pushMatrix();
      g.pose().translate(left, top);
      g.pose().scale(s, s);

      for (int ox = -1; ox <= 1; ox++) {
         for (int oy = -1; oy <= 1; oy++) {
            if (ox != 0 || oy != 0) {
               g.text(font, text, ox, oy, OUTLINE, false);
            }
         }
      }

      g.text(font, text, 0, 0, COMPASS_TEXT, false);
      g.pose().popMatrix();
   }

   /** All outlines first, then all white strokes, so outlines never cover a neighbouring line. */
   private static void drawSegments(GuiGraphicsExtractor g, List<Segment> segments, float thickness) {
      for (Segment s : segments) {
         fillF(g, s.x() - thickness, s.y() - thickness, s.x() + s.w() + thickness, s.y() + s.h() + thickness, OUTLINE);
      }

      for (Segment s : segments) {
         fillF(g, s.x(), s.y(), s.x() + s.w(), s.y() + s.h(), LINE);
      }
   }

   /** A fill with float edges: partial edge pixels get alpha by coverage. */
   private static void fillF(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1, int color) {
      if (!(x1 <= x0) && !(y1 <= y0)) {
         int alpha = color >>> 24 & 0xFF;
         int rgb = color & 0xFFFFFF;
         Span[] xs = spans(x0, x1);
         Span[] ys = spans(y0, y1);

         for (Span sx : xs) {
            for (Span sy : ys) {
               int a = Math.round(alpha * sx.coverage() * sy.coverage());
               if (a > 0) {
                  g.fill(sx.from(), sy.from(), sx.to(), sy.to(), Mth.clamp(a, 0, 255) << 24 | rgb);
               }
            }
         }
      }
   }

   private static Span[] spans(float from, float to) {
      int lo = Mth.floor(from);
      int hi = Mth.ceil(to);
      if (hi - lo == 1) {
         return new Span[]{new Span(lo, lo + 1, to - from)};
      }

      List<Span> out = new ArrayList<>(3);
      float first = lo + 1 - from;
      if (first < 0.999F) {
         out.add(new Span(lo, lo + 1, first));
         lo++;
      }

      float last = to - (hi - 1);
      int innerEnd = hi;
      boolean partialLast = last < 0.999F;
      if (partialLast) {
         innerEnd = hi - 1;
      }

      if (innerEnd > lo) {
         out.add(new Span(lo, innerEnd, 1.0F));
      }

      if (partialLast && hi - 1 >= innerEnd) {
         out.add(new Span(hi - 1, hi, last));
      }

      return out.toArray(new Span[0]);
   }

   static Layout layout(int width, int height) {
      if (width > 0 && height > 0) {
         int size = Math.max(1, Math.round(Math.min(height, Math.round(width * 9.0F / 16.0F)) * HEIGHT_FACTOR * debugScale));
         int cx = width / 2;
         int cy = Math.round(height * 0.5F);
         float thickness = Math.max(MIN_THICKNESS, BASE_THICKNESS * size / BASE_HEIGHT);
         int halfW = Math.max(4, Math.round(size * 0.075F));
         int halfH = Math.max(4, Math.round(size * 0.075F));
         int bracket = Math.max(3, Math.round(size * 0.045F));
         int sideGap = Math.max(4, Math.round(size * 0.015F));
         int stemGap = Math.max(4, Math.round(size * 0.02F));
         int cross = Math.max(3, Math.round(size * 0.00875F));
         int left = cx - halfW;
         int right = cx + halfW;
         int top = cy - halfH;
         int bottom = cy + halfH;
         int overscan = Math.max(2, Math.round(size * 0.03F));
         List<Segment> segs = new ArrayList<>(14);
         horizontal(segs, -overscan, left - sideGap, cy, thickness);
         horizontal(segs, right + sideGap, width + overscan, cy, thickness);
         int stemEnd = top - stemGap;
         int stemSpan = stemEnd + overscan;
         int stemLength = Math.max(Math.round(thickness), Math.round(stemSpan * 0.4F));
         vertical(segs, cx, stemEnd - stemLength, stemEnd, thickness);
         vertical(segs, cx, bottom + stemGap, height + overscan, thickness);
         horizontal(segs, cx - cross, cx + cross, cy, thickness);
         vertical(segs, cx, cy - cross, cy + cross, thickness);
         horizontal(segs, left, left + bracket, top, thickness);
         vertical(segs, left, top, top + bracket, thickness);
         horizontal(segs, right - bracket, right, top, thickness);
         vertical(segs, right, top, top + bracket, thickness);
         horizontal(segs, left, left + bracket, bottom, thickness);
         vertical(segs, left, bottom - bracket, bottom, thickness);
         horizontal(segs, right - bracket, right, bottom, thickness);
         vertical(segs, right, bottom - bracket, bottom, thickness);
         float compassRadius = halfW * COMPASS_RADIUS_FACTOR;
         float scaleX = Math.round(width * 0.08F);
         float scaleHeight = Math.round(size * 0.44F);
         float scaleCentre = cy - Math.round(size * 0.1F);
         float scaleTop = scaleCentre - scaleHeight / 2.0F;
         float scaleBottom = scaleCentre + scaleHeight / 2.0F;
         float tick = Math.max(4.0F, Math.round(size * 0.018F));
         float markerLength = Math.max(5.0F, Math.round(size * 0.026F));
         float markerHeight = Math.max(3.0F, Math.round(size * 0.013F));
         return new Layout(cx, cy, thickness, List.copyOf(segs), compassRadius, scaleX, scaleTop, scaleBottom, tick, markerLength, markerHeight);
      }

      return new Layout(0, 0, 1.0F, List.of(), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   private static void horizontal(List<Segment> out, int x0, int x1, int y, float thickness) {
      if (x1 >= x0) {
         out.add(new Segment(x0, y - thickness / 2.0F, x1 - x0 + 1, thickness));
      }
   }

   private static void vertical(List<Segment> out, int x, int y0, int y1, float thickness) {
      if (y1 >= y0) {
         out.add(new Segment(x - thickness / 2.0F, y0, thickness, y1 - y0 + 1));
      }
   }

   record Layout(
      int centreX,
      int centreY,
      float thickness,
      List<Segment> segments,
      float compassRadius,
      float scaleX,
      float scaleTop,
      float scaleBottom,
      float tickLength,
      float markerLength,
      float markerHeight
   ) {
   }

   private record Span(int from, int to, float coverage) {
   }

   record Segment(float x, float y, float w, float h) {
   }
}
