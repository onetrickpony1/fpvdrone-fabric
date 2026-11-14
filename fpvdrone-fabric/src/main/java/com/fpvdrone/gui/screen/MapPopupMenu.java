package com.fpvdrone.gui.screen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** 1.1.8 data/Record_C0etjf: the tactical map's right-click menu (12 px rows, optional blank header rows). */
public class MapPopupMenu {
   private static final int ROW_HEIGHT = 12;
   private static final int PADDING = 3;
   private static final int BACKGROUND = 0xC0000000;
   private static final int DISABLED_TEXT = 0xFFB8B8C0;
   private static final int TEXT = 0xFFFFFFFF;
   private static final int HOVER = 0x40FFFFFF;
   private final int x;
   private final int y;
   private final int width;
   private final List<Item> items;
   private final int headerRows;

   public MapPopupMenu(int x, int y, List<Item> items, Font font) {
      this(x, y, items, font, 0);
   }

   public MapPopupMenu(int x, int y, List<Item> items, Font font, int headerRows) {
      this.items = new ArrayList<>(items);
      this.headerRows = headerRows;
      int w = 40;
      for (Item item : items) {
         w = Math.max(w, font.width(item.label()) + 6);
      }

      this.width = w;
      this.x = x;
      this.y = y;
   }

   public int v5suI() {
      return this.x;
   }

   public int ibzfxic() {
      return this.y;
   }

   public int wuhB() {
      return this.width;
   }

   public int dOfFwvo() {
      return this.y + PADDING;
   }

   public int rsDd50tfV() {
      return (this.items.size() + this.headerRows) * ROW_HEIGHT + 6;
   }

   public boolean v5suI(double mx, double my) {
      return mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.rsDd50tfV();
   }

   /** Runs the clicked row's action; returns whether the menu stays open. */
   public boolean ibzfxic(double mx, double my) {
      if (!this.v5suI(mx, my)) {
         return false;
      }

      int row = (int)((my - (this.y + PADDING)) / ROW_HEIGHT) - this.headerRows;
      if (row >= 0 && row < this.items.size()) {
         Item item = this.items.get(row);
         if (!item.disabled() && item.action() != null) {
            item.action().run();
            return item.keepOpen();
         }
      }

      return false;
   }

   public void v5suI(GuiGraphicsExtractor g, Font font, int mx, int my) {
      g.fill(this.x, this.y, this.x + this.width, this.y + this.rsDd50tfV(), BACKGROUND);
      int rowY = this.y + PADDING + this.headerRows * ROW_HEIGHT;

      for (Item item : this.items) {
         boolean hover = !item.disabled() && mx >= this.x && mx <= this.x + this.width && my >= rowY && my < rowY + ROW_HEIGHT;
         if (hover) {
            g.fill(this.x, rowY, this.x + this.width, rowY + ROW_HEIGHT, HOVER);
         }

         g.text(font, item.label(), this.x + 3, rowY + 2, item.disabled() ? DISABLED_TEXT : TEXT, true);
         rowY += ROW_HEIGHT;
      }
   }

   /** A row: label, action (null = greyed out header/label row), and whether the menu stays open after it runs. */
   public record Item(Component label, Runnable action, boolean keepOpen) {
      public Item(Component label, Runnable action) {
         this(label, action, false);
      }

      public boolean disabled() {
         return this.action == null;
      }
   }
}
