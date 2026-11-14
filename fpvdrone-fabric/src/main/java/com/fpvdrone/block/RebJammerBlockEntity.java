package com.fpvdrone.block;

import com.fpvdrone.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.1.8 RebJammerEntity (a block entity, despite its package there). Only the client cares: while loaded it adds a
 * jamming zone around the block; the server does nothing with it.
 */
public class RebJammerBlockEntity extends BlockEntity {
    /** Horizontal radius of the zone's ring, in blocks. */
    public static final double RING_RADIUS = 40.0;
    /** Half thickness of the ring (horizontal and, scaled by {@link #VERTICAL_SCALE}, vertical). */
    public static final double TUBE_RADIUS = 20.0;
    public static final double VERTICAL_SCALE = 1.0;
    public static final double Y_OFFSET = 0.0;

    public RebJammerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.REB_JAMMER_ENTITY.get(), pos, state);
    }

    // Forge's onLoad() has no vanilla counterpart; setLevel runs when the block entity joins a level (chunk load or placement).
    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            JammerZones.add(this.getBlockPos(), RING_RADIUS, TUBE_RADIUS, VERTICAL_SCALE, Y_OFFSET);
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (this.level != null && this.level.isClientSide()) {
            JammerZones.add(this.getBlockPos(), RING_RADIUS, TUBE_RADIUS, VERTICAL_SCALE, Y_OFFSET);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null && this.level.isClientSide()) {
            JammerZones.remove(this.getBlockPos());
        }
    }
}
