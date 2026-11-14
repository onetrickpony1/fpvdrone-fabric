package com.fpvdrone.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final RegistryObject<SoundEvent> FPV_CONNECT = register("fpv_connect");

    private static RegistryObject<SoundEvent> register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("fpvdrone", name);
        return new RegistryObject<>(Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id)));
    }

    public static void init() {
    }
}
