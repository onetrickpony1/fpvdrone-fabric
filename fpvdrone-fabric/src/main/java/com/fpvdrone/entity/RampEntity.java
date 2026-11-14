package com.fpvdrone.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.DroneDefinitions;
import com.fpvdrone.data.RampDefinitions;
import com.fpvdrone.data.PayloadStation;
import com.fpvdrone.data.BoneBox;
import com.fpvdrone.data.PlacedInteractZone;
import com.fpvdrone.data.RocketMount;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.data.RampDefinition;
import com.fpvdrone.data.OrientedBox;
import com.fpvdrone.data.AttachTransform;
import com.fpvdrone.item.DroneItem;
import com.fpvdrone.item.FuelCanItem;
import com.fpvdrone.item.RocketPodMountItem;
import com.fpvdrone.registry.ModEntities;
import com.fpvdrone.registry.ModItems;
import com.fpvdrone.registry.ModSounds;
import com.fpvdrone.data.Waypoint;
import com.fpvdrone.client.ClientHooks;
import com.fpvdrone.data.GeoBoneBoxes;
import com.fpvdrone.item.DroneFuel;
import com.fpvdrone.data.FlightRoute;
import com.fpvdrone.item.RocketPodMask;
import com.fpvdrone.entity.DamageZoneTarget;
import com.fpvdrone.util.BitFlags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public class RampEntity extends Entity implements DamageZoneTarget, GeoEntity {
   private static final EntityDataAccessor<String> v5suI = SynchedEntityData.defineId(RampEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> ibzfxic = SynchedEntityData.defineId(RampEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Byte> wuhB = SynchedEntityData.defineId(RampEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> dOfFwvo = SynchedEntityData.defineId(RampEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Float> rsDd50tfV = SynchedEntityData.defineId(RampEntity.class, EntityDataSerializers.FLOAT);
   private static final String uSUb = "InstalledDrone";
   private static final String jGhI2 = "RampType";
   private static final String wEJx3Itb = "Fuel";
   private static final String bAcdvq9 = "RocketPodInstalledMask";
   private static final String c1jHYPb4 = "RocketsLoadedMask";
   private static final String v8ZN = "Route";
   private final AnimatableInstanceCache zTT3nw6 = GeckoLibUtil.createInstanceCache(this);
   private double vmpmU = 0.0;
   @Nullable
   private FlightRoute b69s;
   private static final double cXHvq = 8.0;

   public void v5suI(@Nullable FlightRoute var1) {
      this.b69s = var1 != null && !var1.dOfFwvo() ? var1 : null;
   }

   @Nullable
   public FlightRoute wuhB() {
      return this.b69s;
   }

   public RampEntity(EntityType<? extends RampEntity> var1, Level var2) {
      super(var1, var2);
      this.noPhysics = false;
      this.blocksBuilding = true;
   }

   public String dOfFwvo() {
      return (String)this.entityData.get(v5suI);
   }

   public boolean rsDd50tfV() {
      String var1 = this.dOfFwvo();
      return var1 != null && !var1.isEmpty();
   }

   public void v5suI(String var1) {
      String var2 = var1 == null ? "" : var1;
      if (!this.level().isClientSide() && !var2.equals(this.dOfFwvo())) {
      }

      this.entityData.set(v5suI, var2);
   }

   public String uSUb() {
      String var1 = (String)this.entityData.get(ibzfxic);
      return var1 != null && !var1.isEmpty() ? var1 : "default";
   }

   public void ibzfxic(String var1) {
      this.entityData.set(ibzfxic, var1 == null ? "default" : var1);
   }

   public RampDefinition jGhI2() {
      return RampDefinitions.ibzfxic(this.uSUb());
   }

   public boolean wEJx3Itb() {
      return this.bAcdvq9() != 0;
   }

   public byte bAcdvq9() {
      return (Byte)this.entityData.get(wuhB);
   }

   private void v5suI(byte var1) {
      this.entityData.set(wuhB, var1);
   }

   public boolean v5suI(int var1) {
      return BitFlags.v5suI(this.bAcdvq9(), var1);
   }

   private void v5suI(int var1, boolean var2) {
      byte var3 = this.bAcdvq9();
      this.v5suI(var2 ? BitFlags.ibzfxic(var3, var1) : BitFlags.wuhB(var3, var1));
   }

   private boolean wuhB(String var1) {
      if (!this.rsDd50tfV()) {
         return false;
      }

      DroneDefinition var2 = DroneDefinitions.ibzfxic(this.dOfFwvo());
      if (!var2.wEJx3Itb()) {
         return false;
      }

      int var3 = var2.jt9oI60ST().v5suI(var1);
      return var3 >= 0 && this.v5suI(var3);
   }

   public boolean c1jHYPb4() {
      return this.rsDd50tfV() && DroneDefinitions.ibzfxic(this.dOfFwvo()).wEJx3Itb();
   }

   public double v8ZN() {
      return this.level().isClientSide() ? ((Float)this.entityData.get(rsDd50tfV)).floatValue() : this.vmpmU;
   }

   public void v5suI(double var1) {
      this.vmpmU = Mth.clamp(var1, 0.0, 20.0);
      this.entityData.set(rsDd50tfV, (float)this.vmpmU);
   }

   public double zTT3nw6() {
      return this.rsDd50tfV() && this.vmpmU() ? Math.max(0.0, 20.0 - this.v8ZN()) : 0.0;
   }

   public boolean vmpmU() {
      return !this.rsDd50tfV() || DroneDefinitions.ibzfxic(this.dOfFwvo()).qlv6NKWnH().v5suI();
   }

   public byte b69s() {
      return (Byte)this.entityData.get(dOfFwvo);
   }

   private void ibzfxic(byte var1) {
      this.entityData.set(dOfFwvo, var1);
   }

   public boolean ibzfxic(int var1) {
      return BitFlags.v5suI(this.b69s(), var1);
   }

   @Override
   public List<OrientedBox> v5suI(float var1) {
      Vec3 var2 = this.rsDd50tfV(var1);
      Quaternionf var3 = this.uSUb(var1);
      var var4 = new ArrayList<>(this.v5suI(var2, var3));
      if (this.rsDd50tfV()) {
         var4.addAll(this.ibzfxic(var2, var3));
      }

      return var4;
   }

   private void rxYnaa() {
      AABB var1 = this.dOfFwvo(1.0F);
      if (var1 != null) {
         this.setBoundingBox(var1);
      }
   }

   private AABB dOfFwvo(float var1) {
      var var2 = this.v5suI(var1);
      if (var2.isEmpty()) {
         return null;
      }

      AABB var3 = ((OrientedBox)var2.get(0)).uSUb();

      for (int var4 = 1; var4 < var2.size(); var4++) {
         var3 = var3.minmax(((OrientedBox)var2.get(var4)).uSUb());
      }

      return var3;
   }

   /** 1.1.8 getBoundingBoxForCulling; in 26.3 the renderer culls with it. */
   public AABB cullingBox() {
      AABB var1 = this.dOfFwvo(1.0F);
      return var1 != null ? var1.inflate(0.5) : this.getBoundingBox();
   }

   private Vec3 rsDd50tfV(float var1) {
      return new Vec3(
         Mth.lerp(var1, this.xOld, this.getX()),
         Mth.lerp(var1, this.yOld, this.getY()),
         Mth.lerp(var1, this.zOld, this.getZ())
      );
   }

   private Quaternionf uSUb(float var1) {
      float var2 = Mth.rotLerp(var1, this.yRotO, this.getYRot());
      return new Quaternionf().rotateY((float)Math.toRadians(180.0 - var2));
   }

   public List<OrientedBox> ibzfxic(float var1) {
      return this.v5suI(this.rsDd50tfV(var1), this.uSUb(var1));
   }

   private List<OrientedBox> v5suI(Vec3 var1, Quaternionf var2) {
      String var3 = this.jGhI2().v5suI();
      var var4 = GeoBoneBoxes.ibzfxic(var3);
      var var5 = new ArrayList(var4.size());

      for (BoneBox var7 : var4) {
         var5.add(var7.v5suI(var1, var2, new Vector3f(0.0F, 0.0F, 0.0F)));
      }

      return var5;
   }

   private List<OrientedBox> ibzfxic(Vec3 var1, Quaternionf var2) {
      DroneDefinition var3 = DroneDefinitions.ibzfxic(this.dOfFwvo());
      var var4 = this.r6s7FL3();
      boolean var5 = var3.wEJx3Itb();
      var var6 = var5 ? var3.jt9oI60ST().v5suI() : Set.of();
      var var7 = new ArrayList(var4.size());

      for (BoneBox var9 : var4) {
         if (!var5 || !var6.contains(var9.rsDd50tfV()) || this.wuhB(var9.rsDd50tfV())) {
            var7.add(var9.v5suI(var1, var2, new Vector3f(0.0F, 0.0F, 0.0F)));
         }
      }

      return var7;
   }

   private List<BoneBox> r6s7FL3() {
      DroneDefinition var1 = DroneDefinitions.ibzfxic(this.dOfFwvo());
      return this.v5suI(GeoBoneBoxes.ibzfxic(var1.hOKMYHOaC()));
   }

   private List<BoneBox> iAKNPAj() {
      DroneDefinition var1 = DroneDefinitions.ibzfxic(this.dOfFwvo());
      return this.v5suI(GeoBoneBoxes.dOfFwvo(var1.hOKMYHOaC()));
   }

   private List<BoneBox> v5suI(List<BoneBox> var1) {
      if (var1.isEmpty()) {
         return List.of();
      }

      AttachTransform var2 = this.jGhI2().ibzfxic(this.dOfFwvo());
      Matrix4f var3 = GeoBoneBoxes.v5suI(this.jGhI2().uSUb(), "drone_mount");
      Matrix4f var4 = new Matrix4f();
      var4.translate(-var2.ibzfxic() / 16.0F, var2.wuhB() / 16.0F, var2.dOfFwvo() / 16.0F);
      if (var2.jGhI2() != 0.0F) {
         var4.rotateZ((float)Math.toRadians(var2.jGhI2()));
      }

      if (var2.uSUb() != 0.0F) {
         var4.rotateY((float)Math.toRadians(-var2.uSUb()));
      }

      if (var2.rsDd50tfV() != 0.0F) {
         var4.rotateX((float)Math.toRadians(-var2.rsDd50tfV()));
      }

      if (var2.wEJx3Itb() != 1.0F) {
         var4.scale(var2.wEJx3Itb());
      }

      Matrix4f var5 = new Matrix4f(var3).mul(var4);
      Quaternionf var6 = var5.getNormalizedRotation(new Quaternionf());
      var var7 = new ArrayList(var1.size());

      for (BoneBox var9 : var1) {
         Vector3f var10 = var5.transformPosition(new Vector3f(var9.v5suI()));
         Vector3f var11 = new Vector3f(var9.ibzfxic()).mul(var2.wEJx3Itb());
         Quaternionf var12 = var6.mul(var9.wuhB(), new Quaternionf());
         var7.add(new BoneBox(var10, var11, var12, var9.dOfFwvo(), var9.rsDd50tfV()));
      }

      return var7;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(v5suI, "");
      builder.define(ibzfxic, "default");
      builder.define(wuhB, (byte)0);
      builder.define(dOfFwvo, (byte)0);
      builder.define(rsDd50tfV, 0.0F);
   }

   public void tick() {
      this.baseTick();
      this.rxYnaa();
   }

   public InteractionResult interact(Player var1, InteractionHand var2) {
      ItemStack var3 = var1.getItemInHand(var2);
      if (var3.getItem() instanceof DroneItem var4 && !this.rsDd50tfV()) {
         String var6 = var4.getDroneType();
         if (!this.jGhI2().v5suI(var6)) {
            if (!this.level().isClientSide()) {
               var1.sendOverlayMessage(Component.translatable("message.fpvdrone.ramp_rejects_drone"));
            }

            return InteractionResult.SUCCESS;
         } else {
            if (!this.level().isClientSide()) {
               this.v5suI(var6);
               this.v5suI(this.vmpmU() ? DroneFuel.get(var3) : 20.0);
               this.v5suI(RocketPodMask.installed(var3));
               this.ibzfxic(RocketPodMask.loaded(var3));
               if (!var1.getAbilities().instabuild) {
                  var3.shrink(1);
               }

               this.level().playSound(null, this, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
            }

            return InteractionResult.SUCCESS;
         }
      } else if (var3.isEmpty() && this.rsDd50tfV()) {
         if (!this.level().isClientSide()) {
            this.ibzfxic(var1, this.hOKMYHOaC());
            this.v5suI("");
            this.v5suI(0.0);
            this.v5suI((byte)0);
            this.ibzfxic((byte)0);
            this.level().playSound(null, this, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof FuelCanItem && this.rsDd50tfV() && this.vmpmU()) {
         if (!this.level().isClientSide()) {
            this.v5suI(var1, var2, var3);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof RocketPodMountItem && this.rsDd50tfV()) {
         if (!this.level().isClientSide()) {
            this.v5suI(var1, var3);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof com.fpvdrone.item.BetaFlightItem && this.rsDd50tfV()) {
         if (this.level().isClientSide()) {
            this.qlv6NKWnH();
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResult interactAt(Player var1, Vec3 var2, InteractionHand var3) {
      if (!this.rsDd50tfV()) {
         return InteractionResult.PASS;
      }

      DroneDefinition var4 = DroneDefinitions.ibzfxic(this.dOfFwvo());
      if (!var4.wEJx3Itb()) {
         return InteractionResult.PASS;
      }

      int var5 = this.v5suI(var1, var4);
      if (var5 < 0) {
         return InteractionResult.PASS;
      }

      if (this.ibzfxic(var5)) {
         if (!this.level().isClientSide()) {
            var1.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_tube_full"));
         }

         return InteractionResult.SUCCESS;
      } else {
         PayloadStation var6 = var4.jt9oI60ST().rsDd50tfV().get(var5);
         String var7 = var6.v5suI(var4.jt9oI60ST().dOfFwvo());
         ItemStack var8 = var1.getItemInHand(var3);
         Item var9 = LoiteringMunitionEntity.wuhB(var7);
         if (var9 != null && var8.is(var9)) {
            if (!this.level().isClientSide()) {
               if (!var1.getAbilities().instabuild) {
                  var8.shrink(1);
               }

               this.ibzfxic(BitFlags.ibzfxic(this.b69s(), var5));
               this.level().playSound(null, this, (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
               var1.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_loaded"));
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   private int v5suI(Player var1, DroneDefinition var2) {
      var var3 = var2.jt9oI60ST().rsDd50tfV();
      var var4 = this.r6s7FL3();
      Vec3 var5 = this.position();
      Quaternionf var6 = new Quaternionf().rotateY((float)Math.toRadians(180.0 - this.getYRot()));
      Vec3 var7 = var1.getEyePosition();
      Vec3 var8 = var7.add(var1.getLookAngle().scale(8.0));
      int var9 = -1;
      double var10 = Double.MAX_VALUE;

      for (BoneBox var13 : var4) {
         int var14 = v5suI(var3, var13.rsDd50tfV());
         if (var14 >= 0 && this.wuhB(var13.rsDd50tfV())) {
            OrientedBox var15 = var13.v5suI(var5, var6, new Vector3f(0.0F, 0.0F, 0.0F));
            Vec3 var16 = var15.v5suI(var7, var8);
            if (var16 != null) {
               double var17 = var7.distanceToSqr(var16);
               if (var17 < var10) {
                  var10 = var17;
                  var9 = var14;
               }
            }
         }
      }

      return var9;
   }

   private static int v5suI(List<PayloadStation> var0, String var1) {
      for (int var2 = 0; var2 < var0.size(); var2++) {
         if (((PayloadStation)var0.get(var2)).ibzfxic().equals(var1)) {
            return var2;
         }
      }

      return -1;
   }

   private void wuhB(int var1) {
      this.ibzfxic(BitFlags.ibzfxic(this.b69s(), var1));
   }

   private void dOfFwvo(int var1) {
      this.ibzfxic(BitFlags.wuhB(this.b69s(), var1));
   }

   public boolean v5suI(int var1, ServerPlayer var2) {
      if (!this.level().isClientSide() && this.rsDd50tfV() && var2 != null) {
         DroneDefinition var3 = DroneDefinitions.ibzfxic(this.dOfFwvo());
         if (!var3.wEJx3Itb() || var1 < 0 || var1 >= var3.jt9oI60ST().rsDd50tfV().size()) {
            return false;
         }

         if (this.ibzfxic(var1)) {
            return false;
         }

         PayloadStation var4 = var3.jt9oI60ST().rsDd50tfV().get(var1);
         if (!this.wuhB(var4.ibzfxic())) {
            return false;
         }

         String var5 = var4.v5suI(var3.jt9oI60ST().dOfFwvo());
         Item var6 = LoiteringMunitionEntity.wuhB(var5);
         if (var6 == null) {
            return false;
         }

         ItemStack var7 = ModItems.findInInventory(var2, var6);
         if (var7 == null) {
            return false;
         }

         if (!var2.getAbilities().instabuild) {
            var7.shrink(1);
         }

         this.wuhB(var1);
         this.level().playSound(null, this, (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }

   public void ibzfxic(int var1, @Nullable ServerPlayer var2) {
      if (!this.level().isClientSide() && this.rsDd50tfV() && this.ibzfxic(var1)) {
         DroneDefinition var3 = DroneDefinitions.ibzfxic(this.dOfFwvo());
         if (var3.wEJx3Itb()) {
            PayloadStation var4 = var3.jt9oI60ST().rsDd50tfV().get(var1);
            if (this.wuhB(var4.ibzfxic())) {
               String var5 = var4.v5suI(var3.jt9oI60ST().dOfFwvo());
               Item var6 = LoiteringMunitionEntity.wuhB(var5);
               this.dOfFwvo(var1);
               if (var6 != null) {
                  ItemStack var7 = new ItemStack(var6);
                  if (var2 == null || !var2.getInventory().add(var7)) {
                     if (this.level() instanceof ServerLevel dropLevel) {
         this.spawnAtLocation(dropLevel, var7);
      }
                  }
               }

               this.level().playSound(null, this, (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
         }
      }
   }

   public List<PlacedInteractZone> wuhB(float var1) {
      Vec3 var2 = this.rsDd50tfV(var1);
      Quaternionf var3 = this.uSUb(var1);
      var var4 = new ArrayList();
      var var5 = this.jGhI2().ibzfxic();
      if (!var5.isEmpty()) {
         var var6 = new HashMap();

         for (InteractZone var8 : var5) {
            var6.put(var8.ibzfxic(), var8);
         }

         for (BoneBox var18 : GeoBoneBoxes.dOfFwvo(this.jGhI2().v5suI())) {
            InteractZone var9 = (InteractZone)var6.get(var18.rsDd50tfV());
            if (var9 != null) {
               OrientedBox var10 = var18.v5suI(var2, var3, new Vector3f(0.0F, 0.0F, 0.0F));
               var4.add(new PlacedInteractZone(var9.v5suI(), var10));
            }
         }
      }

      if (this.rsDd50tfV()) {
         DroneDefinition var15 = DroneDefinitions.ibzfxic(this.dOfFwvo());
         var var17 = var15.c1jHYPb4();
         if (!var17.isEmpty()) {
            var var19 = new HashMap();

            for (InteractZone var22 : var17) {
               var19.put(var22.ibzfxic(), var22);
            }

            boolean var21 = var15.wEJx3Itb();
            var var23 = var21 ? var15.jt9oI60ST().v5suI() : Set.of();

            for (BoneBox var12 : this.iAKNPAj()) {
               InteractZone var13 = (InteractZone)var19.get(var12.rsDd50tfV());
               if (var13 != null) {
                  OrientedBox var14 = var12.v5suI(var2, var3, new Vector3f(0.0F, 0.0F, 0.0F));
                  var4.add(new PlacedInteractZone(var13.v5suI(), var14));
               }
            }

            for (BoneBox var25 : this.r6s7FL3()) {
               if (!var21 || !var23.contains(var25.rsDd50tfV()) || this.wuhB(var25.rsDd50tfV())) {
                  InteractZone var26 = (InteractZone)var19.get(var25.rsDd50tfV());
                  if (var26 != null) {
                     OrientedBox var27 = var25.v5suI(var2, var3, new Vector3f(0.0F, 0.0F, 0.0F));
                     var4.add(new PlacedInteractZone(var26.v5suI(), var27));
                  }
               }
            }
         }
      }

      return var4.isEmpty() ? List.of() : var4;
   }

   private void v5suI(Player var1, ItemStack var2) {
      if (!this.c1jHYPb4() || !DroneDefinitions.ibzfxic(this.dOfFwvo()).jt9oI60ST().uSUb().isEmpty()) {
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.no_rocket_pod"));
      } else if (this.v5suI(0)) {
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_pod_already_installed"));
      } else {
         this.v5suI(var2, var1);
      }
   }

   private void v5suI(ItemStack var1, Player var2) {
      if (!var2.getAbilities().instabuild) {
         var1.shrink(1);
      }

      this.v5suI(0, true);
      this.level().playSound(null, this, (SoundEvent)ModSounds.ROCKET_POD_INSTALL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      var2.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_pod_installed"));
   }

   public boolean v5suI(Player var1) {
      if (this.c1jHYPb4() && DroneDefinitions.ibzfxic(this.dOfFwvo()).jt9oI60ST().uSUb().isEmpty() && !this.v5suI(0)) {
         ItemStack var2 = LoiteringMunitionEntity.v5suI(var1, RocketPodMountItem.class);
         if (var2 == null) {
            return false;
         }

         this.v5suI(var2, var1);
         return true;
      } else {
         return false;
      }
   }

   public boolean ibzfxic(Player var1) {
      if (this.c1jHYPb4() && DroneDefinitions.ibzfxic(this.dOfFwvo()).jt9oI60ST().uSUb().isEmpty() && this.v5suI(0)) {
         this.v5suI(0, false);
         this.ibzfxic((byte)0);
         ItemStack var2 = new ItemStack((ItemLike)ModItems.ROCKET_POD_MOUNT.get());
         if (!var1.getInventory().add(var2)) {
            var1.drop(var2, false, Prediction.SERVER_ONLY);
         }

         this.level().playSound(null, this, (SoundEvent)ModSounds.ROCKET_POD_INSTALL.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_pod_removed"));
         return true;
      } else {
         return false;
      }
   }

   public boolean v5suI(int var1, Player var2) {
      if (this.c1jHYPb4() && !this.v5suI(var1)) {
         ItemStack var3 = LoiteringMunitionEntity.v5suI(var2, RocketPodMountItem.class);
         if (var3 == null) {
            return false;
         }

         if (!var2.getAbilities().instabuild) {
            var3.shrink(1);
         }

         this.v5suI(var1, true);
         this.level().playSound(null, this, (SoundEvent)ModSounds.ROCKET_POD_INSTALL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
         var2.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_pod_installed"));
         return true;
      } else {
         return false;
      }
   }

   public boolean ibzfxic(int var1, Player var2) {
      if (this.c1jHYPb4() && this.v5suI(var1)) {
         this.v5suI(var1, false);
         DroneDefinition var3 = DroneDefinitions.ibzfxic(this.dOfFwvo());
         var var4 = var3.jt9oI60ST().rsDd50tfV();
         var var5 = var3.jt9oI60ST().ibzfxic();
         HashSet var6 = new HashSet<>(((RocketMount)var5.get(var1)).wuhB());
         byte var7 = this.b69s();

         for (int var8 = 0; var8 < var4.size(); var8++) {
            PayloadStation var9 = (PayloadStation)var4.get(var8);
            if (var6.contains(var9.ibzfxic()) && BitFlags.v5suI(var7, var8)) {
               var7 = BitFlags.wuhB(var7, var8);
               String var10 = var9.v5suI(var3.jt9oI60ST().dOfFwvo());
               Item var11 = LoiteringMunitionEntity.wuhB(var10);
               if (var11 != null) {
                  ItemStack var12 = new ItemStack(var11);
                  if (!var2.getInventory().add(var12)) {
                     if (this.level() instanceof ServerLevel dropLevel) {
         this.spawnAtLocation(dropLevel, var12);
      }
                  }
               }
            }
         }

         this.ibzfxic(var7);
         ItemStack var13 = new ItemStack((ItemLike)ModItems.ROCKET_POD_MOUNT.get());
         if (!var2.getInventory().add(var13)) {
            var2.drop(var13, false, Prediction.SERVER_ONLY);
         }

         this.level().playSound(null, this, (SoundEvent)ModSounds.ROCKET_POD_INSTALL.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
         var2.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_pod_removed"));
         return true;
      } else {
         return false;
      }
   }

   public void wuhB(Player var1) {
      if (!this.level().isClientSide() && this.rsDd50tfV()) {
         this.jGhI2(var1);
      }
   }

   private void qlv6NKWnH() {
      ClientHooks.v5suI(this);
   }

   private void v5suI(Player var1, InteractionHand var2, ItemStack var3) {
      ItemStack var4 = var3.copy();
      var4.setCount(1);
      double var5 = FuelCanItem.getFuel(var4);
      double var7 = this.v5suI(var4);
      if (var7 <= 0.001) {
         var1.sendOverlayMessage(Component.translatable(var5 <= 0.001 ? "message.fpvdrone.can_empty" : "message.fpvdrone.drone_fuel_full"));
      } else {
         if (var3.getCount() <= 1) {
            var1.setItemInHand(var2, var4);
         } else {
            var3.shrink(1);
            if (!var1.getInventory().add(var4)) {
               var1.drop(var4, false, Prediction.SERVER_ONLY);
            }
         }

         this.level().playSound(null, this, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.7F, 1.0F);
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.fuel_added", new Object[]{String.format("%.1f", var7)}));
      }
   }

   private double v5suI(ItemStack var1) {
      double var2 = FuelCanItem.getFuel(var1);
      double var4 = Math.min(var2, this.zTT3nw6());
      if (var4 > 1.0E-6) {
         this.v5suI(this.vmpmU + var4);
         FuelCanItem.setFuel(var1, var2 - var4);
      }

      return var4;
   }

   public boolean dOfFwvo(Player var1) {
      if (!this.vmpmU()) {
         return false;
      }

      if (this.zTT3nw6() <= 1.0E-6) {
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.drone_fuel_full"));
         return false;
      }

      Inventory var2 = var1.getInventory();
      int var3 = -1;

      for (int var4 = 0; var4 < var2.getContainerSize(); var4++) {
         ItemStack var5 = var2.getItem(var4);
         if (var5.getItem() instanceof FuelCanItem && FuelCanItem.getFuel(var5) > 1.0E-6) {
            var3 = var4;
            break;
         }
      }

      if (var3 < 0) {
         return false;
      }

      ItemStack var8 = var2.getItem(var3);
      ItemStack var9 = var8.copy();
      var9.setCount(1);
      double var6 = this.v5suI(var9);
      if (var6 <= 1.0E-6) {
         return false;
      }

      if (var8.getCount() <= 1) {
         var2.setItem(var3, var9);
      } else {
         var8.shrink(1);
         if (!var2.add(var9)) {
            var1.drop(var9, false, Prediction.SERVER_ONLY);
         }
      }

      this.level().playSound(null, this, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.7F, 1.0F);
      var1.sendOverlayMessage(Component.translatable("message.fpvdrone.fuel_added", new Object[]{String.format("%.1f", var6)}));
      return true;
   }

   public boolean rsDd50tfV(Player var1) {
      if (this.vmpmU() && !(this.v8ZN() <= 0.001)) {
         Inventory var2 = var1.getInventory();
         int var3 = -1;

         for (int var4 = 0; var4 < var2.getContainerSize(); var4++) {
            ItemStack var5 = var2.getItem(var4);
            if (var5.getItem() instanceof FuelCanItem && FuelCanItem.getFuel(var5) < 19.999) {
               var3 = var4;
               break;
            }
         }

         if (var3 < 0) {
            var1.sendOverlayMessage(Component.translatable("message.fpvdrone.can_full"));
            return false;
         }

         ItemStack var10 = var2.getItem(var3);
         ItemStack var11 = var10.copy();
         var11.setCount(1);
         double var6 = FuelCanItem.getFuel(var11);
         double var8 = Math.min(this.v8ZN(), 20.0 - var6);
         if (var8 <= 0.001) {
            return false;
         }

         this.v5suI(this.v8ZN() - var8);
         FuelCanItem.setFuel(var11, var6 + var8);
         if (var10.getCount() <= 1) {
            var2.setItem(var3, var11);
         } else {
            var10.shrink(1);
            if (!var2.add(var11)) {
               var1.drop(var11, false, Prediction.SERVER_ONLY);
            }
         }

         this.level().playSound(null, this, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.7F, 1.0F);
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.fuel_drained", new Object[]{String.format("%.1f", var8)}));
         return true;
      } else {
         return false;
      }
   }

   private void jGhI2(Player var1) {
      if (this.vmpmU <= 0.001) {
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.drone_no_fuel"));
      } else {
         String var2 = this.dOfFwvo();
         float var3 = this.getYRot();
         double var4;
         double var6;
         double var8;
         if (this.b69s != null && !this.b69s.dOfFwvo()) {
            Waypoint var22 = this.b69s.v5suI(0);
            var4 = var22.v5suI;
            var6 = var22.ibzfxic;
            var8 = var22.wuhB;
         } else {
            double var10 = Math.toRadians(var3);
            var4 = this.getX() - Math.sin(var10) * 256.0;
            var8 = this.getZ() + Math.cos(var10) * 256.0;
            var6 = this.getY();
            var1.sendOverlayMessage(Component.translatable("message.fpvdrone.no_target"));
         }

         LoiteringMunitionEntity var23 = ModEntities.LOITERING_MUNITION.get().create(this.level(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
         if (var23 != null) {
            RampDefinition var11 = this.jGhI2();
            double var12 = Math.toRadians(180.0 - var3);
            double var14 = Math.sin(var12);
            double var16 = Math.cos(var12);
            double var18 = this.getX() + var11.v8ZN() * var14 + var11.zTT3nw6() * var16;
            double var20 = this.getZ() + var11.v8ZN() * var16 - var11.zTT3nw6() * var14;
            var23.snapTo(var18, this.getY() + var11.c1jHYPb4(), var20, var3, 0.0F);
            var23.v5suI(var2, var3, var4, var6, var8, var1.getUUID());
            var23.v5suI(this.vmpmU);
            if (this.wEJx3Itb() || this.b69s() != 0) {
               var23.v5suI(this.bAcdvq9(), this.b69s());
            }

            if (this.b69s != null && !this.b69s.dOfFwvo()) {
               var23.v5suI(this.b69s);
            }

            this.level().addFreshEntity(var23);
            this.v5suI("");
            this.b69s = null;
            this.v5suI(0.0);
            this.v5suI((byte)0);
            this.ibzfxic((byte)0);
            this.level().playSound(null, this, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 0.7F);
         }
      }
   }

   @Override
   public boolean hurtServer(ServerLevel hurtLevel, DamageSource var1, float var2) {
      if (!this.level().isClientSide() && !this.isRemoved()) {
         if (var1.getEntity() instanceof Player var3) {
            if (!var3.getAbilities().instabuild) {
               if (this.level() instanceof ServerLevel dropLevel) {
         this.spawnAtLocation(dropLevel, new ItemStack((ItemLike)ModItems.RAMP.get()));
      }
               if (this.rsDd50tfV()) {
                  if (this.level() instanceof ServerLevel dropLevel) {
         this.spawnAtLocation(dropLevel, this.hOKMYHOaC());
      }
               }
            }

            this.discard();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void ibzfxic(Player var1, ItemStack var2) {
      if (!var1.getInventory().add(var2)) {
         if (this.level() instanceof ServerLevel dropLevel) {
         this.spawnAtLocation(dropLevel, var2);
      }
      }
   }

   private ItemStack hOKMYHOaC() {
      ItemStack var1 = new ItemStack(ModItems.droneItemFor(this.dOfFwvo()));
      if (this.vmpmU()) {
         DroneFuel.set(var1, this.vmpmU);
      }

      RocketPodMask.set(var1, this.bAcdvq9(), this.b69s());
      return var1;
   }

   public boolean v5suI(String var1, ServerPlayer var2) {
      if (!this.level().isClientSide() && !this.rsDd50tfV() && this.jGhI2().v5suI(var1)) {
         Item var3 = ModItems.droneItemFor(var1);
         ItemStack var4 = ModItems.findInInventory(var2, var3);
         if (var4 == null) {
            return false;
         }

         this.v5suI(var1);
         this.v5suI(this.vmpmU() ? DroneFuel.get(var4) : 20.0);
         this.v5suI(RocketPodMask.installed(var4));
         this.ibzfxic(RocketPodMask.loaded(var4));
         if (!var2.getAbilities().instabuild) {
            var4.shrink(1);
         }

         this.level().playSound(null, this, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
         return true;
      } else {
         return false;
      }
   }

   public boolean uSUb(Player var1) {
      if (!this.level().isClientSide() && this.rsDd50tfV()) {
         this.ibzfxic(var1, this.hOKMYHOaC());
         this.v5suI("");
         this.v5suI(0.0);
         this.v5suI((byte)0);
         this.ibzfxic((byte)0);
         this.level().playSound(null, this, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
         return true;
      } else {
         return false;
      }
   }

   public boolean v5suI(ServerPlayer var1) {
      if (this.level().isClientSide()) {
         return false;
      } else if (this.rsDd50tfV()) {
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.ramp_remove_has_drone"));
         return false;
      } else {
         this.ibzfxic(var1, new ItemStack((ItemLike)ModItems.RAMP.get()));
         this.discard();
         return true;
      }
   }

   public List<InteractZone> cXHvq() {
      var var1 = this.jGhI2().ibzfxic();
      if (!this.rsDd50tfV()) {
         return var1;
      }

      var var2 = DroneDefinitions.ibzfxic(this.dOfFwvo()).c1jHYPb4();
      var var3 = new ArrayList(var1.size() + var2.size());
      var3.addAll(var1);
      var3.addAll(var2);
      return var3;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public boolean isPushable() {
      return false;
   }

   protected boolean canRide(Entity var1) {
      return false;
   }

   protected void readAdditionalSaveData(ValueInput var1) {
      this.v5suI(var1.getStringOr("InstalledDrone", ""));
      if (var1.getString("RampType").isPresent()) {
         this.ibzfxic(var1.getStringOr("RampType", ""));
      }

      this.v5suI(var1.getDoubleOr("Fuel", 0.0));
      this.v5suI(var1.getByteOr("RocketPodInstalledMask", (byte)0));
      this.ibzfxic(var1.getByteOr("RocketsLoadedMask", (byte)0));
      var routeTag = var1.read("Route", CompoundTag.CODEC);
      if (routeTag.isPresent()) {
         FlightRoute var2 = FlightRoute.v5suI(routeTag.get());
         this.b69s = var2.dOfFwvo() ? null : var2;
      } else {
         this.b69s = null;
      }
   }

   protected void addAdditionalSaveData(ValueOutput var1) {
      var1.putString("InstalledDrone", this.dOfFwvo());
      var1.putString("RampType", this.uSUb());
      var1.putDouble("Fuel", this.vmpmU);
      var1.putByte("RocketPodInstalledMask", this.bAcdvq9());
      var1.putByte("RocketsLoadedMask", this.b69s());
      if (this.b69s != null) {
         var1.store("Route", CompoundTag.CODEC, this.b69s.uSUb());
      }
   }


   public void registerControllers(AnimatableManager.ControllerRegistrar var1) {
      // 1.1.8 registered one controller that always stops; nothing is animated.
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.zTT3nw6;
   }
}
