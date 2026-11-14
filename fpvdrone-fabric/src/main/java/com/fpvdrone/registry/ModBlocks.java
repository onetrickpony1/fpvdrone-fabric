package com.fpvdrone.registry;

import com.fpvdrone.block.RebJammerBlock;
import com.fpvdrone.block.RebJammerBlockEntity;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** 1.1.8 blocks: the EW jammer and its block entity. */
public class ModBlocks {
    public static final RegistryObject<Block> REB_JAMMER = register(
        "reb_jammer", RebJammerBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
    );
    public static final RegistryObject<BlockEntityType<RebJammerBlockEntity>> REB_JAMMER_ENTITY = new RegistryObject<>(Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        Identifier.fromNamespaceAndPath("fpvdrone", "reb_jammer"),
        new BlockEntityType<>(RebJammerBlockEntity::new, Set.of(REB_JAMMER.get()))
    ));

    private static RegistryObject<Block> register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("fpvdrone", name));
        return new RegistryObject<>(Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(props.setId(key))));
    }

    public static void init() {
    }
}
