package com.fpvdrone.data;

import com.fpvdrone.entity.DroneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/** 1.1.8 (Record_PaBwTEfy): where payloads sit on a drone and where they are released, in world space. */
public final class PayloadGeometry {
   private PayloadGeometry() {
   }

   public static Vec3 v5suI(Minecraft var0) {
      return new Vec3(
         var0.player.xOld + (var0.player.getX() - var0.player.xOld) * var0.getDeltaTracker().getGameTimeDeltaPartialTick(true),
         var0.player.yOld + (var0.player.getY() - var0.player.yOld) * var0.getDeltaTracker().getGameTimeDeltaPartialTick(true),
         var0.player.zOld + (var0.player.getZ() - var0.player.zOld) * var0.getDeltaTracker().getGameTimeDeltaPartialTick(true)
      );
   }

   public static Vec3 v5suI(DroneEntity var0, AttachTransform var1, Vec3 var2, Vec3 var3, Vec3 var4) {
      return v5suI(var0, var1, var2, var3, var4, 0.0F, 0.0F, 0.0F);
   }

   public static Vec3 v5suI(DroneEntity var0, AttachTransform var1, Vec3 var2, Vec3 var3, Vec3 var4, float var5, float var6, float var7) {
      double var8 = -(var1.ibzfxic() + var5) / 16.0;
      double var10 = (var1.wuhB() + var6) / 16.0;
      double var12 = (var1.dOfFwvo() + var7) / 16.0;
      double var14 = Math.toRadians(var0.kNWIu());
      double var16 = Math.cos(var14);
      double var18 = Math.sin(var14);
      double var20 = var8 * var16 + var12 * var18;
      double var22 = var10;
      double var24 = -var8 * var18 + var12 * var16;
      return var2.scale(-var20).add(var3.scale(var22)).add(var4.scale(-var24));
   }

   public static PayloadGeometry.KKeMvpuBgEY v5suI(DroneEntity var0) {
      int var1 = var0.mM7DsB9AR() ? var0.zQOhlhTZx() : -1;
      if (var1 >= 0) {
         AttachTransform var5 = var0.oEuCBB().uKpZYj().stations().get(var1).wuhB();
         String var6 = var0.oEuCBB().uKpZYj().projectile();
         return new PayloadGeometry.KKeMvpuBgEY(var5, false, var1, var6);
      } else {
         String var2 = var0.i263D9();
         ProjectileDefinition var3 = var2 != null && !var2.isEmpty() ? ProjectileDefinitions.ibzfxic(var2).orElse(null) : null;
         if (var3 != null && var3.rsDd50tfV()) {
            AttachTransform var4 = var0.oEuCBB().dOfFwvo(var2).orElse(AttachTransform.v5suI("payload_mount"));
            return new PayloadGeometry.KKeMvpuBgEY(var4, true, -1, var2);
         } else {
            return null;
         }
      }
   }

   /** 1.1.8: what the drop key would release next (attach point, hanging payload or bomb-bay station, projectile id). */
   public record KKeMvpuBgEY(AttachTransform v5suI, boolean ibzfxic, int wuhB, String dOfFwvo) {
   }
}
