package com.fpvdrone.data;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class BoneBox {
   private final Vector3f v5suI;
   private final Vector3f ibzfxic;
   private final Quaternionf wuhB;
   private final double dOfFwvo;
   private final String rsDd50tfV;

   public BoneBox(Vector3f var1, Vector3f var2, Quaternionf var3, double var4, String var6) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
      this.dOfFwvo = var4;
      this.rsDd50tfV = var6;
   }

   public BoneBox v5suI(double var1) {
      return new BoneBox(this.v5suI, this.ibzfxic, this.wuhB, var1, this.rsDd50tfV);
   }

   public OrientedBox v5suI(Vec3 var1, Quaternionf var2, Vector3f var3) {
      Vector3f var4 = new Vector3f(this.v5suI);
      if (var3 != null) {
         var4.add(var3);
      }

      Vector3f var5 = var2.transform(var4, new Vector3f());
      Vec3 var6 = var1.add(var5.x, var5.y, var5.z);
      Quaternionf var7 = var2.mul(this.wuhB, new Quaternionf());
      return new OrientedBox(var6, new Vector3f(this.ibzfxic), var7);
   }

   public Vector3f v5suI() {
      return this.v5suI;
   }

   public Vector3f ibzfxic() {
      return this.ibzfxic;
   }

   public Quaternionf wuhB() {
      return this.wuhB;
   }

   public double dOfFwvo() {
      return this.dOfFwvo;
   }

   public String rsDd50tfV() {
      return this.rsDd50tfV;
   }
}
