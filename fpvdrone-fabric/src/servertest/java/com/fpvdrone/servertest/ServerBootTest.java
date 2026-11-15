package com.fpvdrone.servertest;

import com.fpvdrone.registry.ModEntities;
import com.fpvdrone.registry.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Runs on a dedicated server (tools/run-server-check.sh): the drone, Lytui and ramp items are used, FP-1/Lancet/Orlan
 * are spawned, all tick for 3 seconds (spaced so they don't crash into each other), then a dropped bomb explodes.
 * Common code that reaches client-only classes crashes here.
 */
public class ServerBootTest {
    /** 1.1.8 goggles were a leather helmet: 1 armour point, 55 durability. */
    @GameTest
    public void gogglesAreLeatherHelmet(GameTestHelper helper) {
        ItemStack goggles = new ItemStack(ModItems.FPV_GOGGLES.get());
        double[] armor = {0.0};
        goggles.forEachModifier(net.minecraft.world.entity.EquipmentSlot.HEAD, (attr, mod) -> {
            if (attr.is(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)) armor[0] += mod.amount();
        });
        System.out.println("[server] goggles: armour " + armor[0] + ", durability " + goggles.getMaxDamage());
        if (armor[0] != 1.0 || goggles.getMaxDamage() != 55) helper.fail("Goggles armour " + armor[0] + " durability " + goggles.getMaxDamage());
        helper.succeed();
    }

    @GameTest(maxTicks = 300)
    public void dronesTickOnDedicatedServer(GameTestHelper helper) {
        // The test area sits at random coordinates; force-load the chunks around it so every entity ticks.
        BlockPos corner = helper.absolutePos(BlockPos.ZERO);
        for (int cx = (corner.getX() >> 4) - 1; cx <= ((corner.getX() + 16) >> 4) + 1; cx++) {
            for (int cz = (corner.getZ() >> 4) - 1; cz <= ((corner.getZ() + 16) >> 4) + 1; cz++) {
                helper.getLevel().setChunkForced(cx, cz, true);
            }
        }
        Player player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = true;
        Item[] items = {
            ModItems.DRONE.get(), ModItems.THERMAL_DRONE.get(), ModItems.MAVIC_DRONE.get(), ModItems.LYTUI_DRONE.get(),
            ModItems.RAMP.get()
        };
        int used = 0;
        for (int i = 0; i < items.length; i++) {
            BlockPos floor = new BlockPos(1 + (i % 3) * 4, 0, 1 + (i / 3) * 4);
            helper.setBlock(floor, Blocks.STONE);
            BlockPos abs = helper.absolutePos(floor);
            player.snapTo(abs.getX() + 0.5, abs.getY() + 1, abs.getZ() - 1.5, 0.0F, 30.0F);
            ItemStack stack = new ItemStack(items[i]);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            var hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
            var result = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            System.out.println("[server] " + items[i] + " -> " + result);
            if (result.consumesAction()) used++;
        }
        final int placed = used;
        // FP-1, Lancet and Orlan items go onto the ramp (1.1.8); spawn those munitions directly.
        String[] kinds = {"fp1", "lancet51", "orlan10"};
        for (int i = 0; i < kinds.length; i++) {
            var m = helper.spawn(ModEntities.LOITERING_MUNITION.get(), new BlockPos(1 + ((i + 2) % 3) * 4, 1, 5 + ((i + 2) / 3) * 4));
            m.v5suI(kinds[i], 90.0F, (java.util.UUID) null);
        }
        java.util.List<Entity> seen = new java.util.ArrayList<>();
        helper.runAfterDelay(2, () -> {
            for (Entity e : helper.getLevel().getAllEntities()) {
                if (e.getType().builtInRegistryHolder().key().identifier().getNamespace().equals("fpvdrone")) seen.add(e);
            }
            System.out.println("[server] tick 2: " + seen.size() + " fpvdrone entities");
        });
        helper.runAfterDelay(60, () -> {
            long alive = seen.stream().filter(e -> !e.isRemoved()).count();
            var idle = seen.stream().filter(e -> e.tickCount < 50).map(e -> e.getType().toShortString() + "@" + e.blockPosition().toShortString() + " age " + e.tickCount).toList();
            if (!idle.isEmpty()) helper.fail("Some entities never ticked: " + idle);
            System.out.println("[server] tick 60: " + alive + "/" + seen.size() + " still in the world");
            if (seen.size() < items.length + 3 || alive != seen.size()) helper.fail("Entities lost before the bomb: " + alive + "/" + seen.size());
            // Then a dropped bomb: its explosion runs the shrapnel/damage code on the dedicated server.
            seen.add(helper.spawn(ModEntities.DROPPED_BOMB.get(), new BlockPos(5, 6, 5)));
        });
        helper.runAfterDelay(160, () -> {
            for (Entity e : seen) {
                System.out.println("[server]   " + e.getType().toShortString() + " removed=" + e.getRemovalReason() + " at " + e.blockPosition().toShortString());
            }
            var level = helper.getLevel();
            int ours = 0;
            for (Entity e : level.getAllEntities()) {
                if (e.getType().builtInRegistryHolder().key().identifier().getNamespace().equals("fpvdrone")) ours++;
            }
            System.out.println("[server] items used " + placed + "/" + items.length + ", fpvdrone entities ticking: " + ours);
            if (placed != items.length) helper.fail("Items placed: " + placed + "/" + items.length);
            helper.succeed();
        });
    }
}
