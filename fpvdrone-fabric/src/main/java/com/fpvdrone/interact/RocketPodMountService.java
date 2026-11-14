package com.fpvdrone.interact;

import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.data.RocketMount;
import java.util.List;
import org.jspecify.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * 1.1.8 (Pu5Kl3JGe), content "rocketPodMount": one zone per rocket pod mount point (the zone's Bone); installs a rocket
 * pod mount from the inventory or takes it back. On loitering munitions and on aircraft standing on a ramp.
 */
public final class RocketPodMountService implements InteractContent, InteractExecutor {
   private static final String INSTALL = "install";
   private static final String UNINSTALL = "uninstall";

   @Override
   public Component title(Entity entity, InteractZone zone, Player player) {
      return Component.empty();
   }

   @Override
   public List<InteractOption> options(Entity entity, InteractZone zone, Player player) {
      Mounts mounts = mountsOf(entity);
      if (mounts == null || player == null) {
         return List.of();
      }

      int index = mounts.indexOf(zone.ibzfxic());
      if (index < 0) {
         return List.of();
      }

      if (mounts.isInstalled(index)) {
         return List.of(new InteractOption(UNINSTALL, Component.translatable("message.fpvdrone.interact.uninstall_pod")));
      }

      boolean available = com.fpvdrone.registry.ModItems.findInInventory(player, com.fpvdrone.registry.ModItems.ROCKET_POD_MOUNT.get()) != null;
      return List.of(new InteractOption(INSTALL, Component.translatable("message.fpvdrone.interact.install_pod"), available));
   }

   @Override
   public boolean execute(ServerPlayer player, Entity entity, InteractZone zone, String optionId) {
      Mounts mounts = mountsOf(entity);
      if (mounts == null) {
         return false;
      }

      int index = mounts.indexOf(zone.ibzfxic());
      if (index < 0) {
         return false;
      }

      return switch (optionId) {
         case INSTALL -> !mounts.isInstalled(index) && mounts.install(index, player);
         case UNINSTALL -> mounts.isInstalled(index) && mounts.uninstall(index, player);
         default -> false;
      };
   }

   private static @Nullable Mounts mountsOf(Entity entity) {
      // 1.1.8-PENDING (munition slice): LoiteringMunitionEntity and RampEntity adapters. 1.1.8:
      //   munition m: indexOf(bone) = mountIndex(m.uQdlyiwB(), bone); isInstalled = m.v5suI(i);
      //     install = m.v5suI(i, player); uninstall = m.ibzfxic(i, player).
      //   ramp r: indexOf(bone) = !r.rsDd50tfV() ? -1 : mountIndex(DroneDefinitions.ibzfxic(r.dOfFwvo()), bone);
      //     isInstalled = r.v5suI(i); install = r.v5suI(i, player); uninstall = r.ibzfxic(i, player).
      return null;
   }

   /** Index of a rocket pod mount point by its bone in an aircraft's definition, or -1. */
   static int mountIndex(DroneDefinition definition, String bone) {
      if (!definition.wEJx3Itb()) {
         return -1;
      }

      List<RocketMount> mounts = definition.jt9oI60ST().ibzfxic();
      for (int i = 0; i < mounts.size(); i++) {
         if (bone.equals(mounts.get(i).ibzfxic())) {
            return i;
         }
      }

      return -1;
   }

   /** 1.1.8 Pu5Kl3JGe.KKeMvpuBgEY. */
   interface Mounts {
      int indexOf(String bone);

      boolean isInstalled(int index);

      boolean install(int index, Player player);

      boolean uninstall(int index, Player player);
   }
}
