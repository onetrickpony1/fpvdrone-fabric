package com.fpvdrone.client;

import com.fpvdrone.FpvConfig;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.block.JammerZones;
import com.fpvdrone.entity.DroneEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Signal between pilot and drone (1.1.8): range and freeze distance come from the drone's definition, scaled by the
 * global signal range multiplier. 1.1.8 also multiplied the strength by a factor its anti-tamper check lowered to 0.85
 * on copies it judged modified; that is left out.
 */
public class SignalManager {
   private static final SignalManager instance = new SignalManager();
   private double ibzfxic = 0.0;
   private float signalStrength = 1.0F;
   private float dOfFwvo = 1.0F;
   private boolean controlBlocked = false;
   private long lastUpdateTick = 0L;
   private boolean forcedNoSignal = false;
   private boolean wEJx3Itb = false;
   private Level level = null;
   private DroneDefinition.Signal c1jHYPb4 = DroneDefinition.Signal.v5suI;
   private int graceTicksRemaining = 0;
   private static final int zTT3nw6 = 60;
   private Vec3 origin = null;

   private SignalManager() {
   }

   public static SignalManager getInstance() {
      return instance;
   }

   public void setOrigin(Vec3 var1) {
      this.origin = var1;
   }

   /** Where the signal is measured from (the pilot, or the DJI controller). */
   public Vec3 getOrigin() {
      return this.origin;
   }

   public void setLevel(Level var1) {
      this.level = var1;
      SignalCalculator.setLevel(var1);
   }

   public void tick(DroneEntity var1) {
      if (var1 != null) {
         this.c1jHYPb4 = var1.oEuCBB().vmpmU().v5suI(FpvConfig.getSignalRangeMultiplier());
         this.setLevel(var1.level());
         this.tick(var1.position(), var1.level().getGameTime());
      }
   }

   public int getMaxRange() {
      return this.c1jHYPb4.maxRange();
   }

   public void tick(Vec3 var1, long var2) {
      if (this.wEJx3Itb) {
         this.ibzfxic = this.origin != null ? SignalCalculator.calculateDistance(var1, this.origin) : 0.0;
         float var7 = JammerZones.signalFactor(var1);
         this.signalStrength = var7;
         this.dOfFwvo = var7;
         this.controlBlocked = var7 <= 0.01F;
         this.lastUpdateTick = var2;
      } else if (this.forcedNoSignal) {
         this.signalStrength = 0.0F;
         this.dOfFwvo = 0.0F;
         this.controlBlocked = true;
      } else if (this.graceTicksRemaining > 0) {
         this.graceTicksRemaining--;
         this.ibzfxic = 0.0;
         this.signalStrength = 1.0F;
         this.dOfFwvo = 1.0F;
         this.controlBlocked = false;
      } else if (this.origin == null) {
         this.ibzfxic = 0.0;
         this.signalStrength = 1.0F;
         this.dOfFwvo = 1.0F;
         this.controlBlocked = false;
      } else {
         this.ibzfxic = SignalCalculator.calculateDistance(var1, this.origin);
         double var4 = this.c1jHYPb4.maxRange();
         if (this.ibzfxic >= var4) {
            this.signalStrength = 0.0F;
            this.dOfFwvo = 0.0F;
            this.controlBlocked = true;
            this.lastUpdateTick = var2;
         } else {
            if (this.level != null) {
               this.signalStrength = SignalCalculator.v5suI(var1, this.origin, this.level, this.c1jHYPb4);
            } else {
               this.signalStrength = 1.0F;
            }

            this.dOfFwvo = SignalCalculator.v5suI(this.ibzfxic, this.c1jHYPb4);
            float var6 = JammerZones.signalFactor(var1);
            if (var6 < this.signalStrength) {
               this.signalStrength = var6;
               this.dOfFwvo = Math.min(this.dOfFwvo, var6);
            }

            this.controlBlocked = this.signalStrength <= 0.01F;
            this.lastUpdateTick = var2;
         }
      }
   }

   public float getSignalStrength() {
      return this.signalStrength;
   }

   public float getFreezeSignal() {
      return this.dOfFwvo;
   }

   public double getDistance() {
      return this.ibzfxic;
   }

   public boolean isControlBlocked() {
      return this.controlBlocked;
   }

   public void forceNoSignal() {
      this.forcedNoSignal = true;
      this.signalStrength = 0.0F;
      this.dOfFwvo = 0.0F;
      this.controlBlocked = true;
      this.graceTicksRemaining = 0;
   }

   public boolean isForcedNoSignal() {
      return this.forcedNoSignal;
   }

   /** 1.1.8: DJI link mode (Mavic flown from its controller): full signal at any distance, cut only by jammer zones. */
   public void setDjiLink(boolean var1) {
      this.wEJx3Itb = var1;
      if (var1) {
         this.forcedNoSignal = false;
         this.signalStrength = 1.0F;
         this.dOfFwvo = 1.0F;
         this.controlBlocked = false;
      }
   }

   public boolean isDjiLink() {
      return this.wEJx3Itb;
   }

   public boolean isInGracePeriod() {
      return this.graceTicksRemaining > 0;
   }

   public void reset() {
      this.origin = null;
      this.ibzfxic = 0.0;
      this.signalStrength = 1.0F;
      this.dOfFwvo = 1.0F;
      this.controlBlocked = false;
      this.lastUpdateTick = 0L;
      this.forcedNoSignal = false;
      this.wEJx3Itb = false;
      this.graceTicksRemaining = 60;
      this.level = null;
      this.c1jHYPb4 = DroneDefinition.Signal.v5suI;
      SignalCalculator.invalidateCache();
   }
}
