package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import com.fpvdrone.physics.PhysicsState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class DroneSoundInstance extends AbstractTickableSoundInstance {
    private final LocalPlayer player;

    public DroneSoundInstance(LocalPlayer player) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.1F;
    }

    public void tick() {
        if (this.player != null
            && !this.player.isRemoved()
            && FPVMod.isArmed(this.player.getUUID())
            && !BatteryManager.getInstance().isDead()
            && !SignalManager.getInstance().isForcedNoSignal()) {
            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
            float throttle = (ControllerReader.getThrottle() + 1.0F) / 2.0F;
            PhysicsState physics = FPVMod.getPhysicsState(this.player.getUUID());
            if (physics != null && physics.isThrottleCut()) {
                throttle = 0.0F;
            }

            float speed = (float)this.player.getDeltaMovement().length();
            float intensity = Math.max(throttle, speed * 0.3F);
            this.volume = Mth.clamp(0.15F + intensity * 0.85F, 0.15F, 1.0F);
            this.pitch = Mth.clamp(0.7F + intensity * 0.8F, 0.7F, 1.5F);
        } else {
            this.stop();
        }
    }
}
