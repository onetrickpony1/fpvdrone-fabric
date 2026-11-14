package com.fpvdrone.data;

import com.fpvdrone.data.OrientedBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OrientedBoxGrid {
   private final double v5suI;
   private final Map<Long, List<OrientedBox>> ibzfxic = new HashMap<>();

   public OrientedBoxGrid(double var1) {
      this.v5suI = var1;
   }

   public void v5suI() {
      this.ibzfxic.clear();
   }

   public void v5suI(OrientedBox var1) {
      long var2 = this.ibzfxic(var1.jGhI2().x, var1.jGhI2().y, var1.jGhI2().z);
      this.ibzfxic.computeIfAbsent(var2, var0 -> new ArrayList<>()).add(var1);
   }

   public List<OrientedBox> v5suI(double var1, double var3, double var5) {
      long var7 = this.ibzfxic(var1, var3, var5);
      return this.ibzfxic.getOrDefault(var7, List.of());
   }

   private long ibzfxic(double var1, double var3, double var5) {
      long var7 = (long)Math.floor(var1 / this.v5suI);
      long var9 = (long)Math.floor(var3 / this.v5suI);
      long var11 = (long)Math.floor(var5 / this.v5suI);
      return var7 * 73856093L ^ var9 * 19349663L ^ var11 * 83492791L;
   }

   public int ibzfxic() {
      return this.ibzfxic.size();
   }
}
