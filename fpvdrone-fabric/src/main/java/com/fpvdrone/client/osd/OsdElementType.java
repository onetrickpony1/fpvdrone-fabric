package com.fpvdrone.client.osd;

import java.util.function.Function;

public class OsdElementType {
    private final String name;
    private final String displayName;
    private final String category;
    private String previewText;
    private final float defaultX;
    private final float defaultY;
    private final int widthChars;
    private final int heightLines;
    private final boolean hasLiveData;
    private final boolean hasCustomRenderer;
    private Function<OsdRenderContext, String> textRenderer;
    private OsdCustomRenderer customRenderer;

    public OsdElementType(
        String name,
        String displayName,
        String category,
        String previewText,
        float defaultX,
        float defaultY,
        int widthChars,
        int heightLines,
        boolean hasLiveData,
        boolean hasCustomRenderer
    ) {
        this.name = name;
        this.displayName = displayName;
        this.category = category;
        this.previewText = previewText;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.widthChars = widthChars;
        this.heightLines = heightLines;
        this.hasLiveData = hasLiveData;
        this.hasCustomRenderer = hasCustomRenderer;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getCategory() {
        return this.category;
    }

    public String getPreviewText() {
        return this.previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }

    public float getDefaultX() {
        return this.defaultX;
    }

    public float getDefaultY() {
        return this.defaultY;
    }

    public int getWidthChars() {
        return this.widthChars;
    }

    public int getHeightLines() {
        return this.heightLines;
    }

    public boolean hasLiveData() {
        return this.hasLiveData;
    }

    public boolean hasCustomRenderer() {
        return this.hasCustomRenderer || this.customRenderer != null;
    }

    public void setTextRenderer(Function<OsdRenderContext, String> renderer) {
        this.textRenderer = renderer;
    }

    public void setCustomRenderer(OsdCustomRenderer renderer) {
        this.customRenderer = renderer;
    }

    public String renderText(OsdRenderContext ctx) {
        return this.textRenderer != null ? this.textRenderer.apply(ctx) : this.previewText;
    }

    public boolean renderCustom(OsdRenderContext ctx, int screenX, int screenY) {
        if (this.customRenderer != null) {
            this.customRenderer.render(ctx, screenX, screenY);
            return true;
        } else {
            return false;
        }
    }
}
