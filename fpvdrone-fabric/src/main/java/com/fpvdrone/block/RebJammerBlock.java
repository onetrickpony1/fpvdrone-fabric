package com.fpvdrone.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 EW (REB) jammer: a placeholder block (crafting-table look, no recipe) whose block entity registers a
 * jamming zone on the client. Drone video/control signal inside the zone is cut (see {@link JammerZones}).
 */
public class RebJammerBlock extends BaseEntityBlock {
    public RebJammerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RebJammerBlockEntity(pos, state);
    }
}
