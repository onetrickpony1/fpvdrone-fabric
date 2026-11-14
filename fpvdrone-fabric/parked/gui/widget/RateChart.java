package com.fpvdrone.gui.widget;

import net.minecraft.client.gui.GuiGraphics;

public class RateChart {
    public static void render(GuiGraphics graphics, int x, int y, int width, int height, float rcRate, float superRate, float expo, float currentInput) {
        graphics.fill(x, y, x + width, y + height, -15066598);
        graphics.fill(x, y, x + width, y + 1, -12303292);
        graphics.fill(x, y + height - 1, x + width, y + height, -12303292);
        graphics.fill(x, y, x + 1, y + height, -12303292);
        graphics.fill(x + width - 1, y, x + width, y + height, -12303292);

        for (int i = 1; i < 4; i++) {
            int lineY = y + height * i / 4;
            graphics.hLine(x + 1, x + width - 2, lineY, 1090519039);
        }

        for (int i = 1; i < 4; i++) {
            int lineX = x + width * i / 4;
            graphics.vLine(lineX, y + 1, y + height - 2, 1090519039);
        }

        int prevY = y + height - 1;

        for (int px = 0; px <= width; px++) {
            float input = (float)px / width;
            float output = calculateRate(input, rcRate, superRate, expo);
            output = Math.min(output, 1.0F);
            int curY = y + height - 1 - (int)(output * (height - 2));
            if (px > 0) {
                int minY = Math.min(prevY, curY);
                int maxY = Math.max(prevY, curY);

                for (int ly = minY; ly <= maxY; ly++) {
                    graphics.fill(x + px, ly, x + px + 1, ly + 1, -16711936);
                }
            }

            prevY = curY;
        }

        float absInput = Math.abs(currentInput);
        int markerX = x + (int)(absInput * width);
        float markerOutput = calculateRate(absInput, rcRate, superRate, expo);
        markerOutput = Math.min(markerOutput, 1.0F);
        int markerY = y + height - 1 - (int)(markerOutput * (height - 2));
        graphics.fill(markerX - 2, markerY - 2, markerX + 3, markerY + 3, -17664);
    }

    public static float calculateRate(float input, float rcRate, float superRate, float expo) {
        float expoPower = (float)Math.pow(input, 3.0) * expo + input * (1.0F - expo);
        float superFactor = 1.0F / (1.0F - Math.abs(expoPower) * superRate);
        return expoPower * rcRate * superFactor;
    }
}
