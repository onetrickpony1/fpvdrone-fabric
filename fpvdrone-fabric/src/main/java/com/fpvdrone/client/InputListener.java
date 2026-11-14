package com.fpvdrone.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InputListener {
    public static final float DETECTION_THRESHOLD = 0.5F;
    private static final InputListener instance = new InputListener();
    private float[] baselineAxes;
    private boolean[] baselineButtons;
    private Map<Integer, float[]> baselineAxesAll;
    private Map<Integer, boolean[]> baselineButtonsAll;
    private Consumer<InputListener.DetectionResult> onDetected;
    private boolean listening = false;

    public static InputListener getInstance() {
        return instance;
    }

    public void startListening(Consumer<InputListener.DetectionResult> callback) {
        RangeListener.getInstance().stopListening();
        this.onDetected = callback;
        ControllerReader reader = ControllerReader.getInstance();
        if (reader.isPhysicalControllerConnected()) {
            this.baselineAxes = this.captureCurrentAxes();
            this.baselineButtons = this.captureCurrentButtons();
            this.baselineAxesAll = null;
            this.baselineButtonsAll = null;
        } else {
            this.baselineAxesAll = this.scanAllAxes();
            this.baselineButtonsAll = this.scanAllButtons();
            this.baselineAxes = null;
            this.baselineButtons = null;
        }

        this.listening = true;
    }

    public void stopListening() {
        this.listening = false;
        this.onDetected = null;
    }

    public boolean isListening() {
        return this.listening;
    }

    public void tick() {
        if (this.listening && this.onDetected != null) {
            ControllerReader reader = ControllerReader.getInstance();
            if (!reader.isPhysicalControllerConnected() && this.baselineAxesAll != null) {
                this.tickMultiScan(reader);
            } else {
                this.tickSingleDevice(reader);
            }
        }
    }

    private void tickMultiScan(ControllerReader reader) {
        for (int jid = 0; jid <= 15; jid++) {
            if (SdlJoysticks.isPresent(jid)) {
                float[] baseline = this.baselineAxesAll.get(jid);
                if (baseline != null) {
                    float[] current = ControllerReader.readAxes(jid);
                    int bestAxis = -1;
                    float bestDiff = 0.0F;

                    for (int i = 0; i < current.length && i < baseline.length; i++) {
                        float diff = current[i] - baseline[i];
                        if (Math.abs(diff) > 0.5F && Math.abs(diff) > Math.abs(bestDiff)) {
                            bestAxis = i;
                            bestDiff = diff;
                        }
                    }

                    if (bestAxis >= 0) {
                        this.listening = false;
                        reader.bindToJid(jid);
                        this.onDetected.accept(new InputListener.DetectionResult(bestAxis, false, bestDiff));
                        return;
                    }

                    boolean[] baselineBtns = this.baselineButtonsAll.get(jid);
                    if (baselineBtns != null) {
                        boolean[] currentBtns = ControllerReader.readButtons(jid);

                        for (int ix = 0; ix < currentBtns.length && ix < baselineBtns.length; ix++) {
                            if (currentBtns[ix] != baselineBtns[ix]) {
                                int buttonChannel = baseline.length + ix;
                                this.listening = false;
                                reader.bindToJid(jid);
                                this.onDetected.accept(new InputListener.DetectionResult(buttonChannel, true, 1.0F));
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void tickSingleDevice(ControllerReader reader) {
        if (reader.isConnected()) {
            if (this.baselineAxes != null && this.baselineButtons != null) {
                float[] current = this.captureCurrentAxes();
                int bestAxis = -1;
                float bestDiff = 0.0F;

                for (int i = 0; i < current.length && i < this.baselineAxes.length; i++) {
                    float diff = current[i] - this.baselineAxes[i];
                    if (Math.abs(diff) > 0.5F && Math.abs(diff) > Math.abs(bestDiff)) {
                        bestAxis = i;
                        bestDiff = diff;
                    }
                }

                if (bestAxis >= 0) {
                    this.listening = false;
                    this.onDetected.accept(new InputListener.DetectionResult(bestAxis, false, bestDiff));
                } else {
                    boolean[] buttons = this.captureCurrentButtons();

                    for (int ix = 0; ix < buttons.length && ix < this.baselineButtons.length; ix++) {
                        if (buttons[ix] != this.baselineButtons[ix]) {
                            int buttonChannel = this.baselineAxes.length + ix;
                            this.listening = false;
                            this.onDetected.accept(new InputListener.DetectionResult(buttonChannel, true, 1.0F));
                            return;
                        }
                    }
                }
            }
        }
    }

    public float getAxisDiff(int channel) {
        if (this.baselineAxes != null && channel >= 0 && channel < this.baselineAxes.length) {
            float[] current = this.captureCurrentAxes();
            return current[channel] - this.baselineAxes[channel];
        } else {
            return 0.0F;
        }
    }

    private float[] captureCurrentAxes() {
        ControllerReader reader = ControllerReader.getInstance();
        int count = reader.getAxisCount();
        float[] axes = new float[count];

        for (int i = 0; i < count; i++) {
            axes[i] = reader.getRawAxisValue(i);
        }

        return axes;
    }

    private boolean[] captureCurrentButtons() {
        ControllerReader reader = ControllerReader.getInstance();
        int count = reader.getButtonCount();
        boolean[] buttons = new boolean[count];

        for (int i = 0; i < count; i++) {
            buttons[i] = reader.getButton(i);
        }

        return buttons;
    }

    private Map<Integer, float[]> scanAllAxes() {
        Map<Integer, float[]> result = new HashMap<>();

        for (int jid = 0; jid <= 15; jid++) {
            if (SdlJoysticks.isPresent(jid)) {
                result.put(jid, ControllerReader.readAxes(jid));
            }
        }

        return result;
    }

    private Map<Integer, boolean[]> scanAllButtons() {
        Map<Integer, boolean[]> result = new HashMap<>();

        for (int jid = 0; jid <= 15; jid++) {
            if (SdlJoysticks.isPresent(jid)) {
                result.put(jid, ControllerReader.readButtons(jid));
            }
        }

        return result;
    }

    public static class DetectionResult {
        public final int channel;
        public final boolean isButton;
        public final float diff;

        public DetectionResult(int channel, boolean isButton, float diff) {
            this.channel = channel;
            this.isButton = isButton;
            this.diff = diff;
        }

        public boolean isNegative() {
            return this.diff < 0.0F;
        }
    }
}
