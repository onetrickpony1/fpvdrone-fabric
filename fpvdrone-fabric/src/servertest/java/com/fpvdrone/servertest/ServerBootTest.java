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

    /**
     * Port addition (docs/WIND.md): the drone wind field. Base wind is non-zero and the same for the same tick, the
     * height profile is about 1 at 6 m and about half at 0.5 m, a wall upwind shelters, wind off gives zero. With
     * Weathery installed (run-server-check.sh -PwithWeathery) the base wind must come from Weathery.
     */
    @GameTest(maxTicks = 40)
    public void windField(GameTestHelper helper) {
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("weathery")) {
            var server = helper.getLevel().getServer();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "weathery wind_event clear");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "weathery wind_speed 0.5");
        }
        helper.runAfterDelay(10, () -> this.measureWind(helper));
    }

    private void measureWind(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos abs = helper.absolutePos(new BlockPos(1, 1, 1));
        int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, abs.getX(), abs.getZ());
        double x = abs.getX() + 0.5;
        double z = abs.getZ() + 0.5;
        boolean wasOn = com.fpvdrone.FpvConfig.isWindEnabled();
        float wasStrength = com.fpvdrone.FpvConfig.getWindStrength();
        com.fpvdrone.FpvConfig.setWind(true, 1.0F);
        org.joml.Vector3f a = com.fpvdrone.physics.wind.FpvWind.base(level, x, top + 6, z, new org.joml.Vector3f());
        org.joml.Vector3f b = com.fpvdrone.physics.wind.FpvWind.base(level, x, top + 6, z, new org.joml.Vector3f());
        float at6 = com.fpvdrone.physics.wind.FpvWind.profile(level, x, top + 6, z);
        float low = com.fpvdrone.physics.wind.FpvWind.profile(level, x, top + 0.5, z);
        float open = com.fpvdrone.physics.wind.FpvWind.shelter(level, x, top + 1, z, a);
        // a stone wall 6 high, 4 blocks upwind, 7 wide across the wind
        float len = Math.max(1.0E-3F, (float)Math.sqrt(a.x * a.x + a.z * a.z));
        float ux = -a.x / len;
        float uz = -a.z / len;
        java.util.List<BlockPos> wall = new java.util.ArrayList<>();
        for (int w = -3; w <= 3; w++) {
            for (int h = 0; h < 6; h++) {
                BlockPos p = BlockPos.containing(x + ux * 4 - uz * w, top + h, z + uz * 4 + ux * w);
                level.setBlockAndUpdate(p, Blocks.STONE.defaultBlockState());
                wall.add(p);
            }
        }
        float behind = com.fpvdrone.physics.wind.FpvWind.shelter(level, x, top + 1, z, a);
        for (BlockPos p : wall) {
            level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
        }
        com.fpvdrone.FpvConfig.setWind(false, 1.0F);
        float off = com.fpvdrone.physics.wind.FpvWind.base(level, x, top + 6, z, new org.joml.Vector3f()).length();
        com.fpvdrone.FpvConfig.setWind(wasOn, wasStrength);
        boolean weatheryLoaded = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("weathery");
        String seen = String.format(java.util.Locale.ROOT,
            "wind: base %.2f m/s (same next call %s), source %s (weathery loaded %s), profile 6 m %.2f / 0.5 m %.2f, shelter open %.2f / behind a wall %.2f, off %.2f",
            a.length(), a.equals(b), com.fpvdrone.physics.wind.FpvWind.fromWeathery() ? "Weathery" : "built-in", weatheryLoaded, at6, low, open, behind, off);
        System.out.println("[server] " + seen);
        boolean ok = a.equals(b) && Math.abs(at6 - 1) < 0.05F && low > 0.3F && low < 0.65F && open < 0.05F && behind > 0.8F && off == 0
            && weatheryLoaded == com.fpvdrone.physics.wind.FpvWind.fromWeathery() && (weatheryLoaded || a.length() > 0.3F);
        if (!ok) {
            helper.fail(seen);
        }
        helper.succeed();
    }

    /**
     * Wind step 2 (docs/WIND.md): Dryden turbulence has the spec's spread (sigma_u at 20 m in a 5 m/s wind), gusts
     * follow their schedule (some of the time, at most 0.6 x the mean, the same value for the same tick, none when
     * switched off).
     */
    @GameTest
    public void turbulenceAndGusts(GameTestHelper helper) {
        var t = new com.fpvdrone.physics.wind.Turbulence();
        org.joml.Vector3f mean = new org.joml.Vector3f(5, 0, 0);
        org.joml.Vector3f out = new org.joml.Vector3f();
        double sum = 0;
        double sumSq = 0;
        double sumW = 0;
        int n = 40000;
        for (int i = 0; i < n; i++) {
            t.step(0.05F, 20.0F, mean, 5.0F, 5.0F, 0.0F, 1.0F, out);
            sum += out.x;
            sumSq += out.x * out.x;
            sumW += out.y * out.y;
        }
        double sdU = Math.sqrt(sumSq / n - (sum / n) * (sum / n));
        double sdW = Math.sqrt(sumW / n);
        double k = 0.177 + 0.0027 * 20;
        double expectU = 0.5 / Math.pow(k, 0.4);
        boolean wasGusts = com.fpvdrone.FpvConfig.isWindGusts();
        float wasTurb = com.fpvdrone.FpvConfig.getWindTurbulence();
        com.fpvdrone.FpvConfig.setWindTurbulence(wasTurb, true);
        int gusting = 0;
        float peak = 0;
        boolean repeatable = true;
        for (int tick = 0; tick < 24000; tick++) {
            float g = com.fpvdrone.physics.wind.FpvWind.gust(tick, 5.0F);
            if (g > 0.01F) gusting++;
            peak = Math.max(peak, g);
            if (g != com.fpvdrone.physics.wind.FpvWind.gust(tick, 5.0F)) repeatable = false;
        }
        com.fpvdrone.FpvConfig.setWindTurbulence(wasTurb, false);
        float off = 0;
        for (int tick = 0; tick < 2000; tick++) off = Math.max(off, com.fpvdrone.physics.wind.FpvWind.gust(tick, 5.0F));
        com.fpvdrone.FpvConfig.setWindTurbulence(wasTurb, wasGusts);
        String seen = String.format(java.util.Locale.ROOT,
            "turbulence at 20 m in 5 m/s: sigma_u %.3f (spec %.3f), sigma_w %.3f (spec 0.500); gusts %.0f%% of the time, peak %.2f m/s (max 3.00), repeatable %s, off %.2f",
            sdU, expectU, sdW, 100.0 * gusting / 24000, peak, repeatable, off);
        System.out.println("[server] " + seen);
        if (Math.abs(sdU / expectU - 1) > 0.2 || Math.abs(sdW / 0.5 - 1) > 0.2 || gusting < 1000 || gusting > 16000 || peak > 3.01F || peak < 1.0F || !repeatable || off != 0) {
            helper.fail(seen);
        }
        helper.succeed();
    }

    /** Wind step 3: a dropped RGD-5 (310 g) falling 60 blocks drifts downwind in a 10 m/s wind, not in still air. */
    @GameTest
    public void bombDriftsInWind(GameTestHelper helper) {
        var drop = new com.fpvdrone.data.ProjectileDefinition.Drop(0.31F, 0.47F, 0.0026F, 0.2F, 0.0F, 0.0F);
        double[] drift = new double[2];
        int[] ticks = new int[2];
        for (int run = 0; run < 2; run++) {
            Vec3 wind = run == 0 ? Vec3.ZERO : new Vec3(0.5, 0, 0);
            Vec3 v = Vec3.ZERO;
            Vec3 p = Vec3.ZERO;
            while (p.y > -60 && ticks[run] < 400) {
                v = com.fpvdrone.entity.DroppedBombEntity.fallStep(v, drop, wind);
                p = p.add(v);
                ticks[run]++;
            }
            drift[run] = p.x;
        }
        String seen = String.format(java.util.Locale.ROOT, "RGD-5 falling 60 blocks: still air drift %.3f in %d ticks, 10 m/s wind drift %.3f blocks downwind in %d ticks",
            drift[0], ticks[0], drift[1], ticks[1]);
        System.out.println("[server] " + seen);
        if (drift[0] != 0 || drift[1] < 0.2 || drift[1] > 20) {
            helper.fail(seen);
        }
        helper.succeed();
    }

    /** docs/WIND.md step 4: a munition autopilot crabs into a crosswind and holds its line; with no wind nothing changes. */
    @GameTest
    public void munitionCrabsInWind(GameTestHelper helper) {
        double air = 1.5;                 // 30 m/s airspeed, blocks/tick
        double wx = 0.4, wz = -0.1;       // 8 m/s crosswind + a little headwind, blocks/tick
        double tx = 0, tz = 600;          // target 600 blocks north
        double[] miss = new double[2];
        double[] maxOff = new double[2];
        for (int run = 0; run < 2; run++) {
            boolean crab = run == 1;
            double x = 0, z = 0, yaw = 0;
            for (int t = 0; t < 2000; t++) {
                double dx = tx - x, dz = tz - z, d = Math.sqrt(dx * dx + dz * dz);
                if (d < 2.0) break;
                double bearing = Math.toDegrees(Math.atan2(-dx, dz));
                yaw = bearing - (crab ? com.fpvdrone.entity.LoiteringMunitionEntity.crabDeg(dx / d, dz / d, air, wx, wz) : 0.0);
                double r = Math.toRadians(yaw);
                x += -Math.sin(r) * air + wx;
                z += Math.cos(r) * air + wz;
                maxOff[run] = Math.max(maxOff[run], Math.abs(x));
            }
            miss[run] = Math.sqrt((tx - x) * (tx - x) + (tz - z) * (tz - z));
        }
        double still = com.fpvdrone.entity.LoiteringMunitionEntity.crabDeg(0, 1, air, 0, 0);
        double tooStrong = com.fpvdrone.entity.LoiteringMunitionEntity.crabDeg(0, 1, 0.3, 0.5, 0);
        String seen = String.format(java.util.Locale.ROOT,
            "munition 600 blocks in 8 m/s crosswind: pursuit only max off-line %.1f (miss %.1f), crabbing max off-line %.2f (miss %.1f); still air crab %.3f deg; wind faster than airspeed crab %.1f deg",
            maxOff[0], miss[0], maxOff[1], miss[1], still, tooStrong);
        System.out.println("[server] " + seen);
        if (miss[1] > 2.5 || maxOff[1] > 0.5 || maxOff[0] < 5 || still != 0.0 || Math.abs(tooStrong) < 89 || Math.abs(tooStrong) > 91) {
            helper.fail(seen);
        }
        helper.succeed();
    }
}
