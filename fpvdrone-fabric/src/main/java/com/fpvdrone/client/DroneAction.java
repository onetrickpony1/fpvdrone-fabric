package com.fpvdrone.client;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.minecraft.client.Minecraft;

/**
 * 1.1.8: an action that can be bound to a key and/or a controller switch or button
 * (the original's data/Enum_K8rYZiiY). Each one keeps its controller channel in {@link ControllerConfig}
 * and its key in {@link KeyManager}; the handler runs when either is pressed.
 */
public enum DroneAction {
    ARM("arm", "fpvdrone.bind.arm",
        ControllerConfig::getArmChannel, ControllerConfig::setArmChannel, ControllerConfig::isArmIsButton, ControllerConfig::setArmIsButton,
        KeyManager::handleMotorToggle),
    EXIT("exit", "fpvdrone.bind.exit",
        ControllerConfig::getExitChannel, ControllerConfig::setExitChannel, ControllerConfig::isExitIsButton, ControllerConfig::setExitIsButton,
        KeyManager::handleExitView),
    BOMB("bomb", "fpvdrone.bind.bomb",
        ControllerConfig::getBombChannel, ControllerConfig::setBombChannel, ControllerConfig::isBombIsButton, ControllerConfig::setBombIsButton,
        KeyManager::handleDropBomb),
    ZOOM("zoom", "fpvdrone.bind.zoom",
        ControllerConfig::getZoomChannel, ControllerConfig::setZoomChannel, ControllerConfig::isZoomIsButton, ControllerConfig::setZoomIsButton,
        KeyManager::handleZoom),
    RTH("rth", "fpvdrone.bind.rth",
        ControllerConfig::getRthChannel, ControllerConfig::setRthChannel, ControllerConfig::isRthIsButton, ControllerConfig::setRthIsButton,
        KeyManager::handleReturnToHome),
    HOVER("hover", "fpvdrone.bind.hover",
        ControllerConfig::getHoverChannel, ControllerConfig::setHoverChannel, ControllerConfig::isHoverIsButton, ControllerConfig::setHoverIsButton,
        KeyManager::handleHoverHold),
    FIRE_ROCKET("fire_rocket", "fpvdrone.bind.fire_rocket",
        ControllerConfig::getFireRocketChannel, ControllerConfig::setFireRocketChannel, ControllerConfig::isFireRocketIsButton,
        ControllerConfig::setFireRocketIsButton, KeyManager::handleFireRocket),
    FOCUS("focus", "fpvdrone.bind.focus",
        ControllerConfig::getFocusChannel, ControllerConfig::setFocusChannel, ControllerConfig::isFocusIsButton, ControllerConfig::setFocusIsButton,
        KeyManager::handleFocus),
    INTERACT("interact", "fpvdrone.bind.interact",
        ControllerConfig::getInteractChannel, ControllerConfig::setInteractChannel, ControllerConfig::isInteractIsButton,
        ControllerConfig::setInteractIsButton, KeyManager::handleInteract);

    private final String id;
    private final String langKey;
    private final ToIntFunction<ControllerConfig> channelGetter;
    private final ObjIntConsumer<ControllerConfig> channelSetter;
    private final Predicate<ControllerConfig> buttonGetter;
    private final BiConsumer<ControllerConfig, Boolean> buttonSetter;
    private final Consumer<Minecraft> handler;

    DroneAction(
        String id,
        String langKey,
        ToIntFunction<ControllerConfig> channelGetter,
        ObjIntConsumer<ControllerConfig> channelSetter,
        Predicate<ControllerConfig> buttonGetter,
        BiConsumer<ControllerConfig, Boolean> buttonSetter,
        Consumer<Minecraft> handler
    ) {
        this.id = id;
        this.langKey = langKey;
        this.channelGetter = channelGetter;
        this.channelSetter = channelSetter;
        this.buttonGetter = buttonGetter;
        this.buttonSetter = buttonSetter;
        this.handler = handler;
    }

    /** Key in controller.json's "bindings" object. */
    public String id() {
        return this.id;
    }

    public String langKey() {
        return this.langKey;
    }

    public int getChannel(ControllerConfig config) {
        return this.channelGetter.applyAsInt(config);
    }

    public void setChannel(ControllerConfig config, int channel) {
        this.channelSetter.accept(config, channel);
    }

    public boolean isButton(ControllerConfig config) {
        return this.buttonGetter.test(config);
    }

    public void setButton(ControllerConfig config, boolean isButton) {
        this.buttonSetter.accept(config, isButton);
    }

    public void run(Minecraft mc) {
        this.handler.accept(mc);
    }
}
