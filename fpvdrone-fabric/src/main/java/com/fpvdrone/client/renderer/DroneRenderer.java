package com.fpvdrone.client.renderer;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.PayloadAnchors;
import com.fpvdrone.client.model.BedrockModels;
import com.fpvdrone.client.model.DroneModel;
import com.fpvdrone.client.model.ProjectileModels;
import com.fpvdrone.data.AttachTransform;
import com.fpvdrone.data.BatteryDefinition;
import com.fpvdrone.data.BatteryDefinitions;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.PayloadStation;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.physics.PhysicsState;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * GeckoLib 5 renderer for the multirotor drones (FPV, thermal, Mavic and any data-pack drone of that kind). Everything
 * the Forge renderer read from the entity at draw time is captured into the render state in {@link #addRenderData} and
 * applied in {@link #adjustModelBonesForRender} / {@link #applyRotations} / {@link #preRenderPass}.
 * <p>
 * Model and texture come from the drone's definition (see {@link DroneModel}). The Forge version hooked
 * renderRecursively per bone; GeckoLib 5 has no such hook, so the special cases use per-bone render tasks instead:
 * <ul>
 *   <li>propellers between 25% and 55% throttle fade out, drawn with a translucent type that does not write depth;</li>
 *   <li>attachments (hanging payload, payload mount, bomb-bay stations on payload_mount; battery on battery_mount) are
 *       drawn at their mount bone's pose. 1.1.8 captured that pose while walking the model and drew the attachments
 *       after it; here the per-bone task receives the same pose directly.</li>
 * </ul>
 * Every frame the world position and rotation of each drawn payload goes to {@link PayloadAnchors}, so a dropped bomb
 * spawns exactly where it was drawn. The spinning blur discs and the launch bricks are drawn by {@link BlurDiscRenderer}
 * and {@link KirpichikiRenderer}.
 */
public class DroneRenderer extends GeoEntityRenderer<DroneEntity, LivingEntityRenderState> {
    static final float PROP_FADE_START = 0.25F;
    static final float PROP_FADE_END = 0.55F;
    private static final Set<String> PROP_BONES = Set.of("vintleftfront", "vintrightfront", "vintleftback", "vintrightback");
    private static final Set<String> BLUR_BONES = Set.of("vintleftfrontblur", "vintrightfrontblur", "vintleftbackblur", "vintrightbackblur");
    /** 1.1.8 jGhI2: each propeller and the blur bone whose cube tilt gives its spin axis. */
    private static final Map<String, String> PROP_TO_BLUR = Map.of(
        "vintleftfront", "vintleftfrontblur", "vintrightfront", "vintrightfrontblur", "vintleftback", "vintleftbackblur", "vintrightback", "vintrightbackblur"
    );
    /** 1.1.8 cXHvq / rxYnaa. */
    private static final String PAYLOAD_MOUNT = "payload_mount";
    private static final String BATTERY_MOUNT = "battery_mount";
    /** 1.1.8 r6s7FL3 / iAKNPAj / qlv6NKWnH: pivot of the hanging payload's swing, in model pixels from the attach point. */
    private static final float SWING_PIVOT_X = 0.0F;
    private static final float SWING_PIVOT_Y = 2.75F;
    private static final float SWING_PIVOT_Z = 0.0F;
    /** Anchor slot of an attachment: none (mounts, batteries), the hanging payload, or bomb-bay station i (>= 0). */
    private static final int ANCHOR_NONE = -2;
    private static final int ANCHOR_HANGING = -1;
    private static final Vector3f UP = new Vector3f(0.0F, 1.0F, 0.0F);
    /** 1.1.8 wEJx3Itb: propeller spin axis per model and propeller (only used on GPS drones, whose props are tilted). */
    private static final Map<Identifier, Map<String, Vector3f>> PROP_AXES = new ConcurrentHashMap<>();

    private static final DataTicket<Quaternionf> ORIENTATION = DataTicket.create("fpvdrone_orientation", Quaternionf.class);
    private static final DataTicket<Vec3> LOCAL_OFFSET = DataTicket.create("fpvdrone_local_offset", Vec3.class);
    private static final DataTicket<Boolean> HIDE_CAMERA = DataTicket.create("fpvdrone_hide_camera", Boolean.class);
    private static final DataTicket<Boolean> LOCAL_THERMAL_FPV = DataTicket.create("fpvdrone_local_thermal_fpv", Boolean.class);
    private static final DataTicket<Float> PROP_ANGLE = DataTicket.create("fpvdrone_prop_angle", Float.class);
    private static final DataTicket<Float> THROTTLE = DataTicket.create("fpvdrone_throttle", Float.class);
    private static final DataTicket<Boolean> GPS_DRONE = DataTicket.create("fpvdrone_gps_drone", Boolean.class);
    private static final DataTicket<Boolean> HIDE_PROPS = DataTicket.create("fpvdrone_hide_props", Boolean.class);
    private static final DataTicket<Boolean> PROP_BLUR = DataTicket.create("fpvdrone_prop_blur", Boolean.class);
    private static final DataTicket<Vector3f> GIMBAL_ROTATION = DataTicket.create("fpvdrone_gimbal_rotation", Vector3f.class);
    private static final DataTicket<Attachments> ATTACHMENTS = DataTicket.create("fpvdrone_attachments", Attachments.class);

    public DroneRenderer(Context context) {
        super(context, new DroneModel());
        this.shadowRadius = 0.0F;
    }

    /** Registers the custom render pipeline; must run during client init, before shaders are compiled. */
    public static void registerPipelines() {
        PropRenderTypes.init();
    }

    /** Inside a full block the probe reads no light; use the brightest neighbour instead so the drone does not turn black. */
    private static int fixLightInsideBlock(DroneEntity entity, int originalLight, float partialTick) {
        BlockPos pos = BlockPos.containing(entity.getLightProbePosition(partialTick));
        if (!entity.level().getBlockState(pos).isSolidRender()) {
            return originalLight;
        }

        int maxBlock = 0;
        int maxSky = 0;
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            maxBlock = Math.max(maxBlock, entity.level().getBrightness(LightLayer.BLOCK, neighbor));
            maxSky = Math.max(maxSky, entity.level().getBrightness(LightLayer.SKY, neighbor));
        }

        return LightCoordsUtil.pack(maxBlock, maxSky);
    }

    @Override
    public Vec3 getRenderOffset(LivingEntityRenderState state) {
        return Vec3.ZERO;
    }

    /** The original entity inflated its culling box by one block (props stick out past the small hitbox). */
    @Override
    protected AABB getBoundingBoxForCulling(DroneEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(1.0);
    }

    // 1.1.8 render() was gated by FsOzhg.v5suI() || !FsOzhg.v5suI(drone): drones standing in chunks the client has not
    // loaded were skipped in the normal pass and drawn by a separate far pass after the weather, with the camera's saved
    // matrices and full-bright light.
    // 1.1.8-PENDING: FsOzhg far-drone pass. Until it is ported every drone goes through the normal pass here.

    @Override
    public @Nullable RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucentCull(texture);
    }

    @Override
    public void addRenderData(DroneEntity entity, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        GeoRenderState state = (GeoRenderState)renderState;
        renderState.lightCoords = fixLightInsideBlock(entity, renderState.lightCoords, partialTick);
        Minecraft mc = Minecraft.getInstance();
        boolean isOwnDrone = isOwnDrone(mc, entity);
        PhysicsState physics = isOwnDrone ? FPVMod.getPhysicsState(mc.player.getUUID()) : null;
        DroneDefinition definition = entity.oEuCBB();

        // Model and texture per drone definition (1.1.8 DroneModel -> DroneEntity.rYlh() / sZzOK7T()).
        Identifier modelPath = entity.rYlh();
        state.addGeckolibData(DroneModel.MODEL_PATH, modelPath);
        state.addGeckolibData(DroneModel.TEXTURE_PATH, entity.sZzOK7T());

        Quaternionf orientation;
        if (physics != null) {
            // The local pilot's drone follows the client physics directly, at the player's interpolated position.
            orientation = physicsOrientation(physics);
            state.addGeckolibData(
                LOCAL_OFFSET,
                new Vec3(
                    Mth.lerp(partialTick, mc.player.xOld, mc.player.getX()) - Mth.lerp(partialTick, entity.xOld, entity.getX()),
                    Mth.lerp(partialTick, mc.player.yOld, mc.player.getY()) - Mth.lerp(partialTick, entity.yOld, entity.getY()),
                    Mth.lerp(partialTick, mc.player.zOld, mc.player.getZ()) - Mth.lerp(partialTick, entity.zOld, entity.getZ())
                )
            );
        } else {
            orientation = worldOrientation(entity, partialTick);
        }

        // 1.1.8 kNWIu: the definition's model yaw offset, for models built facing another way.
        float yawOffset = entity.kNWIu();
        if (yawOffset != 0.0F) {
            orientation.rotateY(yawOffset * Mth.DEG_TO_RAD);
        }

        boolean gpsDrone = entity.xogV5KBhg();
        boolean thermalFpv = isLocalThermalFpv(entity);
        state.addGeckolibData(ORIENTATION, orientation);
        state.addGeckolibData(HIDE_CAMERA, isLookingThroughCamera(mc));
        state.addGeckolibData(LOCAL_THERMAL_FPV, thermalFpv);
        state.addGeckolibData(PROP_ANGLE, entity.getPropAngle(partialTick));
        state.addGeckolibData(THROTTLE, getThrottleNormalized(entity));
        state.addGeckolibData(GPS_DRONE, gpsDrone);
        // 1.1.8: a GPS drone (Mavic) flown in first person hides its own props; they would fill the camera view.
        state.addGeckolibData(HIDE_PROPS, gpsDrone && isOwnDrone && mc.options.getCameraType().isFirstPerson());
        // 1.1.8 qV33e: props only fade into blur discs when the definition has propeller blur.
        state.addGeckolibData(PROP_BLUR, !thermalFpv && entity.qV33e());
        if (gpsDrone) {
            state.addGeckolibData(GIMBAL_ROTATION, gimbalRotation(entity, physics, partialTick));
        }

        Vec3 entityPos = new Vec3(
            Mth.lerp(partialTick, entity.xOld, entity.getX()), Mth.lerp(partialTick, entity.yOld, entity.getY()), Mth.lerp(partialTick, entity.zOld, entity.getZ())
        );
        state.addGeckolibData(ATTACHMENTS, collectAttachments(entity, definition, orientation, partialTick, entityPos));
    }

    /**
     * Everything 1.1.8 drew after the drone model, in its order: hanging payload (v5suI), battery (ibzfxic), payload mount
     * (wuhB), bomb-bay stations (dOfFwvo).
     */
    private static Attachments collectAttachments(
        DroneEntity entity, DroneDefinition definition, Quaternionf orientation, float partialTick, Vec3 entityPos
    ) {
        List<Attachment> list = new ArrayList<>();

        // Hanging payload (grenade, RPG, ...) on payload_mount, swinging on its hang point.
        String payloadId = entity.i263D9();
        if (payloadId != null && !payloadId.isEmpty()) {
            ProjectileDefinition projectile = ProjectileDefinitions.ibzfxic(payloadId).orElse(null);
            if (projectile != null) {
                BakedGeoModel model = ProjectileModels.dOfFwvo(projectile);
                if (model != null) {
                    AttachTransform attach = definition.dOfFwvo(payloadId).orElse(AttachTransform.v5suI(PAYLOAD_MOUNT));
                    // 1.1.8 Record_SRQuVonquFV.wEJx3Itb(): only hanging payloads swing and publish their anchor.
                    boolean hangs = projectile.wEJx3Itb();
                    list.add(
                        new Attachment(
                            PAYLOAD_MOUNT,
                            model,
                            ProjectileModels.ibzfxic(projectile),
                            attach,
                            hangs ? swingRotation(entity, orientation, partialTick) : null,
                            hangs ? ANCHOR_HANGING : ANCHOR_NONE
                        )
                    );
                }
            }
        }

        // Battery on battery_mount (1.1.8 RL83V3raAx gives the battery's model and texture).
        if (entity.idXga()) {
            BatteryDefinition battery = BatteryDefinitions.ibzfxic(entity.getBattery().getTypeId());
            BakedGeoModel model = BedrockModels.v5suI(Identifier.fromNamespaceAndPath("fpvdrone", battery.c1jHYPb4()));
            if (model != null) {
                AttachTransform attach = definition.uSUb(battery.wuhB()).orElse(AttachTransform.v5suI(BATTERY_MOUNT));
                list.add(new Attachment(BATTERY_MOUNT, model, Identifier.fromNamespaceAndPath("fpvdrone", battery.v8ZN()), attach, null, ANCHOR_NONE));
            }
        }

        // Payload mount (e.g. the Mavic's VOG holder). With a bomb bay it only shows while a station is still loaded.
        if (definition.uSUb() && (!definition.jGhI2() || entity.xYttSy())) {
            DroneDefinition.Mount mount = definition.mM7DsB9AR();
            BakedGeoModel model = BedrockModels.v5suI(Identifier.fromNamespaceAndPath("fpvdrone", mount.model()));
            if (model != null) {
                list.add(new Attachment(PAYLOAD_MOUNT, model, Identifier.fromNamespaceAndPath("fpvdrone", mount.texture()), mount.attach(), null, ANCHOR_NONE));
            }
        }

        // Bomb-bay stations: one projectile per loaded station, each with its own anchor.
        if (definition.jGhI2()) {
            DroneDefinition.BombBay bay = definition.uKpZYj();
            ProjectileDefinition projectile = ProjectileDefinitions.ibzfxic(bay.projectile()).orElse(null);
            if (projectile != null) {
                BakedGeoModel model = ProjectileModels.dOfFwvo(projectile);
                if (model != null) {
                    Identifier texture = ProjectileModels.ibzfxic(projectile);
                    List<PayloadStation> stations = bay.stations();
                    for (int i = 0; i < stations.size(); i++) {
                        if (entity.ibzfxic(i)) {
                            list.add(new Attachment(PAYLOAD_MOUNT, model, texture, stations.get(i).wuhB(), null, i));
                        }
                    }
                }
            }
        }

        return new Attachments(entity.getUUID(), entityPos, List.copyOf(list));
    }

    /**
     * 1.1.8 ibzfxic(DroneEntity, float): the hanging payload's swing. It undoes the drone's attitude (so the payload hangs
     * from gravity, not from the frame) and then applies the simulated swing pitch and roll (DroneEntity.wuhB).
     */
    private static Quaternionf swingRotation(DroneEntity entity, Quaternionf orientation, float partialTick) {
        float[] swing = entity.wuhB(partialTick);
        return new Quaternionf(orientation).conjugate().rotateZ((float)Math.toRadians(swing[1])).rotateX((float)Math.toRadians(swing[0]));
    }

    /**
     * 1.1.8 camera_block_Y rotation (from dOfFwvo(DroneEntity, float)): the gimbal holds the camera level, so it cancels
     * the drone's pitch and roll and keeps the pilot's gimbal tilt. Radians, as bone rotations (x, unused, z).
     */
    private static Vector3f gimbalRotation(DroneEntity entity, @Nullable PhysicsState physics, float partialTick) {
        float pitch;
        float roll;
        if (physics != null) {
            float[] angles = yawPitchRoll(physics.getDroneLook(), physics.getDroneUp());
            pitch = angles[1];
            roll = angles[2];
        } else if (entity.isBeingControlled()) {
            Quaternionf q = entity.dOfFwvo(partialTick);
            float[] angles = yawPitchRoll(q.transform(new Vector3f(0.0F, 0.0F, -1.0F)), q.transform(new Vector3f(0.0F, 1.0F, 0.0F)));
            pitch = angles[1];
            roll = angles[2];
        } else {
            pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            roll = entity.ibzfxic(partialTick);
        }

        // 1.1.8 used the local pilot's live gimbal (HD4RT1Kn.v5suI().ibzfxic()) for its own drone, the synced tilt
        // (DroneEntity.n9lX8A) for everyone else.
        // 1.1.8-PENDING: HD4RT1Kn (local gimbal tilt with key/stick rate and recoil kick); the synced tilt is used meanwhile.
        float tilt = entity.n9lX8A();
        return new Vector3f((float)Math.toRadians(-(pitch - tilt)), 0.0F, (float)Math.toRadians(-roll));
    }

    /**
     * 1.1.8 Transforms.v5suI(Vector3f, Vector3f): yaw, pitch and roll in degrees of a look/up pair, via a YXZ Euler
     * decomposition. Kept here until Transforms itself is moved to 1.1.8.
     */
    static float[] yawPitchRoll(Vector3f look, Vector3f up) {
        Vector3f forward = new Vector3f(look);
        if (forward.lengthSquared() < 1.0E-8F) {
            return new float[]{0.0F, 0.0F, 0.0F};
        }

        forward.normalize();
        Vector3f side = new Vector3f();
        new Vector3f(up).cross(forward, side);
        if (side.lengthSquared() < 1.0E-8F) {
            side.set(1.0F, 0.0F, 0.0F).cross(forward);
            if (side.lengthSquared() < 1.0E-8F) {
                new Vector3f(0.0F, 0.0F, 1.0F).cross(forward, side);
            }
        }

        side.normalize();
        Vector3f realUp = new Vector3f();
        new Vector3f(forward).cross(side, realUp);
        realUp.normalize();
        Matrix3f basis = new Matrix3f(side.x, side.y, side.z, realUp.x, realUp.y, realUp.z, forward.x, forward.y, forward.z);
        Vector3f euler = new Quaternionf().setFromNormalized(basis).getEulerAnglesYXZ(new Vector3f());
        return new float[]{-euler.y * Mth.RAD_TO_DEG, euler.x * Mth.RAD_TO_DEG, euler.z * Mth.RAD_TO_DEG};
    }

    @Override
    protected void applyRotations(RenderPassInfo<LivingEntityRenderState> renderPassInfo, PoseStack poseStack, float nativeScale) {
        LivingEntityRenderState renderState = renderPassInfo.renderState();
        GeoRenderState state = (GeoRenderState)renderState;
        Vec3 offset = state.getGeckolibData(LOCAL_OFFSET);
        if (offset != null) {
            poseStack.translate(offset.x, offset.y, offset.z);
        }

        Quaternionf orientation = state.getGeckolibData(ORIENTATION);
        if (orientation != null) {
            poseStack.rotate(orientation);
        } else {
            poseStack.rotate(Axis.YP.rotationDegrees(180.0F - renderState.bodyRot));
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        GeoRenderState state = (GeoRenderState)renderPassInfo.renderState();
        boolean hideCamera = state.getOrDefaultGeckolibData(HIDE_CAMERA, false);
        boolean thermalFpv = state.getOrDefaultGeckolibData(LOCAL_THERMAL_FPV, false);
        boolean gpsDrone = state.getOrDefaultGeckolibData(GPS_DRONE, false);
        boolean hideProps = state.getOrDefaultGeckolibData(HIDE_PROPS, false);
        boolean propBlur = state.getOrDefaultGeckolibData(PROP_BLUR, !thermalFpv);
        float propAngle = state.getOrDefaultGeckolibData(PROP_ANGLE, 0.0F);
        float throttle = state.getOrDefaultGeckolibData(THROTTLE, 0.0F);

        // Mount bones only mark where attachments go; 1.1.8 never drew them (or anything under them).
        snapshots.ifPresent(PAYLOAD_MOUNT, b -> b.skipRender(true).skipChildrenRender(true));
        snapshots.ifPresent(BATTERY_MOUNT, b -> b.skipRender(true).skipChildrenRender(true));

        // Camera: hidden while looking through it. A fixed camera takes the configured uptilt; a gimbal keeps its own pose.
        snapshots.ifPresent("camera", b -> {
            b.skipRender(hideCamera).skipChildrenRender(hideCamera);
            if (!gpsDrone) {
                b.setRotX((float)Math.toRadians(FpvConfig.getDefaultAngle()));
            }
        });
        if (gpsDrone) {
            Vector3f gimbal = state.getGeckolibData(GIMBAL_ROTATION);
            snapshots.ifPresent("camera_block_Y", b -> {
                b.skipRender(hideCamera).skipChildrenRender(hideCamera);
                if (gimbal != null) {
                    b.setRotX(gimbal.x).setRotZ(gimbal.z);
                }
            });
        }

        snapshots.ifPresent("kirpichiki", b -> b.skipRender(true).skipChildrenRender(true));
        for (String blur : BLUR_BONES) {
            snapshots.ifPresent(blur, b -> b.skipRender(true).skipChildrenRender(true));
        }

        // Solid props below 25% throttle, or always when the definition has no blur (or through a thermal camera);
        // fading props are drawn by their own tasks; above 55% only the blur discs show.
        boolean solidProps = !hideProps && (!propBlur || throttle < PROP_FADE_START);
        Map<String, Vector3f> axes = gpsDrone ? propAxes(state.getOrDefaultGeckolibData(DroneModel.MODEL_PATH, DroneModel.MODEL), renderPassInfo.model()) : null;
        for (String prop : PROP_BONES) {
            snapshots.ifPresent(prop, b -> {
                b.skipRender(!solidProps);
                if (axes == null) {
                    b.setRotation(0.0F, propAngle, 0.0F);
                } else {
                    // A tilted prop spins about its own axis; GeckoLib bones take Z-Y-X Euler angles.
                    Vector3f axis = axes.getOrDefault(prop, UP);
                    Vector3f euler = new Quaternionf().fromAxisAngleRad(axis.x, axis.y, axis.z, propAngle).getEulerAnglesZYX(new Vector3f());
                    b.setRotation(euler.x, euler.y, euler.z);
                }
            });
        }
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        GeoRenderState state = (GeoRenderState)renderPassInfo.renderState();
        Identifier texture = this.getTextureLocation(renderPassInfo.renderState());

        float throttle = state.getOrDefaultGeckolibData(THROTTLE, 0.0F);
        boolean hideProps = state.getOrDefaultGeckolibData(HIDE_PROPS, false);
        boolean propBlur = state.getOrDefaultGeckolibData(PROP_BLUR, false);
        if (!hideProps && propBlur && throttle >= PROP_FADE_START && !(throttle > PROP_FADE_END)) {
            float t = (throttle - PROP_FADE_START) / (PROP_FADE_END - PROP_FADE_START);
            int baseColor = renderPassInfo.renderColor();
            int color = ARGB.color(Math.round(ARGB.alpha(baseColor) * (1.0F - t)), baseColor);
            RenderType noDepth = PropRenderTypes.translucentNoDepthWrite(texture);
            for (String prop : PROP_BONES) {
                renderPassInfo.model()
                    .getBone(prop)
                    .ifPresent(bone -> renderPassInfo.addPerBoneRender(bone, (info, b, collector) -> submitBoneCubes(info, b, collector, noDepth, color)));
            }
        }

        Attachments attachments = state.getGeckolibData(ATTACHMENTS);
        if (attachments != null) {
            for (Attachment attachment : attachments.list()) {
                renderPassInfo.model()
                    .getBone(attachment.bone())
                    .ifPresent(
                        bone -> renderPassInfo.addPerBoneRender(
                            bone, (info, b, collector) -> submitAttachment(info, b, collector, attachment, attachments.droneId(), attachments.entityPos())
                        )
                    );
            }
        }
    }

    /**
     * 1.1.8 v5suI(DroneEntity, Matrix4f, Matrix3f, BakedGeoModel, ResourceLocation, Record_VPNwKsCM, ...): draws one
     * attachment at its mount bone. The pose stack arrives on the bone's pivot; backing off the pivot gives the pose 1.1.8
     * captured with prepMatrixForBone. Then the attach transform, the optional swing, the anchor, and the model.
     */
    private static void submitAttachment(
        RenderPassInfo<LivingEntityRenderState> info, GeoBone bone, SubmitNodeCollector collector, Attachment attachment, UUID droneId, Vec3 entityPos
    ) {
        PoseStack poseStack = new PoseStack();
        poseStack.last().set(info.poseStack().last());
        bone.translateAwayFromPivotPoint(poseStack);
        BedrockModelRenderer.ibzfxic(attachment.attach(), poseStack);
        Quaternionf swing = attachment.swing();
        if (swing != null) {
            poseStack.translate(SWING_PIVOT_X / 16.0F, SWING_PIVOT_Y / 16.0F, SWING_PIVOT_Z / 16.0F);
            poseStack.rotate(swing);
            poseStack.translate(-SWING_PIVOT_X / 16.0F, -SWING_PIVOT_Y / 16.0F, -SWING_PIVOT_Z / 16.0F);
        }

        if (attachment.anchor() != ANCHOR_NONE) {
            // 1.1.8 RenderUtils.invertAndMultiplyMatrices(pose, entityRenderTranslations): the payload's pose relative to
            // the entity's render origin (camera translation and view rotation removed), then moved to world space.
            Matrix4f local = RenderUtil.extractPoseFromRoot(poseStack.last().pose(), info.getPreRenderMatrixState());
            Vector3f t = local.getTranslation(new Vector3f());
            Vec3 worldPos = entityPos.add(t.x, t.y, t.z);
            Quaternionf rotation = local.getNormalizedRotation(new Quaternionf());
            if (attachment.anchor() == ANCHOR_HANGING) {
                PayloadAnchors.v5suI(droneId, worldPos, rotation);
            } else {
                PayloadAnchors.v5suI(droneId, attachment.anchor(), worldPos, rotation);
            }
        }

        BedrockModelRenderer.v5suI(
            attachment.model(), RenderTypes.entityTranslucentCull(attachment.texture()), poseStack, collector, info.packedLight(), OverlayTexture.NO_OVERLAY
        );
    }

    /** Draws one bone's own cubes at the bone's posed transform (the pose stack arrives on the bone's pivot point). */
    private static void submitBoneCubes(
        RenderPassInfo<LivingEntityRenderState> info, GeoBone bone, SubmitNodeCollector collector, RenderType renderType, int color
    ) {
        if (!(bone instanceof CuboidGeoBone cuboid)) {
            return;
        }

        int light = info.packedLight();
        int overlay = info.packedOverlay();
        collector.submitCustomGeometry(info.poseStack(), renderType, (pose, buffer) -> {
            PoseStack poseStack = new PoseStack();
            poseStack.last().set(pose);
            bone.translateAwayFromPivotPoint(poseStack);
            renderCubes(cuboid, poseStack, buffer, light, overlay, color);
        });
    }

    private static void renderCubes(CuboidGeoBone bone, PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        for (GeoCube cube : bone.cubes) {
            poseStack.pushPose();
            cube.render(poseStack, buffer, light, overlay, color);
            poseStack.popPose();
        }
    }

    /**
     * 1.1.8 v5suI(BakedGeoModel) cached per model id: each prop's spin axis is the up vector of its blur bone's first
     * cube rotation (Z-Y-X, as GeckoLib bakes it). Props without a blur bone spin about Y.
     */
    private static Map<String, Vector3f> propAxes(Identifier modelId, BakedGeoModel model) {
        return PROP_AXES.computeIfAbsent(modelId, id -> {
            Map<String, Vector3f> axes = new HashMap<>();
            for (Map.Entry<String, String> entry : PROP_TO_BLUR.entrySet()) {
                Vector3f axis = new Vector3f(UP);
                GeoBone blur = model.getBone(entry.getValue()).orElse(null);
                if (blur instanceof CuboidGeoBone cuboid && cuboid.cubes.length > 0) {
                    Vec3 rot = cuboid.cubes[0].rotation();
                    axis = new Quaternionf().rotationZ((float)rot.z).rotateY((float)rot.y).rotateX((float)rot.x).transform(new Vector3f(UP)).normalize();
                }

                axes.put(entry.getKey(), axis);
            }

            return axes;
        });
    }

    /** Orientation from the local client physics (the drone the local player is flying). */
    static Quaternionf physicsOrientation(PhysicsState physics) {
        Vector3f look = physics.getDroneLook();
        Vector3f up = physics.getDroneUp();
        Vector3f left = physics.getDroneLeft();
        Matrix3f rot = new Matrix3f();
        rot.m00(-left.x);
        rot.m01(-left.y);
        rot.m02(-left.z);
        rot.m10(up.x);
        rot.m11(up.y);
        rot.m12(up.z);
        rot.m20(-look.x);
        rot.m21(-look.y);
        rot.m22(-look.z);
        return new Quaternionf().setFromNormalized(rot);
    }

    /** Orientation of a drone flown by someone else (synced quaternion) or resting (yaw, pitch, roll). */
    static Quaternionf worldOrientation(DroneEntity entity, float partialTick) {
        if (entity.isBeingControlled()) {
            return new Quaternionf(entity.getRemoteOrientation(partialTick));
        }

        float yaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        return new Quaternionf()
            .rotateY((180.0F - yaw) * Mth.DEG_TO_RAD)
            .rotateX(pitch * Mth.DEG_TO_RAD)
            .rotateZ(entity.getDroneRoll(partialTick) * Mth.DEG_TO_RAD);
    }

    private static boolean isOwnDrone(Minecraft mc, DroneEntity entity) {
        return mc.player != null && FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(entity.getUUID())).orElse(false);
    }

    /** True when the local player looks through this thermal drone's own camera in first person. */
    static boolean isLocalThermalFpv(DroneEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        return entity.isThermal() && mc.options.getCameraType().isFirstPerson() && isOwnDrone(mc, entity);
    }

    /** First-person view through the drone camera: hide the camera bone so it does not block the view. */
    private static boolean isLookingThroughCamera(Minecraft mc) {
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            return false;
        }

        if (FPVMod.getPhysicsState(mc.player.getUUID()) != null) {
            return true;
        }

        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.getItem() instanceof FpvGogglesItem && FpvGogglesItem.getLinkedDroneId(helmet).isPresent();
    }

    /**
     * 0..1 throttle (1.1.8 ibzfxic(DroneEntity)): live stick for the local pilot, otherwise the synced throttle
     * (DroneEntity.hXya). Armed props never drop below 10% in flight or 15% idling, so they keep turning.
     */
    static float getThrottleNormalized(DroneEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (isOwnDrone(mc, entity)) {
            PhysicsState physics = FPVMod.getPhysicsState(mc.player.getUUID());
            if (physics != null && physics.isMotorsArmed()) {
                if (physics.isThrottleCut()) {
                    return 0.0F;
                }

                return Math.max(0.1F, Mth.clamp((ControllerReader.getThrottle() + 1.0F) / 2.0F, 0.0F, 1.0F));
            }
        }

        if (entity.areMotorsArmed() && entity.isBeingControlled()) {
            return Math.max(0.1F, entity.hXya());
        }

        return entity.areMotorsArmed() ? Math.max(0.15F, entity.hXya()) : 0.0F;
    }

    /** One model drawn on a mount bone (1.1.8 passed these as arguments to its attachment draw). */
    private record Attachment(String bone, BakedGeoModel model, Identifier texture, AttachTransform attach, @Nullable Quaternionf swing, int anchor) {
    }

    /** Per-frame attachment list for one drone, with what the anchors need. */
    private record Attachments(UUID droneId, Vec3 entityPos, List<Attachment> list) {
    }

    /**
     * Translucent entity type (back faces culled) that does not write depth, so a fading prop never hides what is behind it.
     * Same pipeline as vanilla's entity_translucent_cull apart from the depth write. (1.1.8 Ol3TEDSog.)
     */
    static final class PropRenderTypes {
        private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("fpvdrone", "pipeline/translucent_no_depth"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                .build()
        );
        private static final Function<Identifier, RenderType> TRANSLUCENT_NO_DEPTH = Util.memoize(
            texture -> RenderType.create(
                "fpvdrone_translucent_no_depth",
                RenderSetup.builder(PIPELINE)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .sortOnUpload()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup()
            )
        );

        private PropRenderTypes() {
        }

        static void init() {
            // Loading this class registers the pipeline.
        }

        static RenderType translucentNoDepthWrite(Identifier texture) {
            return TRANSLUCENT_NO_DEPTH.apply(texture);
        }
    }
}
