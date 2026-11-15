package com.fpvdrone.client.renderer;

import com.fpvdrone.client.model.BedrockModels;
import com.fpvdrone.client.model.BedrockPolyMeshes;
import com.fpvdrone.client.model.ProjectileModels;
import com.fpvdrone.client.model.RampGeoModel;
import com.fpvdrone.data.AttachTransform;
import com.fpvdrone.data.BoneBox;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.DroneDefinitions;
import com.fpvdrone.data.GeoBoneBoxes;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.data.OrientedBox;
import com.fpvdrone.data.PayloadStation;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.data.RocketMount;
import com.fpvdrone.entity.RampEntity;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 Renderer_XkC4IsIc: the launch ramp (GeckoLib model from its ramp definition) and the drone installed on it.
 * <ul>
 *   <li>The ramp turns with its interpolated yaw and has no shadow.</li>
 *   <li>The installed drone's fixed-wing model sits on the definition's attach bone (drone_mount by default) with the
 *       definition's per-drone attach transform; its blur bones are hidden, empty rocket-pod mounts are hidden, and the
 *       rockets loaded on the ramp are drawn at their stations.</li>
 *   <li>With hitboxes shown: the ramp's oriented collision boxes, and the drone's bone boxes (stations of empty mounts
 *       left out) with interact zones in orange.</li>
 * </ul>
 * 1.1.8 caught the attach bone's matrix in renderRecursively and drew the drone after the ramp model. GeckoLib 5 has no
 * such hook, so a per-bone render task on the attach bone draws it with the same pose (as {@link DroneRenderer} does
 * for its attachments). Not ported: 1.1.8's live attach-offset tuner (XsK2uxud, a developer key tool whose offset is
 * zero unless toggled with the keypad) and an unused per-drone GeoModel cache (GeoModel_GcecmniuY, never filled).
 */
public class RampRenderer extends GeoEntityRenderer<RampEntity, EntityRenderState> {
    private static final DataTicket<Float> YAW = DataTicket.create("fpvdrone_ramp_yaw", Float.class);
    private static final DataTicket<MountedDrone> DRONE = DataTicket.create("fpvdrone_ramp_drone", MountedDrone.class);
    private static final DataTicket<float[][]> HITBOXES = DataTicket.create("fpvdrone_ramp_hitboxes", float[][].class);

    public RampRenderer(Context context) {
        super(context, new RampGeoModel());
        this.shadowRadius = 0.0F;
    }

    /** 1.1.8: RenderType.entityCutout, which culled back faces in 1.20.1 (26.3 entityCutoutCull). */
    @Override
    public @Nullable RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutoutCull(texture);
    }

    @Override
    public void addRenderData(RampEntity entity, @Nullable Void relatedObject, EntityRenderState renderState, float partialTick) {
        GeoRenderState state = (GeoRenderState)renderState;
        state.addGeckolibData(RampGeoModel.MODEL_PATH, Identifier.fromNamespaceAndPath("fpvdrone", entity.jGhI2().uSUb()));
        state.addGeckolibData(RampGeoModel.TEXTURE_PATH, Identifier.fromNamespaceAndPath("fpvdrone", entity.jGhI2().jGhI2()));
        state.addGeckolibData(YAW, Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()));
        MountedDrone drone = entity.rsDd50tfV() ? mountedDrone(entity) : null;
        if (drone != null) {
            state.addGeckolibData(DRONE, drone);
        }

        if (HitboxLines.enabled() && !entity.isInvisible()) {
            List<OrientedBox> boxes = entity.ibzfxic(partialTick);
            if (!boxes.isEmpty()) {
                state.addGeckolibData(HITBOXES, HitboxLines.relativeCorners(boxes, entity.getPosition(partialTick)));
            }
        }
    }

    /** Everything 1.1.8's v5suI(RampEntity, PoseStack, MultiBufferSource, int, float) read from the ramp and the drone. */
    private static @Nullable MountedDrone mountedDrone(RampEntity ramp) {
        String droneId = ramp.dOfFwvo();
        DroneDefinition definition = DroneDefinitions.ibzfxic(droneId);
        Identifier modelId = Identifier.fromNamespaceAndPath("fpvdrone", definition.iElAbEZe());
        BakedGeoModel model = BedrockModels.v5suI(modelId);
        if (model == null) {
            return null;
        }

        FixedWingModelRenderer.BonePose pose = new FixedWingModelRenderer.BonePose();
        for (Map.Entry<String, GeoBone> entry : model.boneLookup().get().entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ROOT).contains("blur")) {
                pose.hide(entry.getValue());
            }
        }

        if (definition.wEJx3Itb()) {
            List<PayloadStation> stations = definition.jt9oI60ST().rsDd50tfV();
            List<RocketMount> mounts = definition.jt9oI60ST().ibzfxic();
            for (int i = 0; i < mounts.size(); i++) {
                RocketMount mount = mounts.get(i);
                List<String> bones = stations.stream().filter(s -> mount.wuhB().contains(s.ibzfxic())).map(s -> s.wuhB().v5suI()).toList();
                Set<GeoBone> resolved = BedrockModelRenderer.resolveBones(model, bones);
                if (ramp.v5suI(i)) {
                    pose.showAll(resolved);
                } else {
                    pose.hideAll(resolved);
                }
            }
        }

        AttachTransform attach = ramp.jGhI2().ibzfxic(droneId);
        return new MountedDrone(
            attach.v5suI(),
            attach,
            model,
            BedrockPolyMeshes.get(modelId),
            Identifier.fromNamespaceAndPath("fpvdrone", definition.sTrE()),
            pose,
            rockets(ramp, definition),
            HitboxLines.enabled() ? boneBoxes(ramp, definition) : List.of(),
            interactNames(definition)
        );
    }

    /** 1.1.8 v5suI(RampEntity, DroneDefinition, Matrix4f, ...): rockets loaded on the ramp, at their stations. */
    private static List<Rocket> rockets(RampEntity ramp, DroneDefinition definition) {
        if (!definition.wEJx3Itb() || !ramp.wEJx3Itb()) {
            return List.of();
        }

        DroneDefinition.RocketPod pod = definition.jt9oI60ST();
        List<PayloadStation> stations = pod.rsDd50tfV();
        List<Rocket> out = new ArrayList<>();
        for (int i = 0; i < stations.size(); i++) {
            if (!ramp.ibzfxic(i)) {
                continue;
            }

            PayloadStation station = stations.get(i);
            ProjectileDefinition projectile = ProjectileDefinitions.find(station.v5suI(pod.dOfFwvo())).orElse(null);
            if (projectile == null) {
                continue;
            }

            BakedGeoModel model = ProjectileModels.dOfFwvo(projectile);
            if (model == null) {
                continue;
            }

            AttachTransform attach = station.wuhB();
            out.add(new Rocket(model, ProjectileModels.ibzfxic(projectile), attach, GeoBoneBoxes.ibzfxic(definition.hOKMYHOaC(), attach.v5suI())));
        }

        return out;
    }

    /** 1.1.8 v5suI(RampEntity, DroneDefinition, PoseStack, MultiBufferSource): the drone's bone boxes for F3+B. */
    private static List<BoneBox> boneBoxes(RampEntity ramp, DroneDefinition definition) {
        String collision = definition.hOKMYHOaC();
        if (collision.isEmpty()) {
            return List.of();
        }

        List<BoneBox> boxes = GeoBoneBoxes.ibzfxic(collision);
        if (boxes.isEmpty()) {
            return List.of();
        }

        if (definition.wEJx3Itb()) {
            Set<String> stationNames = definition.jt9oI60ST().v5suI();
            boxes = boxes.stream()
                .filter(b -> !stationNames.contains(b.rsDd50tfV()) || ramp.v5suI(definition.jt9oI60ST().v5suI(b.rsDd50tfV())))
                .toList();
        }

        List<BoneBox> zones = GeoBoneBoxes.dOfFwvo(collision);
        return zones.isEmpty() ? boxes : Stream.concat(boxes.stream(), zones.stream()).toList();
    }

    private static Set<String> interactNames(DroneDefinition definition) {
        List<InteractZone> zones = definition.c1jHYPb4();
        if (zones.isEmpty()) {
            return Set.of();
        }

        Set<String> names = new HashSet<>();
        for (InteractZone zone : zones) {
            names.add(zone.ibzfxic());
        }

        return names;
    }

    /** 1.1.8 v5suI(RampEntity, PoseStack, float, float, float): turn by the interpolated yaw. */
    @Override
    protected void applyRotations(RenderPassInfo<EntityRenderState> renderPassInfo, PoseStack poseStack, float nativeScale) {
        GeoRenderState state = (GeoRenderState)renderPassInfo.renderState();
        poseStack.rotate(Axis.YP.rotationDegrees(180.0F - state.getOrDefaultGeckolibData(YAW, 0.0F)));
    }

    @Override
    public void preRenderPass(RenderPassInfo<EntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        GeoRenderState state = (GeoRenderState)renderPassInfo.renderState();
        MountedDrone drone = state.getGeckolibData(DRONE);
        if (drone != null) {
            renderPassInfo.model()
                .getBone(drone.bone())
                .ifPresent(bone -> renderPassInfo.addPerBoneRender(bone, (info, b, collector) -> submitDrone(info, b, collector, drone)));
        }
    }

    /**
     * Draws the installed drone at the attach bone. The pose stack arrives on the bone's pivot; backing off the pivot gives
     * the matrix 1.1.8 captured with prepMatrixForBone.
     */
    private static void submitDrone(RenderPassInfo<EntityRenderState> info, GeoBone bone, SubmitNodeCollector collector, MountedDrone drone) {
        PoseStack poseStack = new PoseStack();
        poseStack.last().set(info.poseStack().last());
        bone.translateAwayFromPivotPoint(poseStack);
        BedrockModelRenderer.ibzfxic(drone.attach(), poseStack);
        int light = info.packedLight();
        Identifier texture = drone.texture();
        // 1.20.1 entityCutout culled back faces (26.3 entityCutoutCull); the library drew poly meshes two-sided.
        FixedWingModelRenderer.submit(
            drone.model(),
            drone.meshes(),
            drone.pose(),
            poseStack,
            collector,
            RenderTypes.entityCutoutCull(texture),
            RenderTypes.entityCutout(texture),
            light,
            OverlayTexture.NO_OVERLAY,
            -1
        );
        Matrix4f base = new Matrix4f(poseStack.last().pose());
        for (Rocket rocket : drone.rockets()) {
            BedrockModelRenderer.v5suI(new Matrix4f(base).mul(rocket.boneMatrix()), rocket.model(), rocket.texture(), rocket.attach(), poseStack, collector, light);
        }

        if (!drone.boneBoxes().isEmpty()) {
            float width = HitboxLines.width();
            collector.submitCustomGeometry(
                poseStack, RenderTypes.lines(), (pose, buffer) -> HitboxLines.drawBoneBoxes(pose, buffer, drone.boneBoxes(), drone.interactNames(), width)
            );
        }
    }

    /** The ramp model, then (1.1.8 render()) its collision boxes with hitboxes on. */
    @Override
    public void submit(EntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector renderTasks, CameraRenderState cameraState) {
        super.submit(renderState, poseStack, renderTasks, cameraState);
        float[][] boxes = ((GeoRenderState)renderState).getGeckolibData(HITBOXES);
        if (boxes != null) {
            float width = HitboxLines.width();
            renderTasks.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                for (float[] box : boxes) {
                    HitboxLines.drawCorners(pose, buffer, box, false, width);
                }
            });
        }
    }

    private record Rocket(BakedGeoModel model, Identifier texture, AttachTransform attach, Matrix4f boneMatrix) {
    }

    private record MountedDrone(
        String bone,
        AttachTransform attach,
        BakedGeoModel model,
        Map<String, float[]> meshes,
        Identifier texture,
        FixedWingModelRenderer.BonePose pose,
        List<Rocket> rockets,
        List<BoneBox> boneBoxes,
        Set<String> interactNames
    ) {
    }
}
