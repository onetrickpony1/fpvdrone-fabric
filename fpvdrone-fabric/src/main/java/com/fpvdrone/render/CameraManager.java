package com.fpvdrone.render;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * 1.1.8 camera input: free look (hold a key in third person to orbit the view), gimbal tilt and camera zoom.
 * In 1.1.4 this class only set the FPV camera angles and FOV; the port does that in {@link com.fpvdrone.mixin.CameraMixin},
 * which calls in here for the 1.1.8 parts.
 */
public final class CameraManager {
    /** Third-person orbit distance, blocks. */
    public static final float THIRD_PERSON_DISTANCE = 4.0F;
    private static final float FREE_LOOK_SENSITIVITY = 0.6F;
    private static final float DEFAULT_ORBIT_PITCH = 15.0F;
    private static final SmoothedValue FREE_LOOK_YAW = new SmoothedValue(0.0F, 10.0F, -170.0F, 170.0F);
    private static final SmoothedValue FREE_LOOK_PITCH = new SmoothedValue(DEFAULT_ORBIT_PITCH, 10.0F, -80.0F, 85.0F);

    /** What the drone's camera can do (1.1.8 DroneDefinition.Camera, reduced to the parts this area reads). */
    public record DroneCamera(float fovDegrees, boolean gimbal, boolean freeLook, float[] zoomLevels) {
        public static final DroneCamera FPV_QUAD = new DroneCamera(0.0F, false, false, CameraZoom.DEFAULT_LEVELS);

        /** FovDegrees 0 in the drone file means "use the FOV from the mod config". */
        public boolean usesConfigFov() {
            return this.fovDegrees <= 0.0F;
        }
    }

    private CameraManager() {
    }

    public static void init() {
        // 1.1.8 clears these with the rest of the client state when joining a world.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(CameraManager::reset));
    }

    public static void reset() {
        GimbalState.getInstance().reset();
        CameraZoom.getInstance().reset();
        FREE_LOOK_YAW.reset(0.0F);
        FREE_LOOK_PITCH.reset(DEFAULT_ORBIT_PITCH);
    }

    public static DroneCamera cameraOf(DroneEntity drone) {
        // 1.1.8: the drone file's "Camera" block, with its kind's defaults filled in.
        com.fpvdrone.data.DroneDefinition.Camera cam = drone.oEuCBB().bAcdvq9();
        return new DroneCamera(cam.fovDegrees(), cam.gimbal(), cam.freeLook(), cam.zoom());
    }

    /** Camera of the drone being flown, or else of the drone the worn goggles watch. */
    public static Optional<DroneCamera> currentCamera(Minecraft mc) {
        return currentDrone(mc).map(CameraManager::cameraOf);
    }

    private static Optional<DroneEntity> currentDrone(Minecraft mc) {
        if (mc.player == null) {
            return Optional.empty();
        }

        UUID playerId = mc.player.getUUID();
        if (FPVMod.getActiveDroneId(playerId).isPresent()) {
            return FPVMod.getActiveDrone(playerId);
        }

        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof FpvGogglesItem)) {
            return Optional.empty();
        }

        Optional<UUID> linked = FpvGogglesItem.getLinkedDroneId(helmet);
        if (linked.isEmpty() || FpvStatusChecker.hasNoSignalState(linked.get())) {
            return Optional.empty();
        }

        return findDroneById(mc, linked.get());
    }

    /** Whether the drone being flown has a stabilised gimbal camera. */
    public static boolean flyingGimbalDrone(Minecraft mc) {
        if (mc.player == null || FPVMod.getActiveDroneId(mc.player.getUUID()).isEmpty()) {
            return false;
        }

        return FPVMod.getActiveDrone(mc.player.getUUID()).map(d -> cameraOf(d).gimbal()).orElse(false);
    }

    // ---- free look ----

    /** Free look runs while its key is held in a third-person view of a drone whose camera allows it. */
    public static boolean isFreeLookActive(Minecraft mc) {
        if (mc.player == null || mc.gui.screen() != null) {
            // 1.1.8-PENDING: the Lancet aim screen also counts as "no screen" here.
            return false;
        }

        if (mc.options.getCameraType().isFirstPerson() || !KeyManager.FREE_LOOK_KEY.isDown()) {
            return false;
        }

        // 1.1.8-PENDING: a loitering munition being flown or observed also allows free look when its camera does.
        return currentCamera(mc).map(DroneCamera::freeLook).orElse(false);
    }

    public static void addFreeLook(float yawDelta, float pitchDelta) {
        FREE_LOOK_YAW.addToTarget(yawDelta * FREE_LOOK_SENSITIVITY);
        FREE_LOOK_PITCH.addToTarget(pitchDelta * FREE_LOOK_SENSITIVITY);
    }

    /** Yaw offset of the third-person orbit; eases back to 0 when free look is released. */
    public static float freeLookYaw(Minecraft mc) {
        if (!isFreeLookActive(mc)) {
            FREE_LOOK_YAW.setTarget(0.0F);
        }

        return FREE_LOOK_YAW.update();
    }

    /** Pitch of the third-person orbit (looking down is positive); eases back to 15 degrees when released. */
    public static float freeLookPitch(Minecraft mc) {
        if (!isFreeLookActive(mc)) {
            FREE_LOOK_PITCH.setTarget(DEFAULT_ORBIT_PITCH);
        }

        return FREE_LOOK_PITCH.update();
    }

    // ---- gimbal and zoom ----

    /** Once per frame while flying: gimbal tilt from the tilt-wheel channel, or else from the tilt keys. */
    public static void tickGimbal(Minecraft mc) {
        if (!flyingGimbalDrone(mc)) {
            return;
        }

        ControllerReader reader = ControllerReader.getInstance();
        float input;
        if (ControllerReader.getConfig().getGimbalChannel() >= 0 && reader.isPhysicalControllerConnected()) {
            input = ControllerReader.getGimbal();
        } else {
            boolean up = KeyManager.GIMBAL_UP_KEY.isDown();
            boolean down = KeyManager.GIMBAL_DOWN_KEY.isDown();
            input = up && !down ? 1.0F : (down && !up ? -1.0F : 0.0F);
        }

        GimbalState.getInstance().applyRate(input);
    }

    /** Mouse wheel while flying a gimbal drone without a tilt wheel tilts the camera. Returns true when consumed. */
    public static boolean onMouseScroll(double delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || delta == 0.0) {
            return false;
        }

        // 1.1.8: the interact menu (scroll through its options) comes first.
        if (com.fpvdrone.client.interact.InteractMenu.onMouseScroll(delta)) {
            return true;
        }

        // 1.1.8-PENDING: loitering-munition zoom (GAduSQ8Kp / MYIp2kM68b) comes next here.
        if (!flyingGimbalDrone(mc)) {
            return false;
        }

        if (ControllerReader.getConfig().getGimbalChannel() < 0 || !ControllerReader.getInstance().isPhysicalControllerConnected()) {
            GimbalState.getInstance().nudge((float)(-delta) * GimbalState.SCROLL_STEP);
            return true;
        }

        return false;
    }

    /** Field of view while looking through a drone camera. */
    public static float computeFov(Minecraft mc, float vanillaFov) {
        // 1.1.8-PENDING: a loitering munition being flown sets its own (zoomable) FOV.
        DroneCamera camera = currentCamera(mc).orElse(DroneCamera.FPV_QUAD);
        if (camera.gimbal()) {
            CameraZoom zoom = CameraZoom.getInstance();
            return zoom.isZoomed() ? (float)zoom.applyToFov(vanillaFov) : vanillaFov;
        }

        return camera.usesConfigFov() ? FpvConfig.getFov() : camera.fovDegrees();
    }

    private static Optional<DroneEntity> findDroneById(Minecraft mc, UUID droneId) {
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
