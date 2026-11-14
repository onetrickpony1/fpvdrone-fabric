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
                if (recipes != 12) throw new AssertionError("Expected 12 FPV recipes, got " + recipes);
                var drone = ModEntities.DRONE.get().create(player.level(), EntitySpawnReason.COMMAND);
                if (drone == null) throw new AssertionError("Drone creation failed");
                drone.snapTo(player.getX() + 2, player.getY() + 1, player.getZ(), 0, 0);
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
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
            context.waitTicks(2);
            context.takeScreenshot("fpv-flight-hidden");
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
            context.waitTicks(2);
            context.takeScreenshot("fpv-flight-visible-again");
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
        }
    }
}
