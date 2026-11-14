package com.fpvdrone.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final RegistryObject<SoundEvent> FPV_CONNECT = register("fpv_connect");
    /** 1.1.8: a rocket slid into a rocket pod. */
    public static final RegistryObject<SoundEvent> ROCKET_LOAD = register("rocket_load");
    /** 1.1.8: a rocket pod mount fitted to an aircraft. */
    public static final RegistryObject<SoundEvent> ROCKET_POD_INSTALL = register("rocket_pod_install");

    private static RegistryObject<SoundEvent> register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("fpvdrone", name);
        return new RegistryObject<>(Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id)));
    }

    public static void init() {
    }
}
