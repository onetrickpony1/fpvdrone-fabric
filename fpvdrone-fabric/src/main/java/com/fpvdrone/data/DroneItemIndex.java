package com.fpvdrone.data;

import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.data.DroneDefinitions;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DroneItemIndex {
   private static final Map<String, String> v5suI = new HashMap<>();

   private DroneItemIndex() {
   }

   public static void v5suI() {
      v5suI.clear();

      for (DroneDefinition var1 : DroneDefinitions.v5suI().values()) {
         v5suI.put(var1.rxYnaa(), var1.wyNDWDB());
      }
   }

   public static Optional<String> v5suI(String var0) {
      return var0 != null && !var0.isEmpty() ? Optional.ofNullable(v5suI.get(var0)) : Optional.empty();
   }

   public static Map<String, String> ibzfxic() {
      return Collections.unmodifiableMap(v5suI);
   }
}
