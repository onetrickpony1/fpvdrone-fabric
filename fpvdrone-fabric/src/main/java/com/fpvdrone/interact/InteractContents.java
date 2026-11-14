package com.fpvdrone.interact;

import com.fpvdrone.data.InteractZone;
import com.fpvdrone.entity.DroneEntity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * 1.1.8 (BBijcV15): the interact zone "Content" names used in the drone / ramp files, and what each one shows and does.
 * Unknown names fall back to "static" (just the zone's "Text" as a title, no options).
 */
public final class InteractContents {
   public static final String STATIC = "static";
   public static final String MOUNT_POINT_LOADOUT = "mountPointLoadout";
   public static final String DRONE_SERVICE = "droneService";
   public static final String ROCKET_POD_MOUNT = "rocketPodMount";
   public static final String RAMP_SERVICE = "rampService";
   public static final String DRONE_ATTACH_SERVICE = "droneAttachService";
   private static final Map<String, InteractContent> CONTENTS = new LinkedHashMap<>();

   private InteractContents() {
   }

   public static void register(String name, InteractContent content) {
      CONTENTS.put(name, content);
   }

   public static InteractContent get(String name) {
      InteractContent content = CONTENTS.get(name);
      return content != null ? content : CONTENTS.get(STATIC);
   }

   public static @Nullable InteractExecutor executor(String name) {
      return get(name) instanceof InteractExecutor executor ? executor : null;
   }

   /** The interact zones an entity's definition declares (1.1.8 MR7Qyh / Packet_ZrfXHAQ1T inline lookups). */
   public static List<InteractZone> zonesOf(Entity entity) {
      // 1.1.8-PENDING (munition slice): LoiteringMunitionEntity m -> m.uQdlyiwB().c1jHYPb4(); RampEntity r -> r.cXHvq().
      return entity instanceof DroneEntity drone ? drone.oEuCBB().c1jHYPb4() : List.of();
   }

   /** One zone of an entity by id, or null. */
   public static @Nullable InteractZone zoneOf(Entity entity, String zoneId) {
      for (InteractZone zone : zonesOf(entity)) {
         if (zone.v5suI().equals(zoneId)) {
            return zone;
         }
      }

      return null;
   }

   /** Entities that can carry interact zones (1.1.8: LoiteringMunitionEntity, DroneEntity, RampEntity). */
   public static boolean hasZones(Entity entity) {
      // 1.1.8-PENDING (munition slice): || entity instanceof LoiteringMunitionEntity || entity instanceof RampEntity.
      return entity instanceof DroneEntity;
   }

   static {
      register(STATIC, new StaticContent());
      register(MOUNT_POINT_LOADOUT, new MountPointLoadout());
      register(DRONE_SERVICE, new DroneService());
      register(ROCKET_POD_MOUNT, new RocketPodMountService());
      register(RAMP_SERVICE, new RampService());
      register(DRONE_ATTACH_SERVICE, new DroneAttachService());
   }

   private static final class StaticContent implements InteractContent {
      @Override
      public Component title(Entity entity, InteractZone zone, Player player) {
         String text = zone.rsDd50tfV();
         return text != null && !text.isEmpty() ? Component.translatable(text) : Component.empty();
      }

      @Override
      public List<InteractOption> options(Entity entity, InteractZone zone, Player player) {
         return List.of();
      }
   }
}
