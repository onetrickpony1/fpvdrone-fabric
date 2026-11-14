package com.fpvdrone.client.osd;

public final class OsdGridHelper {
    private OsdGridHelper() {
    }

    public static int gridToScreenX(float gridX, int screenWidth) {
        return (int)(gridX / 30.0F * screenWidth);
    }

    public static int gridToScreenY(float gridY, int screenHeight) {
        return (int)(gridY / 16.0F * screenHeight);
    }

    public static float clampGridX(float gridX, int elementWidthChars) {
        float max = 30 - elementWidthChars;
        return Math.max(0.0F, Math.min(max, gridX));
    }

    public static float clampGridY(float gridY, int elementHeightLines) {
        float max = 16 - elementHeightLines;
        return Math.max(0.0F, Math.min(max, gridY));
    }
}
