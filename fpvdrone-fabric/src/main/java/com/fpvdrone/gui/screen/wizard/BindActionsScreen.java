package com.fpvdrone.gui.screen.wizard;

import com.fpvdrone.client.ControllerConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.DroneAction;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.client.SettingsLoader;
import com.fpvdrone.gui.addon.BackProceedFooter;
import com.fpvdrone.gui.addon.WizardHeader;
import com.fpvdrone.gui.widget.StickOverlayRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * 1.1.8 wizard step (the original's sxez6mi/LyXDtNEAkn): bind one action to a key and/or a controller switch,
 * then move on to the next action in the list. For Arm it also shows the switch state and an "It's Backwards" button,
 * which replaces 1.1.4's separate arm-switch step.
 */
public class BindActionsScreen extends EmptyListScreen {
    private static final int SLOTS_Y = 142;
    private final List<DroneAction> actions;
    private final DroneAction action;
    private final Function<Screen, Screen> next;
    private Button backwardsButton;
    private final long startTime;
    private long detectedTime = 0L;

    /** Every action, in the order 1.1.8's wizard asks for them. */
    public static List<DroneAction> allActions() {
        return List.of(DroneAction.values());
    }

    public BindActionsScreen(Screen previousScreen, List<DroneAction> actions, Function<Screen, Screen> next) {
        super(previousScreen, new WizardHeader(Component.translatable("fpvdrone.bind.title"), true), new BackProceedFooter());
        this.actions = actions;
        this.action = actions.get(0);
        this.next = next;
        BackProceedFooter footer = (BackProceedFooter)this.footer;
        footer.completeConstructor(this::onProceed, this::getProceedLabel, () -> true);
        footer.overrideOnBack(this::onBack);
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        this.backwardsButton = Button.builder(Component.translatable("fpvdrone.wizard.calibrateArm.backwards"), btn -> this.toggleArmInvert())
            .pos(this.width / 2 - 60, SLOTS_Y + 40)
            .size(120, 20)
            .build();
        this.backwardsButton.visible = false;
        this.addRenderableWidget(this.backwardsButton);
        this.startListening();
    }

    public void tick() {
        super.tick();
        ControllerReader.getInstance().poll();
        ControllerReader.tickListeners();
    }

    private void startListening() {
        ControllerReader.startInputListeningFull(result -> {
            int channel = result.channel;
            boolean isButton = result.isButton;
            if (isButton) {
                channel -= ControllerReader.getInstance().getAxisCount();
            }

            ControllerConfig config = ControllerReader.getConfig();
            this.action.setChannel(config, channel);
            this.action.setButton(config, isButton);
            if (this.action == DroneAction.ARM) {
                config.setInvertArm(false);
            }

            SettingsLoader.saveControllerConfig(config);
            this.detectedTime = System.currentTimeMillis();
            playClickSound();
            this.startListening();
        });
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            return super.keyPressed(event);
        }

        KeyMapping mapping = KeyManager.getKey(this.action);
        if (mapping != null) {
            KeyManager.bindKey(mapping, InputConstants.getKey(event));
        }

        this.detectedTime = System.currentTimeMillis();
        playClickSound();
        return true;
    }

    private void toggleArmInvert() {
        ControllerConfig config = ControllerReader.getConfig();
        config.setInvertArm(!config.isInvertArm());
        SettingsLoader.saveControllerConfig(config);
    }

    private void onProceed() {
        ControllerReader.stopAllListening();
        if (this.actions.size() > 1) {
            Minecraft.getInstance().gui.setScreen(new BindActionsScreen(this, this.actions.subList(1, this.actions.size()), this.next));
        } else {
            Minecraft.getInstance().gui.setScreen(this.next.apply(this));
        }
    }

    private void onBack() {
        ControllerReader.stopAllListening();
        this.onClose();
    }

    private String getProceedLabel() {
        return this.actions.size() > 1 ? I18n.get("fpvdrone.bind.next") : I18n.get("fpvdrone.bind.done");
    }

    private boolean showArmState() {
        return this.action == DroneAction.ARM && this.action.getChannel(ControllerReader.getConfig()) >= 0;
    }

    @Override
    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderCustom(graphics, mouseX, mouseY, partialTicks);
        int titleY = 53;
        graphics.centeredText(this.font, I18n.get(this.action.langKey()), this.width / 2, titleY, -1);
        graphics.centeredText(this.font, I18n.get("fpvdrone.bind.prompt"), this.width / 2, titleY + 15, -1);
        graphics.centeredText(this.font, I18n.get("fpvdrone.bind.keySlot", this.keyLabel()), this.width / 2, SLOTS_Y - 18, 0xFFCCCCCC);
        graphics.centeredText(this.font, I18n.get("fpvdrone.bind.controllerSlot", this.controllerLabel()), this.width / 2, SLOTS_Y - 4, 0xFFCCCCCC);
        if (this.detectedTime > 0L && System.currentTimeMillis() - this.detectedTime < 500L) {
            StickOverlayRenderer.renderDetected(graphics, this.width / 2 - 25, SLOTS_Y + 14);
        } else {
            StickOverlayRenderer.renderListeningDots(graphics, this.width / 2 - 30, SLOTS_Y + 14, this.startTime);
        }

        boolean armState = this.showArmState();
        this.backwardsButton.visible = armState;
        this.backwardsButton.setY(SLOTS_Y + 40);
        if (armState) {
            String state = ControllerReader.getInstance().readArmState()
                ? I18n.get("fpvdrone.wizard.calibrateArm.on")
                : I18n.get("fpvdrone.wizard.calibrateArm.off");
            graphics.centeredText(this.font, state, this.width / 2, SLOTS_Y + 26, -1);
        }
    }

    private String keyLabel() {
        return KeyManager.keyName(KeyManager.getKey(this.action));
    }

    private String controllerLabel() {
        return controllerLabel(this.action);
    }

    /** "Button 3" / "Axis 5" / "None" for an action's controller binding. */
    public static String controllerLabel(DroneAction action) {
        ControllerConfig config = ControllerReader.getConfig();
        int channel = action.getChannel(config);
        return channel >= 0 ? (action.isButton(config) ? "Button " : "Axis ") + (channel + 1) : I18n.get("fpvdrone.bind.none");
    }

    static void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
