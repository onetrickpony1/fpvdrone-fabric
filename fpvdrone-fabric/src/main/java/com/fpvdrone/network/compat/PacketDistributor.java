package com.fpvdrone.network.compat;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** The subset of Forge's PacketDistributor this mod uses, resolved to concrete player lists. */
public final class PacketDistributor<T> {
    public static final PacketDistributor<ServerPlayer> PLAYER = new PacketDistributor<>(p -> p == null ? List.of() : List.of(p));
    public static final PacketDistributor<Entity> TRACKING_ENTITY = new PacketDistributor<>(PlayerLookup::tracking);
    public static final PacketDistributor<TargetPoint> NEAR = new PacketDistributor<>(PacketDistributor::near);

    private final java.util.function.Function<T, Collection<ServerPlayer>> resolver;

    private PacketDistributor(java.util.function.Function<T, Collection<ServerPlayer>> resolver) {
        this.resolver = resolver;
    }

    public PacketTarget with(Supplier<T> input) {
        return () -> this.resolver.apply(input.get());
    }

    private static Collection<ServerPlayer> near(TargetPoint p) {
        var server = ServerHolder.server();
        if (server == null) {
            return List.of();
        }
        ServerLevel level = server.getLevel(p.dim());
        if (level == null) {
            return List.of();
        }
        return PlayerLookup.around(level, new Vec3(p.x(), p.y(), p.z()), p.r()).stream()
            .filter(sp -> sp != p.excluded())
            .toList();
    }

    @FunctionalInterface
    public interface PacketTarget {
        Collection<ServerPlayer> players();
    }

    public record TargetPoint(ServerPlayer excluded, double x, double y, double z, double r, ResourceKey<Level> dim) {
        public TargetPoint(double x, double y, double z, double r, ResourceKey<Level> dim) {
            this(null, x, y, z, r, dim);
        }
    }
}
