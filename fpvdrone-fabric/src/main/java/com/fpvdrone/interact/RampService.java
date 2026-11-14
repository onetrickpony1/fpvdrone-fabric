package com.fpvdrone.interact;

import com.fpvdrone.data.DroneDefinitions;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.data.RampDefinition;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * 1.1.8 (GAdkJUip), content "rampService": on an empty ramp, "Install &lt;aircraft&gt;" for each aircraft the ramp
 * takes (its file's list, or every drone for "*" or an empty list) that the player carries; always "Remove ramp".
 */
public final class RampService implements InteractContent, InteractExecutor {
   private static final String INSTALL_PREFIX = "install:";
   private static final String REMOVE_RAMP = "removeRamp";

   @Override
   public Component title(Entity entity, InteractZone zone, Player player) {
      return Component.empty();
   }

   @Override
   public List<InteractOption> options(Entity entity, InteractZone zone, Player player) {
      // 1.1.8-PENDING (munition slice): RampEntity. 1.1.8:
      //   if (entity instanceof RampEntity ramp && player != null) {
      //      out = [];
      //      if (!ramp.rsDd50tfV()) // no aircraft on it
      //         for (String droneId : acceptedDrones(ramp.jGhI2())) {
      //            Item item = ModItems.droneItemFor(droneId);
      //            if (item != null && ModItems.findInInventory(player, item) != null)
      //               out.add(new InteractOption(INSTALL_PREFIX + droneId, translatable("message.fpvdrone.interact.install_drone",
      //                  translatable(DroneDefinitions.ibzfxic(droneId).iRWc2Pi()))));
      //         }
      //      out.add(new InteractOption(REMOVE_RAMP, translatable("message.fpvdrone.interact.remove_ramp")));
      //      return out;
      //   }
      return List.of();
   }

   @Override
   public boolean execute(ServerPlayer player, Entity entity, InteractZone zone, String optionId) {
      // 1.1.8-PENDING (munition slice): RampEntity. 1.1.8:
      //   optionId "install:<drone>" -> ramp.v5suI(droneId, player); "removeRamp" -> ramp.v5suI(player).
      return false;
   }

   /** The aircraft a ramp takes: its list, or every drone definition when the list is empty or has "*". */
   static List<String> acceptedDrones(RampDefinition ramp) {
      List<String> list = ramp.vmpmU();
      return !list.isEmpty() && !list.contains("*") ? list : List.copyOf(DroneDefinitions.v5suI().keySet());
   }
}
