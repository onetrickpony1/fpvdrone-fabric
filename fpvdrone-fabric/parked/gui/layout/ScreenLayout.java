package com.fpvdrone.gui.layout;

public class ScreenLayout {
    public final int screenW;
    public final int screenH;
    public final float guiScale;
    public final int fontLineHeight;
    public static final int BOX_BORDER = 2;
    public static final int BOX_TITLE_H = 14;
    public static final int GRID_COLS = 30;
    public static final int GRID_ROWS = 16;
    public static final int CHAR_WIDTH = 12;
    public static final int CHAR_HEIGHT = 18;
    private static final int MIN_SIDEBAR_W = 50;
    private static final int MAX_SIDEBAR_W = 80;
    private static final int MIN_HEADER_H = 28;
    private static final int MAX_HEADER_H = 44;
    private static final int MIN_TOOLBAR_H = 20;
    private static final int MAX_TOOLBAR_H = 30;
    private static final int MIN_LOGBAR_H = 12;
    private static final int MAX_LOGBAR_H = 18;
    private static final int MIN_ELEM_PANEL_W = 70;
    private static final int MIN_SETT_PANEL_W = 70;
    private static final int MIN_PREVIEW_W = 60;
    private static final int MIN_PREVIEW_H = 40;
    private static final int MIN_CONTENT_PAD = 3;
    private static final int MAX_CONTENT_PAD = 8;
    public boolean tooSmall;
    public final int minRequiredW;
    public final int minRequiredH;
    public final int headerH;
    public final int sidebarW;
    public final int toolbarH;
    public final int logBarH;
    public final int contentPad;
    public final int boxPad;
    public final int contentX;
    public final int contentY;
    public final int contentW;
    public final int contentH;
    public final int elemPanelX;
    public final int elemPanelW;
    public final int elemBoxTop;
    public int elemBoxH;
    public final int previewX;
    public final int previewY;
    public final int previewW;
    public int previewH;
    public final int settPanelX;
    public int settPanelW;
    public final boolean settingsVisible;
    public final int toolbarX;
    public final int toolbarY;
    public final int toolbarW;
    public final int toolbarH_actual;
    public final int cellW;
    public final int cellH;
    public final int gridOriginX;
    public final int gridOriginY;
    public final int gridW;
    public final int gridH;
    public final int listX;
    public final int listY;
    public final int listW;
    public final int listH;
    public int settSectionH;
    public int settGap;
    public static final int SETTINGS_SECTION_COUNT = 7;

    public ScreenLayout(int screenW, int screenH, float guiScale, int fontLineHeight) {
        this.screenW = screenW;
        this.screenH = screenH;
        this.guiScale = guiScale;
        this.fontLineHeight = fontLineHeight;
        this.minRequiredW = 192;
        this.minRequiredH = 120;
        this.tooSmall = screenW < this.minRequiredW || screenH < this.minRequiredH;
        this.headerH = clamp((int)(screenH * 0.08F), 28, 44);
        this.sidebarW = clamp((int)(screenW * 0.1F), 50, 80);
        this.toolbarH = clamp((int)(screenH * 0.05F), 20, 30);
        this.logBarH = clamp((int)(screenH * 0.03F), 12, 18);
        this.contentPad = clamp((int)(screenW * 0.005F), 3, 8);
        this.boxPad = this.contentPad;
        this.contentX = this.sidebarW;
        this.contentY = this.headerH;
        this.contentW = Math.max(1, screenW - this.sidebarW);
        this.contentH = Math.max(1, screenH - this.headerH - this.toolbarH - this.logBarH);
        int innerX = this.contentX + this.contentPad;
        int innerW = Math.max(1, this.contentW - this.contentPad * 2);
        int underlineH = 2;
        int osdBlockH = fontLineHeight + 1 + underlineH;
        int osdTitleY = this.contentY + 2;
        this.elemBoxTop = osdTitleY + osdBlockH + 4;
        this.elemBoxH = Math.max(1, this.contentY + this.contentH - this.contentPad - this.elemBoxTop);
        int previewBoxY = this.elemBoxTop;
        int titleHPx = 14;
        int borderPx = 2;
        int tmpPreviewY = previewBoxY + titleHPx / 2 + borderPx;
        int previewBoxBottom = this.contentY + this.contentH - this.contentPad;
        int tmpPreviewH = Math.max(1, previewBoxBottom - titleHPx / 2 - borderPx - tmpPreviewY);
        int gaps = this.contentPad * 2;
        int availForColumns = innerW - gaps;
        int idealPreviewW = availForColumns / 2;
        int idealElemW = availForColumns / 4;
        int idealSettW = availForColumns / 4;
        float idealCellW = idealPreviewW / 30.0F;
        float idealCellH = idealCellW * 1.5F;
        if (idealCellH * 16.0F > tmpPreviewH) {
            float idealCellHByH = tmpPreviewH / 16.0F;
            idealCellW = idealCellHByH * 0.6666667F;
        }

        int intCellW = Math.max(1, Math.round(idealCellW));
        int intCellH = Math.max(1, Math.round(intCellW * 1.5F));
        if (intCellH * 16 > tmpPreviewH) {
            intCellH = Math.max(1, (int)(intCellW * 1.5F));
        }

        if (intCellH * 16 > tmpPreviewH) {
            intCellH = Math.max(1, tmpPreviewH / 16);
            intCellW = Math.max(1, Math.round(intCellH * 0.6666667F));
        }

        if (intCellW * 30 > idealPreviewW && intCellW > 1) {
            intCellW = Math.max(1, (int)idealCellW);
            intCellH = Math.max(1, Math.round(intCellW * 1.5F));
            if (intCellH * 16 > tmpPreviewH) {
                intCellH = Math.max(1, (int)(intCellW * 1.5F));
            }

            if (intCellH * 16 > tmpPreviewH) {
                intCellH = Math.max(1, tmpPreviewH / 16);
                intCellW = Math.max(1, Math.round(intCellH * 0.6666667F));
            }
        }

        this.cellW = intCellW;
        this.cellH = intCellH;
        this.gridW = intCellW * 30;
        this.gridH = intCellH * 16;
        int snappedPreviewW = this.gridW;
        this.elemPanelX = innerX;
        int neededForThreeCol = 70 + this.contentPad + snappedPreviewW + this.contentPad + 70;
        int tmpElemPanelW;
        int tmpSettPanelW;
        boolean tmpSettingsVisible;
        if (neededForThreeCol <= innerW && snappedPreviewW >= 60) {
            int excess = innerW - (snappedPreviewW + this.contentPad * 2);
            int halfExcess = excess / 2;
            tmpElemPanelW = Math.max(halfExcess, 70);
            tmpSettPanelW = Math.max(excess - halfExcess, 70);
            tmpSettingsVisible = true;
        } else {
            int neededForTwoCol = 70 + this.contentPad + snappedPreviewW;
            if (neededForTwoCol <= innerW && snappedPreviewW >= 60) {
                tmpElemPanelW = Math.max(innerW - this.contentPad - snappedPreviewW, 70);
                tmpSettPanelW = 0;
                tmpSettingsVisible = false;
            } else {
                tmpElemPanelW = 70;
                tmpSettPanelW = 0;
                tmpSettingsVisible = false;
            }
        }

        this.previewY = tmpPreviewY;
        this.previewH = tmpPreviewH;
        if (this.gridW > snappedPreviewW || this.gridH > tmpPreviewH) {
            this.tooSmall = true;
        }

        if (this.contentW < 133) {
            this.tooSmall = true;
        }

        if (this.contentH < 70) {
            this.tooSmall = true;
        }

        int gridPadBottom = fontLineHeight + 8;
        this.previewH = this.gridH + gridPadBottom;
        this.previewH = Math.min(this.previewH, tmpPreviewH);
        this.elemBoxH = titleHPx / 2 + borderPx * 2 + this.previewH;
        int minSectionH = 14 + fontLineHeight + 4;
        int minGap = 1;
        if (tmpSettingsVisible && this.elemBoxH < minSectionH) {
            tmpSettingsVisible = false;
            tmpSettPanelW = 0;
            int twoColAvail = innerW - this.contentPad;
            tmpElemPanelW = Math.max(70, twoColAvail - snappedPreviewW);
        }

        this.elemPanelW = tmpElemPanelW;
        this.settPanelW = tmpSettPanelW;
        this.settingsVisible = tmpSettingsVisible;
        this.previewW = snappedPreviewW;
        this.previewX = this.elemPanelX + this.elemPanelW + this.contentPad;
        this.settPanelX = this.settingsVisible ? this.previewX + this.previewW + this.contentPad : screenW + 10;
        if (this.settingsVisible && this.settPanelX + this.settPanelW > screenW) {
            this.settPanelW = Math.max(70, screenW - this.settPanelX);
        }

        this.gridOriginX = this.previewX + (this.previewW - this.gridW) / 2;
        this.gridOriginY = this.previewY;
        this.listX = this.elemPanelX + borderPx;
        this.listY = this.elemBoxTop + titleHPx;
        this.listW = Math.max(1, this.elemPanelW - borderPx * 2);
        this.listH = Math.max(1, this.elemBoxH - titleHPx - borderPx);
        this.toolbarW = Math.max(1, this.contentW - this.contentPad);
        this.toolbarX = this.contentX + this.contentPad;
        this.toolbarY = Math.max(0, screenH - this.toolbarH - this.logBarH);
        this.toolbarH_actual = this.toolbarH;
        int maxSectionH = 14 + fontLineHeight + 10;
        int maxGap = 6;
        this.settGap = maxGap;
        int settTotalGaps = 6 * this.settGap;
        int idealSectionH = (this.elemBoxH - settTotalGaps) / 7;
        if (idealSectionH < minSectionH) {
            int neededForSections = minSectionH * 7;
            int availForGaps = this.elemBoxH - neededForSections;
            this.settGap = Math.max(minGap, availForGaps / 6);
            settTotalGaps = 6 * this.settGap;
            idealSectionH = (this.elemBoxH - settTotalGaps) / 7;
        }

        this.settSectionH = Math.min(idealSectionH, maxSectionH);
        this.settSectionH = Math.max(this.settSectionH, 16);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public int settingsFieldY(int sectionIndex) {
        int settBoxY = this.elemBoxTop;
        int boxY = settBoxY + sectionIndex * (this.settSectionH + this.settGap);
        int titleHPx = 14;
        int borderPx = 2;
        int contentTopOffset = titleHPx / 2 + borderPx;
        int contentAreaH = this.settSectionH - contentTopOffset - borderPx;
        int fieldH = this.fontLineHeight + 2;
        int fieldInnerY = contentTopOffset + Math.max(0, (contentAreaH - fieldH) / 2);
        int maxFieldY = this.settSectionH - borderPx - fieldH;
        fieldInnerY = Math.min(fieldInnerY, Math.max(contentTopOffset, maxFieldY));
        return boxY + fieldInnerY;
    }

    public int settingsFieldX() {
        return this.settPanelX + this.boxPad + 2;
    }

    public int settingsFieldW() {
        return Math.max(10, this.settPanelW - this.boxPad * 2 - 4);
    }

    public float mouseToGridX(double mouseX) {
        return (float)((mouseX - this.gridOriginX) / this.cellW);
    }

    public float mouseToGridY(double mouseY) {
        return (float)((mouseY - this.gridOriginY) / this.cellH);
    }

    public float gridToScreenX(float gridX) {
        return this.gridOriginX + gridX * this.cellW;
    }

    public float gridToScreenY(float gridY) {
        return this.gridOriginY + gridY * this.cellH;
    }

    public boolean isInsideGrid(double mouseX, double mouseY) {
        return mouseX >= this.gridOriginX && mouseX < this.gridOriginX + this.gridW && mouseY >= this.gridOriginY && mouseY < this.gridOriginY + this.gridH;
    }

    public String validate() {
        if (this.tooSmall) {
            return null;
        } else if (this.headerH <= 0) {
            return "headerH <= 0: " + this.headerH;
        } else if (this.sidebarW <= 0) {
            return "sidebarW <= 0: " + this.sidebarW;
        } else if (this.toolbarH <= 0) {
            return "toolbarH <= 0: " + this.toolbarH;
        } else if (this.logBarH <= 0) {
            return "logBarH <= 0: " + this.logBarH;
        } else if (this.contentW <= 0) {
            return "contentW <= 0: " + this.contentW;
        } else if (this.contentH <= 0) {
            return "contentH <= 0: " + this.contentH;
        } else if (this.previewW <= 0) {
            return "previewW <= 0: " + this.previewW;
        } else if (this.previewH <= 0) {
            return "previewH <= 0: " + this.previewH;
        } else if (this.elemPanelW <= 0) {
            return "elemPanelW <= 0: " + this.elemPanelW;
        } else if (this.elemBoxH <= 0) {
            return "elemBoxH <= 0: " + this.elemBoxH;
        } else if (this.listW <= 0) {
            return "listW <= 0: " + this.listW;
        } else if (this.listH <= 0) {
            return "listH <= 0: " + this.listH;
        } else if (this.toolbarW <= 0) {
            return "toolbarW <= 0: " + this.toolbarW;
        } else if (this.cellW <= 0) {
            return "cellW <= 0: " + this.cellW;
        } else if (this.cellH <= 0) {
            return "cellH <= 0: " + this.cellH;
        } else if (this.gridW > this.previewW + 1) {
            return "gridW exceeds previewW: " + this.gridW + " > " + this.previewW;
        } else if (this.gridH > this.previewH + 1) {
            return "gridH exceeds previewH: " + this.gridH + " > " + this.previewH;
        } else if (this.sidebarW > this.screenW) {
            return "sidebarW exceeds screenW";
        } else {
            return this.settingsVisible && this.settPanelX + this.settPanelW > this.screenW + 1
                ? "settings panel exceeds screenW: " + (this.settPanelX + this.settPanelW) + " > " + this.screenW
                : null;
        }
    }
}
