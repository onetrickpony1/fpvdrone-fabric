package com.fpvdrone.entity;

import com.fpvdrone.data.OrientedBox;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public interface DamageZoneTarget {
   List<OrientedBox> v5suI(float var1);

   default boolean v5suI() {
      return !this.v5suI(1.0F).isEmpty();
   }

   default boolean ibzfxic() {
      return false;
   }

   default boolean v5suI(DamageSource var1, float var2) {
      return false;
   }

   default Vec3 v5suI(Vec3 var1, Vec3 var2, float var3) {
      Vec3 var4 = null;
      double var5 = Double.MAX_VALUE;

      for (OrientedBox var8 : this.v5suI(var3)) {
         Vec3 var9 = var8.v5suI(var1, var2);
         if (var9 != null) {
            double var10 = var1.distanceToSqr(var9);
            if (var10 < var5) {
               var5 = var10;
               var4 = var9;
            }
         }
      }

      return var4;
   }
}
