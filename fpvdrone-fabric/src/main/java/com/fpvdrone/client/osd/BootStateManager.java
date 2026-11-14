package com.fpvdrone.client.osd;

import java.util.function.LongSupplier;

public final class BootStateManager {
    private static final BootStateManager INSTANCE = new BootStateManager();
    static final long LOGO_DURATION_MS = 3000L;
    static final long TRANSITION_DURATION_MS = 1000L;
    private long bootStartMs = 0L;
    private boolean active = false;
    LongSupplier clock = System::currentTimeMillis;

    private BootStateManager() {
    }

    public static BootStateManager getInstance() {
        return INSTANCE;
    }

    public void start() {
        this.bootStartMs = this.clock.getAsLong();
        this.active = true;
    }

    public void reset() {
        this.active = false;
        this.bootStartMs = 0L;
    }

    public BootStateManager.Phase getPhase() {
        if (!this.active) {
            return BootStateManager.Phase.IDLE;
        } else {
            long elapsed = this.clock.getAsLong() - this.bootStartMs;
            if (elapsed < 3000L) {
                return BootStateManager.Phase.LOGO;
            } else if (elapsed < 4000L) {
                return BootStateManager.Phase.TRANSITION;
            } else {
                this.active = false;
                return BootStateManager.Phase.IDLE;
            }
        }
    }

    public boolean isBootActive() {
        return this.getPhase() != BootStateManager.Phase.IDLE;
    }

    public long getElapsedMs() {
        return !this.active ? 0L : this.clock.getAsLong() - this.bootStartMs;
    }

    public long getTransitionElapsedMs() {
        return this.getPhase() != BootStateManager.Phase.TRANSITION ? 0L : this.clock.getAsLong() - this.bootStartMs - 3000L;
    }

    public static enum Phase {
        IDLE,
        LOGO,
        TRANSITION;
    }
}
