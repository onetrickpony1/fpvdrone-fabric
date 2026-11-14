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
            var droneId = world.getServer().computeOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                long recipes = server.getRecipeManager().getRecipes().stream()
                    .filter(recipe -> recipe.id().identifier().getNamespace().equals("fpvdrone")).count();
                if (recipes != 13) throw new AssertionError("Expected 13 FPV recipes (12 original + HD camera), got " + recipes);
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
            double landedY = context.computeOnClient(mc -> mc.player.getY());
            for (int i = 0; i < 20; i++) {
                context.waitTicks(1);
                double y = context.computeOnClient(mc -> mc.player.getY());
                if (Math.abs(y - landedY) > 0.5) throw new AssertionError("Player moved after flight exit: " + landedY + " -> " + y);
            }
            System.out.println("[smoke] player stays at the pilot spot after exit");
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
                player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                player.setShiftKeyDown(true);
                player.interactOn(drone, net.minecraft.world.InteractionHand.MAIN_HAND, drone.position());
                player.setShiftKeyDown(false);
                if (drone.hasHdCamera() || player.getInventory().countItem(ModItems.HD_CAMERA.get()) != 1) throw new AssertionError("HD camera was not given back");
            });
            System.out.println("[smoke] HD camera fits, refuses a second, stays in the drone item, comes off with Shift");
        }
    }
}
