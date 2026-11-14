package com.fpvdrone.gui.screen;

import com.fpvdrone.client.osd.McmRenderer;
import com.fpvdrone.client.osd.OsdElement;
import com.fpvdrone.client.osd.OsdElementRegistry;
import com.fpvdrone.client.osd.OsdElementType;
import com.fpvdrone.client.osd.OsdGridHelper;
import com.fpvdrone.client.osd.OsdLayout;
import com.fpvdrone.client.osd.OsdLayoutIO;
import com.fpvdrone.client.osd.OsdLogger;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.gui.layout.ScreenLayout;
import com.fpvdrone.gui.list.OsdElementList;
import com.fpvdrone.gui.widget.GuiBoxRenderer;
import com.fpvdrone.gui.widget.SidebarRenderer;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.OsdLayoutPacket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OsdBuilderScreen extends FpvScreen {
    private final DroneEntity drone;
    private OsdLayout editingLayout;
    public static final int COLOR_BG = -14803426;
    public static final int COLOR_SURFACE_100 = -15461356;
    public static final int COLOR_SURFACE_200 = -14737633;
    public static final int COLOR_SURFACE_300 = -14408668;
    public static final int COLOR_SURFACE_400 = -13421773;
    public static final int COLOR_PRIMARY = -17664;
    public static final int COLOR_PRIMARY_HOVER = -14217;
    public static final int COLOR_PRIMARY_T2 = 872397568;
    public static final int COLOR_TEXT = -855310;
    public static final int COLOR_TEXT_DIM = -4473925;
    public static final int COLOR_BORDER = -13421773;
    public static final int COLOR_HOVER_OUTLINE = -2130724096;
    public static final int COLOR_GRID_LINE = -2143865033;
    public static final int OUTLINE_COLOR = -16777216;
    public static final int TEXT_COLOR = -1;
    private static final int COLOR_SHADOW_1 = 1073741824;
    private static final int COLOR_SHADOW_2 = 671088640;
    private static final int COLOR_SHADOW_3 = 402653184;
    private static final int COLOR_SHADOW_4 = 201326592;
    private ScreenLayout layout;
    private OsdElementList elementList;
    private String draggedElement;
    private float dragOffsetGridX;
    private float dragOffsetGridY;
    private boolean isDragging;
    private String hoveredElement;
    private EditBox craftNameField;
    private EditBox pilotNameField;
    private String statusMessage;
    private long statusMessageExpiry;
    private boolean debugOverlay = false;
    private List<OsdBuilderScreen.ToolbarButton> toolbarButtons = List.of();
    private static final String[] SETTINGS_SECTIONS = new String[]{"Video Format", "Units", "Timers", "Alarms", "Warnings", "Craft Name", "Pilot Name"};
    private static final String APP_VERSION = "App Version: 2026.3.1 (d8vc060)";

    public OsdBuilderScreen(Screen previous, DroneEntity drone, OsdLayout layout) {
        super(previous, null, null);
        this.drone = drone;
        this.editingLayout = layout != null ? layout : OsdLayout.defaults();
    }

    @Override
    protected void init() {
        super.init();
        OsdLogger.builderInfo("Screen opened for drone {}", this.drone.getId());
        float gs = (float)this.minecraft.getWindow().getGuiScale();
        this.layout = new ScreenLayout(this.width, this.height, gs, 9);
        if (!this.layout.tooSmall) {
            this.elementList = new OsdElementList(
                this, this.layout.listX, this.layout.listY, this.layout.listW, this.layout.listH, this.editingLayout, () -> {}
            );
            this.addWidget(this.elementList);
            if (this.layout.settingsVisible) {
                int fieldX = this.layout.settingsFieldX();
                int fieldW = this.layout.settingsFieldW();
                int fieldH = 9 + 2;
                int craftFieldY = this.layout.settingsFieldY(5);
                this.craftNameField = new EditBox(this.minecraft.font, fieldX, craftFieldY, fieldW, fieldH, Component.literal("Craft Name"));
                this.craftNameField.setMaxLength(16);
                this.craftNameField.setBordered(false);
                this.craftNameField.setTextColor(-855310);
                String initialCraftName = this.drone.getConfig() != null ? this.drone.getConfig().getDroneName() : "CRAFT";
                this.craftNameField.setValue(initialCraftName);
                this.craftNameField.setResponder(text -> {
                    OsdElementType craftType = OsdElementRegistry.get("CRAFT_NAME");
                    if (craftType != null) {
                        craftType.setPreviewText(text.isEmpty() ? "CRAFT" : text.toUpperCase());
                    }
                });
                int pilotFieldY = this.layout.settingsFieldY(6);
                this.pilotNameField = new EditBox(this.minecraft.font, fieldX, pilotFieldY, fieldW, fieldH, Component.literal("Pilot Name"));
                this.pilotNameField.setMaxLength(16);
                this.pilotNameField.setBordered(false);
                this.pilotNameField.setTextColor(-855310);
                OsdElement pilotElem = this.editingLayout.getElement("PILOT_NAME");
                String initialPilotName;
                if (pilotElem != null && pilotElem.getCustomText() != null && !pilotElem.getCustomText().isEmpty()) {
                    initialPilotName = pilotElem.getCustomText();
                } else if (this.minecraft.player != null) {
                    initialPilotName = this.minecraft.player.getGameProfile().getName().toUpperCase();
                } else {
                    initialPilotName = "PILOT";
                }

                this.pilotNameField.setValue(initialPilotName);
                this.pilotNameField.setResponder(text -> {
                    OsdElementType pilotType = OsdElementRegistry.get("PILOT_NAME");
                    if (pilotType != null) {
                        pilotType.setPreviewText(text.isEmpty() ? "PILOT" : text.toUpperCase());
                    }
                });
            } else {
                this.craftNameField = null;
                this.pilotNameField = null;
            }

            this.rebuildToolbarButtons();
        }
    }

    private void rebuildToolbarButtons() {
        Font font = this.minecraft.font;
        int btnH = 18;
        int btnPad = 6;
        int btnGap = 4;
        int btnY = this.layout.toolbarY + (this.layout.toolbarH_actual - btnH) / 2;
        List<OsdBuilderScreen.ToolbarButton> buttons = new ArrayList<>();
        String saveLabel = "Save";
        int saveBtnW = font.width(saveLabel) + btnPad * 2 + 4;
        int saveBtnX = this.layout.toolbarX + this.layout.toolbarW - saveBtnW - 8;

        record LabelAction(String label, Runnable action) {
        }

        LabelAction[] leftButtons = new LabelAction[]{
            new LabelAction("Font Mgr", this::showFontManagerMessage),
            new LabelAction("Import", this::openImportScreen),
            new LabelAction("Export", this::exportLayout),
            new LabelAction("Reset", this::resetToDefaults)
        };
        int btnX = this.layout.toolbarX + 8;

        for (LabelAction la : leftButtons) {
            int btnW = font.width(la.label()) + btnPad * 2;
            if (btnX + btnW + btnGap > saveBtnX) {
                break;
            }

            buttons.add(new OsdBuilderScreen.ToolbarButton(la.label(), btnX, btnY, btnW, btnH, la.action()));
            btnX += btnW + btnGap;
        }

        buttons.add(new OsdBuilderScreen.ToolbarButton(saveLabel, saveBtnX, btnY, saveBtnW, btnH, this::saveLayout));
        this.toolbarButtons = List.copyOf(buttons);
    }

    private void showFontManagerMessage() {
        this.statusMessage = "Font Manager coming soon";
        this.statusMessageExpiry = System.currentTimeMillis() + 5000L;
    }

    @Override
    public void renderCustom(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int w = this.width;
        int h = this.height;
        graphics.fill(0, 0, w, h, -14803426);
        if (this.layout.tooSmall) {
            this.renderTooSmallMessage(graphics, w, h);
        } else {
            this.renderHeaderBar(graphics);
            this.renderSidebar(graphics, mouseY);
            this.renderContentArea(graphics, mouseX, mouseY, partialTicks);
            this.renderToolbar(graphics, mouseX, mouseY);
            this.renderLogBar(graphics);
            int clipX = this.layout.elemPanelX + 2;
            int clipY = this.layout.elemBoxTop + 14;
            int clipR = this.layout.elemPanelX + this.layout.elemPanelW - 2;
            int clipB = this.layout.elemBoxTop + this.layout.elemBoxH - 2;
            graphics.enableScissor(clipX, clipY, clipR, clipB);
            this.elementList.render(graphics, mouseX, mouseY, partialTicks);
            graphics.disableScissor();
            if (this.craftNameField != null) {
                this.renderEditBoxInSection(graphics, this.craftNameField, 5, mouseX, mouseY, partialTicks);
            }

            if (this.pilotNameField != null) {
                this.renderEditBoxInSection(graphics, this.pilotNameField, 6, mouseX, mouseY, partialTicks);
            }

            if (this.debugOverlay) {
                this.renderDebugOverlay(graphics);
            }
        }
    }

    private void renderTooSmallMessage(GuiGraphics graphics, int w, int h) {
        Font font = this.minecraft.font;
        int panelW = Math.min(w - 20, 220);
        int panelH = 60;
        int px = (w - panelW) / 2;
        int py = (h - panelH) / 2;
        graphics.fill(px, py, px + panelW, py + panelH, -14737633);
        graphics.fill(px, py, px + panelW, py + 2, -17664);
        String line1 = "Window too small";
        String line2 = "Min: " + this.layout.minRequiredW + "x" + this.layout.minRequiredH;
        String line3 = "Current: " + w + "x" + h;
        int y = py + 8;
        graphics.drawString(font, line1, px + (panelW - font.width(line1)) / 2, y, -17664, false);
        y += 9 + 4;
        graphics.drawString(font, line2, px + (panelW - font.width(line2)) / 2, y, -4473925, false);
        y += 9 + 2;
        graphics.drawString(font, line3, px + (panelW - font.width(line3)) / 2, y, -4473925, false);
    }

    private void renderDebugOverlay(GuiGraphics graphics) {
        Font font = this.minecraft.font;
        int alpha = 64;
        this.drawDebugRect(graphics, 0, 0, this.width, this.layout.headerH, 1090453504);
        this.drawDebugLabel(graphics, font, "header " + this.width + "x" + this.layout.headerH, 2, 2);
        this.drawDebugRect(graphics, 0, this.layout.headerH, this.layout.sidebarW, this.height - this.layout.headerH - this.layout.logBarH, 1073807104);
        this.drawDebugLabel(graphics, font, "sidebar " + this.layout.sidebarW, 2, this.layout.headerH + 2);
        this.drawDebugOutline(graphics, this.layout.contentX, this.layout.contentY, this.layout.contentW, this.layout.contentH, -12303105);
        this.drawDebugLabel(
            graphics, font, "content " + this.layout.contentW + "x" + this.layout.contentH, this.layout.contentX + 2, this.layout.contentY + 9 + 6
        );
        this.drawDebugOutline(graphics, this.layout.elemPanelX, this.layout.elemBoxTop, this.layout.elemPanelW, this.layout.elemBoxH, -16711681);
        this.drawDebugLabel(
            graphics, font, "elem " + this.layout.elemPanelW + "x" + this.layout.elemBoxH, this.layout.elemPanelX + 2, this.layout.elemBoxTop + 14 + 2
        );
        this.drawDebugOutline(graphics, this.layout.previewX, this.layout.elemBoxTop, this.layout.previewW, this.layout.elemBoxH, -65281);
        this.drawDebugLabel(
            graphics, font, "preview " + this.layout.previewW + "x" + this.layout.previewH, this.layout.previewX + 2, this.layout.elemBoxTop + 14 + 2
        );
        this.drawDebugOutline(graphics, this.layout.gridOriginX, this.layout.gridOriginY, this.layout.gridW, this.layout.gridH, -256);
        this.drawDebugLabel(
            graphics,
            font,
            "grid " + this.layout.gridW + "x" + this.layout.gridH + " cell=" + this.layout.cellW + "x" + this.layout.cellH,
            this.layout.gridOriginX + 2,
            this.layout.gridOriginY + 2
        );
        if (this.layout.settingsVisible) {
            this.drawDebugOutline(graphics, this.layout.settPanelX, this.layout.elemBoxTop, this.layout.settPanelW, this.layout.elemBoxH, -30720);
            this.drawDebugLabel(
                graphics,
                font,
                "sett " + this.layout.settPanelW + " secH=" + this.layout.settSectionH,
                this.layout.settPanelX + 2,
                this.layout.elemBoxTop + 14 + 2
            );
            int y = this.layout.elemBoxTop;
            int panelBottom = this.layout.elemBoxTop + this.layout.elemBoxH;

            for (int i = 0; i < 7 && y + this.layout.settSectionH <= panelBottom; i++) {
                this.drawDebugOutline(graphics, this.layout.settPanelX + 1, y + 1, this.layout.settPanelW - 2, this.layout.settSectionH - 2, -2130737152);
                y += this.layout.settSectionH + this.layout.settGap;
            }
        }

        this.drawDebugOutline(graphics, this.layout.toolbarX, this.layout.toolbarY, this.layout.toolbarW, this.layout.toolbarH_actual, -1);
        this.drawDebugRect(graphics, 0, this.height - this.layout.logBarH, this.width, this.layout.logBarH, 1082689672);
        String info = "F3=debug | " + this.width + "x" + this.height + " gs=" + String.format("%.1f", this.layout.guiScale);
        int infoW = font.width(info);
        graphics.fill(this.width - infoW - 6, 0, this.width, 9 + 4, -1073741824);
        graphics.drawString(font, info, this.width - infoW - 3, 2, -1, false);
    }

    private void drawDebugRect(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, color);
    }

    private void drawDebugOutline(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void drawDebugLabel(GuiGraphics graphics, Font font, String text, int x, int y) {
        int tw = font.width(text);
        graphics.fill(x, y, x + tw + 2, y + 9 + 1, -1073741824);
        graphics.drawString(font, text, x + 1, y + 1, -1, false);
    }

    private void renderHeaderBar(GuiGraphics graphics) {
        int w = this.width;
        graphics.fill(0, 0, w, this.layout.headerH, -14737633);
        graphics.fill(0, this.layout.headerH - 1, w, this.layout.headerH, -13421773);
    }

    private void renderLogBar(GuiGraphics graphics) {
        int h = this.height;
        int w = this.width;
        int logY = h - this.layout.logBarH;
        graphics.fill(0, logY, w, h, -15461356);
        graphics.fill(0, logY, w, logY + 1, -13421773);
        Font font = this.minecraft.font;
        int textY = logY + (this.layout.logBarH - 9) / 2 + 1;
        String logLeft;
        int logLeftColor;
        if (this.statusMessage != null && System.currentTimeMillis() < this.statusMessageExpiry) {
            logLeft = this.statusMessage;
            logLeftColor = -17664;
        } else {
            this.statusMessage = null;
            logLeft = "2026-03-01 @00:00:00 — App Version: 2026.3.1 (d8vc060)";
            logLeftColor = -4473925;
        }

        graphics.drawString(font, logLeft, this.layout.contentX + 8, textY, logLeftColor, true);
        String showLog = "Show Log";
        int showLogW = font.width(showLog);
        graphics.drawString(font, showLog, w - showLogW - 12, textY, -4473925, true);
    }

    private void renderSidebar(GuiGraphics graphics, float mouseY) {
        SidebarRenderer.render(
            graphics,
            this.minecraft.font,
            this.layout.headerH,
            this.layout.sidebarW,
            this.height,
            this.layout.logBarH,
            -15461356,
            -14737633,
            -13421773,
            -17664,
            872397568,
            -855310,
            -4473925
        );
    }

    private void renderContentArea(GuiGraphics graphics, float mouseX, float mouseY, float partialTicks) {
        graphics.fill(
            this.layout.contentX, this.layout.contentY, this.layout.contentX + this.layout.contentW, this.layout.contentY + this.layout.contentH, -15461356
        );
        Font font = this.minecraft.font;
        int titleY = this.layout.contentY + 2;
        graphics.drawString(font, "OSD", this.layout.contentX + this.layout.contentPad, titleY, -855310, true);
        int osdTextW = font.width("OSD");
        int underlineY = titleY + 9 + 1;
        graphics.fill(
            this.layout.contentX + this.layout.contentPad, underlineY, this.layout.contentX + this.layout.contentPad + osdTextW + 4, underlineY + 2, -17664
        );
        this.renderGuiBox(graphics, this.layout.elemPanelX, this.layout.elemBoxTop, this.layout.elemPanelW, this.layout.elemBoxH, "Elements");
        int previewBoxX = this.layout.previewX;
        int previewBoxY = this.layout.elemBoxTop;
        int previewBoxW = this.layout.previewW;
        int previewBoxH = this.layout.elemBoxH;
        this.renderGuiBoxFrame(graphics, previewBoxX, previewBoxY, previewBoxW, previewBoxH);
        this.renderPreviewContent(graphics, mouseX, mouseY);
        this.renderGuiBoxTitle(graphics, previewBoxX, previewBoxY, previewBoxW, "Preview");
        if (this.layout.settingsVisible) {
            this.renderSettingsPanel(graphics);
        }
    }

    private void renderGuiBox(GuiGraphics graphics, int x, int y, int w, int h, String title) {
        GuiBoxRenderer.renderFrame(graphics, x, y, w, h, -14737633, -13421773);
        GuiBoxRenderer.renderTitle(graphics, this.minecraft.font, x, y, w, title, this.width, -17664);
    }

    private void renderGuiBoxFrame(GuiGraphics graphics, int x, int y, int w, int h) {
        GuiBoxRenderer.renderFrame(graphics, x, y, w, h, -14737633, -13421773);
    }

    private void renderGuiBoxTitle(GuiGraphics graphics, int x, int y, int boxW, String title) {
        GuiBoxRenderer.renderTitle(graphics, this.minecraft.font, x, y, boxW, title, this.width, -17664);
    }

    private void renderPreviewContent(GuiGraphics graphics, float mouseX, float mouseY) {
        int gx = this.layout.gridOriginX;
        int gy = this.layout.gridOriginY;
        int gw = this.layout.gridW;
        int gh = this.layout.gridH;
        graphics.fill(gx, gy, gx + gw, gy + gh, -16119286);
        graphics.enableScissor(gx, gy, gx + gw, gy + gh);
        if (this.isDragging) {
            this.renderGridLines(graphics);
        }

        this.renderElements(graphics, mouseX, mouseY);
        graphics.disableScissor();
        Font font = this.minecraft.font;
        String gridInfo = "Video format: PAL | 30×16";
        int infoY = gy + gh + 4;
        graphics.drawString(font, gridInfo, gx, infoY, -4473925, true);
    }

    private void renderGridLines(GuiGraphics graphics) {
        int ox = this.layout.gridOriginX;
        int oy = this.layout.gridOriginY;

        for (int col = 1; col < 30; col++) {
            int x = ox + col * this.layout.cellW;
            graphics.fill(x, oy, x + 1, oy + this.layout.gridH, -2143865033);
        }

        for (int row = 1; row < 16; row++) {
            int y = oy + row * this.layout.cellH;
            graphics.fill(ox, y, ox + this.layout.gridW, y + 1, -2143865033);
        }
    }

    private void renderElements(GuiGraphics graphics, float mouseX, float mouseY) {
        McmRenderer.ensureLoaded();
        this.hoveredElement = null;
        if (!this.isDragging && this.layout.isInsideGrid(mouseX, mouseY)) {
            this.hoveredElement = this.hitTestGui(mouseX, mouseY);
        }

        for (OsdElement elem : this.editingLayout.getVisibleElements()) {
            OsdElementType type = OsdElementRegistry.get(elem.getName());
            if (type != null) {
                float sx = this.layout.gridToScreenX(elem.getX());
                float sy = this.layout.gridToScreenY(elem.getY());
                String text = type.getPreviewText();
                if (text == null || text.isEmpty()) {
                    text = type.getDisplayName();
                }

                boolean isHovered = elem.getName().equals(this.hoveredElement) || elem.getName().equals(this.draggedElement);
                if (isHovered) {
                    float totalW = type.getWidthChars() * this.layout.cellW;
                    float totalH = type.getHeightLines() * this.layout.cellH;
                    graphics.fill((int)(sx - 1.0F), (int)(sy - 1.0F), (int)(sx + totalW + 1.0F), (int)(sy + totalH + 1.0F), -2130724096);
                }

                if (type.getHeightLines() > 1 && text.length() > type.getWidthChars()) {
                    int w = type.getWidthChars();

                    for (int row = 0; row < type.getHeightLines(); row++) {
                        int start = row * w;
                        int end = Math.min(start + w, text.length());
                        if (start >= text.length()) {
                            break;
                        }

                        String rowText = text.substring(start, end);
                        McmRenderer.drawPlainGrid(graphics, rowText, sx, sy + row * this.layout.cellH, this.layout.cellW, this.layout.cellH);
                    }
                } else {
                    McmRenderer.drawPlainGrid(graphics, text, sx, sy, this.layout.cellW, this.layout.cellH);
                }
            }
        }
    }

    private void renderEditBoxInSection(GuiGraphics graphics, EditBox field, int sectionIndex, int mouseX, int mouseY, float partialTicks) {
        int sectionY = this.layout.elemBoxTop + sectionIndex * (this.layout.settSectionH + this.layout.settGap);
        int sectionBottom = sectionY + this.layout.settSectionH;
        int panelBottom = this.layout.elemBoxTop + this.layout.elemBoxH;
        if (sectionY < panelBottom && sectionBottom > this.layout.elemBoxTop) {
            int clipTop = Math.max(sectionY, this.layout.elemBoxTop);
            int clipBot = Math.min(sectionBottom, panelBottom);
            int clipLeft = this.layout.settPanelX;
            int clipRight = Math.min(this.layout.settPanelX + this.layout.settPanelW, this.width);
            Font font = this.minecraft.font;
            int textX = this.layout.settingsFieldX();
            int frameTop = sectionY + 14;
            int frameBot = sectionBottom - 2;
            int textY = frameTop + Math.max(0, (frameBot - frameTop - 9) / 2);
            graphics.enableScissor(clipLeft, clipTop, clipRight, clipBot);
            String text = field.getValue();
            if (text.isEmpty()) {
                text = field.getMessage().getString();
            }

            int textColor = field.isFocused() ? -855310 : -4473925;
            graphics.drawString(font, text, textX, textY, textColor, true);
            if (field.isFocused()) {
                String beforeCursor = text.substring(0, Math.min(field.getCursorPosition(), text.length()));
                int cursorX = textX + font.width(beforeCursor);
                if (System.currentTimeMillis() % 1000L < 500L) {
                    graphics.fill(cursorX, textY - 1, cursorX + 1, textY + 9, -855310);
                }
            }

            graphics.disableScissor();
        }
    }

    private void renderSettingsPanel(GuiGraphics graphics) {
        Font font = this.minecraft.font;
        int boxX = this.layout.settPanelX;
        int boxY = this.layout.elemBoxTop;
        int boxW = this.layout.settPanelW;
        int panelBottom = boxY + this.layout.elemBoxH;
        int clipRight = Math.min(boxX + boxW, this.width);
        int sectionH = this.layout.settSectionH;
        int gap = this.layout.settGap;
        int y = boxY;

        for (String section : SETTINGS_SECTIONS) {
            if (y + sectionH > panelBottom) {
                break;
            }

            this.renderGuiBox(graphics, boxX, y, boxW, sectionH, section);
            graphics.enableScissor(boxX, y, clipRight, Math.min(y + sectionH, panelBottom));
            int textX = boxX + this.layout.boxPad + 2;
            String content = null;
            if (section.equals("Video Format")) {
                content = "PAL";
            } else if (section.equals("Units")) {
                content = "Metric";
            }

            if (content != null) {
                int frameTop = y + 14;
                int frameBot = y + sectionH - 2;
                int textY = frameTop + Math.max(0, (frameBot - frameTop - 9) / 2);
                graphics.drawString(font, content, textX, textY, -4473925, true);
            }

            graphics.disableScissor();
            y += sectionH + gap;
        }
    }

    private void renderToolbar(GuiGraphics graphics, float mouseX, float mouseY) {
        graphics.fill(this.layout.toolbarX, this.layout.toolbarY - 2, this.layout.toolbarX + this.layout.toolbarW, this.layout.toolbarY, 1073741824);
        graphics.fill(
            this.layout.toolbarX,
            this.layout.toolbarY,
            this.layout.toolbarX + this.layout.toolbarW,
            this.layout.toolbarY + this.layout.toolbarH_actual,
            -14408668
        );
        graphics.fill(this.layout.toolbarX, this.layout.toolbarY, this.layout.toolbarX + this.layout.toolbarW, this.layout.toolbarY + 1, -13421773);
        Font font = this.minecraft.font;

        for (OsdBuilderScreen.ToolbarButton btn : this.toolbarButtons) {
            boolean hovered = btn.contains(mouseX, mouseY);
            int bgColor = hovered ? -14217 : -17664;
            graphics.fill(btn.x(), btn.y(), btn.x() + btn.w(), btn.y() + btn.h(), bgColor);
            int textY = btn.y() + (btn.h() - 9) / 2 + 1;
            int textX = btn.x() + (btn.w() - font.width(btn.label())) / 2;
            graphics.drawString(font, btn.label(), textX, textY, -16777216, false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.layout.tooSmall) {
            return false;
        } else {
            if (button == 0 && this.layout.settingsVisible) {
                boolean craftHit = this.craftNameField != null && this.isInsideSection(mouseX, mouseY, 5);
                boolean pilotHit = this.pilotNameField != null && this.isInsideSection(mouseX, mouseY, 6);
                if (craftHit) {
                    this.craftNameField.setFocused(true);
                    if (this.pilotNameField != null) {
                        this.pilotNameField.setFocused(false);
                    }

                    return true;
                }

                if (pilotHit) {
                    this.pilotNameField.setFocused(true);
                    if (this.craftNameField != null) {
                        this.craftNameField.setFocused(false);
                    }

                    return true;
                }

                if (this.craftNameField != null) {
                    this.craftNameField.setFocused(false);
                }

                if (this.pilotNameField != null) {
                    this.pilotNameField.setFocused(false);
                }
            }

            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (button != 0) {
                return false;
            } else if (mouseY >= this.layout.toolbarY && mouseY < this.height) {
                this.handleToolbarClick(mouseX, mouseY);
                return true;
            } else {
                if (this.layout.isInsideGrid(mouseX, mouseY)) {
                    String hit = this.hitTestGui(mouseX, mouseY);
                    if (hit != null) {
                        this.draggedElement = hit;
                        this.isDragging = true;
                        OsdElement elem = this.editingLayout.getElement(hit);
                        if (elem != null) {
                            float gridMX = this.layout.mouseToGridX(mouseX);
                            float gridMY = this.layout.mouseToGridY(mouseY);
                            this.dragOffsetGridX = gridMX - elem.getX();
                            this.dragOffsetGridY = gridMY - elem.getY();
                        }

                        OsdLogger.builderDebug("Started drag on {}", hit);
                        return true;
                    }
                }

                return false;
            }
        }
    }

    private void handleToolbarClick(double mouseX, double mouseY) {
        for (OsdBuilderScreen.ToolbarButton btn : this.toolbarButtons) {
            if (btn.contains(mouseX, mouseY)) {
                btn.action().run();
                return;
            }
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        } else if (this.isDragging && this.draggedElement != null) {
            OsdElement elem = this.editingLayout.getElement(this.draggedElement);
            OsdElementType type = OsdElementRegistry.get(this.draggedElement);
            if (elem != null && type != null) {
                float rawGridX = this.layout.mouseToGridX(mouseX) - this.dragOffsetGridX;
                float rawGridY = this.layout.mouseToGridY(mouseY) - this.dragOffsetGridY;
                float snappedX = OsdLayout.snapToHalf(rawGridX);
                float snappedY = OsdLayout.snapToHalf(rawGridY);
                float clampedX = OsdGridHelper.clampGridX(snappedX, type.getWidthChars());
                float clampedY = OsdGridHelper.clampGridY(snappedY, type.getHeightLines());
                elem.setX(clampedX);
                elem.setY(clampedY);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isDragging && this.draggedElement != null) {
            OsdElement elem = this.editingLayout.getElement(this.draggedElement);
            if (elem != null) {
                OsdLogger.builderDebug("Drag {} to ({}, {})", this.draggedElement, elem.getX(), elem.getY());
            }

            this.isDragging = false;
            this.draggedElement = null;
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    private boolean isInsideSection(double mouseX, double mouseY, int sectionIndex) {
        int sectionY = this.layout.elemBoxTop + sectionIndex * (this.layout.settSectionH + this.layout.settGap);
        int sectionBottom = sectionY + this.layout.settSectionH;
        int panelBottom = this.layout.elemBoxTop + this.layout.elemBoxH;
        return sectionY < panelBottom && sectionBottom > this.layout.elemBoxTop
            ? mouseX >= this.layout.settPanelX && mouseX < this.layout.settPanelX + this.layout.settPanelW && mouseY >= sectionY && mouseY < sectionBottom
            : false;
    }

    public boolean charTyped(char ch, int modifiers) {
        if (this.craftNameField != null && this.craftNameField.isFocused() && this.craftNameField.charTyped(ch, modifiers)) {
            return true;
        } else {
            return this.pilotNameField != null && this.pilotNameField.isFocused() && this.pilotNameField.charTyped(ch, modifiers)
                ? true
                : super.charTyped(ch, modifiers);
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 292) {
            this.debugOverlay = !this.debugOverlay;
            return true;
        } else if (this.craftNameField != null && this.craftNameField.isFocused() && this.craftNameField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            return this.pilotNameField != null && this.pilotNameField.isFocused() && this.pilotNameField.keyPressed(keyCode, scanCode, modifiers)
                ? true
                : super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void tick() {
        super.tick();
        if (this.craftNameField != null) {
            this.craftNameField.tick();
        }

        if (this.pilotNameField != null) {
            this.pilotNameField.tick();
        }
    }

    private String hitTestGui(double mouseX, double mouseY) {
        for (OsdElement elem : this.editingLayout.getVisibleElements()) {
            OsdElementType type = OsdElementRegistry.get(elem.getName());
            if (type != null) {
                float sx = this.layout.gridToScreenX(elem.getX());
                float sy = this.layout.gridToScreenY(elem.getY());
                float totalW = type.getWidthChars() * this.layout.cellW;
                float totalH = type.getHeightLines() * this.layout.cellH;
                if (mouseX >= sx && mouseX < sx + totalW && mouseY >= sy && mouseY < sy + totalH) {
                    return elem.getName();
                }
            }
        }

        return null;
    }

    private void saveLayout() {
        if (this.craftNameField != null && this.drone.getConfig() != null) {
            String craftName = this.craftNameField.getValue().trim();
            this.drone.getConfig().setDroneName(craftName.isEmpty() ? "CRAFT" : craftName);
        }

        if (this.pilotNameField != null) {
            String pilotName = this.pilotNameField.getValue().trim();
            OsdElement pilotElem = this.editingLayout.getElement("PILOT_NAME");
            if (pilotElem != null) {
                pilotElem.setCustomText(pilotName.isEmpty() ? null : pilotName.toUpperCase());
            }
        }

        this.drone.setOsdLayout(this.editingLayout);
        ModNetwork.CHANNEL.sendToServer(new OsdLayoutPacket(this.drone.getId(), this.editingLayout));
        OsdLogger.builderInfo("Layout saved for drone {}", this.drone.getId());
        this.onClose();
    }

    private void resetToDefaults() {
        this.editingLayout = OsdLayout.defaults();
        this.removeWidget(this.elementList);
        this.elementList = new OsdElementList(this, this.layout.listX, this.layout.listY, this.layout.listW, this.layout.listH, this.editingLayout, () -> {});
        this.addWidget(this.elementList);
        if (this.craftNameField != null) {
            String defaultName = this.drone.getConfig() != null ? this.drone.getConfig().getDroneName() : "CRAFT";
            this.craftNameField.setValue(defaultName);
        }

        if (this.pilotNameField != null) {
            String defaultPilot = this.minecraft.player != null ? this.minecraft.player.getGameProfile().getName().toUpperCase() : "PILOT";
            this.pilotNameField.setValue(defaultPilot);
        }

        OsdLogger.builderInfo("Layout reset to defaults for drone {}", this.drone.getId());
    }

    private void exportLayout() {
        if (this.drone.getConfig() == null) {
            OsdLogger.builderWarn("Cannot export layout — drone config is null");
        } else {
            String craftName = this.drone.getConfig().getDroneName();

            try {
                Path exported = OsdLayoutIO.exportLayout(this.editingLayout, craftName);
                OsdLogger.builderInfo("Exported layout to {}", exported.getFileName());
                this.statusMessage = "Exported: " + exported.getFileName();
                this.statusMessageExpiry = System.currentTimeMillis() + 5000L;
            } catch (Exception var3) {
                OsdLogger.builderWarn("Failed to export layout: {}", var3.getMessage());
                OsdLogger.layoutError("Export stack trace", var3);
                this.statusMessage = "Export failed: " + var3.getMessage();
                this.statusMessageExpiry = System.currentTimeMillis() + 5000L;
            }
        }
    }

    private void openImportScreen() {
        this.minecraft
            .setScreen(
                new OsdImportScreen(
                    this,
                    importedLayout -> {
                        this.editingLayout = importedLayout;
                        this.removeWidget(this.elementList);
                        this.elementList = new OsdElementList(
                            this, this.layout.listX, this.layout.listY, this.layout.listW, this.layout.listH, this.editingLayout, () -> {}
                        );
                        this.addWidget(this.elementList);
                        OsdLogger.builderInfo("Imported layout applied for drone {}", this.drone.getId());
                    }
                )
            );
    }

    public void resize(Minecraft mc, int newWidth, int newHeight) {
        this.width = newWidth;
        this.height = newHeight;
        super.resize(mc, newWidth, newHeight);
    }

    @Override
    public void onClose() {
        OsdLogger.builderInfo("Screen closed for drone {}", this.drone.getId());
        super.onClose();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private record ToolbarButton(String label, int x, int y, int w, int h, Runnable action) {
        boolean contains(double mx, double my) {
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }
    }
}
