package com.fpvdrone.gui.screen;

import com.fpvdrone.FPVClientMod;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.DroneDefinitions;
import com.fpvdrone.data.RouteSpeed;
import com.fpvdrone.data.TerminalMode;

import com.fpvdrone.client.map.ReconMarks;
import com.fpvdrone.entity.LoiteringMunitionEntity;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.RouteAssignPacket;
import com.fpvdrone.registry.ModItems;
import com.fpvdrone.data.Waypoint;
import com.fpvdrone.client.map.ReconLayer;
import com.fpvdrone.client.MunitionClientState;
import com.fpvdrone.client.map.MapTerrainSampler;
import com.fpvdrone.data.FlightRoute;
import com.fpvdrone.client.map.ReconStore;
import com.fpvdrone.client.thermal.ThermalCameraState;
import com.fpvdrone.client.map.MapIcons;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public class TacticalMapScreen extends Screen {
   /** 1.1.8 drew hover glows with blendFunc(SRC_ALPHA, ONE); 26.3 needs a pipeline for that blend. */
   private static final com.mojang.renderpearl.api.pipeline.RenderPipeline GUI_TEXTURED_GLOW = RenderPipelines.register(
      com.mojang.renderpearl.api.pipeline.RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
         .withLocation(Identifier.fromNamespaceAndPath("fpvdrone", "pipeline/gui_textured_glow"))
         .withColorTargetState(new com.mojang.renderpearl.api.pipeline.ColorTargetState(com.mojang.renderpearl.api.pipeline.BlendFunction.LIGHTNING))
         .build()
   );
   private static final double v5suI = 0.05;
   private static final double ibzfxic = 12.0;
   private static final int wuhB = 22;
   private static final int dOfFwvo = 30;
   private static final double rsDd50tfV = 5.0;
   private static final double uSUb = 2000.0;
   private static final double jGhI2 = 100.0;
   private static final double wEJx3Itb = 800.0;
   private static final int bAcdvq9 = 64;
   private static final double c1jHYPb4 = DroneDefinition.LoiterFlight.v5suI.wEJx3Itb() * 20.0;
   private static final int v8ZN = -16777216;
   private static final int zTT3nw6 = -1;
   private static final int vmpmU = -1073741824;
   private static final int b69s = -1;
   private static final int cXHvq = -4671296;
   private static final int rxYnaa = -1;
   private static final int r6s7FL3 = -1;
   private static final Identifier iAKNPAj = MapIcons.uSUb;
   private static final Identifier qlv6NKWnH = MapIcons.jGhI2;
   private static final Identifier hOKMYHOaC = MapIcons.wEJx3Itb;
   private static final Identifier iS1JnZov = MapIcons.bAcdvq9;
   private static final double lfyfLwuw = 2.0;
   private static final int wyNDWDB = 16;
   private static final int iRWc2Pi = 8;
   private static final int tpzIHm = 2;
   private static final float eJDY = 12.0F;
   private static final float zHBAQgZP = 0.55F;
   private static final float we1GYY = 0.22F;
   private static final float pxmzA = 0.7F;
   private static final long iElAbEZe = 3000L;
   private int sTrE;
   private final boolean ifpFOCaAs;
   private String uQdlyiwB;
   private boolean ivDR;
   private boolean n9lX8A;
   private double wBXn;
   private double mM7DsB9AR;
   private double uKpZYj;
   private final float l2IWOAGO;
   private ItemStack xYttSy;
   private final ItemStack kNoG = new ItemStack((ItemLike)ModItems.RAMP.get());
   private double fvsAOg;
   private double lFt1ckP;
   private static double qU1QioDiC = -1.0;
   private final List<Waypoint> zQOhlhTZx = new ArrayList<>();
   private TerminalMode lvk8rWUGD = TerminalMode.DIVE;
   private double pyHY5 = 40.0;
   private RouteSpeed idXga = RouteSpeed.CRUISE;
   private boolean i263D9;
   private long rOIp0p7 = -1L;
   private final ReconLayer iKXOcc = ReconLayer.v5suI();
   private MapPopupMenu jt9oI60ST;
   private EditBox oEuCBB;
   private int uj1PcjLg = -1;
   private boolean zgQgCA9;
   private double qV33e;
   private double xogV5KBhg;
   private double rYlh;
   private Button sZzOK7T;
   private Button kNWIu;
   private Button wTo0I;
   private int hXya = -1;
   private float[] wogVpI = new float[0];
   private final Map<Integer, Float> iY41Hgn0m = new HashMap<>();
   private int nwFPfQ = -1;
   private float qz71Ga;
   private long dZAuCP;
   private double p9T0hGf;
   private double i7EP1lKl;
   private String dReHaJ;
   private long jm7PbrhC = -1L;
   private float jfdtPQG;
   private List<TacticalMapScreen.KKeMvpuBgEY> urdCiPZD;
   private boolean gQiZ;
   private boolean nTdR;
   private boolean be9wl;
   private static final int nL8hNS4cc = Integer.MIN_VALUE;

   public TacticalMapScreen(int var1, boolean var2, String var3, double var4, double var6, double var8, float var10) {
      super(Component.translatable("screen.fpvdrone.drone_map"));
      this.ivDR = false;
      this.n9lX8A = false;
      this.sTrE = var1;
      this.ifpFOCaAs = var2;
      this.uQdlyiwB = var3;
      this.wBXn = var4;
      this.mM7DsB9AR = var6;
      this.uKpZYj = var8;
      this.l2IWOAGO = var10;
      this.fvsAOg = var4;
      this.lFt1ckP = var8;
      this.xYttSy = new ItemStack(var2 ? (ItemLike)ModItems.RAMP.get() : (ItemLike)ModItems.LYTUI_DRONE.get());
   }

   public TacticalMapScreen() {
      super(Component.translatable("screen.fpvdrone.drone_map"));
      this.ivDR = true;
      this.n9lX8A = false;
      this.sTrE = -1;
      this.ifpFOCaAs = false;
      double var1 = 0.0;
      double var3 = 64.0;
      double var5 = 0.0;
      float var7 = 0.0F;
      if (Minecraft.getInstance().player != null) {
         var1 = Minecraft.getInstance().player.getX();
         var3 = Minecraft.getInstance().player.getY();
         var5 = Minecraft.getInstance().player.getZ();
         var7 = Minecraft.getInstance().player.getYRot();
      }

      if (MunitionClientState.n9lX8A()) {
         var1 = MunitionClientState.wBXn();
         var3 = MunitionClientState.mM7DsB9AR();
         var5 = MunitionClientState.uKpZYj();
         this.p9T0hGf = var1;
         this.i7EP1lKl = var5;
         this.dReHaJ = MunitionClientState.l2IWOAGO();
         this.jm7PbrhC = System.currentTimeMillis();
         double var8 = MunitionClientState.xYttSy();
         if (var8 > 0.0) {
            qU1QioDiC = var8;
         }

         MunitionClientState.kNoG();
      }

      this.wBXn = var1;
      this.mM7DsB9AR = var3;
      this.uKpZYj = var5;
      this.l2IWOAGO = var7;
      this.fvsAOg = var1;
      this.lFt1ckP = var5;
      this.xYttSy = ItemStack.EMPTY;
   }

   public TacticalMapScreen(int var1, String var2, double var3, double var5, double var7, float var9, TerminalMode var10) {
      super(Component.translatable("screen.fpvdrone.drone_map"));
      this.ivDR = false;
      this.n9lX8A = true;
      this.sTrE = var1;
      this.ifpFOCaAs = false;
      this.uQdlyiwB = var2;
      this.wBXn = var3;
      this.mM7DsB9AR = var5;
      this.uKpZYj = var7;
      this.l2IWOAGO = var9;
      this.fvsAOg = var3;
      this.lFt1ckP = var7;
      this.xYttSy = v5suI(var2);
      this.lvk8rWUGD = var10 != null ? var10 : TerminalMode.DIVE;
   }

   protected void init() {
      if (qU1QioDiC < 0.0) {
         qU1QioDiC = Mth.clamp((this.height - 22) / 160.0, 1.0, 10.0);
      }

      this.v5suI();
      this.wuhB();
   }

   private void v5suI() {
      if (!this.nTdR) {
         this.nTdR = true;
         ThermalCameraState var1 = ThermalCameraState.getInstance();
         if (var1.isEnabled()) {
            var1.toggle();
            this.be9wl = true;
         }
      }
   }

   private void ibzfxic() {
      if (this.be9wl) {
         this.be9wl = false;
         ThermalCameraState var1 = ThermalCameraState.getInstance();
         if (!var1.isEnabled()) {
            var1.toggle();
         }
      }
   }

   private void wuhB() {
      this.clearWidgets();
      this.kNWIu = null;
      this.wTo0I = null;
      this.sZzOK7T = null;
      if (this.ivDR) {
         byte var7 = 16;
         byte var12 = 104;
         byte var13 = 116;
         byte var14 = 74;
         byte var15 = 4;
         int var16 = Math.max(4, (this.width - (var12 + var15 + var13 + var15 + var14)) / 2);
         this.kNWIu = Button.builder(Component.translatable("gui.fpvdrone.map.take_control"), var1x -> this.vmpmU())
            .bounds(var16, 3, var12, var7)
            .build();
         this.kNWIu.active = false;
         this.addRenderableWidget(this.kNWIu);
         var16 += var12 + var15;
         this.wTo0I = Button.builder(Component.translatable("gui.fpvdrone.map.first_person"), var1x -> this.cXHvq())
            .bounds(var16, 3, var13, var7)
            .build();
         this.wTo0I.active = false;
         this.addRenderableWidget(this.wTo0I);
         var16 += var13 + var15;
         this.addRenderableWidget(
            Button.builder(Component.translatable("gui.fpvdrone.map.refresh"), var0 -> MapTerrainSampler.v5suI()).bounds(var16, 3, var14, var7).build()
         );
      } else {
         short var1 = 286;
         int var2 = Math.max(4, (this.width - var1) / 2);
         byte var3 = 3;
         byte var4 = 16;
         MutableComponent var5 = this.n9lX8A
            ? Component.translatable("gui.fpvdrone.map.apply")
            : (this.ifpFOCaAs ? Component.translatable("gui.fpvdrone.map.launch") : Component.translatable("gui.fpvdrone.map.send"));
         this.addRenderableWidget(Button.builder(var5, var1x -> this.lfyfLwuw()).bounds(var2, var3, 50, var4).build());
         var2 += 52;
         MutableComponent var6 = this.n9lX8A ? Component.translatable("gui.fpvdrone.map.cancel") : Component.translatable("gui.fpvdrone.map.undo");
         this.addRenderableWidget(Button.builder(var6, var1x -> this.dOfFwvo()).bounds(var2, var3, 40, var4).build());
         var2 += 42;
         this.addRenderableWidget(
            Button.builder(Component.translatable("gui.fpvdrone.map.clear"), var1x -> this.zQOhlhTZx.clear()).bounds(var2, var3, 40, var4).build()
         );
         var2 += 42;
         if (this.rsDd50tfV()) {
            this.lvk8rWUGD = TerminalMode.LOITER;
         }

         this.sZzOK7T = Button.builder(this.uSUb(), var1x -> this.jGhI2()).bounds(var2, var3, 96, var4).build();
         this.sZzOK7T.active = !this.rsDd50tfV();
         this.addRenderableWidget(this.sZzOK7T);
         var2 += 98;
         this.addRenderableWidget(
            Button.builder(Component.translatable("gui.fpvdrone.map.refresh"), var0 -> MapTerrainSampler.v5suI()).bounds(var2, var3, 52, var4).build()
         );
      }
   }

   private void dOfFwvo() {
      if (this.n9lX8A) {
         this.v5suI(this.sTrE);
      } else {
         this.rxYnaa();
      }
   }

   private void v5suI(int var1) {
      this.n9lX8A = false;
      this.ivDR = true;
      this.zQOhlhTZx.clear();
      this.hXya = -1;
      this.jt9oI60ST = null;
      this.oEuCBB = null;
      this.uj1PcjLg = -1;
      this.nwFPfQ = var1;
      this.wuhB();
   }

   private boolean rsDd50tfV() {
      return this.uQdlyiwB != null && DroneDefinitions.ibzfxic(this.uQdlyiwB).zTT3nw6();
   }

   private Component uSUb() {
      return Component.translatable("gui.fpvdrone.map.terminal")
         .append(": ")
         .append(Component.translatable("gui.fpvdrone.map.terminal." + this.lvk8rWUGD.name().toLowerCase()));
   }

   private void jGhI2() {
      this.lvk8rWUGD = this.lvk8rWUGD.v5suI();
      if (this.sZzOK7T != null) {
         this.sZzOK7T.setMessage(this.uSUb());
      }
   }

   private void wEJx3Itb() {
      if (this.sZzOK7T != null) {
         this.sZzOK7T.setMessage(this.uSUb());
      }
   }

   private double bAcdvq9() {
      return this.width / 2.0;
   }

   private double c1jHYPb4() {
      return this.height / 2.0;
   }

   private double v5suI(double var1) {
      return this.bAcdvq9() + (var1 - this.fvsAOg) * qU1QioDiC;
   }

   private double ibzfxic(double var1) {
      return this.c1jHYPb4() + (var1 - this.lFt1ckP) * qU1QioDiC;
   }

   private double wuhB(double var1) {
      return this.fvsAOg + (var1 - this.bAcdvq9()) / qU1QioDiC;
   }

   private double dOfFwvo(double var1) {
      return this.lFt1ckP + (var1 - this.c1jHYPb4()) / qU1QioDiC;
   }

   private int v5suI(double var1, double var3) {
      ClientLevel var5 = Minecraft.getInstance().level;
      if (var5 == null) {
         return Integer.MIN_VALUE;
      } else {
         int var6 = Mth.floor(var1);
         int var7 = Mth.floor(var3);
         int var8 = ReconStore.v5suI().ibzfxic(var5.dimension().identifier(), var6, var7);
         if (var8 != Integer.MIN_VALUE) {
            return var8;
         } else {
            return var5.hasChunkAt(var6, var7) ? var5.getHeight(Types.WORLD_SURFACE, var6, var7) : Integer.MIN_VALUE;
         }
      }
   }

   private int ibzfxic(double var1, double var3) {
      int var5 = this.v5suI(var1, var3);
      if (var5 != Integer.MIN_VALUE) {
         return var5;
      }

      ClientLevel var6 = Minecraft.getInstance().level;
      return var6 == null ? (int)this.mM7DsB9AR : var6.getHeight(Types.WORLD_SURFACE, Mth.floor(var1), Mth.floor(var3));
   }

   private double v5suI(Waypoint var1) {
      return this.ibzfxic(var1.v5suI, var1.wuhB) + var1.ibzfxic;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      this.gQiZ = false;
      var1.fill(0, 0, this.width, this.height, -16777216);
      long var5 = System.currentTimeMillis();
      float var7 = this.dZAuCP == 0L ? 0.0F : Math.min((float)(var5 - this.dZAuCP) / 1000.0F, 0.1F);
      this.dZAuCP = var5;
      this.jfdtPQG = 1.0F - (float)Math.exp(-var7 * 12.0F);
      var1.enableScissor(0, 0, this.width, this.height);
      ClientLevel var8 = Minecraft.getInstance().level;
      if (var8 != null) {
         this.iKXOcc
            .v5suI(var1, var8.dimension().identifier(), this.fvsAOg, this.lFt1ckP, qU1QioDiC, this.bAcdvq9(), this.c1jHYPb4(), 0, this.height, this.width);
      }

      this.v8ZN();
      if (!this.ivDR) {
         this.ibzfxic(var1, var2, var3);
      }

      this.v5suI(var1, var2, var3);
      if (!this.ivDR && !this.n9lX8A) {
         this.wuhB(var1, var2, var3);
      }

      if (this.zgQgCA9) {
         double var9 = this.zQOhlhTZx.isEmpty() ? this.wBXn : this.zQOhlhTZx.get(this.zQOhlhTZx.size() - 1).v5suI;
         double var11 = this.zQOhlhTZx.isEmpty() ? this.uKpZYj : this.zQOhlhTZx.get(this.zQOhlhTZx.size() - 1).wuhB;
         double[] var13 = v5suI(this.qV33e, this.xogV5KBhg, this.rYlh, var9, var11);
         this.v5suI(var1, this.v5suI(var9) + 1.0, this.ibzfxic(var11) + 1.0, this.v5suI(var13[0]) + 1.0, this.ibzfxic(var13[1]) + 1.0, ibzfxic(-1));
         this.v5suI(var1, this.v5suI(var9), this.ibzfxic(var11), this.v5suI(var13[0]), this.ibzfxic(var13[1]), -1);
         this.v5suI(var1, this.qV33e, this.xogV5KBhg, this.rYlh, -1);
      }

      if (this.ivDR) {
         this.wuhB(var1);
      }

      if (this.ivDR || this.n9lX8A) {
         this.ibzfxic(var1);
      }

      var1.disableScissor();
      if (this.kNWIu != null) {
         this.kNWIu.active = this.zTT3nw6();
      }

      if (this.wTo0I != null) {
         this.wTo0I.active = this.b69s();
      }

      // 1.1.8 lifted the widgets to z 300 and the popup to z 400; 26.3 GUI layers are strata
      var1.nextStratum();
      super.extractRenderState(var1, var2, var3, var4);
      this.dOfFwvo(var1, var2, var3);
      if (this.jt9oI60ST != null) {
         var1.nextStratum();
         this.jt9oI60ST.v5suI(var1, this.font, var2, var3);
         if (this.oEuCBB != null) {
            var1.text(this.font, Component.translatable("gui.fpvdrone.map.agl_label"), this.jt9oI60ST.v5suI() + 3, this.jt9oI60ST.dOfFwvo(), -1, true);
            this.oEuCBB.extractRenderState(var1, var2, var3, var4);
         }
      }
   }

   private void v8ZN() {
      if (this.n9lX8A) {
         ClientLevel var1 = Minecraft.getInstance().level;
         if (var1 != null) {
            if (var1.getEntity(this.sTrE) instanceof LoiteringMunitionEntity var2 && !var2.isRemoved()) {
               this.wBXn = var2.getX();
               this.mM7DsB9AR = var2.getY();
               this.uKpZYj = var2.getZ();
            } else {
               ReconMarks.KKeMvpuBgEY var4 = ReconMarks.v5suI().v5suI(this.sTrE);
               if (var4 != null && var4.zTT3nw6() == 0L) {
                  this.wBXn = var4.wuhB();
                  this.mM7DsB9AR = var4.dOfFwvo();
                  this.uKpZYj = var4.rsDd50tfV();
               }
            }
         }
      }
   }

   private List<TacticalMapScreen.KKeMvpuBgEY> v5suI(ClientLevel var1) {
      if (this.gQiZ) {
         return this.urdCiPZD;
      }

      var var2 = new ArrayList();
      HashSet var3 = new HashSet();
      LocalPlayer var4 = Minecraft.getInstance().player;
      UUID var5 = var4 != null ? var4.getUUID() : null;

      for (Entity var7 : var1.entitiesForRendering()) {
         if (var7 instanceof LoiteringMunitionEntity var8 && !var8.isRemoved() && !var8.eJDY() && !var8.vmpmU()) {
            var var9 = var8.wBXn();
            if (!var9.isPresent() || ((UUID)var9.get()).equals(var5)) {
               var3.add(var8.getId());
               var2.add(new TacticalMapScreen.KKeMvpuBgEY(var8.getId(), var8.wEJx3Itb(), var8.getX(), var8.getZ(), 1.0F, (float)var8.we1GYY()));
            }
         }
      }

      for (ReconMarks.KKeMvpuBgEY var11 : ReconMarks.v5suI().v5suI(var1.dimension().identifier())) {
         if (!var3.contains(var11.v5suI())) {
            var2.add(
               new TacticalMapScreen.KKeMvpuBgEY(var11.v5suI(), var11.ibzfxic(), var11.wuhB(), var11.rsDd50tfV(), ReconMarks.v5suI(var11), var11.bAcdvq9())
            );
         }
      }

      this.urdCiPZD = var2;
      this.gQiZ = true;
      return var2;
   }

   private int wuhB(double var1, double var3) {
      ClientLevel var5 = Minecraft.getInstance().level;
      if (var5 == null) {
         return -1;
      }

      var var6 = this.v5suI(var5);

      for (int var7 = var6.size() - 1; var7 >= 0; var7--) {
         TacticalMapScreen.KKeMvpuBgEY var8 = (TacticalMapScreen.KKeMvpuBgEY)var6.get(var7);
         if (!(var8.rsDd50tfV() < 1.0F)) {
            int var9 = (int)Math.round(this.v5suI(var8.wuhB()));
            int var10 = (int)Math.round(this.ibzfxic(var8.dOfFwvo()));
            if (var1 >= var9 - 8 && var1 < var9 + 8 && var3 >= var10 - 8 && var3 < var10 + 8) {
               return var8.v5suI();
            }
         }
      }

      return -1;
   }

   private void v5suI(GuiGraphicsExtractor var1, int var2, int var3) {
      ClientLevel var4 = Minecraft.getInstance().level;
      if (var4 != null) {
         var var5 = this.v5suI(var4);
         int var6 = this.wuhB((double)var2, (double)var3);
         HashSet var7 = new HashSet();
         boolean var8 = false;

         for (TacticalMapScreen.KKeMvpuBgEY var10 : var5) {
            var7.add(var10.v5suI());
            if (var10.v5suI() == this.nwFPfQ && var10.rsDd50tfV() >= 1.0F) {
               var8 = true;
            }
         }

         this.iY41Hgn0m.keySet().removeIf(var1x -> !var7.contains(var1x));
         if (this.nwFPfQ != -1 && !var8) {
            this.nwFPfQ = -1;
         }

         for (TacticalMapScreen.KKeMvpuBgEY var21 : var5) {
            if (!(var21.rsDd50tfV() < 1.0F)) {
               float var11 = this.iY41Hgn0m.getOrDefault(var21.v5suI(), 0.0F);
               float var12 = var21.v5suI() != var6 && var21.v5suI() != this.nwFPfQ ? 0.0F : 1.0F;
               this.iY41Hgn0m.put(var21.v5suI(), var11 + (var12 - var11) * this.jfdtPQG);
            }
         }

         for (TacticalMapScreen.KKeMvpuBgEY var22 : var5) {
            float var24 = this.iY41Hgn0m.getOrDefault(var22.v5suI(), 0.0F);
            float var26 = var24 * 0.7F * var22.rsDd50tfV();
            if (var26 > 0.01F) {
               this.v5suI(var1, ReconMarks.v5suI().v5suI(var22.v5suI()), var22.wuhB(), var22.dOfFwvo(), var26);
            }
         }

         for (TacticalMapScreen.KKeMvpuBgEY var23 : var5) {
            int var25 = (int)Math.round(this.v5suI(var23.wuhB()));
            int var27 = (int)Math.round(this.ibzfxic(var23.dOfFwvo()));
            float var13 = this.iY41Hgn0m.getOrDefault(var23.v5suI(), 0.0F);
            float var14 = var23.rsDd50tfV();
            ItemStack var15 = v5suI(var23.ibzfxic());
            if (!(var13 > 0.01F) && !(var14 < 1.0F)) {
               var1.item(var15, var25 - 8, var27 - 8);
            } else {
               float var16 = (1.0F + 0.22F * var13) * var14;
               float var17 = 1.0F + 0.55F * var13;
               var1.pose().pushMatrix();
               var1.pose().translate((float)(var25), (float)(var27));
               var1.pose().scale(var16, var16);
               var1.pose().translate((float)(-var25), (float)(-var27));
               var1.item(var15, var25 - 8, var27 - 8);
               var1.pose().popMatrix();
            }
         }
      }
   }

   private void v5suI(GuiGraphicsExtractor var1, ReconMarks.KKeMvpuBgEY var2, double var3, double var5, float var7) {
      if (var2 != null && var2.jGhI2() != null && !var2.jGhI2().dOfFwvo() && !(var7 <= 0.01F)) {
         FlightRoute var8 = var2.jGhI2();
         int var9 = Mth.clamp((int)(var7 * 255.0F), 0, 255);
         int var10 = var9 << 24 | 16777215;
         int var11 = ibzfxic(var10);
         var var12 = var8.ibzfxic();
         Vec3 var13 = var2.wEJx3Itb();
         double var14 = var13 != null ? var13.x : var3;
         double var16 = var13 != null ? var13.z : var5;
         var var18 = new ArrayList<double[]>();
         if (var13 != null) {
            var18.add(new double[]{var13.x, var13.z});
         } else {
            var18.add(new double[]{var3, var5});
         }

         for (Waypoint var20 : var12) {
            var18.add(new double[]{var20.v5suI, var20.wuhB});
         }

         Waypoint var37 = (Waypoint)var12.get(var12.size() - 1);
         if (var37.dOfFwvo > 0.0) {
            double[] var38 = (double[])var18.get(var18.size() - 2);
            double[] var21 = v5suI(var37.v5suI, var37.wuhB, var37.dOfFwvo, var38[0], var38[1]);
            var18.set(var18.size() - 1, var21);
         }

         var var39 = new ArrayList();

         for (double[] var22 : var18) {
            var39.add(new double[]{this.v5suI(var22[0]), this.ibzfxic(var22[1])});
         }

         int var41 = Mth.clamp(var2.c1jHYPb4(), 0, var12.size() - 1);
         boolean var42 = !var12.isEmpty();
         double var23 = this.v5suI(var3);
         double var25 = this.ibzfxic(var5);
         double var27 = 0.0;
         double var29 = 0.0;
         if (var42) {
            double[] var31 = v5suI(var12, var41, var14, var16);
            var27 = this.v5suI(var31[0]);
            var29 = this.ibzfxic(var31[1]);
         }

         for (int var43 = 0; var43 < var39.size() - 1; var43++) {
            this.v5suI(
               var1,
               ((double[])var39.get(var43))[0] + 1.0,
               ((double[])var39.get(var43))[1] + 1.0,
               ((double[])var39.get(var43 + 1))[0] + 1.0,
               ((double[])var39.get(var43 + 1))[1] + 1.0,
               var11
            );
         }

         if (var42) {
            this.v5suI(var1, var23 + 1.0, var25 + 1.0, var27 + 1.0, var29 + 1.0, var11);
         }

         for (int var44 = 0; var44 < var39.size() - 1; var44++) {
            this.v5suI(
               var1,
               ((double[])var39.get(var44))[0],
               ((double[])var39.get(var44))[1],
               ((double[])var39.get(var44 + 1))[0],
               ((double[])var39.get(var44 + 1))[1],
               var10
            );
         }

         if (var42) {
            this.v5suI(var1, var23, var25, var27, var29, var10);
         }

         this.v5suI(var1, var12, var10);
         boolean var45 = var8.rsDd50tfV() != TerminalMode.LOITER;
         int pinColor = ARGB.colorFromFloat(var7, 1.0F, 1.0F, 1.0F);

         for (int var32 = 0; var32 < var12.size(); var32++) {
            double[] var33 = v5suI(var12, var32, var14, var16);
            int var34 = (int)Math.round(this.v5suI(var33[0]));
            int var35 = (int)Math.round(this.ibzfxic(var33[1]));
            Identifier var36 = var32 == var12.size() - 1 && var45 ? qlv6NKWnH : iAKNPAj;
            var1.blit(RenderPipelines.GUI_TEXTURED, var36, var34 - 8, var35 - 16 - 2, 0.0F, 0.0F, 16, 16, 16, 16, pinColor);
         }

         if (var13 != null) {
            int var46 = (int)Math.round(this.v5suI(var13.x));
            int var47 = (int)Math.round(this.ibzfxic(var13.z));
            var1.item(this.kNoG, var46 - 8, var47 - 8);
         }

         }
   }

   private static ItemStack v5suI(String var0) {
      return new ItemStack(ModItems.droneItemFor(var0));
   }

   private boolean zTT3nw6() {
      if (this.nwFPfQ < 0) {
         return false;
      }

      ClientLevel var1 = Minecraft.getInstance().level;
      if (var1 == null) {
         return false;
      }

      for (TacticalMapScreen.KKeMvpuBgEY var3 : this.v5suI(var1)) {
         if (var3.v5suI() == this.nwFPfQ && var3.rsDd50tfV() >= 1.0F) {
            return DroneDefinitions.ibzfxic(var3.ibzfxic()).rOIp0p7().ibzfxic();
         }
      }

      return false;
   }

   private void vmpmU() {
      ClientLevel var1 = Minecraft.getInstance().level;
      if (var1 != null && this.nwFPfQ >= 0) {
         MunitionClientState.v5suI(qU1QioDiC);

         for (TacticalMapScreen.KKeMvpuBgEY var3 : this.v5suI(var1)) {
            if (var3.v5suI() == this.nwFPfQ) {
               if (!DroneDefinitions.ibzfxic(var3.ibzfxic()).rOIp0p7().ibzfxic()) {
                  return;
               }

               ReconMarks.KKeMvpuBgEY var4 = ReconMarks.v5suI().v5suI(var3.v5suI());
               double var5 = this.mM7DsB9AR;
               Entity var7 = var1.getEntity(var3.v5suI());
               if (var7 != null) {
                  var5 = var7.getY();
               } else if (var4 != null) {
                  var5 = var4.dOfFwvo();
               }

               TerminalMode var8 = var4 != null && var4.jGhI2() != null && !var4.jGhI2().dOfFwvo() ? var4.jGhI2().rsDd50tfV() : TerminalMode.DIVE;
               this.ivDR = false;
               this.n9lX8A = true;
               this.sTrE = var3.v5suI();
               this.wBXn = var3.wuhB();
               this.mM7DsB9AR = var5;
               this.uKpZYj = var3.dOfFwvo();
               this.xYttSy = v5suI(var3.ibzfxic());
               this.lvk8rWUGD = var8;
               this.zQOhlhTZx.clear();
               this.hXya = -1;
               this.jt9oI60ST = null;
               this.oEuCBB = null;
               this.uj1PcjLg = -1;
               this.nwFPfQ = -1;
               this.wuhB();
               return;
            }
         }
      }
   }

   private boolean b69s() {
      if (this.nwFPfQ < 0) {
         return false;
      } else {
         ClientLevel var1 = Minecraft.getInstance().level;
         if (var1 == null) {
            return false;
         } else if (var1.getEntity(this.nwFPfQ) instanceof LoiteringMunitionEntity var4) {
            return !var4.isRemoved() && var4.uQdlyiwB().v8ZN();
         } else {
            ReconMarks.KKeMvpuBgEY var2 = ReconMarks.v5suI().v5suI(this.nwFPfQ);
            return var2 != null && var2.zTT3nw6() == 0L && DroneDefinitions.ibzfxic(var2.ibzfxic()).v8ZN();
         }
      }
   }

   private void cXHvq() {
      ClientLevel var1 = Minecraft.getInstance().level;
      if (var1 != null && this.nwFPfQ >= 0) {
         MunitionClientState.v5suI(qU1QioDiC);
         if (var1.getEntity(this.nwFPfQ) instanceof LoiteringMunitionEntity var2 && var2.uQdlyiwB().v8ZN()) {
            MunitionClientState.v5suI(var2);
            this.onClose();
         } else {
            ReconMarks.KKeMvpuBgEY var4 = ReconMarks.v5suI().v5suI(this.nwFPfQ);
            if (var4 != null && var4.zTT3nw6() == 0L && DroneDefinitions.ibzfxic(var4.ibzfxic()).v8ZN()) {
               MunitionClientState.v5suI(var4.v5suI(), var4.wuhB(), var4.dOfFwvo(), var4.rsDd50tfV());
               this.onClose();
            }
         }
      }
   }

   private void ibzfxic(GuiGraphicsExtractor var1, int var2, int var3) {
      this.rsDd50tfV(var1, ibzfxic(-1), 1);
      this.rsDd50tfV(var1, -1, 0);
      this.v5suI(var1, this.zQOhlhTZx, -1);
      this.v5suI(var1);
      if (this.wogVpI.length != this.zQOhlhTZx.size()) {
         this.wogVpI = Arrays.copyOf(this.wogVpI, this.zQOhlhTZx.size());
      }

      boolean var4 = this.lvk8rWUGD != TerminalMode.LOITER;
      int var5 = this.dOfFwvo((double)var2, (double)var3);

      for (int var6 = 0; var6 < this.zQOhlhTZx.size(); var6++) {
         Waypoint var7 = this.zQOhlhTZx.get(var6);
         double[] var8 = v5suI(this.zQOhlhTZx, var6, this.wBXn, this.uKpZYj);
         int var9 = (int)Math.round(this.v5suI(var8[0]));
         int var10 = (int)Math.round(this.ibzfxic(var8[1]));
         Identifier var11 = var6 == this.zQOhlhTZx.size() - 1 && var4 ? qlv6NKWnH : iAKNPAj;
         float var12 = var6 != var5 && var6 != this.hXya ? 0.0F : 1.0F;
         this.wogVpI[var6] = this.wogVpI[var6] + (var12 - this.wogVpI[var6]) * this.jfdtPQG;
         this.v5suI(var1, var11, var9 - 8, var10 - 16 - 2, this.wogVpI[var6]);
      }
   }

   private void v5suI(GuiGraphicsExtractor var1) {
      double var2 = this.wBXn;
      double var4 = this.mM7DsB9AR;
      double var6 = this.uKpZYj;

      for (int var8 = 0; var8 < this.zQOhlhTZx.size(); var8++) {
         Waypoint var9 = this.zQOhlhTZx.get(var8);
         double var10 = this.v5suI(var9);
         double[] var12 = v5suI(this.zQOhlhTZx, var8, this.wBXn, this.uKpZYj);
         double var13 = var12[0];
         double var15 = var12[1];
         double var17 = var10 - var4;
         if (Math.abs(var17) >= 2.0) {
            Identifier var19 = var17 > 0.0 ? hOKMYHOaC : iS1JnZov;
            int var20 = (int)Math.round((this.v5suI(var2) + this.v5suI(var13)) / 2.0);
            int var21 = (int)Math.round((this.ibzfxic(var6) + this.ibzfxic(var15)) / 2.0);
            var1.blit(RenderPipelines.GUI_TEXTURED, var19, var20 - 8, var21 - 8, 0.0F, 0.0F, 16, 16, 16, 16);
         }

         var2 = var9.v5suI;
         var4 = var10;
         var6 = var9.wuhB;
      }
   }

   private void v5suI(GuiGraphicsExtractor var1, Identifier var2, int var3, int var4, float var5) {
      float var6 = Math.min(1.0F, var5);
      boolean var7 = var6 > 0.01F;
      if (var7) {
         float var8 = 1.0F + 0.22F * var6;
         int var9 = var3 + 8;
         int var10 = var4 + 8;
         var1.pose().pushMatrix();
         var1.pose().translate((float)(var9), (float)(var10));
         var1.pose().scale(var8, var8);
         var1.pose().translate((float)(-var9), (float)(-var10));
      }

      var1.blit(RenderPipelines.GUI_TEXTURED, var2, var3, var4, 0.0F, 0.0F, 16, 16, 16, 16);
      if (var7) {
         float var11 = 0.55F * var6;
         // 1.1.8: second pass with additive blending (SRC_ALPHA, ONE) for the hover glow
         var1.blit(GUI_TEXTURED_GLOW, var2, var3, var4, 0.0F, 0.0F, 16, 16, 16, 16, ARGB.colorFromFloat(1.0F, var11, var11, var11));
         var1.pose().popMatrix();
      }
   }

   private int dOfFwvo(double var1, double var3) {
      for (int var5 = this.zQOhlhTZx.size() - 1; var5 >= 0; var5--) {
         double[] var6 = v5suI(this.zQOhlhTZx, var5, this.wBXn, this.uKpZYj);
         int var7 = (int)Math.round(this.v5suI(var6[0]));
         int var8 = (int)Math.round(this.ibzfxic(var6[1]));
         int var9 = var7 - 8;
         int var10 = var8 - 16 - 2;
         if (var1 >= var9 && var1 < var9 + 16 && var3 >= var10 && var3 < var10 + 16) {
            return var5;
         }
      }

      return -1;
   }

   private void wuhB(GuiGraphicsExtractor var1, int var2, int var3) {
      int var4 = (int)Math.round(this.v5suI(this.wBXn));
      int var5 = (int)Math.round(this.ibzfxic(this.uKpZYj));
      boolean var6 = var2 >= var4 - 8 && var2 < var4 + 8 && var3 >= var5 - 8 && var3 < var5 + 8;
      this.qz71Ga = this.qz71Ga + ((var6 ? 1.0F : 0.0F) - this.qz71Ga) * this.jfdtPQG;
      if (this.qz71Ga > 0.01F) {
         float var7 = 1.0F + 0.22F * this.qz71Ga;
         float var8 = 1.0F + 0.55F * this.qz71Ga;
         var1.pose().pushMatrix();
         var1.pose().translate((float)(var4), (float)(var5));
         var1.pose().scale(var7, var7);
         var1.pose().translate((float)(-var4), (float)(-var5));
         var1.item(this.xYttSy, var4 - 8, var5 - 8);
         var1.pose().popMatrix();
      } else {
         var1.item(this.xYttSy, var4 - 8, var5 - 8);
      }
   }

   private void ibzfxic(GuiGraphicsExtractor var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.player != null) {
         Vec3 var3 = FPVClientMod.getPilotBodyPosition(var2.player.getUUID());
         double var4 = var3 != null ? var3.x : var2.player.getX();
         double var6 = var3 != null ? var3.z : var2.player.getZ();
         net.minecraft.world.entity.player.PlayerSkin var8 = var2.player.getSkin();
         int var9 = (int)Math.round(this.v5suI(var4));
         int var10 = (int)Math.round(this.ibzfxic(var6));
         byte var11 = 8;
         int var12 = var9 - var11 / 2;
         int var13 = var10 - var11 / 2;
         var1.pose().pushMatrix();
         var1.nextStratum(); // 1.1.8: z 200
         PlayerFaceExtractor.extractRenderState(var1, var8, var12 + 1, var13 + 1, var11, ARGB.colorFromFloat(0.35F, 0.0F, 0.0F, 0.0F));
         PlayerFaceExtractor.extractRenderState(var1, var8, var12, var13, var11);
         var1.pose().popMatrix();
      }
   }

   private void wuhB(GuiGraphicsExtractor var1) {
      if (this.jm7PbrhC >= 0L) {
         long var2 = System.currentTimeMillis() - this.jm7PbrhC;
         if (var2 >= 3000L) {
            this.jm7PbrhC = -1L;
         } else {
            float var4 = 1.0F - (float)var2 / 3000.0F;
            int var5 = (int)Math.round(this.v5suI(this.p9T0hGf));
            int var6 = (int)Math.round(this.ibzfxic(this.i7EP1lKl));
            ItemStack var7 = v5suI(this.dReHaJ);
            var1.pose().pushMatrix();
            var1.pose().translate((float)(var5), (float)(var6));
            var1.pose().scale(var4, var4);
            var1.pose().translate((float)(-var5), (float)(-var6));
            var1.nextStratum(); // 1.1.8: z 200 (26.3 GUI items have no alpha; the icon only shrinks)
            var1.item(var7, var5 - 8, var6 - 8);
            var1.pose().popMatrix();
         }
      }
   }

   private void dOfFwvo(GuiGraphicsExtractor var1, int var2, int var3) {
      double var4 = this.wuhB((double)var2);
      double var6 = this.dOfFwvo(var3);
      int var8 = this.ibzfxic(var4, var6);
      if (this.zgQgCA9) {
         int var16 = this.height - 30;
         int var18 = this.width / 2;
         String var20 = "radius " + (int)Math.round(this.rYlh);
         String var22 = Component.translatable("gui.fpvdrone.map.loiter_help").getString();
         this.v5suI(var1, var20, var18, var16 + 5, -1);
         this.v5suI(var1, var22, var18, var16 + 17, -4671296);
      } else {
         String var9 = this.ibzfxic(var2, var3);
         if (this.ivDR) {
            int var17 = this.height - 30;
            int var19 = this.width / 2;
            ClientLevel var21 = Minecraft.getInstance().level;
            int var24 = var21 == null ? 0 : this.v5suI(var21).size();
            String var25 = Component.translatable("gui.fpvdrone.map.observe", new Object[]{var24}).getString();
            String var15 = var9 != null
               ? var9
               : Component.translatable("gui.fpvdrone.map.cursor", new Object[]{Mth.floor(var4), Mth.floor(var6), var8}).getString();
            this.v5suI(var1, var25 + "   " + var15, var19, var17 + 10, -1);
         } else {
            int var10 = this.hXya;
            if (var10 < 0) {
               var10 = this.dOfFwvo((double)var2, (double)var3);
            }

            if (var10 < 0) {
               var10 = this.v5suI(var2, var3);
            }

            String var11 = this.n9lX8A
               ? Component.translatable("gui.fpvdrone.map.target_control").getString()
               : (
                  this.ifpFOCaAs
                     ? Component.translatable("gui.fpvdrone.map.target_ramp").getString()
                     : Component.translatable("gui.fpvdrone.map.target_drone").getString()
               );
            if (!this.zQOhlhTZx.isEmpty()) {
               var11 = var11 + "   " + Component.translatable("gui.fpvdrone.map.route_time", new Object[]{this.iAKNPAj()}).getString();
            }

            if (var9 != null) {
               var11 = var11 + "   " + var9;
            }

            String var12;
            if (var10 >= 0) {
               Waypoint var13 = this.zQOhlhTZx.get(var10);
               var12 = Component.translatable("gui.fpvdrone.map.wp_readout", new Object[]{var10 + 1, (int)Math.round(var13.ibzfxic)}).getString();
            } else {
               var12 = Component.translatable("gui.fpvdrone.map.cursor", new Object[]{Mth.floor(var4), Mth.floor(var6), var8}).getString()
                  + "   "
                  + Component.translatable("gui.fpvdrone.map.wp_count", new Object[]{this.zQOhlhTZx.size()}).getString();
            }

            if (this.rOIp0p7 > 0L && System.currentTimeMillis() - this.rOIp0p7 < 2200L) {
               var12 = Component.translatable("gui.fpvdrone.map.sent").getString();
            }

            int var23 = this.height - 30;
            int var14 = this.width / 2;
            this.v5suI(var1, var11 + "   " + var12, var14, var23 + 10, -1);
         }
      }
   }

   private void v5suI(GuiGraphicsExtractor var1, String var2, int var3, int var4, int var5) {
      int var6 = this.font.width(var2);
      int var7 = var3 - var6 / 2;
      var1.fill(var7 - 3, var4 - 2, var7 + var6 + 3, var4 + 9 - 1, -1073741824);
      var1.centeredText(this.font, var2, var3, var4, var5);
   }

   private void rsDd50tfV(GuiGraphicsExtractor var1, int var2, int var3) {
      double var4 = this.wBXn;
      double var6 = this.uKpZYj;

      for (int var8 = 0; var8 < this.zQOhlhTZx.size(); var8++) {
         Waypoint var9 = this.zQOhlhTZx.get(var8);
         double var10 = var9.v5suI;
         double var12 = var9.wuhB;
         if (var8 == this.zQOhlhTZx.size() - 1 && var9.dOfFwvo > 0.0) {
            double[] var14 = v5suI(var9.v5suI, var9.wuhB, var9.dOfFwvo, var4, var6);
            var10 = var14[0];
            var12 = var14[1];
         }

         this.v5suI(var1, this.v5suI(var4) + var3, this.ibzfxic(var6) + var3, this.v5suI(var10) + var3, this.ibzfxic(var12) + var3, var2);
         var4 = var9.v5suI;
         var6 = var9.wuhB;
      }
   }

   private static int ibzfxic(int var0) {
      return (var0 & 16579836) >> 2 | var0 & 0xFF000000;
   }

   private void v5suI(GuiGraphicsExtractor var1, double var2, double var4, double var6, int var8) {
      double var9 = var6 * qU1QioDiC;
      int var11 = Mth.clamp((int)Math.ceil((Math.PI * 2) * var9 / 6.0), 24, 64);
      double[] var12 = new double[var11 + 1];
      double[] var13 = new double[var11 + 1];

      for (int var14 = 0; var14 <= var11; var14++) {
         double var15 = (Math.PI * 2) * var14 / var11;
         var12[var14] = this.v5suI(var2 + var6 * Math.cos(var15));
         var13[var14] = this.ibzfxic(var4 + var6 * Math.sin(var15));
      }

      int var19 = ibzfxic(var8);

      for (int var20 = 0; var20 < 2; var20++) {
         int var16 = var20 == 0 ? 1 : 0;
         int var17 = var20 == 0 ? var19 : var8;

         for (int var18 = 0; var18 < var11; var18++) {
            this.v5suI(var1, var12[var18] + var16, var13[var18] + var16, var12[var18 + 1] + var16, var13[var18 + 1] + var16, var17, var18 > 0);
         }
      }
   }

   private void v5suI(GuiGraphicsExtractor var1, List<Waypoint> var2, int var3) {
      if (!var2.isEmpty()) {
         Waypoint var4 = (Waypoint)var2.get(var2.size() - 1);
         if (!(var4.dOfFwvo <= 0.0)) {
            this.v5suI(var1, var4.v5suI, var4.wuhB, var4.dOfFwvo, var3);
         }
      }
   }

   private void v5suI(GuiGraphicsExtractor var1, double var2, double var4, double var6, double var8, int var10) {
      this.v5suI(var1, var2, var4, var6, var8, var10, false);
   }

   private void v5suI(GuiGraphicsExtractor var1, double var2, double var4, double var6, double var8, int var10, boolean var11) {
      double[] var12 = this.v5suI(var2, var4, var6, var8);
      if (var12 != null) {
         int var13 = (int)Math.round(var12[0]);
         int var14 = (int)Math.round(var12[1]);
         int var15 = (int)Math.round(var12[2]);
         int var16 = (int)Math.round(var12[3]);
         int var17 = Math.abs(var15 - var13);
         int var18 = Math.abs(var16 - var14);
         int var19 = var17;
         int var20 = -var18;
         int var21 = var13 < var15 ? 1 : -1;
         int var22 = var14 < var16 ? 1 : -1;
         int var23 = var19 + var20;
         if (var11) {
            if (var13 == var15 && var14 == var16) {
               return;
            }

            int var24 = 2 * var23;
            if (var24 >= var20) {
               var23 += var20;
               var13 += var21;
            }

            if (var24 <= var19) {
               var23 += var19;
               var14 += var22;
            }
         }

         if (var17 >= var18) {
            int var28 = var14;
            int var25 = var13;
            int var26 = var13;

            while (true) {
               if (var14 != var28) {
                  var1.fill(var25, var28, var26 + 1, var28 + 1, var10);
                  var28 = var14;
                  var25 = var13;
                  var26 = var13;
               } else {
                  var25 = Math.min(var25, var13);
                  var26 = Math.max(var26, var13);
               }

               if (var13 == var15 && var14 == var16) {
                  var1.fill(var25, var28, var26 + 1, var28 + 1, var10);
                  break;
               }

               int var27 = 2 * var23;
               if (var27 >= var20) {
                  var23 += var20;
                  var13 += var21;
               }

               if (var27 <= var19) {
                  var23 += var19;
                  var14 += var22;
               }
            }
         } else {
            int var29 = var13;
            int var30 = var14;
            int var31 = var14;

            while (true) {
               if (var13 != var29) {
                  var1.fill(var29, var30, var29 + 1, var31 + 1, var10);
                  var29 = var13;
                  var30 = var14;
                  var31 = var14;
               } else {
                  var30 = Math.min(var30, var14);
                  var31 = Math.max(var31, var14);
               }

               if (var13 == var15 && var14 == var16) {
                  var1.fill(var29, var30, var29 + 1, var31 + 1, var10);
                  break;
               }

               int var32 = 2 * var23;
               if (var32 >= var20) {
                  var23 += var20;
                  var13 += var21;
               }

               if (var32 <= var19) {
                  var23 += var19;
                  var14 += var22;
               }
            }
         }
      }
   }

   private double[] v5suI(double var1, double var3, double var5, double var7) {
      double var9 = -1.0;
      double var11 = -1.0;
      double var13 = this.width + 1;
      double var15 = this.height + 1;
      int var17 = v5suI(var1, var3, -1.0, -1.0, var13, var15);
      int var18 = v5suI(var5, var7, -1.0, -1.0, var13, var15);

      while ((var17 | var18) != 0) {
         if ((var17 & var18) != 0) {
            return null;
         }

         int var19 = var17 != 0 ? var17 : var18;
         double var20;
         double var22;
         if ((var19 & 8) != 0) {
            var20 = var1 + (var5 - var1) * (var15 - var3) / (var7 - var3);
            var22 = var15;
         } else if ((var19 & 4) != 0) {
            var20 = var1 + (var5 - var1) * (-1.0 - var3) / (var7 - var3);
            var22 = -1.0;
         } else if ((var19 & 2) != 0) {
            var22 = var3 + (var7 - var3) * (var13 - var1) / (var5 - var1);
            var20 = var13;
         } else {
            var22 = var3 + (var7 - var3) * (-1.0 - var1) / (var5 - var1);
            var20 = -1.0;
         }

         if (var19 == var17) {
            var1 = var20;
            var3 = var22;
            var17 = v5suI(var1, var3, -1.0, -1.0, var13, var15);
         } else {
            var5 = var20;
            var7 = var22;
            var18 = v5suI(var5, var7, -1.0, -1.0, var13, var15);
         }
      }

      return new double[]{var1, var3, var5, var7};
   }

   private static int v5suI(double var0, double var2, double var4, double var6, double var8, double var10) {
      byte var12 = 0;
      if (var0 < var4) {
         var12 |= 1;
      } else if (var0 > var8) {
         var12 |= 2;
      }

      if (var2 < var6) {
         var12 |= 4;
      } else if (var2 > var10) {
         var12 |= 8;
      }

      return var12;
   }

   private int v5suI(int var1, int var2) {
      for (int var3 = 0; var3 < this.zQOhlhTZx.size(); var3++) {
         double[] var4 = v5suI(this.zQOhlhTZx, var3, this.wBXn, this.uKpZYj);
         double var5 = this.v5suI(var4[0]);
         double var7 = this.ibzfxic(var4[1]);
         if (Math.abs(var5 - var1) <= 6.0 && Math.abs(var7 - var2) <= 6.0) {
            return var3;
         }
      }

      return -1;
   }

   // 26.3 input events: the 1.1.8 handlers below keep their (x, y, button) form; these adapters keep the event for
   // the super/EditBox calls.
   private net.minecraft.client.input.MouseButtonEvent curClick;
   private boolean curDoubleClick;
   private net.minecraft.client.input.KeyEvent curKey;
   private String aglLastValid = "";

   @Override
   public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
      this.curClick = event;
      this.curDoubleClick = doubleClick;
      return this.mouseClicked(event.x(), event.y(), event.button());
   }

   @Override
   public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
      this.curClick = event;
      return this.mouseDragged(event.x(), event.y(), event.button(), dx, dy);
   }

   @Override
   public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
      this.curClick = event;
      return this.mouseReleased(event.x(), event.y(), event.button());
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      return this.mouseScrolled(x, y, scrollY);
   }

   @Override
   public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
      return this.oEuCBB != null && this.oEuCBB.isFocused() && this.oEuCBB.charTyped(event) ? true : super.charTyped(event);
   }

   @Override
   public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
      this.curKey = event;
      return this.keyPressed(event.key(), event.keycode(), event.modifiers());
   }

   private boolean mouseClicked(double var1, double var3, int var5) {
      if (this.zgQgCA9) {
         if (var5 == 0) {
            this.iS1JnZov();
         } else {
            this.hOKMYHOaC();
         }

         return true;
      } else {
         if (this.jt9oI60ST != null && this.oEuCBB != null) {
            boolean var6 = var1 >= this.oEuCBB.getX()
               && var1 < this.oEuCBB.getX() + this.oEuCBB.getWidth()
               && var3 >= this.oEuCBB.getY()
               && var3 < this.oEuCBB.getY() + this.oEuCBB.getHeight();
            this.oEuCBB.setFocused(var6);
            if (var6) {
               this.oEuCBB.mouseClicked(this.curClick, this.curDoubleClick);
               return true;
            }
         }

         if (this.jt9oI60ST != null) {
            boolean var10 = this.jt9oI60ST.v5suI(var1, var3);
            boolean var11 = this.jt9oI60ST.ibzfxic(var1, var3);
            if (var11) {
               return true;
            }

            this.qlv6NKWnH();
            if (!var10 && (var5 == 0 || var5 == 1)) {
               int var8 = this.dOfFwvo(var1, var3);
               if (var8 >= 0) {
                  this.v5suI(var8, (int)var1, (int)var3);
               }
            }

            return true;
         } else {
            if (super.mouseClicked(this.curClick, this.curDoubleClick)) {
               return true;
            }

            if (var3 < 22.0) {
               return false;
            }

            int var9 = this.dOfFwvo(var1, var3);
            if (var9 < 0 || var5 != 0 && var5 != 1) {
               if (var5 == 0) {
                  int var7 = this.wuhB(var1, var3);
                  if (var7 >= 0) {
                     this.nwFPfQ = this.nwFPfQ == var7 ? -1 : var7;
                     return true;
                  }
               }

               if (var5 == 1) {
                  if (!this.ivDR) {
                     this.wuhB((int)var1, (int)var3);
                  }

                  return true;
               } else if (var5 == 0) {
                  this.i263D9 = true;
                  return true;
               } else {
                  return false;
               }
            } else {
               this.v5suI(var9, (int)var1, (int)var3);
               return true;
            }
         }
      }
   }

   public void mouseMoved(double var1, double var3) {
      super.mouseMoved(var1, var3);
      if (this.zgQgCA9) {
         double var5 = this.wuhB(var1);
         double var7 = this.dOfFwvo(var3);
         double var9 = var5 - this.qV33e;
         double var11 = var7 - this.xogV5KBhg;
         this.rYlh = Mth.clamp(Math.hypot(var9, var11), 100.0, 800.0);
      }
   }

   private boolean mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      if (this.i263D9 && var5 == 0) {
         this.fvsAOg = this.fvsAOg - var6 / qU1QioDiC;
         this.lFt1ckP = this.lFt1ckP - var8 / qU1QioDiC;
         return true;
      } else {
         return super.mouseDragged(this.curClick, var6, var8);
      }
   }

   private boolean mouseReleased(double var1, double var3, int var5) {
      if (this.i263D9 && var5 == 0) {
         this.i263D9 = false;
         return true;
      } else {
         return super.mouseReleased(this.curClick);
      }
   }

   private boolean mouseScrolled(double var1, double var3, double var5) {
      double var7 = this.wuhB(var1);
      double var9 = this.dOfFwvo(var3);
      double var11 = var5 > 0.0 ? 1.18 : 0.8474576271186441;
      qU1QioDiC = Mth.clamp(qU1QioDiC * var11, 0.05, 12.0);
      this.fvsAOg = var7 - (var1 - this.bAcdvq9()) / qU1QioDiC;
      this.lFt1ckP = var9 - (var3 - this.c1jHYPb4()) / qU1QioDiC;
      return true;
   }

   public void onClose() {
      this.ibzfxic();
      MunitionClientState.lvk8rWUGD();
      super.onClose();
   }

   private boolean keyPressed(int var1, int var2, int var3) {
      if (this.oEuCBB != null && this.oEuCBB.isFocused() && var1 != 256 && this.oEuCBB.keyPressed(this.curKey)) {
         return true;
      }

      if (var1 == 256 && this.zgQgCA9) {
         this.hOKMYHOaC();
         return true;
      }

      switch (var1) {
         case 82:
            MapTerrainSampler.v5suI();
            return true;
         case 257:
         case 335:
            if (this.oEuCBB != null && this.oEuCBB.isFocused()) {
               return true;
            }

            if (!this.ivDR) {
               this.lfyfLwuw();
            }

            return true;
         default:
            if (this.ivDR && KeyManager.OPEN_MAP_KEY.matches(this.curKey)) {
               this.onClose();
               return true;
            } else {
               return super.keyPressed(this.curKey);
            }
      }
   }

   private void rsDd50tfV(double var1, double var3) {
      if (this.zQOhlhTZx.size() < 64) {
         this.zQOhlhTZx.add(new Waypoint(var1, this.pyHY5, var3).wuhB(this.idXga.dOfFwvo));
      }
   }

   private void rxYnaa() {
      if (!this.zQOhlhTZx.isEmpty()) {
         this.zQOhlhTZx.remove(this.zQOhlhTZx.size() - 1);
      }
   }

   private void v5suI(int var1, double var2) {
      if (var1 >= 0 && var1 < this.zQOhlhTZx.size()) {
         Waypoint var4 = this.zQOhlhTZx.get(var1);
         this.zQOhlhTZx.set(var1, var4.v5suI(Mth.clamp(var2, 5.0, 2000.0)));
      }
   }

   private void v5suI(int var1, RouteSpeed var2) {
      if (var1 >= 0 && var1 < this.zQOhlhTZx.size()) {
         Waypoint var3 = this.zQOhlhTZx.get(var1);
         this.zQOhlhTZx.set(var1, var3.wuhB(var2.dOfFwvo));
      }
   }

   private double r6s7FL3() {
      if (this.zQOhlhTZx.isEmpty()) {
         return 0.0;
      }

      double var1 = 0.0;
      double var3 = this.wBXn;
      double var5 = this.mM7DsB9AR;
      double var7 = this.uKpZYj;

      for (Waypoint var10 : this.zQOhlhTZx) {
         double var11 = this.v5suI(var10);
         double var13 = var10.v5suI - var3;
         double var15 = var11 - var5;
         double var17 = var10.wuhB - var7;
         var1 += Math.sqrt(var13 * var13 + var15 * var15 + var17 * var17);
         var3 = var10.v5suI;
         var5 = var11;
         var7 = var10.wuhB;
      }

      return var1 / c1jHYPb4;
   }

   private String iAKNPAj() {
      return rsDd50tfV(this.r6s7FL3());
   }

   private static String rsDd50tfV(double var0) {
      if (var0 < 60.0) {
         return Component.translatable("gui.fpvdrone.map.time_sec", new Object[]{(int)Math.round(var0)}).getString();
      }

      String var2 = String.format(Locale.ROOT, "%.1f", var0 / 60.0);
      return Component.translatable("gui.fpvdrone.map.time_min", new Object[]{var2}).getString();
   }

   private double v5suI(double var1, double var3, ReconMarks.KKeMvpuBgEY var5) {
      if (var5 != null && var5.jGhI2() != null && !var5.jGhI2().dOfFwvo()) {
         var var6 = var5.jGhI2().ibzfxic();
         Vec3 var7 = var5.wEJx3Itb();
         double var8 = var7 != null ? var7.x : var1;
         double var10 = var7 != null ? var7.z : var3;
         int var12 = Mth.clamp(var5.c1jHYPb4(), 0, var6.size() - 1);
         double[] var13 = v5suI(var6, var12, var8, var10);
         double var14 = var13[0] - var1;
         double var16 = var13[1] - var3;
         double var18 = c1jHYPb4 * ((Waypoint)var6.get(var12)).rsDd50tfV;
         double var20 = Math.sqrt(var14 * var14 + var16 * var16) / var18;

         for (int var22 = var12; var22 < var6.size() - 1; var22++) {
            double[] var23 = v5suI(var6, var22, var8, var10);
            double[] var24 = v5suI(var6, var22 + 1, var8, var10);
            double var25 = var24[0] - var23[0];
            double var27 = var24[1] - var23[1];
            double var29 = Math.sqrt(var25 * var25 + var27 * var27);
            double var31 = c1jHYPb4 * ((Waypoint)var6.get(var22 + 1)).rsDd50tfV;
            var20 += var29 / var31;
         }

         return var20;
      } else {
         return -1.0;
      }
   }

   private String ibzfxic(int var1, int var2) {
      ClientLevel var3 = Minecraft.getInstance().level;
      if (var3 == null) {
         return null;
      }

      int var4 = this.wuhB((double)var1, (double)var2);
      if (var4 < 0) {
         var4 = this.nwFPfQ;
      }

      if (var4 < 0) {
         return null;
      }

      for (TacticalMapScreen.KKeMvpuBgEY var6 : this.v5suI(var3)) {
         if (var6.v5suI() == var4) {
            StringBuilder var7 = new StringBuilder();
            ReconMarks.KKeMvpuBgEY var8 = ReconMarks.v5suI().v5suI(var6.v5suI());
            double var9 = this.v5suI(var6.wuhB(), var6.dOfFwvo(), var8);
            if (var9 >= 0.0) {
               var7.append(Component.translatable("gui.fpvdrone.map.eta", new Object[]{rsDd50tfV(var9)}).getString());
            }

            String var11 = this.v5suI(var8);
            if (var11 != null) {
               if (var7.length() > 0) {
                  var7.append("   ");
               }

               var7.append(var11);
            }

            String var12 = String.format(Locale.ROOT, "%.1f", Math.max(0.0F, var6.uSUb()));
            String var13 = String.format(Locale.ROOT, "%.0f", 20.0);
            if (var7.length() > 0) {
               var7.append("   ");
            }

            var7.append(Component.translatable("gui.fpvdrone.map.fuel", new Object[]{var12, var13}).getString());
            return var7.toString();
         }
      }

      return null;
   }

   private String v5suI(ReconMarks.KKeMvpuBgEY var1) {
      if (var1 != null && var1.jGhI2() != null && !var1.jGhI2().dOfFwvo()) {
         var var2 = var1.jGhI2().ibzfxic();
         int var3 = Mth.clamp(var1.c1jHYPb4(), 0, var2.size() - 1);
         RouteSpeed var4 = RouteSpeed.v5suI(((Waypoint)var2.get(var3)).rsDd50tfV);
         return Component.translatable("gui.fpvdrone.map.speed_readout", new Object[]{Component.translatable("gui.fpvdrone.map.speed." + var4.name().toLowerCase())})
            .getString();
      } else {
         return null;
      }
   }

   private void qlv6NKWnH() {
      this.jt9oI60ST = null;
      this.hXya = -1;
      this.oEuCBB = null;
      this.uj1PcjLg = -1;
   }

   private Component v5suI(RouteSpeed var1, boolean var2) {
      TextColor var3 = TextColor.fromRgb(var2 ? 16777215 : 12105920);
      return Component.literal("● ")
         .withStyle(var1x -> var1x.withColor(var3))
         .append(Component.translatable("gui.fpvdrone.map.speed." + var1.name().toLowerCase()).withStyle(var1x -> var1x.withColor(var3)));
   }

   private void v5suI(int var1, int var2, int var3) {
      if (var1 >= 0 && var1 < this.zQOhlhTZx.size()) {
         this.hXya = var1;
         Waypoint var4 = this.zQOhlhTZx.get(var1);
         var var5 = new ArrayList();
         var5.add(new MapPopupMenu.Item(Component.translatable("gui.fpvdrone.map.waypoint", new Object[]{var1 + 1}), null));
         var5.add(
            new MapPopupMenu.Item(Component.translatable("gui.fpvdrone.map.speed").withStyle(var0 -> var0.withColor(TextColor.fromRgb(16777215))), null)
         );
         RouteSpeed var6 = RouteSpeed.v5suI(var4.rsDd50tfV);

         for (RouteSpeed var10 : RouteSpeed.values()) {
            var5.add(new MapPopupMenu.Item(this.v5suI(var10, var10 == var6), () -> {
               this.v5suI(var1, var10);
               this.v5suI(var1, var2, var3);
            }, true));
         }

         var5.add(new MapPopupMenu.Item(Component.translatable("gui.fpvdrone.map.menu.remove_this"), () -> this.zQOhlhTZx.remove(var1)));
         int var11 = Math.min(var2, this.width - 130);
         int var12 = Math.min(var3, this.height - (var5.size() + 2) * 12 - 10);
         this.jt9oI60ST = new MapPopupMenu(var11, Math.max(22, var12), var5, this.font, 2);
         this.ibzfxic(var1, var4.ibzfxic);
      }
   }

   private void ibzfxic(int var1, double var2) {
      this.uj1PcjLg = var1;
      int var4 = this.jt9oI60ST.v5suI() + 3;
      int var5 = this.jt9oI60ST.dOfFwvo() + 12;
      int var6 = this.jt9oI60ST.wuhB() - 6;
      this.oEuCBB = new EditBox(this.font, var4, var5, var6, 10, Component.translatable("gui.fpvdrone.map.agl_field"));
      this.oEuCBB.setValue(String.valueOf((int)Math.round(var2)));
      // 1.1.8 setFilter (digits only, up to 5); 26.3 EditBox has no filter, so invalid edits are put back
      this.aglLastValid = this.oEuCBB.getValue();
      this.oEuCBB.setResponder(var1x -> {
         if (!(var1x.isEmpty() || var1x.matches("[0-9]{0,5}"))) {
            this.oEuCBB.setValue(this.aglLastValid);
            return;
         }

         this.aglLastValid = var1x;
         try {
            double var2x = Double.parseDouble(var1x);
            if (this.uj1PcjLg >= 0) {
               this.v5suI(this.uj1PcjLg, var2x);
            } else {
               this.pyHY5 = Mth.clamp(var2x, 5.0, 2000.0);
            }
         } catch (NumberFormatException var4x) {
         }
      });
      this.oEuCBB.setFocused(true);
   }

   private void wuhB(int var1, int var2) {
      this.hXya = -1;
      if (!this.zQOhlhTZx.isEmpty()) {
         Waypoint var3 = this.zQOhlhTZx.get(this.zQOhlhTZx.size() - 1);
         this.pyHY5 = var3.ibzfxic;
         this.idXga = RouteSpeed.v5suI(var3.rsDd50tfV);
      }

      this.dOfFwvo(var1, var2);
   }

   private void dOfFwvo(int var1, int var2) {
      double var3 = this.wuhB((double)var1);
      double var5 = this.dOfFwvo(var2);
      var var7 = new ArrayList();
      var7.add(
         new MapPopupMenu.Item(Component.translatable("gui.fpvdrone.map.speed").withStyle(var0 -> var0.withColor(TextColor.fromRgb(16777215))), null)
      );

      for (RouteSpeed var11 : RouteSpeed.values()) {
         var7.add(new MapPopupMenu.Item(this.v5suI(var11, var11 == this.idXga), () -> {
            this.idXga = var11;
            this.dOfFwvo(var1, var2);
         }, true));
      }

      var7.add(new MapPopupMenu.Item(Component.translatable("gui.fpvdrone.map.menu.add"), () -> this.rsDd50tfV(var3, var5)));
      var7.add(new MapPopupMenu.Item(Component.translatable("gui.fpvdrone.map.menu.loiter"), () -> this.uSUb(var3, var5)));
      int var12 = Math.min(var1, this.width - 130);
      int var13 = Math.min(var2, this.height - (var7.size() + 2) * 12 - 10);
      this.jt9oI60ST = new MapPopupMenu(var12, Math.max(22, var13), var7, this.font, 2);
      this.ibzfxic(-1, this.pyHY5);
   }

   private void uSUb(double var1, double var3) {
      this.zgQgCA9 = true;
      this.qV33e = var1;
      this.xogV5KBhg = var3;
      this.rYlh = 100.0;
      this.qlv6NKWnH();
   }

   private void hOKMYHOaC() {
      this.zgQgCA9 = false;
   }

   private void iS1JnZov() {
      if (this.zQOhlhTZx.size() >= 64) {
         this.zgQgCA9 = false;
      } else {
         this.zQOhlhTZx.add(new Waypoint(this.qV33e, this.pyHY5, this.xogV5KBhg).ibzfxic(this.rYlh).wuhB(this.idXga.dOfFwvo));
         this.lvk8rWUGD = TerminalMode.LOITER;
         this.wEJx3Itb();
         this.zgQgCA9 = false;
      }
   }

   private static double[] v5suI(List<Waypoint> var0, int var1, double var2, double var4) {
      Waypoint var6 = (Waypoint)var0.get(var1);
      if (var1 == var0.size() - 1 && var6.dOfFwvo > 0.0) {
         double var7 = var1 > 0 ? ((Waypoint)var0.get(var1 - 1)).v5suI : var2;
         double var9 = var1 > 0 ? ((Waypoint)var0.get(var1 - 1)).wuhB : var4;
         return v5suI(var6.v5suI, var6.wuhB, var6.dOfFwvo, var7, var9);
      } else {
         return new double[]{var6.v5suI, var6.wuhB};
      }
   }

   private static double[] v5suI(double var0, double var2, double var4, double var6, double var8) {
      double var10 = var6 - var0;
      double var12 = var8 - var2;
      double var14 = Math.sqrt(var10 * var10 + var12 * var12);
      return var14 < 1.0E-6 ? new double[]{var0 + var4, var2} : new double[]{var0 + var4 * var10 / var14, var2 + var4 * var12 / var14};
   }

   private void lfyfLwuw() {
      if (!this.zQOhlhTZx.isEmpty() || this.ifpFOCaAs) {
         FlightRoute var1 = new FlightRoute(new ArrayList<>(this.zQOhlhTZx), this.lvk8rWUGD);
         ModNetwork.CHANNEL.sendToServer(new RouteAssignPacket(this.sTrE, var1, this.ifpFOCaAs));
         this.rOIp0p7 = System.currentTimeMillis();
         if (this.ifpFOCaAs) {
            this.onClose();
         } else if (this.n9lX8A) {
            this.v5suI(this.sTrE);
         }
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   private static final class KKeMvpuBgEY {
      private final int v5suI;
      private final String ibzfxic;
      private final double wuhB;
      private final double dOfFwvo;
      private final float rsDd50tfV;
      private final float uSUb;

      private KKeMvpuBgEY(int var1, String var2, double var3, double var5, float var7, float var8) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var5;
         this.rsDd50tfV = var7;
         this.uSUb = var8;
      }


      public int v5suI() {

         return this.v5suI;

      }

   
      public String ibzfxic() {
         return this.ibzfxic;
      }

      public double wuhB() {
         return this.wuhB;
      }

      public double dOfFwvo() {
         return this.dOfFwvo;
      }

      public float rsDd50tfV() {
         return this.rsDd50tfV;
      }

      public float uSUb() {
         return this.uSUb;
      }
   }
}
