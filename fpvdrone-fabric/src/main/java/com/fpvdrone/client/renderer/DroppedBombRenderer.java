package com.fpvdrone.client.renderer;

import com.fpvdrone.client.PayloadAnchors;
import com.fpvdrone.client.model.BedrockModels;
import com.fpvdrone.client.model.ProjectileModels;
import com.fpvdrone.client.particle.FpvParticles;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroppedBombEntity;
import com.geckolib.cache.model.BakedGeoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 Renderer_K00KdzwQg: draws a {@link DroppedBombEntity} with its projectile's Bedrock model.
 * <ul>
 *   <li>Grenades are drawn 0.07 blocks up (the model's origin is its base) and turned by their tumbling quaternion.</li>
 *   <li>Other bombs point their nose along the simulated nose vector, with a slow seeded wobble (0.07 rad at 0.21 and 0.29
 *       rad/tick) and spin (0.18 rad/tick) so a falling bomblet does not look rigid, then the definition's ModelYawDeg
 *       authoring correction.</li>
 *   <li>Another player's bomb starts where its payload was last drawn on the carrying drone ({@link PayloadAnchors}) and
 *       blends into its true position over 6 ticks (a grenade also blends its hang orientation), so a remote drop does
 *       not pop. The dropper's own bomb needs none: its predicted stand-in already started there.</li>
 *   <li>A bomb with a "Motor" emits its particle list for the motor's burn time (once per tick).</li>
 *   <li>A server bomb that has adopted a predicted stand-in is hidden while the stand-in is alive (the stand-in is drawn).</li>
 * </ul>
 * 26.3: the entity-dependent work (the blend start and the particles) happens in {@link #extractRenderState}; the submit
 * only applies the captured transform.
 */
public class DroppedBombRenderer extends EntityRenderer<DroppedBombEntity, DroppedBombRenderer.State> {
   private static final float GRENADE_LIFT = 0.07F;
   private static final float SPIN_RATE = 0.18F;
   private static final float WOBBLE_RATE_A = 0.21F;
   private static final float WOBBLE_RATE_B = 0.29F;
   public static final float WOBBLE = 0.07F;

   public DroppedBombRenderer(Context var1) {
      super(var1);
      this.shadowRadius = 0.2F;
   }

   @Override
   public State createRenderState() {
      return new State();
   }

   @Override
   public boolean shouldRender(DroppedBombEntity var1, Frustum var2, double var3, double var5, double var7, float partialTicks) {
      return var1.wuhB() != null ? false : super.shouldRender(var1, var2, var3, var5, var7, partialTicks);
   }

   @Override
   public void extractRenderState(DroppedBombEntity var1, State state, float var3) {
      super.extractRenderState(var1, state, var3);
      ProjectileDefinition def = var1.rsDd50tfV();
      state.model = BedrockModels.v5suI(ProjectileModels.v5suI(def));
      state.texture = ProjectileModels.ibzfxic(def);
      if (state.model == null) {
         return;
      }

      this.ibzfxic(var1);
      if (var1.bAcdvq9()) {
         float var8 = var1.wuhB(var3);
         state.blendOffset = new Vec3(var1.b69s() * var8, var1.cXHvq() * var8, var1.rxYnaa() * var8);
      } else {
         state.blendOffset = Vec3.ZERO;
      }

      state.grenade = def.isGrenade();
      if (state.grenade) {
         Quaternionf var4 = var1.ibzfxic(var3);
         if (var1.c1jHYPb4()) {
            float var5 = var1.wuhB(var3);
            var4 = new Quaternionf(var4).slerp(var1.r6s7FL3(), var5);
         }

         state.orientation = var4;
         state.modelYaw = 0.0F;
      } else {
         state.orientation = this.noseOrientation(var1, var3);
         state.modelYaw = def.dropModelYaw();
      }

      this.ibzfxic(var1, var3);
   }

   @Override
   public void submit(State state, PoseStack var4, SubmitNodeCollector collector, CameraRenderState camera) {
      if (state.model != null && state.texture != null) {
         var4.pushPose();
         var4.translate(state.blendOffset.x, state.blendOffset.y, state.blendOffset.z);
         if (state.grenade) {
            var4.translate(0.0F, GRENADE_LIFT, 0.0F);
            var4.rotate(state.orientation);
         } else {
            var4.rotate(state.orientation);
            var4.rotate(Axis.YP.rotationDegrees(state.modelYaw));
         }

         BedrockModelRenderer.v5suI(state.model, RenderTypes.entityTranslucentCull(state.texture), var4, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
         var4.popPose();
         super.submit(state, var4, collector, camera);
      }
   }

   /** Motor trail: the projectile's particle list, once per tick while the motor burns. */
   private void ibzfxic(DroppedBombEntity var1, float var2) {
      ProjectileDefinition var3 = var1.rsDd50tfV();
      if (var3.hasMotorParticles() && var1.tickCount < var3.motor().v5suI()) {
         if (var1.ibzfxic != var1.tickCount) {
            var1.ibzfxic = var1.tickCount;
            Vec3 var4 = var1.v5suI(var2);
            Vec3 var5 = var4.lengthSqr() > 1.0E-6 ? var4.scale(-1.0) : new Vec3(0.0, 1.0, 0.0);
            Vec3 var6 = var1.getPosition(var2);
            FpvParticles.spawn(var3.motorParticles(), var6, var1.getDeltaMovement(), var5);
         }
      }
   }

   /**
    * First frame only: where should this bomb appear to start? The dropper sees no blend. For anyone else, the offset
    * from the bomb to the spot its payload was last drawn on the carrier drone (the hanging payload for a grenade, the
    * bomb-bay station otherwise), plus the hang orientation for a grenade.
    */
   private void ibzfxic(DroppedBombEntity var1) {
      if (!var1.wEJx3Itb()) {
         Minecraft var2 = Minecraft.getInstance();
         if (var2.player != null && var2.player.getUUID().equals(var1.jGhI2())) {
            var1.v5suI(0.0, 0.0, 0.0, null);
         } else {
            boolean var3 = var1.rsDd50tfV().isGrenade();
            int var4 = var1.v8ZN();
            double var5 = 0.0;
            double var7 = 0.0;
            double var9 = 0.0;
            Quaternionf var11 = null;
            int var12 = var1.uSUb();
            if (var12 >= 0 && var2.level != null && var2.level.getEntity(var12) instanceof DroneEntity var13) {
               UUID var18 = var13.getUUID();
               Vec3 var15;
               Quaternionf var16;
               if (var3) {
                  var15 = PayloadAnchors.v5suI(var18);
                  var16 = PayloadAnchors.ibzfxic(var18);
               } else if (var4 >= 0) {
                  var15 = PayloadAnchors.v5suI(var18, var4);
                  var16 = PayloadAnchors.ibzfxic(var18, var4);
               } else {
                  var15 = null;
                  var16 = null;
               }

               if (var15 != null) {
                  float var17 = var3 ? GRENADE_LIFT : 0.0F;
                  var5 = var15.x - var1.getX();
                  var7 = var15.y - (var1.getY() + var17);
                  var9 = var15.z - var1.getZ();
                  var11 = var3 ? var16 : null;
               }
            }

            var1.v5suI(var5, var7, var9, var11);
         }
      }
   }

   /**
    * Nose-first orientation: maps the model's down axis (-Y) onto the nose, adds the seeded wobble and spin, and builds
    * the rotation from the resulting basis (the same left/up/forward layout the drone renderer uses).
    */
   private Quaternionf noseOrientation(DroppedBombEntity var1, float var3) {
      Vec3 var4 = var1.v5suI(var3);
      Vector3f var5 = new Vector3f((float)var4.x, (float)var4.y, (float)var4.z);
      if (var5.lengthSquared() < 1.0E-6F) {
         var5.set(0.0F, 0.0F, -1.0F);
      }

      var5.normalize();
      float var6 = var1.tickCount + var3;
      long var7 = var1.getUUID().getLeastSignificantBits();
      float var9 = (float)(var7 & 65535L) / 65535.0F * (float) (Math.PI * 2);
      float var10 = (float)(var7 >>> 16 & 65535L) / 65535.0F * (float) (Math.PI * 2);
      Quaternionf var11 = new Quaternionf().rotationTo(0.0F, -1.0F, 0.0F, var5.x(), var5.y(), var5.z());
      Vector3f var12 = var11.transform(new Vector3f(1.0F, 0.0F, 0.0F));
      Vector3f var13 = var11.transform(new Vector3f(0.0F, 0.0F, 1.0F));
      var5.add(new Vector3f(var12).mul(WOBBLE * Mth.sin(var6 * WOBBLE_RATE_A + var9)))
         .add(new Vector3f(var13).mul(WOBBLE * Mth.sin(var6 * WOBBLE_RATE_B + var10)))
         .normalize();
      var11.rotationTo(0.0F, -1.0F, 0.0F, var5.x(), var5.y(), var5.z());
      var12 = var11.transform(new Vector3f(1.0F, 0.0F, 0.0F));
      var13 = var11.transform(new Vector3f(0.0F, 0.0F, 1.0F));
      float var14 = var6 * SPIN_RATE + var9;
      float var15 = Mth.cos(var14);
      float var16 = Mth.sin(var14);
      Vector3f var17 = new Vector3f(var12).mul(var15).add(new Vector3f(var13).mul(var16));
      Vector3f var18 = new Vector3f(var13).mul(var15).sub(new Vector3f(var12).mul(var16));
      var12 = var17;
      var13 = var18;
      Matrix3f var19 = new Matrix3f();
      var19.m00(-var12.x());
      var19.m01(-var12.y());
      var19.m02(-var12.z());
      var19.m10(var13.x());
      var19.m11(var13.y());
      var19.m12(var13.z());
      var19.m20(-var5.x());
      var19.m21(-var5.y());
      var19.m22(-var5.z());
      return new Quaternionf().setFromNormalized(var19);
   }

   public static class State extends EntityRenderState {
      @Nullable BakedGeoModel model;
      @Nullable Identifier texture;
      Vec3 blendOffset = Vec3.ZERO;
      boolean grenade;
      Quaternionf orientation = new Quaternionf();
      float modelYaw;
   }
}
