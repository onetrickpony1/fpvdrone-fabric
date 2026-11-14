package com.fpvdrone.client.renderer;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.model.DroneModel;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.physics.PhysicsState;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.Set;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * GeckoLib 5 renderer for the drone. Everything the Forge renderer read from the entity at draw time is captured into the
 * render state in {@link #addRenderData} and applied in {@link #adjustModelBonesForRender} / {@link #applyRotations}.
 * <p>
 * The Forge version hooked renderRecursively per bone; GeckoLib 5 has no such hook, so the two special cases use
 * per-bone render tasks instead:
 * <ul>
 *   <li>the RPG is left out of the main pass and drawn straight after the rest of the model (it was deferred in the original);</li>
 *   <li>propellers between 25% and 55% throttle fade out, drawn with a translucent type that does not write depth.</li>
 * </ul>
 * The spinning blur discs and the launch bricks are drawn by {@link BlurDiscRenderer} and {@link KirpichikiRenderer}.
 */
public class DroneRenderer extends GeoEntityRenderer<DroneEntity, LivingEntityRenderState> {
    static final float PROP_FADE_START = 0.25F;
    static final float PROP_FADE_END = 0.55F;
    private static final Set<String> PROP_BONES = Set.of("vintleftfront", "vintrightfront", "vintleftback", "vintrightback");
    private static final Set<String> BLUR_BONES = Set.of("vintleftfrontblur", "vintrightfrontblur", "vintleftbackblur", "vintrightbackblur");

    private static final DataTicket<Quaternionf> ORIENTATION = DataTicket.create("fpvdrone_orientation", Quaternionf.class);
    private static final DataTicket<Vec3> LOCAL_OFFSET = DataTicket.create("fpvdrone_local_offset", Vec3.class);
    private static final DataTicket<Boolean> HAS_BATTERY = DataTicket.create("fpvdrone_has_battery", Boolean.class);
    private static final DataTicket<Boolean> HAS_RPG = DataTicket.create("fpvdrone_has_rpg", Boolean.class);
    private static final DataTicket<Boolean> HIDE_CAMERA = DataTicket.create("fpvdrone_hide_camera", Boolean.class);
    private static final DataTicket<Boolean> LOCAL_THERMAL_FPV = DataTicket.create("fpvdrone_local_thermal_fpv", Boolean.class);
    private static final DataTicket<Float> PROP_ANGLE = DataTicket.create("fpvdrone_prop_angle", Float.class);
    private static final DataTicket<Float> THROTTLE = DataTicket.create("fpvdrone_throttle", Float.class);

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

    @Override
    public @Nullable RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucentCull(texture);
    }

    @Override
    public void addRenderData(DroneEntity entity, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        GeoRenderState state = (GeoRenderState)renderState;
        renderState.lightCoords = fixLightInsideBlock(entity, renderState.lightCoords, partialTick);
        Minecraft mc = Minecraft.getInstance();
        boolean isOwnDrone = mc.player != null
            && FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(entity.getUUID())).orElse(false);
        PhysicsState physics = isOwnDrone ? FPVMod.getPhysicsState(mc.player.getUUID()) : null;

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

        state.addGeckolibData(ORIENTATION, orientation);
        state.addGeckolibData(HAS_BATTERY, entity.hasBattery());
        state.addGeckolibData(HAS_RPG, entity.hasRpg());
        state.addGeckolibData(HIDE_CAMERA, isLookingThroughCamera(mc));
        state.addGeckolibData(LOCAL_THERMAL_FPV, isLocalThermalFpv(entity));
        state.addGeckolibData(PROP_ANGLE, entity.getPropAngle(partialTick));
        state.addGeckolibData(THROTTLE, getThrottleNormalized(entity));
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
        boolean hasBattery = state.getOrDefaultGeckolibData(HAS_BATTERY, true);
        boolean hasRpg = state.getOrDefaultGeckolibData(HAS_RPG, false);
        boolean hideCamera = state.getOrDefaultGeckolibData(HIDE_CAMERA, false);
        boolean thermalFpv = state.getOrDefaultGeckolibData(LOCAL_THERMAL_FPV, false);
        float propAngle = state.getOrDefaultGeckolibData(PROP_ANGLE, 0.0F);
        float throttle = state.getOrDefaultGeckolibData(THROTTLE, 0.0F);
        snapshots.ifPresent("batka", b -> b.skipRender(!hasBattery).skipChildrenRender(!hasBattery));
        // A fitted RPG is drawn by its own task after the rest of the model, except through a thermal camera.
        boolean rpgInMainPass = hasRpg && thermalFpv;
        snapshots.ifPresent("rpg", b -> b.skipRender(!rpgInMainPass).skipChildrenRender(!rpgInMainPass));
        snapshots.ifPresent(
            "camera", b -> b.skipRender(hideCamera).skipChildrenRender(hideCamera).setRotX((float)Math.toRadians(FpvConfig.getDefaultAngle()))
        );
        snapshots.ifPresent("kirpichiki", b -> b.skipRender(true).skipChildrenRender(true));
        for (String blur : BLUR_BONES) {
            snapshots.ifPresent(blur, b -> b.skipRender(true).skipChildrenRender(true));
        }

        // Solid props below 25% throttle (always through a thermal camera); fading props are drawn by their own tasks.
        boolean solidProps = thermalFpv || throttle < PROP_FADE_START;
        for (String prop : PROP_BONES) {
            snapshots.ifPresent(prop, b -> b.skipRender(!solidProps).setRotY(propAngle));
        }
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        GeoRenderState state = (GeoRenderState)renderPassInfo.renderState();
        boolean thermalFpv = state.getOrDefaultGeckolibData(LOCAL_THERMAL_FPV, false);
        Identifier texture = this.getTextureLocation(renderPassInfo.renderState());
        if (state.getOrDefaultGeckolibData(HAS_RPG, false) && !thermalFpv) {
            RenderType mainType = this.getRenderType(renderPassInfo.renderState(), texture);
            if (mainType != null) {
                int color = renderPassInfo.renderColor();
                renderPassInfo.model()
                    .getBone("rpg")
                    .ifPresent(bone -> renderPassInfo.addPerBoneRender(bone, (info, b, collector) -> submitBoneCubes(info, b, collector, mainType, color)));
            }
        }

        float throttle = state.getOrDefaultGeckolibData(THROTTLE, 0.0F);
        if (!thermalFpv && throttle >= PROP_FADE_START && !(throttle > PROP_FADE_END)) {
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

    /** True when the local player looks through this thermal drone's own camera in first person. */
    static boolean isLocalThermalFpv(DroneEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        return entity.isThermal()
            && mc.player != null
            && mc.options.getCameraType().isFirstPerson()
            && FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(entity.getUUID())).orElse(false);
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

    /** 0..1 throttle: live stick for the local pilot, otherwise 0.7 when flown by someone, 0.15 when idling armed. */
    static float getThrottleNormalized(DroneEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            boolean isOwn = FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(entity.getUUID())).orElse(false);
            if (isOwn) {
                PhysicsState physics = FPVMod.getPhysicsState(mc.player.getUUID());
                if (physics != null && physics.isMotorsArmed()) {
                    if (physics.isThrottleCut()) {
                        return 0.0F;
                    }

                    return Mth.clamp((ControllerReader.getThrottle() + 1.0F) / 2.0F, 0.0F, 1.0F);
                }
            }
        }

        if (entity.areMotorsArmed() && entity.isBeingControlled()) {
            return 0.7F;
        }

        return entity.areMotorsArmed() ? 0.15F : 0.0F;
    }

    /**
     * Translucent entity type (back faces culled) that does not write depth, so a fading prop never hides what is behind it.
     * Same pipeline as vanilla's entity_translucent_cull apart from the depth write.
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
