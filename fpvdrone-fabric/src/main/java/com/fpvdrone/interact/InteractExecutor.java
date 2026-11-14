package com.fpvdrone.interact;

import com.fpvdrone.data.InteractZone;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** 1.1.8 (JN0gr0rxPP): the server side of an interact zone: runs the chosen option. */
public interface InteractExecutor {
   boolean execute(ServerPlayer player, Entity entity, InteractZone zone, String optionId);
}
