package com.fpvdrone.video;

public enum VideoResolution {
    RES_10(0.1F, "10%"),
    RES_15(0.15F, "15%"),
    RES_20(0.2F, "20%"),
    RES_25(0.25F, "25%"),
    RES_33(0.33F, "33%"),
    RES_50(0.5F, "50%");

    private final float scale;
    private final String displayName;

    private VideoResolution(float scale, String displayName) {
        this.scale = scale;
        this.displayName = displayName;
    }

    public float getScale() {
        return this.scale;
    }

    public int getWidth(int screenWidth, int screenHeight) {
        int baseHeight = (int)(screenHeight * this.scale);
        float aspectRatio = (float)screenWidth / screenHeight;
        return Math.max(16, (int)(baseHeight * aspectRatio));
    }

    public int getHeight(int screenHeight) {
        return Math.max(16, (int)(screenHeight * this.scale));
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public VideoResolution next() {
        VideoResolution[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
