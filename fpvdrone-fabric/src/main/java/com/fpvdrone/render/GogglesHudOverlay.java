package com.fpvdrone.render;

import com.fpvdrone.FPVMod;
import com.fpvdrone.client.BatteryManager;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.client.SignalManager;
import com.fpvdrone.client.Transforms;
import com.fpvdrone.client.osd.BootStateManager;
import com.fpvdrone.client.osd.Max7456CellBuffer;
import com.fpvdrone.client.osd.McmRenderer;
import com.fpvdrone.client.osd.OsdElement;
import com.fpvdrone.client.osd.OsdElementRegistry;
import com.fpvdrone.client.osd.OsdElementType;
import com.fpvdrone.client.osd.OsdLayout;
import com.fpvdrone.client.osd.OsdRenderContext;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.physics.PhysicsState;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

/** The goggles' OSD: drawn just under the chat, in window pixels, on a 30 x 16 MAX7456 character grid. */
public class GogglesHudOverlay {
    private static final int TEXT_COLOR = -1;
    private static final int OUTLINE_COLOR = -16777216;
    private static OsdLayout cachedLayout = null;
    private static UUID cachedLayoutDroneId = null;
    private static Max7456CellBuffer frozenOsdBuffer = null;
    private static final String CMS_HELP_TEXT1 = "MENU:THR MID";
    private static final String CMS_HELP_TEXT2 = "+ YAW LEFT";
    private static final String CMS_HELP_TEXT3 = "+ PITCH UP";
    private static final String BOOT_VERSION = "V1.0.0";
    private static final Max7456CellBuffer cellBuffer = new Max7456CellBuffer();
    private static final long OSD_REFRESH_INTERVAL_MS = 166L;
    private static long lastHorizonUpdateMs = 0L;
    private static int[] cachedHorizonY = null;
    private static int[] cachedHorizonSub = null;
    private static int[] cachedHorizonRow = null;

    static void drawOutlinedString(GuiGraphicsExtractor graphics, Font font, String text, int x, int y) {
        graphics.text(font, text, x - 1, y, -16777216, false);
        graphics.text(font, text, x + 1, y, -16777216, false);
        graphics.text(font, text, x, y - 1, -16777216, false);
        graphics.text(font, text, x, y + 1, -16777216, false);
        graphics.text(font, text, x, y, -1, false);
    }

    public static void init() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("fpvdrone", "goggles_osd"), GogglesHudOverlay::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player != null) {
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.getItem() instanceof FpvGogglesItem) {
                    UUID playerId = player.getUUID();
                    int screenWidth = mc.getWindow().getWidth();
                    int screenHeight = mc.getWindow().getHeight();
                    if (FPVMod.getActiveDroneId(playerId).isEmpty()) {
                        cachedLayout = null;
                        cachedLayoutDroneId = null;
                        frozenOsdBuffer = null;
                    } else {
                        double guiScale = mc.getWindow().getGuiScale();
                        graphics.pose().pushMatrix();
                        graphics.pose().scale(1.0F / (float)guiScale, 1.0F / (float)guiScale);
                        Optional<UUID> activeDroneId = FPVMod.getActiveDroneId(playerId);
                        Optional<UUID> linkedDroneId = FpvGogglesItem.getLinkedDroneId(helmet);
                        Optional<UUID> effectiveDroneId = activeDroneId.isPresent() ? activeDroneId : linkedDroneId;
                        if (effectiveDroneId.isPresent()) {
                            UUID droneId = effectiveDroneId.get();
                            boolean noSignal = FpvStatusChecker.hasNoSignalState(droneId);
                            if (!noSignal && FPVMod.isArmed(playerId)) {
                                Optional<DroneEntity> drone = FPVMod.getActiveDrone(playerId);
                                if (drone.isEmpty()) {
                                    noSignal = true;
                                }
                            }

                            if (noSignal) {
                                renderNoSignalScreen(graphics, mc, screenWidth, screenHeight);
                                renderOSD(graphics, mc, player, screenWidth, screenHeight, true);
                                graphics.pose().popMatrix();
                                return;
                            }
                        }

                        Optional<DroneEntity> osdDrone = FPVMod.getActiveDrone(playerId);
                        boolean hasBattery = osdDrone.map(DroneEntity::hasBattery).orElse(false);
                        if (!hasBattery && !BootStateManager.getInstance().isBootActive()) {
                            graphics.pose().popMatrix();
                        } else {
                            renderOSD(graphics, mc, player, screenWidth, screenHeight, false);
                            graphics.pose().popMatrix();
                        }
                    }
                }
            }
        }
    }

    private static void renderNoSignalScreen(GuiGraphicsExtractor graphics, Minecraft mc, int screenWidth, int screenHeight) {
        // The video feed has drawn the drone's frozen last frame under the GUI; without one, no signal is black.
        UUID droneId = FPVMod.getActiveDroneId(mc.player.getUUID()).orElse(null);
        if (droneId == null) {
            droneId = FpvGogglesItem.getLinkedDroneId(mc.player.getItemBySlot(EquipmentSlot.HEAD)).orElse(null);
        }
        if (droneId == null || !LowResRenderer.getInstance().hasFrozenFrame(droneId)) {
            graphics.fill(0, 0, screenWidth, screenHeight, -16777216);
        }
    }

    private static void renderOSD(GuiGraphicsExtractor graphics, Minecraft mc, Player player, int screenWidth, int screenHeight, boolean noSignal) {
        OsdElementRegistry.bindLiveData();
        McmRenderer.ensureLoaded();
        Font font = mc.font;
        UUID playerId = player.getUUID();
        BatteryManager battery = BatteryManager.getInstance();
        Optional<DroneEntity> activeDrone = FPVMod.getActiveDrone(playerId);
        float totalWeight = activeDrone.map(DroneEntity::getTotalWeight).orElse(580.0F);
        if (!noSignal) {
            activeDrone.ifPresent(dronex -> {
                battery.setHasBattery(dronex.hasBattery());
                battery.setMotorsArmed(dronex.areMotorsArmed());
                if (dronex.hasBattery()) {
                    battery.setBatteryCells(dronex.getBattery().getCells());
                }
            });
            battery.tick(ControllerReader.getThrottle(), totalWeight);
            if (battery.isDead() && !SignalManager.getInstance().isInGracePeriod()) {
                SignalManager.getInstance().forceNoSignal();
                FPVMod.getActiveDroneId(playerId).ifPresent(FpvStatusChecker::markSignalLost);
            }
        }

        SignalManager signal = SignalManager.getInstance();
        PhysicsState physics = FPVMod.getPhysicsState(playerId);
        DroneEntity drone = activeDrone.orElse(null);
        if (drone != null) {
            UUID droneId = drone.getUUID();
            if (cachedLayoutDroneId == null || !cachedLayoutDroneId.equals(droneId)) {
                cachedLayout = null;
                cachedLayoutDroneId = droneId;
                frozenOsdBuffer = null;
            }

            OsdLayout droneLayout = drone.getOsdLayout();
            if (droneLayout != null) {
                cachedLayout = droneLayout;
            }
        }

        OsdLayout layout = cachedLayout != null ? cachedLayout : OsdLayout.defaults();
        int scaleFromH = Math.max(2, Math.round(screenHeight / 288.0F));
        int scaleFromW = Math.max(2, Math.round(screenWidth / 360.0F));
        int scale = Math.min(scaleFromH, scaleFromW);
        float cellW = 12 * scale;
        float cellH = 18 * scale;
        float gridW = cellW * 30.0F;
        float gridH = cellH * 16.0F;
        float gridOffsetX = Math.round((screenWidth - gridW) / 2.0F);
        float gridOffsetY = Math.round((screenHeight - gridH) / 2.0F);
        float fontScale = cellH / 9.0F;
        OsdRenderContext ctx = new OsdRenderContext(
            drone, battery, signal, physics, player, graphics, font, screenWidth, screenHeight, fontScale, cellW, cellH, gridOffsetX, gridOffsetY, noSignal
        );
        if (!noSignal && BootStateManager.getInstance().getPhase() == BootStateManager.Phase.LOGO) {
            renderBootLogo(cellBuffer, gridOffsetX, gridOffsetY, cellW, cellH, graphics, screenWidth, screenHeight);
        } else {
            Max7456CellBuffer renderBuffer;
            if (noSignal && frozenOsdBuffer != null) {
                cellBuffer.clear();

                for (int r = 0; r < 16; r++) {
                    for (int c = 0; c < 30; c++) {
                        char ch = frozenOsdBuffer.getChar(c, r);
                        if (ch != 0) {
                            cellBuffer.writeChar(c, r, ch, frozenOsdBuffer.getOffsetX(c, r), frozenOsdBuffer.getOffsetY(c, r));
                        }
                    }
                }

                for (OsdElement elem : layout.getVisibleElements()) {
                    if ("WARNINGS".equals(elem.getName()) || "DISARMED".equals(elem.getName())) {
                        OsdElementType type = OsdElementRegistry.get(elem.getName());
                        if (type != null) {
                            int w = type.getWidthChars();
                            int baseCol = (int)Math.floor(elem.getX());
                            int baseRow = (int)Math.floor(elem.getY());

                            for (int cx = baseCol; cx < baseCol + w && cx < 30; cx++) {
                                if (cx >= 0 && baseRow >= 0 && baseRow < 16) {
                                    cellBuffer.writeChar(cx, baseRow, '\u0000');
                                }
                            }

                            writeWarningsToBuffer(elem, ctx, type);
                        }
                    }
                }

                renderBuffer = cellBuffer;
            } else {
                cellBuffer.clear();

                for (OsdElement elemx : layout.getVisibleElements()) {
                    if (isBackgroundElement(elemx.getName())) {
                        writeElementToBuffer(elemx, ctx);
                    }
                }

                for (OsdElement elemxx : layout.getVisibleElements()) {
                    if (!isBackgroundElement(elemxx.getName()) && !isForegroundElement(elemxx.getName())) {
                        writeElementToBuffer(elemxx, ctx);
                    }
                }

                for (OsdElement elemxxx : layout.getVisibleElements()) {
                    if (isForegroundElement(elemxxx.getName())) {
                        writeElementToBuffer(elemxxx, ctx);
                    }
                }

                if (!noSignal) {
                    frozenOsdBuffer = cellBuffer.snapshot();
                }

                renderBuffer = cellBuffer;
            }

            for (int row = 0; row < 16; row++) {
                for (int col = 0; col < 30; col++) {
                    char ch = renderBuffer.getChar(col, row);
                    if (ch != 0) {
                        float subX = renderBuffer.getOffsetX(col, row);
                        float subY = renderBuffer.getOffsetY(col, row);
                        float screenX = gridOffsetX + (col + subX) * cellW;
                        float screenY = gridOffsetY + (row + subY) * cellH;
                        McmRenderer.drawOutlinedGrid(graphics, String.valueOf(ch), Math.round(screenX), Math.round(screenY), cellW, cellH);
                    }
                }
            }
        }
    }

    private static void renderBootLogo(
        Max7456CellBuffer buffer, float gridOffsetX, float gridOffsetY, float cellW, float cellH, GuiGraphicsExtractor graphics, int screenWidth, int screenHeight
    ) {
        buffer.clear();
        int midCol = 15;
        int midRow = 8;
        int logoCols = 24;
        int logoRows = 5;
        int logoX = midCol - logoCols / 2;
        int logoY = midRow - 5;

        for (int row = 0; row < logoRows; row++) {
            for (int col = 0; col < logoCols; col++) {
                char ch = (char)(57504 + row * logoCols + col);
                float screenX = gridOffsetX + (logoX + col) * cellW;
                float screenY = gridOffsetY + (logoY + row) * cellH;
                McmRenderer.drawPlainGrid(graphics, String.valueOf(ch), Math.round(screenX), Math.round(screenY), cellW, cellH);
            }
        }

        int versionCol = midCol + 12 - "V1.0.0".length();
        float versionX = gridOffsetX + versionCol * cellW;
        float versionY = gridOffsetY + midRow * cellH;
        McmRenderer.drawOutlinedGrid(graphics, "V1.0.0", Math.round(versionX), Math.round(versionY), cellW, cellH);
        String[] hintTexts = new String[]{"MENU:THR MID", "+ YAW LEFT", "+ PITCH UP"};
        int[] hintCols = new int[]{midCol - 8, midCol - 4, midCol - 4};
        int[] hintRows = new int[]{midRow + 2, midRow + 3, midRow + 4};

        for (int i = 0; i < 3; i++) {
            float hx = gridOffsetX + hintCols[i] * cellW;
            float hy = gridOffsetY + hintRows[i] * cellH;
            McmRenderer.drawOutlinedGrid(graphics, hintTexts[i], Math.round(hx), Math.round(hy), cellW, cellH);
        }
    }

    private static boolean isBackgroundElement(String name) {
        return switch (name) {
            case "HORIZON_SIDEBARS", "CRAFT_NAME", "PILOT_NAME" -> true;
            default -> false;
        };
    }

    private static boolean isForegroundElement(String name) {
        return "CROSSHAIRS".equals(name);
    }

    private static void writeElementToBuffer(OsdElement elem, OsdRenderContext ctx) {
        OsdElementType type = OsdElementRegistry.get(elem.getName());
        if (type != null) {
            float gridX = elem.getX();
            float gridY = elem.getY();
            String var5 = elem.getName();
            switch (var5) {
                case "ARTIFICIAL_HORIZON":
                    writeHorizonToBuffer(elem, ctx);
                    break;
                case "HORIZON_SIDEBARS":
                    writeSidebarsToBuffer(ctx);
                    break;
                case "COMPASS_BAR":
                    writeCompassToBuffer(elem, ctx);
                    break;
                case "CROSSHAIRS":
                    int baseCol = (int)Math.floor(gridX);
                    float subX = gridX - baseCol;
                    float subY = gridY - (int)Math.floor(gridY);
                    int baseRow = (int)Math.floor(gridY);
                    cellBuffer.writeChar(baseCol, baseRow, '\ue072', subX, subY);
                    cellBuffer.writeChar(baseCol + 1, baseRow, '\ue073', subX, subY);
                    cellBuffer.writeChar(baseCol + 2, baseRow, '\ue074', subX, subY);
                    break;
                case "WARNINGS":
                    writeWarningsToBuffer(elem, ctx, type);
                    break;
                case "DISARMED":
                    writeWarningsToBuffer(elem, ctx, type);
                    break;
                case "CRAFT_NAME":
                case "PILOT_NAME":
                    writeCenteredTextToBuffer(elem, ctx, type);
                    break;
                default:
                    String text = type.renderText(ctx);
                    if (text == null || text.isEmpty()) {
                        return;
                    }

                    if (type.getHeightLines() > 1 && text.length() > type.getWidthChars()) {
                        int w = type.getWidthChars();

                        for (int row = 0; row < type.getHeightLines(); row++) {
                            int start = row * w;
                            int end = Math.min(start + w, text.length());
                            if (start >= text.length()) {
                                break;
                            }

                            cellBuffer.writeString(gridX, gridY + row, text.substring(start, end));
                        }
                    } else {
                        cellBuffer.writeString(gridX, gridY, text);
                    }
            }
        }
    }

    private static void writeHorizonToBuffer(OsdElement elem, OsdRenderContext ctx) {
        if (ctx.physics != null) {
            int AH_SYMBOL_COUNT = 9;
            int maxPitchTenths = 200;
            int maxRollTenths = 400;
            long now = System.currentTimeMillis();
            boolean needsUpdate = cachedHorizonY == null || now - lastHorizonUpdateMs >= 166L;
            if (needsUpdate) {
                lastHorizonUpdateMs = now;
                if (cachedHorizonY == null) {
                    cachedHorizonY = new int[9];
                    cachedHorizonSub = new int[9];
                    cachedHorizonRow = new int[9];
                }

                Vector3f look = ctx.physics.getDroneLook();
                Vector3f up = ctx.physics.getDroneUp();
                float[] angles = Transforms.getWorldEulerAngles(look, up);
                int pitchTenths = (int)(angles[1] * 10.0F);
                int rollTenths = (int)(angles[2] * 10.0F);
                int rollAngle = Math.max(-400, Math.min(400, rollTenths));
                int pitchAngle = Math.max(-200, Math.min(200, pitchTenths));
                pitchAngle = pitchAngle * 25 / 200;
                pitchAngle -= 41;

                for (int i = 0; i < 9; i++) {
                    int x = i - 4;
                    int y = -rollAngle * x / 64 - pitchAngle;
                    cachedHorizonY[i] = y;
                    if (y >= 0 && y <= 81) {
                        cachedHorizonSub[i] = y % 9;
                        cachedHorizonRow[i] = y / 9;
                    }
                }
            }

            int baseCol = Math.round(elem.getX());
            int baseRow = Math.round(elem.getY());

            for (int ix = 0; ix < 9; ix++) {
                int y = cachedHorizonY[ix];
                if (y >= 0 && y <= 81) {
                    char barChar = (char)(57472 + cachedHorizonSub[ix]);
                    cellBuffer.writeChar(baseCol + ix, baseRow + cachedHorizonRow[ix], barChar);
                }
            }
        }
    }

    private static void writeSidebarsToBuffer(OsdRenderContext ctx) {
        if (ctx.physics != null) {
            int centerRow = 8;
            int leftCol = 10;
            int rightCol = 19;

            for (int r = 0; r < 7; r++) {
                int row = centerRow - 3 + r;
                char leftChar = (char)(r == 3 ? '\ue003' : '\ue013');
                char rightChar = (char)(r == 3 ? '\ue002' : '\ue013');
                cellBuffer.writeChar(leftCol, row, leftChar);
                cellBuffer.writeChar(rightCol, row, rightChar);
            }
        }
    }

    private static void writeCompassToBuffer(OsdElement elem, OsdRenderContext ctx) {
        float yaw = OsdElementRegistry.getYaw(ctx);
        char[] fullBar = new char[]{
            '\ue01b',
            '\ue01d',
            '\ue01c',
            '\ue01d',
            '\ue018',
            '\ue01d',
            '\ue01c',
            '\ue01d',
            '\ue01a',
            '\ue01d',
            '\ue01c',
            '\ue01d',
            '\ue019',
            '\ue01d',
            '\ue01c',
            '\ue01d',
            '\ue01b',
            '\ue01d',
            '\ue01c',
            '\ue01d',
            '\ue018',
            '\ue01d',
            '\ue01c',
            '\ue01d'
        };
        int heading = ((int)yaw + 360) % 360;
        int offset = heading * 16 / 360 % 16;
        float gridX = elem.getX();
        float gridY = elem.getY();
        int baseCol = (int)Math.floor(gridX);
        int baseRow = (int)Math.floor(gridY);
        float subX = gridX - baseCol;
        float subY = gridY - baseRow;

        for (int i = 0; i < 9; i++) {
            cellBuffer.writeChar(baseCol + i, baseRow, fullBar[offset + i], subX, subY);
        }
    }

    private static void writeWarningsToBuffer(OsdElement elem, OsdRenderContext ctx, OsdElementType type) {
        String text = type.renderText(ctx);
        if (text != null && !text.isEmpty()) {
            int elemWidth = type.getWidthChars();
            int textLen = text.length();
            float offsetCols = Math.max(0.0F, (elemWidth - textLen) / 2.0F);
            cellBuffer.writeString(elem.getX() + offsetCols, elem.getY(), text);
        }
    }

    private static void writeCenteredTextToBuffer(OsdElement elem, OsdRenderContext ctx, OsdElementType type) {
        String text = type.renderText(ctx);
        if (text != null && !text.isEmpty()) {
            int elemWidth = type.getWidthChars();
            int textLen = text.length();
            float offsetCols = Math.max(0.0F, (elemWidth - textLen) / 2.0F);
            cellBuffer.writeString(elem.getX() + offsetCols, elem.getY(), text);
        }
    }
}
