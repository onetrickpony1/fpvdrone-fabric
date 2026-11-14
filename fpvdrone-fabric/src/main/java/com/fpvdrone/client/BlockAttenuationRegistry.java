package com.fpvdrone.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockAttenuationRegistry {
    private static final Map<Block, Float> ATTENUATION_MAP = new HashMap<>();
    private static final float DEFAULT_SOLID = 6.0F;
    private static final float DEFAULT_AIR = 0.0F;

    private static void register(Block block, float attenuation) {
        ATTENUATION_MAP.put(block, attenuation);
    }

    public static float getAttenuation(BlockState state) {
        if (state.isAir()) {
            return 0.0F;
        } else {
            Block block = state.getBlock();
            if (ATTENUATION_MAP.containsKey(block)) {
                return ATTENUATION_MAP.get(block);
            } else if (state.is(BlockTags.LEAVES)) {
                return 1.0F;
            } else if (state.is(BlockTags.LOGS)) {
                return 4.0F;
            } else if (state.is(BlockTags.PLANKS)) {
                return 3.0F;
            } else if (state.is(BlockTags.WOOL)) {
                return 2.0F;
            } else if (state.is(BlockTags.STONE_BRICKS)) {
                return 8.0F;
            } else {
                return !state.isSolidRender() ? 1.0F : 6.0F;
            }
        }
    }

    static {
        register(Blocks.STONE, 8.0F);
        register(Blocks.DEEPSLATE, 10.0F);
        register(Blocks.GRANITE, 8.0F);
        register(Blocks.DIORITE, 8.0F);
        register(Blocks.ANDESITE, 8.0F);
        register(Blocks.COBBLESTONE, 7.0F);
        register(Blocks.STONE_BRICKS, 8.0F);
        register(Blocks.BRICKS, 8.0F);
        register(Blocks.OBSIDIAN, 15.0F);
        register(Blocks.BEDROCK, 20.0F);
        register(Blocks.IRON_BLOCK, 12.0F);
        register(Blocks.GOLD_BLOCK, 10.0F);
        register(Blocks.COPPER_BLOCK.weathering().unaffected(), 11.0F);
        register(Blocks.DIAMOND_BLOCK, 10.0F);
        register(Blocks.DIRT, 4.0F);
        register(Blocks.GRASS_BLOCK, 4.0F);
        register(Blocks.SAND, 3.0F);
        register(Blocks.GRAVEL, 3.0F);
        register(Blocks.CLAY, 5.0F);
        register(Blocks.OAK_LOG, 4.0F);
        register(Blocks.SPRUCE_LOG, 4.0F);
        register(Blocks.BIRCH_LOG, 4.0F);
        register(Blocks.JUNGLE_LOG, 4.0F);
        register(Blocks.ACACIA_LOG, 4.0F);
        register(Blocks.DARK_OAK_LOG, 4.0F);
        register(Blocks.MANGROVE_LOG, 4.0F);
        register(Blocks.CHERRY_LOG, 4.0F);
        register(Blocks.OAK_PLANKS, 3.0F);
        register(Blocks.SPRUCE_PLANKS, 3.0F);
        register(Blocks.BIRCH_PLANKS, 3.0F);
        register(Blocks.JUNGLE_PLANKS, 3.0F);
        register(Blocks.ACACIA_PLANKS, 3.0F);
        register(Blocks.DARK_OAK_PLANKS, 3.0F);
        register(Blocks.OAK_LEAVES, 1.0F);
        register(Blocks.SPRUCE_LEAVES, 1.0F);
        register(Blocks.BIRCH_LEAVES, 1.0F);
        register(Blocks.JUNGLE_LEAVES, 1.0F);
        register(Blocks.ACACIA_LEAVES, 1.0F);
        register(Blocks.DARK_OAK_LEAVES, 1.0F);
        register(Blocks.MANGROVE_LEAVES, 1.0F);
        register(Blocks.CHERRY_LEAVES, 1.0F);
        register(Blocks.AZALEA_LEAVES, 1.0F);
        register(Blocks.GLASS, 1.0F);
        register(Blocks.GLASS_PANE, 0.5F);
        register(Blocks.TINTED_GLASS, 2.0F);
        register(Blocks.WATER, 12.0F);
        register(Blocks.LAVA, 15.0F);
        register(Blocks.ICE, 6.0F);
        register(Blocks.PACKED_ICE, 8.0F);
        register(Blocks.BLUE_ICE, 10.0F);
        register(Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE), 2.0F);
        register(Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.WHITE), 7.0F);
        register(Blocks.TERRACOTTA, 6.0F);
        register(Blocks.AIR, 0.0F);
        register(Blocks.CAVE_AIR, 0.0F);
        register(Blocks.VOID_AIR, 0.0F);
        register(Blocks.SHORT_GRASS, 0.0F);
        register(Blocks.TALL_GRASS, 0.0F);
        register(Blocks.FERN, 0.0F);
        register(Blocks.DEAD_BUSH, 0.0F);
    }
}
