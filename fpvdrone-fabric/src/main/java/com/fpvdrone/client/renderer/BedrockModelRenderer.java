package com.fpvdrone.client.renderer;

import com.fpvdrone.data.AttachTransform;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 1.1.8 A6oLVWDeS: draws a Bedrock model loaded by {@link com.fpvdrone.client.model.BedrockModels} (dropped bombs, payloads
 * hung on a mount bone, the vest's TM-62, fixed-wing airframes).
 * <p>
 * 1.1.8 called the simplebedrockmodel library's renderToBuffer; in 26.3 geometry is submitted to the frame's
 * {@link SubmitNodeCollector} and written later, so the bone walk runs inside a custom-geometry callback. Bones are
 * posed exactly as GeckoLib poses an unanimated model (pivot, Z-Y-X base rotation, back off the pivot).
 * <p>
 * The library kept a "visible" flag on each bone; GeckoLib's baked bones have none, so {@link #v5suI(BakedGeoModel,
 * Collection, boolean)} keeps the hidden bones per model here. A hidden bone hides its children too, as before.
 */
public final class BedrockModelRenderer {
   private static final Map<BakedGeoModel, Set<GeoBone>> HIDDEN = Collections.synchronizedMap(new IdentityHashMap<>());

   private BedrockModelRenderer() {
   }

   /** Default draw: cutout, no overlay. */
   public static void v5suI(BakedGeoModel var0, Identifier var1, PoseStack var2, SubmitNodeCollector var3, int var4, int var5) {
      v5suI(var0, RenderTypes.entityCutout(var1), var2, var3, var4, var5);
   }

   /** Draws the whole model with the given render type at the current pose. */
   public static void v5suI(BakedGeoModel var0, RenderType var1, PoseStack var2, SubmitNodeCollector var3, int var4, int var5) {
      v5suI(var0, var1, var2, var3, var4, var5, -1);
   }

   /** As above with a tint (ARGB). */
   public static void v5suI(BakedGeoModel var0, RenderType var1, PoseStack var2, SubmitNodeCollector var3, int var4, int var5, int var6) {
      Set<GeoBone> var7 = hiddenSnapshot(var0);
      GeoBone[] var8 = var0.topLevelBones();
      var3.submitCustomGeometry(var2, var1, (pose, buffer) -> {
         PoseStack var9 = new PoseStack();
         var9.last().set(pose);

         for (GeoBone var10 : var8) {
            renderBone(var9, var10, buffer, var4, var5, var6, var7);
         }
      });
   }

   /**
    * Draws a model hung on a mount: {@code var0} is the mount bone's pose (as captured while drawing the carrier), then
    * the attach transform's offset (pixels, X mirrored), roll, yaw, pitch and scale are applied, as 1.1.8 did.
    */
   public static void v5suI(Matrix4f var0, BakedGeoModel var1, Identifier var2, AttachTransform var3, PoseStack var4, SubmitNodeCollector var5, int var6) {
      var4.pushPose();
      var4.last().pose().set(var0);
      var4.last().normal().set(new Matrix3f(var0));
      ibzfxic(var3, var4);
      v5suI(var1, RenderTypes.entityTranslucentCull(var2), var4, var5, var6, OverlayTexture.NO_OVERLAY);
      var4.popPose();
   }

   /** The attach transform alone (1.1.8 inlined this in every mount renderer). */
   public static void ibzfxic(AttachTransform var0, PoseStack var1) {
      var1.translate(-var0.ibzfxic() / 16.0F, var0.wuhB() / 16.0F, var0.dOfFwvo() / 16.0F);
      if (var0.jGhI2() != 0.0F) {
         var1.rotate(Axis.ZP.rotationDegrees(var0.jGhI2()));
      }

      if (var0.uSUb() != 0.0F) {
         var1.rotate(Axis.YP.rotationDegrees(-var0.uSUb()));
      }

      if (var0.rsDd50tfV() != 0.0F) {
         var1.rotate(Axis.XP.rotationDegrees(-var0.rsDd50tfV()));
      }

      if (var0.wEJx3Itb() != 1.0F) {
         var1.scale(var0.wEJx3Itb(), var0.wEJx3Itb(), var0.wEJx3Itb());
      }
   }

   /**
    * Shows or hides the named bones. A parent whose children are all hidden is hidden as well (1.1.8 behaviour), so an
    * empty group bone does not keep its own cubes on screen.
    */
   public static void v5suI(BakedGeoModel var0, Collection<String> var1, boolean var2) {
      Map<GeoBone, List<GeoBone>> var3 = new IdentityHashMap<>();

      for (GeoBone var5 : var0.boneLookup().get().values()) {
         if (var5.parent() != null) {
            var3.computeIfAbsent(var5.parent(), var0x -> new ArrayList<>()).add(var5);
         }
      }

      Set<GeoBone> var10 = Collections.newSetFromMap(new IdentityHashMap<>());

      for (String var6 : var1) {
         GeoBone var7 = var0.getBone(var6).orElse(null);
         if (var7 != null) {
            var10.add(var7);

            for (GeoBone var8 = var7.parent(); var8 != null; var8 = var8.parent()) {
               List<GeoBone> var9 = var3.get(var8);
               if (var9 == null || !var10.containsAll(var9)) {
                  break;
               }

               var10.add(var8);
            }
         }
      }

      synchronized (HIDDEN) {
         Set<GeoBone> var11 = HIDDEN.computeIfAbsent(var0, var0x -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
         if (var2) {
            var11.removeAll(var10);
         } else {
            var11.addAll(var10);
         }
      }
   }

   private static Set<GeoBone> hiddenSnapshot(BakedGeoModel model) {
      synchronized (HIDDEN) {
         Set<GeoBone> hidden = HIDDEN.get(model);
         if (hidden == null || hidden.isEmpty()) {
            return Set.of();
         }

         Set<GeoBone> copy = Collections.newSetFromMap(new IdentityHashMap<>());
         copy.addAll(hidden);
         return copy;
      }
   }

   private static void renderBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int light, int overlay, int color, Set<GeoBone> hidden) {
      if (hidden.contains(bone)) {
         return;
      }

      poseStack.pushPose();
      RenderUtil.prepMatrixForBone(poseStack, bone);
      if (bone instanceof CuboidGeoBone cuboid) {
         for (GeoCube cube : cuboid.cubes) {
            poseStack.pushPose();
            cube.render(poseStack, buffer, light, overlay, color);
            poseStack.popPose();
         }
      }

      for (GeoBone child : bone.children()) {
         renderBone(poseStack, child, buffer, light, overlay, color, hidden);
      }

      poseStack.popPose();
   }
}
