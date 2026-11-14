package com.fpvdrone.client.thermal;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/** 1.1.8 UniformManager: the world values the thermal shaders use (time of day, biome, rain). */
public class UniformManager {
   private static final UniformManager INSTANCE = new UniformManager();
   private float gameTime;
   private float biomeTemp;
   private float weatherMod = 1.0F;
   private float agcMin;
   private float agcMax = 1.0F;
   private float ambientTemp;
   private float atmosphericAbsorption = 0.003F;
   private int frameCount;
   private int tickCounter;

   UniformManager() {
   }

   public static UniformManager getInstance() {
      return INSTANCE;
   }

   /** Every rendered frame. */
   public void updatePerFrame(Level level) {
      if (level != null) {
         this.gameTime = (float)(level.getDefaultClockTime() % 24000L / 24000.0);
         this.frameCount++;
         // (1.1.8 Level.getDayTime is the default clock in 26.3.) 1.1.8 computed the ambient temperature only in updatePerThermalFrame, which nothing called, so it stayed 0.
         // Kept that way for the same picture.
      }
   }

   /** Every client tick. */
   public void tick(Level level) {
      if (level != null) {
         float rain = level.getRainLevel(1.0F);
         this.weatherMod = 1.0F - rain;
         this.atmosphericAbsorption = 0.003F * (1.0F + rain * 4.0F);
         if (++this.tickCounter >= 20) {
            this.tickCounter = 0;
            this.updatePer20Ticks(level);
         }
      }
   }

   void updatePer20Ticks(Level level) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         BlockPos pos = mc.player.blockPosition();
         Biome biome = level.getBiome(pos).value();
         this.biomeTemp = Math.max(0.0F, Math.min(1.0F, (biome.getBaseTemperature() + 0.5F) / 2.5F));
      }
   }

   /** 1.1.8 updatePerThermalFrame (no caller in 1.1.8). */
   public void updatePerThermalFrame(float agcMin, float agcMax) {
      this.agcMin = agcMin;
      this.agcMax = agcMax;
      this.ambientTemp = computeAmbient(this.biomeTemp, this.gameTime);
   }

   public static float computeAmbient(float biomeTemp, float gameTime) {
      float day = Math.max(0.0F, (float)Math.cos((gameTime - 0.25F) * 2.0 * Math.PI));
      return biomeTemp * 0.15F + day * 0.08F;
   }

   public float getGameTime() {
      return this.gameTime;
   }

   public float getBiomeTemp() {
      return this.biomeTemp;
   }

   public float getWeatherMod() {
      return this.weatherMod;
   }

   public float getAgcMin() {
      return this.agcMin;
   }

   public float getAgcMax() {
      return this.agcMax;
   }

   public float getAmbientTemp() {
      return this.ambientTemp;
   }

   public float getAtmosphericAbsorption() {
      return this.atmosphericAbsorption;
   }

   public int getFrameCount() {
      return this.frameCount;
   }
}
