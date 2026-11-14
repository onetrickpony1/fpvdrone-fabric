package com.fpvdrone.network;

import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.SimpleChannel;
import net.minecraft.server.level.ServerPlayer;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "3";
    public static SimpleChannel CHANNEL;

    /** 1.1.8 helper: one message to one player. */
    public static <MSG> void sendToPlayer(ServerPlayer player, MSG msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    /** 1.1.8 helper: to every player within the range of a point in a level. */
    public static <MSG> void sendNear(net.minecraft.server.level.ServerLevel level, double x, double y, double z, double range, MSG msg) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(null, x, y, z, range, level.dimension())), msg);
    }

    /** 1.1.8: to everyone within the server's view distance (+1 chunk) of a point. */
    public static <MSG> void sendNearViewDistance(net.minecraft.server.level.ServerLevel level, double x, double y, double z, MSG msg) {
        double range = (level.getServer().getPlayerList().getViewDistance() + 1) * 16.0;
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, range, level.dimension())), msg);
    }

    public static void register() {
        CHANNEL = new SimpleChannel();
        int id = 0;
        CHANNEL.registerMessage(id++, DroneSyncPacket.class, DroneSyncPacket::encode, DroneSyncPacket::decode, DroneSyncPacket::handle, true);
        CHANNEL.registerMessage(
            id++, DroneStateBroadcastPacket.class, DroneStateBroadcastPacket::encode, DroneStateBroadcastPacket::decode, DroneStateBroadcastPacket::handle, false
        );
        CHANNEL.registerMessage(id++, DroneExplodePacket.class, DroneExplodePacket::encode, DroneExplodePacket::decode, DroneExplodePacket::handle, true);
        CHANNEL.registerMessage(id++, ExitFpvPacket.class, ExitFpvPacket::encode, ExitFpvPacket::decode, ExitFpvPacket::handle, true);
        CHANNEL.registerMessage(
            id++, RequestDronePositionPacket.class, RequestDronePositionPacket::encode, RequestDronePositionPacket::decode, RequestDronePositionPacket::handle, true
        );
        CHANNEL.registerMessage(
            id++,
            DronePositionResponsePacket.class,
            DronePositionResponsePacket::encode,
            DronePositionResponsePacket::decode,
            DronePositionResponsePacket::handle, false
        );
        CHANNEL.registerMessage(id++, ForceChunkPacket.class, ForceChunkPacket::encode, ForceChunkPacket::decode, ForceChunkPacket::handle, true);
        CHANNEL.registerMessage(
            id++, SaveDronePositionPacket.class, SaveDronePositionPacket::encode, SaveDronePositionPacket::decode, SaveDronePositionPacket::handle, true
        );
        CHANNEL.registerMessage(id++, DroneConfigPacket.class, DroneConfigPacket::encode, DroneConfigPacket::decode, DroneConfigPacket::handle, true);
        CHANNEL.registerMessage(id++, RemoveGogglesPacket.class, RemoveGogglesPacket::encode, RemoveGogglesPacket::decode, RemoveGogglesPacket::handle, true);
        id++;
        CHANNEL.registerMessage(id++, EnterFpvPacket.class, EnterFpvPacket::encode, EnterFpvPacket::decode, EnterFpvPacket::handle, true);
        CHANNEL.registerMessage(id++, PlayerTeleportPacket.class, PlayerTeleportPacket::encode, PlayerTeleportPacket::decode, PlayerTeleportPacket::handle, false);
        CHANNEL.registerMessage(id++, FpvStatePacket.class, FpvStatePacket::encode, FpvStatePacket::decode, FpvStatePacket::handle, false);
        CHANNEL.registerMessage(id++, DroneDamagePacket.class, DroneDamagePacket::encode, DroneDamagePacket::decode, DroneDamagePacket::handle, false);
        CHANNEL.registerMessage(id++, DroneHitKickPacket.class, DroneHitKickPacket::encode, DroneHitKickPacket::decode, DroneHitKickPacket::handle, false);
        CHANNEL.registerMessage(id++, OsdLayoutPacket.class, OsdLayoutPacket::encode, OsdLayoutPacket::decode, OsdLayoutPacket::handle, true);
        CHANNEL.registerMessage(id++, DroneDestroyedPacket.class, DroneDestroyedPacket::encode, DroneDestroyedPacket::decode, DroneDestroyedPacket::handle, false);
        CHANNEL.registerMessage(
            id++, ChangeGogglesChannelPacket.class, ChangeGogglesChannelPacket::encode, ChangeGogglesChannelPacket::decode, ChangeGogglesChannelPacket::handle, true
        );
        CHANNEL.registerMessage(
            id++,
            ChannelChangedResponsePacket.class,
            ChannelChangedResponsePacket::encode,
            ChannelChangedResponsePacket::decode,
            ChannelChangedResponsePacket::handle, false
        );
        CHANNEL.registerMessage(id++, SwitchDronePacket.class, SwitchDronePacket::encode, SwitchDronePacket::decode, SwitchDronePacket::handle, true);
        CHANNEL.registerMessage(id++, DroneEntityHitPacket.class, DroneEntityHitPacket::encode, DroneEntityHitPacket::decode, DroneEntityHitPacket::handle, true);
        CHANNEL.registerMessage(id++, DroneCrashPacket.class, DroneCrashPacket::encode, DroneCrashPacket::decode, DroneCrashPacket::handle, true);
        CHANNEL.registerMessage(id++, BatterySyncPacket.class, BatterySyncPacket::encode, BatterySyncPacket::decode, BatterySyncPacket::handle, true);
        CHANNEL.registerMessage(id++, ConfigSyncPacket.class, ConfigSyncPacket::encode, ConfigSyncPacket::decode, ConfigSyncPacket::handle, false);
        CHANNEL.registerMessage(
            id++, FpvEntryRejectedPacket.class, FpvEntryRejectedPacket::encode, FpvEntryRejectedPacket::decode, FpvEntryRejectedPacket::handle, false
        );
        CHANNEL.registerMessage(
            id++, DestroyedDronesSyncPacket.class, DestroyedDronesSyncPacket::encode, DestroyedDronesSyncPacket::decode, DestroyedDronesSyncPacket::handle, false
        );
        CHANNEL.registerMessage(
            id++,
            ExplosionSoundPacket.class,
            ExplosionSoundPacket::encode,
            ExplosionSoundPacket::decode,
            ExplosionSoundPacket::handle, false
        );
        CHANNEL.registerMessage(id++, DroneSignalLostPacket.class, DroneSignalLostPacket::encode, DroneSignalLostPacket::decode, DroneSignalLostPacket::handle, true);
        CHANNEL.registerMessage(id++, DefinitionSyncPacket.class, DefinitionSyncPacket::encode, DefinitionSyncPacket::decode, DefinitionSyncPacket::handle, false);
        CHANNEL.registerMessage(id++, ShrapnelDecalPacket.class, ShrapnelDecalPacket::encode, ShrapnelDecalPacket::decode, ShrapnelDecalPacket::handle, false);
        CHANNEL.registerMessage(id++, ProjectileBurstPacket.class, ProjectileBurstPacket::encode, ProjectileBurstPacket::decode, ProjectileBurstPacket::handle, false);
        CHANNEL.registerMessage(id++, RemoteStatePacket.class, RemoteStatePacket::encode, RemoteStatePacket::decode, RemoteStatePacket::handle, false);
        CHANNEL.registerMessage(id++, LaunchSoundPacket.class, LaunchSoundPacket::encode, LaunchSoundPacket::decode, LaunchSoundPacket::handle, false);
        // 1.1.8 dropped bomb
        CHANNEL.registerMessage(id++, BombDeflectPacket.class, BombDeflectPacket::encode, BombDeflectPacket::decode, BombDeflectPacket::handle, false);
        CHANNEL.registerMessage(id++, DropPayloadPacket.class, DropPayloadPacket::encode, DropPayloadPacket::decode, DropPayloadPacket::handle, true);
        CHANNEL.registerMessage(id++, BombBayDropPacket.class, BombBayDropPacket::encode, BombBayDropPacket::decode, BombBayDropPacket::handle, true);
        // 1.1.8 loitering munitions, tactical map and recon
        CHANNEL.registerMessage(id++, MunitionControlPacket.class, MunitionControlPacket::encode, MunitionControlPacket::decode, MunitionControlPacket::handle, true);
        CHANNEL.registerMessage(id++, MunitionStickPacket.class, MunitionStickPacket::encode, MunitionStickPacket::decode, MunitionStickPacket::handle, true);
        CHANNEL.registerMessage(id++, ExitMunitionViewPacket.class, ExitMunitionViewPacket::encode, ExitMunitionViewPacket::decode, ExitMunitionViewPacket::handle, true);
        CHANNEL.registerMessage(id++, EnterMunitionViewPacket.class, EnterMunitionViewPacket::encode, EnterMunitionViewPacket::decode, EnterMunitionViewPacket::handle, true);
        CHANNEL.registerMessage(id++, MunitionPacketB.class, MunitionPacketB::encode, MunitionPacketB::decode, MunitionPacketB::handle, true);
        CHANNEL.registerMessage(id++, MunitionClientStatePacket.class, MunitionClientStatePacket::encode, MunitionClientStatePacket::decode, MunitionClientStatePacket::handle, false);
        CHANNEL.registerMessage(id++, MunitionViewEndedPacket.class, MunitionViewEndedPacket::encode, MunitionViewEndedPacket::decode, MunitionViewEndedPacket::handle, false);
        CHANNEL.registerMessage(id++, RouteAssignPacket.class, RouteAssignPacket::encode, RouteAssignPacket::decode, RouteAssignPacket::handle, true);
        CHANNEL.registerMessage(id++, MapTileRequestPacket.class, MapTileRequestPacket::encode, MapTileRequestPacket::decode, MapTileRequestPacket::handle, true);
        CHANNEL.registerMessage(id++, MapTileBatchPacket.class, MapTileBatchPacket::encode, MapTileBatchPacket::decode, MapTileBatchPacket::handle, false);
        CHANNEL.registerMessage(id++, MapTileEmptyPacket.class, MapTileEmptyPacket::encode, MapTileEmptyPacket::decode, MapTileEmptyPacket::handle, false);
        CHANNEL.registerMessage(id++, ReconMarkPacket.class, ReconMarkPacket::encode, ReconMarkPacket::decode, ReconMarkPacket::handle, false);
        CHANNEL.registerMessage(id++, ReconTilePacket.class, ReconTilePacket::encode, ReconTilePacket::decode, ReconTilePacket::handle, false);
    }
}
