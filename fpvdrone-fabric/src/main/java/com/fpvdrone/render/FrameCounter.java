package com.fpvdrone.render;

public final class FrameCounter {
   private static long frameId;

   private FrameCounter() {
   }

   public static void increment() {
      frameId++;
   }

   public static long get() {
      return frameId;
   }
}
