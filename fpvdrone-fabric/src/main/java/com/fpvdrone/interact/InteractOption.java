package com.fpvdrone.interact;

import net.minecraft.network.chat.Component;

/**
 * 1.1.8 (Record_J0vInytH): one line of the interact menu. {@code available} false greys it out and adds " (None)";
 * {@code holdClicks} above zero means the action needs that many left clicks in a row instead of the interact key.
 */
public record InteractOption(String id, Component label, boolean available, int holdClicks) {
   public InteractOption(String id, Component label) {
      this(id, label, true, 0);
   }

   public InteractOption(String id, Component label, boolean available) {
      this(id, label, available, 0);
   }
}
