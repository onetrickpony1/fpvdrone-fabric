package com.fpvdrone.data;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/** What a fragment hit, judged by the block's sound type; picks the ground-debris (bullet hole) decal set. */
public enum SurfaceMaterial {
    STONE,
    METAL,
    WOOD,
    DIRT,
    CLAY,
    GLASS,
    OTHER;

    public static SurfaceMaterial of(BlockState state) {
        SoundType s = state.getSoundType();
        if (s == SoundType.GLASS) {
            return GLASS;
        } else if (s == SoundType.METAL
            || s == SoundType.COPPER
            || s == SoundType.NETHERITE_BLOCK
            || s == SoundType.ANVIL
            || s == SoundType.CHAIN
            || s == SoundType.LANTERN) {
            return METAL;
        } else if (s == SoundType.WOOD
            || s == SoundType.BAMBOO
            || s == SoundType.BAMBOO_WOOD
            || s == SoundType.LADDER
            || s == SoundType.SCAFFOLDING
            || s == SoundType.NETHER_WOOD
            || s == SoundType.CHERRY_WOOD) {
            return WOOD;
        } else if (s == SoundType.MUD || s == SoundType.MUD_BRICKS || s == SoundType.PACKED_MUD) {
            return CLAY;
        } else if (s == SoundType.GRAVEL
            || s == SoundType.SAND
            || s == SoundType.SNOW
            || s == SoundType.POWDER_SNOW
            || s == SoundType.GRASS
            || s == SoundType.WET_GRASS
            || s == SoundType.ROOTED_DIRT
            || s == SoundType.CROP
            || s == SoundType.NYLIUM
            || s == SoundType.SOUL_SAND
            || s == SoundType.SOUL_SOIL) {
            return DIRT;
        } else if (s == SoundType.STONE
            || s == SoundType.DEEPSLATE
            || s == SoundType.DEEPSLATE_BRICKS
            || s == SoundType.DEEPSLATE_TILES
            || s == SoundType.POLISHED_DEEPSLATE
            || s == SoundType.CALCITE
            || s == SoundType.TUFF
            || s == SoundType.NETHERRACK
            || s == SoundType.NETHER_BRICKS
            || s == SoundType.BASALT
            || s == SoundType.BONE_BLOCK) {
            return STONE;
        } else {
            return OTHER;
        }
    }
}
