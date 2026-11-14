package com.fpvdrone.physics;

import com.fpvdrone.entity.DroneEntity;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
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
    private static final int COLLISION_GRACE_TICKS = 10;
    private IPhysicsCore core;
    private Player player;
    private Consumer<Float> onCollision;
    private PhysicsState.EntityHitCallback onEntityHit;
    private int entityHitCooldown = 0;
    private static final int ENTITY_HIT_COOLDOWN_TICKS = 10;
    private int ticksSinceInit = 0;
    private float mass = 1.05F;
    private float motorKv = 1300.0F;
    private int batteryCells = 6;
    private float propDiameter = 0.2286F;
    private float propPitch = 0.1143F;
    private float dragCoefficient = 1.1F;
    private boolean flightMode3d = false;
    private float thrustMultiplier = 0.45F;
    private boolean groundContact = false;
    private boolean groundContactThisTick = false;
    private float throttleCutTimer = 0.0F;
    private static final float THROTTLE_CUT_DURATION = 0.8F;
    private float lastPlayerYaw = 0.0F;
    private float lastPlayerPitch = 0.0F;
    private float mouseDeltaYaw = 0.0F;
    private float mouseDeltaPitch = 0.0F;
    private static Class<?> ivBuilderClass;
    private static Field ivCollisionBoxesField;
    private static Method ivGetBoxesMethod;
    private static Method ivConvertMethod;
    private static boolean ivCollisionReflectionFailed = false;

    public PhysicsState(Player player) {
        this.player = player;
        this.core = this.createCore();
        this.lastPlayerYaw = player.getYRot();
        this.lastPlayerPitch = player.getXRot();
    }

    private IPhysicsCore createCore() {
        DefaultPhysicsCore dpc = new DefaultPhysicsCore(this.mass, this.motorKv, this.propDiameter, this.dragCoefficient, this.batteryCells);
        dpc.setFlightMode3d(this.flightMode3d);
        dpc.setThrustMultiplier(this.thrustMultiplier);
        dpc.setPhysicsState(this);
        return dpc;
    }

    public void tick(float dt) {
        this.ticksSinceInit++;
        this.groundContactThisTick = false;
        if (this.entityHitCooldown > 0) {
            this.entityHitCooldown--;
        }

        if (this.throttleCutTimer > 0.0F) {
            this.throttleCutTimer = Math.max(0.0F, this.throttleCutTimer - dt);
        }

        this.core.step(dt);
        this.groundContact = this.groundContactThisTick;
    }

    public void setOnCollision(Consumer<Float> callback) {
        this.onCollision = callback;
    }

    public void setOnEntityHit(PhysicsState.EntityHitCallback callback) {
        this.onEntityHit = callback;
    }

    public boolean isGroundContact() {
        return this.groundContact;
    }

    public boolean isThrottleCut() {
        return this.throttleCutTimer > 0.0F;
    }

    public boolean isPointInsideSolidBlock(double x, double y, double z) {
        if (this.player != null && this.player.level() != null) {
            BlockPos pos = BlockPos.containing(x, y, z);
            BlockState state = this.player.level().getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(this.player.level(), pos);
            if (shape.isEmpty()) {
                return false;
            } else {
                double lx = x - pos.getX();
                double ly = y - pos.getY();
                double lz = z - pos.getZ();

                for (AABB box : shape.toAabbs()) {
                    if (lx >= box.minX && lx <= box.maxX && ly >= box.minY && ly <= box.maxY && lz >= box.minZ && lz <= box.maxZ) {
                        return true;
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    public double getPlayerX() {
        return this.player != null ? this.player.getX() : 0.0;
    }

    public double getPlayerY() {
        return this.player != null ? this.player.getY() : 0.0;
    }

    public double getPlayerZ() {
        return this.player != null ? this.player.getZ() : 0.0;
    }

    public Vector3f collideAndMove(Vector3f nextVelocity, float elapsed) {
        if (this.player != null && this.player.level() != null) {
            float limit = 500.0F;
            if (nextVelocity.length() > limit) {
                nextVelocity.normalize().mul(limit);
            }

            Vector3f displacement = new Vector3f(nextVelocity).mul(elapsed);
            Vec3 desired = new Vec3(displacement.x, displacement.y, displacement.z);
            double desiredLen = desired.length();
            float h = 0.28F;
            BlockHitResult bestRayHit = null;
            double bestRayScale = 1.0;
            if (desiredLen > 0.001) {
                double px = this.player.getX();
                double py = this.player.getY();
                double pz = this.player.getZ();
                Vec3 rayStart = new Vec3(px, py + h * 0.5, pz);
                Vec3 rayEnd = rayStart.add(desired);
                ClipContext clipCtx = new ClipContext(rayStart, rayEnd, Block.COLLIDER, Fluid.NONE, this.player);
                BlockHitResult hit = this.player.level().clip(clipCtx);
                if (hit.getType() == Type.BLOCK) {
                    double hitDist = rayStart.distanceTo(hit.getLocation());
                    double margin = 0.02;
                    double safeDist = Math.max(0.0, hitDist - margin);
                    double scale = safeDist / desiredLen;
                    if (scale < bestRayScale) {
                        bestRayScale = scale;
                    }
                }

                if (bestRayScale < 1.0) {
                    desired = desired.scale(bestRayScale);
                }
            }

            AABB droneBox = new AABB(
                this.player.getX() - 0.275F,
                this.player.getY(),
                this.player.getZ() - 0.275F,
                this.player.getX() + 0.275F,
                this.player.getY() + 0.28F,
                this.player.getZ() + 0.275F
            );
            Vec3 clipped = collideBoundingBoxWithDroneSize(this.player, desired, droneBox);
            boolean canTriggerCollision = this.ticksSinceInit > 10;
            Vector3f resultVelocity = new Vector3f(nextVelocity);
            float collisionThreshold = 0.001F;
            boolean hitX = Math.abs((float)(desired.x - clipped.x)) > collisionThreshold;
            boolean hitY = Math.abs((float)(desired.y - clipped.y)) > collisionThreshold;
            boolean hitZ = Math.abs((float)(desired.z - clipped.z)) > collisionThreshold;
            boolean anyHit = hitX || hitY || hitZ;
            float speed = nextVelocity.length();
            if (anyHit && this.onCollision != null && canTriggerCollision) {
                float impactSpeed = nextVelocity.length();
                this.onCollision.accept(impactSpeed);
            }

            if (this.onCollision != null && canTriggerCollision && speed > 3.0F) {
                this.checkEntityCollision(droneBox, speed);
            }

            if (anyHit) {
                boolean wallHit = hitX || hitZ;
                float nx = hitX ? Math.signum(nextVelocity.x) : 0.0F;
                float ny = hitY ? Math.signum(nextVelocity.y) : 0.0F;
                float nz = hitZ ? Math.signum(nextVelocity.z) : 0.0F;
                float nLen = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (nLen > 0.001F) {
                    nx /= nLen;
                    ny /= nLen;
                    nz /= nLen;
                }

                float vDotN = resultVelocity.x * nx + resultVelocity.y * ny + resultVelocity.z * nz;
                if (vDotN > 0.0F) {
                    float bounceCoeff = wallHit ? 0.3F : 0.2F;
                    resultVelocity.x -= (1.0F + bounceCoeff) * vDotN * nx;
                    resultVelocity.y -= (1.0F + bounceCoeff) * vDotN * ny;
                    resultVelocity.z -= (1.0F + bounceCoeff) * vDotN * nz;
                }

                float tangentialFriction = 0.85F;
                float afterDotN = resultVelocity.x * nx + resultVelocity.y * ny + resultVelocity.z * nz;
                float tanX = resultVelocity.x - afterDotN * nx;
                float tanY = resultVelocity.y - afterDotN * ny;
                float tanZ = resultVelocity.z - afterDotN * nz;
                tanX *= tangentialFriction;
                tanY *= tangentialFriction;
                tanZ *= tangentialFriction;
                float normalDamp = 0.5F;
                afterDotN *= normalDamp;
                resultVelocity.x = tanX + afterDotN * nx;
                resultVelocity.y = tanY + afterDotN * ny;
                resultVelocity.z = tanZ + afterDotN * nz;
                float cutThreshold = wallHit ? 8.0F : 10.0F;
                if (speed > cutThreshold && canTriggerCollision && this.isMotorsArmed()) {
                    this.throttleCutTimer = 0.8F;
                }

                if (speed > 8.0F && canTriggerCollision && this.isMotorsArmed() && this.core instanceof DefaultPhysicsCore dpc) {
                    float normalized = Math.min((speed - 8.0F) / 12.0F, 1.0F);
                    float kickScale = normalized * normalized;
                    float maxKick = 1500.0F;
                    float tanLen = (float)Math.sqrt(tanX * tanX + tanY * tanY + tanZ * tanZ);
                    float biasYaw = 0.0F;
                    float biasPitch = 0.0F;
                    if (tanLen > 0.1F) {
                        biasPitch = tanZ / tanLen * 0.5F;
                        biasYaw = tanX / tanLen * 0.3F;
                    }

                    float rYaw = ((float)Math.random() - 0.5F + biasYaw) * maxKick * kickScale;
                    float rPitch = ((float)Math.random() - 0.5F + biasPitch) * maxKick * kickScale;
                    float rRoll = ((float)Math.random() - 0.5F) * maxKick * kickScale;
                    dpc.applyRotationalKick(rYaw, rPitch, rRoll);
                }

                float pushOff = 0.03F;
                if (hitX) {
                    this.player
                        .setPos(
                            this.player.getX() + clipped.x - Math.signum(nextVelocity.x) * pushOff,
                            this.player.getY() + clipped.y,
                            this.player.getZ() + clipped.z
                        );
                } else if (hitZ) {
                    this.player
                        .setPos(
                            this.player.getX() + clipped.x,
                            this.player.getY() + clipped.y,
                            this.player.getZ() + clipped.z - Math.signum(nextVelocity.z) * pushOff
                        );
                } else {
                    this.player
                        .setPos(
                            this.player.getX() + clipped.x, this.player.getY() + clipped.y, this.player.getZ() + clipped.z
                        );
                }

                this.player.setOldPosAndRot();
            } else {
                this.player
                    .setPos(this.player.getX() + clipped.x, this.player.getY() + clipped.y, this.player.getZ() + clipped.z);
                this.player.setOldPosAndRot();
            }

            if ((hitX || hitZ) && speed < 3.0F) {
                float wallFriction = 0.4F;
                resultVelocity.y *= wallFriction;
            }

            if (hitY && desired.y <= 0.0) {
                this.groundContactThisTick = true;
                float groundFriction = 0.87F;
                resultVelocity.x *= groundFriction;
                resultVelocity.z *= groundFriction;
                if (Math.abs(resultVelocity.x) < 0.05F) {
                    resultVelocity.x = 0.0F;
                }

                if (Math.abs(resultVelocity.z) < 0.05F) {
                    resultVelocity.z = 0.0F;
                }
            }

            return resultVelocity;
        } else {
            return nextVelocity;
        }
    }

    private static Vec3 collideBoundingBoxWithDroneSize(Entity entity, Vec3 movement, AABB box) {
        List<VoxelShape> collisions = entity.level().getEntityCollisions(entity, box.expandTowards(movement));
        List<VoxelShape> ivBoxes = getIVCollisionBoxes(entity, box.expandTowards(movement));
        if (ivBoxes != null && !ivBoxes.isEmpty()) {
            List<VoxelShape> merged = new ArrayList<>(collisions);
            merged.addAll(ivBoxes);
            collisions = merged;
        }

        return movement.lengthSqr() == 0.0 ? movement : Entity.collideBoundingBox(entity, movement, box, entity.level(), collisions);
    }

    private static List<VoxelShape> getIVCollisionBoxes(Entity entity, AABB pCollisionBox) {
        if (ivCollisionReflectionFailed) {
            return null;
        } else {
            try {
                if (ivBuilderClass == null) {
                    ivBuilderClass = Class.forName("mcinterface1201.BuilderEntityExisting");
                    ivCollisionBoxesField = ivBuilderClass.getField("collisionBoxes");
                    Class<?> wrapperClass = Class.forName("mcinterface1201.WrapperAABBCollective");
                    ivGetBoxesMethod = wrapperClass.getMethod("getBoxes");
                    Class<?> wrapperWorldClass = Class.forName("mcinterface1201.WrapperWorld");
                    Class<?> boundingBoxClass = Class.forName("minecrafttransportsimulator.baseclasses.BoundingBox");
                    ivConvertMethod = wrapperWorldClass.getMethod("convert", boundingBoxClass);
                }

                List<VoxelShape> result = null;

                for (Entity e : entity.level().getEntities(entity, pCollisionBox, ivBuilderClass::isInstance)) {
                    Object collisionBoxes = ivCollisionBoxesField.get(e);
                    if (collisionBoxes != null && (!(collisionBoxes instanceof AABB collective) || collective.intersects(pCollisionBox))) {
                        AABB collectiveAABB = (AABB)collisionBoxes;
                        if (collectiveAABB.intersects(pCollisionBox)) {
                            for (Object box : (Collection)ivGetBoxesMethod.invoke(collisionBoxes)) {
                                AABB converted = (AABB)ivConvertMethod.invoke(null, box);
                                if (converted.intersects(pCollisionBox)) {
                                    if (result == null) {
                                        result = new ArrayList<>();
                                    }

                                    result.add(Shapes.create(converted));
                                }
                            }
                        }
                    }
                }

                return result;
            } catch (ClassNotFoundException var11) {
                ivCollisionReflectionFailed = true;
                return null;
            } catch (Exception var12) {
                ivCollisionReflectionFailed = true;
                LogUtils.getLogger().warn("[FPV] Could not get IV collision boxes via reflection", var12);
                return null;
            }
        }
    }

    private void checkEntityCollision(AABB droneBox, float speed) {
        if (this.player != null && this.player.level() != null) {
            Vector3f vel = this.core.getVelocity();
            Vec3 mcVel = PhysicsUnits.toMinecraftVelocity(vel);
            AABB searchBox = droneBox.expandTowards(mcVel).inflate(0.2);
            List<Entity> entities = this.player
                .level()
                .getEntities(this.player, searchBox, e -> e.isAlive() && e != this.player && !(e instanceof DroneEntity) && !(e instanceof com.fpvdrone.entity.DroppedBombEntity) && !e.isSpectator() && e.isPickable());
            if (!entities.isEmpty()) {
                Entity hit = entities.get(0);
                double dx = hit.getX() - this.player.getX();
                double dy = hit.getY() + hit.getBbHeight() * 0.5 - (this.player.getY() + 0.14F);
                double dz = hit.getZ() - this.player.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist < 0.001) {
                    dist = 1.0;
                }

                float nx = (float)(dx / dist);
                float ny = (float)(dy / dist);
                float nz = (float)(dz / dist);
                float vDotN = vel.x * nx + vel.y * ny + vel.z * nz;
                if (vDotN > 0.0F) {
                    float bounceCoeff = 0.3F;
                    vel.x -= (1.0F + bounceCoeff) * vDotN * nx;
                    vel.y -= (1.0F + bounceCoeff) * vDotN * ny;
                    vel.z -= (1.0F + bounceCoeff) * vDotN * nz;
                    vel.mul(0.6F);
                    this.core.setVelocity(vel);
                }

                if (speed > 8.0F && this.isMotorsArmed()) {
                    this.throttleCutTimer = 0.8F;
                }

                if (speed > 5.0F && this.isMotorsArmed() && this.core instanceof DefaultPhysicsCore dpc) {
                    float normalized = Math.min((speed - 5.0F) / 12.0F, 1.0F);
                    float kickScale = normalized * normalized;
                    float maxKick = 1200.0F;
                    dpc.applyRotationalKick(
                        ((float)Math.random() - 0.5F) * maxKick * kickScale,
                        ((float)Math.random() - 0.5F) * maxKick * kickScale,
                        ((float)Math.random() - 0.5F) * maxKick * kickScale
                    );
                }

                if (this.onEntityHit != null && speed >= 5.0F && this.entityHitCooldown <= 0) {
                    this.entityHitCooldown = 10;
                    Vector3f hitVel = this.core.getVelocity();
                    double droneX = this.player.getX();
                    double droneY = this.player.getY();
                    double droneZ = this.player.getZ();

                    for (Entity entity : entities) {
                        this.onEntityHit.onHit(entity.getUUID(), speed, hitVel.x, hitVel.y, hitVel.z, droneX, droneY, droneZ);
                    }
                }
            }
        }
    }

    public void initFromDrone(DroneEntity drone) {
        this.ticksSinceInit = 0;
        float yaw = drone.getYRot();
        float pitch = drone.getXRot();
        float roll = drone.getDroneRoll();
        this.initFromAngles(yaw, pitch, roll);
        Vec3 vel = drone.getDeltaMovement();
        this.core.setVelocity(PhysicsUnits.toPhysicsVelocity(vel));
    }

    private void initFromAngles(float yaw, float pitch, float roll) {
        float yawRad = (float)Math.toRadians(-yaw);
        float pitchRad = (float)Math.toRadians(pitch);
        float rollRad = (float)Math.toRadians(roll);
        Quaternionf rot = new Quaternionf();
        rot.rotationYXZ(yawRad, pitchRad, rollRad);
        Vector3f look = rot.transform(new Vector3f(0.0F, 0.0F, 1.0F));
        Vector3f up = rot.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f left = new Vector3f();
        up.cross(look, left);
        left.normalize();
        this.core.setDroneLook(look);
        this.core.setDroneUp(up);
        this.core.setDroneLeft(left);
        this.core.setVelocity(new Vector3f(0.0F, 0.0F, 0.0F));
    }

    public void syncBasisFromAngles(float yaw, float pitch, float roll) {
        float yawRad = (float)Math.toRadians(-yaw);
        float pitchRad = (float)Math.toRadians(pitch);
        float rollRad = (float)Math.toRadians(roll);
        Quaternionf rot = new Quaternionf();
        rot.rotationYXZ(yawRad, pitchRad, rollRad);
        Vector3f look = rot.transform(new Vector3f(0.0F, 0.0F, 1.0F));
        Vector3f up = rot.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f left = new Vector3f();
        up.cross(look, left);
        left.normalize();
        this.core.setDroneLook(look);
        this.core.setDroneUp(up);
        this.core.setDroneLeft(left);
    }

    public Vector3f getVelocity() {
        return this.core.getVelocity();
    }

    public Vec3 getVelocityVec3() {
        Vector3f v = this.core.getVelocity();
        return new Vec3(v.x, v.y, v.z);
    }

    public void setVelocity(Vec3 velocity) {
        this.core.setVelocity(new Vector3f((float)velocity.x, (float)velocity.y, (float)velocity.z));
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

    public void setMass(float mass) {
        this.mass = mass;
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setMass(mass);
        }
    }

    public void setMotorKv(float motorKv) {
        this.motorKv = motorKv;
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setMotorKv(motorKv);
        }
    }

    public void setBatteryCells(int batteryCells) {
        this.batteryCells = batteryCells;
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setBatteryCells(batteryCells);
        }
    }

    public void setPropDiameter(float propDiameter) {
        this.propDiameter = propDiameter;
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setPropDiameter(propDiameter);
        }
    }

    public void setPropPitch(float propPitch) {
        this.propPitch = propPitch;
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setPropPitch(propPitch);
        }
    }

    public void setDragCoefficient(float dragCoefficient) {
        this.dragCoefficient = dragCoefficient;
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setDragCoefficient(dragCoefficient);
        }
    }

    public void setFlightMode3d(boolean mode) {
        this.flightMode3d = mode;
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setFlightMode3d(mode);
        }
    }

    public void setThrustMultiplier(float mult) {
        this.thrustMultiplier = mult;
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setThrustMultiplier(mult);
        }
    }

    public void setRcRate(int ch, float v) {
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setRcRate(ch, v);
        }
    }

    public void setSuperRate(int ch, float v) {
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setSuperRate(ch, v);
        }
    }

    public void setExpo(int ch, float v) {
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setExpo(ch, v);
        }
    }

    public boolean isMotorsArmed() {
        return this.core instanceof DefaultPhysicsCore dpc ? dpc.isMotorsArmed() : false;
    }

    public void setMotorsArmed(boolean armed) {
        if (!this.isFalling()) {
            if (this.core instanceof DefaultPhysicsCore dpc) {
                dpc.setMotorsArmed(armed);
            }
        }
    }

    public void setFalling(boolean falling) {
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setFalling(falling);
        }
    }

    public boolean isFalling() {
        return this.core instanceof DefaultPhysicsCore dpc ? dpc.isFalling() : false;
    }

    public void setFallingParams(float thrustFactor, float dragMultiplier) {
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setFallingParams(thrustFactor, dragMultiplier);
        }
    }

    public void applyImpulse(Vector3f impulse) {
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.applyImpulse(impulse);
        }
    }

    public void setAngularVelocity(float yawRate, float pitchRate, float rollRate) {
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.setAngularVelocity(yawRate, pitchRate, rollRate);
        }
    }

    public void applyRotationalKick(float yaw, float pitch, float roll) {
        if (this.core instanceof DefaultPhysicsCore dpc) {
            dpc.applyRotationalKick(yaw, pitch, roll);
        }
    }

    public float getLastPlayerYaw() {
        return this.lastPlayerYaw;
    }

    public void setLastPlayerYaw(float yaw) {
        this.lastPlayerYaw = yaw;
    }

    public float getLastPlayerPitch() {
        return this.lastPlayerPitch;
    }

    public void setLastPlayerPitch(float pitch) {
        this.lastPlayerPitch = pitch;
    }

    public float getMouseDeltaYaw() {
        return this.mouseDeltaYaw;
    }

    public void setMouseDeltaYaw(float delta) {
        this.mouseDeltaYaw = delta;
    }

    public float getMouseDeltaPitch() {
        return this.mouseDeltaPitch;
    }

    public void setMouseDeltaPitch(float delta) {
        this.mouseDeltaPitch = delta;
    }

    public void clearMouseDelta() {
        this.mouseDeltaYaw = 0.0F;
        this.mouseDeltaPitch = 0.0F;
    }

    @FunctionalInterface
    public interface EntityHitCallback {
        void onHit(UUID var1, float var2, float var3, float var4, float var5, double var6, double var8, double var10);
    }

    /** 1.1.8: true while the motors are cut for a moment after a hard impact. */
    public boolean isCrashCutoff() {
        return false; // 1.1.8-PENDING: the 0.8 s post-impact cut-off comes with the 1.1.8 physics port.
    }
}
