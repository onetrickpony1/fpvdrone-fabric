package com.fpvdrone.client;

import java.util.function.Consumer;

public class RangeListener {
    public static final long DEFAULT_DURATION_MS = 5000L;
    private static final RangeListener instance = new RangeListener();
    private float[] minValues;
    private float[] maxValues;
    private Consumer<float[]> onComplete;
    private boolean listening = false;
    private long startTime;
    private long durationMs;

    public static RangeListener getInstance() {
        return instance;
    }

    public void startListening(Consumer<float[]> callback) {
        this.startListening(callback, 5000L);
    }

    public void startListening(Consumer<float[]> callback, long durationMs) {
        InputListener.getInstance().stopListening();
        this.onComplete = callback;
        this.durationMs = durationMs;
        this.startTime = System.currentTimeMillis();
        ControllerReader reader = ControllerReader.getInstance();
        int axisCount = reader.getAxisCount();
        this.minValues = new float[axisCount];
        this.maxValues = new float[axisCount];

        for (int i = 0; i < axisCount; i++) {
            float current = reader.getRawAxisValue(i);
            this.minValues[i] = current;
            this.maxValues[i] = current;
        }

        this.listening = true;
    }

    public void stopListening() {
        this.listening = false;
        this.onComplete = null;
    }

    public boolean isListening() {
        return this.listening;
    }

    public float getProgress() {
        if (!this.listening) {
            return 0.0F;
        } else {
            long elapsed = System.currentTimeMillis() - this.startTime;
            return Math.min(1.0F, (float)elapsed / (float)this.durationMs);
        }
    }

    public long getRemainingMs() {
        if (!this.listening) {
            return 0L;
        } else {
            long elapsed = System.currentTimeMillis() - this.startTime;
            return Math.max(0L, this.durationMs - elapsed);
        }
    }

    public void tick() {
        if (this.listening && this.onComplete != null) {
            ControllerReader reader = ControllerReader.getInstance();
            if (reader.isPhysicalControllerConnected()) {
                int axisCount = reader.getAxisCount();

                for (int i = 0; i < axisCount && i < this.minValues.length; i++) {
                    float current = reader.getRawAxisValue(i);
                    if (current < this.minValues[i]) {
                        this.minValues[i] = current;
                    }

                    if (current > this.maxValues[i]) {
                        this.maxValues[i] = current;
                    }
                }

                if (System.currentTimeMillis() - this.startTime >= this.durationMs) {
                    this.listening = false;
                    float[] result = this.buildRangeArray();
                    this.onComplete.accept(result);
                }
            }
        }
    }

    private float[] buildRangeArray() {
        int axisCount = this.minValues.length;
        float[] range = new float[axisCount * 2];

        for (int i = 0; i < axisCount; i++) {
            range[i * 2] = this.minValues[i];
            range[i * 2 + 1] = this.maxValues[i];
        }

        return range;
    }
}
