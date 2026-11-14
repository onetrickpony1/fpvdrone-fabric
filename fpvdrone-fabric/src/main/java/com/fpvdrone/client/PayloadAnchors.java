package com.fpvdrone.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * 1.1.8 RYFyZhzE: where each drone's payloads were last drawn, in world space, written by the carrier renderers every
 * frame (the hanging grenade on payload_mount: v5suI(UUID, Vec3, Quaternionf); bomb-bay station i: v5suI(UUID, int, Vec3,
 * Quaternionf)). The drop action spawns its predicted bomb exactly there, and {@link
 * com.fpvdrone.client.renderer.DroppedBombRenderer} blends a remote player's bomb out of that spot so it does not pop.
 */
public final class PayloadAnchors {
   private static final Map<UUID, Vec3> v5suI = new ConcurrentHashMap<>();
   private static final Map<UUID, Quaternionf> ibzfxic = new ConcurrentHashMap<>();
   private static final Map<UUID, Map<Integer, Vec3>> wuhB = new ConcurrentHashMap<>();
   private static final Map<UUID, Map<Integer, Quaternionf>> dOfFwvo = new ConcurrentHashMap<>();

   private PayloadAnchors() {
   }

   public static void v5suI(UUID var0, Vec3 var1, Quaternionf var2) {
      v5suI.put(var0, var1);
      ibzfxic.put(var0, var2);
   }

   public static void v5suI(UUID var0, int var1, Vec3 var2, Quaternionf var3) {
      wuhB.computeIfAbsent(var0, var0x -> new ConcurrentHashMap<>()).put(var1, var2);
      dOfFwvo.computeIfAbsent(var0, var0x -> new ConcurrentHashMap<>()).put(var1, var3);
   }

   public static Vec3 v5suI(UUID var0) {
      return v5suI.get(var0);
   }

   public static Quaternionf ibzfxic(UUID var0) {
      return ibzfxic.get(var0);
   }

   public static Vec3 v5suI(UUID var0, int var1) {
      Map var2 = wuhB.get(var0);
      return var2 == null ? null : (Vec3)var2.get(var1);
   }

   public static Quaternionf ibzfxic(UUID var0, int var1) {
      Map var2 = dOfFwvo.get(var0);
      return var2 == null ? null : (Quaternionf)var2.get(var1);
   }
}
