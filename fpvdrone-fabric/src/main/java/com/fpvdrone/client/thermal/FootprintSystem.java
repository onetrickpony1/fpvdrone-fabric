package com.fpvdrone.client.thermal;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 1.1.8 FootprintSystem: walking mobs and players leave warm footprints that fade over 40 s (800 ticks), left and
 * right in turn, one per 2.5 foot lengths. Footprints vanish when the block under them stops being solid.
 */
public class FootprintSystem {
   public static final int LIFETIME_TICKS = 800;
   static final float FOOT_SIZE = 0.3125F;
   static final float FOOT_SPREAD = 0.25F;
   private static final double MIN_MOVE_SQR = 4.0E-4;
   static final int MAX_DECALS = 2048;
   private static final float STRIDE = 2.5F;
   private static final FootprintSystem INSTANCE = new FootprintSystem();
   private final Deque<FootprintDecal> decals = new ArrayDeque<>();
   private final Map<UUID, EntityFootState> entityStates = new HashMap<>();

   FootprintSystem() {
   }

   public static FootprintSystem getInstance() {
      return INSTANCE;
   }

   public void tick(Level level) {
      if (level == null) {
         return;
      }
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return;
      }
      long gameTime = level.getGameTime();
      this.ageDecals(level);
      if (!(level instanceof ClientLevel clientLevel)) {
         return;
      }
      double range = Math.max(64.0, mc.options.getEffectiveRenderDistance() * 16.0);
      for (Entity entity : clientLevel.entitiesForRendering()) {
         if (entity instanceof LivingEntity living
            && !(living instanceof DroneEntity)
            && !(living instanceof Player player && FPVMod.getActiveDroneId(player.getUUID()).isPresent())
            && living.onGround()) {
            if (living.distanceToSqr(mc.player) > range * range) {
               continue;
            }
            double dx = living.getX() - living.xOld;
            double dz = living.getZ() - living.zOld;
            if (dx * dx + dz * dz < MIN_MOVE_SQR) {
               continue;
            }
            EntityFootState state = this.entityStates.computeIfAbsent(living.getUUID(), id -> new EntityFootState());
            float footLength = living.getBbWidth() * FOOT_SIZE;
            float stride = footLength * STRIDE;
            double moved = Math.sqrt((living.getX() - state.lastX) * (living.getX() - state.lastX) + (living.getZ() - state.lastZ) * (living.getZ() - state.lastZ));
            if (moved >= stride || state.lastTick == 0L) {
               this.depositFootprint(living, state, gameTime);
               state.lastTick = gameTime;
               state.lastX = living.getX();
               state.lastZ = living.getZ();
            }
         }
      }

      if (gameTime % 100L == 0L) {
         this.entityStates.entrySet().removeIf(e -> gameTime - e.getValue().lastTick > LIFETIME_TICKS);
      }
   }

   private void depositFootprint(LivingEntity entity, EntityFootState state, long gameTime) {
      float width = entity.getBbWidth();
      float footSize = width * FOOT_SIZE;
      float spread = width * FOOT_SPREAD;
      float bodyRot = entity.yBodyRot;
      float angle = (float)(-Math.toRadians(bodyRot));
      double forwardX = -Math.sin(Math.toRadians(bodyRot));
      double forwardZ = Math.cos(Math.toRadians(bodyRot));
      double sideX = -forwardZ;
      double sideZ = forwardX;
      state.leftFoot = !state.leftFoot;
      double side = state.leftFoot ? -1.0 : 1.0;
      double x = entity.getX() + sideX * spread * side;
      double z = entity.getZ() + sideZ * spread * side;
      double y = entity.getY() + 0.005;
      this.addDecal(new FootprintDecal(x, y, z, footSize, footSize, angle, 1.0F, gameTime));
   }

   private void addDecal(FootprintDecal decal) {
      this.decals.addLast(decal);
      while (this.decals.size() > MAX_DECALS) {
         this.decals.pollFirst();
      }
   }

   private void ageDecals(Level level) {
      var it = this.decals.iterator();
      while (it.hasNext()) {
         FootprintDecal decal = it.next();
         decal.age++;
         if (decal.age >= LIFETIME_TICKS) {
            it.remove();
         } else {
            BlockPos below = BlockPos.containing(decal.x, decal.y - 0.01, decal.z);
            if (!level.getBlockState(below).isSolidRender()) {
               it.remove();
            }
         }
      }
   }

   public Iterable<FootprintDecal> decals() {
      return this.decals;
   }

   public int getDecalCount() {
      return this.decals.size();
   }

   public void clear() {
      this.decals.clear();
      this.entityStates.clear();
   }

   static class EntityFootState {
      long lastTick;
      double lastX;
      double lastZ;
      boolean leftFoot;
   }

   public static class FootprintDecal {
      public final double x;
      public final double y;
      public final double z;
      public final float width;
      public final float length;
      public final float angle;
      public final float initialIntensity;
      public final long birthTick;
      public int age;

      public FootprintDecal(double x, double y, double z, float width, float length, float angle, float initialIntensity, long birthTick) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.width = width;
         this.length = length;
         this.angle = angle;
         this.initialIntensity = initialIntensity;
         this.birthTick = birthTick;
      }

      public float getIntensity() {
         return this.initialIntensity * (1.0F - this.age / (float)LIFETIME_TICKS);
      }
   }
}
