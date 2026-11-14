package com.fpvdrone.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final RegistryObject<SoundEvent> FPV_CONNECT = register("fpv_connect");
    /** Rocket pod reload (three variants in sounds.json). */
    public static final RegistryObject<SoundEvent> ROCKET_LOAD = register("rocket_load");
    /** Rocket pod fitted to a drone. */
    public static final RegistryObject<SoundEvent> ROCKET_POD_INSTALL = register("rocket_pod_install");

    private static RegistryObject<SoundEvent> register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("fpvdrone", name);
        return new RegistryObject<>(Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id)));
    }

    public static void init() {
    }
}
