package com.fpvdrone.physics;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class PhysicsUnits {
    public static final float TICKS_PER_SECOND = 20.0F;

    public static Vector3f toPhysicsVelocity(Vec3 minecraftVelocity) {
        return new Vector3f((float)minecraftVelocity.x * 20.0F, (float)minecraftVelocity.y * 20.0F, (float)minecraftVelocity.z * 20.0F);
    }

    public static Vec3 toMinecraftVelocity(Vector3f physicsVelocity) {
        return new Vec3(physicsVelocity.x / 20.0F, physicsVelocity.y / 20.0F, physicsVelocity.z / 20.0F);
    }
}
