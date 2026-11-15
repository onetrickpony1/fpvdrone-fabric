package com.fpvdrone.physics;

import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroppedBombEntity;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhysicsState {
   private static final Logger LOGGER = LoggerFactory.getLogger(PhysicsState.class);
   private static final float ibzfxic = 0.5F;
   private IPhysicsCore core;
   private Player dOfFwvo;
   private PhysicsState.KKeMvpuBgEY rsDd50tfV;
   private PhysicsState.Ol3TEDSog uSUb;
   private float jGhI2 = 0.0F;
   private static final float wEJx3Itb = 0.5F;
   private float bAcdvq9 = 0.0F;
   private UUID c1jHYPb4;
   private float mass = 1.05F;
   private float motorKv = 1300.0F;
   private int batteryCells = 6;
   private float propDiameter = 0.2286F;
   private float propPitch = 0.1143F;
   private float rxYnaa = 1.1F;
   private boolean r6s7FL3 = false;
   private boolean gpsMode = false;
   private float thrustMultiplier = 0.45F;
   private boolean groundContact = false;
   private boolean groundContactThisTick = false;
   private float throttleCutTimer = 0.0F;
   private static final float wyNDWDB = 0.8F;
   private float lastPlayerYaw = 0.0F;
   private float lastPlayerPitch = 0.0F;
   private float mouseDeltaYaw = 0.0F;
   private float mouseDeltaPitch = 0.0F;
   private float pxmzA;
   private static Class<?> ivBuilderClass;
   private static Field ivCollisionBoxesField;
   private static Method ivGetBoxesMethod;
   private static Method ivConvertMethod;
   private static boolean ivCollisionReflectionFailed = false;

   public PhysicsState(Player var1) {
      this.dOfFwvo = var1;
      this.core = this.createCore();
      this.lastPlayerYaw = var1.getYRot();
      this.lastPlayerPitch = var1.getXRot();
      // 1.1.8 also sampled a 32-step lookup table (named ThermalState, unrelated to thermal) into pxmzA, which is
      // never read; left out.
   }

   private IPhysicsCore createCore() {
      DefaultPhysicsCore var1 = new DefaultPhysicsCore(this.mass, this.motorKv, this.propDiameter, this.rxYnaa, this.batteryCells);
      var1.setFlightMode3d(this.r6s7FL3);
      var1.setGpsMode(this.gpsMode);
      var1.setThrustMultiplier(this.thrustMultiplier);
      var1.setPhysicsState(this);
      return var1;
   }

   /** Port addition: the wind at the drone this tick (m/s) and the smoothed shelter (docs/WIND.md). */
   private final Vector3f wind = new Vector3f();
   private float shelter = 0.0F;

   /** The wind at the drone this tick, m/s (zero with wind off). */
   public Vector3f getWind() {
      return this.wind;
   }

   private void sampleWind(float dt) {
      if (this.dOfFwvo == null || this.dOfFwvo.level() == null || !com.fpvdrone.FpvConfig.isWindEnabled()) {
         this.wind.set(0.0F, 0.0F, 0.0F);
         return;
      }
      net.minecraft.world.level.Level level = this.dOfFwvo.level();
      double x = this.getPlayerX();
      double y = this.getPlayerY();
      double z = this.getPlayerZ();
      com.fpvdrone.physics.wind.FpvWind.base(level, x, y, z, this.wind);
      float target = com.fpvdrone.physics.wind.FpvWind.shelter(level, x, y, z, this.wind);
      // smoothed over about 0.3 s so flying past a post does not flick the wind on and off
      this.shelter += (target - this.shelter) * Math.min(1.0F, dt / 0.3F);
      com.fpvdrone.physics.wind.FpvWind.at(level, x, y, z, this.shelter, this.wind);
   }

   public void tick(float var1) {
      this.sampleWind(var1);
      this.bAcdvq9 += var1;
      this.groundContactThisTick = false;
      if (this.jGhI2 > 0.0F) {
         this.jGhI2 = Math.max(0.0F, this.jGhI2 - var1);
      }

      if (this.throttleCutTimer > 0.0F) {
         this.throttleCutTimer = Math.max(0.0F, this.throttleCutTimer - var1);
      }

      this.core.step(var1);
      this.groundContact = this.groundContactThisTick;
   }

   public void setOnCollision(PhysicsState.KKeMvpuBgEY var1) {
      this.rsDd50tfV = var1;
   }

   public void setOnEntityHit(PhysicsState.Ol3TEDSog var1) {
      this.uSUb = var1;
   }

   public boolean isGroundContact() {
      return this.groundContact;
   }

   public boolean isThrottleCut() {
      return this.throttleCutTimer > 0.0F;
   }

   public boolean isPointInsideSolidBlock(double var1, double var3, double var5) {
      if (this.dOfFwvo != null && this.dOfFwvo.level() != null) {
         BlockPos var7 = BlockPos.containing(var1, var3, var5);
         BlockState var8 = this.dOfFwvo.level().getBlockState(var7);
         VoxelShape var9 = var8.getCollisionShape(this.dOfFwvo.level(), var7);
         if (var9.isEmpty()) {
            return false;
         }

         double var10 = var1 - var7.getX();
         double var12 = var3 - var7.getY();
         double var14 = var5 - var7.getZ();

         for (AABB var17 : var9.toAabbs()) {
            if (var10 >= var17.minX
               && var10 <= var17.maxX
               && var12 >= var17.minY
               && var12 <= var17.maxY
               && var14 >= var17.minZ
               && var14 <= var17.maxZ) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public double getPlayerX() {
      return this.dOfFwvo != null ? this.dOfFwvo.getX() : 0.0;
   }

   public double getPlayerY() {
      return this.dOfFwvo != null ? this.dOfFwvo.getY() : 0.0;
   }

   public double getPlayerZ() {
      return this.dOfFwvo != null ? this.dOfFwvo.getZ() : 0.0;
   }

   public double getTerrainHeightAhead(double var1, double var3, double var5) {
      if (this.dOfFwvo != null && this.dOfFwvo.level() != null) {
         double var7 = Double.NEGATIVE_INFINITY;

         for (double var9 = 1.0; var9 <= var5; var9++) {
            int var11 = Mth.floor(this.dOfFwvo.getX() + var1 * var9);
            int var12 = Mth.floor(this.dOfFwvo.getZ() + var3 * var9);
            int var13 = this.dOfFwvo.level().getHeight(Types.MOTION_BLOCKING, var11, var12);
            if (var13 > var7) {
               var7 = var13;
            }
         }

         return var7;
      } else {
         return Double.NEGATIVE_INFINITY;
      }
   }

   private static Vec3 v5suI(boolean var0, boolean var1, boolean var2, Vector3f var3, BlockHitResult var4) {
      double var5 = var0 ? (var3.x > 0.0F ? -1.0 : 1.0) : 0.0;
      double var7 = var1 ? (var3.y > 0.0F ? -1.0 : 1.0) : 0.0;
      double var9 = var2 ? (var3.z > 0.0F ? -1.0 : 1.0) : 0.0;
      Vec3 var11 = new Vec3(var5, var7, var9);
      if (var11.lengthSqr() > 1.0E-8) {
         return var11.normalize();
      } else if (var4 != null && var4.getType() == Type.BLOCK) {
         Vec3i var13 = var4.getDirection().getUnitVec3i();
         return new Vec3(var13.getX(), var13.getY(), var13.getZ());
      } else {
         Vec3 var12 = new Vec3(var3.x, var3.y, var3.z);
         return var12.lengthSqr() > 1.0E-8 ? var12.normalize().scale(-1.0) : new Vec3(0.0, 1.0, 0.0);
      }
   }

   public Vector3f collideAndMove(Vector3f var1, float var2) {
      if (this.dOfFwvo != null && this.dOfFwvo.level() != null) {
         float var3 = 500.0F;
         if (var1.length() > var3) {
            var1.normalize().mul(var3);
         }

         Vector3f var4 = new Vector3f(var1).mul(var2);
         Vec3 var5 = new Vec3(var4.x, var4.y, var4.z);
         double var6 = var5.length();
         float var8 = 0.28F;
         BlockHitResult var9 = null;
         double var10 = 1.0;
         if (var6 > 0.001) {
            double var12 = this.dOfFwvo.getX();
            double var14 = this.dOfFwvo.getY();
            double var16 = this.dOfFwvo.getZ();
            Vec3 var18 = new Vec3(var12, var14 + var8 * 0.5, var16);
            Vec3 var19 = var18.add(var5);
            ClipContext var20 = new ClipContext(var18, var19, Block.COLLIDER, Fluid.NONE, this.dOfFwvo);
            BlockHitResult var21 = this.dOfFwvo.level().clip(var20);
            if (var21.getType() == Type.BLOCK) {
               double var22 = var18.distanceTo(var21.getLocation());
               double var24 = 0.02;
               double var26 = Math.max(0.0, var22 - var24);
               double var28 = var26 / var6;
               if (var28 < var10) {
                  var10 = var28;
                  var9 = var21;
               }
            }

            if (var10 < 1.0) {
               var5 = var5.scale(var10);
            }
         }

         AABB var45 = new AABB(
            this.dOfFwvo.getX() - 0.275F,
            this.dOfFwvo.getY(),
            this.dOfFwvo.getZ() - 0.275F,
            this.dOfFwvo.getX() + 0.275F,
            this.dOfFwvo.getY() + 0.28F,
            this.dOfFwvo.getZ() + 0.275F
         );
         Vec3 var13 = collideBoundingBoxWithDroneSize(this.dOfFwvo, var5, var45);
         boolean var46 = this.bAcdvq9 > 0.5F;
         Vector3f var15 = new Vector3f(var1);
         float var47 = 0.001F;
         boolean var17 = Math.abs((float)(var5.x - var13.x)) > var47;
         boolean var48 = Math.abs((float)(var5.y - var13.y)) > var47;
         boolean var49 = Math.abs((float)(var5.z - var13.z)) > var47;
         boolean var50 = var17 || var48 || var49;
         float var51 = var1.length();
         if (var50 && this.rsDd50tfV != null && var46) {
            float var52 = var1.length();
            Vec3 var23 = v5suI(var17, var48, var49, var1, var9);
            this.rsDd50tfV.onCollision(var52, (float)var23.x, (float)var23.y, (float)var23.z);
         }

         if (this.rsDd50tfV != null && var46 && var51 > 3.0F) {
            this.checkEntityCollision(var45, var51);
         }

         if (var50) {
            boolean var53 = var17 || var49;
            float var56 = var17 ? Math.signum(var1.x) : 0.0F;
            float var57 = var48 ? Math.signum(var1.y) : 0.0F;
            float var25 = var49 ? Math.signum(var1.z) : 0.0F;
            float var58 = (float)Math.sqrt(var56 * var56 + var57 * var57 + var25 * var25);
            if (var58 > 0.001F) {
               var56 /= var58;
               var57 /= var58;
               var25 /= var58;
            }

            float var27 = var15.x * var56 + var15.y * var57 + var15.z * var25;
            if (var27 > 0.0F) {
               float var59 = var53 ? 0.3F : 0.2F;
               var15.x -= (1.0F + var59) * var27 * var56;
               var15.y -= (1.0F + var59) * var27 * var57;
               var15.z -= (1.0F + var59) * var27 * var25;
            }

            float var60 = 0.85F;
            float var29 = var15.x * var56 + var15.y * var57 + var15.z * var25;
            float var30 = var15.x - var29 * var56;
            float var31 = var15.y - var29 * var57;
            float var32 = var15.z - var29 * var25;
            var30 *= var60;
            var31 *= var60;
            var32 *= var60;
            float var33 = 0.5F;
            var29 *= var33;
            var15.x = var30 + var29 * var56;
            var15.y = var31 + var29 * var57;
            var15.z = var32 + var29 * var25;
            float var34 = var53 ? 8.0F : 10.0F;
            if (var51 > var34 && var46 && this.isMotorsArmed()) {
               this.throttleCutTimer = 0.8F;
            }

            if (var51 > 8.0F && var46 && this.isMotorsArmed() && this.core instanceof DefaultPhysicsCore var35) {
               float var66 = Math.min((var51 - 8.0F) / 12.0F, 1.0F);
               float var37 = var66 * var66;
               float var38 = 1500.0F;
               float var39 = (float)Math.sqrt(var30 * var30 + var31 * var31 + var32 * var32);
               float var40 = 0.0F;
               float var41 = 0.0F;
               if (var39 > 0.1F) {
                  var41 = var32 / var39 * 0.5F;
                  var40 = var30 / var39 * 0.3F;
               }

               float var42 = ((float)Math.random() - 0.5F + var40) * var38 * var37;
               float var43 = ((float)Math.random() - 0.5F + var41) * var38 * var37;
               float var44 = ((float)Math.random() - 0.5F) * var38 * var37;
               var35.applyRotationalKick(var42, var43, var44);
            }

            float var65 = 0.03F;
            if (var17) {
               this.dOfFwvo
                  .setPos(
                     this.dOfFwvo.getX() + var13.x - Math.signum(var1.x) * var65,
                     this.dOfFwvo.getY() + var13.y,
                     this.dOfFwvo.getZ() + var13.z
                  );
            } else if (var49) {
               this.dOfFwvo
                  .setPos(
                     this.dOfFwvo.getX() + var13.x,
                     this.dOfFwvo.getY() + var13.y,
                     this.dOfFwvo.getZ() + var13.z - Math.signum(var1.z) * var65
                  );
            } else {
               this.dOfFwvo
                  .setPos(this.dOfFwvo.getX() + var13.x, this.dOfFwvo.getY() + var13.y, this.dOfFwvo.getZ() + var13.z);
            }

            this.dOfFwvo.setOldPosAndRot();
         } else {
            this.dOfFwvo.setPos(this.dOfFwvo.getX() + var13.x, this.dOfFwvo.getY() + var13.y, this.dOfFwvo.getZ() + var13.z);
            this.dOfFwvo.setOldPosAndRot();
         }

         if ((var17 || var49) && var51 < 3.0F) {
            float var54 = 0.4F;
            var15.y *= var54;
         }

         if (var48 && var5.y <= 0.0) {
            this.groundContactThisTick = true;
            float var55 = 0.87F;
            var15.x *= var55;
            var15.z *= var55;
            if (Math.abs(var15.x) < 0.05F) {
               var15.x = 0.0F;
            }

            if (Math.abs(var15.z) < 0.05F) {
               var15.z = 0.0F;
            }
         }

         return var15;
      } else {
         return var1;
      }
   }

   private static Vec3 collideBoundingBoxWithDroneSize(Entity var0, Vec3 var1, AABB var2) {
      List var3 = var0.level().getEntityCollisions(var0, var2.expandTowards(var1));
      List var4 = getIVCollisionBoxes(var0, var2.expandTowards(var1));
      if (var4 != null && !var4.isEmpty()) {
         ArrayList var5 = new ArrayList(var3);
         var5.addAll(var4);
         var3 = var5;
      }

      return var1.lengthSqr() == 0.0 ? var1 : Entity.collideBoundingBox(var0, var1, var2, var0.level(), var3);
   }

   private static List<VoxelShape> getIVCollisionBoxes(Entity var0, AABB var1) {
      if (ivCollisionReflectionFailed) {
         return null;
      }

      try {
         if (ivBuilderClass == null) {
            ivBuilderClass = Class.forName("mcinterface1201.BuilderEntityExisting");
            ivCollisionBoxesField = ivBuilderClass.getField("collisionBoxes");
            Class var2 = Class.forName("mcinterface1201.WrapperAABBCollective");
            ivGetBoxesMethod = var2.getMethod("getBoxes");
            Class var3 = Class.forName("mcinterface1201.WrapperWorld");
            Class var4 = Class.forName("minecrafttransportsimulator.baseclasses.BoundingBox");
            ivConvertMethod = var3.getMethod("convert", var4);
         }

         ArrayList var13 = null;

         for (Entity var15 : var0.level().getEntities(var0, var1, ivBuilderClass::isInstance)) {
            Object var5 = ivCollisionBoxesField.get(var15);
            if (var5 != null && (!(var5 instanceof AABB var6) || var6.intersects(var1))) {
               AABB var16 = (AABB)var5;
               if (var16.intersects(var1)) {
                  for (Object var9 : (Collection)ivGetBoxesMethod.invoke(var5)) {
                     AABB var10 = (AABB)ivConvertMethod.invoke(null, var9);
                     if (var10.intersects(var1)) {
                        if (var13 == null) {
                           var13 = new ArrayList();
                        }

                        var13.add(Shapes.create(var10));
                     }
                  }
               }
            }
         }

         return var13;
      } catch (ClassNotFoundException var11) {
         ivCollisionReflectionFailed = true;
         return null;
      } catch (Exception var12) {
         ivCollisionReflectionFailed = true;
         LogUtils.getLogger().warn("[FPV] Could not get IV collision boxes via reflection", var12);
         return null;
      }
   }

   private void checkEntityCollision(AABB var1, float var2) {
      if (this.dOfFwvo != null && this.dOfFwvo.level() != null) {
         Vector3f var3 = this.core.getVelocity();
         Vec3 var4 = PhysicsUnits.toMinecraftVelocity(var3);
         AABB var5 = var1.expandTowards(var4).inflate(0.2);
         List var6 = this.dOfFwvo
            .level()
            .getEntities(
               this.dOfFwvo,
               var5,
               var1x -> var1x.isAlive()
                  && var1x != this.dOfFwvo
                  && !(var1x instanceof DroneEntity var2x && var2x.getUUID().equals(this.c1jHYPb4))
                  && !(var1x instanceof DroppedBombEntity)
                  && !var1x.isSpectator()
                  && var1x.isPickable()
            );
         if (!var6.isEmpty()) {
            Entity var7 = (Entity)var6.get(0);
            double var8 = var7.getX() - this.dOfFwvo.getX();
            double var10 = var7.getY() + var7.getBbHeight() * 0.5 - (this.dOfFwvo.getY() + 0.14F);
            double var12 = var7.getZ() - this.dOfFwvo.getZ();
            double var14 = Math.sqrt(var8 * var8 + var10 * var10 + var12 * var12);
            if (var14 < 0.001) {
               var14 = 1.0;
            }

            float var16 = (float)(var8 / var14);
            float var17 = (float)(var10 / var14);
            float var18 = (float)(var12 / var14);
            float var19 = var3.x * var16 + var3.y * var17 + var3.z * var18;
            if (var19 > 0.0F) {
               float var20 = 0.3F;
               var3.x -= (1.0F + var20) * var19 * var16;
               var3.y -= (1.0F + var20) * var19 * var17;
               var3.z -= (1.0F + var20) * var19 * var18;
               var3.mul(0.6F);
               this.core.setVelocity(var3);
            }

            if (var2 > 8.0F && this.isMotorsArmed()) {
               this.throttleCutTimer = 0.8F;
            }

            if (var2 > 5.0F && this.isMotorsArmed() && this.core instanceof DefaultPhysicsCore var29) {
               float var31 = Math.min((var2 - 5.0F) / 12.0F, 1.0F);
               float var22 = var31 * var31;
               float var23 = 1200.0F;
               var29.applyRotationalKick(
                  ((float)Math.random() - 0.5F) * var23 * var22, ((float)Math.random() - 0.5F) * var23 * var22, ((float)Math.random() - 0.5F) * var23 * var22
               );
            }

            if (this.uSUb != null && var2 >= 5.0F && this.jGhI2 <= 0.0F) {
               this.jGhI2 = 0.5F;
               Vector3f var30 = this.core.getVelocity();
               double var32 = this.dOfFwvo.getX();
               double var33 = this.dOfFwvo.getY();
               double var25 = this.dOfFwvo.getZ();

               for (Entity var28 : (List<Entity>)var6) {
                  this.uSUb.onHit(var28.getUUID(), var2, var30.x, var30.y, var30.z, var32, var33, var25);
               }
            }
         }
      }
   }

   public void initFromDrone(DroneEntity var1) {
      this.bAcdvq9 = 0.0F;
      this.c1jHYPb4 = var1.getUUID();
      float var2 = var1.getYRot();
      float var3 = var1.getXRot();
      float var4 = var1.getDroneRoll();
      this.dOfFwvo(var2, var3, var4);
      Vec3 var5 = var1.getDeltaMovement();
      this.core.setVelocity(PhysicsUnits.toPhysicsVelocity(var5));
   }

   private void dOfFwvo(float var1, float var2, float var3) {
      float var4 = (float)Math.toRadians(-var1);
      float var5 = (float)Math.toRadians(var2);
      float var6 = (float)Math.toRadians(var3);
      Quaternionf var7 = new Quaternionf();
      var7.rotationYXZ(var4, var5, var6);
      Vector3f var8 = var7.transform(new Vector3f(0.0F, 0.0F, 1.0F));
      Vector3f var9 = var7.transform(new Vector3f(0.0F, 1.0F, 0.0F));
      Vector3f var10 = new Vector3f();
      var9.cross(var8, var10);
      var10.normalize();
      this.core.setDroneLook(var8);
      this.core.setDroneUp(var9);
      this.core.setDroneLeft(var10);
      this.core.setVelocity(new Vector3f(0.0F, 0.0F, 0.0F));
   }

   public void syncBasisFromAngles(float var1, float var2, float var3) {
      float var4 = (float)Math.toRadians(-var1);
      float var5 = (float)Math.toRadians(var2);
      float var6 = (float)Math.toRadians(var3);
      Quaternionf var7 = new Quaternionf();
      var7.rotationYXZ(var4, var5, var6);
      Vector3f var8 = var7.transform(new Vector3f(0.0F, 0.0F, 1.0F));
      Vector3f var9 = var7.transform(new Vector3f(0.0F, 1.0F, 0.0F));
      Vector3f var10 = new Vector3f();
      var9.cross(var8, var10);
      var10.normalize();
      this.core.setDroneLook(var8);
      this.core.setDroneUp(var9);
      this.core.setDroneLeft(var10);
   }

   public Vector3f getVelocity() {
      return this.core.getVelocity();
   }

   public Vec3 getVelocityVec3() {
      Vector3f var1 = this.core.getVelocity();
      return new Vec3(var1.x, var1.y, var1.z);
   }

   public void setVelocity(Vec3 var1) {
      this.core.setVelocity(new Vector3f((float)var1.x, (float)var1.y, (float)var1.z));
   }

   public Vector3f getDroneLook() {
      return this.core.getDroneLook();
   }

   public Vector3f getDroneUp() {
      return this.core.getDroneUp();
   }

   public Vector3f getDroneLeft() {
      return this.core.getDroneLeft();
   }

   public static Vector3f getGravity() {
      return new Vector3f(0.0F, -9.80665F, 0.0F);
   }

   public void setMass(float var1) {
      this.mass = var1;
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setMass(var1);
      }
   }

   public void applyReleaseKick(float var1, float var2) {
      if (this.core instanceof DefaultPhysicsCore var3) {
         var3.applyReleaseKick(var1, var2);
      }
   }

   public void setHoverHold(boolean var1) {
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setHoverHold(var1);
      }
   }

   public boolean isHoverHold() {
      return this.core instanceof DefaultPhysicsCore var1 ? var1.isHoverHold() : false;
   }

   /** Puts the drone in the given attitude (look and up need not be exactly perpendicular). */
   public void setDroneOrientation(org.joml.Vector3f look, org.joml.Vector3f up) {
      if (this.core != null) {
         org.joml.Vector3f u = new org.joml.Vector3f(up).normalize();
         org.joml.Vector3f left = new org.joml.Vector3f(u).cross(look).normalize();
         org.joml.Vector3f l = new org.joml.Vector3f(left).cross(u).normalize();
         this.core.setDroneUp(u);
         this.core.setDroneLeft(left);
         this.core.setDroneLook(l);
      }
   }

   public void setTurtleMode(boolean on) {
      if (this.core instanceof DefaultPhysicsCore core) {
         core.setTurtleMode(on);
      }
   }

   public boolean isTurtleMode() {
      return this.core instanceof DefaultPhysicsCore core && core.isTurtleMode();
   }

   public boolean consumeTurtleFlipped() {
      return this.core instanceof DefaultPhysicsCore core && core.consumeTurtleFlipped();
   }

   public void startReturnHome(double var1, double var3, double var5) {
      if (this.core instanceof DefaultPhysicsCore var7) {
         var7.startReturnHome((float)var1, (float)var3, (float)var5);
      }
   }

   public void startGpsRescue(double x, double y, double z) {
      if (this.core instanceof DefaultPhysicsCore core) {
         core.startGpsRescue((float)x, (float)y, (float)z);
      }
   }

   public boolean isGpsRescue() {
      return this.core instanceof DefaultPhysicsCore core && core.isGpsRescue();
   }

   public void stopReturnHome() {
      if (this.core instanceof DefaultPhysicsCore var1) {
         var1.stopReturnHome();
      }
   }

   public boolean isReturningHome() {
      return this.core instanceof DefaultPhysicsCore var1 && var1.isReturningHome();
   }

   public int getReturnHomePhase() {
      return this.core instanceof DefaultPhysicsCore var1 ? var1.getReturnHomePhase() : 0;
   }

   public boolean consumeReturnHomeArrived() {
      return this.core instanceof DefaultPhysicsCore var1 && var1.consumeReturnHomeArrived();
   }

   public void setMotorKv(float var1) {
      this.motorKv = var1;
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setMotorKv(var1);
      }
   }

   public void setBatteryCells(int var1) {
      this.batteryCells = var1;
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setBatteryCells(var1);
      }
   }

   public void setPropDiameter(float var1) {
      this.propDiameter = var1;
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setPropDiameter(var1);
      }
   }

   public void setPropPitch(float var1) {
      this.propPitch = var1;
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setPropPitch(var1);
      }
   }

   public void setDragCoefficient(float var1) {
      this.rxYnaa = var1;
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setDragCoefficient(var1);
      }
   }

   public void setFlightMode3d(boolean var1) {
      this.r6s7FL3 = var1;
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setFlightMode3d(var1);
      }
   }

   public void setGpsMode(boolean var1) {
      this.gpsMode = var1;
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setGpsMode(var1);
      }
   }

   public void setThrustMultiplier(float var1) {
      this.thrustMultiplier = var1;
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setThrustMultiplier(var1);
      }
   }

   public void setRcRate(int var1, float var2) {
      if (this.core instanceof DefaultPhysicsCore var3) {
         var3.setRcRate(var1, var2);
      }
   }

   public void setSuperRate(int var1, float var2) {
      if (this.core instanceof DefaultPhysicsCore var3) {
         var3.setSuperRate(var1, var2);
      }
   }

   public void setExpo(int var1, float var2) {
      if (this.core instanceof DefaultPhysicsCore var3) {
         var3.setExpo(var1, var2);
      }
   }

   public boolean isMotorsArmed() {
      return this.core instanceof DefaultPhysicsCore var1 ? var1.isMotorsArmed() : false;
   }

   public void setMotorsArmed(boolean var1) {
      if (!this.isFalling()) {
         if (this.core instanceof DefaultPhysicsCore var2) {
            var2.setMotorsArmed(var1);
         }
      }
   }

   public void setFalling(boolean var1) {
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.setFalling(var1);
      }
   }

   public boolean isFalling() {
      return this.core instanceof DefaultPhysicsCore var1 ? var1.isFalling() : false;
   }

   public void setFallingParams(float var1, float var2) {
      if (this.core instanceof DefaultPhysicsCore var3) {
         var3.setFallingParams(var1, var2);
      }
   }

   public void applyImpulse(Vector3f var1) {
      if (this.core instanceof DefaultPhysicsCore var2) {
         var2.applyImpulse(var1);
      }
   }

   public void setAngularVelocity(float var1, float var2, float var3) {
      if (this.core instanceof DefaultPhysicsCore var4) {
         var4.setAngularVelocity(var1, var2, var3);
      }
   }

   public void applyRotationalKick(float var1, float var2, float var3) {
      if (this.core instanceof DefaultPhysicsCore var4) {
         var4.applyRotationalKick(var1, var2, var3);
      }
   }

   public float getLastPlayerYaw() {
      return this.lastPlayerYaw;
   }

   public void setLastPlayerYaw(float var1) {
      this.lastPlayerYaw = var1;
   }

   public float getLastPlayerPitch() {
      return this.lastPlayerPitch;
   }

   public void setLastPlayerPitch(float var1) {
      this.lastPlayerPitch = var1;
   }

   public float getMouseDeltaYaw() {
      return this.mouseDeltaYaw;
   }

   public void setMouseDeltaYaw(float var1) {
      this.mouseDeltaYaw = var1;
   }

   public float getMouseDeltaPitch() {
      return this.mouseDeltaPitch;
   }

   public void setMouseDeltaPitch(float var1) {
      this.mouseDeltaPitch = var1;
   }

   public void clearMouseDelta() {
      this.mouseDeltaYaw = 0.0F;
      this.mouseDeltaPitch = 0.0F;
   }

   @FunctionalInterface
   public interface KKeMvpuBgEY {
      void onCollision(float var1, float var2, float var3, float var4);
   }

   @FunctionalInterface
   public interface Ol3TEDSog {
      void onHit(UUID var1, float var2, float var3, float var4, float var5, double var6, double var8, double var10);
   }
}
