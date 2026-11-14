package com.fpvdrone.registry;

import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.PilotBodyEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    static {
        com.fpvdrone.entity.FpvDataSerializers.init();
    }

    public static final RegistryObject<EntityType<PilotBodyEntity>> PILOT_BODY = register(
        "pilot_body", EntityType.Builder.of(PilotBodyEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(64)
    );
    public static final RegistryObject<EntityType<DroneEntity>> DRONE = register(
        "drone", EntityType.Builder.of(DroneEntity::new, MobCategory.MISC).sized(0.85F, 0.55F).clientTrackingRange(64).updateInterval(1)
    );
    // 1.1.8 dropped bomb
    public static final RegistryObject<EntityType<com.fpvdrone.entity.DroppedBombEntity>> DROPPED_BOMB = register(
        "dropped_bomb",
        EntityType.Builder.of(com.fpvdrone.entity.DroppedBombEntity::new, MobCategory.MISC).sized(0.35F, 0.35F).clientTrackingRange(64).updateInterval(1).fireImmune()
    );

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("fpvdrone", name));
        return new RegistryObject<>(Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key)));
    }

    public static void init() {
    }
}
