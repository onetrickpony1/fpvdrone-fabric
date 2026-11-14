package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.client.Transforms;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.physics.PhysicsState;
import com.fpvdrone.render.CameraManager;
import com.fpvdrone.render.GimbalState;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * FPV camera: puts the view at the drone's camera mount with the drone's yaw, pitch and roll.
 * Replaces the Forge-era CameraMixin + CameraManager (ComputeCameraAngles/ComputeFov) + GameRendererMixin.
 * In 26.3 the camera's quaternion drives both the view matrix and the cull frustum, so roll goes straight in.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Unique
    private static final float fpvdrone$CAM_PIVOT_Y = 0.22122687F;
    @Unique
    private static final float fpvdrone$CAM_PIVOT_Z = 0.24639937F;
    @Unique
    private static final float fpvdrone$CAMERAPOS_OFFSET_Z = 0.0625F;
    /** 1.1.8: camera mount of a gimbal drone (the Mavic's), drone-local up and forward. */
    @Unique
    private static final float fpvdrone$GIMBAL_CAM_PIVOT_Y = 0.103125F;
    @Unique
    private static final float fpvdrone$GIMBAL_CAM_PIVOT_Z = 0.2515625F;
    @Unique
    private static final Vector3fc fpvdrone$FORWARDS = new Vector3f(0.0F, 0.0F, -1.0F);
    @Unique
    private static final Vector3fc fpvdrone$UP = new Vector3f(0.0F, 1.0F, 0.0F);
    @Unique
    private static final Vector3fc fpvdrone$LEFT = new Vector3f(-1.0F, 0.0F, 0.0F);

    @Shadow
    @Final
    private Quaternionf rotation;
    @Shadow
    @Final
    private Vector3f forwards;
    @Shadow
    @Final
    private Vector3f up;
    @Shadow
    @Final
    private Vector3f left;
    @Shadow
    private int matrixPropertiesDirty;
    @Unique
    private boolean fpvdrone$active;

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void fpvdrone$adjustCamera(float partialTick, CallbackInfo ci) {
        this.fpvdrone$active = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        UUID playerId = mc.player.getUUID();
        PhysicsState physics = FPVMod.getPhysicsState(playerId);
        Optional<UUID> activeDroneId = FPVMod.getActiveDroneId(playerId);
        if (physics != null && activeDroneId.isPresent()) {
            Vector3f look = physics.getDroneLook();
            Vector3f up = physics.getDroneUp();
            float[] bodyAngles = Transforms.getWorldEulerAngles(look, up);
            mc.player.setYRot(bodyAngles[0]);
            mc.player.setXRot(bodyAngles[1]);
            mc.player.yRotO = bodyAngles[0];
            mc.player.xRotO = bodyAngles[1];
            mc.player.setYHeadRot(bodyAngles[0]);
            double baseX = mc.player.xOld + (mc.player.getX() - mc.player.xOld) * partialTick;
            double baseY = mc.player.yOld + (mc.player.getY() - mc.player.yOld) * partialTick;
            double baseZ = mc.player.zOld + (mc.player.getZ() - mc.player.zOld) * partialTick;
            if (mc.options.getCameraType().isFirstPerson()) {
                float[] local;
                if (CameraManager.flyingGimbalDrone(mc)) {
                    // 1.1.8: a stabilised gimbal keeps the horizon level; only its tilt is under the pilot's control.
                    GimbalState gimbal = GimbalState.getInstance();
                    float pitch = gimbal.getPitch();
                    this.fpvdrone$applyRotation(bodyAngles[0], pitch, gimbal.getRollKick());
                    local = new float[]{fpvdrone$GIMBAL_CAM_PIVOT_Y, fpvdrone$GIMBAL_CAM_PIVOT_Z};
                } else {
                    Vector3f left = physics.getDroneLeft();
                    Quaternionf tilt = new Quaternionf().rotationAxis(-FpvConfig.getDefaultAngle() * (float) (Math.PI / 180.0), left);
                    float[] camAngles = Transforms.getWorldEulerAngles(tilt.transform(new Vector3f(look)), tilt.transform(new Vector3f(up)));
                    this.fpvdrone$applyRotation(camAngles[0], camAngles[1], camAngles[2]);
                    local = fpvdrone$mountOffset();
                }

                this.setPosition(
                    baseX + up.x * local[0] + look.x * local[1], baseY + up.y * local[0] + look.y * local[1], baseZ + up.z * local[0] + look.z * local[1]
                );
            } else {
                // 1.1.8: third person orbits the drone at a level heading (free look turns it), not along the drone's pitch.
                float heading = (float)Math.toDegrees(Math.atan2(-look.x, look.z));
                float bodyHeight = FPVMod.getActiveDrone(playerId).map(d -> d.getBbHeight()).orElse(0.0F);
                this.fpvdrone$orbit(mc, heading, baseX, baseY + bodyHeight * 0.5, baseZ);
            }

            this.fpvdrone$active = true;
            return;
        }

        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof FpvGogglesItem)) {
            return;
        }

        Optional<UUID> linkedDroneId = FpvGogglesItem.getLinkedDroneId(helmet);
        if (linkedDroneId.isEmpty() || FpvStatusChecker.hasNoSignalState(linkedDroneId.get())) {
            return;
        }

        Optional<DroneEntity> droneOpt = fpvdrone$findDroneById(mc, linkedDroneId.get());
        if (droneOpt.isEmpty()) {
            float[] savedRot = FPVMod.getLastKnownDroneRotation(linkedDroneId.get());
            if (savedRot != null) {
                mc.player.setYRot(savedRot[0]);
                mc.player.setXRot(savedRot[1]);
                mc.player.yRotO = savedRot[0];
                mc.player.xRotO = savedRot[1];
                mc.player.setYHeadRot(savedRot[0]);
                this.setRotation(savedRot[0], savedRot[1]);
            }

            return;
        }

        // Watching a drone that someone (or the server) is moving: follow its synced pose.
        DroneEntity drone = droneOpt.get();
        double x = drone.xOld + (drone.getX() - drone.xOld) * partialTick;
        double y = drone.yOld + (drone.getY() - drone.yOld) * partialTick;
        double z = drone.zOld + (drone.getZ() - drone.zOld) * partialTick;
        mc.player.setPos(drone.getX(), drone.getY(), drone.getZ());
        mc.player.xOld = drone.xOld;
        mc.player.yOld = drone.yOld;
        mc.player.zOld = drone.zOld;
        float droneYaw = drone.getYRot();
        float dronePitch = drone.getXRot();
        mc.player.setYRot(droneYaw);
        mc.player.setXRot(dronePitch);
        mc.player.yRotO = droneYaw;
        mc.player.xRotO = dronePitch;
        mc.player.setYHeadRot(droneYaw);
        if (!mc.options.getCameraType().isFirstPerson()) {
            // 1.1.8: watching through goggles in third person orbits the drone too.
            this.fpvdrone$orbit(mc, droneYaw, x, y + drone.getBbHeight() * 0.5, z);
            this.fpvdrone$active = true;
            return;
        }

        Quaternionf droneRot = new Quaternionf()
            .rotationYXZ((float)Math.toRadians(-droneYaw), (float)Math.toRadians(dronePitch), (float)Math.toRadians(drone.getDroneRoll()));
        Vector3f droneLook = droneRot.transform(new Vector3f(0.0F, 0.0F, 1.0F));
        Vector3f droneUp = droneRot.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f droneLeft = droneUp.cross(droneLook, new Vector3f()).normalize();
        Quaternionf tilt = new Quaternionf().rotationAxis(-FpvConfig.getDefaultAngle() * (float) (Math.PI / 180.0), droneLeft);
        if (CameraManager.cameraOf(drone).gimbal()) {
            // 1.1.8: a gimbal drone being watched shows its level, tilted camera (mount position stays the FPV one, as in 1.1.8).
            this.fpvdrone$applyRotation(droneYaw, GimbalState.getInstance().getPitch(), 0.0F);
        } else {
            float[] camAngles = Transforms.getWorldEulerAngles(tilt.transform(new Vector3f(droneLook)), tilt.transform(new Vector3f(droneUp)));
            this.fpvdrone$applyRotation(camAngles[0], camAngles[1], camAngles[2]);
        }

        float[] local = fpvdrone$mountOffset();
        Vector3f offset = droneRot.transform(new Vector3f(0.0F, local[0], local[1]));
        this.setPosition(x + offset.x, y + offset.y, z + offset.z);
        this.fpvdrone$active = true;
    }

    /** Field of view while looking through the drone camera (mod config, or the gimbal camera zoom). */
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void fpvdrone$fov(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (this.fpvdrone$active) {
            cir.setReturnValue(CameraManager.computeFov(Minecraft.getInstance(), cir.getReturnValue()));
        }
    }

    /** The drone body is tiny, so pull the near plane in to stop terrain clipping into the view. */
    @ModifyArg(
        method = "update",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setupPerspective(FFFFF)V"),
        index = 0
    )
    private float fpvdrone$nearPlane(float zNear) {
        return this.fpvdrone$active ? 0.01F : zNear;
    }

    /** Same mapping as Camera.setRotation, plus roll (Forge applied roll as Z rotation of the view; the camera quaternion is its inverse). */
    @Unique
    private void fpvdrone$applyRotation(float yaw, float pitch, float roll) {
        this.setRotation(yaw, pitch);
        this.rotation
            .rotationYXZ(
                (float) Math.PI - yaw * (float) (Math.PI / 180.0), -pitch * (float) (Math.PI / 180.0), -roll * (float) (Math.PI / 180.0)
            );
        fpvdrone$FORWARDS.rotate(this.rotation, this.forwards);
        fpvdrone$UP.rotate(this.rotation, this.up);
        fpvdrone$LEFT.rotate(this.rotation, this.left);
        this.matrixPropertiesDirty |= 3;
    }

    /**
     * 1.1.8 third-person view: THIRD_PERSON_DISTANCE blocks from the drone's centre, behind it (or in front for the
     * front view), at the free-look yaw offset and orbit pitch (15 degrees down unless free look moves it).
     */
    @Unique
    private void fpvdrone$orbit(Minecraft mc, float heading, double cx, double cy, double cz) {
        boolean front = mc.options.getCameraType().isMirrored();
        float orbitYaw = heading + CameraManager.freeLookYaw(mc);
        float orbitPitch = CameraManager.freeLookPitch(mc);
        double yawRad = Math.toRadians(orbitYaw);
        double pitchRad = Math.toRadians(orbitPitch);
        double horizontal = CameraManager.THIRD_PERSON_DISTANCE * Math.cos(pitchRad);
        double vertical = CameraManager.THIRD_PERSON_DISTANCE * Math.sin(pitchRad);
        double side = front ? 1.0 : -1.0;
        this.setPosition(cx + side * -Math.sin(yawRad) * horizontal, cy + vertical, cz + side * Math.cos(yawRad) * horizontal);
        this.fpvdrone$applyRotation(front ? orbitYaw + 180.0F : orbitYaw, orbitPitch, 0.0F);
    }

    /** Camera mount position in drone-local (up, forward) coordinates for the configured uptilt. */
    @Unique
    private static float[] fpvdrone$mountOffset() {
        float angleRad = (float)Math.toRadians(-FpvConfig.getDefaultAngle());
        return new float[]{
            fpvdrone$CAM_PIVOT_Y + fpvdrone$CAMERAPOS_OFFSET_Z * (float)Math.sin(angleRad),
            fpvdrone$CAM_PIVOT_Z + fpvdrone$CAMERAPOS_OFFSET_Z * (float)Math.cos(angleRad)
        };
    }

    @Unique
    private static Optional<DroneEntity> fpvdrone$findDroneById(Minecraft mc, UUID droneId) {
        if (mc.level != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof DroneEntity drone && drone.getUUID().equals(droneId)) {
                    return Optional.of(drone);
                }
            }
        }

        return Optional.empty();
    }
}
