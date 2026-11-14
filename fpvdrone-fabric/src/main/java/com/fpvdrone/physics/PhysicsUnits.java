package com.fpvdrone.physics;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class PhysicsUnits {
   public static final float v5suI = 20.0F;

   public static Vector3f toPhysicsVelocity(Vec3 var0) {
      return new Vector3f((float)var0.x * 20.0F, (float)var0.y * 20.0F, (float)var0.z * 20.0F);
   }

   public static Vec3 toMinecraftVelocity(Vector3f var0) {
      return new Vec3(var0.x / 20.0F, var0.y / 20.0F, var0.z / 20.0F);
   }
}
