package com.fpvdrone.util;

public final class BitFlags {
   private BitFlags() {
   }

   public static boolean v5suI(byte var0, int var1) {
      return var1 >= 0 && var1 < 8 ? (var0 & 1 << var1) != 0 : false;
   }

   public static byte ibzfxic(byte var0, int var1) {
      return var1 >= 0 && var1 < 8 ? (byte)(var0 | 1 << var1) : var0;
   }

   public static byte wuhB(byte var0, int var1) {
      return var1 >= 0 && var1 < 8 ? (byte)(var0 & ~(1 << var1)) : var0;
   }

   public static int dOfFwvo(byte var0, int var1) {
      for (int var2 = 0; var2 < var1; var2++) {
         if (v5suI(var0, var2)) {
            return var2;
         }
      }

      return -1;
   }

   public static int rsDd50tfV(byte var0, int var1) {
      for (int var2 = 0; var2 < var1; var2++) {
         if (!v5suI(var0, var2)) {
            return var2;
         }
      }

      return -1;
   }

   public static boolean uSUb(byte var0, int var1) {
      return var1 > 0 && var0 == v5suI(var1);
   }

   public static byte v5suI(int var0) {
      return var0 <= 0 ? 0 : (byte)((1 << var0) - 1);
   }
}
