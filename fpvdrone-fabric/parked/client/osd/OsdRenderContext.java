package com.fpvdrone.client.osd;

import com.fpvdrone.client.BatteryManager;
import com.fpvdrone.client.SignalManager;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.physics.PhysicsState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class OsdRenderContext {
    public final DroneEntity drone;
    public final BatteryManager battery;
    public final SignalManager signal;
    public final PhysicsState physics;
    public final Player player;
    public final GuiGraphics graphics;
    public final Font font;
    public final int screenWidth;
    public final int screenHeight;
    public final float fontScale;
    public final float cellW;
    public final float cellH;
    public final float gridOffsetX;
    public final float gridOffsetY;
    public final boolean noSignal;

    public OsdRenderContext(
        DroneEntity drone,
        BatteryManager battery,
        SignalManager signal,
        PhysicsState physics,
        Player player,
        GuiGraphics graphics,
        Font font,
        int screenWidth,
        int screenHeight,
        float fontScale,
        float cellW,
        float cellH,
        float gridOffsetX,
        float gridOffsetY,
        boolean noSignal
    ) {
        this.drone = drone;
        this.battery = battery;
        this.signal = signal;
        this.physics = physics;
        this.player = player;
        this.graphics = graphics;
        this.font = font;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.fontScale = fontScale;
        this.cellW = cellW;
        this.cellH = cellH;
        this.gridOffsetX = gridOffsetX;
        this.gridOffsetY = gridOffsetY;
        this.noSignal = noSignal;
    }
}
