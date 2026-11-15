package com.fpvdrone.client.renderer;

import com.fpvdrone.client.model.BedrockPolyMeshes;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

/**
 * Draws a fixed-wing Bedrock model (the bedrock_geo airframes) with per-frame bone changes: cubes and poly meshes.
 * <p>
 * 1.1.8 drew these models with the simplebedrockmodel library, whose bones were mutable: each frame it reset the bind
 * pose, then turned ailerons, spun the rotor, shook the engine and hid bones by setting fields on the shared model. The
 * baked GeckoLib model used here is immutable and drawn later (26.3 submits geometry and writes it after the frame is
 * collected), so the changes are kept in a {@link BonePose} made per frame and applied while walking the bones.
 * <p>
 * The library's bone frame (pivot relative to the parent, cubes relative to the pivot) and GeckoLib's (absolute pivots
 * and cubes) give the same transforms; the library's {@code getGlobalTransform()} of a bone is
 * {@code boneMatrix(bone) * T(pivot)} here. Rotations: the library's {@code rotation.set(q).mul(base)} is
 * {@link BonePose#preRotate}, {@code rotation.rotateX(..)} is {@link BonePose#postRotate}, {@code x/y/z +=} is
 * {@link BonePose#offset} (pixels, same mirrored X as GeckoLib).
 */
public final class FixedWingModelRenderer {
    private FixedWingModelRenderer() {
    }

    /** Per-frame bone changes of one model (1.1.8 wrote these straight into the library's bones). */
    public static final class BonePose {
        private final Map<GeoBone, Quaternionf> pre = new IdentityHashMap<>();
        private final Map<GeoBone, Quaternionf> post = new IdentityHashMap<>();
        private final Map<GeoBone, Vector3f> offset = new IdentityHashMap<>();
        private final Set<GeoBone> hidden = Collections.newSetFromMap(new IdentityHashMap<>());

        /** Rotation applied before the bone's own (in its parent's frame). */
        public void preRotate(GeoBone bone, Quaternionf q) {
            Quaternionf old = this.pre.get(bone);
            this.pre.put(bone, old == null ? new Quaternionf(q) : new Quaternionf(q).mul(old));
        }

        /** Rotation applied after the bone's own (about the bone's own axes). */
        public void postRotate(GeoBone bone, Quaternionf q) {
            Quaternionf old = this.post.get(bone);
            this.post.put(bone, old == null ? new Quaternionf(q) : old.mul(q, new Quaternionf()));
        }

        /** Moves the bone's pivot by the given pixels. */
        public void offset(GeoBone bone, float x, float y, float z) {
            this.offset.computeIfAbsent(bone, b -> new Vector3f()).add(x, y, z);
        }

        public void hide(GeoBone bone) {
            this.hidden.add(bone);
        }

        public void show(GeoBone bone) {
            this.hidden.remove(bone);
        }

        public void hideAll(Collection<GeoBone> bones) {
            this.hidden.addAll(bones);
        }

        public void showAll(Collection<GeoBone> bones) {
            this.hidden.removeAll(bones);
        }

        public boolean isHidden(GeoBone bone) {
            return this.hidden.contains(bone);
        }

        /** Copy with the same rotations and offsets and its own hidden set. */
        public BonePose copy() {
            BonePose copy = new BonePose();
            copy.pre.putAll(this.pre);
            copy.post.putAll(this.post);
            copy.offset.putAll(this.offset);
            copy.hidden.addAll(this.hidden);
            return copy;
        }
    }

    /** The bone's own transform, applied to a pose stack (pivot, rotations, back off the pivot). */
    public static void applyBone(PoseStack poseStack, GeoBone bone, BonePose pose) {
        Vector3f off = pose.offset.get(bone);
        float ox = off == null ? 0.0F : off.x;
        float oy = off == null ? 0.0F : off.y;
        float oz = off == null ? 0.0F : off.z;
        poseStack.translate((bone.pivotX() + ox) / 16.0F, (bone.pivotY() + oy) / 16.0F, (bone.pivotZ() + oz) / 16.0F);
        Quaternionf pre = pose.pre.get(bone);
        if (pre != null) {
            poseStack.rotate(pre);
        }

        if (bone.baseRotZ() != 0.0F) {
            poseStack.rotate(Axis.ZP.rotation(bone.baseRotZ()));
        }

        if (bone.baseRotY() != 0.0F) {
            poseStack.rotate(Axis.YP.rotation(bone.baseRotY()));
        }

        if (bone.baseRotX() != 0.0F) {
            poseStack.rotate(Axis.XP.rotation(bone.baseRotX()));
        }

        Quaternionf post = pose.post.get(bone);
        if (post != null) {
            poseStack.rotate(post);
        }

        poseStack.translate(-bone.pivotX() / 16.0F, -bone.pivotY() / 16.0F, -bone.pivotZ() / 16.0F);
    }

    /** Model-space matrix of a bone, parents included (the matrix its cubes are drawn with). */
    public static Matrix4f boneMatrix(@Nullable GeoBone bone, BonePose pose) {
        if (bone == null) {
            return new Matrix4f();
        }

        PoseStack stack = new PoseStack();
        applyChain(stack, bone, pose);
        return new Matrix4f(stack.last().pose());
    }

    private static void applyChain(PoseStack stack, GeoBone bone, BonePose pose) {
        if (bone.parent() != null) {
            applyChain(stack, bone.parent(), pose);
        }

        applyBone(stack, bone, pose);
    }

    /**
     * 1.1.8 {@code parent.getGlobalTransform().translate(bone.x / 16, ...)}: the bone's (offset) pivot in model space,
     * with its parents' transforms but not its own rotation.
     */
    public static Matrix4f pivotMatrix(GeoBone bone, BonePose pose) {
        Vector3f off = pose.offset.get(bone);
        Matrix4f m = boneMatrix(bone.parent(), pose);
        return m.translate(
            (bone.pivotX() + (off == null ? 0.0F : off.x)) / 16.0F,
            (bone.pivotY() + (off == null ? 0.0F : off.y)) / 16.0F,
            (bone.pivotZ() + (off == null ? 0.0F : off.z)) / 16.0F
        );
    }

    /** Draws the whole model: cubes with {@code cubeType}, poly meshes with {@code meshType}. */
    public static void submit(
        BakedGeoModel model,
        Map<String, float[]> meshes,
        BonePose pose,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        RenderType cubeType,
        RenderType meshType,
        int light,
        int overlay,
        int color
    ) {
        submitBones(List.of(model.topLevelBones()), null, meshes, pose, poseStack, collector, cubeType, meshType, light, overlay, color);
    }

    /**
     * Draws the given bones (and their children) at their place in the model; {@code parent} is the bone whose
     * transform they hang from (null for top-level bones).
     */
    public static void submitBones(
        List<GeoBone> roots,
        @Nullable GeoBone parent,
        Map<String, float[]> meshes,
        BonePose pose,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        RenderType cubeType,
        @Nullable RenderType meshType,
        int light,
        int overlay,
        int color
    ) {
        Matrix4f start = boneMatrix(parent, pose);
        collector.submitCustomGeometry(poseStack, cubeType, (p, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.last().set(p);
            stack.mulPose(start);
            for (GeoBone bone : roots) {
                walk(stack, bone, pose, buffer, light, overlay, color, null);
            }
        });
        if (meshType != null && !meshes.isEmpty()) {
            collector.submitCustomGeometry(poseStack, meshType, (p, buffer) -> {
                PoseStack stack = new PoseStack();
                stack.last().set(p);
                stack.mulPose(start);
                for (GeoBone bone : roots) {
                    walk(stack, bone, pose, buffer, light, overlay, color, meshes);
                }
            });
        }
    }

    /** Cubes when {@code meshes} is null, otherwise only the poly meshes. */
    private static void walk(
        PoseStack stack, GeoBone bone, BonePose pose, VertexConsumer buffer, int light, int overlay, int color, @Nullable Map<String, float[]> meshes
    ) {
        if (pose.isHidden(bone)) {
            return;
        }

        stack.pushPose();
        applyBone(stack, bone, pose);
        if (meshes == null) {
            if (bone instanceof CuboidGeoBone cuboid) {
                for (GeoCube cube : cuboid.cubes) {
                    stack.pushPose();
                    cube.render(stack, buffer, light, overlay, color);
                    stack.popPose();
                }
            }
        } else {
            float[] triangles = meshes.get(bone.name());
            if (triangles != null) {
                emitTriangles(stack.last(), triangles, buffer, light, overlay, color);
            }
        }

        for (GeoBone child : bone.children()) {
            walk(stack, child, pose, buffer, light, overlay, color, meshes);
        }

        stack.popPose();
    }

    /** Each triangle as a quad with its last vertex doubled, so it goes into the normal quad entity buffers. */
    private static void emitTriangles(PoseStack.Pose pose, float[] t, VertexConsumer buffer, int light, int overlay, int color) {
        Matrix4f m = pose.pose();
        Matrix3f n = pose.normal();
        Vector3f p = new Vector3f();
        Vector3f nv = new Vector3f();
        int stride = BedrockPolyMeshes.STRIDE;
        for (int tri = 0; tri + 3 * stride <= t.length; tri += 3 * stride) {
            for (int k = 0; k < 4; k++) {
                int i = tri + Math.min(k, 2) * stride;
                m.transformPosition(t[i], t[i + 1], t[i + 2], p);
                n.transform(t[i + 5], t[i + 6], t[i + 7], nv);
                if (nv.lengthSquared() > 1.0E-12F) {
                    nv.normalize();
                }

                buffer.addVertex(p.x, p.y, p.z, color, t[i + 3], t[i + 4], overlay, light, nv.x, nv.y, nv.z);
            }
        }
    }

    /**
     * 1.1.8 munition rotor blur: the blur bone's cubes as plain boxes (no cube or bone rotation), with one fixed normal,
     * at {@code matrix}.
     */
    public static void emitPlainCubes(GeoBone bone, Matrix4f matrix, VertexConsumer buffer, int light, int overlay, int color) {
        if (!(bone instanceof CuboidGeoBone cuboid)) {
            return;
        }

        Vector4f p = new Vector4f();
        for (GeoCube cube : cuboid.cubes) {
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) {
                    continue;
                }

                for (GeoVertex v : quad.vertices()) {
                    matrix.transform(p.set(v.posX(), v.posY(), v.posZ(), 1.0F));
                    // 1.1.8 wrote (0, 1, 0) as the normal of every blur vertex (flat, view-independent lighting).
                    buffer.addVertex(p.x, p.y, p.z, color, v.texU(), v.texV(), overlay, light, 0.0F, 1.0F, 0.0F);
                }
            }
        }
    }
}
