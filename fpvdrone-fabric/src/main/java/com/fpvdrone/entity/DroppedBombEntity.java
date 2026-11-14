package com.fpvdrone.entity;

import com.fpvdrone.client.PredictedBombs;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.network.BombDeflectPacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.ProjectileBurstPacket;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.registry.ModEntities;
import com.fpvdrone.sync.FixedStepClock;
import com.fpvdrone.sync.ObserverRange;
import com.fpvdrone.sync.RemoteSmoothedSimple;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.PositionAndRotation;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8's dropped bomb (entity "dropped_bomb", 0.35 blocks): a grenade or bomblet released from a drone, either the single
 * payload hung on payload_mount (RGD-5, 40 mm, TM-62...) or one station of a bomb bay.
 * <ul>
 *   <li>Falls with gravity (0.0245 blocks/tick^2) and quadratic air drag from the projectile's "Drop" block (mass, Cd,
 *       cross-section; air density 1.225), and weathervanes its nose into the airflow (AlignStiffness, AngularDamping).</li>
 *   <li>Impact-fuzed bombs arm after the projectile's Fuze delay and once they are 1.25 blocks from the release point, then
 *       go off on any block or entity contact (a direct entity hit also deals 0.5 "thrown" damage first).</li>
 *   <li>Grenades (a "Grenade" block, e.g. the RGD-5) instead bounce (Restitution, MinBounceSpeed, GroundFriction), ricochet
 *       off entity hitboxes (EntityRestitution), tumble as a full quaternion and go off when their FuseTicks run out.</li>
 *   <li>"Deflect": a player can bat a falling bomb away by hitting it (any item, or the RequiredTool); the hitter becomes
 *       the owner and the bomb stays disarmed against them for ArmDelayAfterDeflect ticks.</li>
 *   <li>Detonation uses the projectile's Detonator (shrapnel) and sends a ProjectileBurstPacket for the effects.</li>
 *   <li>The pilot's client predicts the drop (see {@link PredictedBombs}); other clients blend the bomb out of the spot
 *       where it hung on the drone (see the renderer). On clients the fall is also stepped every frame at 240 Hz.</li>
 * </ul>
 * Port notes: Forge's IEntityAdditionalSpawnData is replaced by a synced tag holding the same bytes (read once, as soon as
 * the client gets it, right after the spawn packet); the lerpTo filter became an InterpolationHandler. The unused GeckoLib
 * GeoEntity (no controllers) was dropped: the renderer draws a Bedrock model directly.
 */
public class DroppedBombEntity extends Entity implements ObserverRange, RemoteSmoothedSimple {
   /** Replaces Forge's spawn data: the bytes 1.1.8 wrote in writeSpawnData, read once on the client. */
   private static final EntityDataAccessor<CompoundTag> DATA_SPAWN = SynchedEntityData.defineId(DroppedBombEntity.class, FpvDataSerializers.COMPOUND_TAG);
   private boolean spawnDataApplied = false;
   private static final double dOfFwvo = 0.0245;
   private static final double rsDd50tfV = 1.225;
   private static final double uSUb = 0.03;
   private static final double jGhI2 = 0.03;
   private static final int wEJx3Itb = 600;
   private static final double bAcdvq9 = 1.25;
   private static final double c1jHYPb4 = 0.6;
   private static final double v8ZN = 0.45;
   private static final double zTT3nw6 = 0.4;
   private static final double vmpmU = 0.16;
   private static final float b69s = 0.4F;
   private static final float cXHvq = 0.5F;
   public static final float v5suI = 0.07F;
   private String rxYnaa = "40mm_explosive";
   private Vec3 r6s7FL3 = new Vec3(0.0, 0.0, -1.0);
   private Vec3 iAKNPAj = Vec3.ZERO;
   private Vec3 qlv6NKWnH = new Vec3(0.0, 1.0, 0.0);
   private final Quaternionf hOKMYHOaC = new Quaternionf();
   private final Quaternionf iS1JnZov = new Quaternionf();
   private float lfyfLwuw = 0.0F;
   private float wyNDWDB = 0.0F;
   private float iRWc2Pi = 0.0F;
   private float tpzIHm = 1.0F;
   @Nullable
   private UUID eJDY;
   private int zHBAQgZP = -1;
   private Vec3 we1GYY = Vec3.ZERO;
   private boolean pxmzA = false;
   private int iElAbEZe = 0;
   private long sTrE = Long.MIN_VALUE;
   private long ifpFOCaAs = Long.MIN_VALUE;
   private static final int uQdlyiwB = 5;
   private int ivDR = -1;
   public int ibzfxic = Integer.MIN_VALUE;
   private static final int n9lX8A = 6;
   private int wBXn = 0;
   private double mM7DsB9AR;
   private double uKpZYj;
   private double l2IWOAGO;
   private final Quaternionf xYttSy = new Quaternionf();
   private boolean kNoG = false;
   private boolean fvsAOg = false;
   private boolean lFt1ckP = false;
   private boolean qU1QioDiC = false;
   private int zQOhlhTZx = 0;
   private long lvk8rWUGD = Long.MIN_VALUE;
   private boolean pyHY5 = false;
   private Vec3 idXga = new Vec3(0.0, 0.0, -1.0);
   private final FixedStepClock i263D9 = new FixedStepClock();
   private boolean rOIp0p7 = false;
   private boolean iKXOcc = false;
   private static final int jt9oI60ST = 60;
   private boolean oEuCBB = false;
   @Nullable
   private DroppedBombEntity uj1PcjLg;
   @Nullable
   private DroppedBombEntity zgQgCA9;

   public void v5suI() {
      this.rOIp0p7 = true;
   }

   public boolean ibzfxic() {
      boolean var1 = this.rOIp0p7;
      this.rOIp0p7 = false;
      return var1;
   }

   @Nullable
   public DroppedBombEntity wuhB() {
      return this.uj1PcjLg != null && !this.uj1PcjLg.isRemoved() && !this.uj1PcjLg.pxmzA && this.level().getEntity(this.uj1PcjLg.getId()) == this.uj1PcjLg
         ? this.uj1PcjLg
         : null;
   }

   public void remove(RemovalReason var1) {
      if (this.level().isClientSide() && this.uj1PcjLg != null && !this.uj1PcjLg.isRemoved()) {
         this.uj1PcjLg.discard();
      }

      if (this.level() instanceof ServerLevel var2) {
         LookaheadChunkLoader.v5suI(var2, this.getUUID());
      }

      super.remove(var1);
   }

   public DroppedBombEntity(EntityType<? extends DroppedBombEntity> var1, Level var2) {
      super(var1, var2);
      this.noPhysics = false;
      this.blocksBuilding = false;
   }

   public boolean shouldRenderAtSqrDistance(double var1) {
      return this.wuhB(var1);
   }

   /**
    * Creates a real (server) bomb; the caller adds it with addFreshEntity.
    *
    * @param var0 level
    * @param var1 release position (where the payload was drawn)
    * @param var2 initial velocity (blocks/tick; the drone's velocity at release)
    * @param var3 nose direction (the drone's look vector)
    * @param var4 projectile id ("40mm_explosive" if empty)
    * @param var5 owner (the pilot)
    * @param var6 entity id of the carrier drone, or -1
    * @param var7 grenade hang orientation x (a grenade's starting quaternion; ignored for other bombs)
    * @param var8 ... y
    * @param var9 ... z
    * @param var10 ... w
    * @param var11 bomb-bay station, or -1 for the payload_mount payload
    * @param var12 game tick of the release on the pilot's client (matches the predicted stand-in), or Long.MIN_VALUE
    */
   public static DroppedBombEntity v5suI(
      Level var0,
      Vec3 var1,
      Vec3 var2,
      Vec3 var3,
      String var4,
      @Nullable UUID var5,
      int var6,
      float var7,
      float var8,
      float var9,
      float var10,
      int var11,
      long var12
   ) {
      DroppedBombEntity var14 = new DroppedBombEntity((EntityType<? extends DroppedBombEntity>)ModEntities.DROPPED_BOMB.get(), var0);
      var14.setPos(var1.x, var1.y, var1.z);
      var14.setDeltaMovement(var2);
      Vec3 var15 = var3.lengthSqr() > 1.0E-6 ? var3.normalize() : new Vec3(0.0, 0.0, -1.0);
      var14.r6s7FL3 = var15;
      var14.idXga = var15;
      var14.rxYnaa = var4 != null && !var4.isEmpty() ? var4 : "40mm_explosive";
      var14.eJDY = var5;
      var14.zHBAQgZP = var6;
      var14.we1GYY = var1;
      var14.sTrE = var0.getGameTime();
      var14.ivDR = var11;
      var14.ifpFOCaAs = var12;
      ProjectileDefinition var16 = ProjectileDefinitions.ibzfxic(var14.rxYnaa).orElse(null);
      if (var16 != null && var16.wEJx3Itb()) {
         var14.iAKNPAj = ibzfxic(var2);
         var14.lfyfLwuw = var7;
         var14.wyNDWDB = var8;
         var14.iRWc2Pi = var9;
         var14.tpzIHm = var10;
         var14.qlv6NKWnH();
      }

      if (!var0.isClientSide()) {
         var14.syncSpawnData();
      }

      return var14;
   }

   /**
    * Creates the pilot's client-side predicted stand-in (no carrier id; var6 is the station, var11 the release tick).
    * Register it with {@link PredictedBombs#v5suI(DroppedBombEntity)}. It is never sent to the server, is moved only by
    * the per-frame step, vanishes on touching the ground or after 60 ticks unless the server's bomb adopts it.
    */
   public static DroppedBombEntity v5suI(
      Level var0, Vec3 var1, Vec3 var2, Vec3 var3, String var4, @Nullable UUID var5, int var6, float var7, float var8, float var9, float var10, long var11
   ) {
      DroppedBombEntity var13 = v5suI(var0, var1, var2, var3, var4, var5, -1, var7, var8, var9, var10, var6, var11);
      var13.iKXOcc = true;
      var13.setOldPosAndRot();
      return var13;
   }

   private void qlv6NKWnH() {
      this.hOKMYHOaC.set(this.lfyfLwuw, this.wyNDWDB, this.iRWc2Pi, this.tpzIHm);
      if (this.hOKMYHOaC.lengthSquared() < 1.0E-6F) {
         this.hOKMYHOaC.identity();
      } else {
         this.hOKMYHOaC.normalize();
      }

      this.iS1JnZov.set(this.hOKMYHOaC);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(DATA_SPAWN, new CompoundTag());
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
      super.onSyncedDataUpdated(accessor);
      if (DATA_SPAWN.equals(accessor) && this.level().isClientSide() && !this.spawnDataApplied) {
         byte[] bytes = this.entityData.get(DATA_SPAWN).getByteArray("D").orElse(null);
         if (bytes != null && bytes.length > 0) {
            this.spawnDataApplied = true;
            this.readSpawnData(new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes)));
         }
      }
   }

   /** Server: (re)publish the spawn data for clients that start tracking this bomb. */
   private void syncSpawnData() {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      this.writeSpawnData(buf);
      byte[] bytes = new byte[buf.readableBytes()];
      buf.readBytes(bytes);
      CompoundTag tag = new CompoundTag();
      tag.putByteArray("D", bytes);
      this.entityData.set(DATA_SPAWN, tag);
   }

   public String dOfFwvo() {
      return this.rxYnaa;
   }

   public ProjectileDefinition rsDd50tfV() {
      return ProjectileDefinitions.ibzfxic(this.rxYnaa).orElseGet(() -> ProjectileDefinitions.wuhB("40mm_explosive"));
   }

   public Vec3 v5suI(float var1) {
      Vec3 var2 = this.idXga;
      Vec3 var3 = this.r6s7FL3;
      Vec3 var4 = new Vec3(
         var2.x + (var3.x - var2.x) * var1,
         var2.y + (var3.y - var2.y) * var1,
         var2.z + (var3.z - var2.z) * var1
      );
      return var4.lengthSqr() > 1.0E-6 ? var4.normalize() : new Vec3(0.0, 0.0, -1.0);
   }

   public void tick() {
      this.xOld = this.getX();
      this.yOld = this.getY();
      this.zOld = this.getZ();
      this.idXga = this.r6s7FL3;
      this.iS1JnZov.set(this.hOKMYHOaC);
      this.lvk8rWUGD = this.level().getGameTime();
      if (this.level().isClientSide()) {
         this.pyHY5 = true;
      }

      if (this.level().isClientSide() && this.wBXn > 0) {
         this.wBXn--;
      }

      super.tick();
      if (!this.pxmzA) {
         if (this.level() instanceof ServerLevel var1) {
            LookaheadChunkLoader.v5suI(var1, this.getUUID(), this.position(), this.getDeltaMovement(), this.rsDd50tfV().vmpmU().v5suI());
         }

         if (this.iKXOcc) {
            ProjectileDefinition var9 = this.rsDd50tfV();
            ProjectileDefinition.Drop var11 = var9.pxmzA();
            if (var11 != null && !var9.wEJx3Itb()) {
               this.ibzfxic(this.getDeltaMovement(), var11);
            }

            if (!this.oEuCBB && this.tickCount > 60) {
               this.discard();
            }
         } else {
            ProjectileDefinition var8 = this.rsDd50tfV();
            ProjectileDefinition.Drop var10 = var8.pxmzA();
            Vec3 var3 = v5suI(this.getDeltaMovement(), var10);
            this.setDeltaMovement(var3);
            if (var10 != null && !var8.wEJx3Itb()) {
               this.ibzfxic(var3, var10);
            }

            Vec3 var4 = this.getDeltaMovement();
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.lfyfLwuw();
            if (this.horizontalCollision || this.verticalCollision) {
               this.qlv6NKWnH = this.v5suI(var4);
            } else if (this.onGround()) {
               this.qlv6NKWnH = new Vec3(0.0, 1.0, 0.0);
            }

            ProjectileDefinition.Grenade var5 = var8.sTrE();
            if (var5 != null) {
               this.v5suI(var4, var5);
               this.iS1JnZov();
               long var6 = this.sTrE == Long.MIN_VALUE ? this.tickCount : this.level().getGameTime() - this.sTrE;
               if (this.tickCount >= var8.dOfFwvo().v5suI() && var6 >= var5.fuseTicks()) {
                  this.tpzIHm();
                  return;
               }
            } else if (this.wyNDWDB()) {
               Entity var12 = this.iRWc2Pi();
               boolean var7 = this.horizontalCollision || this.verticalCollision || this.onGround() || var12 != null;
               if (var7) {
                  if (var12 != null && !this.horizontalCollision && !this.verticalCollision && var4.lengthSqr() > 1.0E-8) {
                     this.qlv6NKWnH = var4.normalize().scale(-1.0);
                  }

                  if (var12 != null) {
                     this.v5suI(var12);
                  }

                  this.tpzIHm();
                  return;
               }
            }

            if (!this.level().isClientSide() && ++this.iElAbEZe > 600) {
               this.discard();
            }
         }
      }
   }

   private static Vec3 v5suI(Vec3 var0, ProjectileDefinition.Drop var1) {
      var0 = var0.add(0.0, -0.0245, 0.0);
      double var2 = var0.length();
      if (var1 != null && var2 > 1.0E-5) {
         double var4 = 0.6125 * var1.dragCoefficient() * var1.crossSection() / var1.mass();
         double var6 = var4 * var2 * var2;
         double var8 = Math.max(0.0, 1.0 - var6 / var2);
         var0 = var0.scale(var8);
      }

      return var0;
   }

   public void v5suI(double var1) {
      if (this.level().isClientSide() && this.iKXOcc && !this.pxmzA) {
         if (this.onGround()) {
            this.discard();
         } else {
            ProjectileDefinition.Drop var3 = this.rsDd50tfV().pxmzA();
            if (this.i263D9.v5suI(var1, var2 -> this.v5suI(var3, var2))) {
               this.setOldPosAndRot();
               this.hOKMYHOaC();
            }
         }
      }
   }

   @Override
   public void ibzfxic(double var1) {
      if (!this.ibzfxic()) {
         this.v5suI(var1);
      }
   }

   private void hOKMYHOaC() {
      DroppedBombEntity var1 = this.zgQgCA9;
      if (var1 != null && !var1.isRemoved() && !var1.pxmzA) {
         var1.setPos(this.getX(), this.getY(), this.getZ());
      }
   }

   private boolean v5suI(ProjectileDefinition.@Nullable Drop var1, double var2) {
      double var4 = var2 / 0.05;
      Vec3 var6 = this.getDeltaMovement();
      var6 = var6.add(0.0, -0.0245 * var4, 0.0);
      double var7 = var6.length();
      if (var1 != null && var7 > 1.0E-5) {
         double var9 = 0.6125 * var1.dragCoefficient() * var1.crossSection() / var1.mass();
         double var11 = var9 * var7 * var7 * var4;
         double var13 = Math.max(0.0, 1.0 - var11 / var7);
         var6 = var6.scale(var13);
      }

      this.setDeltaMovement(var6);
      this.move(MoverType.SELF, var6.scale(var4));
      if (this.onGround()) {
         this.discard();
         return false;
      } else {
         return true;
      }
   }

   public void v5suI(DroppedBombEntity var1) {
      var1.oEuCBB = true;
      boolean var2 = this.rsDd50tfV().wEJx3Itb();
      boolean var3 = !var1.pxmzA && !var1.onGround() && var1.position().distanceToSqr(this.position()) < 64.0;
      if (var3) {
         this.setPos(var1.getX(), var1.getY(), var1.getZ());
         this.setOldPosAndRot();
         this.setDeltaMovement(var1.getDeltaMovement());
         if (!var2 && var1.r6s7FL3.lengthSqr() > 1.0E-6) {
            this.r6s7FL3 = var1.r6s7FL3;
            this.idXga = var1.idXga;
         }
      }

      if (!var3) {
         var1.discard();
      } else {
         this.uj1PcjLg = var1;
         var1.zgQgCA9 = this;
      }
   }

   private void v5suI(int var1) {
      ProjectileDefinition.Drop var2 = this.rsDd50tfV().pxmzA();
      Vec3 var3 = this.getDeltaMovement();
      double var4 = this.getX();
      double var6 = this.getY();
      double var8 = this.getZ();

      for (int var10 = 0; var10 < var1; var10++) {
         var3 = v5suI(var3, var2);
         var4 += var3.x;
         var6 += var3.y;
         var8 += var3.z;
      }

      this.setDeltaMovement(var3);
      this.setPos(var4, var6, var8);
   }

   private Vec3 v5suI(Vec3 var1) {
      Vec3 var2 = this.getDeltaMovement();
      double var3 = 0.0;
      double var5 = 0.0;
      double var7 = 0.0;
      if (this.verticalCollision) {
         if (var1.y < -1.0E-4) {
            var5 = 1.0;
         } else if (var1.y > 1.0E-4) {
            var5 = -1.0;
         }
      }

      if (this.horizontalCollision) {
         if (Math.abs(var1.x) > 1.0E-4 && Math.abs(var2.x) < Math.abs(var1.x) - 1.0E-4) {
            var3 = var1.x > 0.0 ? -1.0 : 1.0;
         }

         if (Math.abs(var1.z) > 1.0E-4 && Math.abs(var2.z) < Math.abs(var1.z) - 1.0E-4) {
            var7 = var1.z > 0.0 ? -1.0 : 1.0;
         }
      }

      Vec3 var9 = new Vec3(var3, var5, var7);
      if (var9.lengthSqr() < 1.0E-8) {
         return var1.lengthSqr() > 1.0E-8 ? var1.normalize().scale(-1.0) : new Vec3(0.0, 1.0, 0.0);
      } else {
         return var9.normalize();
      }
   }

   private void v5suI(Vec3 var1, ProjectileDefinition.Grenade var2) {
      Vec3 var3 = this.getDeltaMovement();
      double var4 = var3.x;
      double var6 = var3.y;
      double var8 = var3.z;
      boolean var10 = false;
      if (this.horizontalCollision) {
         if (Math.abs(var1.x) > 1.0E-4 && Math.abs(var3.x) < Math.abs(var1.x) - 1.0E-4) {
            var4 = -var1.x * var2.restitution();
            var10 = true;
         }

         if (Math.abs(var1.z) > 1.0E-4 && Math.abs(var3.z) < Math.abs(var1.z) - 1.0E-4) {
            var8 = -var1.z * var2.restitution();
            var10 = true;
         }
      }

      if (this.verticalCollision) {
         if (var1.y < -1.0E-4) {
            double var11 = -var1.y * var2.restitution();
            if (var11 < var2.minBounceSpeed()) {
               var6 = 0.0;
            } else {
               var6 = var11;
               var10 = true;
            }
         } else if (var1.y > 1.0E-4) {
            var6 = -var1.y * var2.restitution();
         }
      }

      if (this.onGround() && var6 == 0.0) {
         var4 *= var2.groundFriction();
         var8 *= var2.groundFriction();
         if (Math.abs(var4) < 1.0E-4) {
            var4 = 0.0;
         }

         if (Math.abs(var8) < 1.0E-4) {
            var8 = 0.0;
         }
      }

      this.setDeltaMovement(var4, var6, var8);
      if (var10) {
         Vec3 var22 = var1.cross(new Vec3(0.0, 1.0, 0.0));
         if (var22.lengthSqr() > 1.0E-8) {
            this.iAKNPAj = this.iAKNPAj.add(var22.normalize().scale(var1.length() * 0.8));
         }

         this.zHBAQgZP();
      }

      Entity var23 = this.iRWc2Pi();
      if (var23 != null) {
         Vec3 var12 = this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
         Vec3 var13 = var23.getBoundingBox().getCenter();
         Vec3 var14 = var12.subtract(var13);
         var14 = var14.lengthSqr() > 1.0E-6 ? var14.normalize() : new Vec3(0.0, 1.0, 0.0);
         Vec3 var15 = this.getDeltaMovement();
         double var16 = var15.dot(var14);
         if (var16 < 0.0) {
            Vec3 var18 = var14.scale(var16);
            Vec3 var19 = var15.subtract(var18);
            Vec3 var20 = var19.scale(0.4).subtract(var18.scale(var2.entityRestitution()));
            if (var20.length() > 0.16) {
               var20 = var20.normalize().scale(0.16);
            }

            this.setDeltaMovement(var20);
            this.setPos(this.getX() + var14.x * 0.05, this.getY() + var14.y * 0.05, this.getZ() + var14.z * 0.05);
            Vec3 var21 = var19.cross(var14);
            if (var21.lengthSqr() > 1.0E-8) {
               this.iAKNPAj = this.iAKNPAj.add(var21.normalize().scale(0.6));
            }

            this.v5suI(var23);
            this.zHBAQgZP();
         }
      }
   }

   private void iS1JnZov() {
      boolean var1 = this.onGround();
      this.iAKNPAj = this.iAKNPAj.scale(var1 ? 0.4 : 0.985);
      double var2 = this.iAKNPAj.length();
      if (var2 >= 0.005) {
         Vec3 var4 = this.iAKNPAj.scale(1.0 / var2);
         Quaternionf var5 = new Quaternionf().fromAxisAngleRad((float)var4.x, (float)var4.y, (float)var4.z, (float)var2);
         var5.mul(this.hOKMYHOaC, this.hOKMYHOaC);
         this.hOKMYHOaC.normalize();
      } else {
         this.iAKNPAj = Vec3.ZERO;
      }

      float var11 = var1 ? 0.3F : 0.04F;
      Vector3f var12 = this.hOKMYHOaC.transform(new Vector3f(0.0F, 1.0F, 0.0F));
      Vector3f var6 = var12.cross(0.0F, 1.0F, 0.0F, new Vector3f());
      float var7 = var6.length();
      float var8 = var12.y;
      if (var7 < 1.0E-4F) {
         if (var8 >= 0.0F) {
            return;
         }

         var6.set(1.0F, 0.0F, 0.0F);
         var7 = 1.0F;
      }

      var6.normalize();
      float var9 = (float)Math.atan2(var7, var8) * var11;
      Quaternionf var10 = new Quaternionf().fromAxisAngleRad(var6.x, var6.y, var6.z, var9);
      var10.mul(this.hOKMYHOaC, this.hOKMYHOaC);
      this.hOKMYHOaC.normalize();
   }

   private static Vec3 ibzfxic(Vec3 var0) {
      return Vec3.ZERO;
   }

   public Quaternionf ibzfxic(float var1) {
      float var2 = Math.max(0.0F, Math.min(1.0F, var1));
      return new Quaternionf(this.iS1JnZov).slerp(this.hOKMYHOaC, var2);
   }

   private void lfyfLwuw() {
      if (!this.qU1QioDiC) {
         if (!this.onGround() && !this.horizontalCollision && !this.verticalCollision) {
            double var1 = this.getX() - this.we1GYY.x;
            double var3 = this.getY() - this.we1GYY.y;
            double var5 = this.getZ() - this.we1GYY.z;
            if (var1 * var1 + var3 * var3 + var5 * var5 > 1.5625) {
               this.qU1QioDiC = true;
            }
         } else {
            this.qU1QioDiC = true;
         }
      }
   }

   private boolean wyNDWDB() {
      int var1 = this.rsDd50tfV().dOfFwvo().v5suI();
      return this.tickCount >= var1 && this.qU1QioDiC && this.tickCount >= this.zQOhlhTZx;
   }

   private void ibzfxic(Vec3 var1, ProjectileDefinition.Drop var2) {
      double var3 = var1.length();
      Vec3 var5 = var3 > 1.0E-4 ? var1.scale(1.0 / var3) : this.r6s7FL3;
      Vec3 var6 = this.r6s7FL3.cross(var5);
      double var7 = var3 * var3;
      double var9 = var7 / (0.03 + var7);
      Vec3 var11 = var6.scale(var2.alignStiffness() * 0.03 * var9);
      this.iAKNPAj = this.iAKNPAj.add(var11);
      double var12 = Math.min(1.0, var2.angularDamping() * 0.05);
      this.iAKNPAj = this.iAKNPAj.scale(1.0 - var12);
      double var14 = this.iAKNPAj.length();
      if (var14 > 1.0E-7) {
         Vec3 var16 = this.iAKNPAj.scale(1.0 / var14);
         this.r6s7FL3 = v5suI(this.r6s7FL3, var16, var14).normalize();
      }
   }

   private static Vec3 v5suI(Vec3 var0, Vec3 var1, double var2) {
      double var4 = Math.cos(var2);
      double var6 = Math.sin(var2);
      Vec3 var8 = var1.cross(var0);
      double var9 = var1.dot(var0);
      return var0.scale(var4).add(var8.scale(var6)).add(var1.scale(var9 * (1.0 - var4)));
   }

   private void v5suI(@Nullable Entity var1) {
      if (var1 != null && !this.level().isClientSide()) {
         ServerLevel var2 = (ServerLevel)this.level();
         Entity var3 = this.eJDY != null ? var2.getEntity(this.eJDY) : null;
         DamageSource var4 = var2.damageSources().thrown(this, var3);
         var1.hurtServer(var2, var4, 0.5F);
      }
   }

   @Nullable
   private Entity iRWc2Pi() {
      Vec3 var1 = new Vec3(this.xOld, this.yOld, this.zOld).add(0.0, this.getBbHeight() * 0.5, 0.0);
      Vec3 var2 = this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
      AABB var3 = this.getBoundingBox();
      AABB var4 = var3.minmax(var3.move(var1.subtract(var2))).inflate(1.0);
      Entity var5 = null;
      double var6 = Double.MAX_VALUE;

      for (Entity var9 : this.level().getEntities(this, var4)) {
         if (var9 != this
            && (this.qU1QioDiC || this.eJDY == null || !var9.getUUID().equals(this.eJDY))
            && !(var9 instanceof DroppedBombEntity)
            && var9.isPickable()
            && !var9.isSpectator()) {
            AABB var10 = var9.getBoundingBox().inflate(0.2);
            if (var10.contains(var1)) {
               return var9;
            }

            Optional<Vec3> var11 = var10.clip(var1, var2);
            if (var11.isPresent()) {
               double var12 = var1.distanceToSqr(var11.get());
               if (var12 < var6) {
                  var6 = var12;
                  var5 = var9;
               }
            }
         }
      }

      return var5;
   }

   private void tpzIHm() {
      if (!this.pxmzA) {
         this.pxmzA = true;
         if (this.iKXOcc) {
            this.discard();
         } else if (!this.level().isClientSide()) {
            ProjectileDefinition var1 = this.rsDd50tfV();
            ServerLevel var2 = (ServerLevel)this.level();
            Entity var3 = this.eJDY != null ? var2.getEntity(this.eJDY) : null;
            Vec3 var4 = this.eJDY();
            var1.ibzfxic().detonate(var2, var4, var1.hOKMYHOaC(), var1.iS1JnZov(), var3, true, null, var1.eJDY(), var1.r6s7FL3(), true);
            ModNetwork.CHANNEL
               .send(
                  PacketDistributor.NEAR
                     .with(() -> new PacketDistributor.TargetPoint(var4.x, var4.y, var4.z, com.fpvdrone.util.ParticleSendRange.get(), var2.dimension())),
                  new ProjectileBurstPacket(this.rxYnaa, var4, this.qlv6NKWnH)
               );
            this.discard();
         }
      }
   }

   private Vec3 eJDY() {
      return this.rsDd50tfV().wEJx3Itb() ? this.position().add(0.0, 0.07F, 0.0) : this.position();
   }

   public int uSUb() {
      return this.zHBAQgZP;
   }

   @Nullable
   public UUID jGhI2() {
      return this.eJDY;
   }

   public boolean wEJx3Itb() {
      return this.lFt1ckP;
   }

   public void v5suI(double var1, double var3, double var5, @Nullable Quaternionf var7) {
      this.lFt1ckP = true;
      double var8 = var1 * var1 + var3 * var3 + var5 * var5;
      if (var8 > 64.0) {
         this.kNoG = false;
      } else if (var8 < 0.01) {
         this.kNoG = false;
      } else {
         this.mM7DsB9AR = var1;
         this.uKpZYj = var3;
         this.l2IWOAGO = var5;
         if (var7 != null) {
            this.xYttSy.set(var7);
            if (this.xYttSy.lengthSquared() < 1.0E-6F) {
               this.xYttSy.identity();
            } else {
               this.xYttSy.normalize();
            }

            this.fvsAOg = true;
         } else {
            this.fvsAOg = false;
         }

         this.wBXn = 6;
         this.kNoG = true;
      }
   }

   public boolean bAcdvq9() {
      return this.kNoG && this.wBXn > 0;
   }

   public boolean c1jHYPb4() {
      return this.kNoG && this.fvsAOg && this.wBXn > 0;
   }

   public int v8ZN() {
      return this.ivDR;
   }

   public boolean zTT3nw6() {
      return this.iKXOcc;
   }

   public long vmpmU() {
      return this.ifpFOCaAs;
   }

   public float wuhB(float var1) {
      if (!this.kNoG) {
         return 0.0F;
      }

      float var2 = (this.wBXn - var1) / 6.0F;
      var2 = var2 < 0.0F ? 0.0F : (var2 > 1.0F ? 1.0F : var2);
      return var2 * var2 * (3.0F - 2.0F * var2);
   }

   public double b69s() {
      return this.mM7DsB9AR;
   }

   public double cXHvq() {
      return this.uKpZYj;
   }

   public double rxYnaa() {
      return this.l2IWOAGO;
   }

   public Quaternionf r6s7FL3() {
      return new Quaternionf(this.xYttSy);
   }

   /**
    * 1.1.8 lerpTo: while this client is simulating the bomb (it ticked within the last 2 game ticks) server positions are
    * ignored; otherwise they are taken as is, without vanilla interpolation.
    */
   @Override
   protected InterpolationHandler createInterpolationHandler() {
      return new InterpolationHandler.NoOpInterpolationHandler() {
         @Override
         public boolean interpolateTo(@Nullable PositionPath position, float yRot, float xRot, boolean hasRotation) {
            DroppedBombEntity.this.acceptServerPosition(position);
            return true;
         }
      };
   }

   private void acceptServerPosition(@Nullable PositionPath position) {
      if (position != null && this.level().isClientSide()) {
         if (this.lvk8rWUGD != Long.MIN_VALUE) {
            if (this.level().getGameTime() - this.lvk8rWUGD > 2L) {
               this.xOld = this.getX();
               this.yOld = this.getY();
               this.zOld = this.getZ();
               this.setPos(position.endPosition());
            }
         }
      }
   }

   @Override
   public void lerpMotion(Vec3 movement) {
   }

   @Override
   public void iAKNPAj() {
      if (this.level().isClientSide()) {
         if (this.pyHY5) {
            this.pyHY5 = false;
         } else if (this.lvk8rWUGD != Long.MIN_VALUE && !this.pxmzA) {
            this.setOldPosAndRot();
            this.tickCount++;
            this.tick();
            this.pyHY5 = false;
         }
      }
   }

   public boolean isPickable() {
      return !this.pxmzA && !this.iKXOcc;
   }

   public boolean canCollideWith(Entity var1) {
      return false;
   }

   public float getPickRadius() {
      return this.pxmzA ? 0.0F : 0.4F;
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource var1, float var2) {
      if (!this.pxmzA && var1.getEntity() instanceof Player var3) {
         this.v5suI(var3);
      }

      return false;
   }

   private void v5suI(Player var1) {
      ProjectileDefinition var2 = this.rsDd50tfV();
      if (var2.jGhI2()) {
         ProjectileDefinition.Deflect var3 = var2.iElAbEZe();
         if (var3.requiredTool() != null && !var3.requiredTool().isEmpty()) {
            ItemStack var4 = var1.getMainHandItem();
            Identifier var5 = BuiltInRegistries.ITEM.getKey(var4.getItem());
            if (var5 == null || !var5.toString().equals(var3.requiredTool())) {
               return;
            }
         }

         ProjectileDefinition.Drop var20 = var2.pxmzA();
         double var21 = var20 != null ? Math.max(0.01, var20.mass()) : 0.25;
         Vec3 var7 = this.getDeltaMovement();
         Vec3 var8 = var1.getEyePosition();
         Vec3 var9 = this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
         Vec3 var10 = var9.subtract(var8);
         var10 = var10.lengthSqr() > 1.0E-6 ? var10.normalize() : var1.getLookAngle();
         Vec3 var11 = var1.getLookAngle();
         var11 = var11.lengthSqr() > 1.0E-6 ? var11.normalize() : var10;
         double var12 = var7.dot(var10);
         Vec3 var14 = var12 < 0.0 ? var7.subtract(var10.scale((1.0 + var3.restitution()) * var12)) : var7;
         double var15 = var3.maxImpulse() / var21;
         Vec3 var17 = var14.add(var11.scale(var15));
         if (var17.length() > 0.45) {
            var17 = var17.normalize().scale(0.45);
         }

         Vec3 var18 = var10.cross(var11);
         Vec3 var19 = this.iAKNPAj.add(var18.scale(var15 * 0.6));
         this.setDeltaMovement(var17);
         this.iAKNPAj = var19;
         this.eJDY = var1.getUUID();
         this.zHBAQgZP = -1;
         this.qU1QioDiC = true;
         this.zQOhlhTZx = this.tickCount + var3.armDelayAfterDeflect();
         this.level()
            .playSound(
               null,
               this.getX(),
               this.getY(),
               this.getZ(),
               SoundEvents.PLAYER_ATTACK_STRONG,
               SoundSource.PLAYERS,
               0.8F,
               1.2F + (this.random.nextFloat() - 0.5F) * 0.2F
            );
         ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new BombDeflectPacket(this.getId(), var17, this.r6s7FL3, this.iAKNPAj));
      }
   }

   public void v5suI(Vec3 var1, Vec3 var2, Vec3 var3) {
      this.setDeltaMovement(var1);
      if (var2.lengthSqr() > 1.0E-6) {
         this.r6s7FL3 = var2.normalize();
         this.idXga = this.r6s7FL3;
      }

      this.iAKNPAj = var3;
      this.zHBAQgZP = -1;
      this.qU1QioDiC = true;
      this.zHBAQgZP();
   }

   private void zHBAQgZP() {
      DroppedBombEntity var1 = this.wuhB();
      if (var1 != null) {
         var1.setPos(this.getX(), this.getY(), this.getZ());
         var1.setOldPosAndRot();
         var1.setDeltaMovement(this.getDeltaMovement());
         var1.r6s7FL3 = this.r6s7FL3;
         var1.idXga = this.r6s7FL3;
         var1.iAKNPAj = this.iAKNPAj;
      }
   }

   public void writeSpawnData(FriendlyByteBuf var1) {
      var1.writeUtf(this.rxYnaa);
      Vec3 var2 = this.getDeltaMovement();
      var1.writeDouble(var2.x);
      var1.writeDouble(var2.y);
      var1.writeDouble(var2.z);
      var1.writeDouble(this.r6s7FL3.x);
      var1.writeDouble(this.r6s7FL3.y);
      var1.writeDouble(this.r6s7FL3.z);
      boolean var3 = this.eJDY != null;
      var1.writeBoolean(var3);
      if (var3) {
         var1.writeUUID(this.eJDY);
      }

      var1.writeInt(this.zHBAQgZP);
      var1.writeFloat(this.lfyfLwuw);
      var1.writeFloat(this.wyNDWDB);
      var1.writeFloat(this.iRWc2Pi);
      var1.writeFloat(this.tpzIHm);
      var1.writeLong(this.sTrE);
      var1.writeVarInt(this.ivDR);
      var1.writeLong(this.ifpFOCaAs);
   }

   public void readSpawnData(FriendlyByteBuf var1) {
      this.rxYnaa = var1.readUtf();
      this.setDeltaMovement(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.r6s7FL3 = new Vec3(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.idXga = this.r6s7FL3;
      this.eJDY = var1.readBoolean() ? var1.readUUID() : null;
      this.zHBAQgZP = var1.readInt();
      this.lfyfLwuw = var1.readFloat();
      this.wyNDWDB = var1.readFloat();
      this.iRWc2Pi = var1.readFloat();
      this.tpzIHm = var1.readFloat();
      this.sTrE = var1.readLong();
      this.ivDR = var1.readVarInt();
      this.ifpFOCaAs = var1.readLong();
      this.we1GYY = this.position();
      long var2 = this.ifpFOCaAs != Long.MIN_VALUE ? this.ifpFOCaAs : this.sTrE;
      if (var2 != Long.MIN_VALUE) {
         long var4 = this.level().getGameTime() - var2;
         if (var4 >= 1L && var4 <= 5L) {
            this.v5suI((int)var4);
         }
      }

      this.setOldPosAndRot();
      if (this.rsDd50tfV().wEJx3Itb()) {
         this.iAKNPAj = ibzfxic(this.getDeltaMovement());
         this.qlv6NKWnH();
      }

      if (this.level().isClientSide()) {
         PredictedBombs.wuhB(this);
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput var1) {
      var1.getString("ProjectileId").ifPresent(var1x -> this.rxYnaa = var1x);
      var1.read("Owner", UUIDUtil.CODEC).ifPresent(var1x -> this.eJDY = var1x);
      if (var1.read("NoseX", com.mojang.serialization.Codec.DOUBLE).isPresent()) {
         this.r6s7FL3 = new Vec3(var1.getDoubleOr("NoseX", 0.0), var1.getDoubleOr("NoseY", 0.0), var1.getDoubleOr("NoseZ", 0.0));
         this.idXga = this.r6s7FL3;
      }

      this.iElAbEZe = var1.getIntOr("Age", 0);
      this.we1GYY = this.position();
      this.qU1QioDiC = true;
      this.sTrE = this.level().getGameTime() - this.iElAbEZe;
      if (!this.level().isClientSide()) {
         this.syncSpawnData();
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput var1) {
      var1.putString("ProjectileId", this.rxYnaa);
      if (this.eJDY != null) {
         var1.store("Owner", UUIDUtil.CODEC, this.eJDY);
      }

      var1.putDouble("NoseX", this.r6s7FL3.x);
      var1.putDouble("NoseY", this.r6s7FL3.y);
      var1.putDouble("NoseZ", this.r6s7FL3.z);
      var1.putInt("Age", this.iElAbEZe);
   }
}
