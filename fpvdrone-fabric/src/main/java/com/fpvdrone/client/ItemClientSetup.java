package com.fpvdrone.client;

import com.fpvdrone.block.JammerZones;
import com.fpvdrone.item.ItemIconTooltip;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;

/** Client hooks for the 1.1.8 items and blocks: the item-icon tooltip block, and forgetting jammers on leaving a world. */
public final class ItemClientSetup {
    private ItemClientSetup() {
    }

    public static void init() {
        ClientTooltipComponentCallback.EVENT.register(component -> component instanceof ItemIconTooltip icons ? new ItemIconTooltipRenderer(icons) : null);
        // 1.1.8 cleared the jammer list on client level unload.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> JammerZones.clear());
    }
}
