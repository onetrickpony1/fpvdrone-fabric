package com.fpvdrone.network;

import com.fpvdrone.network.compat.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "3";
    public static SimpleChannel CHANNEL;

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
    }
}
