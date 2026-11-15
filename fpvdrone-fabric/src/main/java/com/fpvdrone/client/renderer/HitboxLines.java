package com.fpvdrone.client.renderer;

import com.fpvdrone.data.BoneBox;
import com.fpvdrone.data.OrientedBox;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Wireframe boxes the 1.1.8 ramp and munition renderers drew with entity hitboxes on (F3+B): the entity's oriented
 * collision boxes, and the drone's bone boxes in model space. Interact zones are orange, everything else green.
 * 1.1.8 drew into RenderType.lines() during the entity render; 26.3 lines carry a width, taken from the window as the
 * vanilla line renderers do.
 */
public final class HitboxLines {
    /** 1.1.8: the 12 edges of an OrientedBox's 8 corners. */
    private static final int[][] OBB_EDGES = {{0, 4}, {1, 5}, {2, 6}, {3, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 1}, {2, 3}, {4, 5}, {6, 7}};

    private HitboxLines() {
    }

    /** 1.1.8: {@code entityRenderDispatcher.shouldRenderHitBoxes() && !Minecraft.showOnlyReducedInfo()}. */
    public static boolean enabled() {
        Minecraft mc = Minecraft.getInstance();
        return mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES) && !mc.showOnlyReducedInfo();
    }

    public static float width() {
        return Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
    }

    /** Corners of world-space boxes, made relative to {@code origin} (the entity's render position). */
    public static float[][] relativeCorners(List<OrientedBox> boxes, Vec3 origin) {
        float[][] out = new float[boxes.size()][];
        for (int i = 0; i < boxes.size(); i++) {
            Vec3[] c = boxes.get(i).rsDd50tfV();
            float[] r = new float[24];
            for (int k = 0; k < 8; k++) {
                r[k * 3] = (float)(c[k].x - origin.x);
                r[k * 3 + 1] = (float)(c[k].y - origin.y);
                r[k * 3 + 2] = (float)(c[k].z - origin.z);
            }

            out[i] = r;
        }

        return out;
    }

    /** Draws boxes whose corners came from {@link #relativeCorners}. */
    public static void drawCorners(PoseStack.Pose pose, VertexConsumer buffer, float[] corners, boolean interact, float width) {
        for (int[] e : OBB_EDGES) {
            line(
                pose,
                buffer,
                new Vector3f(corners[e[0] * 3], corners[e[0] * 3 + 1], corners[e[0] * 3 + 2]),
                new Vector3f(corners[e[1] * 3], corners[e[1] * 3 + 1], corners[e[1] * 3 + 2]),
                interact,
                width
            );
        }
    }

    /** 1.1.8 ramp: a drone's bone boxes in model space (corner bit 0 = x, 1 = y, 2 = z). */
    public static void drawBoneBoxes(PoseStack.Pose pose, VertexConsumer buffer, List<BoneBox> boxes, Set<String> interactNames, float width) {
        for (BoneBox box : boxes) {
            boolean interact = interactNames.contains(box.rsDd50tfV());
            Vector3f center = box.v5suI();
            Vector3f half = box.ibzfxic();
            Quaternionf rotation = box.wuhB();
            Vector3f[] c = new Vector3f[8];
            for (int i = 0; i < 8; i++) {
                Vector3f v = new Vector3f((i & 1) == 0 ? -half.x() : half.x(), (i & 2) == 0 ? -half.y() : half.y(), (i & 4) == 0 ? -half.z() : half.z());
                rotation.transform(v);
                v.add(center);
                c[i] = v;
            }

            for (int i = 0; i < 8; i++) {
                if ((i & 1) == 0) {
                    line(pose, buffer, c[i], c[i | 1], interact, width);
                }

                if ((i & 2) == 0) {
                    line(pose, buffer, c[i], c[i | 2], interact, width);
                }

                if ((i & 4) == 0) {
                    line(pose, buffer, c[i], c[i | 4], interact, width);
                }
            }
        }
    }

    private static void line(PoseStack.Pose pose, VertexConsumer buffer, Vector3f a, Vector3f b, boolean interact, float width) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float dz = b.z - a.z;
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-6F) {
            return;
        }

        dx /= length;
        dy /= length;
        dz /= length;
        float r = interact ? 1.0F : 0.2F;
        float g = interact ? 0.65F : 1.0F;
        float bl = interact ? 0.0F : 0.3F;
        buffer.addVertex(pose, a.x, a.y, a.z).setColor(r, g, bl, 1.0F).setNormal(pose, dx, dy, dz).setLineWidth(width);
        buffer.addVertex(pose, b.x, b.y, b.z).setColor(r, g, bl, 1.0F).setNormal(pose, dx, dy, dz).setLineWidth(width);
    }
}
