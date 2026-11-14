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
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            context.setScreen(com.fpvdrone.client.gui.FlightSettingsScreen::new);
            context.takeScreenshot("fpv-controls");
            context.clickScreenButton("Done");
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
            double startY = context.computeOnClient(mc -> mc.player.getY());
            context.getInput().holdKey(com.mojang.blaze3d.platform.InputConstants.KEY_W);
            context.waitFor(mc -> mc.player.getY() > startY + 2.0, 400);
            context.getInput().releaseKey(com.mojang.blaze3d.platform.InputConstants.KEY_W);
            context.takeScreenshot("fpv-flight");
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
        }
    }
}
