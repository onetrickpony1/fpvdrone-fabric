package com.fpvdrone.client.thermal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/**
 * 1.1.8 NUCManager: non-uniformity correction. Every 3-5 minutes (or on the NUC key) the camera clicks, "freezes"
 * for 0.8 s, and afterwards its fixed-pattern noise is 40% lower, wearing off over 3 minutes.
 */
public class NUCManager {
   private static final NUCManager INSTANCE = new NUCManager();
   static final long AUTO_MIN_MS = 180000L;
   static final long AUTO_RANGE_MS = 120000L;
   static final long FREEZE_MS = 800L;
   static final float FPN_REDUCTION = 0.4F;
   static final long FPN_DECAY_MS = 180000L;
   private long lastNUCTime;
   private long nextAutoInterval;
   private boolean freezeActive = false;
   private long freezeStartTime;
   private float fpnReduction = 0.0F;
   private long reductionStartTime;

   NUCManager() {
      this.lastNUCTime = System.currentTimeMillis();
      this.nextAutoInterval = randomInterval();
   }

   public static NUCManager getInstance() {
      return INSTANCE;
   }

   /** Every client tick while thermal is on. */
   public void tick() {
      long now = System.currentTimeMillis();
      if (this.freezeActive) {
         if (now - this.freezeStartTime >= FREEZE_MS) {
            this.freezeActive = false;
            this.fpnReduction = FPN_REDUCTION;
            this.reductionStartTime = now;
         }
      } else {
         if (this.fpnReduction > 0.0F) {
            long elapsed = now - this.reductionStartTime;
            if (elapsed >= FPN_DECAY_MS) {
               this.fpnReduction = 0.0F;
            } else {
               this.fpnReduction = FPN_REDUCTION * (1.0F - (float)elapsed / FPN_DECAY_MS);
            }
         }

         if (now - this.lastNUCTime >= this.nextAutoInterval) {
            this.triggerNUC();
         }
      }
   }

   public void triggerNUC() {
      long now = System.currentTimeMillis();
      this.lastNUCTime = now;
      this.nextAutoInterval = randomInterval();
      this.freezeActive = true;
      this.freezeStartTime = now;
      this.playClickSound();
   }

   /** 1.1.8 dOfFwvo(): nothing reads it in 1.1.8 (the picture does not actually freeze). */
   public boolean isFreezeActive() {
      return this.freezeActive;
   }

   public float getFpnReduction() {
      return this.fpnReduction;
   }

   public void reset() {
      this.freezeActive = false;
      this.fpnReduction = 0.0F;
      this.lastNUCTime = System.currentTimeMillis();
      this.nextAutoInterval = randomInterval();
   }

   private void playClickSound() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getSoundManager() != null) {
         mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }
   }

   private static long randomInterval() {
      return AUTO_MIN_MS + (long)(Math.random() * AUTO_RANGE_MS);
   }
}
