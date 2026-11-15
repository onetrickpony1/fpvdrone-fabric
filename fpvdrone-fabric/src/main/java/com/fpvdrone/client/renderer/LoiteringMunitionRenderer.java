package com.fpvdrone.client.renderer;

import com.fpvdrone.client.MunitionClientState;
import com.fpvdrone.client.model.BedrockModels;
import com.fpvdrone.client.model.BedrockPolyMeshes;
import com.fpvdrone.client.model.ProjectileModels;
import com.fpvdrone.client.particle.FpvParticles;
import com.fpvdrone.data.AttachTransform;
import com.fpvdrone.data.BoneBox;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.GeoBoneBoxes;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.data.OrientedBox;
import com.fpvdrone.data.PayloadStation;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.data.RocketMount;
import com.fpvdrone.entity.LoiteringMunitionEntity;
import com.fpvdrone.entity.MunitionCollision;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 Renderer_O9i1A5c: draws a {@link LoiteringMunitionEntity} (FP-1, Lancet, Orlan) with its definition's Bedrock
 * model.
 * <ul>
 *   <li>The model is turned by the munition's yaw, pitch and roll and shifted (0, -0.25, -0.6875) blocks.</li>
 *   <li>Ailerons deflect with the synced controls (22 degrees per unit; the back pair about axes tilted 40 degrees), the
 *       rotor spins, and the engine bone shakes with throttle when the definition's Engine asks for it.</li>
 *   <li>The rotor is solid below 25% throttle, fades out up to 55%, and the rotor_blur disc fades in from 25% and spins
 *       2.5 degrees per tick.</li>
 *   <li>Empty rocket-pod mounts are hidden; loaded rockets slide 24 px into their tubes over 7 ticks after loading.</li>
 *   <li>Seen through its own camera: a fixed camera hides the whole model, a gimbal camera hides only its camera bone and
 *       turns it with the view.</li>
 *   <li>Engine exhaust (once per tick) and rocket launch particles come from the model's bones.</li>
 *   <li>A crashed or damaged munition uses the "_damaged" texture; inside a block the light is probed nearby.</li>
 *   <li>With hitboxes shown, the oriented collision boxes and interact zones are drawn.</li>
 * </ul>
 * 26.3: everything that reads the entity, mutates its per-tick fields or spawns particles happens in
 * {@link #extractRenderState}; {@link #submit} only draws what was captured. 1.1.8's GeckoLib variant of this renderer
 * (Renderer_OAUndsK9b) was never registered, so it is not ported.
 */
public class LoiteringMunitionRenderer extends EntityRenderer<LoiteringMunitionEntity, LoiteringMunitionRenderer.State> {
    private static final float AILERON_DEG = 22.0F;
    private static final float ROTOR_FADE_START = 0.25F;
    private static final float ROTOR_FADE_END = 0.55F;
    private static final float BLUR_SPIN_DEG_PER_TICK = 2.5F;
    private static final double BACK_AILERON_TILT_DEG = 40.0;
    private static final int ROCKET_SLIDE_TICKS = 7;
    private static final float ROCKET_SLIDE_PX = 24.0F;
    private static final Map<Identifier, Identifier> DAMAGED_TEXTURES = new ConcurrentHashMap<>();
    /** 1.1.8 c1jHYPb4: per munition, the last station mask and the tick each station was loaded. */
    private static final Map<Integer, RocketSlide> ROCKET_SLIDES = new ConcurrentHashMap<>();

    public LoiteringMunitionRenderer(Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    /** 1.1.8 set noCulling on the entity, so a munition is never frustum-culled. */
    @Override
    protected boolean affectedByCulling(LoiteringMunitionEntity entity) {
        return false;
    }

    /** 1.1.8 v5suI(LoiteringMunitionEntity): the definition's texture, or its "_damaged" twin once damaged. */
    private static Identifier texture(LoiteringMunitionEntity entity) {
        Identifier base = entity.n9lX8A();
        return entity.vmpmU() ? damaged(base) : base;
    }

    private static Identifier damaged(Identifier texture) {
        return DAMAGED_TEXTURES.computeIfAbsent(texture, t -> {
            String path = t.getPath();
            String damaged = path.endsWith(".png") ? path.substring(0, path.length() - 4) + "_damaged.png" : path + "_damaged";
            return Identifier.fromNamespaceAndPath(t.getNamespace(), damaged);
        });
    }

    @Override
    public void extractRenderState(LoiteringMunitionEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.model = BedrockModels.v5suI(entity.ivDR());
        state.hitboxes = null;
        if (state.model == null) {
            return;
        }

        BakedGeoModel model = state.model;
        DroneDefinition definition = entity.uQdlyiwB();
        Minecraft mc = Minecraft.getInstance();
        boolean ownView = MunitionClientState.iElAbEZe()
            && MunitionClientState.fvsAOg() == entity.getId()
            && mc.options.getCameraType() == CameraType.FIRST_PERSON;
        boolean gimbal = definition.bAcdvq9().gimbal();
        state.hideAll = ownView && !gimbal;
        boolean hideCamera = ownView && gimbal;
        if (!state.hideAll) {
            if (entity.eJDY() || entity.vmpmU()) {
                state.lightCoords = lightAbove(entity, partialTick, state.lightCoords);
            } else if (insideBlock(entity, partialTick)) {
                state.lightCoords = lightBehind(entity, partialTick, state.lightCoords);
            }

            float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            float roll = entity.ibzfxic(partialTick);
            state.orientation = MunitionCollision.v5suI(yaw, pitch, roll);
            state.meshes = BedrockPolyMeshes.get(entity.ivDR());
            state.texture = texture(entity);

            FixedWingModelRenderer.BonePose pose = new FixedWingModelRenderer.BonePose();
            animateBones(model, pose, entity, partialTick);
            state.rotor = model.getBone("rotor").orElse(null);
            state.rotorBlur = model.getBone("rotor_blur").orElse(null);
            if (state.rotor != null) {
                pose.hide(state.rotor);
            }

            if (state.rotorBlur != null) {
                pose.hide(state.rotorBlur);
            }

            rocketMountVisibility(model, pose, entity);
            if (gimbal) {
                turnGimbalCamera(model, pose, entity, yaw);
            }

            model.getBone("camera").ifPresent(b -> {
                if (hideCamera) {
                    pose.hide(b);
                } else {
                    pose.show(b);
                }
            });
            state.pose = pose;
            state.throttle = entity.ifpFOCaAs();
            state.blurSpinDeg = (entity.tickCount + partialTick) * BLUR_SPIN_DEG_PER_TICK % 360.0F;
            state.rockets = rockets(entity, definition, partialTick);
            exhaustParticles(entity, model, pose, partialTick, yaw, pitch, roll);
            rocketFireParticles(entity, model, pose, partialTick, yaw, pitch, roll);
        }

        if (HitboxLines.enabled() && !entity.isInvisible()) {
            state.hitboxes = hitboxes(entity, partialTick);
        }
    }

    /** 1.1.8 v5suI(BedrockModel, LoiteringMunitionEntity, float): ailerons, rotor spin, engine shake. */
    private static void animateBones(BakedGeoModel model, FixedWingModelRenderer.BonePose pose, LoiteringMunitionEntity entity, float partialTick) {
        float rotorAngle = (float)Math.toRadians(entity.wuhB(partialTick));
        for (Map.Entry<String, GeoBone> entry : model.boneLookup().get().entrySet()) {
            String name = entry.getKey().toLowerCase(Locale.ROOT);
            GeoBone bone = entry.getValue();
            if (name.contains("blur")) {
                continue;
            }

            if (name.equals("rotor")) {
                pose.postRotate(bone, new Quaternionf().rotationZ(rotorAngle));
            } else {
                Float deflection = aileronDeflection(entity, name);
                if (deflection != null) {
                    Vector3f axis = aileronAxis(name);
                    pose.preRotate(bone, new Quaternionf().fromAxisAngleRad(axis.x, axis.y, axis.z, (float)Math.toRadians(deflection)));
                }
            }
        }

        DroneDefinition.Engine engine = entity.uQdlyiwB().cXHvq();
        GeoBone engineBone = engine.ibzfxic() ? model.getBone(engine.wuhB()).orElse(null) : null;
        if (engineBone != null && (engine.dOfFwvo() > 0.0F || engine.rsDd50tfV() > 0.0F)) {
            float throttle = entity.ifpFOCaAs();
            if (throttle > 0.02F) {
                double t = System.nanoTime() % 100000000000L * 1.0E-9;
                double freq = engine.uSUb();
                double angle = Math.toRadians(engine.dOfFwvo()) * throttle;
                float shakeX = (float)((Math.sin(t * 6.0 * freq) * 0.6 + Math.sin(t * 9.5 * freq) * 0.4) * angle);
                float shakeZ = (float)((Math.sin(t * 7.5 * freq + 1.1) * 0.6 + Math.sin(t * 11.0 * freq) * 0.4) * angle);
                pose.postRotate(engineBone, new Quaternionf().rotationX(shakeX).rotateZ(shakeZ));
                double amplitude = engine.rsDd50tfV() * throttle;
                float dy = (float)((Math.sin(t * 7.0 * freq) * 0.6 + Math.sin(t * 10.5 * freq) * 0.4) * amplitude);
                float dx = (float)(Math.sin(t * 6.5 * freq + 0.7) * amplitude * 0.6);
                pose.offset(engineBone, dx, dy, 0.0F);
            }
        }
    }

    private static Vector3f aileronAxis(String bone) {
        double tilt = Math.toRadians(BACK_AILERON_TILT_DEG);
        float c = (float)Math.cos(tilt);
        float s = (float)Math.sin(tilt);
        return switch (bone) {
            case "aileron_right_back" -> new Vector3f(c, s, 0.0F).normalize();
            case "aileron_left_back" -> new Vector3f(c, -s, 0.0F).normalize();
            default -> new Vector3f(1.0F, 0.0F, 0.0F);
        };
    }

    private static @Nullable Float aileronDeflection(LoiteringMunitionEntity entity, String bone) {
        float pitch = entity.c1jHYPb4();
        float rollFront = entity.v8ZN();
        float rollBack = entity.zTT3nw6();
        return switch (bone) {
            case "aileron_left_front" -> (pitch + rollFront) * AILERON_DEG;
            case "aileron_right_front" -> (pitch - rollFront) * AILERON_DEG;
            case "aileron_left_back" -> (pitch + rollBack) * AILERON_DEG;
            case "aileron_right_back" -> (pitch - rollBack) * AILERON_DEG;
            default -> null;
        };
    }

    /** 1.1.8 ibzfxic(BedrockModel, LoiteringMunitionEntity, float): the gimbal camera follows the pilot's view yaw. */
    private static void turnGimbalCamera(BakedGeoModel model, FixedWingModelRenderer.BonePose pose, LoiteringMunitionEntity entity, float bodyYaw) {
        if (MunitionClientState.iElAbEZe() && MunitionClientState.fvsAOg() == entity.getId()) {
            model.getBone("camera").ifPresent(b -> {
                float viewYaw = MunitionClientState.r6s7FL3()[0];
                float delta = Mth.wrapDegrees(viewYaw - bodyYaw);
                pose.postRotate(b, new Quaternionf().rotationY((float)Math.toRadians(-delta)));
            });
        }
    }

    /** 1.1.8 v5suI(BedrockModel, LoiteringMunitionEntity): each rocket mount's bones show only while it is loaded. */
    private static void rocketMountVisibility(BakedGeoModel model, FixedWingModelRenderer.BonePose pose, LoiteringMunitionEntity entity) {
        DroneDefinition definition = entity.uQdlyiwB();
        if (!definition.wEJx3Itb()) {
            return;
        }

        List<PayloadStation> stations = definition.jt9oI60ST().rsDd50tfV();
        List<RocketMount> mounts = definition.jt9oI60ST().ibzfxic();
        for (int i = 0; i < mounts.size(); i++) {
            RocketMount mount = mounts.get(i);
            List<String> bones = new ArrayList<>();
            for (PayloadStation station : stations) {
                if (mount.wuhB().contains(station.ibzfxic())) {
                    bones.add(station.wuhB().v5suI());
                }
            }

            Set<GeoBone> resolved = BedrockModelRenderer.resolveBones(model, bones);
            if (entity.v5suI(i)) {
                pose.showAll(resolved);
            } else {
                pose.hideAll(resolved);
            }
        }
    }

    /** 1.1.8 v5suI(LoiteringMunitionEntity, Matrix4f, float, ...): loaded rockets, sliding in after loading. */
    private static List<Rocket> rockets(LoiteringMunitionEntity entity, DroneDefinition definition, float partialTick) {
        if (!definition.wEJx3Itb() || !entity.l2IWOAGO()) {
            return List.of();
        }

        DroneDefinition.RocketPod pod = definition.jt9oI60ST();
        List<PayloadStation> stations = pod.rsDd50tfV();
        byte mask = entity.fvsAOg();
        RocketSlide slide = ROCKET_SLIDES.computeIfAbsent(entity.getId(), id -> new RocketSlide());
        long now = entity.tickCount;
        for (int i = 0; i < stations.size() && i < 8; i++) {
            boolean loaded = (mask & 1 << i) != 0;
            boolean wasLoaded = (slide.mask & 1 << i) != 0;
            if (loaded && !wasLoaded) {
                slide.loadedAt[i] = now;
            }
        }

        slide.mask = mask;
        List<Rocket> out = new ArrayList<>();
        for (int i = 0; i < stations.size(); i++) {
            if ((mask & 1 << i) == 0) {
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

            float progress = i < 8 ? Mth.clamp(((float)(now - slide.loadedAt[i]) + partialTick) / ROCKET_SLIDE_TICKS, 0.0F, 1.0F) : 1.0F;
            float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
            float slidePx = ROCKET_SLIDE_PX * (1.0F - eased);
            AttachTransform attach = station.wuhB();
            AttachTransform drawn = slidePx > 0.0F
                ? new AttachTransform(
                    attach.v5suI(), attach.ibzfxic(), attach.wuhB(), attach.dOfFwvo() + slidePx, attach.rsDd50tfV(), attach.uSUb(), attach.jGhI2(), attach.wEJx3Itb()
                )
                : attach;
            Matrix4f boneMatrix = GeoBoneBoxes.ibzfxic(definition.hOKMYHOaC(), attach.v5suI());
            out.add(new Rocket(model, ProjectileModels.ibzfxic(projectile), drawn, boneMatrix));
        }

        return out;
    }

    /** Model-space origin of the munition model: orientation, then the fixed shift (1.1.8 applied it to every bone lookup). */
    private static Matrix4f modelBase(float yaw, float pitch, float roll) {
        return new Matrix4f().rotate(MunitionCollision.v5suI(yaw, pitch, roll)).translate(0.0F, -0.25F, -0.6875F);
    }

    /** 1.1.8 v5suI(LoiteringMunitionEntity, BedrockModel, ...): engine exhaust particles, once per tick, from the exhaust bone. */
    private static void exhaustParticles(
        LoiteringMunitionEntity entity, BakedGeoModel model, FixedWingModelRenderer.BonePose pose, float partialTick, float yaw, float pitch, float roll
    ) {
        if (entity.tpzIHm() || entity.vmpmU() || entity.ifpFOCaAs() < 0.15F) {
            return;
        }

        DroneDefinition.Engine engine = entity.uQdlyiwB().cXHvq();
        if (!engine.ibzfxic() || engine.wEJx3Itb().isEmpty()) {
            return;
        }

        GeoBone bone = model.getBone(engine.jGhI2()).orElse(null);
        if (bone == null || entity.ibzfxic == entity.tickCount) {
            return;
        }

        entity.ibzfxic = entity.tickCount;
        Vector3f local = modelBase(yaw, pitch, roll).mul(FixedWingModelRenderer.pivotMatrix(bone, pose)).transformPosition(new Vector3f());
        Vec3 pos = entity.getPosition(partialTick).add(local.x, local.y, local.z);
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(-pitch);
        double cos = Math.cos(pitchRad);
        Vec3 backwards = new Vec3(Math.sin(yawRad) * cos, -Math.sin(pitchRad), -Math.cos(yawRad) * cos);
        FpvParticles.spawn(engine.wEJx3Itb(), pos, entity.getDeltaMovement(), backwards);
    }

    /** 1.1.8 ibzfxic(LoiteringMunitionEntity, BedrockModel, ...): launch particles at the firing tube's bone. */
    private static void rocketFireParticles(
        LoiteringMunitionEntity entity, BakedGeoModel model, FixedWingModelRenderer.BonePose pose, float partialTick, float yaw, float pitch, float roll
    ) {
        String fireBone = entity.wuhB();
        if (fireBone == null) {
            return;
        }

        DroneDefinition.RocketPod pod = entity.uQdlyiwB().jt9oI60ST();
        if (pod == null || pod.jGhI2().isEmpty()) {
            return;
        }

        GeoBone bone = model.getBone(fireBone).orElse(null);
        if (bone == null) {
            return;
        }

        Vector3f local = modelBase(yaw, pitch, roll).mul(FixedWingModelRenderer.pivotMatrix(bone, pose)).transformPosition(new Vector3f());
        Vec3 pos = entity.getPosition(partialTick).add(local.x, local.y, local.z);
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(-pitch);
        double cos = Math.cos(pitchRad);
        Vec3 forwards = new Vec3(-Math.sin(yawRad) * cos, Math.sin(pitchRad), Math.cos(yawRad) * cos);
        FpvParticles.spawn(pod.jGhI2(), pos, entity.getDeltaMovement(), forwards);
    }

    /** 1.1.8 v5suI(LoiteringMunitionEntity, float, PoseStack, MultiBufferSource): collision boxes and interact zones. */
    private static @Nullable Hitboxes hitboxes(LoiteringMunitionEntity entity, float partialTick) {
        List<OrientedBox> boxes = entity.v5suI(partialTick);
        if (boxes.isEmpty()) {
            return null;
        }

        Vec3 origin = entity.getPosition(partialTick);
        Set<String> interactNames = interactNames(entity.uQdlyiwB());
        List<String> boxNames = interactNames.isEmpty() ? List.of() : entity.mM7DsB9AR();
        boolean[] interact = new boolean[boxes.size()];
        for (int i = 0; i < boxes.size(); i++) {
            interact[i] = i < boxNames.size() && interactNames.contains(boxNames.get(i));
        }

        float[][] zones = new float[0][];
        List<BoneBox> zoneBoxes = GeoBoneBoxes.dOfFwvo(entity.uQdlyiwB().hOKMYHOaC());
        if (!zoneBoxes.isEmpty()) {
            List<OrientedBox> placed = MunitionCollision.v5suI(zoneBoxes, entity, partialTick);
            zones = HitboxLines.relativeCorners(placed.subList(0, Math.min(placed.size(), zoneBoxes.size())), origin);
        }

        return new Hitboxes(HitboxLines.relativeCorners(boxes, origin), interact, zones);
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

    private static boolean insideBlock(LoiteringMunitionEntity entity, float partialTick) {
        BlockPos pos = BlockPos.containing(entity.getPosition(partialTick));
        return entity.level().getBlockState(pos).isSolidRender();
    }

    /** 1.1.8: inside a block, light from the first open block behind the munition (against its motion). */
    private static int lightBehind(LoiteringMunitionEntity entity, float partialTick, int fallback) {
        Vec3 pos = entity.getPosition(partialTick);
        Vec3 motion = entity.getDeltaMovement();
        Vec3 dir = motion.lengthSqr() > 1.0E-6 ? motion.normalize() : new Vec3(0.0, 1.0, 0.0);
        for (double d = 0.5; d <= 4.0; d += 0.5) {
            BlockPos probe = BlockPos.containing(pos.subtract(dir.scale(d)));
            if (!entity.level().getBlockState(probe).isSolidRender()) {
                return LightCoordsUtil.getLightCoords(entity.level(), probe);
            }
        }

        return fallback;
    }

    /** 1.1.8: a crashed or damaged munition takes the light of the first open block above it. */
    private static int lightAbove(LoiteringMunitionEntity entity, float partialTick, int fallback) {
        Vec3 pos = entity.getPosition(partialTick);
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        int x = Mth.floor(pos.x);
        int y = Mth.floor(pos.y);
        int z = Mth.floor(pos.z);
        for (int i = 0; i <= 4; i++) {
            probe.set(x, y + i, z);
            if (!entity.level().getBlockState(probe).isSolidRender()) {
                return LightCoordsUtil.getLightCoords(entity.level(), probe);
            }
        }

        return fallback;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.model != null && !state.hideAll && state.texture != null && state.pose != null) {
            poseStack.pushPose();
            poseStack.rotate(state.orientation);
            poseStack.translate(0.0F, -0.25F, -0.6875F);
            Identifier texture = state.texture;
            int light = state.lightCoords;
            int overlay = OverlayTexture.NO_OVERLAY;
            // 1.20.1 entityCutout culled back faces (26.3 entityCutoutCull); the library drew poly meshes two-sided.
            FixedWingModelRenderer.submit(
                state.model, state.meshes, state.pose, poseStack, collector, RenderTypes.entityCutoutCull(texture), RenderTypes.entityCutout(texture), light, overlay, -1
            );
            this.submitRotor(state, poseStack, collector, texture, light, overlay);
            Matrix4f base = new Matrix4f(poseStack.last().pose());
            for (Rocket rocket : state.rockets) {
                BedrockModelRenderer.v5suI(new Matrix4f(base).mul(rocket.boneMatrix()), rocket.model(), rocket.texture(), rocket.attach(), poseStack, collector, light);
            }

            poseStack.popPose();
        }

        Hitboxes hitboxes = state.hitboxes;
        if (hitboxes != null) {
            float width = HitboxLines.width();
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                for (int i = 0; i < hitboxes.boxes().length; i++) {
                    HitboxLines.drawCorners(pose, buffer, hitboxes.boxes()[i], hitboxes.interact()[i], width);
                }

                for (float[] zone : hitboxes.zones()) {
                    HitboxLines.drawCorners(pose, buffer, zone, true, width);
                }
            });
        }

        super.submit(state, poseStack, collector, camera);
    }

    /**
     * 1.1.8 v5suI(BedrockModel, LoiteringMunitionEntity, Matrix4f, ...): the rotor and its blur disc, drawn after the
     * model. 1.1.8 flushed the translucent buffer right after each; 26.3 sorts translucent geometry itself.
     */
    private void submitRotor(State state, PoseStack poseStack, SubmitNodeCollector collector, Identifier texture, int light, int overlay) {
        GeoBone rotor = state.rotor;
        if (rotor == null) {
            return;
        }

        GeoBone blur = state.rotorBlur;
        boolean hasBlur = blur != null;
        float throttle = state.throttle;
        if (!hasBlur || throttle <= ROTOR_FADE_END) {
            boolean fading = hasBlur && throttle >= ROTOR_FADE_START;
            float alpha = fading ? 1.0F - (throttle - ROTOR_FADE_START) / (ROTOR_FADE_END - ROTOR_FADE_START) : 1.0F;
            FixedWingModelRenderer.BonePose rotorPose = state.pose.copy();
            rotorPose.show(rotor);
            if (blur != null) {
                rotorPose.hide(blur);
            }

            // 1.1.8 drew the rotor with renderCubes, i.e. without poly meshes.
            FixedWingModelRenderer.submitBones(
                List.of(rotor),
                rotor.parent(),
                Map.of(),
                rotorPose,
                poseStack,
                collector,
                fading ? BlurDiscRenderer.blurType(texture) : RenderTypes.entityCutoutCull(texture),
                null,
                light,
                overlay,
                ARGB.color(alpha, 0xFFFFFF)
            );
        }

        if (hasBlur && throttle > ROTOR_FADE_START) {
            float alpha = Mth.clamp((throttle - ROTOR_FADE_START) / (ROTOR_FADE_END - ROTOR_FADE_START), 0.0F, 1.0F);
            Matrix4f matrix = FixedWingModelRenderer.boneMatrix(rotor.parent(), state.pose)
                .translate(blur.pivotX() / 16.0F, blur.pivotY() / 16.0F, blur.pivotZ() / 16.0F)
                .rotateZ((float)Math.toRadians(state.blurSpinDeg))
                .translate(-blur.pivotX() / 16.0F, -blur.pivotY() / 16.0F, -blur.pivotZ() / 16.0F);
            int color = ARGB.color(alpha, 0xFFFFFF);
            collector.submitCustomGeometry(
                poseStack,
                BlurDiscRenderer.blurType(texture),
                (pose, buffer) -> FixedWingModelRenderer.emitPlainCubes(blur, new Matrix4f(pose.pose()).mul(matrix), buffer, light, overlay, color)
            );
        }
    }

    private static final class RocketSlide {
        byte mask = 0;
        final long[] loadedAt = new long[8];
    }

    private record Rocket(BakedGeoModel model, Identifier texture, AttachTransform attach, Matrix4f boneMatrix) {
    }

    private record Hitboxes(float[][] boxes, boolean[] interact, float[][] zones) {
    }

    public static class State extends EntityRenderState {
        @Nullable BakedGeoModel model;
        Map<String, float[]> meshes = Map.of();
        @Nullable Identifier texture;
        boolean hideAll;
        Quaternionf orientation = new Quaternionf();
        FixedWingModelRenderer.@Nullable BonePose pose;
        @Nullable GeoBone rotor;
        @Nullable GeoBone rotorBlur;
        float throttle;
        float blurSpinDeg;
        List<Rocket> rockets = List.of();
        @Nullable Hitboxes hitboxes;
    }
}
