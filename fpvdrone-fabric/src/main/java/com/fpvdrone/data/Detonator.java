package com.fpvdrone.data;

import com.fpvdrone.data.ShrapnelStats;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface Detonator {
   void detonate(
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
   );
}
