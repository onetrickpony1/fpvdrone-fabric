package com.fpvdrone.entity;

import com.fpvdrone.FPVMod;
import com.fpvdrone.client.BatteryData;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.DroneConfig;
import com.fpvdrone.client.osd.OsdLayout;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.DroneDefinitions;
import com.fpvdrone.sync.SmoothingMode;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.data.PlacedInteractZone;
import com.fpvdrone.data.WeaponDamageRule;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.AttachableEntry;
import com.fpvdrone.data.IvHitInfo;
import com.fpvdrone.item.BatteryItem;
import com.fpvdrone.item.DjiControllerItem;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.item.RemoteControlItem;
import com.fpvdrone.item.Rgd5Item;
import com.fpvdrone.item.Rpg7Item;
import com.fpvdrone.network.DroneDamagePacket;
import com.fpvdrone.network.DroneDestroyedPacket;
import com.fpvdrone.network.DroneHitKickPacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.ProjectileBurstPacket;
import com.fpvdrone.physics.PhysicsState;
import com.fpvdrone.physics.PhysicsUnits;
import com.fpvdrone.registry.ModItems;
import com.fpvdrone.registry.ModSounds;
import com.fpvdrone.util.LoadedChunks;
import com.fpvdrone.sync.ObserverRange;
import com.fpvdrone.client.ClientHooks;
import com.fpvdrone.entity.EntityChunkForcer;
import com.fpvdrone.entity.ExplosionSoundRangeSource;
import com.fpvdrone.sync.RemoteSmoothed;
import com.fpvdrone.data.WeaponDamageRules;
import com.fpvdrone.sync.RemoteSmoother;
import com.fpvdrone.sync.LinearExtrapolator;
import com.fpvdrone.util.ParticleSendRange;
import com.fpvdrone.entity.InteractZoneLocator;
import com.fpvdrone.sync.KinematicState;
import com.fpvdrone.sync.RemoteStateBroadcaster;
import com.fpvdrone.entity.LookaheadChunkLoader;
import com.fpvdrone.sync.Extrapolator;
import com.fpvdrone.util.UplinkQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.ServerHolder;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public class DroneEntity extends Mob implements ObserverRange, ExplosionSoundRangeSource, RemoteSmoothed, GeoEntity {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private static final String b69s = "OwnerUUID";
   private static final String cXHvq = "DroneRoll";
   private static final String rxYnaa = "DroneConfig";
   private static final String r6s7FL3 = "AddedWeight";
   private static final String iAKNPAj = "BatteryData";
   private static final String qlv6NKWnH = "HasBattery";
   private static final String hOKMYHOaC = "OsdLayout";
   private static final String iS1JnZov = "HasRpg";
   private static final String lfyfLwuw = "ProjectileId";
   private static final String wyNDWDB = "DroneType";
   private static final String iRWc2Pi = "KirpichikiShown";
   private static final String tpzIHm = "IsThermal";
   public static final float v5suI = 580.0F;
   private static final EntityDataAccessor<Boolean> DATA_CONTROLLED = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> DATA_ROLL = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> DATA_MOTORS_ARMED = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_HAS_BATTERY = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> DATA_ADDED_WEIGHT = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(DroneEntity.class, FpvDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Boolean> ifpFOCaAs = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> uQdlyiwB = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_IN_FPV = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_FALLING = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Byte> DATA_FALL_TYPE = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<CompoundTag> DATA_OSD_LAYOUT = SynchedEntityData.defineId(DroneEntity.class, FpvDataSerializers.COMPOUND_TAG);
   private static final EntityDataAccessor<Byte> DATA_VIDEO_CHANNEL = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<String> l2IWOAGO = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> xYttSy = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Byte> DATA_THROTTLE = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Boolean> DATA_KIRPICHIKI_SHOWN = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> DATA_USED_MAH = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<String> qU1QioDiC = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Boolean> DATA_IS_THERMAL = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> lvk8rWUGD = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Byte> pyHY5 = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BYTE);
   /** Port addition: an HD Digital Camera is fitted, which allows the clean feed (F6) in survival. */
   private static final EntityDataAccessor<Boolean> DATA_HAS_HD_CAMERA = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
   // Replace Forge's IEntityAdditionalSpawnData: config and battery reach clients as synced tags.
   private static final EntityDataAccessor<CompoundTag> DATA_CONFIG = SynchedEntityData.defineId(DroneEntity.class, FpvDataSerializers.COMPOUND_TAG);
   private static final EntityDataAccessor<CompoundTag> DATA_BATTERY = SynchedEntityData.defineId(DroneEntity.class, FpvDataSerializers.COMPOUND_TAG);
   public static final int ibzfxic = 1;
   public static final int wuhB = 8;
   private float idXga;
   private float fallPitchRate;
   private float fallRollRate;
   private float fallThrustFactor;
   private float fallDragMultiplier;
   private boolean flipPhaseComplete;
   private float flipAccumulated;
   private int fallingGraceTicks;
   private static final int qV33e = 5;
   private static final float xogV5KBhg = 1.2F;
   private boolean rYlh = false;
   private boolean dead = false;
   private int kNWIu = 0;
   private int wTo0I = -1;
   private int hXya = -1;
   private boolean wogVpI = false;
   private int iY41Hgn0m = 0;
   private double nwFPfQ;
   private double qz71Ga;
   private double dZAuCP;
   private float p9T0hGf;
   private float i7EP1lKl = 0.0F;
   private float dReHaJ = 0.0F;
   private float jm7PbrhC = 0.0F;
   private boolean jfdtPQG = false;
   private double urdCiPZD;
   private double gQiZ;
   private double nTdR;
   private boolean be9wl = false;
   private static final float nL8hNS4cc = 4.0F;
   private static final float e89G = 4.0F;
   private static final float jy4YJZH = 10.0F;
   private static final float qJ6kALs = 8.0F;
   private static final float pWmAxmwe = 0.6F;
   private static final float srOgYIAu = 1.5F;
   private static final float kbDIB5 = 1.2F;
   private static final float pEnzO8R = 4.0F;
   private static final float a4SU3J = 1.5F;
   private static final float mWzvAips = 30.0F;
   private static final float siD3Qkq = 120.0F;
   private static final double i4g8OuWg = 8.0;
   private static final double ymIwVc = 4.0;
   private ChunkPos lastForcedChunk = null;
   private DroneConfig kNkq = DroneConfig.defaults();
   private BatteryData battery = BatteryData.defaults();
   private float serverUsedMah = 0.0F;
   private int spawnGraceTicks = 0;
   private int motorsArmedTicks = 0;
   private double rEs46;
   private double xgtm4P;
   private double iYcPD;
   private boolean kirpichikiAnchored = false;
   private int kirpichikiLiftoffTick = -1;
   private float prevDroneRoll = 0.0F;
   private Quaternionf iUE1QRL1b = new Quaternionf();
   private Quaternionf remoteOrientation = new Quaternionf();
   private final Quaternionf remotePrevOrientation = new Quaternionf();
   private boolean vtqeBx = false;
   private final RemoteSmoother tFKQMgw = new RemoteSmoother();
   public double dOfFwvo;
   public double rsDd50tfV;
   public double uSUb;
   public double jGhI2;
   public double wEJx3Itb;
   public double bAcdvq9;
   public int lerpSteps = 0;
   private float wbLyr = 0.0F;
   private float prevPropAngle = 0.0F;
   private float xUE3ql;
   private float ymJN;
   private float aB5HSbE7;
   private float lBCdCh6v;
   private float g47bf1Oba;
   private float iHgiE174k;
   private double reKjJYGBq;
   private double rMBK;
   private boolean ov9UANGPJ = false;
   private final UplinkQueue oWdz8 = new UplinkQueue();
   private String c2CFHNH;
   private byte yJOe8XI = 0;
   private byte xGLwK = 0;
   private boolean aZlEtI = false;
   private String je9602rx8 = "";
   private long l0qpS = Long.MIN_VALUE;
   private static final int qVWk = 40;
   private boolean signalLossFall = false;

   public DroneEntity(EntityType<? extends Mob> var1, Level var2) {
      super(var1, var2);
      this.setNoGravity(true);
      this.setNoAi(true);
      this.setPersistenceRequired();
      // 1.1.8 set noCulling here; in 26.3 the renderer decides (DroneRenderer.shouldRender).
      this.c2CFHNH = this.oWdz8.rsDd50tfV();
   }

   public boolean shouldRenderAtSqrDistance(double var1) {
      return this.wuhB(var1);
   }

   public boolean isAlwaysTicking() {
      return true;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_HAS_HD_CAMERA, false);
      builder.define(DATA_CONFIG, new CompoundTag());
      builder.define(DATA_BATTERY, new CompoundTag());
      builder.define(DATA_CONTROLLED, false);
      builder.define(DATA_ROLL, 0.0F);
      builder.define(DATA_MOTORS_ARMED, false);
      builder.define(DATA_HAS_BATTERY, false);
      builder.define(DATA_ADDED_WEIGHT, 0.0F);
      builder.define(DATA_OWNER_UUID, Optional.empty());
      builder.define(ifpFOCaAs, false);
      builder.define(uQdlyiwB, false);
      builder.define(DATA_IN_FPV, false);
      builder.define(DATA_FALLING, false);
      builder.define(DATA_FALL_TYPE, (byte)0);
      builder.define(DATA_OSD_LAYOUT, new CompoundTag());
      builder.define(DATA_VIDEO_CHANNEL, (byte)1);
      builder.define(l2IWOAGO, "");
      builder.define(xYttSy, "default");
      builder.define(DATA_THROTTLE, (byte)0);
      builder.define(DATA_KIRPICHIKI_SHOWN, false);
      builder.define(DATA_USED_MAH, 0.0F);
      builder.define(qU1QioDiC, "default");
      builder.define(DATA_IS_THERMAL, false);
      builder.define(lvk8rWUGD, 0.0F);
      builder.define(pyHY5, (byte)0);
   }

   public void checkDespawn() {
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.0);
   }

   public void tick() {
      double var1 = this.xOld;
      double var3 = this.yOld;
      double var5 = this.zOld;
      boolean var7 = this.level().isClientSide() && this.isBeingControlled();
      super.tick();
      if (var7) {
         this.xOld = var1;
         this.yOld = var3;
         this.zOld = var5;
      }

      if (this.level().isClientSide()) {
         this.prevDroneRoll = this.getDroneRoll();
         this.prevPropAngle = this.wbLyr;
         this.wbLyr = this.wbLyr + this.pWmAxmwe();
         float var8 = (float) (Math.PI * 2);
         if (this.wbLyr > var8) {
            this.wbLyr -= var8;
            this.prevPropAngle -= var8;
         }

         this.nL8hNS4cc();
      }

      if (this.level().isClientSide() && !this.isBeingControlled()) {
         this.yBodyRotO = this.yBodyRot;
         this.yHeadRotO = this.getYHeadRot();
         this.yBodyRot = this.getYRot();
         this.setYHeadRot(this.getYRot());
      }

      if (this.level().isClientSide()) {
         if (this.areMotorsArmed() || this.motorsArmedTicks > 0) {
            this.motorsArmedTicks++;
         }

         if (!this.kirpichikiAnchored) {
            this.rEs46 = this.getX();
            this.xgtm4P = this.getY();
            this.iYcPD = this.getZ();
         }

         if (this.areMotorsArmed() && !this.kirpichikiAnchored) {
            this.kirpichikiAnchored = true;
         }

         if (this.kirpichikiAnchored && this.kirpichikiLiftoffTick < 0 && !this.onGround()) {
            this.kirpichikiLiftoffTick = this.motorsArmedTicks;
         }
      }

      if (!this.level().isClientSide() && this.areMotorsArmed() && !(Boolean)this.entityData.get(DATA_KIRPICHIKI_SHOWN)) {
         this.entityData.set(DATA_KIRPICHIKI_SHOWN, true);
      }

      if (!this.level().isClientSide() && (this.isInWater() || this.isInLava())) {
         this.hurt(this.damageSources().drown(), Float.MAX_VALUE);
      }

      if (!this.level().isClientSide() && this.isBeingControlled()) {
         this.gQiZ();
      }

      if (!this.level().isClientSide() && FPVMod.consumeDroneCleanup(this.getUUID())) {
         this.v5suI(false);
         this.setBeingControlled(false);
         this.uSUb(false);
      }

      if (!this.level().isClientSide() && this.idXga() && !this.isFalling()) {
         float var71 = 0.05F;
         float var9 = this.areMotorsArmed() ? 25.0F : 1.5F;
         float var10 = var9 * var71 * 1000.0F / 3600.0F;
         this.serverUsedMah += var10;
         if (this.tickCount % 20 == 0) {
            this.entityData.set(DATA_USED_MAH, this.serverUsedMah);
         }
      }

      if (!this.level().isClientSide() && !this.isBeingControlled() && !this.rsDd50tfV() && this.idXga()) {
         this.gQiZ();
      }

      if (!this.level().isClientSide() && this.rsDd50tfV()) {
         float var77 = 0.05F;
         if (this.fallingGraceTicks > 0) {
            this.fallingGraceTicks--;
         }

         boolean var82 = false;
         if (this.getFallType() == FallType.FLIP_AND_DROP && !this.flipPhaseComplete) {
            var82 = true;
            float var88 = this.fallPitchRate * var77;
            this.flipAccumulated += var88;
            if (this.flipAccumulated >= 180.0F) {
               this.flipAccumulated = 180.0F;
               this.flipPhaseComplete = true;
               this.idXga = 0.0F;
               this.fallPitchRate = 0.0F;
               this.fallRollRate = 0.0F;
               var82 = false;
            }
         }

         if (!var82) {
            float var89 = (float)Math.exp(-var77 / 1.2F);
            this.idXga *= var89;
            this.fallPitchRate *= var89;
            this.fallRollRate *= var89;
         }

         this.setYRot(this.getYRot() + this.idXga * var77);
         this.setXRot(this.getXRot() + this.fallPitchRate * var77);
         this.setDroneRoll(this.getDroneRoll() + this.fallRollRate * var77);
         Vec3 var90 = this.getDeltaMovement();
         Vector3f var94 = PhysicsUnits.toPhysicsVelocity(var90);
         float var97 = var94.length();
         DroneDefinition.Physics var100 = this.oEuCBB().zHBAQgZP();
         float var101 = 1.225F * var100.frontalArea() * var100.bodyDragCoefficient() / 2.0F;
         var101 *= this.fallDragMultiplier;
         if (var97 > 1.0E-4F) {
            float var105 = var97 * var97 * var101 / var100.mass();
            Vector3f var107 = var94;
            var107.x = var107.x + -var94.x / var97 * var105 * var77;
            var107 = var94;
            var107.y = var107.y + -var94.y / var97 * var105 * var77;
            var107 = var94;
            var107.z = var107.z + -var94.z / var97 * var105 * var77;
         }

         var94.y -= 9.80665F * var77;
         Vec3 var106 = PhysicsUnits.toMinecraftVelocity(var94);
         this.setDeltaMovement(var106);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.gQiZ();
         if ((this.onGround() || this.isInWater() || this.isInLava()) && this.fallingGraceTicks <= 0) {
            float var110 = var94.length();
            this.handleGroundImpact(var110);
         } else if (this.getY() < this.level().getMinY() - 64) {
            this.handleGroundImpact(0.0F);
         }
      } else {
         if (!this.isBeingControlled() || this.isInFpv()) {
            if (this.level().isClientSide() && !LoadedChunks.v5suI(this.level(), Mth.floor(this.getX()), Mth.floor(this.getZ()))) {
               this.setDeltaMovement(Vec3.ZERO);
               return;
            }

            if (this.spawnGraceTicks > 0) {
               LevelChunk var76 = this.level().getChunkSource().getChunk(this.blockPosition().getX() >> 4, this.blockPosition().getZ() >> 4, false);
               if (var76 != null && var76.getPersistedStatus() == ChunkStatus.FULL) {
                  this.spawnGraceTicks--;
                  this.setDeltaMovement(Vec3.ZERO);
                  return;
               }

               this.setDeltaMovement(Vec3.ZERO);
               return;
            }

            if (this.isFalling()) {
               this.setDeltaMovement(Vec3.ZERO);
               if (!this.level().isClientSide()) {
                  this.setSyncedThrottle(0.0F);
                  if (this.idXga()) {
                     this.gQiZ();
                  }
               }

               return;
            }

            if (!this.level().isClientSide() && !this.isInFpv()) {
               BlockPos var72 = this.blockPosition();
               if (this.level().getBlockState(var72).isSuffocating(this.level(), var72)) {
                  for (int var79 = 1; var79 <= 10; var79++) {
                     BlockPos var84 = var72.above(var79);
                     if (!this.level().getBlockState(var84).isSuffocating(this.level(), var84)) {
                        this.setPos(this.getX(), var84.getY(), this.getZ());
                        this.setDeltaMovement(Vec3.ZERO);
                        this.setOnGround(true);
                        return;
                     }
                  }
               } else {
                  BlockPos var78 = var72.below();
                  BlockState var83 = this.level().getBlockState(var78);
                  if (!var83.isAir()) {
                     VoxelShape var11 = var83.getCollisionShape(this.level(), var78);
                     if (!var11.isEmpty()) {
                        double var12 = var78.getY() + var11.max(Axis.Y);
                        if (this.getY() < var12) {
                           this.setPos(this.getX(), var12, this.getZ());
                           this.setDeltaMovement(Vec3.ZERO);
                           this.setOnGround(true);
                           return;
                        }
                     }
                  }
               }
            }

            if (this.idXga() && !this.rsDd50tfV() && this.xogV5KBhg() && this.isInFpv()) {
               if (this.level().isClientSide()) {
                  return;
               }

               double var75 = 0.05;
               double var87 = 9.80665F;
               float var96 = 30.0F;
               double var99 = 9.80665F * Math.tan(Math.toRadians(30.0));
               double var104 = this.getX();
               double var112 = this.getY();
               double var115 = this.getZ();
               double var116 = this.nwFPfQ - var104;
               double var117 = this.dZAuCP - var115;
               double var118 = Math.sqrt(var116 * var116 + var117 * var117);
               double var119 = var118 > 0.001 ? var116 / var118 : 0.0;
               double var120 = var118 > 0.001 ? var117 / var118 : 0.0;
               double var121 = 0.0;
               double var122 = 0.0;
               double var123;
               switch (this.iY41Hgn0m) {
                  case 0:
                     var123 = 4.0;
                     if (var112 >= this.p9T0hGf - 0.5) {
                        this.iY41Hgn0m = 1;
                     }
                     break;
                  case 1:
                     double var125 = this.v5suI(var104, var115, var119, var120, Math.min(var118, 8.0));
                     if (var125 > Double.NEGATIVE_INFINITY) {
                        float var129 = (float)(var125 + 4.0);
                        if (var129 > this.p9T0hGf) {
                           this.p9T0hGf = var129;
                        }
                     }

                     var123 = Mth.clamp((this.p9T0hGf - var112) * 1.2F, -4.0, 4.0);
                     double var130 = Math.max(0.0, this.p9T0hGf - var112);
                     double var41 = Mth.clamp(1.0 - var130 / 3.0, 0.0, 1.0);
                     double var43 = Math.min(var118 * 0.6F, 10.0) * var41;
                     var121 = var119 * var43;
                     var122 = var120 * var43;
                     if (var118 < 1.5) {
                        this.iY41Hgn0m = 2;
                     }
                     break;
                  default:
                     double var124 = var118 < 0.4 ? 0.0 : Math.min(var118 * 0.6F, 1.5);
                     var121 = var119 * var124;
                     var122 = var120 * var124;
                     double var128 = var112 - this.qz71Ga;
                     var123 = -Mth.clamp(var128 * 1.5, 0.6, 4.0);
               }

               if (var118 > 5.0) {
                  float var126 = (float)Math.toDegrees(Math.atan2(-var116, var117));
                  if (!this.jfdtPQG || Math.abs(Mth.wrapDegrees(var126 - this.jm7PbrhC)) > 8.0F) {
                     this.jm7PbrhC = var126;
                     this.jfdtPQG = true;
                  }
               }

               if (this.jfdtPQG) {
                  this.setYRot(v5suI(this.getYRot(), this.jm7PbrhC, 6.0F));
                  this.setYBodyRot(this.getYRot());
               }

               Vec3 var127 = this.getDeltaMovement();
               double var38 = var127.x * 20.0;
               double var40 = var127.y * 20.0;
               double var42 = var127.z * 20.0;
               double var44 = Mth.clamp((var121 - var38) * 1.5, -var99, var99);
               double var46 = Mth.clamp((var122 - var42) * 1.5, -var99, var99);
               double var48 = var38 + var44 * 0.05;
               double var50 = var42 + var46 * 0.05;
               double var52 = var40 + Mth.clamp((var123 - var40) * 4.0, -30.0, 30.0) * 0.05;
               this.setDeltaMovement(var48 / 20.0, var52 / 20.0, var50 / 20.0);
               this.move(MoverType.SELF, this.getDeltaMovement());
               double var54 = Math.toRadians(this.getYRot());
               double var56 = -Math.sin(var54);
               double var58 = Math.cos(var54);
               double var60 = Math.cos(var54);
               double var62 = Math.sin(var54);
               double var64 = var44 * var56 + var46 * var58;
               double var66 = var44 * var60 + var46 * var62;
               float var68 = ibzfxic((float)Math.toDegrees(Math.atan2(var64, 9.80665F)), 30.0F);
               float var69 = ibzfxic((float)Math.toDegrees(Math.atan2(var66, 9.80665F)), 30.0F);
               if (this.iY41Hgn0m == 2 && var112 - this.qz71Ga < 1.0) {
                  var68 = 0.0F;
                  var69 = 0.0F;
               }

               float var70 = 0.25F;
               this.i7EP1lKl = this.i7EP1lKl + (var68 - this.i7EP1lKl) * 0.25F;
               this.dReHaJ = this.dReHaJ + (var69 - this.dReHaJ) * 0.25F;
               this.setXRot(this.i7EP1lKl);
               this.setDroneRoll(this.dReHaJ);
               this.setSyncedThrottle(0.6F);
               this.gQiZ();
               if (this.iY41Hgn0m != 2 || !this.onGround() && !(this.getY() <= this.qz71Ga + 0.25)) {
                  this.nTdR();
               } else {
                  this.entityData.set(DATA_IN_FPV, false);
                  this.setBeingControlled(false);
                  this.uSUb(false);
                  this.setSyncedThrottle(0.0F);
                  this.setDeltaMovement(Vec3.ZERO);
                  this.setXRot(0.0F);
                  this.setDroneRoll(0.0F);
                  this.i7EP1lKl = 0.0F;
                  this.dReHaJ = 0.0F;
                  this.nTdR();
               }

               return;
            }

            if (this.idXga() && !this.rsDd50tfV() && this.xogV5KBhg() && this.areMotorsArmed() && !this.onGround()) {
               Vec3 var74 = this.getDeltaMovement();
               float var81 = 6.0F;
               float var86 = 30.0F;
               double var93 = 0.05;
               double var98 = 9.80665F;
               double var103 = Math.toRadians(this.getYRot());
               double var111 = -Math.sin(var103);
               double var114 = Math.cos(var103);
               double var21 = Math.cos(var103);
               double var23 = Math.sin(var103);
               double var25 = (var74.x * var111 + var74.z * var114) * 20.0;
               double var27 = (var74.x * var21 + var74.z * var23) * 20.0;
               float var29 = ibzfxic(-6.0F * (float)var25, 30.0F);
               float var30 = ibzfxic(-6.0F * (float)var27, 30.0F);
               double var31 = var25 + 9.80665F * Math.tan(Math.toRadians(var29)) * 0.05;
               double var33 = var27 + 9.80665F * Math.tan(Math.toRadians(var30)) * 0.05;
               if (var31 * var25 < 0.0) {
                  var31 = 0.0;
               }

               if (var33 * var27 < 0.0) {
                  var33 = 0.0;
               }

               double var35 = (var31 * var111 + var33 * var21) / 20.0;
               double var37 = (var31 * var114 + var33 * var23) / 20.0;
               if (Math.abs(var35) < 8.0E-4) {
                  var35 = 0.0;
               }

               if (Math.abs(var37) < 8.0E-4) {
                  var37 = 0.0;
               }

               double var39 = var74.y * 0.6;
               if (Math.abs(var39) < 0.0015) {
                  var39 = 0.0;
               }

               this.setDeltaMovement(var35, var39, var37);
               this.move(MoverType.SELF, this.getDeltaMovement());
               this.setXRot(var29);
               this.setDroneRoll(var30);
               this.setSyncedThrottle(0.6F);
               this.gQiZ();
               return;
            }

            if (this.idXga() && !this.rsDd50tfV() && this.dOfFwvo()) {
               this.setDeltaMovement(Vec3.ZERO);
               return;
            }

            Vec3 var73 = this.getDeltaMovement();
            Vector3f var80 = PhysicsUnits.toPhysicsVelocity(var73);
            float var85 = var80.x;
            float var91 = var80.y;
            float var95 = var80.z;
            float var13 = var80.length();
            float var14 = 0.05F;
            DroneDefinition.Physics var15 = this.oEuCBB().zHBAQgZP();
            float var16 = 1.225F * var15.frontalArea() * var15.bodyDragCoefficient() / 2.0F;
            float var17 = var13 * var13 * var16;
            float var18 = var13 > 1.0E-4F ? var17 / var15.mass() : 0.0F;
            if (var13 > 1.0E-4F) {
               var85 += -var85 / var13 * var18 * var14;
               var91 += -var91 / var13 * var18 * var14;
               var95 += -var95 / var13 * var18 * var14;
            }

            var91 -= 9.80665F * var14;
            if (this.onGround()) {
               var91 = Math.max(0.0F, var91);
               var85 *= 0.4F;
               var95 *= 0.4F;
               if (Math.abs(var85) < 0.05F) {
                  var85 = 0.0F;
               }

               if (Math.abs(var95) < 0.05F) {
                  var95 = 0.0F;
               }

               float var19 = 12.0F;
               float var20 = Math.min(1.0F, var19 * var14);
               this.setXRot(this.getXRot() * (1.0F - var20));
               this.setDroneRoll(this.getDroneRoll() * (1.0F - var20));
            }

            var80.set(var85, var91, var95);
            Vec3 var113 = PhysicsUnits.toMinecraftVelocity(var80);
            this.setDeltaMovement(var113);
            this.move(MoverType.SELF, this.getDeltaMovement());
         }
      }
   }

   private void gQiZ() {
      this.lastForcedChunk = EntityChunkForcer.v5suI(this, this.lastForcedChunk);
   }

   public void ibzfxic() {
      this.lastForcedChunk = EntityChunkForcer.ibzfxic(this, this.lastForcedChunk);
      if (this.level() instanceof ServerLevel var1) {
         LookaheadChunkLoader.v5suI(var1, this.getUUID());
      }
   }

   public boolean removeWhenFarAway(double var1) {
      return false;
   }

   protected void registerGoals() {
   }

   public boolean causeFallDamage(float var1, float var2, DamageSource var3) {
      return false;
   }

   protected SoundEvent getHurtSound(DamageSource var1) {
      return null;
   }

   protected SoundEvent getDeathSound() {
      return null;
   }

   protected void checkFallDamage(double var1, boolean var3, BlockState var4, BlockPos var5) {
   }

   public void registerControllers(AnimatableManager.ControllerRegistrar var1) {
      // The idle/fly animations in drone.animation.json are empty; props are spun by the renderer.
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public boolean isPushable() {
      return false;
   }

   public void knockback(double var1, double var3, double var5) {
   }

   protected void pushEntities() {
   }

   public boolean isInWall() {
      return false;
   }

   // 1.1.8 getBoundingBoxForCulling() inflated the box by 1; in 26.3 culling is the renderer's (DroneRenderer).

   public boolean isAffectedByPotions() {
      return false;
   }

   public boolean canBeLeashed(Player var1) {
      return false;
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean isPickable() {
      return true;
   }

   public void setOwner(@Nullable UUID var1) {
      this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(var1));
   }

   public Optional<UUID> getOwner() {
      return (Optional<UUID>)this.entityData.get(DATA_OWNER_UUID);
   }

   public void v5suI(boolean var1) {
      this.entityData.set(ifpFOCaAs, var1);
   }

   public boolean dOfFwvo() {
      return (Boolean)this.entityData.get(ifpFOCaAs);
   }

   public boolean rsDd50tfV() {
      return (Boolean)this.entityData.get(uQdlyiwB);
   }

   public void ibzfxic(boolean var1) {
      this.entityData.set(uQdlyiwB, var1);
   }

   public boolean isFalling() {
      return (Boolean)this.entityData.get(DATA_FALLING);
   }

   public void wuhB(boolean var1) {
      this.entityData.set(DATA_FALLING, var1);
      if (var1) {
         this.ibzfxic(false);
         this.uSUb(false);
         this.setSyncedThrottle(0.0F);
         this.setDeltaMovement(Vec3.ZERO);
      }
   }

   public boolean isInFpv() {
      return (Boolean)this.entityData.get(DATA_IN_FPV);
   }

   public boolean isAirborneAbove(double var1) {
      if (this.level().isClientSide()) {
         return !this.onGround();
      }

      double var3 = this.getX();
      double var5 = this.getY();
      double var7 = this.getZ();
      int var9 = Mth.floor(var3);
      int var10 = Mth.floor(var7);
      int var11 = Mth.floor(var5);
      MutableBlockPos var12 = new MutableBlockPos();

      for (int var13 = var11; var13 >= var11 - 8; var13--) {
         var12.set(var9, var13, var10);
         VoxelShape var14 = this.level().getBlockState(var12).getCollisionShape(this.level(), var12);
         if (!var14.isEmpty()) {
            double var15 = var13 + var14.max(Axis.Y);
            return var5 - var15 > var1;
         }
      }

      return true;
   }

   public void wEJx3Itb() {
      this.entityData.set(DATA_IN_FPV, false);
   }

   private void nTdR() {
      RemoteStateBroadcaster.v5suI(this, this);
   }

   public void startServerReturnHome(double var1, double var3, double var5) {
      if (this.be9wl) {
         this.nwFPfQ = this.urdCiPZD;
         this.qz71Ga = this.gQiZ;
         this.dZAuCP = this.nTdR;
      } else {
         this.nwFPfQ = var1;
         this.qz71Ga = var3;
         this.dZAuCP = var5;
      }

      this.p9T0hGf = (float)(this.qz71Ga + 8.0);
      this.iY41Hgn0m = this.getY() < this.p9T0hGf - 0.5 ? 0 : 1;
      this.entityData.set(DATA_IN_FPV, true);
      this.entityData.set(DATA_FALLING, false);
      this.ibzfxic(false);
      this.be9wl();
   }

   private void be9wl() {
      this.jm7PbrhC = this.getYRot();
      this.jfdtPQG = true;
      this.i7EP1lKl = this.getXRot();
      this.dReHaJ = this.getDroneRoll();
      this.v5suI(false);
      this.setBeingControlled(true);
      this.entityData.set(DATA_MOTORS_ARMED, true);
   }

   public FallType getFallType() {
      byte var1 = (Byte)this.entityData.get(DATA_FALL_TYPE);
      FallType[] var2 = FallType.values();
      return var1 >= 0 && var1 < var2.length ? var2[var1] : FallType.DEAD_DROP;
   }

   public void setFallType(FallType var1) {
      this.entityData.set(DATA_FALL_TYPE, (byte)var1.ordinal());
   }

   public void dOfFwvo(boolean var1) {
      this.rYlh = var1;
   }

   public void enterFallingState(FallType var1, float[] var2) {
      this.ibzfxic(true);
      this.setFallType(var1);
      this.idXga = var2[0];
      this.fallPitchRate = var2[1];
      this.fallRollRate = var2[2];
      this.fallThrustFactor = var1.getThrustFactor();
      this.fallDragMultiplier = var1.getDragMultiplier();
      this.flipPhaseComplete = false;
      this.flipAccumulated = 0.0F;
      this.fallingGraceTicks = 5;
   }

   public void ensureAboveGround() {
      if (!this.level().isClientSide()) {
         BlockPos var1 = this.blockPosition();
         if (this.level().getBlockState(var1).isSuffocating(this.level(), var1)) {
            for (int var7 = 1; var7 <= 10; var7++) {
               BlockPos var8 = var1.above(var7);
               if (!this.level().getBlockState(var8).isSuffocating(this.level(), var8)) {
                  this.setPos(this.getX(), var8.getY(), this.getZ());
                  this.setDeltaMovement(Vec3.ZERO);
                  return;
               }
            }
         } else {
            BlockPos var2 = var1.below();
            BlockState var3 = this.level().getBlockState(var2);
            if (!var3.isAir()) {
               VoxelShape var4 = var3.getCollisionShape(this.level(), var2);
               if (!var4.isEmpty()) {
                  double var5 = var2.getY() + var4.max(Axis.Y);
                  if (this.getY() < var5) {
                     this.setPos(this.getX(), var5, this.getZ());
                     this.setDeltaMovement(Vec3.ZERO);
                  }
               }
            }
         }
      }
   }

   public void setBeingControlled(boolean var1) {
      boolean var2 = this.isBeingControlled();
      this.entityData.set(DATA_CONTROLLED, var1);
      if (var2 && !var1 && !this.rsDd50tfV()) {
         this.ibzfxic();
      }
   }

   public boolean isBeingControlled() {
      return (Boolean)this.entityData.get(DATA_CONTROLLED);
   }

   public void setDroneRoll(float var1) {
      this.entityData.set(DATA_ROLL, var1);
   }

   public float getDroneRoll() {
      return (Float)this.entityData.get(DATA_ROLL);
   }

   public float ibzfxic(float var1) {
      float var2 = this.prevDroneRoll;
      return var2 + Mth.wrapDegrees(this.getDroneRoll() - var2) * var1;
   }

   private void nL8hNS4cc() {
      ProjectileDefinition.HangSwing var1 = this.e89G();
      if (var1 != null && var1.enabled()) {
         Vec3 var2 = this.jy4YJZH();
         float var3 = 0.0F;
         float var4 = 0.0F;
         if (this.ov9UANGPJ) {
            var3 = (float)(var2.x - this.reKjJYGBq);
            var4 = (float)(var2.z - this.rMBK);
         }

         this.aB5HSbE7 = this.xUE3ql;
         this.lBCdCh6v = this.ymJN;
         float var5 = var1.accelGain() * var3 + var1.dragGain() * (float)var2.x;
         float var6 = var1.accelGain() * var4 + var1.dragGain() * (float)var2.z;
         float var7 = v5suI(-var5, var1.maxLeanDeg());
         float var8 = v5suI(var6, var1.maxLeanDeg());
         this.g47bf1Oba = this.g47bf1Oba + ((var8 - this.xUE3ql) * var1.stiffness() - this.g47bf1Oba * var1.damping());
         this.xUE3ql = this.xUE3ql + this.g47bf1Oba;
         this.iHgiE174k = this.iHgiE174k + ((var7 - this.ymJN) * var1.stiffness() - this.iHgiE174k * var1.damping());
         this.ymJN = this.ymJN + this.iHgiE174k;
         this.reKjJYGBq = var2.x;
         this.rMBK = var2.z;
         this.ov9UANGPJ = true;
      } else {
         this.ov9UANGPJ = false;
      }
   }

   private ProjectileDefinition.HangSwing e89G() {
      String var1 = this.i263D9();
      return var1 != null && !var1.isEmpty() ? ProjectileDefinitions.find(var1).map(ProjectileDefinition::hangSwing).orElse(null) : null;
   }

   private Vec3 jy4YJZH() {
      if (!this.level().isClientSide()) {
         return this.getDeltaMovement();
      }

      Vec3 var1 = ClientHooks.ibzfxic(this.getUUID());
      return var1 != null ? var1 : this.getDeltaMovement();
   }

   private static float v5suI(float var0, float var1) {
      return var0 < -var1 ? -var1 : (var0 > var1 ? var1 : var0);
   }

   public float[] wuhB(float var1) {
      float var2 = Math.max(0.0F, Math.min(1.0F, var1));
      return new float[]{this.aB5HSbE7 + (this.xUE3ql - this.aB5HSbE7) * var2, this.lBCdCh6v + (this.ymJN - this.lBCdCh6v) * var2};
   }

   public float[] vmpmU() {
      return new float[]{this.xUE3ql, this.ymJN};
   }

   public Quaternionf dOfFwvo(float var1) {
      return new Quaternionf(this.remoteOrientation).slerp(this.iUE1QRL1b, var1);
   }

   public Quaternionf getRemoteOrientation(float var1) {
      if (this.level().isClientSide() && this.isBeingControlled()) {
         return this.dOfFwvo(var1);
      }

      float var2 = Mth.rotLerp(var1, this.yBodyRotO, this.yBodyRot);
      float var3 = Mth.lerp(var1, this.xRotO, this.getXRot());
      float var4 = this.ibzfxic(var1);
      return new Quaternionf().rotationYXZ((float)Math.toRadians(180.0F - var2), (float)Math.toRadians(var3), (float)Math.toRadians(var4));
   }

   public List<PlacedInteractZone> uSUb(float var1) {
      List<InteractZone> var2 = this.oEuCBB().c1jHYPb4();
      if (var2.isEmpty()) {
         return List.of();
      }

      HashMap<String, InteractZone> var3 = new HashMap<>();

      for (InteractZone var5 : var2) {
         var3.put(var5.ibzfxic(), var5);
      }

      return InteractZoneLocator.v5suI(this, var1, var3);
   }

   public void v5suI(int var1, ServerPlayer var2) {
      if (!this.level().isClientSide() && this.mM7DsB9AR() && var1 >= 0 && var1 < this.uKpZYj()) {
         if (!this.ibzfxic(var1) && !this.lFt1ckP() && !this.iKXOcc()) {
            Item var3 = ModItems.projectileItemFor(this.oEuCBB().uKpZYj().projectile());
            if (var3 != null) {
               ItemStack var4 = ModItems.findInInventory(var2, var3);
               if (var4 != null) {
                  this.v5suI((byte)(this.wBXn() | 1 << var1));
                  if (!var2.getAbilities().instabuild) {
                     var4.shrink(1);
                  }
               }
            }
         }
      }
   }

   @Override
   public SmoothingMode b69s() {
      if (!this.level().isClientSide()) {
         return SmoothingMode.REMOTE;
      }

      boolean var1 = ClientHooks.v5suI(this.getUUID());
      return var1 ? SmoothingMode.LOCAL_PILOT : SmoothingMode.REMOTE;
   }

   @Override
   public RemoteSmoother cXHvq() {
      return this.tFKQMgw;
   }

   @Override
   public Extrapolator rxYnaa() {
      return LinearExtrapolator.v5suI;
   }

   @Override
   public double r6s7FL3() {
      return this.oEuCBB().b69s().snapDistance();
   }

   @Override
   public double iAKNPAj() {
      return this.oEuCBB().b69s().v5suI();
   }

   @Override
   public double qlv6NKWnH() {
      return this.oEuCBB().b69s().ibzfxic();
   }

   public boolean isKirpichikiAnchored() {
      return this.isBeingControlled();
   }

   private boolean qJ6kALs() {
      return this.isBeingControlled() && !this.isInFpv();
   }

   @Override
   public void v5suI(KinematicState var1) {
      Vec3 var2 = this.getDeltaMovement();
      var1.v5suI(this.getX(), this.getY(), this.getZ());
      var1.ibzfxic(var2.x, var2.y, var2.z);
      var1.v5suI(this.getYRot(), this.getXRot(), this.getDroneRoll());
      if (this.vtqeBx && this.qJ6kALs()) {
         var1.v5suI(this.remotePrevOrientation.x, this.remotePrevOrientation.y, this.remotePrevOrientation.z, this.remotePrevOrientation.w);
      } else {
         Quaternionf var3 = new Quaternionf()
            .rotationYXZ((float)Math.toRadians(180.0 - this.getYRot()), (float)Math.toRadians(this.getXRot()), (float)Math.toRadians(this.getDroneRoll()));
         var1.v5suI(var3.x, var3.y, var3.z, var3.w);
      }
   }

   public void v5suI(float var1, float var2, float var3, float var4) {
      this.remotePrevOrientation.set(var1, var2, var3, var4);
      this.vtqeBx = true;
   }

   @Override
   public void v5suI(KinematicState var1, boolean var2) {
      this.setPos(var1.v5suI, var1.ibzfxic, var1.wuhB);
      if (var1.v8ZN) {
         this.setYRot(var1.jGhI2);
         this.setXRot(var1.wEJx3Itb);
         this.yBodyRot = var1.jGhI2;
         this.setYHeadRot(var1.jGhI2);
         this.setDroneRoll(var1.bAcdvq9);
      }

      if (var1.zTT3nw6) {
         this.remoteOrientation.set(var1.c1jHYPb4);
         this.iUE1QRL1b.set(var1.c1jHYPb4);
      }

      this.setDeltaMovement(var1.dOfFwvo, var1.rsDd50tfV, var1.uSUb);
      if (var2) {
         this.setOldPosAndRot();
         this.yBodyRotO = this.yBodyRot;
         this.yHeadRotO = this.yHeadRot;
         this.prevDroneRoll = this.getDroneRoll();
      }
   }

   @Override
   public UUID iS1JnZov() {
      return !this.qJ6kALs() ? null : this.getOwner().orElse(null);
   }

   @Override
   public Vec3 lfyfLwuw() {
      if (!this.isBeingControlled()) {
         return null;
      }

      UUID var1 = this.getOwner().orElse(null);
      if (var1 == null) {
         return null;
      }

      PilotBodyEntity var2 = PilotBodyManager.getPilotBody(var1);
      return var2 != null && var2.isAlive() ? var2.position() : null;
   }

   public float getPropAngle(float var1) {
      return this.prevPropAngle + (this.wbLyr - this.prevPropAngle) * var1;
   }

   private float pWmAxmwe() {
      float var1;
      if (this.areMotorsArmed() && this.isBeingControlled()) {
         Minecraft var2 = Minecraft.getInstance();
         if (var2.player != null && FPVMod.getActiveDroneId(var2.player.getUUID()).map(var1x -> var1x.equals(this.getUUID())).orElse(false)) {
            PhysicsState var3 = FPVMod.getPhysicsState(var2.player.getUUID());
            if (var3 == null || !var3.isMotorsArmed()) {
               var1 = 0.7F;
            } else if (var3.isThrottleCut()) {
               var1 = 0.0F;
            } else {
               float var4 = ControllerReader.getThrottle();
               var1 = Mth.clamp((var4 + 1.0F) / 2.0F, 0.0F, 1.0F);
               var1 = Math.max(0.1F, var1);
            }
         } else {
            var1 = Math.max(0.1F, this.hXya());
         }
      } else if (this.areMotorsArmed()) {
         var1 = Math.max(0.15F, this.hXya());
      } else {
         var1 = 0.0F;
      }

      return var1 * 6.0F;
   }

   private static float ibzfxic(float var0, float var1) {
      return var0 < -var1 ? -var1 : (var0 > var1 ? var1 : var0);
   }

   private static float v5suI(float var0, float var1, float var2) {
      float var3 = Mth.wrapDegrees(var1 - var0);
      return Math.abs(var3) <= var2 ? var1 : var0 + Math.signum(var3) * var2;
   }

   private double v5suI(double var1, double var3, double var5, double var7, double var9) {
      double var11 = Double.NEGATIVE_INFINITY;

      for (double var13 = 1.0; var13 <= var9; var13++) {
         int var15 = Mth.floor(var1 + var5 * var13);
         int var16 = Mth.floor(var3 + var7 * var13);
         int var17 = LoadedChunks.ibzfxic(this.level(), var15, var16);
         if (var17 != Integer.MIN_VALUE && var17 > var11) {
            var11 = var17;
         }
      }

      return var11;
   }

   public void uSUb(boolean var1) {
      boolean var2 = (Boolean)this.entityData.get(DATA_MOTORS_ARMED);
      this.entityData.set(DATA_MOTORS_ARMED, var1);
      if (var1 && !var2 && !this.level().isClientSide() && this.onGround()) {
         this.urdCiPZD = this.getX();
         this.gQiZ = this.getY();
         this.nTdR = this.getZ();
         this.be9wl = true;
      }
   }

   public boolean areMotorsArmed() {
      return (Boolean)this.entityData.get(DATA_MOTORS_ARMED);
   }

   public int getMotorsArmedTicks() {
      return this.motorsArmedTicks;
   }

   public double tpzIHm() {
      return this.rEs46;
   }

   public double eJDY() {
      return this.xgtm4P;
   }

   public double zHBAQgZP() {
      return this.iYcPD;
   }

   public boolean we1GYY() {
      return this.kirpichikiAnchored;
   }

   public boolean isKirpichikiEligible() {
      return !(Boolean)this.entityData.get(DATA_KIRPICHIKI_SHOWN);
   }

   public int getKirpichikiLiftoffTick() {
      return this.kirpichikiLiftoffTick;
   }

   public DroneConfig getConfig() {
      return this.kNkq;
   }

   public void setConfig(DroneConfig var1) {
      this.kNkq = var1 != null ? var1 : DroneConfig.defaults();
      if (!this.level().isClientSide()) {
         this.entityData.set(DATA_CONFIG, this.kNkq.toNbt());
      }
   }

   public float getAddedWeight() {
      return (Float)this.entityData.get(DATA_ADDED_WEIGHT);
   }

   public void setAddedWeight(float var1) {
      this.entityData.set(DATA_ADDED_WEIGHT, Math.max(0.0F, var1));
   }

   public void addWeight(float var1) {
      float var2 = this.getAddedWeight();
      this.setAddedWeight(var2 + var1);
   }

   public BatteryData getBattery() {
      return this.battery;
   }

   public void setBattery(BatteryData var1) {
      this.battery = var1 != null ? var1 : BatteryData.defaults();
      if (!this.level().isClientSide()) {
         this.entityData.set(DATA_BATTERY, this.battery.toNbt());
      }
      this.entityData.set(qU1QioDiC, this.battery.getTypeId());
      this.serverUsedMah = 0.0F;
      this.entityData.set(DATA_USED_MAH, 0.0F);
   }

   public float getSyncedUsedMah() {
      return (Float)this.entityData.get(DATA_USED_MAH);
   }

   public float n9lX8A() {
      return (Float)this.entityData.get(lvk8rWUGD);
   }

   public void c1jHYPb4(float var1) {
      this.entityData.set(lvk8rWUGD, var1);
   }

   public byte wBXn() {
      return (Byte)this.entityData.get(pyHY5);
   }

   public void v5suI(byte var1) {
      this.entityData.set(pyHY5, var1);
   }

   public boolean mM7DsB9AR() {
      return this.oEuCBB().jGhI2();
   }

   public int uKpZYj() {
      DroneDefinition var1 = this.oEuCBB();
      return var1.jGhI2() ? var1.uKpZYj().stations().size() : 0;
   }

   public void v5suI(int var1) {
      if (this.level().isClientSide() && var1 >= 0 && var1 < this.uKpZYj()) {
         if (!this.signalLossFall) {
            this.yJOe8XI |= (byte)(1 << var1);
            this.xGLwK = this.wBXn();
            this.l0qpS = this.level().getGameTime() + 40L;
         }
      }
   }

   public void l2IWOAGO() {
      if (this.level().isClientSide()) {
         this.aZlEtI = true;
         this.je9602rx8 = (String)this.entityData.get(l2IWOAGO);
         this.l0qpS = this.level().getGameTime() + 40L;
      }
   }

   private byte srOgYIAu() {
      if (this.yJOe8XI == 0) {
         return 0;
      }

      byte var1 = this.wBXn();
      if (var1 != this.xGLwK) {
         this.yJOe8XI &= var1;
         this.xGLwK = var1;
      }

      if (this.yJOe8XI != 0 && this.level().getGameTime() > this.l0qpS) {
         this.yJOe8XI = 0;
      }

      return this.yJOe8XI;
   }

   public boolean ibzfxic(int var1) {
      if (var1 >= 0 && var1 < this.uKpZYj()) {
         int var2 = this.wBXn();
         if (this.level().isClientSide()) {
            var2 &= ~this.srOgYIAu();
         }

         return (var2 & 1 << var1) != 0;
      } else {
         return false;
      }
   }

   public boolean xYttSy() {
      return this.uKpZYj() > 0 && this.wBXn() != 0;
   }

   public int kNoG() {
      return Integer.bitCount(this.wBXn() & 0xFF);
   }

   public void fvsAOg() {
      int var1 = this.uKpZYj();
      if (var1 > 0) {
         this.v5suI((byte)((1 << var1) - 1));
      }
   }

   public boolean lFt1ckP() {
      int var1 = this.uKpZYj();
      return var1 > 0 && this.wBXn() == (byte)((1 << var1) - 1);
   }

   public int qU1QioDiC() {
      int var1 = this.uKpZYj();

      for (int var2 = 0; var2 < var1; var2++) {
         if (!this.ibzfxic(var2)) {
            this.v5suI((byte)(this.wBXn() | 1 << var2));
            return var2;
         }
      }

      return -1;
   }

   public int zQOhlhTZx() {
      int var1 = this.uKpZYj();

      for (int var2 = 0; var2 < var1; var2++) {
         if (this.ibzfxic(var2)) {
            return var2;
         }
      }

      return -1;
   }

   public boolean lvk8rWUGD() {
      return this.xYttSy();
   }

   public void setSignalLossFall(boolean var1) {
      this.signalLossFall = var1;
   }

   public boolean pyHY5() {
      return this.signalLossFall;
   }

   public void wuhB(int var1) {
      if (var1 >= 0 && var1 < this.uKpZYj()) {
         if (!this.signalLossFall) {
            this.v5suI((byte)(this.wBXn() & ~(1 << var1)));
         }
      }
   }

   public void setServerUsedMah(float var1) {
      this.serverUsedMah = Math.max(0.0F, var1);
      this.entityData.set(DATA_USED_MAH, this.serverUsedMah);
   }

   public boolean idXga() {
      return (Boolean)this.entityData.get(DATA_HAS_BATTERY);
   }

   public void setHasBattery(boolean var1) {
      this.entityData.set(DATA_HAS_BATTERY, var1);
   }

   public String i263D9() {
      String var1 = (String)this.entityData.get(l2IWOAGO);
      if (this.level().isClientSide() && this.aZlEtI) {
         if (var1.equals(this.je9602rx8) && this.level().getGameTime() <= this.l0qpS) {
            return "";
         }

         this.aZlEtI = false;
      }

      return var1;
   }

   public void v5suI(String var1) {
      this.entityData.set(l2IWOAGO, var1 == null ? "" : var1);
   }

   public Optional<ProjectileDefinition> rOIp0p7() {
      return ProjectileDefinitions.find(this.i263D9());
   }

   public boolean iKXOcc() {
      return !this.i263D9().isEmpty();
   }

   private Component kbDIB5() {
      String var1 = ProjectileDefinitions.find(this.i263D9()).map(ProjectileDefinition::displayName).orElse("item.fpvdrone.rpg7");
      return Component.translatable(var1);
   }

   public void bAcdvq9(boolean var1) {
      this.v5suI(var1 ? "rpg7" : "");
   }

   private static ItemStack rsDd50tfV(String var0) {
      Item var1 = ModItems.projectileItemFor(var0);
      return new ItemStack((ItemLike)(var1 != null ? var1 : (ItemLike)ModItems.RPG7.get()));
   }

   private static String uSUb(String var0) {
      return "rgd5".equals(var0) ? "entity.fpvdrone.drone.rgd_detached" : "entity.fpvdrone.drone.rpg_detached";
   }

   public String jt9oI60ST() {
      return (String)this.entityData.get(xYttSy);
   }

   public void ibzfxic(String var1) {
      this.entityData.set(xYttSy, var1 != null && !var1.isEmpty() ? var1 : "default");
      this.pEnzO8R();
      this.refreshDimensions();
   }

   public DroneDefinition oEuCBB() {
      return DroneDefinitions.ibzfxic(this.jt9oI60ST());
   }

   @Override
   public float uj1PcjLg() {
      return this.oEuCBB().qV33e();
   }

   public boolean zgQgCA9() {
      return this.oEuCBB().pxmzA().kirpichiki();
   }

   public boolean qV33e() {
      return this.oEuCBB().pxmzA().propellerBlur();
   }

   public boolean xogV5KBhg() {
      return this.oEuCBB().dOfFwvo();
   }

   public Identifier rYlh() {
      return Identifier.fromNamespaceAndPath("fpvdrone", this.oEuCBB().iElAbEZe());
   }

   public Identifier sZzOK7T() {
      return Identifier.fromNamespaceAndPath("fpvdrone", this.oEuCBB().sTrE());
   }

   public float kNWIu() {
      return this.oEuCBB().ifpFOCaAs();
   }

   public boolean wuhB(String var1) {
      return this.oEuCBB().wuhB(var1);
   }

   public boolean dOfFwvo(String var1) {
      return this.oEuCBB().rsDd50tfV(var1);
   }

   private void pEnzO8R() {
      if (this.kNkq.equals(DroneConfig.defaults())) {
         DroneDefinition var1 = this.oEuCBB();
         DroneDefinition.Physics var2 = var1.zHBAQgZP();
         this.kNkq.setMotorKv(var2.motorKv());
         this.kNkq.setPropDiameter(var2.propDiameter());
         this.kNkq.setPropPitch(var2.propPitch());
         this.kNkq.setDragCoefficient(var2.dragCoefficient());
         this.kNkq.setThrustMultiplier(var2.thrustMultiplier());
         DroneDefinition.Rates var3 = var1.we1GYY();

         for (int var4 = 0; var4 < 4; var4++) {
            this.kNkq.setRcRate(var4, var3.rcRate()[var4]);
            this.kNkq.setSuperRate(var4, var3.superRate()[var4]);
            this.kNkq.setExpo(var4, var3.expo()[var4]);
         }

         this.kNkq.setFlightMode3d(var1.uQdlyiwB());
         this.kNkq.setDroneName(var1.ivDR());
      }
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> var1) {
      super.onSyncedDataUpdated(var1);
      if (this.level().isClientSide()) {
         if (DATA_CONFIG.equals(var1)) {
            CompoundTag var9 = this.entityData.get(DATA_CONFIG);
            if (!var9.isEmpty()) {
               this.kNkq = DroneConfig.fromNbt(var9);
            }
         } else if (DATA_BATTERY.equals(var1)) {
            CompoundTag var9 = this.entityData.get(DATA_BATTERY);
            if (!var9.isEmpty()) {
               this.battery = BatteryData.fromNbt(var9);
            }
         }
      }

      if (this.level().isClientSide() && xYttSy.equals(var1)) {
         this.pEnzO8R();
         this.refreshDimensions();
      }

      if (this.level().isClientSide() && qU1QioDiC.equals(var1)) {
         String var2 = (String)this.entityData.get(qU1QioDiC);
         if (!var2.equals(this.battery.getTypeId())) {
            this.battery = BatteryData.forType(var2);
         }
      }
   }

   public boolean isThermal() {
      return (Boolean)this.entityData.get(DATA_IS_THERMAL);
   }

   public void setThermal(boolean var1) {
      this.entityData.set(DATA_IS_THERMAL, var1);
   }

   public float hXya() {
      return ((Byte)this.entityData.get(DATA_THROTTLE) & 0xFF) / 255.0F;
   }

   public void setSyncedThrottle(float var1) {
      this.entityData.set(DATA_THROTTLE, (byte)(Mth.clamp(var1, 0.0F, 1.0F) * 255.0F));
   }

   public OsdLayout getOsdLayout() {
      CompoundTag var1 = (CompoundTag)this.entityData.get(DATA_OSD_LAYOUT);
      return var1 != null && !var1.isEmpty() ? OsdLayout.fromNbt(var1) : OsdLayout.defaults();
   }

   public void setOsdLayout(OsdLayout var1) {
      this.entityData.set(DATA_OSD_LAYOUT, var1 != null ? var1.toNbt() : new CompoundTag());
   }

   public int getVideoChannel() {
      return (Byte)this.entityData.get(DATA_VIDEO_CHANNEL);
   }

   public void dOfFwvo(int var1) {
      this.entityData.set(DATA_VIDEO_CHANNEL, (byte)Math.max(1, Math.min(8, var1)));
   }

   public String getVideoChannelName() {
      return "R" + this.getVideoChannel();
   }

   public void assignUniqueChannel(UUID var1) {
      if (!this.level().isClientSide() && this.level() instanceof ServerLevel var2) {
         HashSet var10 = new HashSet();
         ServerPlayer var4 = var2.getServer().getPlayerList().getPlayer(var1);
         if (var4 != null) {
            for (int var5 = 0; var5 < var4.getInventory().getContainerSize(); var5++) {
               ItemStack var6 = var4.getInventory().getItem(var5);
               if (var6.getItem() instanceof RemoteControlItem) {
                  RemoteControlItem.getLinkedDroneId(var6).filter(var1x -> !var1x.equals(this.getUUID())).ifPresent(var10::add);
               } else if (var6.getItem() instanceof DjiControllerItem) {
                  DjiControllerItem.getLinkedDroneId(var6).filter(var1x -> !var1x.equals(this.getUUID())).ifPresent(var10::add);
               } else if (var6.getItem() instanceof FpvGogglesItem) {
                  Map<Integer, UUID> var7 = FpvGogglesItem.getLinkedDrones(var6);

                  for (UUID var9 : var7.values()) {
                     if (!var9.equals(this.getUUID())) {
                        var10.add(var9);
                     }
                  }
               }
            }

            ItemStack var11 = var4.getItemBySlot(EquipmentSlot.HEAD);
            if (var11.getItem() instanceof FpvGogglesItem) {
               Map<Integer, UUID> var13 = FpvGogglesItem.getLinkedDrones(var11);

               for (UUID var18 : var13.values()) {
                  if (!var18.equals(this.getUUID())) {
                     var10.add(var18);
                  }
               }
            }
         }

         HashSet var12 = new HashSet();

         for (Entity var17 : var2.getAllEntities()) {
            if (var17 instanceof DroneEntity var19 && var10.contains(var19.getUUID()) && !var19.isRemoved() && var19.isAlive()) {
               var12.add(var19.getVideoChannel());
            }
         }

         if (var12.contains(this.getVideoChannel())) {
            for (int var15 = 1; var15 <= 8; var15++) {
               if (!var12.contains(var15)) {
                  this.dOfFwvo(var15);
                  return;
               }
            }
         }
      }
   }

   public float qz71Ga() {
      float var1 = this.rOIp0p7().map(ProjectileDefinition::weightGrams).orElse(0.0F);
      return this.oEuCBB().eJDY() + var1 + this.dZAuCP() + this.battery.getWeightGrams();
   }

   public float dZAuCP() {
      if (!this.mM7DsB9AR()) {
         return 0.0F;
      }

      DroneDefinition.BombBay var1 = this.oEuCBB().uKpZYj();
      float var2 = ProjectileDefinitions.find(var1.projectile()).map(ProjectileDefinition::weightGrams).orElse(0.0F);
      if (var2 <= 0.0F) {
         return 0.0F;
      }

      int var3 = 0;
      int var4 = this.uKpZYj();

      for (int var5 = 0; var5 < var4; var5++) {
         if (this.ibzfxic(var5)) {
            var3++;
         }
      }

      return var2 * var3;
   }

   protected void addAdditionalSaveData(ValueOutput var1) {
      super.addAdditionalSaveData(var1);
      this.getOwner().ifPresent(var1x -> var1.store("OwnerUUID", UUIDUtil.CODEC, var1x));
      var1.putFloat("DroneRoll", this.getDroneRoll());
      if (this.xogV5KBhg() && this.idXga() && this.areMotorsArmed() && !this.rsDd50tfV() && !this.isInFpv() && this.isAirborneAbove(0.1)) {
         var1.putBoolean("HoverOnReload", true);
      }

      var1.store("DroneConfig", CompoundTag.CODEC, this.kNkq.toNbt());
      var1.putFloat("AddedWeight", this.getAddedWeight());
      var1.store("BatteryData", CompoundTag.CODEC, this.battery.toNbt());
      var1.putBoolean("HasBattery", this.idXga());
      var1.putFloat("ServerUsedMah", this.serverUsedMah);
      var1.putBoolean("HasRpg", this.iKXOcc());
      var1.putString("ProjectileId", this.i263D9());
      var1.putString("DroneType", this.jt9oI60ST());
      var1.putBoolean("IsThermal", this.isThermal());
      var1.putBoolean("HasHdCamera", this.hasHdCamera());
      var1.putByte("BombsLoaded", this.wBXn());
      var1.putBoolean("KirpichikiShown", (Boolean)this.entityData.get(DATA_KIRPICHIKI_SHOWN));
      CompoundTag var2 = (CompoundTag)this.entityData.get(DATA_OSD_LAYOUT);
      if (var2 != null && !var2.isEmpty()) {
         var1.store("OsdLayout", CompoundTag.CODEC, var2);
      }

      var1.putByte("VideoChannel", (byte)this.getVideoChannel());
      var1.putInt("WeaponHits", this.kNWIu);
      var1.putInt("CoreZoneHp", this.wTo0I);
      var1.putInt("PeripheralZoneHp", this.hXya);
      var1.putBoolean("Frozen", this.isFalling());
      var1.putString("TelemetryLinkStatus", this.c2CFHNH);
      if (this.be9wl) {
         var1.putBoolean("HasTakeoff", true);
         var1.putDouble("TakeoffX", this.urdCiPZD);
         var1.putDouble("TakeoffY", this.gQiZ);
         var1.putDouble("TakeoffZ", this.nTdR);
      }

      if (this.isInFpv()) {
         var1.putBoolean("ServerRth", true);
         var1.putInt("RthPhase", this.iY41Hgn0m);
         var1.putDouble("RthHomeX", this.nwFPfQ);
         var1.putDouble("RthHomeY", this.qz71Ga);
         var1.putDouble("RthHomeZ", this.dZAuCP);
         var1.putFloat("RthCruiseY", this.p9T0hGf);
      }

      if (this.rsDd50tfV()) {
         var1.putBoolean("IsFalling", true);
         var1.putByte("FallTypeId", (byte)this.getFallType().ordinal());
         var1.putFloat("FallYawRate", this.idXga);
         var1.putFloat("FallPitchRate", this.fallPitchRate);
         var1.putFloat("FallRollRate", this.fallRollRate);
         var1.putFloat("FallThrustFactor", this.fallThrustFactor);
         var1.putFloat("FallDragMultiplier", this.fallDragMultiplier);
         var1.putBoolean("FlipPhaseComplete", this.flipPhaseComplete);
         var1.putFloat("FlipAccumulated", this.flipAccumulated);
         var1.putBoolean("ProjectileKill", this.dead);
      }
   }

   protected void readAdditionalSaveData(ValueInput var1) {
      super.readAdditionalSaveData(var1);
      var1.read("OwnerUUID", UUIDUtil.CODEC).ifPresent(this::setOwner);

      if (has(var1, "DroneRoll")) {
         this.setDroneRoll(var1.getFloatOr("DroneRoll", 0.0F));
      }

      if (has(var1, "Frozen")) {
         this.entityData.set(DATA_FALLING, var1.getBooleanOr("Frozen", false));
      }

      if (has(var1, "WeaponHits")) {
         this.kNWIu = var1.getIntOr("WeaponHits", 0);
      }

      if (has(var1, "CoreZoneHp")) {
         this.wTo0I = var1.getIntOr("CoreZoneHp", 0);
      }

      if (has(var1, "PeripheralZoneHp")) {
         this.hXya = var1.getIntOr("PeripheralZoneHp", 0);
      }

      if (has(var1, "TelemetryLinkStatus")) {
         this.c2CFHNH = var1.getStringOr("TelemetryLinkStatus", "");
      }

      if (var1.getBooleanOr("HasTakeoff", false)) {
         this.be9wl = true;
         this.urdCiPZD = var1.getDoubleOr("TakeoffX", 0.0);
         this.gQiZ = var1.getDoubleOr("TakeoffY", 0.0);
         this.nTdR = var1.getDoubleOr("TakeoffZ", 0.0);
      }

      if (var1.getBooleanOr("ServerRth", false)) {
         this.entityData.set(DATA_IN_FPV, true);
         this.iY41Hgn0m = var1.getIntOr("RthPhase", 0);
         this.nwFPfQ = var1.getDoubleOr("RthHomeX", 0.0);
         this.qz71Ga = var1.getDoubleOr("RthHomeY", 0.0);
         this.dZAuCP = var1.getDoubleOr("RthHomeZ", 0.0);
         this.p9T0hGf = var1.getFloatOr("RthCruiseY", 0.0F);
      }

      if (has(var1, "DroneConfig")) {
         this.kNkq = DroneConfig.fromNbt(var1.read("DroneConfig", CompoundTag.CODEC).orElseGet(CompoundTag::new));
      }

      if (has(var1, "AddedWeight")) {
         this.setAddedWeight(var1.getFloatOr("AddedWeight", 0.0F));
      }

      if (has(var1, "BatteryData")) {
         this.battery = BatteryData.fromNbt(var1.read("BatteryData", CompoundTag.CODEC).orElseGet(CompoundTag::new));
      }

      this.entityData.set(qU1QioDiC, this.battery.getTypeId());
      this.entityData.set(DATA_CONFIG, this.kNkq.toNbt());
      this.entityData.set(DATA_BATTERY, this.battery.toNbt());
      if (has(var1, "HasBattery")) {
         this.setHasBattery(var1.getBooleanOr("HasBattery", false));
      }

      if (has(var1, "ServerUsedMah")) {
         this.serverUsedMah = var1.getFloatOr("ServerUsedMah", 0.0F);
         this.entityData.set(DATA_USED_MAH, this.serverUsedMah);
      }

      if (has(var1, "BombsLoaded")) {
         this.v5suI(var1.getByteOr("BombsLoaded", (byte)0));
      }

      if (has(var1, "ProjectileId")) {
         this.v5suI(var1.getStringOr("ProjectileId", ""));
      } else if (has(var1, "HasRpg")) {
         this.bAcdvq9(var1.getBooleanOr("HasRpg", false));
      }

      if (this.iKXOcc() && this.xYttSy()) {
         this.v5suI((byte)0);
      }

      if (has(var1, "DroneType")) {
         this.ibzfxic(var1.getStringOr("DroneType", ""));
      }

      if (has(var1, "IsThermal")) {
         this.setThermal(var1.getBooleanOr("IsThermal", false));
      }

      this.setHasHdCamera(var1.getBooleanOr("HasHdCamera", false));

      if (has(var1, "KirpichikiShown")) {
         this.entityData.set(DATA_KIRPICHIKI_SHOWN, var1.getBooleanOr("KirpichikiShown", false));
      } else {
         this.entityData.set(DATA_KIRPICHIKI_SHOWN, true);
      }

      if (has(var1, "OsdLayout")) {
         this.setOsdLayout(OsdLayout.fromNbt(var1.read("OsdLayout", CompoundTag.CODEC).orElseGet(CompoundTag::new)));
      }

      if (has(var1, "VideoChannel")) {
         this.dOfFwvo(var1.getByteOr("VideoChannel", (byte)0));
      }

      if (has(var1, "IsFalling") && var1.getBooleanOr("IsFalling", false)) {
         this.ibzfxic(true);
         if (has(var1, "FallTypeId")) {
            byte var2 = var1.getByteOr("FallTypeId", (byte)0);
            FallType[] var3 = FallType.values();
            if (var2 >= 0 && var2 < var3.length) {
               this.setFallType(var3[var2]);
            }
         }

         this.idXga = var1.getFloatOr("FallYawRate", 0.0F);
         this.fallPitchRate = var1.getFloatOr("FallPitchRate", 0.0F);
         this.fallRollRate = var1.getFloatOr("FallRollRate", 0.0F);
         this.fallThrustFactor = var1.getFloatOr("FallThrustFactor", 0.0F);
         this.fallDragMultiplier = var1.getFloatOr("FallDragMultiplier", 0.0F);
         this.flipPhaseComplete = var1.getBooleanOr("FlipPhaseComplete", false);
         this.flipAccumulated = var1.getFloatOr("FlipAccumulated", 0.0F);
         this.dead = var1.getBooleanOr("ProjectileKill", false);
         this.fallingGraceTicks = 5;
      }

      this.setBeingControlled(false);
      this.v5suI(false);
      this.uSUb(false);
      if (this.isInFpv() && !this.rsDd50tfV()) {
         this.be9wl();
      }

      if (var1.getBooleanOr("HoverOnReload", false) && this.xogV5KBhg() && this.idXga() && !this.rsDd50tfV() && !this.isInFpv()) {
         this.entityData.set(DATA_MOTORS_ARMED, true);
      }

      this.spawnGraceTicks = 10;
      DroneLinkManager.linkDrone(this.getUUID(), this.getOwner().orElse(this.getUUID()));
   }

   /** ValueInput has no contains(); present means it decodes as anything. */
   private static boolean has(ValueInput in, String key) {
      return in.read(key, com.mojang.serialization.Codec.PASSTHROUGH).isPresent();
   }



   public ItemStack toItemStack() {
      Item var1;
      if (this.isThermal()) {
         var1 = (Item)ModItems.THERMAL_DRONE.get();
      } else if ("mavic".equals(this.jt9oI60ST())) {
         var1 = (Item)ModItems.MAVIC_DRONE.get();
      } else {
         var1 = (Item)ModItems.DRONE.get();
      }

      ItemStack var2 = new ItemStack(var1);
      CompoundTag var3 = new CompoundTag();
      if (this.idXga()) {
         var3.putBoolean("HasBattery", true);
         BatteryData var4 = this.getBattery();
         int var5 = var4.getCapacityMah() - (int)this.serverUsedMah;
         if (var5 < 0) {
            var5 = 0;
         }

         var4.setRemainingMah(var5);
         var3.put("BatteryData", var4.toNbt());
      }

      if (this.iKXOcc()) {
         var3.putBoolean("HasRpg", true);
         var3.putString("ProjectileId", this.i263D9());
      }

      if (this.xYttSy()) {
         var3.putByte("BombsLoaded", this.wBXn());
      }

      var3.putString("DroneType", this.jt9oI60ST());
      if (this.hasHdCamera()) {
         var3.putBoolean("HasHdCamera", true);
      }

      com.fpvdrone.util.ItemData.edit(var2, var9 -> var9.merge(var3));
      return var2;
   }

   @Nullable
   public ItemStack v5suI(Player var1) {
      Inventory var2 = var1.getInventory();

      for (int var3 = 0; var3 < var2.getContainerSize(); var3++) {
         ItemStack var4 = var2.getItem(var3);
         if (var4.getItem() instanceof BatteryItem) {
            BatteryData var5 = BatteryItem.getBatteryData(var4);
            if (this.dOfFwvo(var5.getTypeId())) {
               return var4;
            }
         }
      }

      return null;
   }

   public boolean ibzfxic(Player var1) {
      return !this.idXga() && this.v5suI(var1) != null;
   }

   public boolean v5suI(ServerPlayer var1, ItemStack var2) {
      if (this.idXga()) {
         var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.battery_already_attached").withStyle(Style.EMPTY.withColor(16749622)));
         return false;
      }

      BatteryData var3 = BatteryItem.getBatteryData(var2);
      if (!this.dOfFwvo(var3.getTypeId())) {
         var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.battery_not_supported").withStyle(Style.EMPTY.withColor(16749622)));
         return false;
      }

      this.setBattery(var3);
      if (!var3.isFullCharge()) {
         this.serverUsedMah = var3.getCapacityMah() - var3.getRemainingMah();
         this.entityData.set(DATA_USED_MAH, this.serverUsedMah);
      }

      this.setHasBattery(true);
      if (!var1.getAbilities().instabuild) {
         var2.shrink(1);
      }

      this.level().playSound(null, this.blockPosition(), (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
      var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.battery_attached").withStyle(Style.EMPTY.withColor(16749622)));
      return true;
   }

   public boolean v5suI(ServerPlayer var1) {
      if (!this.idXga()) {
         return false;
      }

      BatteryData var2 = this.getBattery();
      int var3 = var2.getCapacityMah() - (int)this.serverUsedMah;
      if (var3 < 0) {
         var3 = 0;
      }

      var2.setRemainingMah(var3);
      ItemStack var4 = new ItemStack(ModItems.batteryItemFor(var2.getTypeId()));
      BatteryItem.setBatteryData(var4, var2);
      this.setHasBattery(false);
      this.setBattery(BatteryData.defaults());
      this.a4SU3J();
      this.ibzfxic();
      if (!var1.getInventory().add(var4)) {
         var1.drop(var4, false, net.minecraft.util.Prediction.SERVER_ONLY);
      }

      this.level().playSound(null, this.blockPosition(), (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
      var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.battery_detached").withStyle(Style.EMPTY.withColor(16749622)));
      return true;
   }

   private void a4SU3J() {
      if (!this.level().isClientSide()) {
         this.wEJx3Itb();
         this.uSUb(false);
         this.setSyncedThrottle(0.0F);
         this.setBeingControlled(false);
      }
   }

   @Nullable
   private String v5suI(ItemStack var1) {
      for (AttachableEntry var3 : this.oEuCBB().wBXn()) {
         Item var4 = ModItems.projectileItemFor(var3.v5suI());
         if (var4 != null && var1.is(var4)) {
            return var3.v5suI();
         }
      }

      return null;
   }

   @Nullable
   public AttachableEntry wuhB(Player var1) {
      for (AttachableEntry var3 : this.oEuCBB().wBXn()) {
         Item var4 = ModItems.projectileItemFor(var3.v5suI());
         if (var4 != null && ModItems.findInInventory(var1, var4) != null) {
            return var3;
         }
      }

      return null;
   }

   @Nullable
   public ItemStack dOfFwvo(Player var1) {
      AttachableEntry var2 = this.wuhB(var1);
      return var2 == null ? null : ModItems.findInInventory(var1, ModItems.projectileItemFor(var2.v5suI()));
   }

   public boolean rsDd50tfV(Player var1) {
      return !this.iKXOcc() && this.dOfFwvo(var1) != null;
   }

   public boolean ibzfxic(ServerPlayer var1, ItemStack var2) {
      if (this.iKXOcc()) {
         var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.projectile_already_attached", new Object[]{this.kbDIB5()})
               .withStyle(Style.EMPTY.withColor(16749622)));
         return false;
      }

      String var3 = this.v5suI(var2);
      if (var3 == null || !this.wuhB(var3)) {
         var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.projectile_not_supported").withStyle(Style.EMPTY.withColor(16749622)));
         return false;
      }

      if (this.xYttSy()) {
         var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.bomb_blocks_projectile").withStyle(Style.EMPTY.withColor(16749622)));
         return false;
      }

      this.v5suI(var3);
      if (!var1.getAbilities().instabuild) {
         var2.shrink(1);
      }

      var1.sendOverlayMessage(Component.translatable(jGhI2(var3)).withStyle(Style.EMPTY.withColor(16749622)));
      return true;
   }

   private static String jGhI2(String var0) {
      return "rgd5".equals(var0) ? "entity.fpvdrone.drone.rgd_attached" : "entity.fpvdrone.drone.rpg_attached";
   }

   public boolean ibzfxic(ServerPlayer var1) {
      if (!this.iKXOcc()) {
         return false;
      }

      String var2 = this.i263D9();
      this.bAcdvq9(false);
      ItemStack var3 = rsDd50tfV(var2);
      if (!var1.getInventory().add(var3)) {
         var1.drop(var3, false, net.minecraft.util.Prediction.SERVER_ONLY);
      }

      var1.sendOverlayMessage(Component.translatable(uSUb(var2)).withStyle(Style.EMPTY.withColor(16749622)));
      return true;
   }

   public Component i7EP1lKl() {
      String var1 = this.mM7DsB9AR() ? this.oEuCBB().uKpZYj().projectile() : "";
      String var2 = ProjectileDefinitions.find(var1).map(ProjectileDefinition::displayName).orElse("item.fpvdrone.40mm_explosive");
      return Component.translatable(var2);
   }

   @Nullable
   public ItemStack uSUb(Player var1) {
      if (!this.mM7DsB9AR()) {
         return null;
      }

      Item var2 = ModItems.projectileItemFor(this.oEuCBB().uKpZYj().projectile());
      return var2 == null ? null : ModItems.findInInventory(var1, var2);
   }

   public boolean jGhI2(Player var1) {
      return this.mM7DsB9AR() && !this.lFt1ckP() && !this.iKXOcc() && this.uSUb(var1) != null;
   }

   public boolean wuhB(ServerPlayer var1, ItemStack var2) {
      if (!this.mM7DsB9AR()) {
         var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.no_bomb_bay").withStyle(Style.EMPTY.withColor(16749622)));
         return false;
      }

      if (this.lFt1ckP()) {
         var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.bomb_bay_full").withStyle(Style.EMPTY.withColor(16749622)));
         return false;
      }

      if (this.iKXOcc()) {
         var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.projectile_blocks_bomb").withStyle(Style.EMPTY.withColor(16749622)));
         return false;
      }

      this.qU1QioDiC();
      if (!var1.getAbilities().instabuild) {
         var2.shrink(1);
      }

      var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.bomb_loaded").withStyle(Style.EMPTY.withColor(16749622)));
      return true;
   }

   public boolean wuhB(ServerPlayer var1) {
      int var2 = this.zQOhlhTZx();
      if (var2 < 0) {
         return false;
      }

      this.wuhB(var2);
      ItemStack var3 = rsDd50tfV(this.oEuCBB().uKpZYj().projectile());
      if (!var1.getInventory().add(var3)) {
         var1.drop(var3, false, net.minecraft.util.Prediction.SERVER_ONLY);
      }

      var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.bomb_detached").withStyle(Style.EMPTY.withColor(16749622)));
      return true;
   }

   @Nullable
   public ItemStack wEJx3Itb(Player var1) {
      Class<? extends Item> var2 = this.xogV5KBhg() ? DjiControllerItem.class : RemoteControlItem.class;
      return ModItems.findInInventory(var1, var2);
   }

   public boolean bAcdvq9(Player var1) {
      return this.wEJx3Itb(var1) != null;
   }

   public boolean dOfFwvo(ServerPlayer var1, ItemStack var2) {
      if (var2.getItem() instanceof RemoteControlItem) {
         if (this.xogV5KBhg()) {
            var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.use_dji_controller").withStyle(Style.EMPTY.withColor(16749622)));
            return false;
         }

         Optional var5 = RemoteControlItem.getLinkedDroneId(var2);
         if (var5.isPresent()) {
            DroneLinkManager.unlinkDrone((UUID)var5.get());
         }

         UUID var6 = this.getUUID();
         this.setOwner(var1.getUUID());
         this.dOfFwvo(1);
         this.assignUniqueChannel(var1.getUUID());
         RemoteControlItem.setLinkedDroneId(var2, var6);
         RemoteControlItem.setChannel(var2, this.getVideoChannel());
         DroneLinkManager.linkDrone(var6, var1.getUUID());
         var1.sendOverlayMessage(Component.translatable("item.fpvdrone.remote_control.link_success", new Object[]{this.getVideoChannelName()})
               .withStyle(Style.EMPTY.withColor(16749622)));
         return true;
      } else if (var2.getItem() instanceof DjiControllerItem) {
         if (!this.xogV5KBhg()) {
            var1.sendOverlayMessage(Component.translatable("item.fpvdrone.dji_controller.wrong_drone").withStyle(Style.EMPTY.withColor(16749622)));
            return false;
         }

         Optional var3 = DjiControllerItem.getLinkedDroneId(var2);
         if (var3.isPresent()) {
            DroneLinkManager.unlinkDrone((UUID)var3.get());
         }

         UUID var4 = this.getUUID();
         this.setOwner(var1.getUUID());
         this.dOfFwvo(1);
         this.assignUniqueChannel(var1.getUUID());
         DjiControllerItem.setLinkedDroneId(var2, var4);
         DjiControllerItem.setChannel(var2, this.getVideoChannel());
         DroneLinkManager.linkDrone(var4, var1.getUUID());
         var1.sendOverlayMessage(Component.translatable("item.fpvdrone.dji_controller.link_success", new Object[]{this.getVideoChannelName()})
               .withStyle(Style.EMPTY.withColor(16749622)));
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack c1jHYPb4(Player var1) {
      return this.xogV5KBhg() ? null : ModItems.findInInventory(var1, FpvGogglesItem.class);
   }

   public boolean v8ZN(Player var1) {
      return this.c1jHYPb4(var1) != null;
   }

   public boolean rsDd50tfV(ServerPlayer var1, ItemStack var2) {
      if (this.xogV5KBhg()) {
         var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.use_dji_controller").withStyle(Style.EMPTY.withColor(16749622)));
         return false;
      } else {
         UUID var3 = this.getUUID();
         this.setOwner(var1.getUUID());
         this.dOfFwvo(1);
         this.assignUniqueChannel(var1.getUUID());
         FpvGogglesItem.linkDroneOnChannel(var2, this.getVideoChannel(), var3);
         DroneLinkManager.linkDrone(var3, var1.getUUID());
         var1.sendOverlayMessage(Component.translatable("item.fpvdrone.fpv_goggles.link_success", new Object[]{this.getVideoChannelName()})
               .withStyle(Style.EMPTY.withColor(16749622)));
         return true;
      }
   }

   @Nullable
   public ItemStack zTT3nw6(Player var1) {
      Class<? extends Item> var2 = this.xogV5KBhg() ? DjiControllerItem.class : RemoteControlItem.class;
      UUID var3 = this.getUUID();
      Inventory var4 = var1.getInventory();

      for (int var5 = 0; var5 < var4.getContainerSize(); var5++) {
         ItemStack var6 = var4.getItem(var5);
         if (!var6.isEmpty() && var2.isInstance(var6.getItem())) {
            Optional var7 = var6.getItem() instanceof DjiControllerItem ? DjiControllerItem.getLinkedDroneId(var6) : RemoteControlItem.getLinkedDroneId(var6);
            if (var7.isPresent() && ((UUID)var7.get()).equals(var3)) {
               return var6;
            }
         }
      }

      return null;
   }

   public boolean vmpmU(Player var1) {
      return this.zTT3nw6(var1) != null;
   }

   @Nullable
   public ItemStack b69s(Player var1) {
      if (this.xogV5KBhg()) {
         return null;
      }

      UUID var2 = this.getUUID();
      int var3 = this.getVideoChannel();
      Inventory var4 = var1.getInventory();

      for (int var5 = 0; var5 < var4.getContainerSize(); var5++) {
         ItemStack var6 = var4.getItem(var5);
         if (!var6.isEmpty() && var6.getItem() instanceof FpvGogglesItem) {
            Optional var7 = FpvGogglesItem.getDroneOnChannel(var6, var3);
            if (var7.isPresent() && ((UUID)var7.get()).equals(var2)) {
               return var6;
            }
         }
      }

      return null;
   }

   public boolean cXHvq(Player var1) {
      return this.b69s(var1) != null;
   }

   public boolean dOfFwvo(ServerPlayer var1) {
      ItemStack var2 = this.zTT3nw6(var1);
      if (var2 == null) {
         return false;
      }

      if (var2.getItem() instanceof DjiControllerItem) {
         DjiControllerItem.clearLink(var2);
      } else {
         RemoteControlItem.clearLink(var2);
      }

      if (!this.cXHvq(var1)) {
         DroneLinkManager.unlinkDrone(this.getUUID());
      }

      this.level().playSound(null, this.blockPosition(), (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
      var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.remote_detached").withStyle(Style.EMPTY.withColor(16749622)));
      return true;
   }

   public boolean rsDd50tfV(ServerPlayer var1) {
      ItemStack var2 = this.b69s(var1);
      if (var2 == null) {
         return false;
      }

      int var3 = this.getVideoChannel();
      FpvGogglesItem.unlinkChannel(var2, var3);
      Map<Integer, UUID> var4 = FpvGogglesItem.getLinkedDrones(var2);
      if (var4.isEmpty()) {
         FpvGogglesItem.clearLink(var2);
      } else if (FpvGogglesItem.getChannel(var2) == var3) {
         Entry var5 = new TreeMap(var4).firstEntry();
         FpvGogglesItem.setChannel(var2, (Integer)var5.getKey());
         FpvGogglesItem.setLinkedDroneId(var2, (UUID)var5.getValue());
      }

      if (!this.vmpmU(var1)) {
         DroneLinkManager.unlinkDrone(this.getUUID());
      }

      this.level().playSound(null, this.blockPosition(), (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
      var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.goggles_detached").withStyle(Style.EMPTY.withColor(16749622)));
      return true;
   }

   protected InteractionResult mobInteract(Player var1, InteractionHand var2) {
      ItemStack var3 = var1.getItemInHand(var2);
      boolean var4 = !this.areMotorsArmed() && !this.isBeingControlled() && !this.dOfFwvo() && !this.rsDd50tfV();
      if (var1.isShiftKeyDown() && var3.isEmpty() && var4) {
         if (!this.level().isClientSide()) {
            this.pickupDrone(var1);
         }

         return InteractionResult.SUCCESS;
      } else if (var1.isShiftKeyDown() && this.iKXOcc()) {
         if (!this.level().isClientSide()) {
            this.ibzfxic((ServerPlayer)var1);
         }

         return InteractionResult.SUCCESS;
      } else if (var1.isShiftKeyDown() && this.mM7DsB9AR() && this.zQOhlhTZx() >= 0) {
         if (!this.level().isClientSide()) {
            this.wuhB((ServerPlayer)var1);
         }

         return InteractionResult.SUCCESS;
      } else if (var1.isShiftKeyDown() && this.idXga()) {
         if (this.level().isClientSide()) {
            ClientHooks.v5suI();
         } else {
            this.v5suI((ServerPlayer)var1);
         }

         return InteractionResult.SUCCESS;
      } else if (var1.isShiftKeyDown() && this.hasHdCamera()) {
         // Port addition. After the RPG, the bomb bay and the battery, so swapping batteries never pulls the camera off.
         if (!this.level().isClientSide()) {
            this.setHasHdCamera(false);
            ItemStack var7 = new ItemStack((Item)ModItems.HD_CAMERA.get());
            if (!var1.getInventory().add(var7)) {
               var1.drop(var7, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }

            var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.hd_camera_detached").withStyle(Style.EMPTY.withColor(16749622)));
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof com.fpvdrone.item.HdCameraItem) {
         // Port addition: fit the HD Digital Camera (not on thermal drones).
         if (!this.level().isClientSide()) {
            if (this.isThermal()) {
               var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.hd_camera_not_thermal").withStyle(Style.EMPTY.withColor(16749622)));
            } else if (this.hasHdCamera()) {
               var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.hd_camera_already_attached").withStyle(Style.EMPTY.withColor(16749622)));
            } else {
               this.setHasHdCamera(true);
               if (!var1.getAbilities().instabuild) {
                  var3.shrink(1);
               }

               var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.hd_camera_attached").withStyle(Style.EMPTY.withColor(16749622)));
            }
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof RemoteControlItem) {
         if (!this.level().isClientSide()) {
            this.dOfFwvo((ServerPlayer)var1, var3);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof FpvGogglesItem) {
         if (!this.level().isClientSide()) {
            this.rsDd50tfV((ServerPlayer)var1, var3);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof DjiControllerItem) {
         if (!this.level().isClientSide()) {
            this.dOfFwvo((ServerPlayer)var1, var3);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof Rpg7Item) {
         if (!this.level().isClientSide()) {
            this.ibzfxic((ServerPlayer)var1, var3);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof Rgd5Item) {
         if (!this.level().isClientSide()) {
            this.ibzfxic((ServerPlayer)var1, var3);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof BatteryItem) {
         if (this.level().isClientSide()) {
            ClientHooks.v5suI();
         } else {
            this.v5suI((ServerPlayer)var1, var3);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.is((Item)ModItems.DEBUG_INFINITE_BOMB.get())) {
         if (!this.mM7DsB9AR()) {
            if (!this.level().isClientSide()) {
               var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.no_bomb_bay").withStyle(Style.EMPTY.withColor(16749622)));
            }
         } else {
            this.setSignalLossFall(true);
            if (!this.level().isClientSide()) {
               this.fvsAOg();
               var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.debug_bomb_bay_filled").withStyle(Style.EMPTY.withColor(16733525)));
            }
         }

         return InteractionResult.SUCCESS;
      } else {
         Item var5 = this.mM7DsB9AR() ? ModItems.projectileItemFor(this.oEuCBB().uKpZYj().projectile()) : null;
         if (var3.is(var5 != null ? var5 : (Item)ModItems.EXPLOSIVE_40MM.get())) {
            if (!this.level().isClientSide()) {
               this.wuhB((ServerPlayer)var1, var3);
            }

            return InteractionResult.SUCCESS;
         } else {
            boolean var6 = !this.areMotorsArmed() && !this.isBeingControlled() && !this.dOfFwvo() && !this.rsDd50tfV();
            if (!var6) {
               if (!this.level().isClientSide()) {
                  var1.sendOverlayMessage(Component.translatable("entity.fpvdrone.drone.disarm_before_pickup").withStyle(Style.EMPTY.withColor(16749622)));
               }

               return InteractionResult.SUCCESS;
            } else {
               if (!this.level().isClientSide()) {
                  this.pickupDrone(var1);
               }

               return InteractionResult.SUCCESS;
            }
         }
      }
   }

   protected EntityDimensions getDefaultDimensions(Pose var1) {
      DroneDefinition.Hitbox var2 = this.oEuCBB().lfyfLwuw();
      return EntityDimensions.scalable(var2.width(), var2.height());
   }

   public AABB dReHaJ() {
      DroneDefinition.DamageBox var1 = this.oEuCBB().iS1JnZov();
      double var2 = var1.model();
      double var4 = var1.enabled();
      double var6 = this.getX();
      double var8 = this.getY() + this.getBbHeight() * 0.5;
      double var10 = this.getZ();
      return new AABB(var6 - var2, var8 - var4, var10 - var2, var6 + var2, var8 + var4, var10 + var2);
   }

   private boolean v5suI(DamageSource var1) {
      AABB var2 = this.dReHaJ();
      Entity var3 = var1.getDirectEntity();
      Entity var4 = var1.getEntity();
      if (var1.is(DamageTypeTags.IS_EXPLOSION)) {
         return false;
      } else if (IVDamageHelper.isIVDamageSource(var1)) {
         return false;
      } else if (var3 != null && var3 != var4) {
         Vec3 var9 = var3.position();
         Vec3 var10 = var9.add(var3.getDeltaMovement());
         return var2.clip(var9, var10).isEmpty() && !var2.inflate(0.05).contains(var3.position());
      } else if (var4 != null) {
         Vec3 var5 = var4.getEyePosition();
         double var6 = var5.distanceTo(this.position()) + 2.0;
         Vec3 var8 = var5.add(var4.getLookAngle().scale(var6));
         return var2.clip(var5, var8).isEmpty();
      } else {
         return false;
      }
   }

   private boolean ibzfxic(DamageSource var1) {
      return var1.is(DamageTypeTags.IS_EXPLOSION) ? false : IVDamageHelper.isIVDamageSource(var1) || var1.getDirectEntity() != null || var1.getEntity() != null;
   }

   public boolean hurtServer(ServerLevel var24, DamageSource var1, float var2) {
      var2 = this.oEuCBB().v5suI(var1, var2);
      boolean var3 = !this.level().isClientSide() && this.oEuCBB().wuhB();
      boolean var4 = !this.level().isClientSide() && this.v5suI(var1);
      if (var4 && !var3) {
         return false;
      }

      if (this.isInvulnerable()) {
         this.setPermanentlyInvulnerable(false);
      }

      if (this.rsDd50tfV()) {
         return false;
      }

      this.setInvulnerableTime(0);
      this.lastHurt = 0.0F;
      if (!this.level().isClientSide() && this.iKXOcc()) {
         this.jfdtPQG();
         return false;
      }

      boolean var5 = !this.level().isClientSide() && this.getHealth() - var2 <= 0.0F;
      if (var5) {
         this.setHealth(var2 + 1.0F);
      }

      boolean var6 = !this.level().isClientSide() && this.ibzfxic(var1);
      boolean var7 = var6 && this.oEuCBB().ibzfxic();
      boolean var9 = false;
      boolean var8;
      if (this.level().isClientSide()) {
         var8 = false;
      } else if (var3) {
         DroneEntity.KKeMvpuBgEY var10 = this.v5suI(this.oEuCBB().qU1QioDiC(), var4, var2, var1);
         var8 = var10 != DroneEntity.KKeMvpuBgEY.v5suI;
         var9 = var10 == DroneEntity.KKeMvpuBgEY.wuhB;
      } else if (var7) {
         this.kNWIu++;
         var8 = this.kNWIu >= this.oEuCBB().lFt1ckP();
      } else {
         var8 = var5;
      }

      boolean var25 = super.hurtServer(var24, var1, var2);
      if (var25 && !this.level().isClientSide() && !var8 && !var3) {
         Vec3 var11 = var1.getSourcePosition();
         if (var11 != null) {
            Vec3 var12 = this.position().subtract(var11).normalize();
            float var13 = Math.min(var2 * 0.4F, 2.0F);
            Vec3 var14 = var12.scale(var13 / 20.0);
            this.setDeltaMovement(this.getDeltaMovement().add(var14));
            float var15 = (float)Math.toRadians(this.getYRot());
            float var16 = (float)(var12.x * Math.cos(var15) + var12.z * Math.sin(var15)) * var2 * 25.0F;
            float var17 = (float)var12.y * var2 * -20.0F;
            float var18 = (float)(-var12.x * Math.sin(var15) + var12.z * Math.cos(var15)) * var2 * 22.0F;
            this.setYRot(this.getYRot() + var16 * 0.05F);
            this.setXRot(this.getXRot() + var17 * 0.05F);
            this.setDroneRoll(this.getDroneRoll() + var18 * 0.05F);
            UUID var19 = this.getUUID();
            UUID var20 = FPVMod.findPilotForDrone(var19);
            if (var20 != null) {
               MinecraftServer var21 = ServerHolder.server();
               if (var21 != null) {
                  ServerPlayer var22 = var21.getPlayerList().getPlayer(var20);
                  if (var22 != null) {
                     Vector3f var23 = new Vector3f((float)(var12.x * var13), (float)(var12.y * var13), (float)(var12.z * var13));
                     ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var22), new DroneHitKickPacket(var19, var23, var16, var17, var18));
                  }
               }
            }
         }
      }

      if (var25 && !this.level().isClientSide() && var8) {
         if (var9) {
            this.mWzvAips();
            return var25;
         }

         this.setHealth(1.0F);
         this.deathTime = 0;
         this.dead = false;
         if (var1.is(DamageTypeTags.IS_PROJECTILE) || var1.is(DamageTypes.PLAYER_ATTACK) || var1.is(DamageTypes.MOB_ATTACK)) {
            this.dead = true;
         }

         Vec3 var26 = this.getDeltaMovement();
         Vector3f var27 = PhysicsUnits.toPhysicsVelocity(var26);
         float var28 = var27.length();
         Vec3 var29 = var1.getSourcePosition();
         boolean var30 = false;
         if (var29 != null) {
            var30 = var29.y > this.getY() + this.getBbHeight();
         }

         FallType var31 = FallType.determine(var1, var2, var28, var30);
         float[] var32 = var31.randomizeAngularVelocity(new Random(this.getRandom().nextLong()));
         Vec3 var33 = this.getDeltaMovement();
         Vector3f var34 = PhysicsUnits.toPhysicsVelocity(var33);
         if (!var3) {
            if (var29 != null) {
               Vec3 var35 = this.position().subtract(var29).normalize();
               var34.add((float)var35.x * 2.5F, (float)var35.y * 2.5F, (float)var35.z * 2.5F);
            } else {
               var34.add(0.0F, -2.0F, 0.0F);
            }
         }

         this.enterFallingState(var31, var32);
         this.setBeingControlled(false);
         this.v5suI(var31, var34, var32);
      }

      return var25;
   }

   private DroneEntity.KKeMvpuBgEY v5suI(DroneDefinition.DamageZones var1, boolean var2, float var3, DamageSource var4) {
      if (this.wTo0I < 0) {
         this.wTo0I = var1.type().hp();
      }

      if (this.hXya < 0) {
         this.hXya = var1.entity().hp();
      }

      float var5 = var3;
      boolean var6 = var2;
      IvHitInfo.KKeMvpuBgEY var7 = IvHitInfo.v5suI(this.getId());
      if (var7 != null) {
         if (var7.dOfFwvo() != null) {
            Vec3 var8 = var7.dOfFwvo();
            var6 = !this.dReHaJ().contains(var8.x, var8.y, var8.z);
         }

         Optional var9 = WeaponDamageRules.v5suI(var7);
         if (var9.isPresent()) {
            var5 = ((WeaponDamageRule)var9.get()).ibzfxic(var7);
         }
      }

      if (var6) {
         this.hXya = this.hXya - Math.round(var5);
         this.v5suI(var4, "PERIPHERAL", var5, this.hXya, var1.entity().hp());
         return this.hXya <= 0 ? DroneEntity.KKeMvpuBgEY.ibzfxic : DroneEntity.KKeMvpuBgEY.v5suI;
      } else {
         this.wTo0I = this.wTo0I - Math.round(var5);
         this.v5suI(var4, "CORE", var5, this.wTo0I, var1.type().hp());
         if (this.wTo0I > 0) {
            return DroneEntity.KKeMvpuBgEY.v5suI;
         } else {
            return this.random.nextFloat() < var1.type().explodeChance() ? DroneEntity.KKeMvpuBgEY.wuhB : DroneEntity.KKeMvpuBgEY.ibzfxic;
         }
      }
   }

   private void v5suI(DamageSource var1, String var2, float var3, int var4, int var5) {
   }

   @org.jetbrains.annotations.Nullable
   private static ServerPlayer wuhB(DamageSource var0) {
      if (var0.getEntity() instanceof ServerPlayer var3) {
         return var3;
      } else {
         return var0.getDirectEntity() instanceof ServerPlayer var1 ? var1 : null;
      }
   }

   private void mWzvAips() {
      if (!this.level().isClientSide() && !this.wogVpI) {
         this.wogVpI = true;
         ServerLevel var1 = (ServerLevel)this.level();
         Vec3 var2 = this.position();
         String var3 = this.oEuCBB().qU1QioDiC().damage();
         UUID var4 = this.getUUID();
         UUID var5 = FPVMod.findPilotForDrone(var4);
         UUID var6 = this.rYlh ? var5 : null;
         if (var3 != null && !var3.isEmpty()) {
            ProjectileDefinition var7 = ProjectileDefinitions.getOrDefault(var3);
            float var8 = var7.explosionPower();
            int var9 = var7.fragmentCount();
            var7.detonator().detonate(var1, var2, var8, var9, this, true, var6, var7.stats(), var7.id(), false);
            ModNetwork.sendNear(var1, var2.x, var2.y, var2.z, ParticleSendRange.get(), new ProjectileBurstPacket(var7.id(), var2, this.jm7PbrhC()));
         } else {
            var1.explode(this, null, null, var2.x, var2.y, var2.z, 1.6F, false, ExplosionInteraction.MOB);
         }

         if (var5 != null) {
            MinecraftServer var10 = ServerHolder.server();
            if (var10 != null) {
               ServerPlayer var11 = var10.getPlayerList().getPlayer(var5);
               if (var11 != null) {
                  ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var11), new DroneDestroyedPacket(var4));
               }
            }
         }

         this.setBeingControlled(false);
         this.uSUb(false);
         FPVMod.onDroneDestroyedServer(var4);
         this.ibzfxic();
         this.discard();
      }
   }

   private void v5suI(FallType var1, Vector3f var2, float[] var3) {
      UUID var4 = this.getUUID();
      UUID var5 = FPVMod.findPilotForDrone(var4);
      if (var5 != null) {
         MinecraftServer var6 = ServerHolder.server();
         if (var6 != null) {
            ServerPlayer var7 = var6.getPlayerList().getPlayer(var5);
            if (var7 != null) {
               ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var7), new DroneDamagePacket(var4, var1, var2, var3));
            }
         }
      }
   }

   public void crashFromImpact(float var1, float var2, float var3, float var4) {
      if (!this.level().isClientSide()) {
         if (!this.rsDd50tfV() && !this.wogVpI && !this.iKXOcc()) {
            if (!(var1 < 12.0F)) {
               this.setHealth(1.0F);
               this.deathTime = 0;
               this.dead = false;
               this.dead = true;
               FallType var5 = var1 >= 18.0F ? FallType.TUMBLE : FallType.SPIN_OUT;
               float[] var6 = var5.randomizeAngularVelocity(new Random(this.getRandom().nextLong()));
               Vector3f var7 = PhysicsUnits.toPhysicsVelocity(this.getDeltaMovement());
               var7.add(var2 * 2.0F, var3 * 2.0F - 2.0F, var4 * 2.0F);
               this.enterFallingState(var5, var6);
               this.setBeingControlled(false);
               this.v5suI(var5, var7, var6);
            }
         }
      }
   }

   /** Client-side filter for server position updates (1.1.8 lerpTo override), called from EntityInterpolationMixin. */
   public boolean acceptServerPosition(@Nullable PositionPath var1) {
      if (this.isBeingControlled() && this.level().isClientSide()) {
         if (this.b69s() == SmoothingMode.LOCAL_PILOT) {
            return false;
         }

         if (this.tFKQMgw.v5suI()) {
            return false;
         }
      }

      if (this.isBeingControlled()) {
         return true;
      }

      return var1 == null || this.position().distanceToSqr(var1.endPosition()) > 1.0;
   }

   public void travel(Vec3 var1) {
   }

   protected float tickHeadTurn(float var1, float var2) {
      return var2;
   }

   public Vec3 jm7PbrhC() {
      Vec3 var1 = this.getDeltaMovement();
      if (var1.lengthSqr() < 1.0E-6) {
         var1 = this.getLookAngle();
      }

      if (var1.lengthSqr() < 1.0E-6) {
         return new Vec3(0.0, 1.0, 0.0);
      } else {
         var1 = var1.normalize();
         Vec3 var2 = this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
         Vec3 var3 = var2.add(var1.scale(1.5));
         BlockHitResult var4 = this.level().clip(new ClipContext(var2, var3, Block.COLLIDER, Fluid.NONE, this));
         if (var4.getType() == Type.BLOCK) {
            Vec3i var5 = var4.getDirection().getUnitVec3i();
            return new Vec3(var5.getX(), var5.getY(), var5.getZ());
         } else {
            return var1.scale(-1.0);
         }
      }
   }

   public void jfdtPQG() {
      if (!this.level().isClientSide()) {
         if (!this.wogVpI) {
            this.wogVpI = true;
            ServerLevel var1 = (ServerLevel)this.level();
            Vec3 var2 = this.position();
            ProjectileDefinition var3 = ProjectileDefinitions.getOrDefault(this.i263D9());
            float var4 = var3.explosionPower();
            int var5 = var3.fragmentCount();
            UUID var6 = this.getUUID();
            UUID var7 = FPVMod.findPilotForDrone(var6);
            UUID var8 = this.rYlh ? var7 : null;
            var3.detonator().detonate(var1, var2, var4, var5, this, true, var8, var3.stats(), var3.id(), false);
            ModNetwork.sendNear(var1, var2.x, var2.y, var2.z, ParticleSendRange.get(), new ProjectileBurstPacket(this.i263D9(), var2, this.jm7PbrhC()));
            if (var7 != null) {
               MinecraftServer var9 = ServerHolder.server();
               if (var9 != null) {
                  ServerPlayer var10 = var9.getPlayerList().getPlayer(var7);
                  if (var10 != null) {
                     ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var10), new DroneDestroyedPacket(var6));
                  }
               }
            }

            this.setBeingControlled(false);
            this.uSUb(false);
            FPVMod.onDroneDestroyedServer(var6);
            this.ibzfxic();
            this.discard();
         }
      }
   }

   private void handleGroundImpact(float var1) {
      if (!this.level().isClientSide()) {
         if (this.iKXOcc()) {
            this.jfdtPQG();
         } else if (!this.wogVpI) {
            this.wogVpI = true;
            if (this.dead) {
               this.dropAsItem();
            } else {
               ServerLevel var2 = (ServerLevel)this.level();
               Vec3 var3 = this.position();
               UUID var4 = this.getUUID();
               UUID var5 = FPVMod.findPilotForDrone(var4);
               UUID var6 = this.rYlh ? var5 : null;
               if (var1 >= 20.0F) {
                  ShrapnelExplosion.detonate(var2, var3, 4.0F, 40, this, true, var6);
                  var2.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
               } else if (var1 > 10.0F) {
                  ShrapnelExplosion.detonate(var2, var3, 2.5F, 20, this, true, var6);
               } else if (var1 >= 3.0F) {
                  var2.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
                  var2.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 5, 0.2, 0.2, 0.2, 0.01);
               }

               if (var5 != null) {
                  MinecraftServer var7 = ServerHolder.server();
                  if (var7 != null) {
                     ServerPlayer var8 = var7.getPlayerList().getPlayer(var5);
                     if (var8 != null) {
                        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var8), new DroneDestroyedPacket(var4));
                     }
                  }
               }

               FPVMod.onDroneDestroyedServer(var4);
               this.ibzfxic();
               this.discard();
            }
         }
      }
   }

   public void dropAsItem() {
      if (!this.level().isClientSide()) {
         UUID var1 = this.getUUID();
         UUID var2 = FPVMod.findPilotForDrone(var1);
         if (var2 != null) {
            MinecraftServer var3 = ServerHolder.server();
            if (var3 != null) {
               ServerPlayer var4 = var3.getPlayerList().getPlayer(var2);
               if (var4 != null) {
                  ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var4), new DroneDestroyedPacket(var1));
               }
            }
         }

         FPVMod.onDroneDestroyedServer(var1);
         ItemEntity var5 = this.spawnAtLocation((ServerLevel)this.level(), this.toItemStack());
         if (var5 != null) {
            if (this.oEuCBB().v5suI()) {
               var5.setUnlimitedLifetime();
            }
            // 1.1.8 sets Forge's ItemEntity.lifespan to ItemLifetimeSeconds; every bundled drone uses 300 s, which is
            // vanilla's own 6000-tick despawn. Other values would need an ItemEntity accessor.
         }

         this.ibzfxic();
         this.discard();
      }
   }

   public boolean rxYnaa(Player var1) {
      if (!this.areMotorsArmed() && !this.isBeingControlled() && !this.dOfFwvo() && !this.rsDd50tfV()) {
         this.pickupDrone(var1);
         return true;
      } else {
         return false;
      }
   }

   private void pickupDrone(Player var1) {
      UUID var2 = this.getUUID();
      UUID var3 = FPVMod.findPilotForDrone(var2);
      if (var3 != null) {
         MinecraftServer var4 = ServerHolder.server();
         if (var4 != null) {
            ServerPlayer var5 = var4.getPlayerList().getPlayer(var3);
            if (var5 != null) {
               ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var5), new DroneDestroyedPacket(var2));
            }
         }
      }

      FPVMod.onDroneDestroyedServer(var2);
      ItemStack var6 = this.toItemStack();
      if (!var1.getInventory().add(var6)) {
         var1.drop(var6, false, net.minecraft.util.Prediction.SERVER_ONLY);
      }

      this.unlinkAllRemotes(var1);
      this.ibzfxic();
      this.discard();
   }

   private void unlinkAllRemotes(Player var1) {
      UUID var2 = this.getUUID();

      for (int var3 = 0; var3 < var1.getInventory().getContainerSize(); var3++) {
         ItemStack var4 = var1.getInventory().getItem(var3);
         if (var4.getItem() instanceof RemoteControlItem) {
            Optional var5 = RemoteControlItem.getLinkedDroneId(var4);
            if (var5.isPresent() && ((UUID)var5.get()).equals(var2)) {
               RemoteControlItem.clearLink(var4);
            }
         }

         if (var4.getItem() instanceof FpvGogglesItem) {
            Optional var6 = FpvGogglesItem.getLinkedDroneId(var4);
            if (var6.isPresent() && ((UUID)var6.get()).equals(var2)) {
               FpvGogglesItem.clearLink(var4);
            }
         }

         if (var4.getItem() instanceof DjiControllerItem) {
            Optional var7 = DjiControllerItem.getLinkedDroneId(var4);
            if (var7.isPresent() && ((UUID)var7.get()).equals(var2)) {
               DjiControllerItem.clearLink(var4);
            }
         }
      }

      DroneLinkManager.unlinkDrone(var2);
   }

   protected void dropAllDeathLoot(DamageSource var1) {
   }

   private enum KKeMvpuBgEY {
      v5suI,
      ibzfxic,
      wuhB;
   }

   public boolean hasHdCamera() {
      return (Boolean)this.entityData.get(DATA_HAS_HD_CAMERA);
   }

   public void setHasHdCamera(boolean value) {
      this.entityData.set(DATA_HAS_HD_CAMERA, value);
   }

   // Port bridge: the names the rest of the 26.3 port (ported from 1.1.4) still calls. Each forwards to the 1.1.8
   // member; remove them as the callers are ported to 1.1.8.
   public boolean hasBattery() {
      return this.idXga();
   }

   public boolean hasRpg() {
      return this.iKXOcc();
   }

   public void setHasRpg(boolean var1) {
      this.bAcdvq9(var1);
   }

   public void setMotorsArmed(boolean var1) {
      this.uSUb(var1);
   }

   public void setInFpv(boolean var1) {
      this.entityData.set(DATA_IN_FPV, var1);
   }

   public void detonateRpg() {
      this.jfdtPQG();
   }

   public float getSyncedThrottle() {
      return this.hXya();
   }

   public float getTotalWeight() {
      return this.qz71Ga();
   }

   public void releaseChunkLoading() {
      this.ibzfxic();
   }

   public float getDroneRoll(float var1) {
      return this.ibzfxic(var1);
   }

   public double getKirpichikiAnchorX() {
      return this.tpzIHm();
   }

   public double getKirpichikiAnchorY() {
      return this.eJDY();
   }

   public double getKirpichikiAnchorZ() {
      return this.zHBAQgZP();
   }
}
