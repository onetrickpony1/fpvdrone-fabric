package com.fpvdrone.command;

import com.fpvdrone.FpvConfig;
import com.fpvdrone.network.ConfigSyncPacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.compat.PacketDistributor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /fpvdrones explosive enable|disable} (operators): server-wide switch for block-breaking explosions (1.1.8). */
public final class FpvCommands {
    private FpvCommands() {
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("fpvdrones")
                .then(
                    Commands.literal("explosive")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("enable").executes(ctx -> setExplosive(ctx, true)))
                        .then(Commands.literal("disable").executes(ctx -> setExplosive(ctx, false)))
                )
        );
    }

    private static int setExplosive(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        FpvConfig.setExplosionBreaksBlocks(enabled);

        for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ConfigSyncPacket());
        }

        ctx.getSource()
            .sendSuccess(() -> Component.translatable(enabled ? "commands.fpvdrone.explosive.enabled" : "commands.fpvdrone.explosive.disabled"), true);
        return 1;
    }
}
