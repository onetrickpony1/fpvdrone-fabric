package com.fpvdrone.physics;

import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.Transforms;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DefaultPhysicsCore implements IPhysicsCore {
   private static final float v5suI = (float) (Math.PI / 180.0);
   private Vector3f ibzfxic = new Vector3f(0.0F, 0.0F, 0.0F);
   private Vector3f droneLook = new Vector3f(0.0F, 0.0F, 1.0F);
   private Vector3f dOfFwvo = new Vector3f(0.0F, 1.0F, 0.0F);
   private Vector3f droneLeft = new Vector3f(1.0F, 0.0F, 0.0F);
   private float mass = 1.05F;
   private float motorKv = 1300.0F;
   private int batteryCells = 6;
   private float propDiameter = 0.2286F;
   private float propPitch = 0.1143F;
   private float dragCoefficient = 1.1F;
   private boolean flightMode3d = false;
   private boolean gpsMode = false;
   private boolean hoverHold = false;
   private static final float cXHvq = 6.0F;
   private static final float rxYnaa = 30.0F;
   private static final float r6s7FL3 = 7.0F;
   private static final float iAKNPAj = 0.25F;
   private static final float qlv6NKWnH = 0.35F;
   private static final float hOKMYHOaC = 6.0F;
   private static final float iS1JnZov = 4.0F;
   private static final float lfyfLwuw = 0.05F;
   private static final float wyNDWDB = 3.0F;
   private float iRWc2Pi = 0.0F;
   private boolean yawReferenceSet = false;
   private float eJDY = 0.0F;
   private float zHBAQgZP = 0.0F;
   private float thrustMultiplier = 1.0F;
   private float reverseThrustFactor = 0.65F;
   private boolean motorsArmed = false;
   /** Turtle mode switch (crash flip). */
   private boolean turtleMode = false;
   /** Set when turtle mode switched itself off after flipping the drone upright; read once by the client. */
   private boolean turtleFlipped = false;
   private float turtleMousePitch = 0.0F;
   private float turtleMouseRoll = 0.0F;
   private float kickYaw = 0.0F;
   private float ifpFOCaAs = 0.0F;
   private float kickRoll = 0.0F;
   private static final float ivDR = 0.85F;
   private float kickPitch = 0.0F;
   private float wBXn = 0.0F;
   private static final float mM7DsB9AR = 0.28F;
   private static final float uKpZYj = 9.0F;
   private float smoothYawRate = 0.0F;
   private float xYttSy = 0.0F;
   private float smoothRollRate = 0.0F;
   private static final float fvsAOg = 0.04F;
   private float[] rcRate = new float[]{1.0F, 1.15F, 1.15F, 1.15F};
   private float[] superRate = new float[]{0.7F, 0.67F, 0.67F, 0.67F};
   private float[] expo = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
   private boolean falling = false;
   private float fallingThrustFactor = 0.0F;
   private float idXga = 1.0F;
   private float angVelYaw = 0.0F;
   private float angVelPitch = 0.0F;
   private float iKXOcc = 0.0F;
   private static final float jt9oI60ST = 1.2F;
   private boolean returningHome = false;
   /** Port addition: GPS Rescue on a non-GPS quad (GPS module): flies on the GPS model while returning home. */
   private boolean gpsRescue = false;
   private boolean uj1PcjLg = false;
   private int zgQgCA9 = 0;
   private float qV33e;
   private float xogV5KBhg;
   private float rYlh;
   private float sZzOK7T;
   private float kNWIu = 0.0F;
   private float wTo0I = 0.0F;
   private float hXya = 0.0F;
   private float angVelRoll = 0.0F;
   private boolean iY41Hgn0m = false;
   private static final float nwFPfQ = 4.0F;
   private static final float qz71Ga = 2.0F;
   private static final float dZAuCP = 10.0F;
   private static final float p9T0hGf = 12.0F;
   private static final float i7EP1lKl = 0.6F;
   private static final float dReHaJ = 1.5F;
   private static final float jm7PbrhC = 1.2F;
   private static final float jfdtPQG = 120.0F;
   private static final double urdCiPZD = 8.0;
   private static final float gQiZ = 4.0F;
   private static final float nTdR = 0.196F;
   private static final float be9wl = 1.1F;
   private static final float nL8hNS4cc = 3.7F;
   private PhysicsState physicsState;

   public DefaultPhysicsCore(float var1, float var2, float var3, float var4, int var5) {
      this.mass = var1;
      this.motorKv = var2;
      this.propDiameter = var3;
      this.dragCoefficient = var4;
      this.batteryCells = var5;
   }

   /** Rotor drag coefficient (blade flapping), 1/s: about 0.4 for a 5 inch quad (docs/WIND.md). */
   private static final float ROTOR_DRAG = 0.4F;
   private static final Vector3f NO_WIND = new Vector3f();

   private Vector3f wind() {
      return this.physicsState != null ? this.physicsState.getWind() : NO_WIND;
   }

   /**
    * Port addition (docs/WIND.md): a GPS drone leans into the wind like a real one holding position: the extra
    * pitch/roll whose sideways thrust cancels the wind's push on a hovering drone (body drag of the GPS model plus
    * rotor drag). Zero in still air. Above 30 degrees the drone cannot hold and drifts.
    */
   private void leanIntoWind() {
      Vector3f w = this.wind();
      float speed = (float)Math.sqrt(w.x * w.x + w.z * w.z);
      if (speed < 0.01F) {
         return;
      }
      float push = 0.132055F * 0.25F * speed * speed + this.mass * ROTOR_DRAG * speed;
      float lean = (float)Math.toDegrees(Math.atan(push / (this.mass * 9.80665F)));
      Vector3f fwd = new Vector3f(this.droneLook.x, 0.0F, this.droneLook.z);
      if (fwd.lengthSquared() < 1.0E-6F) {
         fwd.set(0.0F, 0.0F, 1.0F);
      }
      fwd.normalize();
      Vector3f right = new Vector3f(-this.droneLeft.x, 0.0F, -this.droneLeft.z);
      if (right.lengthSquared() < 1.0E-6F) {
         right.set(1.0F, 0.0F, 0.0F);
      }
      right.normalize();
      float ux = -w.x / speed;
      float uz = -w.z / speed;
      this.eJDY = clamp(this.eJDY + lean * (ux * fwd.x + uz * fwd.z), -30.0F, 30.0F);
      this.zHBAQgZP = clamp(this.zHBAQgZP + lean * (ux * right.x + uz * right.z), -30.0F, 30.0F);
   }

   /** Rotor drag from the wind alone: m * d * (wind across the rotor disc). */
   private Vector3f rotorDrag(Vector3f wind) {
      if (wind.lengthSquared() < 1.0E-6F) {
         return new Vector3f();
      }
      Vector3f across = new Vector3f(wind).sub(new Vector3f(this.dOfFwvo).mul(wind.dot(this.dOfFwvo)));
      return across.mul(this.mass * ROTOR_DRAG);
   }

   public void setPhysicsState(PhysicsState var1) {
      this.physicsState = var1;
   }

   public void setRcRate(int var1, float var2) {
      if (var1 >= 0 && var1 < 4) {
         this.rcRate[var1] = var2;
      }
   }

   public void setSuperRate(int var1, float var2) {
      if (var1 >= 0 && var1 < 4) {
         this.superRate[var1] = var2;
      }
   }

   public void setExpo(int var1, float var2) {
      if (var1 >= 0 && var1 < 4) {
         this.expo[var1] = var2;
      }
   }

   @Override
   public void step(float var1) {
      if (this.falling) {
         this.fallingStep(var1);
      } else {
         if (this.returningHome && this.motorsArmed) {
            this.b69s();
         }

         this.staticStep(var1);
         boolean var2 = this.physicsState != null && this.physicsState.isThrottleCut();
         if (this.physicsState != null && this.physicsState.isGroundContact() && !var2) {
            this.groundStep(var1);
         } else if (this.motorsArmed) {
            if (this.gpsFlight()) {
               this.gpsStep(var1);
            } else {
               this.rotateDrone(var1);
            }

            if (this.physicsState != null && this.isCameraInsideBlock()) {
               this.levelDrone(var1);
            }
         }
      }
   }

   /**
    * On the ground. 1.1.8 always rights the drone ({@link #levelDrone}). With the turtle mode config on, a drone
    * resting more than 90 degrees over settles flat on its back instead, and only turtle mode flips it back.
    */
   private void groundStep(float dt) {
      if (!com.fpvdrone.FpvConfig.isTurtleMode() || this.dOfFwvo.y >= 0.0F) {
         this.levelDrone(dt);
         if (this.turtleMode && this.dOfFwvo.y > 0.7F) {
            // Upright again: turtle mode switches itself off (Betaflight 4.5 crash-flip auto re-arm).
            this.turtleMode = false;
            this.turtleFlipped = true;
         }
      } else if (this.turtleMode && this.motorsArmed) {
         this.turtleStep(dt);
      } else {
         this.settleTowards(new Vector3f(0.0F, -1.0F, 0.0F), dt);
      }
   }

   /** Rests the drone flat with its up vector at {@code target} (levelDrone with any target). */
   private void settleTowards(Vector3f target, float dt) {
      float t = Math.min(1.0F, 12.0F * dt);
      this.dOfFwvo.lerp(target, t);
      this.dOfFwvo.normalize();
      this.droneLeft = cross(this.dOfFwvo, this.droneLook);
      if (this.droneLeft.lengthSquared() < 1.0E-4F) {
         this.droneLeft = cross(this.dOfFwvo, new Vector3f(0.0F, 0.0F, 1.0F));
      }

      this.droneLeft.normalize();
      this.droneLook = cross(this.droneLeft, this.dOfFwvo);
      this.droneLook.normalize();
   }

   /**
    * Turtle mode (Betaflight "flip over after crash"): the sticks spin one side's motors in reverse and roll the
    * drone over that edge. Only the stronger of pitch and roll acts (or yaw alone, which spins it on its back);
    * the throttle is not used. Up to 270 degrees per second at full stick, slower for heavier drones.
    */
   private void turtleStep(float dt) {
      float yaw;
      float pitch;
      float roll;
      if (ControllerReader.getInstance().isPhysicalControllerConnected()) {
         yaw = ControllerReader.getYaw();
         pitch = ControllerReader.getPitch();
         roll = ControllerReader.getRoll();
      } else {
         // Mouse movement arrives in bursts (once per mouse event, not every physics step): hold it as a stick
         // position that falls back to centre over about 0.15 s.
         float decay = (float)Math.exp(-dt / 0.15F);
         this.turtleMousePitch = Mth.clamp(this.turtleMousePitch * decay + this.physicsState.getMouseDeltaPitch() * 0.067F, -1.0F, 1.0F);
         this.turtleMouseRoll = Mth.clamp(this.turtleMouseRoll * decay + this.physicsState.getMouseDeltaYaw() * 0.067F, -1.0F, 1.0F);
         this.physicsState.clearMouseDelta();
         pitch = this.turtleMousePitch;
         roll = this.turtleMouseRoll;
         yaw = ControllerReader.getYaw();
      }

      float deadband = 0.15F;
      float rate = 270.0F * (float)Math.sqrt(1.05F / this.mass) * dt * (float) (Math.PI / 180.0);
      Quaternionf turn;
      if (Math.abs(pitch) >= Math.abs(roll) && Math.abs(pitch) > deadband) {
         turn = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLeft), pitch * rate);
      } else if (Math.abs(roll) > deadband) {
         turn = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLook), roll * rate);
      } else if (Math.abs(yaw) > deadband) {
         turn = new Quaternionf().fromAxisAngleRad(new Vector3f(this.dOfFwvo), -yaw * rate);
      } else {
         this.settleTowards(new Vector3f(0.0F, -1.0F, 0.0F), dt);
         return;
      }

      turn.transform(this.droneLook);
      turn.transform(this.dOfFwvo);
      turn.transform(this.droneLeft);
      this.droneLook.normalize();
      this.dOfFwvo.normalize();
      this.droneLeft = cross(this.dOfFwvo, this.droneLook);
      this.droneLeft.normalize();
   }

   private boolean isCameraInsideBlock() {
      if (this.physicsState == null) {
         return false;
      }

      float var1 = 0.30889937F;
      float var2 = 0.22122687F;
      double var3 = this.physicsState.getPlayerX() + this.dOfFwvo.x * var2 + this.droneLook.x * var1;
      double var5 = this.physicsState.getPlayerY() + this.dOfFwvo.y * var2 + this.droneLook.y * var1;
      double var7 = this.physicsState.getPlayerZ() + this.dOfFwvo.z * var2 + this.droneLook.z * var1;
      return this.physicsState.isPointInsideSolidBlock(var3, var5, var7);
   }

   private void levelDrone(float var1) {
      Vector3f var2 = new Vector3f(0.0F, 1.0F, 0.0F);
      float var3 = 12.0F;
      float var4 = Math.min(1.0F, var3 * var1);
      this.dOfFwvo.lerp(var2, var4);
      this.dOfFwvo.normalize();
      this.droneLeft = cross(this.dOfFwvo, this.droneLook);
      if (this.droneLeft.lengthSquared() < 1.0E-4F) {
         this.droneLeft = cross(this.dOfFwvo, new Vector3f(0.0F, 0.0F, 1.0F));
      }

      this.droneLeft.normalize();
      this.droneLook = cross(this.droneLeft, this.dOfFwvo);
      this.droneLook.normalize();
   }

   private void fallingStep(float var1) {
      float var2 = this.angVelYaw * var1 * (float) (Math.PI / 180.0);
      float var3 = this.angVelPitch * var1 * (float) (Math.PI / 180.0);
      float var4 = this.iKXOcc * var1 * (float) (Math.PI / 180.0);
      Quaternionf var5 = new Quaternionf().fromAxisAngleRad(new Vector3f(this.dOfFwvo), -var2);
      var5.transform(this.droneLook);
      var5.transform(this.droneLeft);
      Quaternionf var6 = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLeft), var3);
      var6.transform(this.droneLook);
      var6.transform(this.dOfFwvo);
      Quaternionf var7 = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLook), var4);
      var7.transform(this.dOfFwvo);
      var7.transform(this.droneLeft);
      this.droneLook.normalize();
      this.dOfFwvo.normalize();
      this.droneLeft.normalize();
      this.droneLeft = cross(this.dOfFwvo, this.droneLook);
      this.droneLeft.normalize();
      float var8 = (float)Math.exp(-var1 / 1.2F);
      this.angVelYaw *= var8;
      this.angVelPitch *= var8;
      this.iKXOcc *= var8;
      Vector3f var9 = PhysicsState.getGravity();
      float var10 = 0.132055F;
      var10 *= this.idXga;
      float var11 = this.batteryCells * 3.7F;
      float var12 = 2.04F * (this.propDiameter / 0.2286F) * (this.propPitch / 0.1143F) * (this.motorKv / 1300.0F) * (var11 / 22.2F);
      float var13 = var12 * 4.0F;
      float var14 = var13 / this.mass * 9.80665F * this.thrustMultiplier;
      float var15 = 0.0078125F;
      int var16 = (int)(var1 / var15) + 10;
      float var17 = 0.0F;

      for (int var18 = 0; var18 < var16 && var1 - var17 > 1.0E-5F; var18++) {
         float var19 = var1 - var17;
         float var20 = Math.min(var15, var19);
         Vector3f air = new Vector3f(this.ibzfxic).sub(this.wind());
         float var21 = air.length();
         Vector3f var22 = var21 > 1.0E-4F ? new Vector3f(air).normalize() : new Vector3f(0.0F, 0.0F, 0.0F);
         Vector3f var23 = new Vector3f(var22).mul(-var21 * var21 * var10);
         Vector3f var24 = new Vector3f(var9).mul(this.mass);
         Vector3f var25 = new Vector3f(var24).add(var23);
         float var26 = this.fallingThrustFactor * var14;
         Vector3f var27 = new Vector3f(this.dOfFwvo).mul(var26);
         Vector3f var28 = new Vector3f(var25).div(this.mass);
         var28.add(var27);
         Vector3f var29 = new Vector3f(this.ibzfxic).add(new Vector3f(var28).mul(var20));
         if (this.physicsState != null) {
            this.ibzfxic = this.physicsState.collideAndMove(var29, var20);
         } else {
            this.ibzfxic = var29;
         }

         float var30 = 500.0F;
         if (this.ibzfxic.length() > var30) {
            this.ibzfxic.normalize().mul(var30);
         }

         if (Float.isNaN(this.ibzfxic.x) || Float.isNaN(this.ibzfxic.y) || Float.isNaN(this.ibzfxic.z)) {
            this.ibzfxic.set(0.0F, 0.0F, 0.0F);
         }

         var17 += var20;
      }
   }

   private void staticStep(float var1) {
      Vector3f var2 = PhysicsState.getGravity();
      float var3 = this.motorsArmed ? ControllerReader.getThrottle() : -1.0F;
      if (this.turtleMode) {
         // Turtle mode: the motors spin in reverse to flip the drone; they give no lift.
         var3 = -1.0F;
      }
      if (this.physicsState != null && this.physicsState.isThrottleCut()) {
         var3 = -1.0F;
      }

      float var4 = 0.132055F;
      if (this.gpsFlight()) {
         var4 *= 0.25F;
      }

      boolean var6 = false;
      float var32;
      if (this.flightMode3d) {
         var32 = clamp(var3, -1.0F, 1.0F);
         var6 = var32 < 0.0F;
         var32 = Math.abs(var32);
      } else {
         var32 = clamp((var3 + 1.0F) * 0.5F, 0.0F, 1.0F);
      }

      float var7 = 0.0078125F;
      int var8 = (int)(var1 / var7) + 10;
      float var9 = 0.0F;

      for (int var10 = 0; var10 < var8 && var1 - var9 > 1.0E-5F; var10++) {
         float var11 = var1 - var9;
         float var12 = Math.min(var7, var11);
         boolean var13 = this.physicsState != null && this.physicsState.isThrottleCut();
         float var14 = var13 ? 0.0F : var32;
         boolean var15 = var13 ? false : var6;
         // Port addition (docs/WIND.md): drag acts on the air-relative velocity; rotor drag pushes the drone along
         // the part of the wind across its rotors. Both vanish in still air (= 1.1.8).
         Vector3f wind = this.wind();
         Vector3f air = new Vector3f(this.ibzfxic).sub(wind);
         float var16 = air.length();
         Vector3f var17 = var16 > 1.0E-4F ? new Vector3f(air).normalize() : new Vector3f(0.0F, 0.0F, 0.0F);
         Vector3f var18 = new Vector3f(var17).mul(-var16 * var16 * var4);
         Vector3f var19 = new Vector3f(var2).mul(this.mass);
         Vector3f var20 = new Vector3f(var19).add(var18).add(this.rotorDrag(wind));
         float var21 = this.batteryCells * 3.7F;
         float var22 = 2.04F * (this.propDiameter / 0.2286F) * (this.propPitch / 0.1143F) * (this.motorKv / 1300.0F) * (var21 / 22.2F);
         float var23 = var22 * 4.0F;
         float var24 = var23 * var14;
         Vector3f var25;
         if (this.gpsFlight()) {
            float var26;
            if (!var13 && this.motorsArmed) {
               float var27 = Math.max(0.3F, this.dOfFwvo.y);
               float var28 = Math.abs(var3) < 0.05F ? 0.0F : var3;
               float var29 = var28 * 6.0F;
               if (this.returningHome) {
                  var29 = this.kNWIu;
               }

               float var30 = 29.41995F;
               float var31 = clamp((var29 - this.ibzfxic.y) * 4.0F, -var30, var30);
               var26 = (9.80665F + var31) / var27;
               var26 = clamp(var26, 0.0F, var30);
            } else {
               var26 = 0.0F;
            }

            var25 = new Vector3f(this.dOfFwvo).mul(var26);
         } else {
            float var34 = var24 / this.mass * 9.80665F * this.thrustMultiplier;
            if (var15) {
               var34 *= this.reverseThrustFactor;
            }

            Vector3f var36 = var15 ? new Vector3f(this.dOfFwvo).negate() : new Vector3f(this.dOfFwvo);
            var25 = new Vector3f(var36).mul(var34);
         }

         Vector3f var35 = new Vector3f(var20).div(this.mass);
         var35.add(var25);
         Vector3f var37 = new Vector3f(this.ibzfxic).add(new Vector3f(var35).mul(var12));
         if (this.physicsState != null) {
            this.ibzfxic = this.physicsState.collideAndMove(var37, var12);
         } else {
            this.ibzfxic = var37;
         }

         float var38 = 500.0F;
         if (this.ibzfxic.length() > var38) {
            this.ibzfxic.normalize().mul(var38);
         }

         if (Float.isNaN(this.ibzfxic.x) || Float.isNaN(this.ibzfxic.y) || Float.isNaN(this.ibzfxic.z)) {
            this.ibzfxic.set(0.0F, 0.0F, 0.0F);
         }

         var9 += var12;
      }
   }

   private void rotateDrone(float var1) {
      boolean var2 = this.physicsState != null && this.physicsState.isThrottleCut();
      boolean var3 = var2 || !this.motorsArmed;
      float var4;
      float var5;
      float var6;
      if (ControllerReader.getInstance().isPhysicalControllerConnected()) {
         var4 = var3 ? 0.0F : ControllerReader.getYaw();
         var5 = var3 ? 0.0F : ControllerReader.getPitch();
         var6 = var3 ? 0.0F : ControllerReader.getRoll();
      } else if (this.physicsState != null && !var3) {
         float var7 = this.physicsState.getMouseDeltaYaw();
         float var8 = this.physicsState.getMouseDeltaPitch();
         this.physicsState.clearMouseDelta();
         float var9 = 0.067F;
         var5 = Mth.clamp(var8 * var9, -1.0F, 1.0F);
         var6 = Mth.clamp(var7 * var9, -1.0F, 1.0F);
         var4 = ControllerReader.getYaw();
      } else {
         var4 = 0.0F;
         var5 = 0.0F;
         var6 = 0.0F;
      }

      float var20 = Transforms.bfRate(var4, this.rcRate[1], this.superRate[1], this.expo[1]);
      float var21 = Transforms.bfRate(var5, this.rcRate[2], this.superRate[2], this.expo[2]);
      float var22 = Transforms.bfRate(var6, this.rcRate[3], this.superRate[3], this.expo[3]);
      float var10 = var1 / (var1 + 0.04F);
      this.smoothYawRate = this.smoothYawRate + var10 * (var20 - this.smoothYawRate);
      this.xYttSy = this.xYttSy + var10 * (var21 - this.xYttSy);
      this.smoothRollRate = this.smoothRollRate + var10 * (var22 - this.smoothRollRate);
      float var11 = (float)Math.sqrt(1.05F / this.mass);
      float var12 = this.smoothYawRate * var11 * var1 * (float) (Math.PI / 180.0);
      float var13 = this.xYttSy * var11 * var1 * (float) (Math.PI / 180.0);
      float var14 = this.smoothRollRate * var11 * var1 * (float) (Math.PI / 180.0);
      if (Math.abs(this.kickYaw) > 0.01F || Math.abs(this.ifpFOCaAs) > 0.01F || Math.abs(this.kickRoll) > 0.01F) {
         var12 += this.kickYaw * var1 * (float) (Math.PI / 180.0);
         var13 += this.ifpFOCaAs * var1 * (float) (Math.PI / 180.0);
         var14 += this.kickRoll * var1 * (float) (Math.PI / 180.0);
         float var15 = var2 ? 0.96F : 0.85F;
         this.kickYaw *= var15;
         this.ifpFOCaAs *= var15;
         this.kickRoll *= var15;
      }

      Quaternionf var23 = new Quaternionf().fromAxisAngleRad(new Vector3f(this.dOfFwvo), -var12);
      var23.transform(this.droneLook);
      var23.transform(this.droneLeft);
      Vector3f var16 = new Vector3f(this.droneLook);
      Vector3f var17 = new Vector3f(this.dOfFwvo);
      Quaternionf var18 = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLeft), var13);
      var18.transform(this.droneLook);
      var18.transform(this.dOfFwvo);
      if (this.physicsState != null && this.isCameraInsideBlock()) {
         this.droneLook.set(var16);
         this.dOfFwvo.set(var17);
      }

      Quaternionf var19 = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLook), var14);
      var19.transform(this.dOfFwvo);
      var19.transform(this.droneLeft);
      this.droneLook.normalize();
      this.dOfFwvo.normalize();
      this.droneLeft.normalize();
      this.droneLeft = cross(this.dOfFwvo, this.droneLook);
      this.droneLeft.normalize();
   }

   private void gpsStep(float var1) {
      boolean var2 = this.physicsState != null && this.physicsState.isThrottleCut();
      boolean var3 = var2 || !this.motorsArmed;
      float var5 = 0.0F;
      float var4;
      if (this.returningHome && !var2) {
         Vector3f var22 = new Vector3f(this.droneLook.x, 0.0F, this.droneLook.z);
         if (var22.lengthSquared() < 1.0E-6F) {
            var22.set(0.0F, 0.0F, 1.0F);
         }

         var22.normalize();
         Vector3f var24 = new Vector3f(-this.droneLeft.x, 0.0F, -this.droneLeft.z);
         if (var24.lengthSquared() < 1.0E-6F) {
            var24.set(1.0F, 0.0F, 0.0F);
         }

         var24.normalize();
         float var26 = this.ibzfxic.x * var22.x + this.ibzfxic.z * var22.z;
         float var28 = this.ibzfxic.x * var24.x + this.ibzfxic.z * var24.z;
         float var10 = this.wTo0I * var22.x + this.hXya * var22.z;
         float var11 = this.wTo0I * var24.x + this.hXya * var24.z;
         this.eJDY = clamp(6.0F * (var10 - var26), -30.0F, 30.0F);
         this.zHBAQgZP = clamp(6.0F * (var11 - var28), -30.0F, 30.0F);
         if (this.physicsState != null) {
            this.physicsState.clearMouseDelta();
         }

         var4 = 0.0F;
      } else if (var3) {
         this.eJDY *= 0.9F;
         this.zHBAQgZP *= 0.9F;
         var4 = 0.0F;
      } else if (this.hoverHold) {
         Vector3f var6 = new Vector3f(this.droneLook.x, 0.0F, this.droneLook.z);
         if (var6.lengthSquared() < 1.0E-6F) {
            var6.set(0.0F, 0.0F, 1.0F);
         }

         var6.normalize();
         Vector3f var7 = new Vector3f(-this.droneLeft.x, 0.0F, -this.droneLeft.z);
         if (var7.lengthSquared() < 1.0E-6F) {
            var7.set(1.0F, 0.0F, 0.0F);
         }

         var7.normalize();
         float var8 = this.ibzfxic.x * var6.x + this.ibzfxic.z * var6.z;
         float var9 = this.ibzfxic.x * var7.x + this.ibzfxic.z * var7.z;
         this.eJDY = clamp(-6.0F * var8, -30.0F, 30.0F);
         this.zHBAQgZP = clamp(-6.0F * var9, -30.0F, 30.0F);
         if (this.physicsState != null) {
            this.physicsState.clearMouseDelta();
         }

         var4 = ControllerReader.getYaw();
      } else if (ControllerReader.getInstance().isPhysicalControllerConnected()) {
         this.eJDY = ControllerReader.getPitch() * 30.0F;
         this.zHBAQgZP = ControllerReader.getRoll() * 30.0F;
         var4 = ControllerReader.getYaw();
      } else if (this.physicsState != null) {
         this.eJDY = ControllerReader.getPitch() * 30.0F;
         this.zHBAQgZP = ControllerReader.getRoll() * 30.0F;
         float var21 = this.physicsState.getMouseDeltaYaw();
         this.physicsState.clearMouseDelta();
         var5 = var21 * 0.35F;
         var4 = 0.0F;
      } else {
         var4 = 0.0F;
      }

      if (!var3) {
         this.leanIntoWind();
      }

      if (!this.yawReferenceSet) {
         this.iRWc2Pi = (float)Math.atan2(-this.droneLook.x, this.droneLook.z);
         this.yawReferenceSet = true;
         var5 = 0.0F;
      }

      float var23 = Transforms.bfRate(var4, this.rcRate[1], this.superRate[1], this.expo[1]);
      float var25 = var1 / (var1 + 0.04F);
      this.smoothYawRate = this.smoothYawRate + var25 * (var23 - this.smoothYawRate);
      float var27 = (float)Math.sqrt(1.05F / this.mass);
      this.iRWc2Pi = this.iRWc2Pi + this.smoothYawRate * var27 * var1 * (float) (Math.PI / 180.0);
      this.iRWc2Pi += var5 * (float) (Math.PI / 180.0);
      if (this.returningHome && this.iY41Hgn0m) {
         this.iRWc2Pi = dOfFwvo(this.iRWc2Pi, this.angVelRoll, (float) (Math.PI * 2.0 / 3.0) * var1);
      }

      float var29 = this.eJDY * (float) (Math.PI / 180.0);
      float var30 = this.zHBAQgZP * (float) (Math.PI / 180.0);
      Vector3f var31 = new Vector3f(0.0F, 0.0F, 1.0F);
      Vector3f var12 = new Vector3f(0.0F, 1.0F, 0.0F);
      Vector3f var13 = new Vector3f(1.0F, 0.0F, 0.0F);
      Quaternionf var14 = new Quaternionf().fromAxisAngleRad(0.0F, 1.0F, 0.0F, -this.iRWc2Pi);
      var14.transform(var31);
      var14.transform(var13);
      Quaternionf var15 = new Quaternionf().fromAxisAngleRad(var13, var29);
      var15.transform(var31);
      var15.transform(var12);
      Quaternionf var16 = new Quaternionf().fromAxisAngleRad(var31, var30);
      var16.transform(var12);
      var16.transform(var13);
      var31.normalize();
      var12.normalize();
      float var17 = Math.min(1.0F, 7.0F * var1);
      this.droneLook.lerp(var31, var17).normalize();
      this.dOfFwvo.lerp(var12, var17).normalize();
      this.droneLeft = cross(this.dOfFwvo, this.droneLook);
      if (this.droneLeft.lengthSquared() < 1.0E-4F) {
         this.droneLeft = cross(this.dOfFwvo, new Vector3f(0.0F, 0.0F, 1.0F));
      }

      this.droneLeft.normalize();
      this.droneLook = cross(this.droneLeft, this.dOfFwvo);
      this.droneLook.normalize();
      if (Math.abs(this.kickPitch) > 0.01F || Math.abs(this.wBXn) > 0.01F) {
         Quaternionf var18 = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLeft), this.kickPitch * (float) (Math.PI / 180.0));
         var18.transform(this.droneLook);
         var18.transform(this.dOfFwvo);
         Quaternionf var19 = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLook), this.wBXn * (float) (Math.PI / 180.0));
         var19.transform(this.dOfFwvo);
         var19.transform(this.droneLeft);
         this.droneLook.normalize();
         this.dOfFwvo.normalize();
         this.droneLeft = cross(this.dOfFwvo, this.droneLook);
         this.droneLeft.normalize();
         this.droneLook = cross(this.droneLeft, this.dOfFwvo);
         this.droneLook.normalize();
         float var20 = (float)Math.exp(-var1 / 0.28F);
         this.kickPitch *= var20;
         this.wBXn *= var20;
      }
   }

   private static Vector3f cross(Vector3f var0, Vector3f var1) {
      float var2 = var0.y * var1.z - var0.z * var1.y;
      float var3 = var0.z * var1.x - var0.x * var1.z;
      float var4 = var0.x * var1.y - var0.y * var1.x;
      return new Vector3f(var2, var3, var4);
   }

   @Override
   public Vector3f getVelocity() {
      return this.ibzfxic;
   }

   @Override
   public void setVelocity(Vector3f var1) {
      this.ibzfxic = new Vector3f(var1);
   }

   @Override
   public Vector3f getDroneLook() {
      return this.droneLook;
   }

   @Override
   public void setDroneLook(Vector3f var1) {
      this.droneLook = new Vector3f(var1);
   }

   @Override
   public Vector3f getDroneUp() {
      return this.dOfFwvo;
   }

   @Override
   public void setDroneUp(Vector3f var1) {
      this.dOfFwvo = new Vector3f(var1);
   }

   @Override
   public Vector3f getDroneLeft() {
      return this.droneLeft;
   }

   @Override
   public void setDroneLeft(Vector3f var1) {
      this.droneLeft = new Vector3f(var1);
   }

   @Override
   public void reset() {
      this.ibzfxic = new Vector3f(0.0F, 0.0F, 0.0F);
      this.droneLook = new Vector3f(0.0F, 0.0F, 1.0F);
      this.dOfFwvo = new Vector3f(0.0F, 1.0F, 0.0F);
      this.droneLeft = new Vector3f(1.0F, 0.0F, 0.0F);
      this.yawReferenceSet = false;
      this.eJDY = 0.0F;
      this.zHBAQgZP = 0.0F;
      this.hoverHold = false;
      this.turtleMode = false;
      this.turtleFlipped = false;
      this.returningHome = false;
      this.gpsRescue = false;
      this.uj1PcjLg = false;
      this.zgQgCA9 = 0;
      this.kickPitch = 0.0F;
      this.wBXn = 0.0F;
   }

   public void startReturnHome(float var1, float var2, float var3) {
      this.qV33e = var1;
      this.xogV5KBhg = var2;
      this.rYlh = var3;
      this.returningHome = true;
      this.uj1PcjLg = false;
      float var4 = this.physicsState != null ? (float)this.physicsState.getPlayerY() : var2;
      this.sZzOK7T = Math.max(var4, var2 + 12.0F);
      this.zgQgCA9 = var4 >= this.sZzOK7T - 0.5F ? 1 : 0;
   }

   /** GPS model flight: a GPS drone, or a quad on GPS Rescue. */
   private boolean gpsFlight() {
      return this.gpsMode || this.gpsRescue && this.returningHome;
   }

   /** Return home as a GPS Rescue (quad with a GPS module); the quad flies acro again once it ends. */
   public void startGpsRescue(float x, float y, float z) {
      this.startReturnHome(x, y, z);
      this.gpsRescue = !this.gpsMode;
      this.hoverHold = false;
   }

   public boolean isGpsRescue() {
      return this.gpsRescue && this.returningHome;
   }

   public void stopReturnHome() {
      this.returningHome = false;
      this.gpsRescue = false;
   }

   public boolean isReturningHome() {
      return this.returningHome;
   }

   public int getReturnHomePhase() {
      return this.zgQgCA9;
   }

   public boolean consumeReturnHomeArrived() {
      if (this.uj1PcjLg) {
         this.uj1PcjLg = false;
         return true;
      } else {
         return false;
      }
   }

   private void b69s() {
      if (this.physicsState == null) {
         this.returningHome = false;
         this.gpsRescue = false;
      } else {
         float var1 = (float)this.physicsState.getPlayerX();
         float var2 = (float)this.physicsState.getPlayerY();
         float var3 = (float)this.physicsState.getPlayerZ();
         float var4 = this.qV33e - var1;
         float var5 = this.rYlh - var3;
         float var6 = (float)Math.sqrt(var4 * var4 + var5 * var5);
         if (var6 > 0.5F) {
            this.angVelRoll = (float)Math.atan2(-var4, var5);
            this.iY41Hgn0m = true;
         } else {
            this.iY41Hgn0m = false;
         }

         switch (this.zgQgCA9) {
            case 0:
               this.wTo0I = 0.0F;
               this.hXya = 0.0F;
               this.kNWIu = 4.0F;
               if (var2 >= this.sZzOK7T - 0.5F) {
                  this.zgQgCA9 = 1;
               }
               break;
            case 1:
               float var14 = var6 > 0.001F ? var4 / var6 : 0.0F;
               float var8 = var6 > 0.001F ? var5 / var6 : 0.0F;
               double var9 = this.physicsState.getTerrainHeightAhead(var14, var8, Math.min(var6, 8.0));
               if (var9 > Double.NEGATIVE_INFINITY) {
                  float var11 = (float)(var9 + 4.0);
                  if (var11 > this.sZzOK7T) {
                     this.sZzOK7T = var11;
                  }
               }

               this.kNWIu = clamp((this.sZzOK7T - var2) * 1.2F, -2.0F, 4.0F);
               float var15 = this.sZzOK7T - var2;
               float var12 = var15 > 3.0F ? 0.0F : (var15 > 1.0F ? 0.4F : 1.0F);
               float var13 = clamp(var6 * 0.6F, 0.0F, 10.0F) * var12;
               if (var6 > 0.001F) {
                  this.wTo0I = var14 * var13;
                  this.hXya = var8 * var13;
               } else {
                  this.wTo0I = 0.0F;
                  this.hXya = 0.0F;
               }

               if (var6 < 1.5F) {
                  this.zgQgCA9 = 2;
               }
               break;
            default:
               float var7 = clamp(var6 * 0.6F, 0.0F, 2.0F);
               if (var6 > 0.001F) {
                  this.wTo0I = var4 / var6 * var7;
                  this.hXya = var5 / var6 * var7;
               } else {
                  this.wTo0I = 0.0F;
                  this.hXya = 0.0F;
               }

               this.kNWIu = -2.0F;
               if (this.physicsState.isGroundContact() || var2 <= this.xogV5KBhg + 0.3F) {
                  this.returningHome = false;
                  this.gpsRescue = false;
                  this.uj1PcjLg = true;
                  this.kNWIu = 0.0F;
                  this.wTo0I = 0.0F;
                  this.hXya = 0.0F;
               }
         }
      }
   }

   private static float dOfFwvo(float var0, float var1, float var2) {
      float var3 = var1 - var0;

      while (var3 > (float) Math.PI) {
         var3 -= (float) (Math.PI * 2);
      }

      while (var3 < (float) -Math.PI) {
         var3 += (float) (Math.PI * 2);
      }

      return Math.abs(var3) <= var2 ? var1 : var0 + Math.signum(var3) * var2;
   }

   private static float clamp(float var0, float var1, float var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   public void setMass(float var1) {
      this.mass = var1;
   }

   public void setMotorKv(float var1) {
      this.motorKv = var1;
   }

   public void setDragCoefficient(float var1) {
      this.dragCoefficient = var1;
   }

   public void setBatteryCells(int var1) {
      this.batteryCells = var1;
   }

   public void setFlightMode3d(boolean var1) {
      this.flightMode3d = var1;
   }

   public void setGpsMode(boolean var1) {
      this.gpsMode = var1;
   }

   public void setHoverHold(boolean var1) {
      this.hoverHold = var1;
   }

   public boolean isHoverHold() {
      return this.hoverHold;
   }

   public void setThrustMultiplier(float var1) {
      this.thrustMultiplier = var1;
   }

   public void setPropDiameter(float var1) {
      this.propDiameter = var1;
   }

   public void setPropPitch(float var1) {
      this.propPitch = var1;
   }

   public void setMotorsArmed(boolean var1) {
      this.motorsArmed = var1;
   }

   public boolean isMotorsArmed() {
      return this.motorsArmed;
   }

   public void setTurtleMode(boolean on) {
      this.turtleMode = on;
   }

   public boolean isTurtleMode() {
      return this.turtleMode;
   }

   /** True once after turtle mode flipped the drone upright and switched itself off. */
   public boolean consumeTurtleFlipped() {
      boolean flipped = this.turtleFlipped;
      this.turtleFlipped = false;
      return flipped;
   }

   @Override
   public void setFalling(boolean var1) {
      this.falling = var1;
   }

   @Override
   public boolean isFalling() {
      return this.falling;
   }

   @Override
   public void setFallingParams(float var1, float var2) {
      this.fallingThrustFactor = var1;
      this.idXga = var2;
   }

   @Override
   public void applyImpulse(Vector3f var1) {
      this.ibzfxic.add(var1);
   }

   public void applyRotationalKick(float var1, float var2, float var3) {
      this.kickYaw += var1;
      this.ifpFOCaAs += var2;
      this.kickRoll += var3;
   }

   public void applyReleaseKick(float var1, float var2) {
      if (this.gpsFlight()) {
         this.kickPitch += var1;
         this.wBXn += var2;
      } else {
         this.applyRotationalKick(0.0F, var1 * 9.0F, var2 * 9.0F);
      }
   }

   @Override
   public void setAngularVelocity(float var1, float var2, float var3) {
      this.angVelYaw = var1;
      this.angVelPitch = var2;
      this.iKXOcc = var3;
   }
}
