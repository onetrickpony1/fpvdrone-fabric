package com.fpvdrone.interact;

import com.fpvdrone.data.InteractZone;
import net.minecraft.world.entity.Entity;

/**
 * 1.1.8 (client/thermal/PostProcessPass, a misleading name in the decompiled jar): an option handled on the client
 * (opening a screen) instead of being sent to the server. Returns true when it handled the option.
 */
public interface ClientInteractHandler {
   boolean handleOnClient(Entity entity, InteractZone zone, String optionId);
}
