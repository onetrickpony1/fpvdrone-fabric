package com.fpvdrone.sync;

import com.fpvdrone.sync.SmoothingMode;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface RemoteSmoothed {
   SmoothingMode b69s();

   RemoteSmoother cXHvq();

   Extrapolator rxYnaa();

   void v5suI(KinematicState var1);

   void v5suI(KinematicState var1, boolean var2);

   default void a_() {
   }

   default boolean hOKMYHOaC() {
      return true;
   }

   @Nullable
   default UUID iS1JnZov() {
      return null;
   }

   @Nullable
   default Vec3 lfyfLwuw() {
      return null;
   }

   default double r6s7FL3() {
      return 8.0;
   }

   default double iAKNPAj() {
      return 0.12;
   }

   default double qlv6NKWnH() {
      return 0.5;
   }
}
