package com.fpvdrone.data;

public enum TerminalMode {
   LOITER,
   DIVE,
   DESTROY;

   public static TerminalMode v5suI(int var0) {
      TerminalMode[] var1 = values();
      return var0 >= 0 && var0 < var1.length ? var1[var0] : LOITER;
   }

   public TerminalMode v5suI() {
      TerminalMode[] var1 = values();
      return var1[(this.ordinal() + 1) % var1.length];
   }
}
