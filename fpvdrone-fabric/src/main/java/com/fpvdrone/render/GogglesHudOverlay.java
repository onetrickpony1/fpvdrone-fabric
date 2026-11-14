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

    // ---- 1.1.8 OSD profiles (OKxteFPhA / HBikj69R): MAX7456 (this class) or ZALA (ZalaOsd), per aircraft camera ----

    /** 1.1.8 GogglesHudOverlay.KKeMvpuBgEY: whether the camera shows an OSD, and which one. */
    private record OsdChoice(boolean showOsd, com.fpvdrone.data.DroneDefinition.OsdProfile profile) {
        static final OsdChoice DEFAULT = new OsdChoice(true, com.fpvdrone.data.DroneDefinition.OsdProfile.v5suI);
    }

    private static final java.util.Map<UUID, OsdChoice> droneOsdChoice = new java.util.HashMap<>();
    private static OsdChoice munitionOsdChoice = null;
    private static PhysicsState munitionPhysics = null;
    private static OsdLayout munitionLayout = null;
    private static Max7456CellBuffer munitionFrozenBuffer = null;

    /** 1.1.8 OKxteFPhA.v5suI(profile, frame). */
    private static void renderProfile(com.fpvdrone.data.DroneDefinition.OsdProfile profile, OsdFrame frame) {
        if (profile == com.fpvdrone.data.DroneDefinition.OsdProfile.ibzfxic) {
            ZalaOsd.render(frame);
        } else if (frame.feed() == OsdFrame.Feed.MUNITION) {
            renderMunitionOsd(frame);
        } else {
            renderOSD(frame.graphics(), frame.mc(), frame.player(), frame.width(), frame.height(), frame.frozen());
        }
    }

    /** 1.1.8 OKxteFPhA.v5suI(feed): forget the per-feed OSD state (only the MAX7456 renderer keeps any). */
    private static void resetFeed(OsdFrame.Feed feed) {
        if (feed == OsdFrame.Feed.MUNITION) {
            munitionFrozenBuffer = null;
            munitionPhysics = null;
        } else {
            cachedLayout = null;
            cachedLayoutDroneId = null;
            frozenOsdBuffer = null;
        }
    }

    private static OsdChoice droneChoice(UUID playerId, UUID droneId) {
        Optional<DroneEntity> drone = FPVMod.getActiveDrone(playerId);
        if (drone.isPresent()) {
            com.fpvdrone.data.DroneDefinition.Camera camera = drone.get().oEuCBB().bAcdvq9();
            OsdChoice choice = new OsdChoice(camera.showOsd(), camera.osdProfile());
            droneOsdChoice.put(droneId, choice);
            return choice;
        }

        return droneOsdChoice.getOrDefault(droneId, OsdChoice.DEFAULT);
    }

    /** 1.1.8 GogglesHudOverlay.v5suI(): back to the default OSD profile after a loitering munition view. */
    public static void resetOsdProfile() {
        munitionOsdChoice = null;
        resetFeed(OsdFrame.Feed.MUNITION);
    }

    public static void init() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("fpvdrone", "goggles_osd"), GogglesHudOverlay::render);
    }

    /**
     * The battery, once per frame. The original ran this inside the OSD drawing; in 26.3 the OSD is not drawn while
     * the HUD is hidden (F1), and the clean feed hides it too, which would stop the drain. So the video hook calls
     * this every frame, under the same conditions the OSD used: goggles on, flying, signal, battery in or booting.
     */
    public static void tickBattery() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof FpvGogglesItem)) {
            return;
        }

        UUID playerId = player.getUUID();
        Optional<UUID> activeDroneId = FPVMod.getActiveDroneId(playerId);
        if (activeDroneId.isEmpty()) {
            return;
        }

        if (FpvStatusChecker.hasNoSignalState(activeDroneId.get())) {
            return;
        }

        Optional<DroneEntity> activeDrone = FPVMod.getActiveDrone(playerId);
        if (FPVMod.isArmed(playerId) && activeDrone.isEmpty()) {
            return;
        }

        boolean hasBattery = activeDrone.map(DroneEntity::hasBattery).orElse(false);
        if (!hasBattery && !BootStateManager.getInstance().isBootActive()) {
            return;
        }

        BatteryManager battery = BatteryManager.getInstance();
        float totalWeight = activeDrone.map(DroneEntity::getTotalWeight).orElse(580.0F);
        activeDrone.ifPresent(dronex -> {
            battery.setHasBattery(dronex.hasBattery());
            battery.setMotorsArmed(dronex.areMotorsArmed());
            if (dronex.hasBattery()) {
                battery.setBatteryCells(dronex.getBattery().getCells());
            }
        });
        battery.setCurrentMultiplier(com.fpvdrone.client.CleanFeed.currentMultiplier());
        battery.tick(ControllerReader.getThrottle(), totalWeight);
        if (battery.isDead() && !SignalManager.getInstance().isInGracePeriod()) {
            SignalManager.getInstance().forceNoSignal();
            FPVMod.getActiveDroneId(playerId).ifPresent(FpvStatusChecker::markSignalLost);
        }
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            // 1.1.8: the OSD is drawn in first person only
            if (player != null && mc.options.getCameraType().isFirstPerson()) {
                if (com.fpvdrone.client.MunitionClientState.iElAbEZe()) {
                    renderMunitionFeed(mc, player, graphics, delta.getGameTimeDeltaPartialTick(false));
                    return;
                }

                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.getItem() instanceof FpvGogglesItem) {
                    UUID playerId = player.getUUID();
                    int screenWidth = mc.getWindow().getWidth();
                    int screenHeight = mc.getWindow().getHeight();
                    if (FPVMod.getActiveDroneId(playerId).isEmpty()) {
                        resetFeed(OsdFrame.Feed.DRONE);
                        droneOsdChoice.clear();
                    } else {
                        UUID viewedDroneId = FPVMod.getActiveDroneId(playerId).get();
                        OsdChoice choice = droneChoice(playerId, viewedDroneId);
                        if (!choice.showOsd()) {
                            resetFeed(OsdFrame.Feed.DRONE);
                            return;
                        }

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
                                if (!com.fpvdrone.client.CleanFeed.isActive()) {
                                    renderProfile(choice.profile(), OsdFrame.drone(
                                        viewedDroneId, mc, graphics, player, screenWidth, screenHeight, true, FPVMod.getActiveDrone(playerId).orElse(null)));
                                }
                                graphics.pose().popMatrix();
                                return;
                            }
                        }

                        Optional<DroneEntity> osdDrone = FPVMod.getActiveDrone(playerId);
                        boolean hasBattery = osdDrone.map(DroneEntity::hasBattery).orElse(false);
                        if (!hasBattery && !BootStateManager.getInstance().isBootActive() || com.fpvdrone.client.CleanFeed.isActive()) {
                            graphics.pose().popMatrix();
                        } else {
                            renderProfile(choice.profile(), OsdFrame.drone(
                                viewedDroneId, mc, graphics, player, screenWidth, screenHeight, false, osdDrone.orElse(null)));
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
            frozenOsdBuffer = drawLayout(layout, ctx, frozenOsdBuffer, true);
        }
    }

    /**
     * 1.1.8 GogglesHudOverlay.v5suI(layout, ctx, frozen, clearDisarmed): fills the MAX7456 grid and draws it. With no
     * signal the frozen grid is shown and only the warnings (and DISARMED for drones) are rewritten; otherwise the
     * grid is built afresh (background, middle, foreground elements) and kept as the new frozen grid.
     */
    private static Max7456CellBuffer drawLayout(OsdLayout layout, OsdRenderContext ctx, Max7456CellBuffer frozen, boolean clearDisarmed) {
        java.util.Collection<OsdElement> elements = layout.getVisibleElements();
        if (ctx.noSignal && frozen != null) {
            cellBuffer.clear();

            for (int r = 0; r < 16; r++) {
                for (int c = 0; c < 30; c++) {
                    char ch = frozen.getChar(c, r);
                    if (ch != 0) {
                        cellBuffer.writeChar(c, r, ch, frozen.getOffsetX(c, r), frozen.getOffsetY(c, r));
                    }
                }
            }

            for (OsdElement elem : elements) {
                String name = elem.getName();
                if ("WARNINGS".equals(name) || clearDisarmed && "DISARMED".equals(name)) {
                    OsdElementType type = OsdElementRegistry.get(name);
                    if (type != null) {
                        int baseCol = (int)Math.floor(elem.getX());
                        int baseRow = (int)Math.floor(elem.getY());

                        for (int row = baseRow; row < baseRow + type.getHeightLines() && row < 16; row++) {
                            for (int col = baseCol; col < baseCol + type.getWidthChars() && col < 30; col++) {
                                if (col >= 0 && row >= 0) {
                                    cellBuffer.writeChar(col, row, '\u0000');
                                }
                            }
                        }

                        writeWarningsToBuffer(elem, ctx, type);
                    }
                }
            }
        } else {
            cellBuffer.clear();

            for (OsdElement elem : elements) {
                if (isBackgroundElement(elem.getName())) {
                    writeElementToBuffer(elem, ctx);
                }
            }

            for (OsdElement elem : elements) {
                if (!isBackgroundElement(elem.getName()) && !isForegroundElement(elem.getName())) {
                    writeElementToBuffer(elem, ctx);
                }
            }

            for (OsdElement elem : elements) {
                if (isForegroundElement(elem.getName())) {
                    writeElementToBuffer(elem, ctx);
                }
            }

            if (!ctx.noSignal) {
                frozen = cellBuffer.snapshot();
            }
        }

        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 30; col++) {
                char ch = cellBuffer.getChar(col, row);
                if (ch != 0) {
                    float screenX = ctx.gridOffsetX + (col + cellBuffer.getOffsetX(col, row)) * ctx.cellW;
                    float screenY = ctx.gridOffsetY + (row + cellBuffer.getOffsetY(col, row)) * ctx.cellH;
                    McmRenderer.drawOutlinedGrid(ctx.graphics, String.valueOf(ch), Math.round(screenX), Math.round(screenY), ctx.cellW, ctx.cellH);
                }
            }
        }

        return frozen;
    }

    // ---- loitering munition feed (1.1.8 GogglesHudOverlay munition branch) ----

    private static void renderMunitionFeed(Minecraft mc, Player player, GuiGraphicsExtractor graphics, float partialTick) {
        boolean observing = com.fpvdrone.client.MunitionClientState.ifpFOCaAs();
        com.fpvdrone.entity.LoiteringMunitionEntity munition = com.fpvdrone.client.MunitionClientState.lFt1ckP();
        if (!observing) {
            if (munition == null) {
                return;
            }

            com.fpvdrone.data.DroneDefinition.Camera camera = munition.uQdlyiwB().bAcdvq9();
            munitionOsdChoice = new OsdChoice(camera.showOsd(), camera.osdProfile());
        }

        if (munitionOsdChoice == null || !munitionOsdChoice.showOsd()) {
            return;
        }

        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        UUID sourceId = munition != null ? munition.getUUID() : com.fpvdrone.client.MunitionClientState.ivDR();
        if (sourceId == null) {
            return;
        }

        double guiScale = mc.getWindow().getGuiScale();
        graphics.pose().pushMatrix();
        graphics.pose().scale(1.0F / (float)guiScale, 1.0F / (float)guiScale);
        OsdFrame frame;
        if (!observing && munition != null && com.fpvdrone.client.MunitionClientState.we1GYY()) {
            boolean gimbalObserve = com.fpvdrone.client.MunitionClientState.v5suI();
            float markerX = 0.0F;
            float markerY = 0.0F;
            boolean onScreen = true;
            if (com.fpvdrone.client.MunitionClientState.bAcdvq9()) {
                com.fpvdrone.client.AimMath.Ol3TEDSog projected = projectTarget(mc, width, height, partialTick);
                if (projected != null && !projected.wuhB()) {
                    markerX = projected.v5suI();
                    markerY = projected.ibzfxic() + (0.5F - ZalaOsd.centreFraction()) * height;
                    onScreen = markerOnScreen(markerX, markerY, width, height);
                } else {
                    onScreen = false;
                }
            }

            float gimbalYaw = gimbalObserve ? com.fpvdrone.client.MunitionClientState.r6s7FL3()[0] : 0.0F;
            boolean fullReticle = gimbalObserve || munition.wyNDWDB();
            com.fpvdrone.client.MunitionClientState.v5suI(markerX, markerY, onScreen, gimbalObserve, gimbalYaw, fullReticle);
            frame = gimbalObserve
                ? OsdFrame.observe(sourceId, mc, graphics, player, width, height, munition, markerX, markerY, onScreen, gimbalYaw)
                : OsdFrame.designate(sourceId, mc, graphics, player, width, height, false, munition, markerX, markerY, onScreen, fullReticle);
        } else if (observing && com.fpvdrone.client.MunitionClientState.qlv6NKWnH()) {
            // the last marker state, kept while the link is down
            float markerX = com.fpvdrone.client.MunitionClientState.hOKMYHOaC();
            float markerY = com.fpvdrone.client.MunitionClientState.iS1JnZov();
            boolean onScreen = com.fpvdrone.client.MunitionClientState.lfyfLwuw();
            frame = com.fpvdrone.client.MunitionClientState.wyNDWDB()
                ? OsdFrame.observe(sourceId, mc, graphics, player, width, height, munition, markerX, markerY, onScreen, com.fpvdrone.client.MunitionClientState.iRWc2Pi())
                : OsdFrame.designate(sourceId, mc, graphics, player, width, height, true, munition, markerX, markerY, onScreen, com.fpvdrone.client.MunitionClientState.tpzIHm());
        } else {
            frame = OsdFrame.munition(sourceId, mc, graphics, player, width, height, observing, munition);
        }

        renderProfile(munitionOsdChoice.profile(), frame);
        graphics.pose().popMatrix();
    }

    /** 1.1.8: the designated target projected into window pixels relative to the screen centre. */
    private static com.fpvdrone.client.AimMath.Ol3TEDSog projectTarget(Minecraft mc, int width, int height, float partialTick) {
        net.minecraft.client.Camera camera = mc.gameRenderer.mainCamera();
        net.minecraft.world.phys.Vec3 eye = camera.position();
        com.fpvdrone.client.AimMath.KKeMvpuBgEY basis = com.fpvdrone.client.AimMath.v5suI(camera.yRot(), camera.xRot());
        double fov = com.fpvdrone.client.MunitionClientState.iAKNPAj();
        net.minecraft.world.phys.Vec3 target = com.fpvdrone.client.MunitionClientState.v5suI(partialTick);
        return com.fpvdrone.client.AimMath.v5suI(
            eye.x, eye.y, eye.z,
            basis.v5suI(), basis.ibzfxic(), basis.wuhB(), basis.jGhI2(), basis.wEJx3Itb(), basis.bAcdvq9(), basis.dOfFwvo(), basis.rsDd50tfV(), basis.uSUb(),
            target.x, target.y, target.z, width, height, fov
        );
    }

    private static boolean markerOnScreen(float markerX, float markerY, int width, int height) {
        float x = width / 2.0F + markerX;
        float y = height * ZalaOsd.centreFraction() + markerY;
        return x >= 0.0F && x <= width && y >= 0.0F && y <= height;
    }

    /** 1.1.8 GogglesHudOverlay.ibzfxic(frame): the MAX7456 OSD for a loitering munition, with a fixed fixed-wing layout. */
    private static void renderMunitionOsd(OsdFrame frame) {
        Minecraft mc = frame.mc();
        Player player = frame.player();
        GuiGraphicsExtractor graphics = frame.graphics();
        boolean frozen = frame.frozen();
        com.fpvdrone.entity.LoiteringMunitionEntity munition = frame.munition();
        if (!frozen) {
            if (munition == null) {
                return;
            }
        } else if (munitionFrozenBuffer == null) {
            return;
        }

        OsdElementRegistry.bindLiveData();
        McmRenderer.ensureLoaded();
        if (munitionPhysics == null) {
            munitionPhysics = new PhysicsState(player);
        }

        if (!frozen) {
            munitionPhysics.syncBasisFromAngles(munition.getYRot(), munition.getXRot(), munition.bAcdvq9());
            munitionPhysics.setVelocity(munition.getDeltaMovement().scale(20.0));
        }

        int screenWidth = frame.width();
        int screenHeight = frame.height();
        int scale = Math.min(Math.max(2, Math.round(screenHeight / 288.0F)), Math.max(2, Math.round(screenWidth / 360.0F)));
        float cellW = 12 * scale;
        float cellH = 18 * scale;
        float gridOffsetX = Math.round((screenWidth - cellW * 30.0F) / 2.0F);
        float gridOffsetY = Math.round((screenHeight - cellH * 16.0F) / 2.0F);
        OsdRenderContext ctx = new OsdRenderContext(
            null, BatteryManager.getInstance(), SignalManager.getInstance(), munitionPhysics, player, graphics, mc.font,
            screenWidth, screenHeight, cellH / 9.0F, cellW, cellH, gridOffsetX, gridOffsetY, frozen
        );
        munitionFrozenBuffer = drawLayout(munitionLayout(), ctx, munitionFrozenBuffer, false);
    }

    private static OsdLayout munitionLayout() {
        if (munitionLayout == null) {
            OsdLayout layout = new OsdLayout();
            layout.setElement("ARTIFICIAL_HORIZON", new OsdElement("ARTIFICIAL_HORIZON", 11.0F, 4.0F, true));
            layout.setElement("HORIZON_SIDEBARS", new OsdElement("HORIZON_SIDEBARS", 10.0F, 5.0F, true));
            layout.setElement("CROSSHAIRS", new OsdElement("CROSSHAIRS", 14.0F, 7.0F, true));
            layout.setElement("ALTITUDE", new OsdElement("ALTITUDE", 23.0F, 12.0F, true));
            layout.setElement("GPS_SPEED", new OsdElement("GPS_SPEED", 24.0F, 10.0F, true));
            layout.setElement("NUMERICAL_VARIO", new OsdElement("NUMERICAL_VARIO", 24.0F, 11.0F, true));
            layout.setElement("COMPASS_BAR", new OsdElement("COMPASS_BAR", 11.0F, 2.0F, true));
            layout.setElement("NUMERICAL_HEADING", new OsdElement("NUMERICAL_HEADING", 14.0F, 1.0F, true));
            layout.setElement("PITCH_ANGLE", new OsdElement("PITCH_ANGLE", 1.0F, 12.0F, true));
            layout.setElement("ROLL_ANGLE", new OsdElement("ROLL_ANGLE", 1.0F, 13.0F, true));
            layout.setElement("WARNINGS", new OsdElement("WARNINGS", 7.5F, 8.0F, true));
            munitionLayout = layout;
        }

        return munitionLayout;
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
