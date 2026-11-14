package com.fpvdrone.interact;

import com.fpvdrone.data.InteractZone;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** 1.1.8 (BBPAfwDBvb): what an interact zone shows: a title line and the options, built on the client each frame. */
public interface InteractContent {
   Component title(Entity entity, InteractZone zone, Player player);

   List<InteractOption> options(Entity entity, InteractZone zone, Player player);
}
