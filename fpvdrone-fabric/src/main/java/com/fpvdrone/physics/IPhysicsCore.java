package com.fpvdrone.physics;

import org.joml.Vector3f;

public interface IPhysicsCore {
   void step(float var1);

   Vector3f getVelocity();

   void setVelocity(Vector3f var1);

   Vector3f getDroneLook();

   void setDroneLook(Vector3f var1);

   Vector3f getDroneUp();

   void setDroneUp(Vector3f var1);

   Vector3f getDroneLeft();

   void setDroneLeft(Vector3f var1);

   void reset();

   void setFalling(boolean var1);

   boolean isFalling();

   void setFallingParams(float var1, float var2);

   void applyImpulse(Vector3f var1);

   void setAngularVelocity(float var1, float var2, float var3);
}
