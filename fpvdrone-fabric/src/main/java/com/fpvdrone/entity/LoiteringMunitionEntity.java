package com.fpvdrone.entity;

import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.util.Prediction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.DroneDefinitions;
import com.fpvdrone.data.DroneRole;
import com.fpvdrone.data.TerminalMode;
import com.fpvdrone.sync.SmoothingMode;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.data.PayloadStation;
import com.fpvdrone.data.SoundProfile;
import com.fpvdrone.data.BoneBox;
import com.fpvdrone.data.PlacedInteractZone;
import com.fpvdrone.data.RocketMount;
import com.fpvdrone.data.WeaponDamageRule;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.data.NearestBoxHit;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.entity.DelayedMunitionSpawns;
import com.fpvdrone.data.OrientedBox;
import com.fpvdrone.data.IvHitInfo;
import com.fpvdrone.item.FuelCanItem;
import com.fpvdrone.item.RocketPodMountItem;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.ProjectileBurstPacket;
import com.fpvdrone.network.ReconMarkPacket;
import com.fpvdrone.network.MunitionClientStatePacket;
import com.fpvdrone.network.ReconTilePacket;
import com.fpvdrone.network.LaunchSoundPacket;
import com.fpvdrone.registry.ModItems;
import com.fpvdrone.registry.ModSounds;
import com.fpvdrone.util.LoadedChunks;
import com.fpvdrone.data.OrientedBoxGrid;
import com.fpvdrone.data.Waypoint;
import com.fpvdrone.sync.ObserverRange;
import com.fpvdrone.client.ClientHooks;
import com.fpvdrone.entity.ExplosionSoundRangeSource;
import com.fpvdrone.sync.RemoteSmoothed;
import com.fpvdrone.data.GeoBoneBoxes;
import com.fpvdrone.data.WeaponDamageRules;
import com.fpvdrone.item.DroneFuel;
import com.fpvdrone.sync.RemoteSmoother;
import com.fpvdrone.entity.TerrainProbe;
import com.fpvdrone.data.FlightRoute;
import com.fpvdrone.sync.LinearExtrapolator;
import com.fpvdrone.item.RocketPodMask;
import com.fpvdrone.util.ResetSequence;
import com.fpvdrone.util.ParticleSendRange;
import com.fpvdrone.entity.DamageZoneTarget;
import com.fpvdrone.util.BitFlags;
import com.fpvdrone.physics.BoxTerrainCollision;
import com.fpvdrone.sync.KinematicState;
import com.fpvdrone.sync.RemoteStateBroadcaster;
import com.fpvdrone.entity.LookaheadChunkLoader;
import com.fpvdrone.entity.MunitionCollision;
import com.fpvdrone.sync.Extrapolator;
import com.fpvdrone.entity.ProjectileHitPoints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.PacketDistributor.TargetPoint;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public class LoiteringMunitionEntity extends Entity implements ObserverRange, ExplosionSoundRangeSource, RemoteSmoothed, DamageZoneTarget, GeoEntity {
   private static final Logger wuhB = LoggerFactory.getLogger("FPVDrone/FP1");
   private static final EntityDataAccessor<String> dOfFwvo = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Float> rsDd50tfV = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> uSUb = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> jGhI2 = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> wEJx3Itb = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> bAcdvq9 = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> c1jHYPb4 = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> v8ZN = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> zTT3nw6 = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> vmpmU = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> b69s = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Optional<UUID>> cXHvq = SynchedEntityData.defineId(LoiteringMunitionEntity.class, FpvDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Byte> rxYnaa = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> r6s7FL3 = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Boolean> iAKNPAj = SynchedEntityData.defineId(LoiteringMunitionEntity.class, EntityDataSerializers.BOOLEAN);
   private final AnimatableInstanceCache qlv6NKWnH = GeckoLibUtil.createInstanceCache(this);
   private LoiteringMunitionEntity.Ol3TEDSog hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.v5suI;
   private int iS1JnZov = 0;
   private double lfyfLwuw;
   private double wyNDWDB;
   private double iRWc2Pi;
   private boolean tpzIHm = false;
   private double eJDY;
   private double zHBAQgZP;
   private double we1GYY;
   private double pxmzA;
   private double iElAbEZe;
   private double sTrE;
   private double ifpFOCaAs;
   private int uQdlyiwB = Integer.MIN_VALUE;
   private int ivDR = Integer.MIN_VALUE;
   @Nullable
   private FlightRoute n9lX8A;
   private int wBXn = 0;
   private boolean mM7DsB9AR = true;
   private boolean uKpZYj = true;
   private double l2IWOAGO;
   private double xYttSy;
   private double kNoG;
   private double fvsAOg;
   private double lFt1ckP = Double.NEGATIVE_INFINITY;
   private int qU1QioDiC = 0;
   private boolean zQOhlhTZx = false;
   private int lvk8rWUGD = 0;
   private int pyHY5 = -1;
   private int idXga = -1;
   private boolean i263D9 = false;
   private double rOIp0p7;
   private double iKXOcc;
   private double jt9oI60ST;
   private float oEuCBB;
   private boolean uj1PcjLg = false;
   private boolean zgQgCA9 = false;
   private int qV33e = 0;
   private double xogV5KBhg = 0.0;
   private double rYlh;
   private double sZzOK7T;
   private double kNWIu;
   private boolean wTo0I = false;
   private double hXya;
   private double wogVpI;
   private double iY41Hgn0m;
   private boolean nwFPfQ = false;
   private boolean qz71Ga = false;
   private Vec3 dZAuCP = Vec3.ZERO;
   private double p9T0hGf = 0.0;
   private double i7EP1lKl;
   private boolean dReHaJ = false;
   private boolean jm7PbrhC = false;
   private double jfdtPQG;
   private double urdCiPZD;
   private double gQiZ;
   private boolean nTdR = false;
   @Nullable
   private Vec3 be9wl;
   private Vec3 nL8hNS4cc = Vec3.ZERO;
   private double e89G;
   private int jy4YJZH = -1;
   private static final int qJ6kALs = 3;
   @Nullable
   private UUID pWmAxmwe;
   @Nullable
   private ChunkPos srOgYIAu;
   private int kbDIB5 = 0;
   private static final int pEnzO8R = 6;
   private static final int a4SU3J = 10;
   private static final int mWzvAips = 12;
   private int siD3Qkq = 0;
   private final Set<Long> i4g8OuWg = new HashSet<>();
   private static final int ymIwVc = 4;
   private int d7g0AQ = 0;
   private boolean kNkq = false;
   public static final double v5suI = 20.0;
   private double tLYLf = 0.0;
   private int rY9ql8AT = 0;
   private int cd43 = 0;
   private static final int nVSOzEs = 6;
   private static final double rEs46 = 0.12;
   private double xgtm4P;
   private double iYcPD;
   private double w1AKSl;
   private boolean w7IgPx = false;
   private int lXZR = -1;
   private boolean iUE1QRL1b = false;
   private LoiteringMunitionEntity.KKeMvpuBgEY gHP6 = LoiteringMunitionEntity.KKeMvpuBgEY.ibzfxic;
   private double labwdcQcK = Double.NaN;
   private static final double vtqeBx = 0.02;
   private static final double tFKQMgw = 6.0;
   private static final double wbLyr = 0.3;
   private static final double mQmm97fR = 0.05;
   private static final double xUE3ql = 0.35;
   private static final double ymJN = 1.5;
   private float aB5HSbE7;
   private float lBCdCh6v;
   private float g47bf1Oba;
   private float iHgiE174k;
   private double reKjJYGBq;
   private double rMBK;
   private double ov9UANGPJ;
   private boolean oWdz8;
   private double c2CFHNH;
   private double yJOe8XI;
   private static final float xGLwK = 40.0F;
   private static final float aZlEtI = 120.0F;
   private static final float je9602rx8 = 0.06F;
   private float l0qpS = 0.0F;
   private float qVWk = 0.0F;
   private float bbO1 = 40.0F;
   private boolean oycRoM = false;
   public int ibzfxic = Integer.MIN_VALUE;
   private String rRet69u = null;
   private int zMsu6RA = Integer.MIN_VALUE;
   private static final int tmC6 = 2;
   private final RemoteSmoother iBH0q = new RemoteSmoother();
   private final OrientedBoxGrid ffvb = new OrientedBoxGrid(4.0);
   private int rcg4HTTux;
   private static final double rsLnpSv = 4.0;
   private static final int cDES0eK = 10;
   private int h7dk9 = -1;
   private static final int htey6sa = 4;
   private long wycnSHfF = Long.MIN_VALUE;
   private static final double yZLmB = 8.0;
   private static final double wMPl = 6.0;
   private static final double w3z45 = 1.5;
   private static final float jPK5A = 0.08F;
   private static final float iQLPaw = 0.05F;
   private static final float aqjnLPe9H = 6.0F;

   public void v5suI(String var1) {
      this.rRet69u = var1;
      this.zMsu6RA = this.tickCount;
   }

   public String wuhB() {
      String var1 = this.rRet69u;
      this.rRet69u = null;
      if (var1 == null) {
         return null;
      } else {
         return this.tickCount - this.zMsu6RA <= 2 ? var1 : null;
      }
   }

   public LoiteringMunitionEntity(EntityType<? extends LoiteringMunitionEntity> var1, Level var2) {
      super(var1, var2);
      this.noPhysics = false;
      this.blocksBuilding = false;
      this.rcg4HTTux = this.ffvb.ibzfxic();
      // 1.1.8 set noCulling; in 26.3 the renderer decides (cullingBox()).
   }

   public void v5suI(String var1, float var2, double var3, double var5, double var7, @Nullable UUID var9) {
      this.ibzfxic(var1);
      DroneDefinition.LoiterFlight var10 = this.zQOhlhTZx();
      this.l2IWOAGO = var2;
      this.xYttSy = var10.c1jHYPb4();
      this.kNoG = 0.0;
      this.fvsAOg = var10.uSUb();
      this.lfyfLwuw = var3;
      this.wyNDWDB = var5;
      this.iRWc2Pi = var7;
      this.tpzIHm = true;
      this.ibzfxic(var9);
      this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.v5suI;
      this.iS1JnZov = var10.v8ZN();
      this.setYRot(var2);
      this.yRotO = var2;
      this.setXRot((float)(-this.xYttSy));
      this.xRotO = (float)(-this.xYttSy);
      this.rsDd50tfV(0.0F);
   }

   public void v5suI(String var1, float var2, @Nullable UUID var3) {
      this.ibzfxic(var1);
      this.l2IWOAGO = var2;
      this.xYttSy = 0.0;
      this.kNoG = 0.0;
      this.fvsAOg = 0.0;
      this.dZAuCP = Vec3.ZERO;
      this.p9T0hGf = 0.0;
      this.tpzIHm = false;
      this.ibzfxic(var3);
      this.uj1PcjLg = true;
      this.zgQgCA9 = false;
      this.xogV5KBhg = 0.0;
      this.qV33e = 10;
      this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.v5suI;
      this.setYRot(var2);
      this.yRotO = var2;
      this.setXRot(0.0F);
      this.xRotO = 0.0F;
      this.rsDd50tfV(0.0F);
      this.entityData.set(zTT3nw6, true);
      this.entityData.set(vmpmU, true);
      this.wogVpI();
      this.yOld = this.yo = this.getY();
      this.xOld = this.xo = this.getX();
      this.zOld = this.zo = this.getZ();
   }

   private DroneDefinition.LoiterFlight zQOhlhTZx() {
      return this.uQdlyiwB().qlv6NKWnH();
   }

   private DroneDefinition.GroundLaunch lvk8rWUGD() {
      return this.zQOhlhTZx().wuhB();
   }

   private DroneDefinition.Wreck pyHY5() {
      return this.zQOhlhTZx().dOfFwvo();
   }

   @Nullable
   private Vector3f idXga() {
      DroneDefinition.Collision var1 = this.uQdlyiwB().pyHY5();
      return var1 != null && var1.dOfFwvo() != null ? MunitionCollision.v5suI(var1.dOfFwvo()) : null;
   }

   private void v5suI(BoxTerrainCollision.Rdl0X5jeod var1) {
      DroneDefinition.Wreck var2 = this.pyHY5();
      var1.hOKMYHOaC = var2.gravity();
      var1.iS1JnZov = var2.maxFall();
      var1.cXHvq = var2.skidFriction();
      var1.lfyfLwuw = var2.staticFrictionSpeed();
      var1.wyNDWDB = var2.yawDamping();
      var1.iRWc2Pi = var2.maxYawVel();
      var1.tpzIHm = this.idXga();
   }

   private boolean i263D9() {
      if (this.tLYLf <= 0.0) {
         return false;
      }

      if (this.cd43 > 0 && (this.aB5HSbE7 + 1.0) / 2.0 <= 0.05) {
         return true;
      }

      double var1 = 20.0 / Math.max(1, this.zQOhlhTZx().iElAbEZe());
      this.v5suI(this.tLYLf - var1);
      return this.tLYLf > 0.0;
   }

   public void v5suI(@Nullable FlightRoute var1) {
      if (var1 != null && !var1.dOfFwvo()) {
         this.n9lX8A = var1;
         this.wBXn = 0;
         this.entityData.set(iAKNPAj, true);
         Waypoint var2 = var1.v5suI(0);
         this.lfyfLwuw = var2.v5suI;
         this.wyNDWDB = var2.ibzfxic;
         this.iRWc2Pi = var2.wuhB;
         this.tpzIHm = true;
         this.rYlh = var2.v5suI;
         this.sZzOK7T = var2.ibzfxic;
         this.kNWIu = var2.wuhB;
         this.wTo0I = true;
         if (!this.uj1PcjLg && !this.qz71Ga && !this.zQOhlhTZx && this.hOKMYHOaC == LoiteringMunitionEntity.Ol3TEDSog.dOfFwvo) {
            this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.wuhB;
         }
      } else {
         this.n9lX8A = null;
         this.entityData.set(iAKNPAj, false);
      }
   }

   public boolean dOfFwvo() {
      return this.n9lX8A != null && !this.n9lX8A.dOfFwvo();
   }

   public boolean rsDd50tfV() {
      return (Boolean)this.entityData.get(iAKNPAj);
   }

   @Nullable
   public FlightRoute uSUb() {
      return this.n9lX8A;
   }

   public void v5suI(float var1, float var2, float var3, float var4) {
      this.aB5HSbE7 = Mth.clamp(var1, -1.0F, 1.0F);
      this.lBCdCh6v = Mth.clamp(var2, -1.0F, 1.0F);
      this.g47bf1Oba = Mth.clamp(var3, -1.0F, 1.0F);
      this.iHgiE174k = Mth.clamp(var4, -1.0F, 1.0F);
      this.cd43 = 6;
   }

   public void v5suI(double var1, double var3, double var5, boolean var7, boolean var8, int var9) {
      this.w7IgPx = var7;
      if (var7) {
         this.xgtm4P = var1;
         this.iYcPD = var3;
         this.w1AKSl = var5;
         this.lXZR = var9;
         if (var8 && !this.iUE1QRL1b) {
            double var10 = var1 - this.getX();
            double var12 = var5 - this.getZ();
            double var14 = Math.sqrt(var10 * var10 + var12 * var12);
            double var16 = var3 - this.getY();
            float var18 = Mth.clamp(this.uQdlyiwB().qlv6NKWnH().rsDd50tfV().guidedAttackAngleDeg(), 1.0F, 89.0F);
            double var19 = Math.toDegrees(Math.atan2(-var16, Math.max(0.001, var14)));
            this.gHP6 = var19 >= var18 ? LoiteringMunitionEntity.KKeMvpuBgEY.v5suI : LoiteringMunitionEntity.KKeMvpuBgEY.ibzfxic;
            this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.wuhB;
            this.labwdcQcK = this.getY();
            this.xYttSy = 0.0;
            this.kNoG = 0.0;
         }

         if (var8) {
            this.iUE1QRL1b = true;
         }
      } else {
         this.iUE1QRL1b = false;
         this.gHP6 = LoiteringMunitionEntity.KKeMvpuBgEY.ibzfxic;
         this.lXZR = -1;
         this.labwdcQcK = Double.NaN;
      }

      this.cd43 = 6;
   }

   public boolean jGhI2() {
      return this.cd43 > 0;
   }

   public String wEJx3Itb() {
      return (String)this.entityData.get(dOfFwvo);
   }

   public void ibzfxic(String var1) {
      this.entityData.set(dOfFwvo, var1 == null ? "fp1" : var1);
   }

   private boolean rOIp0p7() {
      return "test_target".equals(this.wEJx3Itb());
   }

   public float bAcdvq9() {
      return (Float)this.entityData.get(rsDd50tfV);
   }

   private void rsDd50tfV(float var1) {
      this.entityData.set(rsDd50tfV, var1);
   }

   public float c1jHYPb4() {
      return (Float)this.entityData.get(uSUb);
   }

   public float v8ZN() {
      return (Float)this.entityData.get(jGhI2);
   }

   public float zTT3nw6() {
      return (Float)this.entityData.get(wEJx3Itb);
   }

   public boolean vmpmU() {
      return (Boolean)this.entityData.get(bAcdvq9);
   }

   public boolean wyNDWDB() {
      return (Boolean)this.entityData.get(c1jHYPb4);
   }

   public boolean iRWc2Pi() {
      return (Boolean)this.entityData.get(v8ZN);
   }

   public boolean tpzIHm() {
      return (Boolean)this.entityData.get(zTT3nw6);
   }

   public boolean eJDY() {
      return (Boolean)this.entityData.get(vmpmU);
   }

   public void setDeltaMovement(Vec3 var1) {
      if (this.qz71Ga || this.uj1PcjLg) {
         super.setDeltaMovement(var1);
      }
   }

   public float zHBAQgZP() {
      return this.uQdlyiwB().eJDY();
   }

   public double we1GYY() {
      return this.level().isClientSide() ? ((Float)this.entityData.get(b69s)).floatValue() : this.tLYLf;
   }

   public void v5suI(double var1) {
      this.tLYLf = Mth.clamp(var1, 0.0, 20.0);
      this.entityData.set(b69s, (float)this.tLYLf);
   }

   public double pxmzA() {
      return Math.max(0.0, 20.0 - this.we1GYY());
   }

   public boolean iElAbEZe() {
      return this.we1GYY() > 1.0E-6;
   }

   public boolean sTrE() {
      return this.zQOhlhTZx().v5suI();
   }

   public float ibzfxic(float var1) {
      return (float)Mth.lerp(var1, this.yJOe8XI, this.c2CFHNH);
   }

   public float wuhB(float var1) {
      return this.qVWk + (this.l0qpS - this.qVWk) * var1;
   }

   public float ifpFOCaAs() {
      return !this.oycRoM ? 0.0F : Mth.clamp(this.bbO1 / 40.0F, 0.0F, 1.0F);
   }

   public DroneDefinition uQdlyiwB() {
      return DroneDefinitions.ibzfxic(this.wEJx3Itb());
   }

   @Override
   public float uj1PcjLg() {
      return this.uQdlyiwB().qV33e();
   }

   public boolean v5suI(UUID var1) {
      return this.pWmAxmwe != null && var1 != null && this.pWmAxmwe.equals(var1);
   }

   public Identifier ivDR() {
      return Identifier.fromNamespaceAndPath("fpvdrone", this.uQdlyiwB().iElAbEZe());
   }

   public Identifier n9lX8A() {
      return Identifier.fromNamespaceAndPath("fpvdrone", this.uQdlyiwB().sTrE());
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(dOfFwvo, "fp1");
      builder.define(rsDd50tfV, 0.0F);
      builder.define(uSUb, 0.0F);
      builder.define(jGhI2, 0.0F);
      builder.define(wEJx3Itb, 0.0F);
      builder.define(bAcdvq9, false);
      builder.define(c1jHYPb4, false);
      builder.define(v8ZN, false);
      builder.define(zTT3nw6, false);
      builder.define(vmpmU, false);
      builder.define(b69s, 0.0F);
      builder.define(cXHvq, Optional.empty());
      builder.define(rxYnaa, (byte)0);
      builder.define(r6s7FL3, (byte)0);
      builder.define(iAKNPAj, false);
   }

   public Optional<UUID> wBXn() {
      return (Optional<UUID>)this.entityData.get(cXHvq);
   }

   private void ibzfxic(@Nullable UUID var1) {
      this.pWmAxmwe = var1;
      this.entityData.set(cXHvq, Optional.ofNullable(var1));
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public boolean isPushable() {
      return false;
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean shouldRenderAtSqrDistance(double var1) {
      return this.wuhB(var1);
   }

   public boolean isAlwaysTicking() {
      return true;
   }

   @Override
   public List<OrientedBox> v5suI(float var1) {
      return MunitionCollision.v5suI(this.iKXOcc(), this, var1);
   }

   public List<String> mM7DsB9AR() {
      var var1 = this.iKXOcc();
      var var2 = new ArrayList(var1.size());

      for (BoneBox var4 : var1) {
         var2.add(var4.rsDd50tfV());
      }

      return var2;
   }

   private List<BoneBox> iKXOcc() {
      var var1 = MunitionCollision.v5suI(this);
      if (!this.uKpZYj()) {
         return var1;
      }

      var var2 = this.jt9oI60ST();
      var var3 = new ArrayList(var1.size());

      for (BoneBox var5 : var1) {
         if (!var2.contains(var5.rsDd50tfV()) || this.dOfFwvo(var5.rsDd50tfV())) {
            var3.add(var5);
         }
      }

      return var3;
   }

   public List<PlacedInteractZone> dOfFwvo(float var1) {
      DroneDefinition var2 = this.uQdlyiwB();
      var var3 = var2.c1jHYPb4();
      if (var3.isEmpty()) {
         return List.of();
      }

      var var4 = new HashMap();

      for (InteractZone var6 : var3) {
         var4.put(var6.ibzfxic(), var6);
      }

      var var12 = new ArrayList();
      String var13 = var2.hOKMYHOaC();
      var var7 = GeoBoneBoxes.dOfFwvo(var13);
      if (!var7.isEmpty()) {
         var var8 = MunitionCollision.v5suI(var7, this, var1);

         for (int var9 = 0; var9 < var7.size() && var9 < var8.size(); var9++) {
            InteractZone var10 = (InteractZone)var4.get(((BoneBox)var7.get(var9)).rsDd50tfV());
            if (var10 != null) {
               var12.add(new PlacedInteractZone(var10.v5suI(), (OrientedBox)var8.get(var9)));
            }
         }
      }

      var var14 = this.iKXOcc();
      var var15 = this.v5suI(var1);

      for (int var16 = 0; var16 < var14.size() && var16 < var15.size(); var16++) {
         InteractZone var11 = (InteractZone)var4.get(((BoneBox)var14.get(var16)).rsDd50tfV());
         if (var11 != null) {
            var12.add(new PlacedInteractZone(var11.v5suI(), (OrientedBox)var15.get(var16)));
         }
      }

      return var12.isEmpty() ? List.of() : var12;
   }

   public boolean uKpZYj() {
      return this.uQdlyiwB().wEJx3Itb();
   }

   public boolean l2IWOAGO() {
      return this.xYttSy() != 0;
   }

   public byte xYttSy() {
      return (Byte)this.entityData.get(rxYnaa);
   }

   private void v5suI(byte var1) {
      this.entityData.set(rxYnaa, var1);
   }

   public boolean v5suI(int var1) {
      return BitFlags.v5suI(this.xYttSy(), var1);
   }

   private void v5suI(int var1, boolean var2) {
      byte var3 = this.xYttSy();
      this.v5suI(var2 ? BitFlags.ibzfxic(var3, var1) : BitFlags.wuhB(var3, var1));
   }

   public int kNoG() {
      DroneDefinition var1 = this.uQdlyiwB();
      return var1.wEJx3Itb() ? var1.jt9oI60ST().rsDd50tfV().size() : 0;
   }

   private Set<String> jt9oI60ST() {
      DroneDefinition var1 = this.uQdlyiwB();
      return var1.wEJx3Itb() ? var1.jt9oI60ST().v5suI() : Set.of();
   }

   private boolean dOfFwvo(String var1) {
      DroneDefinition var2 = this.uQdlyiwB();
      if (!var2.wEJx3Itb()) {
         return false;
      }

      int var3 = var2.jt9oI60ST().v5suI(var1);
      return var3 >= 0 && this.v5suI(var3);
   }

   public byte fvsAOg() {
      return (Byte)this.entityData.get(r6s7FL3);
   }

   private void ibzfxic(byte var1) {
      this.entityData.set(r6s7FL3, var1);
   }

   public boolean ibzfxic(int var1) {
      return BitFlags.v5suI(this.fvsAOg(), var1);
   }

   public int lFt1ckP() {
      if (!this.uKpZYj()) {
         return -1;
      }

      int var1 = this.kNoG();

      for (int var2 = 1; var2 <= var1; var2++) {
         int var3 = (this.h7dk9 + var2) % var1;
         if (this.ibzfxic(var3)) {
            return var3;
         }
      }

      return -1;
   }

   private void wuhB(int var1) {
      this.ibzfxic(BitFlags.ibzfxic(this.fvsAOg(), var1));
   }

   private void dOfFwvo(int var1) {
      this.ibzfxic(BitFlags.wuhB(this.fvsAOg(), var1));
   }

   public void v5suI(int var1, @Nullable ServerPlayer var2) {
      if (!this.level().isClientSide() && this.ibzfxic(var1)) {
         DroneDefinition var3 = this.uQdlyiwB();
         if (var3.wEJx3Itb()) {
            PayloadStation var4 = var3.jt9oI60ST().rsDd50tfV().get(var1);
            if (this.dOfFwvo(var4.ibzfxic())) {
               String var5 = var4.v5suI(var3.jt9oI60ST().dOfFwvo());
               Item var6 = wuhB(var5);
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

   public boolean ibzfxic(int var1, ServerPlayer var2) {
      if (!this.level().isClientSide() && var2 != null) {
         DroneDefinition var3 = this.uQdlyiwB();
         if (!var3.wEJx3Itb() || var1 < 0 || var1 >= var3.jt9oI60ST().rsDd50tfV().size()) {
            return false;
         }

         if (this.ibzfxic(var1)) {
            return false;
         }

         PayloadStation var4 = var3.jt9oI60ST().rsDd50tfV().get(var1);
         if (!this.dOfFwvo(var4.ibzfxic())) {
            return false;
         }

         String var5 = var4.v5suI(var3.jt9oI60ST().dOfFwvo());
         Item var6 = wuhB(var5);
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

   public void v5suI(byte var1, byte var2) {
      this.v5suI(var1);
      this.ibzfxic(var2);
   }

   @Override
   public boolean ibzfxic() {
      return false;
   }

   /** 1.1.8 getBoundingBoxForCulling: the collision box grown by half a block; in 26.3 the renderer culls with it. */
   public AABB cullingBox() {
      AABB var1 = MunitionCollision.ibzfxic(this, 1.0F);
      return var1 != null ? var1.inflate(0.5) : this.getBoundingBox();
   }

   private void oEuCBB() {
      AABB var1 = MunitionCollision.ibzfxic(this, 1.0F);
      if (var1 != null) {
         this.setBoundingBox(var1);
      }
   }


   public void registerControllers(AnimatableManager.ControllerRegistrar var1) {
      // 1.1.8 registered one controller that always stops; nothing is animated.
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.qlv6NKWnH;
   }

   public void tick() {
      if (!this.level().isClientSide()) {
         super.tick();
         if (this.zQOhlhTZx) {
            this.dReHaJ();
            return;
         }

         if (this.uj1PcjLg) {
            this.qV33e();
            return;
         }

         if (this.qz71Ga) {
            this.gQiZ();
            return;
         }

         if (this.rOIp0p7()) {
            this.zgQgCA9();
            return;
         }

         LookaheadChunkLoader.v5suI((ServerLevel)this.level(), this.getUUID(), this.position(), this.getDeltaMovement(), this.uQdlyiwB().b69s().wuhB());
         if (!this.nwFPfQ) {
            this.hXya = this.getX();
            this.wogVpI = this.getY();
            this.iY41Hgn0m = this.getZ();
            this.nwFPfQ = true;
         }

         this.qJ6kALs();
         this.pWmAxmwe();
         this.nwFPfQ();
      }
   }

   private void zgQgCA9() {
      if (!this.i263D9) {
         this.rOIp0p7 = this.getX();
         this.iKXOcc = this.getY();
         this.jt9oI60ST = this.getZ();
         this.oEuCBB = this.getYRot();
         this.i263D9 = true;
      }

      this.noPhysics = true;
      this.setPos(this.rOIp0p7, this.iKXOcc, this.jt9oI60ST);
      super.setDeltaMovement(Vec3.ZERO);
      this.setYRot(this.oEuCBB);
      this.setXRot(0.0F);
      this.rsDd50tfV(0.0F);
      this.oEuCBB();
      this.v5suI(false);
   }

   @Override
   public SmoothingMode b69s() {
      return SmoothingMode.REMOTE;
   }

   @Override
   public RemoteSmoother cXHvq() {
      return this.iBH0q;
   }

   @Override
   public Extrapolator rxYnaa() {
      return LinearExtrapolator.v5suI;
   }

   @Override
   public double r6s7FL3() {
      return this.uQdlyiwB().b69s().snapDistance();
   }

   @Override
   public double iAKNPAj() {
      return this.uQdlyiwB().b69s().v5suI();
   }

   @Override
   public double qlv6NKWnH() {
      return this.uQdlyiwB().b69s().ibzfxic();
   }

   @Override
   public void v5suI(KinematicState var1) {
      Vec3 var2 = this.getDeltaMovement();
      var1.v5suI(this.getX(), this.getY(), this.getZ());
      var1.ibzfxic(var2.x, var2.y, var2.z);
      var1.v5suI((float)this.l2IWOAGO, (float)(-this.xYttSy), (float)this.kNoG);
   }

   @Override
   public void v5suI(KinematicState var1, boolean var2) {
      this.setPos(var1.v5suI, var1.ibzfxic, var1.wuhB);
      if (var1.v8ZN) {
         this.setYRot(var1.jGhI2);
         this.setXRot(var1.wEJx3Itb);
         this.yJOe8XI = var1.bAcdvq9;
         this.c2CFHNH = var1.bAcdvq9;
      }

      super.setDeltaMovement(var1.dOfFwvo, var1.rsDd50tfV, var1.uSUb);
      if (var2) {
         this.setOldPosAndRot();
      }

      this.oEuCBB();
   }

   /** 1.1.8 lerpTo: server positions are ignored while this client simulates the munition, else taken as is. */
   @Override
   protected InterpolationHandler createInterpolationHandler() {
      return new InterpolationHandler.NoOpInterpolationHandler() {
         @Override
         public boolean interpolateTo(@Nullable PositionPath position, float yRot, float xRot, boolean hasRotation) {
            return LoiteringMunitionEntity.this.level().isClientSide() && LoiteringMunitionEntity.this.iBH0q.v5suI();
         }
      };
   }

   @Override
   public void a_() {
      float var1;
      if (!this.tpzIHm()) {
         var1 = 40.0F;
      } else {
         Vec3 var2 = this.getDeltaMovement();
         double var3 = Math.sqrt(var2.x * var2.x + var2.y * var2.y + var2.z * var2.z);
         var1 = (float)Math.min(var3 * 120.0, 40.0);
      }

      if (!this.oycRoM) {
         this.bbO1 = var1;
         this.oycRoM = true;
      } else {
         this.bbO1 = this.bbO1 + (var1 - this.bbO1) * 0.06F;
      }

      if (this.bbO1 < 0.05F) {
         this.bbO1 = 0.0F;
      }

      this.qVWk = this.l0qpS;
      this.l0qpS = this.l0qpS + this.bbO1;
      if (this.l0qpS >= 360.0F) {
         this.l0qpS -= 360.0F;
         this.qVWk -= 360.0F;
      }
   }

   private void qV33e() {
      this.noPhysics = true;
      if (this.rY9ql8AT > 0) {
         this.rY9ql8AT--;
      }

      var var1 = MunitionCollision.v5suI(this);
      if (var1.isEmpty()) {
         this.wTo0I();
         super.setDeltaMovement(Vec3.ZERO);
         this.v5suI(false);
      } else {
         DroneDefinition.GroundLaunch var2 = this.lvk8rWUGD();
         boolean var3 = this.qV33e > 0;
         if (var3) {
            this.qV33e--;
         }

         boolean var4 = !var3 && this.v5suI(false, this.pyHY5().shoveSpeed());
         boolean var5 = !var3 && !this.zgQgCA9 && this.hXya();
         double var6 = Math.toRadians(this.l2IWOAGO);
         double var8 = -Math.sin(var6);
         double var10 = Math.cos(var6);
         if (var5) {
            this.dZAuCP = this.dZAuCP.add(var8 * var2.pushImpulse(), 0.0, var10 * var2.pushImpulse());
         }

         if (this.zgQgCA9) {
            this.i263D9();
            this.dZAuCP = this.dZAuCP.add(var8 * var2.rollAccel(), 0.0, var10 * var2.rollAccel());
         }

         double var12 = Math.sqrt(this.dZAuCP.x * this.dZAuCP.x + this.dZAuCP.z * this.dZAuCP.z);
         double var14 = this.zgQgCA9 ? var2.takeoffSpeed() : var2.pushCap();
         if ((this.zgQgCA9 || var5) && var12 > var14 && var12 > 1.0E-9) {
            double var16 = var14 / var12;
            this.dZAuCP = new Vec3(this.dZAuCP.x * var16, this.dZAuCP.y, this.dZAuCP.z * var16);
         }

         double var35 = this.getX();
         double var18 = this.getZ();
         double var20 = this.xYttSy;
         double var22 = this.kNoG;
         BoxTerrainCollision.Rdl0X5jeod var24 = new BoxTerrainCollision.Rdl0X5jeod(
            this.getX(),
            this.getY(),
            this.getZ(),
            this.xYttSy,
            this.kNoG,
            (float)this.l2IWOAGO,
            this.dZAuCP.x,
            this.dZAuCP.y,
            this.dZAuCP.z,
            this.p9T0hGf
         );
         this.v5suI(var24);
         var24.cXHvq = var2.rollFriction();
         var24.eJDY = 4.0;
         BoxTerrainCollision.v5suI(this.level(), var1, var24);
         this.setPos(var24.v5suI, var24.ibzfxic, var24.wuhB);
         this.l2IWOAGO = Mth.wrapDegrees(var24.uSUb);
         this.dZAuCP = new Vec3(var24.jGhI2, var24.wEJx3Itb, var24.bAcdvq9);
         this.p9T0hGf = var24.c1jHYPb4;
         if (var24.zTT3nw6) {
            this.xYttSy = var24.dOfFwvo;
            this.kNoG = var24.rsDd50tfV;
         } else {
            double var25 = Math.sqrt(this.dZAuCP.x * this.dZAuCP.x + this.dZAuCP.z * this.dZAuCP.z);
            double var27 = Mth.clamp(
               Math.toDegrees(Math.atan2(this.dZAuCP.y, Math.max(0.05, var25))), -this.pyHY5().glideMaxPitch(), this.pyHY5().glideMaxPitch()
            );
            this.xYttSy = v5suI(var20, var27, this.pyHY5().glideDiveRate());
            this.kNoG = v5suI(var22, 0.0, 1.5);
         }

         this.wTo0I();
         super.setDeltaMovement(this.dZAuCP.x, this.dZAuCP.y, this.dZAuCP.z);
         if (var24.b69s && !var4 && !var5 && !this.zgQgCA9) {
            this.nL8hNS4cc();
            if (this.level() instanceof ServerLevel var36) {
               LookaheadChunkLoader.v5suI(var36, this.getUUID());
            }
         } else {
            this.be9wl();
         }

         double var37 = Math.toRadians(this.l2IWOAGO);
         double var38 = -Math.sin(var37);
         double var29 = Math.cos(var37);
         if (!this.zgQgCA9) {
            double var31 = this.dZAuCP.x * var38 + this.dZAuCP.z * var29;
            if (var5 && var31 >= var2.commitSpeed()) {
               if (!this.iElAbEZe()) {
                  this.xogV5KBhg();
               } else if (!this.v5suI(var1)) {
                  this.rYlh();
               }
            }
         }

         if (this.zgQgCA9) {
            double var39 = (this.getX() - var35) * var38 + (this.getZ() - var18) * var29;
            if (var39 > 0.0) {
               this.xogV5KBhg += var39;
            }

            if (this.xogV5KBhg >= var2.runway()) {
               double var33 = Math.sqrt(this.dZAuCP.x * this.dZAuCP.x + this.dZAuCP.z * this.dZAuCP.z);
               this.ibzfxic(Math.max(var33, var2.takeoffSpeed()));
               return;
            }
         }

         this.v5suI(false);
         this.dZAuCP();
      }
   }

   private void xogV5KBhg() {
      if (!this.level().isClientSide() && this.rY9ql8AT <= 0) {
         this.rY9ql8AT = 40;
         Player var1 = this.level().getNearestPlayer(this, 4.0);
         if (var1 != null) {
            var1.sendOverlayMessage(Component.translatable("message.fpvdrone.drone_no_fuel"));
         }
      }
   }

   private void rYlh() {
      if (!this.zgQgCA9) {
         if (this.iElAbEZe()) {
            this.zgQgCA9 = true;
            this.xogV5KBhg = 0.0;
            this.entityData.set(zTT3nw6, false);
         }
      }
   }

   private Item sZzOK7T() {
      return ModItems.droneItemFor(this.wEJx3Itb());
   }

   public boolean v5suI(Player var1) {
      if (!this.vmpmU()) {
         return false;
      }

      this.kNWIu();
      return true;
   }

   private void kNWIu() {
      if (!this.level().isClientSide() && !this.isRemoved()) {
         this.nL8hNS4cc();
         if (this.level() instanceof ServerLevel var1) {
            LookaheadChunkLoader.v5suI(var1, this.getUUID());
         }

         double var10 = this.getX();
         double var3 = this.getY() + 0.25;
         double var5 = this.getZ();
         var var7 = this.v5suI(1.0F);
         if (!var7.isEmpty()) {
            var10 = 0.0;
            var3 = 0.0;
            var5 = 0.0;

            for (OrientedBox var9 : var7) {
               var10 += var9.jGhI2().x;
               var3 += var9.jGhI2().y;
               var5 += var9.jGhI2().z;
            }

            var10 /= var7.size();
            var3 /= var7.size();
            var5 /= var7.size();
         }

         ItemStack var14 = new ItemStack(this.sZzOK7T());
         DroneFuel.set(var14, this.we1GYY());
         if (this.uKpZYj()) {
            RocketPodMask.set(var14, this.xYttSy(), this.fvsAOg());
         }

         ItemEntity var15 = new ItemEntity(this.level(), var10, var3, var5, var14);
         var15.setDefaultPickUpDelay();
         var15.setDeltaMovement(Vec3.ZERO);
         this.level().addFreshEntity(var15);
         this.discard();
      }
   }

   private void wTo0I() {
      this.setYRot((float)this.l2IWOAGO);
      this.setXRot((float)(-this.xYttSy));
      this.rsDd50tfV((float)this.kNoG);
      this.oEuCBB();
   }

   private boolean hXya() {
      DroneDefinition.GroundLaunch var1 = this.lvk8rWUGD();
      var var2 = this.v5suI(1.0F);
      if (var2.isEmpty()) {
         return false;
      }

      double var3 = 0.0;
      double var5 = 0.0;

      for (OrientedBox var8 : var2) {
         var3 += var8.jGhI2().x;
         var5 += var8.jGhI2().z;
      }

      var3 /= var2.size();
      var5 /= var2.size();
      AABB var37 = MunitionCollision.ibzfxic(this, 1.0F);
      if (var37 == null) {
         var37 = this.getBoundingBox();
      }

      double var38 = Math.toRadians(this.l2IWOAGO);
      double var10 = -Math.sin(var38);
      double var12 = Math.cos(var38);
      double var14 = var12;
      double var16 = -var10;

      for (Player var20 : this.level().getEntitiesOfClass(Player.class, var37.inflate(0.5), var0 -> !var0.isShiftKeyDown() && !var0.isSpectator())) {
         Vec3 var21 = var20.getLookAngle();
         double var22 = var21.x;
         double var24 = var21.z;
         double var26 = Math.sqrt(var22 * var22 + var24 * var24);
         if (!(var26 < 1.0E-4)) {
            var22 /= var26;
            var24 /= var26;
            if (!(var22 * var10 + var24 * var12 < var1.behindDot()) && !((var3 - var20.getX()) * var10 + (var5 - var20.getZ()) * var12 <= 0.0)) {
               double var28 = Math.abs((var20.getX() - var3) * var14 + (var20.getZ() - var5) * var16);
               if (!(var28 > var1.centerHalfWidth())) {
                  Vec3 var30 = new Vec3(var20.getX(), var20.getY() + 0.5, var20.getZ());
                  double var31 = Double.MAX_VALUE;

                  for (OrientedBox var34 : var2) {
                     var31 = Math.min(var31, var34.ibzfxic(var30).distanceToSqr(var30));
                  }

                  double var41 = this.pyHY5().shoveReach();
                  if (!(var31 > var41 * var41)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private boolean uSUb(Player var1) {
      DroneDefinition.GroundLaunch var2 = this.lvk8rWUGD();
      var var3 = this.v5suI(1.0F);
      if (var3.isEmpty()) {
         return false;
      }

      double var4 = 0.0;
      double var6 = 0.0;

      for (OrientedBox var9 : var3) {
         var4 += var9.jGhI2().x;
         var6 += var9.jGhI2().z;
      }

      var4 /= var3.size();
      var6 /= var3.size();
      double var29 = Math.toRadians(this.l2IWOAGO);
      double var10 = -Math.sin(var29);
      double var12 = Math.cos(var29);
      double var14 = var12;
      double var16 = -var10;
      Vec3 var18 = var1.getLookAngle();
      double var19 = var18.x;
      double var21 = var18.z;
      double var23 = Math.sqrt(var19 * var19 + var21 * var21);
      if (var23 < 1.0E-4) {
         return false;
      }

      var19 /= var23;
      var21 /= var23;
      if (var19 * var10 + var21 * var12 < var2.behindDot()) {
         return false;
      }

      if ((var4 - var1.getX()) * var10 + (var6 - var1.getZ()) * var12 <= 0.0) {
         return false;
      }

      double var25 = Math.abs((var1.getX() - var4) * var14 + (var1.getZ() - var6) * var16);
      return var25 <= var2.centerHalfWidth();
   }

   private void wogVpI() {
      this.dZAuCP = Vec3.ZERO;
      this.p9T0hGf = 0.0;
      this.xYttSy = 0.0;
      this.kNoG = 0.0;
      var var1 = MunitionCollision.v5suI(this);
      if (var1.isEmpty()) {
         this.wTo0I();
      } else {
         double var2 = MunitionCollision.v5suI(this.level(), var1, this.getX(), this.getY(), this.getZ(), (float)this.l2IWOAGO);
         if (var2 != 0.0) {
            this.setPos(this.getX(), this.getY() + var2, this.getZ());
         }

         for (int var4 = 0; var4 < 80; var4++) {
            BoxTerrainCollision.Rdl0X5jeod var5 = new BoxTerrainCollision.Rdl0X5jeod(
               this.getX(), this.getY(), this.getZ(), this.xYttSy, this.kNoG, (float)this.l2IWOAGO, 0.0, this.dZAuCP.y, 0.0, 0.0
            );
            this.v5suI(var5);
            var5.cXHvq = this.lvk8rWUGD().rollFriction();
            var5.eJDY = 4.0;
            BoxTerrainCollision.v5suI(this.level(), var1, var5);
            this.setPos(var5.v5suI, var5.ibzfxic, var5.wuhB);
            this.xYttSy = var5.dOfFwvo;
            this.kNoG = var5.rsDd50tfV;
            this.dZAuCP = new Vec3(0.0, var5.wEJx3Itb, 0.0);
            if (var5.v8ZN && var5.b69s) {
               break;
            }
         }

         BoxTerrainCollision.Rdl0X5jeod var6 = new BoxTerrainCollision.Rdl0X5jeod(
            this.getX(), this.getY(), this.getZ(), this.xYttSy, this.kNoG, (float)this.l2IWOAGO, 0.0, 0.0, 0.0, 0.0
         );
         if (BoxTerrainCollision.wuhB(this.level(), var1, var6)) {
            this.setPos(var6.v5suI, var6.ibzfxic, var6.wuhB);
         }

         this.dZAuCP = Vec3.ZERO;
         this.p9T0hGf = 0.0;
         this.wTo0I();
      }
   }

   private boolean v5suI(List<BoneBox> var1) {
      if (var1.isEmpty()) {
         return false;
      }

      BoxTerrainCollision.Rdl0X5jeod var2 = new BoxTerrainCollision.Rdl0X5jeod(
         this.getX(), this.getY(), this.getZ(), this.xYttSy, this.kNoG, (float)this.l2IWOAGO, 0.0, 0.0, 0.0, 0.0
      );
      if (BoxTerrainCollision.ibzfxic(this.level(), var1, var2)) {
         return true;
      }

      BoxTerrainCollision.Rdl0X5jeod var3 = new BoxTerrainCollision.Rdl0X5jeod(
         this.getX(), this.getY() + 1.0, this.getZ(), this.xYttSy, this.kNoG, (float)this.l2IWOAGO, 0.0, 0.0, 0.0, 0.0
      );
      return BoxTerrainCollision.ibzfxic(this.level(), var1, var3);
   }

   private void ibzfxic(double var1) {
      DroneDefinition.LoiterFlight var3 = this.zQOhlhTZx();
      this.uj1PcjLg = false;
      this.zgQgCA9 = false;
      this.xogV5KBhg = 0.0;
      this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.ibzfxic;
      this.fvsAOg = var1;
      this.xYttSy = 0.0;
      this.kNoG = 0.0;
      this.dZAuCP = Vec3.ZERO;
      this.p9T0hGf = 0.0;
      if (this.wTo0I) {
         this.lfyfLwuw = this.rYlh;
         this.wyNDWDB = this.sZzOK7T;
         this.iRWc2Pi = this.kNWIu;
      } else {
         double var4 = Math.toRadians(this.l2IWOAGO);
         this.lfyfLwuw = this.getX() - Math.sin(var4) * 512.0;
         this.iRWc2Pi = this.getZ() + Math.cos(var4) * 512.0;
         this.wyNDWDB = this.getY() + var3.r6s7FL3();
      }

      this.tpzIHm = true;
      this.setYRot((float)this.l2IWOAGO);
      this.setXRot((float)(-this.xYttSy));
      this.rsDd50tfV(0.0F);
      this.entityData.set(zTT3nw6, false);
      this.entityData.set(vmpmU, false);
      this.v5suI(false);
   }

   public InteractionResult interact(Player var1, InteractionHand var2) {
      ItemStack var3 = var1.getItemInHand(var2);
      if (var3.getItem() instanceof FuelCanItem && this.sTrE()) {
         if (!this.level().isClientSide()) {
            this.v5suI(var1, var2, var3);
         }

         return InteractionResult.SUCCESS;
      } else if (var3.getItem() instanceof RocketPodMountItem) {
         if (!this.level().isClientSide()) {
            this.ibzfxic(var1, var2, var3);
         }

         return InteractionResult.SUCCESS;
      } else {
         if (this.level().isClientSide()) {
            this.iY41Hgn0m();
         }

         return InteractionResult.SUCCESS;
      }
   }

   public InteractionResult interactAt(Player var1, Vec3 var2, InteractionHand var3) {
      if (!this.uKpZYj()) {
         return InteractionResult.PASS;
      }

      int var4 = this.jGhI2(var1);
      if (var4 < 0) {
         return InteractionResult.PASS;
      }

      if (this.ibzfxic(var4)) {
         if (!this.level().isClientSide()) {
            var1.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_tube_full"));
         }

         return InteractionResult.SUCCESS;
      } else {
         DroneDefinition var5 = this.uQdlyiwB();
         PayloadStation var6 = var5.jt9oI60ST().rsDd50tfV().get(var4);
         String var7 = var6.v5suI(var5.jt9oI60ST().dOfFwvo());
         ItemStack var8 = var1.getItemInHand(var3);
         Item var9 = wuhB(var7);
         if (var9 != null && var8.is(var9)) {
            if (!this.level().isClientSide()) {
               if (!var1.getAbilities().instabuild) {
                  var8.shrink(1);
               }

               this.wuhB(var4);
               this.level().playSound(null, this, (SoundEvent)ModSounds.FPV_CONNECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
               var1.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_loaded"));
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   private int jGhI2(Player var1) {
      DroneDefinition var2 = this.uQdlyiwB();
      if (!var2.wEJx3Itb()) {
         return -1;
      }

      var var3 = var2.jt9oI60ST().rsDd50tfV();
      var var4 = MunitionCollision.v5suI(this);
      var var5 = MunitionCollision.v5suI(this, 1.0F);
      var var6 = new ArrayList();
      var var7 = new ArrayList();

      for (int var8 = 0; var8 < var4.size() && var8 < var5.size(); var8++) {
         String var9 = ((BoneBox)var4.get(var8)).rsDd50tfV();
         if (v5suI(var3, var9) >= 0 && this.dOfFwvo(var9)) {
            var6.add((BoneBox)var4.get(var8));
            var7.add((OrientedBox)var5.get(var8));
         }
      }

      Vec3 var11 = var1.getEyePosition();
      Vec3 var12 = var11.add(var1.getLookAngle().scale(8.0));
      NearestBoxHit.KKeMvpuBgEY var10 = NearestBoxHit.v5suI(var6, var7, var11, var12);
      return var10 == null ? -1 : v5suI(var3, var10.v5suI());
   }

   static Item wuhB(String var0) {
      return ModItems.projectileItemFor(var0);
   }

   public void v5suI(@Nullable ServerPlayer var1) {
      if (!this.level().isClientSide() && this.l2IWOAGO()) {
         long var2 = this.level().getGameTime();
         if (var2 >= this.wycnSHfF) {
            DroneDefinition var4 = this.uQdlyiwB();
            if (var4.wEJx3Itb()) {
               int var5 = this.lFt1ckP();
               if (var5 >= 0) {
                  DroneDefinition.RocketPod var6 = var4.jt9oI60ST();
                  PayloadStation var7 = var6.rsDd50tfV().get(var5);
                  String var8 = var7.v5suI(var6.dOfFwvo());
                  ProjectileDefinition var9 = ProjectileDefinitions.find(var8).orElse(null);
                  if (var9 != null) {
                     var var10 = MunitionCollision.v5suI(this);
                     var var11 = MunitionCollision.v5suI(this, 1.0F);
                     Vec3 var12 = null;
                     Vector3f var13 = null;

                     for (int var14 = 0; var14 < var10.size() && var14 < var11.size(); var14++) {
                        if (var7.ibzfxic().equals(((BoneBox)var10.get(var14)).rsDd50tfV())) {
                           var12 = ((OrientedBox)var11.get(var14)).jGhI2();
                           var13 = ((BoneBox)var10.get(var14)).v5suI();
                           break;
                        }
                     }

                     if (var12 != null) {
                        double var29 = Math.toRadians(this.l2IWOAGO);
                        double var16 = Math.toRadians(this.xYttSy);
                        double var18 = Math.cos(var16);
                        Vec3 var20 = new Vec3(-Math.sin(var29) * var18, Math.sin(var16), Math.cos(var29) * var18);
                        ProjectileDefinition.Launch var21 = var9.launch();
                        Vec3 var22 = this.getDeltaMovement().add(var20.scale(var21.muzzleVelocity()));
                        DroppedBombEntity var23 = DroppedBombEntity.v5suI(
                           this.level(),
                           var12,
                           var22,
                           var20,
                           var8,
                           this.wBXn().orElse(null),
                           this.getId(),
                           0.0F,
                           0.0F,
                           0.0F,
                           1.0F,
                           var5,
                           this.level().getGameTime()
                        );
                        this.level().addFreshEntity(var23);
                        this.wycnSHfF = var2 + 4L;
                        this.v5suI(var13, var9.weightGrams(), var21.muzzleVelocity());
                        this.h7dk9 = var5;
                        this.dOfFwvo(var5);
                        this.v5suI(false);
                        SoundProfile var24 = var9.launchSound();
                        if (var24 != null) {
                           Vec3 var25 = var12;
                           double var26 = var24.maxRange();
                           LaunchSoundPacket var28 = new LaunchSoundPacket(var25, var8);
                           ModNetwork.CHANNEL
                              .send(
                                 PacketDistributor.NEAR
                                    .with(() -> new TargetPoint(var25.x, var25.y, var25.z, var26, this.level().dimension())),
                                 var28
                              );
                        }

                        if (var6.wuhB()) {
                           Vec3 var30 = var12;
                           MunitionClientStatePacket var31 = new MunitionClientStatePacket(this.getId(), var7.v5suI());
                           ModNetwork.CHANNEL
                              .send(
                                 PacketDistributor.NEAR
                                    .with(() -> new TargetPoint(var30.x, var30.y, var30.z, FpvConfig.getDroneObserverRange(), this.level().dimension())),
                                 var31
                              );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void v5suI(Vector3f var1, float var2, float var3) {
      if (var1 != null) {
         float var4 = var2 / 1000.0F * var3;
         double var5 = Mth.clamp(0.08F * var4 * var1.x(), -6.0F, 6.0F);
         double var7 = Mth.clamp(0.05F * var4 * var1.y(), -6.0F, 6.0F);
         this.kNoG += var5;
         this.xYttSy += var7;
      }
   }

   private void iY41Hgn0m() {
      ClientHooks.v5suI(this);
   }

   private void v5suI(Player var1, InteractionHand var2, ItemStack var3) {
      ItemStack var4 = var3.copy();
      var4.setCount(1);
      double var5 = FuelCanItem.getFuel(var4);
      boolean var7 = this.qz71Ga || var5 <= 0.001;
      String var8;
      Object[] var9;
      if (var7) {
         double var10 = Math.min(this.we1GYY(), 20.0 - var5);
         if (var10 <= 0.001) {
            var1.sendOverlayMessage(Component.translatable(this.we1GYY() <= 0.001 ? "message.fpvdrone.wreck_empty" : "message.fpvdrone.can_full"));
            return;
         }

         this.v5suI(this.we1GYY() - var10);
         FuelCanItem.setFuel(var4, var5 + var10);
         var8 = "message.fpvdrone.fuel_drained";
         var9 = new Object[]{String.format("%.1f", var10)};
         this.level().playSound(null, this, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.7F, 1.0F);
      } else {
         double var12 = this.v5suI(var4);
         if (var12 <= 0.001) {
            var1.sendOverlayMessage(Component.translatable(var5 <= 0.001 ? "message.fpvdrone.can_empty" : "message.fpvdrone.drone_fuel_full"));
            return;
         }

         var8 = "message.fpvdrone.fuel_added";
         var9 = new Object[]{String.format("%.1f", var12)};
         this.level().playSound(null, this, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.7F, 1.0F);
      }

      if (var3.getCount() <= 1) {
         var1.setItemInHand(var2, var4);
      } else {
         var3.shrink(1);
         if (!var1.getInventory().add(var4)) {
            var1.drop(var4, false, Prediction.SERVER_ONLY);
         }
      }

      var1.sendOverlayMessage(Component.translatable(var8, var9));
   }

   private double v5suI(ItemStack var1) {
      double var2 = FuelCanItem.getFuel(var1);
      double var4 = Math.min(var2, this.pxmzA());
      if (var4 > 1.0E-6) {
         this.v5suI(this.we1GYY() + var4);
         FuelCanItem.setFuel(var1, var2 - var4);
      }

      return var4;
   }

   private void ibzfxic(Player var1, InteractionHand var2, ItemStack var3) {
      if (!this.uKpZYj() || !this.uQdlyiwB().jt9oI60ST().uSUb().isEmpty()) {
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.no_rocket_pod"));
      } else if (this.v5suI(0)) {
         var1.sendOverlayMessage(Component.translatable("message.fpvdrone.rocket_pod_already_installed"));
      } else {
         this.v5suI(var3, var1);
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

   public boolean v5suI(int var1, Player var2) {
      if (this.uKpZYj() && !this.v5suI(var1)) {
         ItemStack var3 = v5suI(var2, RocketPodMountItem.class);
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
      if (this.uKpZYj() && this.v5suI(var1)) {
         this.v5suI(var1, false);
         DroneDefinition var3 = this.uQdlyiwB();
         var var4 = var3.jt9oI60ST().rsDd50tfV();
         var var5 = var3.jt9oI60ST().ibzfxic();
         HashSet var6 = new HashSet<>(((RocketMount)var5.get(var1)).wuhB());
         byte var7 = this.fvsAOg();

         for (int var8 = 0; var8 < var4.size(); var8++) {
            PayloadStation var9 = (PayloadStation)var4.get(var8);
            if (var6.contains(var9.ibzfxic()) && BitFlags.v5suI(var7, var8)) {
               var7 = BitFlags.wuhB(var7, var8);
               String var10 = var9.v5suI(var3.jt9oI60ST().dOfFwvo());
               Item var11 = wuhB(var10);
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

   @Nullable
   static ItemStack v5suI(Player var0, Class<? extends Item> var1) {
      Inventory var2 = var0.getInventory();

      for (int var3 = 0; var3 < var2.getContainerSize(); var3++) {
         ItemStack var4 = var2.getItem(var3);
         if (!var4.isEmpty() && var1.isInstance(var4.getItem())) {
            return var4;
         }
      }

      return null;
   }

   public boolean ibzfxic(Player var1) {
      if (!this.sTrE()) {
         return false;
      }

      if (this.pxmzA() <= 1.0E-6) {
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

   public boolean wuhB(Player var1) {
      if (this.sTrE() && !(this.we1GYY() <= 0.001)) {
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
         double var8 = Math.min(this.we1GYY(), 20.0 - var6);
         if (var8 <= 0.001) {
            return false;
         }

         this.v5suI(this.we1GYY() - var8);
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

   public boolean dOfFwvo(Player var1) {
      if (this.uKpZYj() && this.uQdlyiwB().jt9oI60ST().uSUb().isEmpty() && !this.v5suI(0)) {
         ItemStack var2 = v5suI(var1, RocketPodMountItem.class);
         if (var2 == null) {
            return false;
         }

         this.v5suI(var2, var1);
         return true;
      } else {
         return false;
      }
   }

   public boolean rsDd50tfV(Player var1) {
      if (this.uKpZYj() && this.uQdlyiwB().jt9oI60ST().uSUb().isEmpty() && this.v5suI(0)) {
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

   private double v5suI(DroneDefinition.LoiterFlight var1, TerminalMode var2) {
      int var3 = this.v5suI(Types.MOTION_BLOCKING);
      double var4 = var2 == TerminalMode.DESTROY ? var3 + 4 : var3;
      double var6 = Math.max(0.0, this.getY() - var4);
      double var8 = Math.tan(Math.toRadians(Mth.clamp(var1.iRWc2Pi(), 10.0F, 80.0F)));
      double var10 = var6 / Math.max(0.1, var8);
      return Math.max(var1.zHBAQgZP(), var10);
   }

   private void v5suI(DroneDefinition.LoiterFlight var1, boolean var2) {
      this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.wuhB;
      DroneDefinition.Manual var3 = var1.rsDd50tfV();
      double var4 = v5suI(this.iHgiE174k, var3.expoRoll());
      double var6 = v5suI(-this.g47bf1Oba, var3.expoPitch());
      double var8 = v5suI(this.lBCdCh6v, var3.expoYaw());
      if (var2) {
         this.reKjJYGBq = var1.qlv6NKWnH() > 0.0F ? Mth.clamp(this.kNoG / var1.qlv6NKWnH(), -1.0, 1.0) : var4;
         this.rMBK = var1.iRWc2Pi() > 0.0F ? Mth.clamp(this.xYttSy / var1.iRWc2Pi(), -1.0, 1.0) : var6;
         this.ov9UANGPJ = var8;
      } else {
         double var10 = Mth.clamp(var3.inputSmoothing(), 0.01, 1.0);
         this.reKjJYGBq = this.reKjJYGBq + (var4 - this.reKjJYGBq) * var10;
         this.rMBK = this.rMBK + (var6 - this.rMBK) * var10;
         this.ov9UANGPJ = this.ov9UANGPJ + (var8 - this.ov9UANGPJ) * var10;
      }

      double var61 = Math.max(0.2, var1.wEJx3Itb());
      double var12 = Mth.clamp(this.fvsAOg / var61, var3.speedAuthorityMin(), 1.0);
      double var14 = this.reKjJYGBq * var1.qlv6NKWnH();
      double var16 = (var14 - this.kNoG) * Mth.clamp(var3.rollAgility() * var12, 0.0, 1.0);
      double var18 = Math.abs(this.reKjJYGBq) < 0.05 ? var3.levelReturnRate() : var3.maxRollRate();
      var16 = Mth.clamp(var16, -var18, var18);
      this.kNoG = Mth.clamp(this.kNoG + var16, -var1.qlv6NKWnH(), var1.qlv6NKWnH());
      double var20 = this.rMBK * var1.iRWc2Pi();
      double var22 = (var20 - this.xYttSy) * Mth.clamp(var3.pitchAgility() * var12, 0.0, 1.0);
      double var24 = Math.abs(this.rMBK) < 0.05 ? var3.levelReturnRate() : var3.maxPitchRate();
      var22 = Mth.clamp(var22, -var24, var24);
      this.xYttSy += var22;
      double var26 = (this.aB5HSbE7 + 1.0) / 2.0;
      double var28 = Mth.clamp(1.0 - var26 / 0.3, 0.0, 1.0);
      double var30 = Math.toDegrees(Math.atan(0.16666666666666666)) * var28;
      double var32 = Math.max(var61 + 0.1, var1.jGhI2());
      double var34 = 0.02 * Math.sin(Math.atan(0.16666666666666666)) / (var61 * var61);
      double var36 = var34 * var32 * var32;
      double var38 = var26 > 0.05 ? var36 * var26 : 0.0;
      double var40 = Math.max(-90.0, this.xYttSy - var30);
      this.fvsAOg = this.fvsAOg + (var38 - var34 * this.fvsAOg * this.fvsAOg - 0.02 * Math.sin(Math.toRadians(var40)));
      this.fvsAOg = Mth.clamp(this.fvsAOg, 0.0, Math.max(var32, var1.bAcdvq9()));
      double var42 = var61 * 0.35;
      if (this.fvsAOg < var42) {
         this.xYttSy = this.xYttSy - 1.5 * ((var42 - this.fvsAOg) / var42);
         this.xYttSy = Math.max(this.xYttSy, -var1.iRWc2Pi());
      }

      double var44 = Math.toDegrees(var1.hOKMYHOaC() * Math.tan(Math.toRadians(this.kNoG)) / Math.max(this.fvsAOg, 0.2));
      var44 = Mth.clamp(var44, -var1.iS1JnZov(), var1.iS1JnZov());
      double var46 = this.ov9UANGPJ * var1.iS1JnZov() * 0.12;
      this.l2IWOAGO = Mth.wrapDegrees(this.l2IWOAGO + var44 + var46);
      double var48 = this.xYttSy;
      double var50 = this.kNoG;
      double var52 = this.l2IWOAGO;
      double var54 = Math.toRadians(var52);
      double var56 = Math.toRadians(Math.max(-90.0, var48 - var30));
      double var58 = Math.cos(var56);
      Vec3 var60 = new Vec3(-Math.sin(var54) * var58, Math.sin(var56), Math.cos(var54) * var58).scale(this.fvsAOg);
      super.setDeltaMovement(var60);
      this.setPos(this.getX() + var60.x, this.getY() + var60.y, this.getZ() + var60.z);
      this.oEuCBB();
      this.setYRot((float)var52);
      this.setXRot((float)(-var48));
      this.rsDd50tfV((float)var50);
      this.entityData.set(jGhI2, (float)Mth.clamp(this.kNoG / var1.qlv6NKWnH(), -1.0, 1.0));
      this.entityData.set(uSUb, (float)Mth.clamp(this.xYttSy / var1.iRWc2Pi(), -1.0, 1.0));
      this.entityData.set(wEJx3Itb, (float)Mth.clamp(this.ov9UANGPJ, -1.0, 1.0));
      this.mM7DsB9AR = false;
      this.uKpZYj = v5suI(var1);
      this.v5suI(false);
      this.dZAuCP();
   }

   private void nwFPfQ() {
      DroneDefinition.LoiterFlight var1 = this.zQOhlhTZx();
      this.noPhysics = true;
      if (!this.i263D9()) {
         this.urdCiPZD();
      } else if (var1.rsDd50tfV().controlScheme() == DroneDefinition.ControlScheme.ibzfxic) {
         if (this.cd43 > 0) {
            this.cd43--;
         }

         this.oWdz8 = false;
         this.wuhB(var1);
      } else if (this.cd43 > 0) {
         this.cd43--;
         boolean var2 = !this.oWdz8;
         this.oWdz8 = true;
         this.v5suI(var1, var2);
      } else {
         this.oWdz8 = false;
         this.iUE1QRL1b = false;
         this.ibzfxic(var1);
      }
   }

   private static boolean v5suI(DroneDefinition.LoiterFlight var0) {
      return var0.n9lX8A() > 0.0F;
   }

   private void ibzfxic(DroneDefinition.LoiterFlight var1) {
      boolean var2 = true;
      boolean var3 = this.uQdlyiwB().zTT3nw6() || !v5suI(var1);
      TerminalMode var4 = var3 ? TerminalMode.LOITER : TerminalMode.DIVE;
      if (this.dOfFwvo()) {
         if (this.wBXn >= this.n9lX8A.wuhB()) {
            this.wBXn = this.n9lX8A.wuhB() - 1;
         }

         if (this.hOKMYHOaC == LoiteringMunitionEntity.Ol3TEDSog.wuhB && this.wBXn < this.n9lX8A.wuhB() - 1) {
            Waypoint var5 = this.n9lX8A.v5suI(this.wBXn);
            double var6 = var5.v5suI - this.getX();
            double var8 = var5.wuhB - this.getZ();
            double var10 = var6 * var6 + var8 * var8;
            double var12 = this.fvsAOg / Math.max(1.0E-4, Math.toRadians(var1.iS1JnZov()));
            double var14 = Math.max(16.0, var12 * 1.35);
            double var16 = -Math.sin(Math.toRadians(this.l2IWOAGO));
            double var18 = Math.cos(Math.toRadians(this.l2IWOAGO));
            boolean var20 = var16 * var6 + var18 * var8 < 0.0 && var10 < var12 * 2.5 * (var12 * 2.5);
            if (var10 < var14 * var14 || var20) {
               this.wBXn++;
            }
         }

         Waypoint var26 = this.n9lX8A.v5suI(this.wBXn);
         if (this.wBXn == 0) {
            this.we1GYY = this.nwFPfQ ? this.hXya : this.getX();
            this.pxmzA = this.nwFPfQ ? this.iY41Hgn0m : this.getZ();
            this.eJDY = var1.r6s7FL3();
         } else {
            Waypoint var28 = this.n9lX8A.v5suI(this.wBXn - 1);
            this.we1GYY = var28.v5suI;
            this.pxmzA = var28.wuhB;
            this.eJDY = var28.ibzfxic;
         }

         this.zHBAQgZP = var26.ibzfxic;
         this.ifpFOCaAs = var26.rsDd50tfV;
         this.iElAbEZe = var26.v5suI;
         this.sTrE = var26.wuhB;
         this.lfyfLwuw = var26.v5suI;
         this.wyNDWDB = var26.ibzfxic;
         this.iRWc2Pi = var26.wuhB;
         this.tpzIHm = true;
         var2 = this.wBXn >= this.n9lX8A.wuhB() - 1;
         var4 = this.n9lX8A.rsDd50tfV();
         if (var3 && (var4 == TerminalMode.DIVE || var4 == TerminalMode.DESTROY)) {
            var4 = TerminalMode.LOITER;
         }

         if (var2 && var4 == TerminalMode.LOITER && this.hOKMYHOaC == LoiteringMunitionEntity.Ol3TEDSog.wuhB) {
            double var29 = this.getX() - var26.v5suI;
            double var30 = this.getZ() - var26.wuhB;
            double var31 = Math.sqrt(var29 * var29 + var30 * var30);
            double var32 = var1.hOKMYHOaC() * Math.tan(Math.toRadians(var1.qlv6NKWnH())) / Math.max(this.fvsAOg, 0.2);
            double var33 = Math.min(var32, Math.toRadians(var1.iS1JnZov()));
            double var34 = this.fvsAOg / Math.max(1.0E-4, var33) * 1.2;
            double var35 = var26.dOfFwvo > 0.0 ? var26.dOfFwvo : var34;
            if (var31 < var35 * 2.5) {
               double var36 = Math.atan2(var30, var29) + 0.6;
               this.lfyfLwuw = var26.v5suI + var35 * Math.cos(var36);
               this.iRWc2Pi = var26.wuhB + var35 * Math.sin(var36);
            } else if (var26.dOfFwvo > 0.0) {
               double var37 = this.we1GYY - var26.v5suI;
               double var22 = this.pxmzA - var26.wuhB;
               double var24 = Math.sqrt(var37 * var37 + var22 * var22);
               if (var24 > 1.0E-6) {
                  this.lfyfLwuw = var26.v5suI + var35 * var37 / var24;
                  this.iRWc2Pi = var26.wuhB + var35 * var22 / var24;
               }
            }
         }
      }

      boolean var27 = !var3 && (!this.dOfFwvo() || var2 && (var4 == TerminalMode.DIVE || var4 == TerminalMode.DESTROY));
      this.v5suI(var1, var2, var4, var27, this.dOfFwvo(), this.dOfFwvo(), false);
   }

   private void wuhB(DroneDefinition.LoiterFlight var1) {
      this.noPhysics = true;
      if (!this.w7IgPx) {
         this.ibzfxic(var1);
      } else {
         if (this.lXZR >= 0) {
            Entity var2 = this.level().getEntity(this.lXZR);
            if (var2 != null && var2.isAlive()) {
               this.xgtm4P = var2.getX();
               this.iYcPD = var2.getY() + var2.getBbHeight() * 0.5;
               this.w1AKSl = var2.getZ();
            } else {
               this.lXZR = -1;
            }
         }

         if (!this.iUE1QRL1b) {
            this.ibzfxic(var1);
         } else {
            this.lfyfLwuw = this.xgtm4P;
            this.wyNDWDB = this.iYcPD;
            this.iRWc2Pi = this.w1AKSl;
            this.tpzIHm = true;
            if (this.hOKMYHOaC == LoiteringMunitionEntity.Ol3TEDSog.wuhB) {
               double var17 = this.lfyfLwuw - this.getX();
               double var4 = this.iRWc2Pi - this.getZ();
               double var6 = Math.sqrt(var17 * var17 + var4 * var4);
               double var8 = this.wyNDWDB - this.getY();
               float var10 = Mth.clamp(var1.rsDd50tfV().guidedAttackAngleDeg(), 1.0F, 89.0F);
               double var11 = Math.toDegrees(Math.atan2(-var8, Math.max(0.001, var6)));
               if (this.gHP6 == LoiteringMunitionEntity.KKeMvpuBgEY.v5suI) {
                  if (!(var11 <= var10)) {
                     double var13 = Math.toRadians(this.l2IWOAGO);
                     double var15 = Math.max(64.0, var6 * 2.0);
                     this.lfyfLwuw = this.getX() - Math.sin(var13) * var15;
                     this.iRWc2Pi = this.getZ() + Math.cos(var13) * var15;
                     this.tpzIHm = true;
                     this.v5suI(var1, false, TerminalMode.DIVE, false, false, false, false);
                     return;
                  }

                  this.gHP6 = LoiteringMunitionEntity.KKeMvpuBgEY.ibzfxic;
               } else if (var11 >= var10 || var6 < this.v5suI(var1, TerminalMode.DIVE)) {
                  this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.dOfFwvo;
               }
            }

            if (this.hOKMYHOaC != LoiteringMunitionEntity.Ol3TEDSog.dOfFwvo) {
               this.v5suI(var1, false, TerminalMode.DIVE, false, false, false, false);
            } else {
               this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.dOfFwvo;
               this.v5suI(var1, true, TerminalMode.DIVE, v5suI(var1), false, false, true);
            }
         }
      }
   }

   private void v5suI(DroneDefinition.LoiterFlight var1, boolean var2, TerminalMode var3, boolean var4, boolean var5, boolean var6, boolean var7) {
      this.mM7DsB9AR = var4;
      this.uKpZYj = var4;
      double var8 = this.lfyfLwuw - this.getX();
      double var10 = this.iRWc2Pi - this.getZ();
      double var12 = Math.sqrt(var8 * var8 + var10 * var10);
      if (var6 && this.hOKMYHOaC == LoiteringMunitionEntity.Ol3TEDSog.dOfFwvo && var2 && (var3 == TerminalMode.DIVE || var3 == TerminalMode.DESTROY)) {
         int var14 = this.v5suI(Types.MOTION_BLOCKING);
         this.wyNDWDB = var3 == TerminalMode.DESTROY ? var14 + 4 : var14;
      }

      double var40 = this.wyNDWDB - this.getY();
      int var16 = this.ibzfxic(Types.MOTION_BLOCKING_NO_LEAVES);
      double var17 = this.getY() - var16;
      boolean var24 = false;
      double var19;
      double var21;
      boolean var23;
      switch (this.hOKMYHOaC) {
         case v5suI:
            var19 = var1.jGhI2();
            var21 = var1.c1jHYPb4();
            var23 = false;
            if (--this.iS1JnZov <= 0) {
               this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.ibzfxic;
            }
            break;
         case ibzfxic:
            var19 = var1.wEJx3Itb();
            var21 = var1.iAKNPAj();
            var23 = false;
            var24 = true;
            if (var17 >= var1.r6s7FL3()) {
               this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.wuhB;
            }
            break;
         case wuhB:
            double var25 = var5 && this.ifpFOCaAs > 0.0 ? this.ifpFOCaAs : 1.0;
            var19 = var1.wEJx3Itb() * var25;
            if (var7 && this.tpzIHm) {
               var21 = Mth.clamp(Math.toDegrees(Math.atan2(var40, Math.max(0.001, var12))), -var1.iRWc2Pi(), var1.iRWc2Pi());
               var23 = true;
               var24 = false;
            } else {
               double var27;
               if (!Double.isNaN(this.labwdcQcK)) {
                  var27 = this.labwdcQcK;
               } else if (var5) {
                  double var29 = this.iElAbEZe - this.we1GYY;
                  double var31 = this.sTrE - this.pxmzA;
                  double var33 = var29 * var29 + var31 * var31;
                  double var35 = var33 <= 1.0E-6
                     ? 1.0
                     : Mth.clamp(((this.getX() - this.we1GYY) * var29 + (this.getZ() - this.pxmzA) * var31) / var33, 0.0, 1.0);
                  var27 = var16 + this.eJDY + (this.zHBAQgZP - this.eJDY) * var35;
               } else {
                  var27 = var16 + var1.r6s7FL3();
               }

               if (this.lFt1ckP > Double.NEGATIVE_INFINITY) {
                  var27 = Math.max(var27, this.lFt1ckP);
               }

               double var48 = var1.iAKNPAj();
               var21 = Mth.clamp((var27 - this.getY()) * var1.eJDY(), -var48, var48);
               var23 = true;
               var24 = true;
            }

            boolean var44 = var3 == TerminalMode.DIVE || var3 == TerminalMode.DESTROY;
            if (this.tpzIHm && var2 && var44 && var12 < this.v5suI(var1, var3)) {
               this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.dOfFwvo;
            }

            if (var17 < var1.sTrE()) {
               var21 = Math.max(var21, var1.iRWc2Pi());
            }
            break;
         default:
            var19 = var1.bAcdvq9();
            var21 = Mth.clamp(Math.toDegrees(Math.atan2(var40, Math.max(0.001, var12))), -var1.iRWc2Pi() * 2.0F, var1.iRWc2Pi() * 2.0F);
            var23 = true;
      }

      this.entityData.set(c1jHYPb4, this.hOKMYHOaC == LoiteringMunitionEntity.Ol3TEDSog.dOfFwvo);
      if (var24) {
         if (--this.qU1QioDiC <= 0) {
            this.lFt1ckP = this.qz71Ga();
            this.qU1QioDiC = var1.uQdlyiwB();
         }

         if (this.lFt1ckP > this.getY()) {
            var21 = Math.max(var21, Mth.clamp((this.lFt1ckP - this.getY()) * var1.eJDY(), 0.0, var1.iRWc2Pi()));
         }
      } else {
         this.lFt1ckP = Double.NEGATIVE_INFINITY;
      }

      if (var23 && this.tpzIHm && var12 > 0.5) {
         double var41 = Math.toDegrees(Math.atan2(-var8, var10));
         double var45 = Mth.wrapDegrees(var41 - this.l2IWOAGO);
         double var49 = Mth.clamp(var45 * var1.lfyfLwuw(), -var1.qlv6NKWnH(), var1.qlv6NKWnH());
         this.kNoG = v5suI(this.kNoG, var49, var1.wyNDWDB());
         double var51 = Math.toDegrees(var1.hOKMYHOaC() * Math.tan(Math.toRadians(this.kNoG)) / Math.max(this.fvsAOg, 0.2));
         var51 = Mth.clamp(var51, -var1.iS1JnZov(), var1.iS1JnZov());
         this.l2IWOAGO = Mth.wrapDegrees(this.l2IWOAGO + var51);
      } else {
         this.kNoG = v5suI(this.kNoG, 0.0, var1.wyNDWDB());
      }

      this.xYttSy = v5suI(this.xYttSy, var21, var1.tpzIHm());
      if (this.hOKMYHOaC == LoiteringMunitionEntity.Ol3TEDSog.v5suI) {
         double var42 = Mth.clamp(1.0 - (double)this.iS1JnZov / Math.max(1, var1.v8ZN()), 0.0, 1.0);
         double var46 = 1.0 - (1.0 - var42) * (1.0 - var42);
         this.fvsAOg = var1.uSUb() + (var1.jGhI2() - var1.uSUb()) * var46;
      } else {
         this.fvsAOg = v5suI(this.fvsAOg, var19, var1.rxYnaa());
      }

      double var43 = this.l2IWOAGO;
      double var47 = this.xYttSy;
      double var50 = this.kNoG;
      double var53 = Math.toRadians(var43);
      double var54 = Math.toRadians(var47);
      double var55 = Math.cos(var54);
      Vec3 var37 = new Vec3(-Math.sin(var53) * var55, Math.sin(var54), Math.cos(var53) * var55).scale(this.fvsAOg);
      super.setDeltaMovement(var37);
      this.setPos(this.getX() + var37.x, this.getY() + var37.y, this.getZ() + var37.z);
      this.oEuCBB();
      this.setYRot((float)var43);
      this.setXRot((float)(-var47));
      this.rsDd50tfV((float)var50);
      this.entityData.set(jGhI2, (float)Mth.clamp(this.kNoG / var1.qlv6NKWnH(), -1.0, 1.0));
      this.entityData.set(uSUb, (float)Mth.clamp(this.xYttSy / var1.iRWc2Pi(), -1.0, 1.0));
      double var38 = this.tpzIHm && var12 > 0.5 ? Mth.wrapDegrees(Math.toDegrees(Math.atan2(-var8, var10)) - this.l2IWOAGO) : 0.0;
      this.entityData.set(wEJx3Itb, (float)Mth.clamp(var38 / 30.0, -1.0, 1.0));
      this.v5suI(false);
      this.dZAuCP();
   }

   private void v5suI(boolean var1) {
      if (var1) {
         RemoteStateBroadcaster.v5suI(this);
      } else {
         RemoteStateBroadcaster.v5suI(this, this);
      }
   }

   private double qz71Ga() {
      DroneDefinition.LoiterFlight var1 = this.zQOhlhTZx();
      double var2 = Math.toRadians(this.l2IWOAGO);
      double var4 = -Math.sin(var2);
      double var6 = Math.cos(var2);
      double var8 = Double.NEGATIVE_INFINITY;

      for (float var13 : var1.ifpFOCaAs()) {
         int var14 = Mth.floor(this.getX() + var4 * var13);
         int var15 = Mth.floor(this.getZ() + var6 * var13);
         int var16 = LoadedChunks.ibzfxic(this.level(), var14, var15);
         if (var16 != Integer.MIN_VALUE && var16 > var8) {
            var8 = var16;
         }
      }

      return var8 == Double.NEGATIVE_INFINITY ? Double.NEGATIVE_INFINITY : var8 + var1.sTrE();
   }

   private int v5suI(Types var1) {
      int var2 = LoadedChunks.v5suI(this.level(), var1, Mth.floor(this.lfyfLwuw), Mth.floor(this.iRWc2Pi));
      if (var2 != Integer.MIN_VALUE) {
         this.uQdlyiwB = var2;
         return var2;
      } else {
         return this.uQdlyiwB != Integer.MIN_VALUE ? this.uQdlyiwB : Mth.floor(this.getY());
      }
   }

   private int ibzfxic(Types var1) {
      int var2 = LoadedChunks.v5suI(this.level(), var1, Mth.floor(this.getX()), Mth.floor(this.getZ()));
      if (var2 != Integer.MIN_VALUE) {
         this.ivDR = var2;
         return var2;
      } else {
         return this.ivDR != Integer.MIN_VALUE ? this.ivDR : Mth.floor(this.getY());
      }
   }

   private boolean ibzfxic(List<BoneBox> var1) {
      double var2 = this.getX();
      double var4 = this.getY();
      double var6 = this.getZ();
      boolean var8 = false;
      if (this.nTdR && !var1.isEmpty()) {
         double var9 = var2 - this.jfdtPQG;
         double var11 = var4 - this.urdCiPZD;
         double var13 = var6 - this.gQiZ;
         double var15 = Math.sqrt(var9 * var9 + var11 * var11 + var13 * var13);
         int var17 = Mth.clamp((int)Math.ceil(var15 / 0.5), 1, 8);

         for (int var18 = 1; var18 < var17 && !var8; var18++) {
            double var19 = (double)var18 / var17;
            var var21 = MunitionCollision.v5suI(
               var1,
               this.jfdtPQG + var9 * var19,
               this.urdCiPZD + var11 * var19,
               this.gQiZ + var13 * var19,
               (float)this.l2IWOAGO,
               (float)this.xYttSy,
               (float)this.kNoG
            );
            var8 = MunitionCollision.v5suI(this.level(), var21);
         }
      }

      this.jfdtPQG = var2;
      this.urdCiPZD = var4;
      this.gQiZ = var6;
      this.nTdR = true;
      return var8;
   }

   private void dZAuCP() {
      if (!this.zQOhlhTZx) {
         DroneDefinition.LoiterFlight var1 = this.zQOhlhTZx();
         float var2 = var1.we1GYY();
         float var3 = var1.pxmzA();
         float var4 = var1.mM7DsB9AR();
         boolean var5 = !this.uQdlyiwB().zTT3nw6() && v5suI(var1);
         var var6 = MunitionCollision.v5suI(this);
         boolean var7 = this.ibzfxic(var6);
         boolean var8 = (this.mM7DsB9AR || this.uKpZYj)
            && this.tpzIHm
            && this.getPosition(1.0F).distanceToSqr(this.lfyfLwuw, this.wyNDWDB, this.iRWc2Pi) <= (double)var4 * var4;
         if (!var8 && this.uKpZYj) {
            AABB var9 = MunitionCollision.ibzfxic(this, 1.0F);
            if (var9 == null) {
               var9 = this.getBoundingBox();
            }

            var8 = !this.level().getEntities(this, var9.inflate(var4), this::v5suI).isEmpty();
         }

         if (!var8
            && var5
            && this.hOKMYHOaC != LoiteringMunitionEntity.Ol3TEDSog.v5suI
            && this.fvsAOg > var3
            && LoadedChunks.v5suI(this.level(), Mth.floor(this.getX()), Mth.floor(this.getZ()))) {
            var8 = MunitionCollision.ibzfxic(this, 1.0F, var4);
         }

         if (!var8 && var5 && this.hOKMYHOaC == LoiteringMunitionEntity.Ol3TEDSog.dOfFwvo) {
            int var19 = LoadedChunks.ibzfxic(this.level(), Mth.floor(this.getX()), Mth.floor(this.getZ()));
            var8 = var19 != Integer.MIN_VALUE && this.getY() - var19 <= var4;
         }

         this.entityData.set(v8ZN, var8);
         if (this.mM7DsB9AR && this.tpzIHm && this.getPosition(1.0F).distanceToSqr(this.lfyfLwuw, this.wyNDWDB, this.iRWc2Pi) <= var2 * var2) {
            this.rsDd50tfV("proximity-to-target");
         } else {
            if (var5) {
               if (this.hOKMYHOaC != LoiteringMunitionEntity.Ol3TEDSog.v5suI
                  && this.fvsAOg > var3
                  && LoadedChunks.v5suI(this.level(), Mth.floor(this.getX()), Mth.floor(this.getZ()))
                  && (MunitionCollision.wuhB(this, 1.0F) || var7)) {
                  this.rsDd50tfV("block-obb-contact");
                  return;
               }

               if (this.hOKMYHOaC == LoiteringMunitionEntity.Ol3TEDSog.dOfFwvo) {
                  int var20 = LoadedChunks.ibzfxic(this.level(), Mth.floor(this.getX()), Mth.floor(this.getZ()));
                  if (var20 != Integer.MIN_VALUE && this.getY() <= var20) {
                     this.rsDd50tfV("terminal-ground-fallback");
                     return;
                  }
               }
            } else if (this.hOKMYHOaC != LoiteringMunitionEntity.Ol3TEDSog.v5suI
               && LoadedChunks.v5suI(this.level(), Mth.floor(this.getX()), Mth.floor(this.getZ()))
               && (MunitionCollision.wuhB(this, 1.0F) || var7)) {
               this.urdCiPZD();
               return;
            }

            if (this.uKpZYj) {
               AABB var21 = MunitionCollision.ibzfxic(this, 1.0F);
               if (var21 == null) {
                  var21 = this.getBoundingBox();
               }

               var var10 = this.level().getEntities(this, var21.inflate(0.3), this::v5suI);
               if (!var10.isEmpty()) {
                  var var11 = this.v5suI(1.0F);
                  boolean var12 = false;

                  for (Entity var14 : var10) {
                     boolean var15;
                     if (var11.isEmpty()) {
                        var15 = true;
                     } else {
                        var15 = false;
                        OrientedBox var16 = OrientedBox.v5suI(var14.getBoundingBox());

                        for (OrientedBox var18 : var11) {
                           if (var18.ibzfxic(var16)) {
                              var15 = true;
                              break;
                           }
                        }
                     }

                     if (var15) {
                        boolean var22 = this.ibzfxic(var14);
                        if (!var22) {
                           this.rsDd50tfV(var11.isEmpty() ? "entity-broadbox" : "entity-obb");
                           return;
                        }

                        var12 = true;
                     }
                  }

                  if (var12) {
                     this.kbDIB5();
                  }
               }
            }
         }
      }
   }

   private boolean v5suI(Entity var1) {
      return !var1.is(this)
         && (this.pWmAxmwe == null || !this.pWmAxmwe.equals(var1.getUUID()))
         && !(var1 instanceof RampEntity)
         && !(var1 instanceof DroppedBombEntity)
         && var1.isPickable()
         && !var1.isSpectator();
   }

   private boolean ibzfxic(Entity var1) {
      return var1 instanceof DroneEntity ? true : var1 instanceof Player && FPVMod.hasActiveDrone(var1.getUUID());
   }

   private void p9T0hGf() {
      if (!this.level().isClientSide()) {
         FPVMod.onMunitionRemoved(this.getId());
      }
   }

   private void i7EP1lKl() {
      this.rsDd50tfV("unknown");
   }

   private void rsDd50tfV(String var1) {
      this.v5suI(var1, this.position());
   }

   private void v5suI(String var1, Vec3 var2) {
      if (!this.zQOhlhTZx && !this.level().isClientSide()) {
         this.zQOhlhTZx = true;
         if (this.level() instanceof ServerLevel var3) {
            ShrapnelExplosion.holdChunks(var3, var2, 4);
         }

         this.p9T0hGf();
         this.entityData.set(zTT3nw6, true);
         this.be9wl = var2;
         Vec3 var5 = this.getDeltaMovement();
         this.nL8hNS4cc = var5.lengthSqr() > 0.0025 ? var5 : Vec3.ZERO;
         if (this.nL8hNS4cc.lengthSqr() > 1.0E-6) {
            Vec3 var6 = this.nL8hNS4cc.normalize();
            this.l2IWOAGO = Math.toDegrees(Math.atan2(-var6.x, var6.z));
            this.xYttSy = Math.toDegrees(Math.asin(Mth.clamp(var6.y, -1.0, 1.0)));
         }

         this.kNoG = 0.0;
         this.setYRot((float)this.l2IWOAGO);
         this.setXRot((float)(-this.xYttSy));
         this.rsDd50tfV(0.0F);
         this.e89G = Math.max(0.0, this.zQOhlhTZx().xYttSy());
         this.jy4YJZH = 3;
         this.v5suI(false);
      }
   }

   private void dReHaJ() {
      if (this.e89G > 0.0 && this.nL8hNS4cc.lengthSqr() > 1.0E-6) {
         this.setPos(this.getX() + this.nL8hNS4cc.x, this.getY() + this.nL8hNS4cc.y, this.getZ() + this.nL8hNS4cc.z);
         this.oEuCBB();
         this.e89G = this.e89G - this.nL8hNS4cc.length();
         this.v5suI(false);
      } else {
         this.v5suI(false);
         if (--this.jy4YJZH <= 0) {
            this.jm7PbrhC();
         }
      }
   }

   private void jm7PbrhC() {
      if (!this.level().isClientSide()) {
         final ServerLevel var1 = (ServerLevel)this.level();
         final Vec3 var2 = this.be9wl != null ? this.be9wl : this.position();
         final DroneDefinition.LoiterFlight var3 = this.zQOhlhTZx();
         final ProjectileDefinition var4 = ProjectileDefinitions.getOrDefault(var3.ivDR());
         final Vec3 var5 = this.getDeltaMovement().lengthSqr() > 1.0E-6 ? this.getDeltaMovement().normalize().scale(-1.0) : new Vec3(0.0, 1.0, 0.0);
         ResetSequence.v5suI(new ResetSequence.KKeMvpuBgEY() {
            @Override
            public void v5suI() {
               LoiteringMunitionEntity.this.nL8hNS4cc();
               LookaheadChunkLoader.v5suI(var1, LoiteringMunitionEntity.this.getUUID());
            }

            @Override
            public void ibzfxic() {
               LoiteringMunitionEntity.this.v5suI(true);
            }

            @Override
            public void wuhB() {
               var4.detonator().detonate(var1, var2, var3.n9lX8A(), var3.wBXn(), LoiteringMunitionEntity.this, true, null, var4.stats(), var4.id(), false);
            }

            @Override
            public void dOfFwvo() {
               ModNetwork.sendNear(var1, var2.x, var2.y, var2.z, ParticleSendRange.get(), new ProjectileBurstPacket(var4.id(), var2, var5));
            }

            @Override
            public void rsDd50tfV() {
               LoiteringMunitionEntity.this.discard();
            }
         });
      }
   }

   private boolean jfdtPQG() {
      return this.uQdlyiwB().iKXOcc() != DroneRole.RECON && this.zQOhlhTZx().n9lX8A() > 0.0;
   }

   private void urdCiPZD() {
      if (!this.qz71Ga && !this.zQOhlhTZx && !this.level().isClientSide()) {
         this.qz71Ga = true;
         this.entityData.set(bAcdvq9, true);
         Vec3 var1 = this.getDeltaMovement();
         double var2 = Math.sqrt(var1.x * var1.x + var1.z * var1.z);
         this.i7EP1lKl = Math.max(var2, this.zQOhlhTZx().wEJx3Itb());
         this.dZAuCP = new Vec3(var1.x, Math.min(var1.y, 0.0), var1.z);
         this.p9T0hGf = 0.0;
         this.v5suI(false);
      }
   }

   private void gQiZ() {
      this.noPhysics = true;
      var var1 = MunitionCollision.v5suI(this);
      if (var1.isEmpty()) {
         double var8 = Math.max(this.dZAuCP.y - this.pyHY5().gravity(), -this.pyHY5().maxFall());
         int var11 = this.ibzfxic(Types.MOTION_BLOCKING_NO_LEAVES);
         double var12 = Math.max(this.getY() + var8, var11);
         boolean var7 = var12 <= var11 && !this.dReHaJ;
         this.dZAuCP = new Vec3(this.dZAuCP.x * 0.9, var12 > var11 ? var8 : 0.0, this.dZAuCP.z * 0.9);
         this.setPos(this.getX() + this.dZAuCP.x, var12, this.getZ() + this.dZAuCP.z);
         this.oEuCBB();
         if (var7) {
            this.dReHaJ = true;
            this.entityData.set(zTT3nw6, true);
            if (this.jfdtPQG() && this.random.nextFloat() < 0.5F) {
               this.rsDd50tfV("impact");
               return;
            }
         }

         this.v5suI(false);
      } else {
         boolean var2 = this.nTdR();
         BoxTerrainCollision.Rdl0X5jeod var3 = new BoxTerrainCollision.Rdl0X5jeod(
            this.getX(),
            this.getY(),
            this.getZ(),
            this.xYttSy,
            this.kNoG,
            (float)this.l2IWOAGO,
            this.dZAuCP.x,
            this.dZAuCP.y,
            this.dZAuCP.z,
            this.p9T0hGf
         );
         this.v5suI(var3);
         BoxTerrainCollision.v5suI(this.level(), var1, var3);
         this.setPos(var3.v5suI, var3.ibzfxic, var3.wuhB);
         this.xYttSy = var3.dOfFwvo;
         this.kNoG = var3.rsDd50tfV;
         this.l2IWOAGO = Mth.wrapDegrees(var3.uSUb);
         this.dZAuCP = new Vec3(var3.jGhI2, var3.wEJx3Itb, var3.bAcdvq9);
         this.p9T0hGf = var3.c1jHYPb4;
         if (!var3.zTT3nw6) {
            double var4 = Math.sqrt(this.dZAuCP.x * this.dZAuCP.x + this.dZAuCP.z * this.dZAuCP.z);
            double var6 = Math.toDegrees(Math.atan2(this.dZAuCP.y, Math.max(0.05, var4)));
            var6 = Mth.clamp(var6, -this.pyHY5().glideMaxPitch(), this.pyHY5().glideMaxPitch());
            this.xYttSy = v5suI(this.xYttSy, var6, this.pyHY5().glideDiveRate());
            this.kNoG = v5suI(this.kNoG, 0.0, 1.5);
         } else if (var2 && var3.vmpmU && var3.v8ZN) {
            double var9 = 0.0;

            for (double var14 = 0.25; var14 <= 1.05; var14 += 0.25) {
               if (!BoxTerrainCollision.v5suI(
                  this.level(), var1, this.getX(), this.getY() + var14, this.getZ(), (float)this.l2IWOAGO, (float)this.xYttSy, (float)this.kNoG
               )) {
                  var9 = var14;
                  break;
               }
            }

            if (var9 > 0.0) {
               this.setPos(this.getX(), this.getY() + var9, this.getZ());
               this.dZAuCP = new Vec3(this.dZAuCP.x, 0.0, this.dZAuCP.z);
            }
         }

         super.setDeltaMovement(this.dZAuCP);
         this.setYRot((float)this.l2IWOAGO);
         this.setXRot((float)(-this.xYttSy));
         this.rsDd50tfV((float)this.kNoG);
         this.oEuCBB();
         if (var3.v8ZN && !this.dReHaJ) {
            this.dReHaJ = true;
            this.entityData.set(zTT3nw6, true);
            if (this.jfdtPQG() && this.random.nextFloat() < 0.5F) {
               this.rsDd50tfV("impact");
               return;
            }
         }

         if (!var3.b69s || var2) {
            this.jm7PbrhC = false;
            this.be9wl();
         } else if (!this.jm7PbrhC) {
            this.jm7PbrhC = true;
            this.nL8hNS4cc();
            if (this.level() instanceof ServerLevel var10) {
               LookaheadChunkLoader.v5suI(var10, this.getUUID());
            }
         }

         this.v5suI(false);
      }
   }

   private boolean nTdR() {
      return this.ibzfxic(true);
   }

   private boolean ibzfxic(boolean var1) {
      return this.v5suI(var1, this.pyHY5().shoveSpeed());
   }

   private boolean v5suI(boolean var1, double var2) {
      var var4 = this.v5suI(1.0F);
      if (var4.isEmpty()) {
         return false;
      }

      AABB var5 = MunitionCollision.ibzfxic(this, 1.0F);
      if (var5 == null) {
         return false;
      }

      var var6 = this.level().getEntitiesOfClass(Player.class, var5.inflate(0.5), var1x -> (!var1 || var1x.isShiftKeyDown()) && !var1x.isSpectator());
      if (var6.isEmpty()) {
         return false;
      }

      double var7 = 0.0;
      double var9 = 0.0;
      double var11 = 0.0;

      for (OrientedBox var14 : var4) {
         var7 += var14.jGhI2().x;
         var9 += var14.jGhI2().y;
         var11 += var14.jGhI2().z;
      }

      var7 /= var4.size();
      var9 /= var4.size();
      var11 /= var4.size();
      boolean var50 = false;

      for (Player var15 : var6) {
         Vec3 var16 = var15.getLookAngle();
         double var17 = var16.x;
         double var19 = var16.z;
         double var21 = Math.sqrt(var17 * var17 + var19 * var19);
         if (!(var21 < 1.0E-4)) {
            var17 /= var21;
            var19 /= var21;
            Vec3 var23 = new Vec3(var15.getX(), var9, var15.getZ());
            Vec3 var24 = null;
            double var25 = Double.MAX_VALUE;
            double var27 = 1.0;

            for (OrientedBox var30 : var4) {
               Vec3 var31 = var30.ibzfxic(var23);
               double var32 = var31.distanceToSqr(var23);
               if (var32 < var25) {
                  var25 = var32;
                  var24 = var31;
               }

               for (Vec3 var37 : var30.rsDd50tfV()) {
                  double var38 = var37.x - var7;
                  double var40 = var37.z - var11;
                  var27 = Math.max(var27, var38 * var38 + var40 * var40);
               }
            }

            if (var24 != null) {
               double var54 = var24.x - var15.getX();
               double var55 = var24.z - var15.getZ();
               double var33 = Math.sqrt(var54 * var54 + var55 * var55);
               if (!(var33 > this.pyHY5().shoveReach()) && (!(var33 > 1.0E-4) || !((var17 * var54 + var19 * var55) / var33 < -0.1))) {
                  var50 = true;
                  double var56 = var24.x - var7;
                  double var57 = var24.z - var11;
                  double var39 = Math.sqrt(var27);
                  double var41 = this.dZAuCP.x * var17 + this.dZAuCP.z * var19;
                  double var43 = var2 - var41;
                  if (!(var43 <= 0.0)) {
                     this.dZAuCP = this.dZAuCP.add(var17 * var43, 0.0, var19 * var43);
                     double var45 = (var56 * var19 - var57 * var17) / var39;
                     this.p9T0hGf = this.p9T0hGf + this.pyHY5().shoveYawGain() * var43 * var45;
                  }
               }
            }
         }
      }

      return var50;
   }

   private void wEJx3Itb(Player var1) {
      Vec3 var2 = var1.getLookAngle();
      double var3 = Math.sqrt(var2.x * var2.x + var2.z * var2.z);
      if (!(var3 < 1.0E-4)) {
         double var5 = var2.x / var3;
         double var7 = var2.z / var3;
         this.dZAuCP = new Vec3(var5 * this.pyHY5().meleeKnockSpeed(), 0.0, var7 * this.pyHY5().meleeKnockSpeed());
         var var9 = this.v5suI(1.0F);
         if (!var9.isEmpty()) {
            double var10 = 0.0;
            double var12 = 0.0;
            double var14 = 0.0;

            for (OrientedBox var17 : var9) {
               var10 += var17.jGhI2().x;
               var12 += var17.jGhI2().y;
               var14 += var17.jGhI2().z;
            }

            var10 /= var9.size();
            var12 /= var9.size();
            var14 /= var9.size();
            Vec3 var38 = new Vec3(var1.getX(), var12, var1.getZ());
            Vec3 var39 = null;
            double var18 = Double.MAX_VALUE;
            double var20 = 1.0;

            for (OrientedBox var23 : var9) {
               Vec3 var24 = var23.ibzfxic(var38);
               double var25 = var24.distanceToSqr(var38);
               if (var25 < var18) {
                  var18 = var25;
                  var39 = var24;
               }

               for (Vec3 var30 : var23.rsDd50tfV()) {
                  double var31 = var30.x - var10;
                  double var33 = var30.z - var14;
                  var20 = Math.max(var20, var31 * var31 + var33 * var33);
               }
            }

            if (var39 != null) {
               double var40 = var39.x - var10;
               double var41 = var39.z - var14;
               double var26 = var40 * var7 - var41 * var5;
               double var42 = Math.sqrt(var20);
               this.p9T0hGf = this.p9T0hGf + this.pyHY5().meleeYawGain() * (var26 / var42);
            }
         }
      }
   }

   private static double v5suI(double var0, double var2, double var4) {
      return var0 < var2 ? Math.min(var0 + var4, var2) : Math.max(var0 - var4, var2);
   }

   private static double v5suI(double var0, double var2) {
      double var4 = Mth.clamp(var2, 0.0, 1.0);
      return var4 * var0 * var0 * var0 + (1.0 - var4) * var0;
   }

   private void be9wl() {
      if (this.level() instanceof ServerLevel var1) {
         int var8 = this.jy4YJZH() ? Math.max(this.zQOhlhTZx().uKpZYj(), 6) : this.zQOhlhTZx().uKpZYj();
         ChunkPos var3 = ChunkPos.containing(this.blockPosition());
         if (!var3.equals(this.srOgYIAu) || var8 != this.kbDIB5) {
            for (int var4 = -var8; var4 <= var8; var4++) {
               for (int var5 = -var8; var5 <= var8; var5++) {
                  DroneChunkTickets.forceChunk(var1, var3.x() + var4, var3.z() + var5, true, true);
               }
            }

            if (this.srOgYIAu != null) {
               for (int var9 = -this.kbDIB5; var9 <= this.kbDIB5; var9++) {
                  for (int var10 = -this.kbDIB5; var10 <= this.kbDIB5; var10++) {
                     int var6 = this.srOgYIAu.x() + var9;
                     int var7 = this.srOgYIAu.z() + var10;
                     if (Math.abs(var6 - var3.x()) > var8 || Math.abs(var7 - var3.z()) > var8) {
                        DroneChunkTickets.forceChunk(var1, var6, var7, false, true);
                     }
                  }
               }
            }

            this.srOgYIAu = var3;
            this.kbDIB5 = var8;
         }
      }
   }

   private void nL8hNS4cc() {
      if (this.srOgYIAu != null && this.level() instanceof ServerLevel var1) {
         for (int var4 = -this.kbDIB5; var4 <= this.kbDIB5; var4++) {
            for (int var3 = -this.kbDIB5; var3 <= this.kbDIB5; var3++) {
               DroneChunkTickets.forceChunk(var1, this.srOgYIAu.x() + var4, this.srOgYIAu.z() + var3, false, true);
            }
         }

         this.srOgYIAu = null;
         this.kbDIB5 = 0;
      }
   }

   @Nullable
   private ServerPlayer e89G() {
      return this.pWmAxmwe != null && this.level() instanceof ServerLevel var1 ? var1.getServer().getPlayerList().getPlayer(this.pWmAxmwe) : null;
   }

   private boolean jy4YJZH() {
      return this.e89G() != null;
   }

   private void qJ6kALs() {
      if (this.level() instanceof ServerLevel var1) {
         ServerPlayer var12 = this.e89G();
         if (var12 != null) {
            if (++this.siD3Qkq >= 10) {
               this.siD3Qkq = 0;
               int var3 = this.blockPosition().getX() >> 4;
               int var4 = this.blockPosition().getZ() >> 4;
               int var5 = 0;

               for (int var6 = -6; var6 <= 6 && var5 < 12; var6++) {
                  for (int var7 = -6; var7 <= 6 && var5 < 12; var7++) {
                     int var8 = var3 + var6;
                     int var9 = var4 + var7;
                     long var10 = ChunkPos.pack(var8, var9);
                     if (!this.i4g8OuWg.contains(var10) && var1.hasChunk(var8, var9)) {
                        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var12), this.v5suI(var1, var8, var9));
                        this.i4g8OuWg.add(var10);
                        var5++;
                     }
                  }
               }
            }
         }
      }
   }

   private void pWmAxmwe() {
      if (this.level() instanceof ServerLevel) {
         ServerPlayer var1 = this.e89G();
         if (var1 != null) {
            if (++this.d7g0AQ >= 4) {
               this.d7g0AQ = 0;
               this.kNkq = false;
               Vec3 var2 = this.nwFPfQ ? new Vec3(this.hXya, this.wogVpI, this.iY41Hgn0m) : null;
               ModNetwork.CHANNEL
                  .send(
                     PacketDistributor.PLAYER.with(() -> var1),
                     ReconMarkPacket.v5suI(
                        this.getId(),
                        this.wEJx3Itb(),
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        this.level().dimension().identifier(),
                        this.n9lX8A,
                        var2,
                        (float)this.we1GYY(),
                        this.wBXn
                     )
                  );
            }
         }
      }
   }

   private void srOgYIAu() {
      if (!this.kNkq && this.level() instanceof ServerLevel) {
         ServerPlayer var1 = this.e89G();
         if (var1 != null) {
            this.kNkq = true;
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var1), ReconMarkPacket.v5suI(this.getId()));
         }
      }
   }

   private ReconTilePacket v5suI(ServerLevel var1, int var2, int var3) {
      int var4 = var2 << 4;
      int var5 = var3 << 4;
      byte var6 = 17;
      short[] var7 = new short[289];
      int[] var8 = new int[289];
      MutableBlockPos var9 = new MutableBlockPos();

      for (int var10 = -1; var10 < 16; var10++) {
         for (int var11 = -1; var11 < 16; var11++) {
            int var12 = var4 + var11;
            int var13 = var5 + var10;
            if (!var1.hasChunk(var12 >> 4, var13 >> 4)) {
               var12 = var4 + Math.max(0, Math.min(15, var11));
               var13 = var5 + Math.max(0, Math.min(15, var10));
            }

            int var14 = TerrainProbe.v5suI(var1, var12, var13);
            BlockState var15 = var1.getBlockState(var9.set(var12, var14, var13));
            int var16 = TerrainProbe.v5suI(var15) ? var14 - 1 : var14;
            int var17 = (var10 + 1) * var6 + var11 + 1;
            var7[var17] = (short)var16;
            var8[var17] = var14;
         }
      }

      int[] var18 = new int[256];
      int[] var19 = new int[256];
      Registry var20 = var1.registryAccess().lookupOrThrow(Registries.BIOME);

      for (int var21 = 0; var21 < 16; var21++) {
         for (int var22 = 0; var22 < 16; var22++) {
            int var23 = var8[(var21 + 1) * var6 + var22 + 1];
            var9.set(var4 + var22, var23, var5 + var21);
            var18[var21 * 16 + var22] = Block.getId(var1.getBlockState(var9));
            var19[var21 * 16 + var22] = var20.getId((Biome)var1.getBiome(var9).value());
         }
      }

      return new ReconTilePacket(var1.dimension().identifier(), var2, var3, var7, var18, var19);
   }

   public void remove(RemovalReason var1) {
      this.p9T0hGf();
      this.nL8hNS4cc();
      this.srOgYIAu();
      super.remove(var1);
   }

   @Override
   public boolean hurtServer(ServerLevel hurtLevel, DamageSource var1, float var2) {
      if (!this.level().isClientSide() && !this.isRemoved()) {
         var2 = this.uQdlyiwB().v5suI(var1, var2);
         String var3 = this.v5suI(ProjectileHitPoints.v5suI(this.getId()));
         IvHitInfo.KKeMvpuBgEY var4 = null;
         if (var3 == null) {
            var4 = IvHitInfo.v5suI(this.getId());
            if (var4 != null) {
               var3 = this.v5suI(var4.dOfFwvo());
               if (var3 == null) {
                  var3 = this.v5suI(var4.dOfFwvo(), 1.5);
               }
            }
         }

         this.uSUb(var3);
         if (!this.uj1PcjLg) {
            if (this.qz71Ga && var1.getDirectEntity() instanceof Player var16) {
               if (var16.isShiftKeyDown()) {
                  this.kNWIu();
                  return true;
               } else if (this.dReHaJ) {
                  this.wEJx3Itb(var16);
                  this.jm7PbrhC = false;
                  this.v5suI(false);
                  return true;
               } else if (var16.distanceToSqr(this) <= 36.0) {
                  this.discard();
                  return true;
               } else {
                  return false;
               }
            } else {
               if (this.zQOhlhTZx || this.qz71Ga) {
                  return false;
               }

               if (this.v5suI(var1)) {
                  return false;
               }

               this.v5suI(var3, var2, var4, var1);
               return true;
            }
         } else if (var1.getDirectEntity() instanceof Player var5) {
            if (var5.isShiftKeyDown()) {
               this.kNWIu();
               return true;
            }

            var var17 = MunitionCollision.v5suI(this);
            if (!this.zgQgCA9 && this.uSUb(var5) && this.iElAbEZe() && !this.v5suI(var17)) {
               double var7 = Math.toRadians(this.l2IWOAGO);
               double var9 = -Math.sin(var7);
               double var11 = Math.cos(var7);
               double var13 = this.lvk8rWUGD().hitImpulse();
               this.dZAuCP = this.dZAuCP.add(var9 * var13, 0.0, var11 * var13);
               this.p9T0hGf = 0.0;
               this.rYlh();
            } else {
               this.wEJx3Itb(var5);
            }

            this.v5suI(false);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean v5suI(DamageSource var1, float var2) {
      if (!this.level().isClientSide() && !this.isRemoved()) {
         this.uSUb(this.v5suI(ProjectileHitPoints.v5suI(this.getId())));
         if (this.uj1PcjLg || this.zQOhlhTZx || this.qz71Ga) {
            return false;
         }

         if (this.v5suI(var1)) {
            return false;
         }

         this.kbDIB5();
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   private String v5suI(@Nullable Vec3 var1) {
      if (var1 == null) {
         return null;
      }

      var var2 = MunitionCollision.v5suI(this);
      var var3 = MunitionCollision.v5suI(this, 1.0F);

      for (int var4 = 0; var4 < var2.size() && var4 < var3.size(); var4++) {
         if (((OrientedBox)var3.get(var4)).wuhB(var1)) {
            return ((BoneBox)var2.get(var4)).rsDd50tfV();
         }
      }

      return null;
   }

   @Nullable
   private String v5suI(@Nullable Vec3 var1, double var2) {
      if (var1 == null) {
         return null;
      }

      var var4 = MunitionCollision.v5suI(this);
      var var5 = MunitionCollision.v5suI(this, 1.0F);
      String var6 = null;
      double var7 = var2 * var2;

      for (int var9 = 0; var9 < var4.size() && var9 < var5.size(); var9++) {
         double var10 = ((OrientedBox)var5.get(var9)).ibzfxic(var1).distanceToSqr(var1);
         if (var10 < var7) {
            var7 = var10;
            var6 = ((BoneBox)var4.get(var9)).rsDd50tfV();
         }
      }

      return var6;
   }

   private void uSUb(@Nullable String var1) {
      if (var1 != null && this.uKpZYj() && this.l2IWOAGO()) {
         byte var2 = this.fvsAOg();
         if (var2 != 0) {
            var var3 = this.uQdlyiwB().jt9oI60ST().rsDd50tfV();
            int var4 = v5suI(var3, var1);
            if (var4 >= 0 && BitFlags.v5suI(var2, var4)) {
               this.dOfFwvo(var4);
               this.level().playSound(null, this, SoundEvents.GENERIC_SMALL_FALL, SoundSource.HOSTILE, 0.8F, 1.4F);
               this.v5suI(false);
            }
         }
      }
   }

   private static int v5suI(List<PayloadStation> var0, String var1) {
      for (int var2 = 0; var2 < var0.size(); var2++) {
         if (((PayloadStation)var0.get(var2)).ibzfxic().equals(var1)) {
            return var2;
         }
      }

      return -1;
   }

   private boolean v5suI(DamageSource var1) {
      return var1.getEntity() == this || var1.getDirectEntity() == this;
   }

   private void kbDIB5() {
      this.urdCiPZD();
   }

   private void v5suI(@Nullable String var1, float var2, IvHitInfo.@Nullable KKeMvpuBgEY var3, DamageSource var4) {
      DroneDefinition var5 = this.uQdlyiwB();
      DroneDefinition.LoiterFlight var6 = this.zQOhlhTZx();
      if (!var5.wuhB()) {
         if (var6.ibzfxic() && var1 != null) {
            boolean var11 = !var1.equals(var6.zQOhlhTZx());
            if (!var11) {
               float var13 = var6.lvk8rWUGD();
               float var9 = this.random.nextFloat();
               boolean var10 = var9 < var13;
               if (var10) {
                  this.rsDd50tfV("damage");
               }
            } else {
               this.lvk8rWUGD++;
               int var12 = var6.pyHY5();
               if (this.lvk8rWUGD >= var12) {
                  this.urdCiPZD();
               }
            }
         } else {
            this.kbDIB5();
         }
      } else {
         boolean var7 = var1 != null && var6.ibzfxic() && !var1.equals(var6.zQOhlhTZx());
         LoiteringMunitionEntity.Rdl0X5jeod var8 = this.v5suI(var5.qU1QioDiC(), var7, var2, var3, var1 != null ? var1 : "unresolved", var4);
         if (this.rOIp0p7()) {
            this.v5suI(var1, var7, var8, var4);
         }

         switch (var8) {
            case wuhB:
               this.rsDd50tfV("damage");
               break;
            case ibzfxic:
               this.urdCiPZD();
            case v5suI:
         }
      }
   }

   private void v5suI(@Nullable String var1, boolean var2, LoiteringMunitionEntity.Rdl0X5jeod var3, DamageSource var4) {
      if (var3 != LoiteringMunitionEntity.Rdl0X5jeod.v5suI && this.level() instanceof ServerLevel var5) {
         DelayedMunitionSpawns.schedule(var5, this.rOIp0p7, this.iKXOcc, this.jt9oI60ST, this.oEuCBB, this.wEJx3Itb());
      }
   }

   private LoiteringMunitionEntity.Rdl0X5jeod v5suI(
      DroneDefinition.DamageZones var1, boolean var2, float var3, IvHitInfo.@Nullable KKeMvpuBgEY var4, String var5, DamageSource var6
   ) {
      if (this.pyHY5 < 0) {
         this.pyHY5 = var1.type().hp();
      }

      if (this.idXga < 0) {
         this.idXga = var1.entity().hp();
      }

      float var7 = var3;
      if (var4 != null) {
         var var8 = WeaponDamageRules.v5suI(var4);
         if (var8.isPresent()) {
            var7 = ((WeaponDamageRule)var8.get()).ibzfxic(var4);
         }
      }

      if (var2) {
         this.idXga = this.idXga - Math.round(var7);
         this.v5suI(var6, "PERIPHERAL bone='" + var5 + "'", var7, this.idXga, var1.entity().hp());
         return this.idXga <= 0 ? LoiteringMunitionEntity.Rdl0X5jeod.ibzfxic : LoiteringMunitionEntity.Rdl0X5jeod.v5suI;
      } else {
         this.pyHY5 = this.pyHY5 - Math.round(var7);
         this.v5suI(var6, "CORE bone='" + var5 + "'", var7, this.pyHY5, var1.type().hp());
         if (this.pyHY5 > 0) {
            return LoiteringMunitionEntity.Rdl0X5jeod.v5suI;
         } else {
            return this.random.nextFloat() < var1.type().explodeChance()
               ? LoiteringMunitionEntity.Rdl0X5jeod.wuhB
               : LoiteringMunitionEntity.Rdl0X5jeod.ibzfxic;
         }
      }
   }

   private void v5suI(DamageSource var1, String var2, float var3, int var4, int var5) {
   }

   @Nullable
   private static ServerPlayer ibzfxic(DamageSource var0) {
      if (var0.getEntity() instanceof ServerPlayer var3) {
         return var3;
      } else {
         return var0.getDirectEntity() instanceof ServerPlayer var1 ? var1 : null;
      }
   }

   protected void readAdditionalSaveData(ValueInput var1) {
      this.ibzfxic(var1.getString("DroneType").isPresent() ? var1.getStringOr("DroneType", "") : "fp1");
      this.hOKMYHOaC = LoiteringMunitionEntity.Ol3TEDSog.values()[Mth.clamp(
         var1.getIntOr("Phase", 0), 0, LoiteringMunitionEntity.Ol3TEDSog.values().length - 1
      )];
      this.iS1JnZov = var1.getIntOr("BoostLeft", 0);
      this.l2IWOAGO = var1.getDoubleOr("Heading", 0.0);
      this.xYttSy = var1.getDoubleOr("Pitch", 0.0);
      this.kNoG = var1.getDoubleOr("Roll", 0.0);
      this.fvsAOg = var1.getDoubleOr("Speed", 0.0);
      this.tpzIHm = var1.getBooleanOr("HasTarget", false);
      this.lfyfLwuw = var1.getDoubleOr("TargetX", 0.0);
      this.wyNDWDB = var1.getDoubleOr("TargetY", 0.0);
      this.iRWc2Pi = var1.getDoubleOr("TargetZ", 0.0);
      var1.read("Owner", UUIDUtil.CODEC).ifPresent(this::ibzfxic);

      this.qz71Ga = var1.getBooleanOr("Crashing", false);
      this.lvk8rWUGD = var1.getIntOr("PeripheralHits", 0);
      if (var1.getInt("CoreZoneHp").isPresent()) {
         this.pyHY5 = var1.getIntOr("CoreZoneHp", 0);
      }

      if (var1.getInt("PeripheralZoneHp").isPresent()) {
         this.idXga = var1.getIntOr("PeripheralZoneHp", 0);
      }

      if (var1.getInt("BroadPhaseCellCount").isPresent()) {
         this.rcg4HTTux = var1.getIntOr("BroadPhaseCellCount", 0);
      }

      this.dReHaJ = var1.getBooleanOr("CrashLanded", false);
      this.jm7PbrhC = var1.getBooleanOr("CrashSettled", false);
      this.i7EP1lKl = var1.getDoubleOr("CrashFwdSpeed", 0.0);
      this.uj1PcjLg = var1.getBooleanOr("Prelaunch", false);
      this.zgQgCA9 = var1.getBooleanOr("GroundStarted", false);
      this.xogV5KBhg = var1.getDoubleOr("GroundPushDist", 0.0);
      this.wTo0I = var1.getBooleanOr("HasLaunchTarget", false);
      this.rYlh = var1.getDoubleOr("LaunchTargetX", 0.0);
      this.sZzOK7T = var1.getDoubleOr("LaunchTargetY", 0.0);
      this.kNWIu = var1.getDoubleOr("LaunchTargetZ", 0.0);
      this.nwFPfQ = var1.getBooleanOr("HasLaunchOrigin", false);
      this.hXya = var1.getDoubleOr("LaunchOriginX", 0.0);
      this.wogVpI = var1.getDoubleOr("LaunchOriginY", 0.0);
      this.iY41Hgn0m = var1.getDoubleOr("LaunchOriginZ", 0.0);
      var routeTag = var1.read("Route", CompoundTag.CODEC);
      if (routeTag.isPresent()) {
         FlightRoute var2 = FlightRoute.v5suI(routeTag.get());
         this.n9lX8A = var2.dOfFwvo() ? null : var2;
      } else {
         this.n9lX8A = null;
      }

      this.wBXn = var1.getIntOr("RouteIndex", 0);
      this.v5suI(var1.getDoubleOr("Fuel", 0.0));
      this.v5suI(var1.getByteOr("RocketPodInstalledMask", (byte)0));
      this.ibzfxic(var1.getByteOr("RocketsLoaded", (byte)0));
      this.h7dk9 = var1.getInt("RocketFireCursor").isPresent() ? var1.getIntOr("RocketFireCursor", 0) : -1;
      this.entityData.set(bAcdvq9, this.qz71Ga);
      this.entityData.set(zTT3nw6, this.dReHaJ);
      if (this.uj1PcjLg) {
         this.entityData.set(zTT3nw6, !this.zgQgCA9);
      }

      this.entityData.set(vmpmU, this.uj1PcjLg);
      this.rsDd50tfV((float)this.kNoG);
      if (this.qz71Ga || this.uj1PcjLg) {
         this.dZAuCP = this.getDeltaMovement();
      }
   }

   protected void addAdditionalSaveData(ValueOutput var1) {
      var1.putString("DroneType", this.wEJx3Itb());
      var1.putInt("Phase", this.hOKMYHOaC.ordinal());
      var1.putInt("BoostLeft", this.iS1JnZov);
      var1.putDouble("Heading", this.l2IWOAGO);
      var1.putDouble("Pitch", this.xYttSy);
      var1.putDouble("Roll", this.kNoG);
      var1.putDouble("Speed", this.fvsAOg);
      var1.putBoolean("HasTarget", this.tpzIHm);
      var1.putDouble("TargetX", this.lfyfLwuw);
      var1.putDouble("TargetY", this.wyNDWDB);
      var1.putDouble("TargetZ", this.iRWc2Pi);
      if (this.pWmAxmwe != null) {
         var1.store("Owner", UUIDUtil.CODEC, this.pWmAxmwe);
      }

      var1.putBoolean("Crashing", this.qz71Ga);
      var1.putInt("PeripheralHits", this.lvk8rWUGD);
      var1.putInt("CoreZoneHp", this.pyHY5);
      var1.putInt("PeripheralZoneHp", this.idXga);
      var1.putInt("BroadPhaseCellCount", this.rcg4HTTux);
      var1.putBoolean("CrashLanded", this.dReHaJ);
      var1.putBoolean("CrashSettled", this.jm7PbrhC);
      var1.putDouble("CrashFwdSpeed", this.i7EP1lKl);
      var1.putBoolean("Prelaunch", this.uj1PcjLg);
      var1.putBoolean("GroundStarted", this.zgQgCA9);
      var1.putDouble("GroundPushDist", this.xogV5KBhg);
      var1.putBoolean("HasLaunchTarget", this.wTo0I);
      var1.putDouble("LaunchTargetX", this.rYlh);
      var1.putDouble("LaunchTargetY", this.sZzOK7T);
      var1.putDouble("LaunchTargetZ", this.kNWIu);
      var1.putBoolean("HasLaunchOrigin", this.nwFPfQ);
      if (this.nwFPfQ) {
         var1.putDouble("LaunchOriginX", this.hXya);
         var1.putDouble("LaunchOriginY", this.wogVpI);
         var1.putDouble("LaunchOriginZ", this.iY41Hgn0m);
      }

      if (this.n9lX8A != null) {
         var1.store("Route", CompoundTag.CODEC, this.n9lX8A.uSUb());
         var1.putInt("RouteIndex", this.wBXn);
      }

      var1.putDouble("Fuel", this.tLYLf);
      var1.putByte("RocketPodInstalledMask", this.xYttSy());
      var1.putByte("RocketsLoaded", this.fvsAOg());
      var1.putInt("RocketFireCursor", this.h7dk9);
   }

   private enum KKeMvpuBgEY {
      v5suI,
      ibzfxic;
   }

   private enum Ol3TEDSog {
      v5suI,
      ibzfxic,
      wuhB,
      dOfFwvo;
   }

   private enum Rdl0X5jeod {
      v5suI,
      ibzfxic,
      wuhB;
   }
}
