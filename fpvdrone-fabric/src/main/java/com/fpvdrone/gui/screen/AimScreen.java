package com.fpvdrone.gui.screen;

import com.fpvdrone.client.KeyManager;
import com.fpvdrone.client.AimMath;
import com.fpvdrone.entity.LoiteringMunitionEntity;
import com.fpvdrone.entity.PilotBodyEntity;
import com.fpvdrone.client.MunitionClientState;
import com.fpvdrone.render.CameraZoom;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class AimScreen extends Screen {
   private static final double v5suI = 2048.0;
   private final int ibzfxic;

   public AimScreen(int var1) {
      super(Component.translatable("gui.fpvdrone.aim.title"));
      this.ibzfxic = var1;
   }

   public boolean isPauseScreen() {
      return false;
   }

   protected void init() {
   }

   public void render(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
   }

   private LoiteringMunitionEntity v5suI() {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.level == null) {
         return null;
      } else {
         return var1.level.getEntity(this.ibzfxic) instanceof LoiteringMunitionEntity var3 ? var3 : null;
      }
   }

   private static AimMath.KKeMvpuBgEY v5suI(Camera var0) {
      return AimMath.v5suI(var0.yRot(), var0.xRot());
   }

   public boolean mouseClicked(MouseButtonEvent var7, boolean var8) {
      if (var7.button() == 0) {
         this.v5suI(var7.x(), var7.y());
         return true;
      } else {
         return super.mouseClicked(var7, var8);
      }
   }

   public boolean mouseScrolled(double var1, double var3, double var7, double var5) {
      if (var5 != 0.0 && MunitionClientState.ibzfxic()) {
         CameraZoom.getInstance().scroll((float)var5, MunitionClientState.dOfFwvo());
         return true;
      } else {
         return super.mouseScrolled(var1, var3, var7, var5);
      }
   }

   private void v5suI(double var1, double var3) {
      Minecraft var5 = Minecraft.getInstance();
      if (var5.level != null) {
         Camera var6 = var5.gameRenderer.mainCamera();
         Vec3 var7 = var6.position();
         Vec3 var8 = this.v5suI(var5, var6, var1, var3);
         v5suI(var7, var8, this.v5suI());
      }
   }

   public static void v5suI(Vec3 var0, Vec3 var1, LoiteringMunitionEntity var2) {
      Minecraft var3 = Minecraft.getInstance();
      if (var3.level != null) {
         Vec3 var4 = var0.add(var1.scale(2048.0));
         BlockHitResult var5 = var3.level.clip(new ClipContext(var0, var4, Block.OUTLINE, Fluid.NONE, var3.player));
         Vec3 var6 = var5.getType() == Type.BLOCK ? var5.getLocation() : var4;
         AABB var7 = new AABB(var0, var6).inflate(3.5);
         double var8 = var0.distanceToSqr(var6);
         EntityHitResult var10 = v5suI(
            var3.level,
            var3.player,
            var0,
            var6,
            var7,
            var1x -> var1x.isPickable() && !var1x.isSpectator() && var1x != var2 && !(var1x instanceof PilotBodyEntity),
            var8
         );
         if (var10 != null) {
            MunitionClientState.v5suI(var10.getEntity(), var10.getLocation());
         } else {
            MunitionClientState.v5suI(var6.x, var6.y, var6.z);
         }
      }
   }

   private static EntityHitResult v5suI(Level var0, Entity var1, Vec3 var2, Vec3 var3, AABB var4, Predicate<Entity> var5, double var6) {
      Entity var8 = null;
      Vec3 var9 = null;
      double var10 = var6;

      for (Entity var13 : var0.getEntities(var1, var4, var5)) {
         AABB var14 = MunitionClientState.v5suI(var13).inflate(var13.getPickRadius());
         var var15 = var14.clip(var2, var3);
         Vec3 var16;
         double var17;
         if (var14.contains(var2)) {
            var16 = var15.orElse(var2);
            var17 = 0.0;
         } else {
            if (!var15.isPresent()) {
               continue;
            }

            var16 = (Vec3)var15.get();
            var17 = var2.distanceToSqr(var16);
         }

         if (var17 <= var10) {
            var8 = var13;
            var9 = var16;
            var10 = var17;
         }
      }

      return var8 == null ? null : new EntityHitResult(var8, var9);
   }

   private Vec3 v5suI(Minecraft var1, Camera var2, double var3, double var5) {
      AimMath.KKeMvpuBgEY var7 = v5suI(var2);
      double var8 = var3 / this.width * 2.0 - 1.0;
      double var10 = (this.height / 2.0 - var5) / (this.height / 2.0);
      double var12 = this.v5suI(var1);
      double var14 = (double)this.width / Math.max(1, this.height);
      double var16 = Math.tan(Math.toRadians(var12) * 0.5);
      double var18 = var8 * var16 * var14;
      double var20 = var10 * var16;
      Vec3 var22 = new Vec3(
         var7.v5suI() + var7.jGhI2() * var18 + var7.dOfFwvo() * var20,
         var7.ibzfxic() + var7.wEJx3Itb() * var18 + var7.rsDd50tfV() * var20,
         var7.wuhB() + var7.bAcdvq9() * var18 + var7.uSUb() * var20
      );
      return var22.normalize();
   }

   private double v5suI(Minecraft var1) {
      return MunitionClientState.iAKNPAj();
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean keyPressed(KeyEvent var3) {
      if (var3.key() == 256) {
         Minecraft.getInstance().gui.setScreen(new PauseScreen(true));
         return true;
      }

      if (KeyManager.EXIT_KEY.matches(var3)) {
         MunitionClientState.qU1QioDiC();
         return true;
      }

      if (KeyManager.OPEN_MAP_KEY.matches(var3)) {
         MunitionClientState.zQOhlhTZx();
         Minecraft.getInstance().gui.setScreen(new TacticalMapScreen());
         return true;
      }

      if (KeyManager.BOMB_KEY.matches(var3)) {
         KeyManager.handleDropBomb(Minecraft.getInstance());
         return true;
      }

      if (KeyManager.ZOOM_KEY.matches(var3) && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
         KeyManager.handleZoom(Minecraft.getInstance());
         return true;
      }

      Minecraft var4 = Minecraft.getInstance();
      if (var4.options.keyTogglePerspective.matches(var3)) {
         CameraType var5 = var4.options.getCameraType();
         CameraType var6 = var5.cycle();
         var4.options.setCameraType(var6);
         if (var5.isFirstPerson() != var6.isFirstPerson()) {
            var4.gameRenderer.checkEntityPostEffect(var6.isFirstPerson() ? var4.getCameraEntity() : null);
         }

         return true;
      } else {
         var4 = Minecraft.getInstance();
         if (var4.options.keyChat.matches(var3)) {
            var4.gui.setScreen(new ChatScreen("", false));
            return true;
         } else if (var4.options.keyCommand.matches(var3)) {
            var4.gui.setScreen(new ChatScreen("/", false));
            return true;
         } else {
            return super.keyPressed(var3);
         }
      }
   }
}
