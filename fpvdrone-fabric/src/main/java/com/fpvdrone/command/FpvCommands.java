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
                .then(
                    Commands.literal("wind")
                        .executes(FpvCommands::showWind)
                        .then(Commands.literal("on").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(ctx -> setWind(ctx, true, com.fpvdrone.FpvConfig.getWindStrength())))
                        .then(Commands.literal("off").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(ctx -> setWind(ctx, false, com.fpvdrone.FpvConfig.getWindStrength())))
                        .then(Commands.literal("strength").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .then(Commands.argument("multiplier", com.mojang.brigadier.arguments.FloatArgumentType.floatArg(0.0F, 3.0F))
                                .executes(ctx -> setWind(ctx, true, com.mojang.brigadier.arguments.FloatArgumentType.getFloat(ctx, "multiplier")))))
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

    /** Port addition: the wind where the command runs (docs/WIND.md). */
    private static int showWind(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var level = src.getLevel();
        var p = src.getPosition();
        org.joml.Vector3f base = com.fpvdrone.physics.wind.FpvWind.base(level, p.x, p.y, p.z, new org.joml.Vector3f());
        float shelter = com.fpvdrone.physics.wind.FpvWind.shelter(level, p.x, p.y, p.z, base);
        org.joml.Vector3f here = com.fpvdrone.physics.wind.FpvWind.at(level, p.x, p.y, p.z, shelter, new org.joml.Vector3f());
        float heading = (float)Math.toDegrees(Math.atan2(-here.x, here.z));
        String fmt = "%.1f";
        src.sendSuccess(() -> Component.translatable("commands.fpvdrone.wind.show",
            String.format(java.util.Locale.ROOT, fmt, here.length()), Math.round((heading + 360) % 360),
            String.format(java.util.Locale.ROOT, fmt, base.length()), Math.round(shelter * 100),
            !com.fpvdrone.FpvConfig.isWindEnabled() ? "off" : com.fpvdrone.physics.wind.FpvWind.fromWeathery() ? "Weathery" : "built-in"), false);
        return 1;
    }

    private static int setWind(CommandContext<CommandSourceStack> ctx, boolean enabled, float strength) {
        com.fpvdrone.FpvConfig.setWind(enabled, strength);
        com.fpvdrone.FpvConfig.save();
        for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ConfigSyncPacket());
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.fpvdrone.wind.set", enabled ? "on" : "off", com.fpvdrone.FpvConfig.getWindStrength()), true);
        return 1;
    }
}
