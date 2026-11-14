package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import com.fpvdrone.item.FpvGogglesItem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
    modid = "fpvdrone",
    value = {Dist.CLIENT}
)
public class FpvPlayerBodyRenderer {
    private static boolean isRenderingPlayer = false;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == Stage.AFTER_ENTITIES) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                if (mc.screen == null) {
                    if (!FPVMod.isArmed(mc.player.getUUID())) {
                        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
                        if (helmet.getItem() instanceof FpvGogglesItem) {
                            Optional<UUID> linkedId = FpvGogglesItem.getLinkedDroneId(helmet);
                            if (!linkedId.isEmpty()) {
                                if (!FpvStatusChecker.hasNoSignalState(linkedId.get())) {
                                    PoseStack poseStack = event.getPoseStack();
                                    float partialTick = event.getPartialTick();
                                    Vec3 camPos = event.getCamera().getPosition();
                                    double x = mc.player.getX() - camPos.x;
                                    double y = mc.player.getY() - camPos.y;
                                    double z = mc.player.getZ() - camPos.z;
                                    poseStack.pushPose();
                                    poseStack.translate(x, y, z);
                                    EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
                                    BufferSource buffer = mc.renderBuffers().bufferSource();
                                    int light = mc.getEntityRenderDispatcher().getPackedLightCoords(mc.player, partialTick);
                                    isRenderingPlayer = true;
                                    dispatcher.render(mc.player, 0.0, 0.0, 0.0, mc.player.getYRot(), partialTick, poseStack, buffer, light);
                                    isRenderingPlayer = false;
                                    buffer.endBatch();
                                    poseStack.popPose();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
