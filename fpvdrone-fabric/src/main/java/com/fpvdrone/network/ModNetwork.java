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
    }
}
