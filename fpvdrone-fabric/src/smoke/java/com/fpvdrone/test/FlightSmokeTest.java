package com.fpvdrone.test;

import com.fpvdrone.FPVClientMod;
import com.fpvdrone.FPVMod;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.client.osd.BootStateManager;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroneLinkManager;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.item.RemoteControlItem;
import com.fpvdrone.registry.ModEntities;
import com.fpvdrone.registry.ModItems;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class FlightSmokeTest implements FabricClientGameTest {
    /** A button with this text anywhere in the screen, inside lists and their rows too. */
    static net.minecraft.client.gui.components.AbstractWidget findButton(net.minecraft.client.gui.components.events.GuiEventListener node, String text) {
        if (node instanceof net.minecraft.client.gui.components.AbstractWidget w && w.getMessage().getString().equals(text)) {
            return w;
        }
        if (node instanceof net.minecraft.client.gui.components.events.ContainerEventHandler c) {
            for (var child : c.children()) {
                var found = findButton(child, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            // The original's settings flow: a button in Minecraft's Controls screen opens the setup wizard.
            context.setScreen(() -> new net.minecraft.client.gui.screens.options.controls.ControlsScreen(null, net.minecraft.client.Minecraft.getInstance().options));
            context.waitTicks(2);
            context.takeScreenshot("gui-controls");
            // The button is a row of the Controls options list: find it there and click it with the mouse.
            var at = context.computeOnClient(mc -> {
                var button = findButton(mc.gui.screen(), "Remote-Controller Settings");
                if (button == null) throw new AssertionError("No Remote-Controller Settings button in the Controls screen");
                return new double[] {button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0};
            });
            context.runOnClient(mc -> {
                var click = new net.minecraft.client.input.MouseButtonEvent(at[0], at[1], new net.minecraft.client.input.MouseButtonInfo(com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT, 0));
                mc.gui.screen().mouseClicked(click, false);
                mc.gui.screen().mouseReleased(click);
                System.out.println("[smoke] clicked at " + at[0] + "," + at[1] + " gui " + mc.getWindow().getGuiScaledWidth() + "x" + mc.getWindow().getGuiScaledHeight() + " -> screen now " + mc.gui.screen());
            });
            context.waitFor(mc -> mc.gui.screen() instanceof com.fpvdrone.gui.screen.wizard.DecideControllerScreen, 40);
            context.takeScreenshot("gui-wizard-decide");
            context.clickScreenButton("No");
            context.waitFor(mc -> mc.gui.screen() instanceof com.fpvdrone.gui.screen.wizard.CalibrateKeyboardScreen, 40);
            context.takeScreenshot("gui-wizard-keyboard");
            context.setScreen(() -> new com.fpvdrone.gui.screen.wizard.CalibrateControllerStickScreen(null));
            context.waitTicks(2);
            context.takeScreenshot("gui-wizard-stick");
            context.setScreen(() -> new com.fpvdrone.gui.screen.wizard.CalibrateControllerArmScreen(null));
            context.waitTicks(2);
            context.takeScreenshot("gui-wizard-arm");
            context.setScreen(() -> new com.fpvdrone.gui.screen.wizard.CompleteScreen(null, com.fpvdrone.gui.screen.wizard.CompleteScreen.Mode.KEYBOARD));
            context.waitTicks(2);
            context.takeScreenshot("gui-wizard-complete");
            context.clickScreenButton("More Settings");
            context.waitTicks(2);
            context.takeScreenshot("gui-more-settings");
            context.setScreen(() -> null);
            System.out.println("[smoke] settings screens opened");
            context.setScreen(com.fpvdrone.client.gui.FlightSettingsScreen::new);
            context.takeScreenshot("fpv-controls");
            context.clickScreenButton("Done");
            // While a drone detonates, vanilla's explosion hurts no entity: the mod's shrapnel pass does the damage.
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                var cow = net.minecraft.world.entity.EntityTypes.COW.create(level, EntitySpawnReason.COMMAND);
                cow.snapTo(player.getX() + 6, player.getY(), player.getZ(), 0, 0);
                level.addFreshEntity(cow);
                FPVMod.suppressVanillaTntEntityDamage(true);
                try {
                    level.explode(null, cow.getX() + 1, cow.getY(), cow.getZ(), 3.0F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                } finally {
                    FPVMod.suppressVanillaTntEntityDamage(false);
                }
                if (cow.getHealth() < cow.getMaxHealth()) {
                    throw new AssertionError("Vanilla explosion damage was not suppressed: cow at " + cow.getHealth());
                }
                level.explode(null, cow.getX() + 1, cow.getY(), cow.getZ(), 3.0F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                if (cow.isAlive() && cow.getHealth() >= cow.getMaxHealth()) {
                    throw new AssertionError("Control explosion did no damage; the check above proves nothing");
                }
                cow.discard();
            });
            System.out.println("[smoke] vanilla explosion damage suppressed during drone detonation");
            // Start the flight with the player standing (not still settling after spawn), so the exit check measures the mod.
            context.waitFor(mc -> mc.player.onGround(), 200);
            var droneId = world.getServer().computeOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                long recipes = server.getRecipeManager().getRecipes().stream()
                    .filter(recipe -> recipe.id().identifier().getNamespace().equals("fpvdrone")).count();
                if (recipes != 14) throw new AssertionError("Expected 14 FPV recipes (12 original + HD camera + GPS module), got " + recipes);
                var droneIds = com.fpvdrone.data.DroneDefinitions.v5suI().keySet();
                for (String id : java.util.List.of("default", "thermal", "mavic", "fp1", "lancet51", "lytui_drun", "orlan10", "test_target")) {
                    if (!droneIds.contains(id)) throw new AssertionError("Drone definition '" + id + "' not loaded; have " + droneIds);
                }
                System.out.println("[smoke] 1.1.8 drone definitions loaded: " + droneIds);
                var drone = ModEntities.DRONE.get().create(player.level(), EntitySpawnReason.COMMAND);
                if (drone == null) throw new AssertionError("Drone creation failed");
                drone.snapTo(player.getX(), player.getY() + 1, player.getZ() + 3, 180.0F, 0.0F);
                drone.setOwner(player.getUUID());
                drone.setHasBattery(true);
                drone.assignUniqueChannel(player.getUUID());
                player.level().addFreshEntity(drone);
                DroneLinkManager.linkDrone(drone.getUUID(), player.getUUID());
                var remote = new ItemStack(ModItems.REMOTE_CONTROL.get());
                RemoteControlItem.setLinkedDroneId(remote, drone.getUUID());
                player.setItemSlot(EquipmentSlot.MAINHAND, remote);
                var goggles = new ItemStack(ModItems.FPV_GOGGLES.get());
                FpvGogglesItem.linkDroneOnChannel(goggles, drone.getVideoChannel(), drone.getUUID());
                player.setItemSlot(EquipmentSlot.HEAD, goggles);
                return drone.getUUID();
            });
            context.waitFor(mc -> FPVClientMod.getActiveDroneId(mc.player.getUUID()).filter(droneId::equals).isPresent(), 400);
            context.runOnClient(mc -> {
                BootStateManager.getInstance().reset();
                KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_N));
            });
            context.waitFor(mc -> FPVClientMod.isArmed(mc.player.getUUID()), 200);
            context.waitTicks(40);
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                if (!droneId.equals(FPVMod.getServerActiveDroneId(player.getUUID()))) {
                    throw new AssertionError("Server did not receive flight entry");
                }
                if (!(player.level().getEntity(droneId) instanceof DroneEntity drone) || !drone.areMotorsArmed()) {
                    throw new AssertionError("Drone arm state did not synchronize");
                }
            });
            // The drone faces the pilot: the pilot body with goggles on, seen through the feed.
            context.waitTicks(10);
            context.takeScreenshot("render-pilot-from-drone");
            // The inventory screen while flying shows the pilot body (with the goggles) instead of the flying player.
            context.setScreen(() -> new net.minecraft.client.gui.screens.inventory.InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
            // The mouse over the preview, where a player's cursor would be (the original's mouse-follow is not clamped).
            context.getInput().setCursorPos(360, 120);
            context.waitTicks(5);
            context.takeScreenshot("gui-inventory-in-fpv");
            context.setScreen(() -> null);
            // A pilot in FPV can't throw items; the server refuses a drop action (the original cancels the toss).
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                player.drop(false);
                var held = player.getMainHandItem();
                if (!held.is(ModItems.REMOTE_CONTROL.get())) {
                    throw new AssertionError("The remote was thrown while flying: holding " + held);
                }
            });
            System.out.println("[smoke] item toss blocked in FPV");
            double startY = context.computeOnClient(mc -> mc.player.getY());
            context.runOnClient(mc -> com.fpvdrone.FpvConfig.setStickOverlay(true));
            float usedBefore = context.computeOnClient(mc -> com.fpvdrone.client.BatteryManager.getInstance().getUsedMahFloat());
            context.getInput().holdKey(com.mojang.blaze3d.platform.InputConstants.KEY_W);
            context.waitFor(mc -> mc.player.getY() > startY + 2.0, 400);
            context.waitTicks(20);
            context.getInput().releaseKey(com.mojang.blaze3d.platform.InputConstants.KEY_W);
            context.getInput().holdKey(com.mojang.blaze3d.platform.InputConstants.KEY_D);
            context.waitTicks(3);
            context.takeScreenshot("fpv-flight");
            context.getInput().releaseKey(com.mojang.blaze3d.platform.InputConstants.KEY_D);
            // The OSD draws every frame and runs the battery: flying under throttle uses charge.
            float usedAfter = context.computeOnClient(mc -> com.fpvdrone.client.BatteryManager.getInstance().getUsedMahFloat());
            float volts = context.computeOnClient(mc -> com.fpvdrone.client.BatteryManager.getInstance().getTotalVoltage());
            if (!(usedAfter > usedBefore)) {
                throw new AssertionError("Battery did not drain in flight: " + usedBefore + " -> " + usedAfter + " mAh");
            }
            System.out.println("[smoke] battery " + usedBefore + " -> " + usedAfter + " mAh used, " + volts + " V");
            // 1.1.8 payload drop: hang an RGD-5 on the drone, press the bomb key, and a dropped bomb leaves the drone.
            world.getServer().runOnServer(server -> {
                var drone = (DroneEntity) world.getConnection().getServerPlayer().level().getEntity(droneId);
                drone.v5suI("rgd5");
            });
            context.waitFor(mc -> FPVMod.getActiveDrone(mc.player.getUUID()).map(d -> "rgd5".equals(d.i263D9())).orElse(false), 100);
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_B);
            context.waitTicks(4);
            world.getServer().runOnServer(server -> {
                var level = world.getConnection().getServerPlayer().level();
                var drone = (DroneEntity) level.getEntity(droneId);
                var bombs = level.getEntities(com.fpvdrone.registry.ModEntities.DROPPED_BOMB.get(), drone.getBoundingBox().inflate(8.0), b -> true);
                if (bombs.isEmpty()) throw new AssertionError("Pressing the bomb key dropped nothing");
                if (!drone.i263D9().isEmpty()) throw new AssertionError("The drone still carries its payload after the drop: " + drone.i263D9());
                bombs.forEach(net.minecraft.world.entity.Entity::discard);
            });
            System.out.println("[smoke] bomb key drops the hanging RGD-5 (1.1.8)");
            // The goggles' video feed: low-res picture on, F7 cycles the resolution (creative), last frame kept.
            boolean lowRes = context.computeOnClient(mc -> com.fpvdrone.render.LowResRenderer.getInstance().isEnabled());
            if (!lowRes) throw new AssertionError("Low-res video feed is off while flying");
            boolean frozenKept = context.computeOnClient(mc -> com.fpvdrone.video.FrozenFrameManager.getInstance().hasFrozenFrame(droneId));
            if (!frozenKept) throw new AssertionError("No last frame kept for the drone's video feed");
            // F7 is a creative-only key in the original (survival scales the picture with range).
            world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().setGameMode(net.minecraft.world.level.GameType.CREATIVE));
            context.waitFor(mc -> mc.player.isCreative(), 100);
            context.waitTicks(3);
            var resBefore = context.computeOnClient(mc -> com.fpvdrone.render.LowResRenderer.getInstance().getResolution());
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F7);
            context.waitTicks(3);
            var resAfter = context.computeOnClient(mc -> com.fpvdrone.render.LowResRenderer.getInstance().getResolution());
            if (resAfter == resBefore) throw new AssertionError("F7 did not change the video resolution: " + resBefore);
            context.takeScreenshot("fpv-video-" + resAfter.name());
            world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().setGameMode(net.minecraft.world.level.GameType.SURVIVAL));
            System.out.println("[smoke] video feed: low-res on, frame kept, F7 " + resBefore + " -> " + resAfter);
            // F1 is vanilla (hides the HUD only); the battery keeps draining with the HUD hidden.
            float usedBeforeF1 = context.computeOnClient(mc -> com.fpvdrone.client.BatteryManager.getInstance().getUsedMahFloat());
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
            context.waitTicks(40);
            context.takeScreenshot("fpv-flight-hidden");
            float usedAfterF1 = context.computeOnClient(mc -> com.fpvdrone.client.BatteryManager.getInstance().getUsedMahFloat());
            if (!(usedAfterF1 > usedBeforeF1)) throw new AssertionError("Battery stopped draining with the HUD hidden: " + usedBeforeF1 + " -> " + usedAfterF1);
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
            context.waitTicks(2);
            context.takeScreenshot("fpv-flight-visible-again");
            System.out.println("[smoke] battery drains with F1: " + usedBeforeF1 + " -> " + usedAfterF1);
            // Clean feed (F6): refused in survival without an HD camera, allowed with one; it costs extra current.
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F6);
            context.waitTicks(2);
            if (context.computeOnClient(mc -> com.fpvdrone.client.CleanFeed.isToggled())) throw new AssertionError("Clean feed turned on in survival without an HD camera");
            world.getServer().runOnServer(server -> {
                if (world.getConnection().getServerPlayer().level().getEntity(droneId) instanceof DroneEntity d) d.setHasHdCamera(true);
            });
            context.waitFor(mc -> FPVMod.getActiveDrone(mc.player.getUUID()).map(DroneEntity::hasHdCamera).orElse(false), 40);
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F6);
            context.waitTicks(10);
            if (!context.computeOnClient(mc -> com.fpvdrone.client.CleanFeed.isActive())) throw new AssertionError("Clean feed did not turn on with an HD camera");
            float multiplier = context.computeOnClient(mc -> com.fpvdrone.client.BatteryManager.getInstance().getCurrentMultiplier());
            if (multiplier != com.fpvdrone.client.CleanFeed.SURVIVAL_CURRENT_MULTIPLIER) throw new AssertionError("Clean feed current multiplier is " + multiplier);
            context.takeScreenshot("fpv-clean-feed");
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F6);
            context.waitTicks(2);
            if (context.computeOnClient(mc -> com.fpvdrone.client.CleanFeed.isActive()) || context.computeOnClient(mc -> com.fpvdrone.client.BatteryManager.getInstance().getCurrentMultiplier()) != 1.0F) {
                throw new AssertionError("Clean feed did not turn off");
            }
            context.takeScreenshot("fpv-clean-feed-off");
            System.out.println("[smoke] clean feed: refused without HD camera, on with it (current x" + multiplier + "), off again");
            // Turtle mode (not in 1.1.8, config turtleMode): land, lie on the back, stay there, flip back with the key + pitch.
            context.runOnClient(mc -> com.fpvdrone.FpvConfig.setTurtleMode(true));
            context.getInput().holdKey(com.mojang.blaze3d.platform.InputConstants.KEY_S);
            context.waitFor(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).isGroundContact(), 400);
            context.getInput().releaseKey(com.mojang.blaze3d.platform.InputConstants.KEY_S);
            context.runOnClient(mc -> FPVMod.getPhysicsState(mc.player.getUUID())
                .setDroneOrientation(new org.joml.Vector3f(0.0F, 0.0F, 1.0F), new org.joml.Vector3f(0.1F, -1.0F, 0.0F)));
            context.waitTicks(30);
            float upsideDownY = context.computeOnClient(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).getDroneUp().y);
            if (upsideDownY > -0.9F) throw new AssertionError("Upside-down drone did not stay on its back: up.y " + upsideDownY);
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_Y);
            context.waitTicks(2);
            if (!context.computeOnClient(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).isTurtleMode())) throw new AssertionError("Y did not turn turtle mode on");
            context.takeScreenshot("fpv-turtle-mode");
            for (int i = 0; i < 200 && context.computeOnClient(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).isTurtleMode()); i++) {
                context.runOnClient(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).setMouseDeltaPitch(20.0F));
                context.waitTicks(1);
            }
            float flippedY = context.computeOnClient(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).getDroneUp().y);
            if (context.computeOnClient(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).isTurtleMode()) || flippedY < 0.7F) {
                throw new AssertionError("Turtle mode did not flip the drone upright: up.y " + flippedY);
            }
            context.runOnClient(mc -> com.fpvdrone.FpvConfig.setTurtleMode(false));
            System.out.println("[smoke] turtle mode: drone stays on its back, Y + pitch flips it upright (up.y " + upsideDownY + " -> " + flippedY + ")");
            // GPS Rescue (port addition): fit a GPS module, climb, P flies the quad home on the GPS model and lands it.
            world.getServer().runOnServer(server -> ((DroneEntity) world.getConnection().getServerPlayer().level().getEntity(droneId)).setHasGpsModule(true));
            context.waitFor(mc -> FPVMod.getActiveDrone(mc.player.getUUID()).map(DroneEntity::hasGpsModule).orElse(false), 100);
            double rescueStartY = context.computeOnClient(mc -> mc.player.getY());
            context.getInput().holdKey(com.mojang.blaze3d.platform.InputConstants.KEY_W);
            context.waitFor(mc -> mc.player.getY() > rescueStartY + 3.0, 400);
            context.getInput().releaseKey(com.mojang.blaze3d.platform.InputConstants.KEY_W);
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_P);
            context.waitTicks(2);
            if (!context.computeOnClient(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).isGpsRescue())) throw new AssertionError("P did not start a GPS Rescue");
            context.waitTicks(20);
            context.takeScreenshot("fpv-gps-rescue");
            context.waitFor(mc -> !FPVClientMod.isArmed(mc.player.getUUID()), 1200);
            String rescueEnd = context.computeOnClient(mc -> {
                var home = mc.player.position();
                var ps = FPVMod.getPhysicsState(mc.player.getUUID());
                return "landed at " + net.minecraft.core.BlockPos.containing(home).toShortString() + ", rescue " + ps.isGpsRescue() + ", ground " + ps.isGroundContact();
            });
            if (context.computeOnClient(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).isGpsRescue())) throw new AssertionError("GPS Rescue still on after landing: " + rescueEnd);
            System.out.println("[smoke] GPS Rescue: P flies the quad with a GPS module home and lands it (" + rescueEnd + ")");
            // Landing disarmed the quad; arm again (once the drone entity reports it) for the exit check below.
            context.waitFor(mc -> FPVMod.getActiveDrone(mc.player.getUUID()).map(d -> !d.areMotorsArmed()).orElse(false), 100);
            context.runOnClient(mc -> KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_N)));
            context.waitFor(mc -> FPVClientMod.isArmed(mc.player.getUUID()), 200);
            // Signal lost with a GPS module: GPS Rescue flies home instead of 1.1.8's failsafe drop.
            double lossStartY = context.computeOnClient(mc -> mc.player.getY());
            context.getInput().holdKey(com.mojang.blaze3d.platform.InputConstants.KEY_W);
            context.waitFor(mc -> mc.player.getY() > lossStartY + 3.0, 400);
            context.getInput().releaseKey(com.mojang.blaze3d.platform.InputConstants.KEY_W);
            net.minecraft.world.phys.Vec3 realOrigin = context.computeOnClient(mc -> com.fpvdrone.client.SignalManager.getInstance().getOrigin());
            context.runOnClient(mc -> com.fpvdrone.client.SignalManager.getInstance().setOrigin(mc.player.position().add(100000.0, 0.0, 0.0)));
            context.waitFor(mc -> FPVMod.getPhysicsState(mc.player.getUUID()).isGpsRescue(), 100);
            context.runOnClient(mc -> com.fpvdrone.client.SignalManager.getInstance().setOrigin(realOrigin));
            context.waitFor(mc -> !FPVClientMod.isArmed(mc.player.getUUID()), 1200);
            boolean lostDrone = context.computeOnClient(mc -> FPVMod.getActiveDroneId(mc.player.getUUID())
                .map(id -> com.fpvdrone.client.FpvStatusChecker.isDroneDestroyed(id) || com.fpvdrone.client.FpvStatusChecker.isDroneFalling(id)).orElse(true));
            if (lostDrone) throw new AssertionError("Signal loss with a GPS module still dropped the drone");
            System.out.println("[smoke] GPS Rescue on signal loss: the quad flies home and lands instead of falling");
            context.waitFor(mc -> FPVMod.getActiveDrone(mc.player.getUUID()).map(d -> !d.areMotorsArmed()).orElse(false), 100);
            context.runOnClient(mc -> KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_N)));
            context.waitFor(mc -> FPVClientMod.isArmed(mc.player.getUUID()), 200);
            context.runOnClient(mc -> KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_N)));
            context.waitFor(mc -> !FPVClientMod.isArmed(mc.player.getUUID()), 200);
            world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY));
            context.waitFor(mc -> FPVClientMod.getActiveDroneId(mc.player.getUUID()).isEmpty(), 200);
            world.getConnection().waitForServerboundPackets();
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                if (FPVMod.getServerActiveDroneId(player.getUUID()) != null || player.noPhysics) {
                    throw new AssertionError("Flight exit did not restore server player state");
                }
            });
            // After leaving FPV the player stays where the pilot body stood (it was once flung up by drone riding).
            // The return can leave them a little above the ground for a moment; measure once they stand.
            context.waitFor(mc -> mc.player.onGround(), 100);
            double landedY = context.computeOnClient(mc -> mc.player.getY());
            for (int i = 0; i < 20; i++) {
                context.waitTicks(1);
                double y = context.computeOnClient(mc -> mc.player.getY());
                if (Math.abs(y - landedY) > 0.5) throw new AssertionError("Player moved after flight exit: " + landedY + " -> " + y);
            }
            System.out.println("[smoke] player stays at the pilot spot after exit");
            // Port addition (docs/WIND.md): wind on the drone physics. A free-falling drone body dropped from 60 m
            // for one second drifts with the wind, and not at all with wind off (= 1.1.8).
            double[] home = context.computeOnClient(mc -> new double[] {mc.player.getX(), mc.player.getY(), mc.player.getZ()});
            String drift = context.computeOnClient(mc -> {
                StringBuilder out = new StringBuilder();
                float[] moved = new float[2];
                for (int run = 0; run < 2; run++) {
                    com.fpvdrone.FpvConfig.setWind(run == 1, 2.0F);
                    mc.player.setPos(home[0], home[1] + 60, home[2]);
                    var ps = new com.fpvdrone.physics.PhysicsState(mc.player);
                    ps.setFalling(true);
                    for (int t = 0; t < 20; t++) {
                        ps.tick(0.05F);
                    }
                    org.joml.Vector3f w = new org.joml.Vector3f(ps.getWind());
                    double dx = mc.player.getX() - home[0];
                    double dz = mc.player.getZ() - home[2];
                    moved[run] = (float)Math.hypot(dx, dz);
                    double along = w.length() > 0 ? (dx * w.x + dz * w.z) / w.length() : 0;
                    out.append(String.format(java.util.Locale.ROOT, "%s: wind %.1f m/s, drifted %.2f m (%.2f downwind); ",
                        run == 0 ? "off" : "on", w.length(), moved[run], along));
                    if (run == 1 && (w.length() < 0.5F || along < 0.3)) out.append("FAIL");
                }
                if (moved[0] > 0.01F) out.append("FAIL");
                com.fpvdrone.FpvConfig.setWind(true, 1.0F);
                return out.toString();
            });
            world.getServer().runCommand(String.format(java.util.Locale.ROOT, "tp @a %.2f %.2f %.2f", home[0], home[1], home[2]));
            context.waitTicks(5);
            System.out.println("[smoke] wind drift: " + drift);
            if (drift.contains("FAIL")) throw new AssertionError("Wind drift wrong: " + drift);
            // The BetaFlight configurator (OSD builder) on the drone.
            context.runOnClient(mc -> {
                for (var e : mc.level.entitiesForRendering()) {
                    if (e instanceof DroneEntity d && d.getUUID().equals(droneId)) {
                        com.fpvdrone.gui.GuiEvents.openOsdBuilder(d, d.getOsdLayout());
                    }
                }
            });
            context.waitFor(mc -> mc.gui.screen() instanceof com.fpvdrone.gui.screen.OsdBuilderScreen, 40);
            context.waitTicks(2);
            context.takeScreenshot("gui-osd-builder");
            context.setScreen(() -> null);
            System.out.println("[smoke] OSD builder opened");
            // Rendering from outside: a second drone disarmed, armed on its launch bricks, and at high throttle
            // with prop blur discs; a villager nearby should run from it.
            context.waitFor(mc -> mc.player.onGround(), 200);
            context.setScreen(() -> new net.minecraft.client.gui.screens.inventory.InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
            context.getInput().setCursorPos(360, 120);
            context.waitTicks(5);
            context.takeScreenshot("gui-inventory-normal");
            context.setScreen(() -> null);
            context.waitTicks(20);
            var drone2Id = world.getServer().computeOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                var drone = ModEntities.DRONE.get().create(level, EntitySpawnReason.COMMAND);
                drone.snapTo(player.getX(), player.getY(), player.getZ() + 2.5, 180.0F, 0.0F);
                drone.setOwner(player.getUUID());
                drone.setHasBattery(true);
                level.addFreshEntity(drone);
                var villager = net.minecraft.world.entity.EntityTypes.VILLAGER.create(level, EntitySpawnReason.COMMAND);
                villager.snapTo(drone.getX() + 3, player.getY(), drone.getZ() + 2, 0, 0);
                level.addFreshEntity(villager);
                return drone.getUUID();
            });
            context.runOnClient(mc -> {
                mc.player.setYRot(0.0F);
                mc.player.setXRot(40.0F);
            });
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
            context.waitTicks(10);
            context.takeScreenshot("render-drone-disarmed");
            world.getServer().runOnServer(server -> {
                var level = world.getConnection().getServerPlayer().level();
                if (level.getEntity(drone2Id) instanceof DroneEntity d) d.setMotorsArmed(true);
            });
            context.waitTicks(10);
            context.takeScreenshot("render-drone-armed");
            world.getServer().runOnServer(server -> {
                var level = world.getConnection().getServerPlayer().level();
                if (level.getEntity(drone2Id) instanceof DroneEntity d) d.setSyncedThrottle(0.8F);
            });
            context.waitTicks(10);
            context.takeScreenshot("render-drone-throttle");
            // 1.1.8: a Mavic beside it (own model, blur from its own blur bones, loaded bomb bay with the VOG rack).
            var mavicId = world.getServer().computeOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                var mavic = ModEntities.DRONE.get().create(level, EntitySpawnReason.COMMAND);
                mavic.snapTo(player.getX() + 1.5, player.getY(), player.getZ() + 2.5, 180.0F, 0.0F);
                mavic.ibzfxic("mavic");
                mavic.setHasBattery(true);
                mavic.v5suI((byte)0x0F);
                level.addFreshEntity(mavic);
                return mavic.getUUID();
            });
            context.waitTicks(10);
            context.takeScreenshot("render-mavic-idle");
            world.getServer().runOnServer(server -> {
                var level = world.getConnection().getServerPlayer().level();
                if (level.getEntity(mavicId) instanceof DroneEntity d) {
                    d.setMotorsArmed(true);
                    d.setSyncedThrottle(0.8F);
                }
            });
            context.waitTicks(10);
            context.takeScreenshot("render-mavic-throttle");
            world.getServer().runOnServer(server -> {
                var level = world.getConnection().getServerPlayer().level();
                if (level.getEntity(mavicId) instanceof DroneEntity d) {
                    if (!d.xogV5KBhg()) throw new AssertionError("The Mavic is not a GPS drone");
                    var cam = com.fpvdrone.render.CameraManager.cameraOf(d);
                    if (!cam.gimbal() || cam.freeLook() != d.oEuCBB().bAcdvq9().freeLook()) {
                        throw new AssertionError("The Mavic's camera does not come from its file: gimbal=" + cam.gimbal());
                    }
                    d.discard();
                }
            });
            System.out.println("[smoke] Mavic drawn with its own model and blur, flies as a GPS drone with a gimbal camera (1.1.8)");
            // 1.1.8: a launch ramp with an FP-1 on it and three loitering munitions (FP-1 with its rocket pods loaded,
            // Lancet, Orlan) are drawn by their own renderers; 26.3 crashes on an entity type without one. They stand
            // behind the player, apart from each other and from the villager and drones: a munition on the ground touching
            // an entity goes off.
            var launchIds = world.getServer().computeOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                var ids = new java.util.ArrayList<java.util.UUID>();
                var ramp = ModEntities.RAMP.get().create(level, EntitySpawnReason.COMMAND);
                ramp.snapTo(player.getX(), player.getY(), player.getZ() - 17.0, 90.0F, 0.0F);
                ramp.v5suI("fp1");
                level.addFreshEntity(ramp);
                ids.add(ramp.getUUID());
                String[] kinds = {"fp1", "lancet51", "orlan10"};
                double[][] spot = {{-7.0, -10.0}, {0.0, -10.0}, {7.0, -10.0}};
                for (int i = 0; i < kinds.length; i++) {
                    var munition = ModEntities.LOITERING_MUNITION.get().create(level, EntitySpawnReason.COMMAND);
                    munition.snapTo(player.getX() + spot[i][0], player.getY(), player.getZ() + spot[i][1], 90.0F, 0.0F);
                    munition.v5suI(kinds[i], 90.0F, (java.util.UUID) null);
                    if (i == 0) munition.v5suI((byte) 0x03, (byte) 0xFF);
                    level.addFreshEntity(munition);
                    ids.add(munition.getUUID());
                }
                return ids;
            });
            context.runOnClient(mc -> {
                mc.player.setYRot(180.0F);
                mc.player.setXRot(12.0F);
            });
            context.waitTicks(20);
            String launchServerState = world.getServer().computeOnServer(server -> {
                var level = (net.minecraft.server.level.ServerLevel) world.getConnection().getServerPlayer().level();
                var sb = new StringBuilder();
                for (var id : launchIds) {
                    var e = level.getEntity(id);
                    sb.append(e == null ? "gone" : e.getType().toShortString() + "@" + e.blockPosition().toShortString() + (e.isRemoved() ? " removed " + e.getRemovalReason() : "")).append("; ");
                }
                return sb.toString();
            });
            System.out.println("[smoke] ramp/munitions on the server: " + launchServerState);
            // 1.1.8 ProjectileUtilMixin: a ray down through the Lancet hits it on its zone hitboxes (with the hit point kept).
            String rayResult = world.getServer().computeOnServer(server -> {
                var level = (net.minecraft.server.level.ServerLevel) world.getConnection().getServerPlayer().level();
                var lancet = level.getEntity(launchIds.get(2));
                if (lancet == null) return "Lancet missing for the hitbox check";
                var c = lancet.getBoundingBox().getCenter();
                var from = new net.minecraft.world.phys.Vec3(c.x, c.y + 6.0, c.z);
                var to = new net.minecraft.world.phys.Vec3(c.x, c.y - 3.0, c.z);
                var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                    level, (net.minecraft.world.entity.Entity) world.getConnection().getServerPlayer(), from, to,
                    new net.minecraft.world.phys.AABB(from, to).inflate(1.0), e -> e == lancet, 0.3F);
                if (hit == null || hit.getEntity() != lancet) return "A ray through the Lancet did not hit it (" + hit + ", box " + lancet.getBoundingBox() + ")";
                if (com.fpvdrone.entity.ProjectileHitPoints.v5suI(lancet.getId()) == null) return "No zone hit point recorded";
                return "ok " + hit.getLocation();
            });
            System.out.println("[smoke] projectile ray check: " + rayResult);
            if (!rayResult.startsWith("ok ")) throw new AssertionError(rayResult);
            System.out.println("[smoke] projectile ray hits the Lancet on its zone hitboxes at " + rayResult.substring(3));
            context.runOnClient(mc -> {
                int found = 0;
                for (var e : mc.level.entitiesForRendering()) {
                    if (!launchIds.contains(e.getUUID())) continue;
                    found++;
                    Object renderer = mc.getEntityRenderDispatcher().getRenderer(e);
                    boolean ok = e instanceof com.fpvdrone.entity.RampEntity
                        ? renderer instanceof com.fpvdrone.client.renderer.RampRenderer
                        : renderer instanceof com.fpvdrone.client.renderer.LoiteringMunitionRenderer;
                    if (!ok) throw new AssertionError("Wrong renderer for " + e + ": " + renderer);
                    if (e instanceof com.fpvdrone.entity.LoiteringMunitionEntity m) {
                        if (com.fpvdrone.client.model.BedrockModels.v5suI(m.ivDR()) == null) throw new AssertionError("No model for " + m.ivDR());
                        if (com.fpvdrone.client.model.BedrockPolyMeshes.get(m.ivDR()).isEmpty()) throw new AssertionError("No poly meshes in " + m.ivDR());
                    }
                }
                if (found != launchIds.size()) throw new AssertionError("Only " + found + " of " + launchIds.size() + " ramp/munition entities reached the client");
            });
            context.takeScreenshot("render-ramp-munitions");
            context.runOnClient(mc -> mc.debugEntries.toggleStatus(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES));
            context.waitTicks(5);
            context.takeScreenshot("render-ramp-munitions-hitboxes");
            context.runOnClient(mc -> mc.debugEntries.toggleStatus(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES));
            world.getServer().runOnServer(server -> {
                var level = world.getConnection().getServerPlayer().level();
                for (var id : launchIds) {
                    var e = ((net.minecraft.server.level.ServerLevel) level).getEntity(id);
                    if (e != null) e.discard();
                }
            });
            context.runOnClient(mc -> {
                mc.player.setYRot(0.0F);
                mc.player.setXRot(40.0F);
            });
            context.waitTicks(5);
            System.out.println("[smoke] launch ramp with its drone and loitering munitions drawn by their 1.1.8 renderers");
            // 1.1.8: an EW jammer registers its zone on the client; with hitboxes on (F3+B) the zone is drawn.
            var jammerPos = world.getServer().computeOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var pos = player.blockPosition().offset(0, 0, 8);
                player.level().setBlockAndUpdate(pos, com.fpvdrone.registry.ModBlocks.REB_JAMMER.get().defaultBlockState());
                return pos;
            });
            context.waitTicks(10);
            context.runOnClient(mc -> {
                if (com.fpvdrone.block.JammerZones.zones().isEmpty()) throw new AssertionError("Jammer zone not registered on the client");
                mc.debugEntries.setStatus(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES,
                    net.minecraft.client.gui.components.debug.DebugScreenEntryStatus.ALWAYS_ON);
            });
            context.waitTicks(3);
            // The test client does not draw debug hitboxes (not even vanilla's), so the ring itself is checked in game.
            world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().level().removeBlock(jammerPos, false));
            context.waitTicks(10);
            context.runOnClient(mc -> {
                mc.debugEntries.setStatus(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES,
                    net.minecraft.client.gui.components.debug.DebugScreenEntryStatus.NEVER);
                if (!com.fpvdrone.block.JammerZones.zones().isEmpty()) throw new AssertionError("Jammer zone kept after the block was removed");
            });
            System.out.println("[smoke] EW jammer zone registered on the client and removed with the block (1.1.8)");
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
            // Villagers near a drone run away from it (and ring a bell if there is one).
            context.waitTicks(60);
            world.getServer().runOnServer(server -> {
                var level = (net.minecraft.server.level.ServerLevel) world.getConnection().getServerPlayer().level();
                var drone = level.getEntity(drone2Id);
                double dist = level.getEntities(net.minecraft.world.entity.EntityTypes.VILLAGER, v -> true).stream()
                    .mapToDouble(v -> v.distanceTo(drone)).max().orElse(0.0);
                System.out.println("[smoke] villager distance from drone after 4 s: " + dist);
                if (dist < 6.0) throw new AssertionError("The villager did not run from the drone: " + dist + " blocks");
            });
            System.out.println("[smoke] villager panics near a drone");
            // 1.1.8 interact menu: looking at a drone on foot shows its options; the interact key (F) runs the selected one.
            world.getServer().runOnServer(server -> {
                var level = world.getConnection().getServerPlayer().level();
                if (level.getEntity(drone2Id) instanceof DroneEntity d) {
                    d.setSyncedThrottle(0.0F);
                    d.setMotorsArmed(false);
                }
            });
            context.waitTicks(5);
            context.runOnClient(mc -> {
                var drone = mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(8.0), e -> e.getUUID().equals(drone2Id)).getFirst();
                var to = drone.getBoundingBox().getCenter().subtract(mc.player.getEyePosition());
                mc.player.setYRot((float)Math.toDegrees(Math.atan2(-to.x, to.z)));
                mc.player.setXRot((float)Math.toDegrees(-Math.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z))));
            });
            context.waitFor(mc -> com.fpvdrone.client.interact.InteractMenu.current() != null, 60);
            context.waitTicks(10);
            var menuOptions = context.computeOnClient(mc -> com.fpvdrone.client.interact.InteractMenu.current().options().stream()
                .map(o -> o.id() + (o.available() ? "" : "(n/a)")).toList());
            System.out.println("[smoke] interact menu on the drone: " + menuOptions);
            if (!menuOptions.contains("detachBattery") || !menuOptions.contains("pickupDrone")) throw new AssertionError("Interact menu options wrong: " + menuOptions);
            context.takeScreenshot("interact-menu");
            // Scroll down one line and back: the selection moves.
            context.runOnClient(mc -> com.fpvdrone.client.interact.InteractMenu.onMouseScroll(-1.0));
            context.waitTicks(5);
            if (context.computeOnClient(mc -> com.fpvdrone.client.interact.InteractMenu.current().selected()) != 1) throw new AssertionError("Scrolling did not move the interact menu");
            context.takeScreenshot("interact-menu-scrolled");
            context.runOnClient(mc -> com.fpvdrone.client.interact.InteractMenu.onMouseScroll(1.0));
            context.waitTicks(2);
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F);
            world.getConnection().waitForServerboundPackets();
            context.waitTicks(5);
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var drone = (DroneEntity) player.level().getEntity(drone2Id);
                if (drone.idXga()) throw new AssertionError("The interact menu did not detach the battery");
                if (player.getInventory().countItem(ModItems.BATTERY.get()) != 1) throw new AssertionError("The detached battery was not given to the player");
                if (!player.getOffhandItem().isEmpty()) throw new AssertionError("F also swapped hands while the menu was open");
            });
            context.waitFor(mc -> com.fpvdrone.client.interact.InteractMenu.current() != null
                && com.fpvdrone.client.interact.InteractMenu.current().options().get(0).id().equals("attachBattery"), 40);
            context.takeScreenshot("interact-menu-battery-off");
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F);
            world.getConnection().waitForServerboundPackets();
            context.waitTicks(5);
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var drone = (DroneEntity) player.level().getEntity(drone2Id);
                if (!drone.idXga()) throw new AssertionError("The interact menu did not attach the battery back");
                if (player.getInventory().countItem(ModItems.BATTERY.get()) != 0) throw new AssertionError("The battery stayed in the inventory");
            });
            System.out.println("[smoke] interact menu: shown on the drone, scrolls, F detaches and re-attaches the battery (1.1.8)");
            // 1.1.8 item tooltips list required / optional / installed parts with icons.
            context.runOnClient(mc -> {
                var stack = new ItemStack(ModItems.DRONE.get());
                var tip = com.fpvdrone.item.DroneTooltipIcons.forItem(stack);
                if (tip.isEmpty() || ((com.fpvdrone.item.ItemIconTooltip) tip.get()).rows().isEmpty()) throw new AssertionError("Drone item has no part icons tooltip");
                if (com.fpvdrone.item.DroneTooltipIcons.forOpener((com.fpvdrone.item.OpenerItem) ModItems.OPENER.get()).isEmpty()) throw new AssertionError("Vest has no mine tooltip");
                // Onboarding hints are off in 1.1.8; the hint for this drone is still worked out.
                var drone = (DroneEntity) mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(8.0), e -> e.getUUID().equals(drone2Id)).getFirst();
                var hint = com.fpvdrone.client.interact.OnboardingHints.hintFor(mc.player, drone);
                if (hint.kind() != com.fpvdrone.client.interact.OnboardingHints.Kind.CONNECT) throw new AssertionError("Unlinked drone should hint CONNECT, got " + hint.kind());
                System.out.println("[smoke] tooltip rows " + ((com.fpvdrone.item.ItemIconTooltip) tip.get()).rows().size() + ", onboarding hint " + hint.kind() + " " + hint.icons());
            });
            // HD camera: right-click fits it (not on top of an existing one), Shift + right-click takes it back.
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var drone = (DroneEntity) player.level().getEntity(drone2Id);
                drone.setMotorsArmed(false);
                drone.setHasBattery(false);
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.HD_CAMERA.get(), 2));
                player.interactOn(drone, net.minecraft.world.InteractionHand.MAIN_HAND, drone.position());
                if (!drone.hasHdCamera() || player.getMainHandItem().getCount() != 1) throw new AssertionError("HD camera was not fitted");
                player.interactOn(drone, net.minecraft.world.InteractionHand.MAIN_HAND, drone.position());
                if (player.getMainHandItem().getCount() != 1) throw new AssertionError("A second HD camera was taken");
                ItemStack picked = drone.toItemStack();
                if (!com.fpvdrone.util.ItemData.get(picked).getBooleanOr("HasHdCamera", false)) throw new AssertionError("The drone item lost its HD camera");
                // 1.1.8: Shift + empty hand picks the drone up, so parts come off with something in the hand.
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.STICK));
                player.setShiftKeyDown(true);
                player.interactOn(drone, net.minecraft.world.InteractionHand.MAIN_HAND, drone.position());
                player.setShiftKeyDown(false);
                player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                if (drone.isRemoved()) throw new AssertionError("The drone was picked up instead of losing its HD camera");
                if (drone.hasHdCamera() || player.getInventory().countItem(ModItems.HD_CAMERA.get()) != 1) throw new AssertionError("HD camera was not given back");
            });
            System.out.println("[smoke] HD camera fits, refuses a second, stays in the drone item, comes off with Shift");
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var drone = (DroneEntity) player.level().getEntity(drone2Id);
                int before = player.getInventory().countItem(drone.toItemStack().getItem());
                player.setShiftKeyDown(true);
                player.interactOn(drone, net.minecraft.world.InteractionHand.MAIN_HAND, drone.position());
                player.setShiftKeyDown(false);
                if (!drone.isRemoved() || player.getInventory().countItem(drone.toItemStack().getItem()) != before + 1) throw new AssertionError("Shift + empty hand did not pick the drone up");
            });
            System.out.println("[smoke] Shift + empty hand picks the drone up (1.1.8)");
            thermalStep(context, world);
            // 1.1.8 "Can Opener" vest: worn with its TM-62 drawn on it; the drop key (B) sets the mine off.
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.OPENER.get()));
            });
            context.waitFor(mc -> mc.player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.OPENER.get()) && mc.player.isCreative(), 40);
            context.runOnClient(mc -> {
                mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
                mc.player.setXRot(10.0F);
            });
            context.waitTicks(10);
            context.takeScreenshot("opener-vest-worn");
            context.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_B);
            world.getConnection().waitForServerboundPackets();
            context.waitTicks(10);
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) throw new AssertionError("The drop key did not set the vest's mine off");
                if (!player.isAlive()) throw new AssertionError("A creative wearer died in his own vest's blast");
            });
            context.waitFor(mc -> com.fpvdrone.render.ShrapnelDebugRenderer.eventCount() > 0, 40);
            System.out.println("[smoke] Can Opener: drop key detonates the vest's mine; shrapnel debug rays received (1.1.8)");
            // A lethal hit on a survival wearer cooks the mine off instead of the plain damage.
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.OPENER.get()));
                if (com.fpvdrone.item.OpenerCookOff.onLethalHit(player, 1.0F)) throw new AssertionError("The vest went off on a light hit");
                if (!player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.OPENER.get())) throw new AssertionError("Vest lost on a light hit");
                if (!com.fpvdrone.item.OpenerCookOff.onLethalHit(player, 1000.0F)) throw new AssertionError("The vest did not go off on a lethal hit");
                if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) throw new AssertionError("The vest is still worn after cooking off");
                if (player.isAlive()) throw new AssertionError("A survival wearer outlived his vest's blast");
            });
            System.out.println("[smoke] Can Opener: a lethal hit cooks the mine off (1.1.8)");
        }
    }

    /** 1.1.8 thermal camera: a thermal drone turns the thermal view on in FPV; palettes, focus (Home) and NUC (U). */
    private static void thermalStep(ClientGameTestContext context, net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext world) {
        context.waitFor(mc -> mc.player.onGround(), 200);
        var thermalId = world.getServer().computeOnServer(server -> {
            var player = world.getConnection().getServerPlayer();
            var level = (net.minecraft.server.level.ServerLevel) player.level();
            var base = player.blockPosition();
            // A scene raised into the camera's view (the drone looks south, its FPV camera tilted up): a stone-brick wall with magma, an
            // iron block and glass in it, a plank ledge with a villager, a campfire and a torch; lava on the ground.
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = 0; dy <= 8; dy++) {
                    level.setBlockAndUpdate(base.offset(dx, dy, -6), net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState());
                }
            }
            level.setBlockAndUpdate(base.offset(-2, 6, -6), net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(base.offset(-1, 6, -6), net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(base.offset(2, 6, -6), net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(base.offset(0, 7, -6), net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState());
            for (int dx = -3; dx <= 3; dx++) {
                level.setBlockAndUpdate(base.offset(dx, 3, -5), net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState());
            }
            level.setBlockAndUpdate(base.offset(2, 4, -5), net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState());
            level.setBlockAndUpdate(base.offset(-3, 4, -5), net.minecraft.world.level.block.Blocks.TORCH.defaultBlockState());
            level.setBlockAndUpdate(base.offset(-2, -1, 2), net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
            var villager2 = net.minecraft.world.entity.EntityTypes.VILLAGER.create(level, EntitySpawnReason.COMMAND);
            villager2.snapTo(base.getX() + 0.5, base.getY() + 4, base.getZ() - 4.5, 0, 0);
            villager2.setNoAi(true);
            level.addFreshEntity(villager2);
            // A scene raised into the camera's view (the drone looks south, its FPV camera tilted up): a stone-brick wall with magma, an
            // iron block and glass in it, a plank ledge with a villager, a campfire and a torch; lava on the ground.
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = 0; dy <= 8; dy++) {
                    level.setBlockAndUpdate(base.offset(dx, dy, 20), net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState());
                }
            }
            level.setBlockAndUpdate(base.offset(-2, 6, 20), net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(base.offset(-1, 6, 20), net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(base.offset(2, 6, 20), net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(base.offset(0, 7, 20), net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState());
            for (int dx = -3; dx <= 3; dx++) {
                level.setBlockAndUpdate(base.offset(dx, 3, 19), net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState());
            }
            level.setBlockAndUpdate(base.offset(2, 4, 19), net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState());
            level.setBlockAndUpdate(base.offset(-3, 4, 19), net.minecraft.world.level.block.Blocks.TORCH.defaultBlockState());
            level.setBlockAndUpdate(base.offset(-2, -1, 12), net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
            var villager = net.minecraft.world.entity.EntityTypes.VILLAGER.create(level, EntitySpawnReason.COMMAND);
            villager.snapTo(base.getX() + 0.5, base.getY() + 4, base.getZ() + 18.5, 0, 0);
            villager.setNoAi(true);
            level.addFreshEntity(villager);
            var drone = ModEntities.DRONE.get().create(level, EntitySpawnReason.COMMAND);
            drone.ibzfxic("thermal");
            drone.setThermal(true);
            drone.snapTo(player.getX(), player.getY() + 1.5, player.getZ() + 7, 180.0F, 0.0F);
            drone.setOwner(player.getUUID());
            drone.setHasBattery(true);
            drone.assignUniqueChannel(player.getUUID());
            level.addFreshEntity(drone);
            DroneLinkManager.linkDrone(drone.getUUID(), player.getUUID());
            var remote = new ItemStack(ModItems.REMOTE_CONTROL.get());
            RemoteControlItem.setLinkedDroneId(remote, drone.getUUID());
            player.setItemSlot(EquipmentSlot.MAINHAND, remote);
            var goggles = new ItemStack(ModItems.FPV_GOGGLES.get());
            FpvGogglesItem.linkDroneOnChannel(goggles, drone.getVideoChannel(), drone.getUUID());
            player.setItemSlot(EquipmentSlot.HEAD, goggles);
            return drone.getUUID();
        });
        // Through the goggles, motors off: the drone stays on the ground facing the scene.
        context.waitFor(mc -> FPVClientMod.getActiveDroneId(mc.player.getUUID()).filter(thermalId::equals).isPresent(), 400);
        context.waitFor(mc -> com.fpvdrone.client.thermal.ThermalCameraState.getInstance().isEnabled(), 100);
        System.out.println("[smoke] thermal drone turns the thermal view on (mode "
            + com.fpvdrone.client.thermal.FallbackManager.getMode() + ")");
        context.waitTicks(40);
        var state = com.fpvdrone.client.thermal.ThermalCameraState.getInstance();
        for (var palette : com.fpvdrone.client.thermal.PaletteMode.values()) {
            context.runOnClient(mc -> state.setPalette(palette));
            context.waitTicks(3);
            context.takeScreenshot("thermal-" + palette.name().toLowerCase(java.util.Locale.ROOT));
        }
        context.runOnClient(mc -> state.setPalette(com.fpvdrone.client.thermal.PaletteMode.WHITE_HOT));
        // Home: thermal focus (depth of field) off and on again.
        context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_HOME);
        context.waitTicks(3);
        if (context.computeOnClient(mc -> state.isDepthOfField())) throw new AssertionError("Home did not turn the thermal focus off");
        context.takeScreenshot("thermal-focus-off");
        context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_HOME);
        context.waitTicks(2);
        if (!context.computeOnClient(mc -> state.isDepthOfField())) throw new AssertionError("Home did not turn the thermal focus back on");
        // U: NUC calibration (0.8 s, then less fixed-pattern noise).
        context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_U);
        context.waitTicks(2);
        if (!context.computeOnClient(mc -> com.fpvdrone.client.thermal.NUCManager.getInstance().isFreezeActive())) throw new AssertionError("U did not start a NUC calibration");
        context.waitTicks(25);
        if (!(context.computeOnClient(mc -> com.fpvdrone.client.thermal.NUCManager.getInstance().getFpnReduction()) > 0.0F)) throw new AssertionError("NUC did not reduce the fixed-pattern noise");
        System.out.println("[smoke] thermal palettes drawn, focus key (Home) and NUC (U) work");
        // Footprints: a villager walked across the ground in front of the drone leaves warm prints.
        var walkerId = world.getServer().computeOnServer(server -> {
            var level = (net.minecraft.server.level.ServerLevel) world.getConnection().getServerPlayer().level();
            var walker = net.minecraft.world.entity.EntityTypes.VILLAGER.create(level, EntitySpawnReason.COMMAND);
            walker.snapTo(-3.0, -60.0, 3.5, 270.0F, 0.0F);
            walker.setNoAi(true);
            level.addFreshEntity(walker);
            return walker.getUUID();
        });
        for (int i = 0; i < 24; i++) {
            final double x = -3.0 + i * 0.25;
            world.getServer().runOnServer(server -> {
                var e = world.getConnection().getServerPlayer().level().getEntity(walkerId);
                if (e != null) {
                    e.setYRot(270.0F);
                    ((net.minecraft.world.entity.LivingEntity) e).yBodyRot = 270.0F;
                    e.teleportTo(x, e.getY(), e.getZ());
                }
            });
            context.waitTicks(1);
        }
        context.waitTicks(3);
        int prints = context.computeOnClient(mc -> com.fpvdrone.client.thermal.FootprintSystem.getInstance().getDecalCount());
        if (prints == 0) throw new AssertionError("No thermal footprints after a villager walked");
        context.takeScreenshot("thermal-footprints");
        System.out.println("[smoke] walking leaves " + prints + " warm footprints");
        // The same view without thermal, for comparison.
        context.runOnClient(mc -> state.setEnabled(false));
        context.waitTicks(3);
        context.takeScreenshot("thermal-view-normal");
        context.runOnClient(mc -> state.setEnabled(true));
        // The screen-space fallback used with Sodium or Iris (1.1.8 FallbackThermalPass), forced here on vanilla.
        context.runOnClient(mc -> com.fpvdrone.client.thermal.FallbackManager.forceFallback("smoke test"));
        context.waitTicks(3);
        context.takeScreenshot("thermal-fallback-white_hot");
        context.runOnClient(mc -> state.setPalette(com.fpvdrone.client.thermal.PaletteMode.IRONBOW));
        context.waitTicks(3);
        context.takeScreenshot("thermal-fallback-ironbow");
        context.runOnClient(mc -> state.setPalette(com.fpvdrone.client.thermal.PaletteMode.WHITE_HOT));
        world.getServer().runOnServer(server -> world.getConnection().getServerPlayer().setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY));
        context.waitFor(mc -> FPVClientMod.getActiveDroneId(mc.player.getUUID()).isEmpty(), 200);
        context.waitFor(mc -> !state.isEnabled(), 100);
        context.waitTicks(5);
        context.takeScreenshot("thermal-after-exit");
        System.out.println("[smoke] thermal view off after leaving the thermal drone");
    }
}
