package com.fpvdrone.data;

import com.fpvdrone.data.ShrapnelStats;
import com.fpvdrone.entity.ShrapnelExplosion;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ShrapnelDetonator implements Detonator {
   public static final String v5suI = "shrapnel";

   @Override
   public void detonate(
      ServerLevel var1,
      Vec3 var2,
      float var3,
      int var4,
      Entity var5,
      boolean var6,
      @Nullable UUID var7,
      ShrapnelStats var8,
      @Nullable String var9,
      boolean var10
   ) {
      // 1.1.8-PENDING: stats (var8), sound id (var9) and the last flag go to the 1.1.8 ShrapnelExplosion (effects branch).
      ShrapnelExplosion.detonate(var1, var2, var3, var4, var5, var6, var7);
   }
}
