package com.fpvdrone.client.interact;

import com.fpvdrone.client.DroneAction;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.interact.InteractOption;
import com.fpvdrone.render.SmoothedValue;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;

/**
 * 1.1.8 (CjnQs6IuhPJ): draws the interact menu under the crosshair, before the chat. A title line, then the options;
 * more than three scroll in a three-line window with ▲ / ▼ marks. The selected line starts with "> [F] " (the interact
 * binding); unavailable options are grey and end in " (None)". It keeps clear of the hotbar and the chat.
 */
public final class InteractMenuOverlay {
   private static final int WHITE = -1;
   private static final int GREY = -1865626420;
   private static final int LINE_HEIGHT = 12;
   private static final int GAP_BELOW_CROSSHAIR = 16;
   private static final int EXTRA_GAP_NO_TITLE = 6;
   private static final int VISIBLE_LINES = 3;
   private static final int CLIP_MARGIN = 3;
   private static final String DOWN = "▼";
   private static final String UP = "▲";
   private static final int HOTBAR_SPACE = 68;
   private static final int BOTTOM_MARGIN = 10;
   private static final int CHAT_MARGIN = 6;
   private static int lastEntityId = -1;
   private static @Nullable String lastZoneId = null;
   private static final SmoothedValue SCROLL = new SmoothedValue(0.0F, 5.0F, 0.0F, 0.0F);

   private InteractMenuOverlay() {
   }

   public static void init() {
      HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath("fpvdrone", "interact_menu"), InteractMenuOverlay::render);
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
      InteractMenu.Menu menu = InteractMenu.shown();
      float alphaF = InteractMenu.alpha();
      if (menu == null || alphaF <= 0.0F) {
         return;
      }

      int alpha = Math.round(alphaF * 255.0F);
      if (alpha <= 0) {
         return;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.level == null) {
         return;
      }

      int width = mc.getWindow().getGuiScaledWidth();
      int height = mc.getWindow().getGuiScaledHeight();
      int centreX = width / 2;
      String title = menu.title().getString();
      List<InteractOption> options = menu.options();
      int count = options.size();
      int selected = menu.selected();
      boolean sameMenu = menu.entityId() == lastEntityId && Objects.equals(menu.zoneId(), lastZoneId);
      lastEntityId = menu.entityId();
      lastZoneId = menu.zoneId();
      boolean scrolls = count > VISIBLE_LINES;
      float scroll;
      if (scrolls) {
         SCROLL.setRange(0.0F, count - 1);
         if (sameMenu) {
            SCROLL.setTarget(selected);
         } else {
            SCROLL.snapTo(selected);
         }

         scroll = SCROLL.update();
      } else {
         SCROLL.setRange(0.0F, Math.max(0, count - 1));
         SCROLL.snapTo(selected);
         scroll = selected;
      }

      int visible = Math.min(count, VISIBLE_LINES);
      int gap = GAP_BELOW_CROSSHAIR + (scrolls && title.isEmpty() ? EXTRA_GAP_NO_TITLE : 0);
      int lines = (title.isEmpty() ? 0 : 1) + visible + (scrolls ? 2 : 0);
      int blockHeight = lines * LINE_HEIGHT;
      int y = height / 2 + gap;
      int bottom = height - HOTBAR_SPACE - BOTTOM_MARGIN;
      bottom = Math.min(bottom, chatTop(mc, height) - CHAT_MARGIN);
      if (y + blockHeight > bottom) {
         y = Math.max(height / 2 + gap, bottom - blockHeight);
      }

      Font font = mc.font;
      if (!title.isEmpty()) {
         drawString(graphics, font, title, centreX - font.width(title) / 2, y, withAlpha(WHITE, alpha));
         y += LINE_HEIGHT;
      }

      if (!scrolls) {
         for (int i = 0; i < count; i++) {
            drawOption(graphics, font, options.get(i), i == selected, centreX, y, alpha);
            y += LINE_HEIGHT;
         }
      } else {
         if (selected > 0) {
            drawString(graphics, font, UP, centreX - font.width(UP) / 2, y, withAlpha(GREY, alpha));
         }

         y += LINE_HEIGHT;
         int windowTop = y;
         int firstLineY = windowTop + LINE_HEIGHT;
         int windowBottom = windowTop + visible * LINE_HEIGHT;
         int clipBottom = windowBottom - CLIP_MARGIN;
         graphics.enableScissor(0, windowTop, width, clipBottom);

         for (int i = 0; i < count; i++) {
            float lineY = firstLineY + (i - scroll) * LINE_HEIGHT;
            int lineYi = Mth.floor(lineY);
            float fraction = lineY - lineYi;
            if (lineYi + LINE_HEIGHT >= windowTop && lineYi <= clipBottom) {
               graphics.pose().pushMatrix();
               graphics.pose().translate(0.0F, fraction);
               drawOption(graphics, font, options.get(i), i == selected, centreX, lineYi, alpha);
               graphics.pose().popMatrix();
            }
         }

         graphics.disableScissor();
         y = windowBottom;
         if (selected < count - 1) {
            drawString(graphics, font, DOWN, centreX - font.width(DOWN) / 2, y, withAlpha(GREY, alpha));
         }
      }
   }

   /** Top of the chat area, so the menu stays above it (the bottom of the screen when chat is hidden). */
   private static int chatTop(Minecraft mc, int height) {
      if (mc.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
         return height;
      }

      double pct = mc.gui.screen() instanceof ChatScreen ? mc.options.chatHeightFocused().get() : mc.options.chatHeightUnfocused().get();
      int chatHeight = ChatComponent.getHeight(pct);
      float scale = mc.options.chatScale().get().floatValue();
      return height - 40 - Mth.ceil(chatHeight * scale);
   }

   private static void drawOption(GuiGraphicsExtractor graphics, Font font, InteractOption option, boolean selected, int centreX, int y, int alpha) {
      Component text = option.available() ? option.label() : option.label().copy().append(Component.translatable("message.fpvdrone.interact.unavailable"));
      if (selected) {
         String key = KeyManager.bindingLabel(DroneAction.INTERACT);
         String prefix = "None".equals(key) ? "> " : "> [" + key + "] ";
         MutableComponent withPrefix = Component.literal(prefix).append(text);
         text = withPrefix;
      }

      int color = selected && option.available() ? WHITE : GREY;
      drawComponent(graphics, font, text, centreX - font.width(text) / 2, y, withAlpha(color, alpha));
   }

   private static int withAlpha(int argb, int alpha) {
      int a = (argb >>> 24) * alpha / 255;
      return a << 24 | argb & 0xFFFFFF;
   }

   private static void drawString(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
      if (color >>> 24 >= 4) {
         graphics.text(font, text, x, y, color, true);
      }
   }

   private static void drawComponent(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
      if (color >>> 24 >= 4) {
         graphics.text(font, text, x, y, color, true);
      }
   }
}
