package com.fpvdrone.gui.list;

import com.fpvdrone.client.ControllerConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.DroneAction;
import com.fpvdrone.client.SettingsLoader;
import com.fpvdrone.gui.entry.ActionBindingEntry;
import com.fpvdrone.gui.entry.CategoryEntry;
import com.fpvdrone.gui.entry.ChannelEntry;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/** Controller channel mapping: the stick axes, the 1.1.8 gimbal tilt axis, the arm switch and (1.1.8) every action's switch and key. */
public class ChannelMappingList extends FPVList implements ActionBindingEntry.Listener {
    private final ControllerConfig config;
    private ChannelEntry activeChannelEntry;
    private ActionBindingEntry activeActionEntry;

    public ChannelMappingList(Screen parentScreen, ControllerConfig config) {
        super(parentScreen);
        this.config = config;
        this.buildEntries();
    }

    private void buildEntries() {
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.channel.axes")));
        this.addChannel(I18n.get("fpvdrone.channel.throttle"), this.config::getThrottleChannel, this.config::setThrottleChannel,
            this.config::isInvertThrottle, this.config::setInvertThrottle);
        this.addChannel(I18n.get("fpvdrone.channel.yaw"), this.config::getYawChannel, this.config::setYawChannel,
            this.config::isInvertYaw, this.config::setInvertYaw);
        this.addChannel(I18n.get("fpvdrone.channel.pitch"), this.config::getPitchChannel, this.config::setPitchChannel,
            this.config::isInvertPitch, this.config::setInvertPitch);
        this.addChannel(I18n.get("fpvdrone.channel.roll"), this.config::getRollChannel, this.config::setRollChannel,
            this.config::isInvertRoll, this.config::setInvertRoll);
        this.addChannel(I18n.get("fpvdrone.channel.gimbal"), this.config::getGimbalChannel, this.config::setGimbalChannel,
            this.config::isInvertGimbal, this.config::setInvertGimbal);
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.channel.switches")));
        this.addChannel(I18n.get("fpvdrone.channel.arm"), this.config::getArmChannel, this.config::setArmChannel,
            this.config::isInvertArm, this.config::setInvertArm);
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.channel.actions")));

        for (DroneAction action : DroneAction.values()) {
            if (action != DroneAction.ARM) {
                this.addEntry(new ActionBindingEntry(this, action, this));
            }
        }
    }

    private void addChannel(String name, Supplier<Integer> getter, Consumer<Integer> setter, Supplier<Boolean> invertGetter, Consumer<Boolean> invertSetter) {
        ChannelEntry[] self = new ChannelEntry[1];
        ChannelEntry entry = new ChannelEntry(this, name, getter, ch -> {
            setter.accept(ch);
            this.save();
        }, invertGetter, inv -> {
            invertSetter.accept(inv);
            this.save();
        }, () -> this.listenForChannel(self[0]));
        self[0] = entry;
        this.addEntry(entry);
    }

    private void listenForChannel(ChannelEntry entry) {
        this.stopListening();
        this.activeChannelEntry = entry;
        ControllerReader.startInputListening(channel -> {
            entry.setChannel(channel);
            this.activeChannelEntry = null;
            playClickSound();
            this.save();
        });
    }

    @Override
    public void onListenController(ActionBindingEntry entry) {
        this.stopListening();
        this.activeActionEntry = entry;
        ControllerReader.startInputListeningFull(result -> {
            if (this.activeActionEntry == entry) {
                int channel = result.channel;
                if (result.isButton) {
                    channel -= ControllerReader.getInstance().getAxisCount();
                }

                entry.setController(channel, result.isButton);
                this.activeActionEntry = null;
                playClickSound();
                this.save();
            }
        });
    }

    @Override
    public void onListenKey(ActionBindingEntry entry) {
        this.stopListening();
        this.activeActionEntry = entry;
    }

    /** A key press while an action waits for its key binds it. Returns true when consumed. */
    public boolean onKeyPressed(KeyEvent event) {
        if (this.activeActionEntry != null && this.activeActionEntry.getListening() == ActionBindingEntry.Listening.KEY && !event.isEscape()) {
            this.activeActionEntry.setKey(InputConstants.getKey(event));
            this.activeActionEntry = null;
            playClickSound();
            this.save();
            return true;
        }

        return false;
    }

    public void stopListening() {
        if (this.activeChannelEntry != null) {
            this.activeChannelEntry.stopListening();
            this.activeChannelEntry = null;
        }

        if (this.activeActionEntry != null) {
            this.activeActionEntry.stopListening();
            this.activeActionEntry = null;
        }

        ControllerReader.stopAllListening();
    }

    private void save() {
        SettingsLoader.saveControllerConfig(this.config);
    }

    private static void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
