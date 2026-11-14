package com.fpvdrone.sync;

import com.fpvdrone.network.RemoteStatePacket;
import com.fpvdrone.util.ParticleSendRange;

import com.fpvdrone.network.ModNetwork;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class RemoteStateBroadcaster {
   private RemoteStateBroadcaster() {
   }

   public static void v5suI(Entity var0, RemoteSmoothed var1) {
      v5suI(var0, var1, false);
   }

   public static void v5suI(Entity var0, RemoteSmoothed var1, boolean var2) {
      if (var0.level() instanceof ServerLevel var3) {
         KinematicState var5 = new KinematicState();
         var1.v5suI(var5);
         v5suI(var3, var0, var1, RemoteStatePacket.v5suI(var0.getId(), var3.getGameTime(), var2, var5));
      }
   }

   public static void v5suI(Entity var0) {
      if (var0.level() instanceof ServerLevel var1) {
         v5suI(var1, var0, var0 instanceof RemoteSmoothed var3 ? var3 : null, RemoteStatePacket.v5suI(var0.getId(), var1.getGameTime()));
      }
   }

   private static void v5suI(ServerLevel var0, Entity var1, @Nullable RemoteSmoothed var2, RemoteStatePacket var3) {
      double var4 = ParticleSendRange.get();
      Vec3 var6 = var1.position();
      Vec3 var7 = var2 == null ? null : var2.lfyfLwuw();
      UUID var8 = var2 == null ? null : var2.iS1JnZov();

      for (ServerPlayer var10 : var0.players()) {
         if ((var8 == null || !var10.getUUID().equals(var8)) && v5suI(var10.position(), var6, var7, var4)) {
            ModNetwork.sendToPlayer(var10, var3);
         }
      }
   }

   public static boolean v5suI(Vec3 var0, Vec3 var1, @Nullable Vec3 var2, double var3) {
      double var5 = var3 * var3;
      return var0.distanceToSqr(var1) <= var5 ? true : var2 != null && var0.distanceToSqr(var2) <= var5;
   }
}
