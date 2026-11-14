package com.fpvdrone.gui.list;

import com.fpvdrone.client.ControllerConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.SettingsLoader;
import com.fpvdrone.gui.entry.CategoryEntry;
import com.fpvdrone.gui.entry.ChannelEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ChannelMappingList extends FPVList {
    private final ControllerConfig config;
    private ChannelEntry activeChannelEntry;

    public ChannelMappingList(Screen parentScreen, ControllerConfig config) {
        super(parentScreen);
        this.config = config;
        this.buildEntries();
    }

    private void buildEntries() {
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.channel.axes", new Object[0])));
        this.addEntry(new ChannelEntry(this, I18n.get("fpvdrone.channel.throttle", new Object[0]), this.config::getThrottleChannel, ch -> {
            this.config.setThrottleChannel(ch);
            this.save();
        }, this.config::isInvertThrottle, inv -> {
            this.config.setInvertThrottle(inv);
            this.save();
        }, () -> this.startListening(0)));
        this.addEntry(new ChannelEntry(this, I18n.get("fpvdrone.channel.yaw", new Object[0]), this.config::getYawChannel, ch -> {
            this.config.setYawChannel(ch);
            this.save();
        }, this.config::isInvertYaw, inv -> {
            this.config.setInvertYaw(inv);
            this.save();
        }, () -> this.startListening(1)));
        this.addEntry(new ChannelEntry(this, I18n.get("fpvdrone.channel.pitch", new Object[0]), this.config::getPitchChannel, ch -> {
            this.config.setPitchChannel(ch);
            this.save();
        }, this.config::isInvertPitch, inv -> {
            this.config.setInvertPitch(inv);
            this.save();
        }, () -> this.startListening(2)));
        this.addEntry(new ChannelEntry(this, I18n.get("fpvdrone.channel.roll", new Object[0]), this.config::getRollChannel, ch -> {
            this.config.setRollChannel(ch);
            this.save();
        }, this.config::isInvertRoll, inv -> {
            this.config.setInvertRoll(inv);
            this.save();
        }, () -> this.startListening(3)));
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.channel.switches", new Object[0])));
        this.addEntry(new ChannelEntry(this, I18n.get("fpvdrone.channel.arm", new Object[0]), this.config::getArmChannel, ch -> {
            this.config.setArmChannel(ch);
            this.save();
        }, this.config::isInvertArm, inv -> {
            this.config.setInvertArm(inv);
            this.save();
        }, () -> this.startListening(4)));
    }

    private void startListening(int entryIndex) {
        if (this.activeChannelEntry != null) {
            this.activeChannelEntry.stopListening();
        }

        this.activeChannelEntry = (ChannelEntry)this.children().get(entryIndex + 1);
        ControllerReader.getInstance();
        ControllerReader.startInputListening(channel -> {
            this.activeChannelEntry.setChannel(channel);
            this.activeChannelEntry = null;
            this.save();
        });
    }

    private void save() {
        SettingsLoader.saveControllerConfig(this.config);
    }
}
