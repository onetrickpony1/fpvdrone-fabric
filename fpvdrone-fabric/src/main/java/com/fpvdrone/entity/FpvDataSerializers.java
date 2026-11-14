package com.fpvdrone.entity;

import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

/** Synced-data serializers that 1.20 had built in but 26.x dropped. */
public final class FpvDataSerializers {
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, CompoundTag> codec() {
            return ByteBufCodecs.TRUSTED_COMPOUND_TAG;
        }

        @Override
        public CompoundTag copy(CompoundTag value) {
            return value.copy();
        }
    };
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(
        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)
    );

    static {
        FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath("fpvdrone", "compound_tag"), COMPOUND_TAG);
        FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath("fpvdrone", "optional_uuid"), OPTIONAL_UUID);
    }

    private FpvDataSerializers() {
    }

    public static void init() {
    }
}
