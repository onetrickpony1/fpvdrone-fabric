package com.fpvdrone.item;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 1.1.8 launch ramp: placed on top of a block as an entity (so it can sit at the player's heading and carry a drone). */
public class RampItem extends UsageTooltipItem {
    public RampItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        } else if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        } else {
            if (!level.isClientSide()) {
                Vec3 at = context.getClickLocation();
                float yaw = player.getYRot();
                if (!spawnRamp(level, at, yaw)) {
                    return InteractionResult.PASS;
                }

                if (!player.getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }

            return InteractionResult.SUCCESS;
        }
    }

    /** 1.1.8: a ramp facing the way the player looks, on the clicked top face. */
    private static boolean spawnRamp(Level level, Vec3 at, float yaw) {
        com.fpvdrone.entity.RampEntity ramp = com.fpvdrone.registry.ModEntities.RAMP.get().create(level, net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE);
        if (ramp == null) {
            return false;
        }

        ramp.snapTo(at.x, at.y, at.z, yaw, 0.0F);
        ramp.setYRot(yaw);
        ramp.yRotO = yaw;
        level.addFreshEntity(ramp);
        return true;
    }
}
