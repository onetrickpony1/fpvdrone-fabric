package com.fpvdrone.entity;

import com.fpvdrone.data.BoneBox;
import com.fpvdrone.data.GeoBoneBoxes;
import com.fpvdrone.data.PlacedInteractZone;
import com.fpvdrone.data.InteractZone;

import com.fpvdrone.data.BoneBox;
import com.fpvdrone.data.PlacedInteractZone;
import com.fpvdrone.data.InteractZone;
import com.fpvdrone.entity.DroneEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class InteractZoneLocator {
   private InteractZoneLocator() {
   }

   public static List<PlacedInteractZone> v5suI(DroneEntity var0, float var1, Map<String, InteractZone> var2) {
      if (var2.isEmpty()) {
         return List.of();
      }

      String var3 = var0.oEuCBB().hOKMYHOaC();
      List<BoneBox> var4 = GeoBoneBoxes.dOfFwvo(var3);
      if (var4.isEmpty()) {
         return List.of();
      }

      Quaternionf var5 = var0.getRemoteOrientation(var1);
      double var6 = Mth.lerp(var1, var0.xOld, var0.getX());
      double var8 = Mth.lerp(var1, var0.yOld, var0.getY());
      double var10 = Mth.lerp(var1, var0.zOld, var0.getZ());
      Vec3 var12 = new Vec3(var6, var8, var10);
      ArrayList<PlacedInteractZone> var13 = new ArrayList<>(var4.size());

      for (BoneBox var15 : var4) {
         InteractZone var16 = (InteractZone)var2.get(var15.rsDd50tfV());
         if (var16 != null) {
            var13.add(new PlacedInteractZone(var16.v5suI(), var15.v5suI(var12, var5, null)));
         }
      }

      return var13;
   }
}
