package com.fpvdrone.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ShrapnelClip {
    private ShrapnelClip() {
    }

    public static ShrapnelClip.Result trace(Level level, Vec3 from, Vec3 to) {
        boolean penetratedWood = false;
        Vec3 current = from;
        Vec3 target = to;

        for (int step = 0; step < 64; step++) {
            BlockHitResult hit = level.clip(new ClipContext(current, target, Block.COLLIDER, Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
            if (hit.getType() == Type.MISS) {
                return new ShrapnelClip.Result(target, penetratedWood ? 0.25 : 1.0, null);
            }

            BlockPos hitPos = hit.getBlockPos();
            BlockState state = level.getBlockState(hitPos);
            if (isDoor(state)) {
                current = advancePast(hit.getLocation(), from, to);
            } else {
                if (penetratedWood || !isWood(state)) {
                    return new ShrapnelClip.Result(hit.getLocation(), 0.0, hit);
                }

                penetratedWood = true;
                current = advancePast(hit.getLocation(), from, to);
            }
        }

        return new ShrapnelClip.Result(target, penetratedWood ? 0.25 : 1.0, null);
    }

    public static boolean checkBlastLos(Level level, Vec3 from, Vec3 to) {
        Vec3 current = from;

        for (int step = 0; step < 64; step++) {
            BlockHitResult hit = level.clip(new ClipContext(current, to, Block.COLLIDER, Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
            if (hit.getType() == Type.MISS) {
                return true;
            }

            BlockPos hitPos = hit.getBlockPos();
            BlockState state = level.getBlockState(hitPos);
            if (!isDoor(state)) {
                return false;
            }

            current = advancePast(hit.getLocation(), from, to);
        }

        return true;
    }

    private static boolean isDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock || state.getBlock() instanceof FenceGateBlock;
    }

    private static boolean isWood(BlockState state) {
        return state.is(BlockTags.PLANKS)
            || state.is(BlockTags.LOGS)
            || state.is(BlockTags.WOODEN_FENCES)
            || state.is(BlockTags.WOODEN_SLABS)
            || state.is(BlockTags.WOODEN_STAIRS)
            || state.is(BlockTags.WOODEN_BUTTONS)
            || state.is(BlockTags.WOODEN_PRESSURE_PLATES);
    }

    private static Vec3 advancePast(Vec3 hitLocation, Vec3 origin, Vec3 target) {
        Vec3 dir = target.subtract(origin).normalize();
        return hitLocation.add(dir.scale(0.01));
    }

    /** {@code blockHit} is the block that stopped the fragment (1.1.8: used to place ground-debris decals). */
    public record Result(Vec3 end, double damageMultiplier, @org.jspecify.annotations.Nullable BlockHitResult blockHit) {
    }
}
