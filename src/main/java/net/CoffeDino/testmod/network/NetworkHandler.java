package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.races.races;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = ChannelBuilder.named(
            ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "main")
    ).simpleChannel();

    public static void register() {
        INSTANCE.messageBuilder(RaceSelectionPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RaceSelectionPacket::encode)
                .decoder(RaceSelectionPacket::new)
                .consumerMainThread(RaceSelectionPacket::handle)
                .add();

        INSTANCE.messageBuilder(SyncRacePacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncRacePacket::encode)
                .decoder(SyncRacePacket::new)
                .consumerMainThread(SyncRacePacket::handle)
                .add();
        INSTANCE.messageBuilder(RaceSizeSyncPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RaceSizeSyncPacket::encode)
                .decoder(RaceSizeSyncPacket::decode)
                .consumerMainThread((packet, context) -> packet.handle())
                .add();
        INSTANCE.messageBuilder(OpenSculkStoragePacket.class, 4, NetworkDirection.PLAY_TO_SERVER)
                .encoder(OpenSculkStoragePacket::encode)
                .decoder(OpenSculkStoragePacket::new)
                .consumerMainThread(OpenSculkStoragePacket::handle)
                .add();
        INSTANCE.messageBuilder(ActivateWarderAbilityPacket.class, 5, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActivateWarderAbilityPacket::encode)
                .decoder(ActivateWarderAbilityPacket::new)
                .consumerMainThread(ActivateWarderAbilityPacket::handle)
                .add();

        INSTANCE.messageBuilder(DeactivateWarderAbilityPacket.class, 6, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeactivateWarderAbilityPacket::encode)
                .decoder(DeactivateWarderAbilityPacket::new)
                .consumerMainThread(DeactivateWarderAbilityPacket::handle)
                .add();
        INSTANCE.messageBuilder(ActivateLoverAbilityPacket.class, 7, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActivateLoverAbilityPacket::encode)
                .decoder(ActivateLoverAbilityPacket::new)
                .consumerMainThread(ActivateLoverAbilityPacket::handle)
                .add();

        INSTANCE.messageBuilder(DeactivateLoverAbilityPacket.class, 8, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeactivateLoverAbilityPacket::encode)
                .decoder(DeactivateLoverAbilityPacket::new)
                .consumerMainThread(DeactivateLoverAbilityPacket::handle)
                .add();

        INSTANCE.messageBuilder(EnderTeleportPacket.class, 9, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EnderTeleportPacket::encode)
                .decoder(EnderTeleportPacket::new)
                .consumerMainThread(EnderTeleportPacket::handle)
                .add();
        INSTANCE.messageBuilder(ActivatePhantomAbilityPacket.class, 10, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActivatePhantomAbilityPacket::encode)
                .decoder(ActivatePhantomAbilityPacket::new)
                .consumerMainThread(ActivatePhantomAbilityPacket::handle)
                .add();
        INSTANCE.messageBuilder(BelieverAbilityPacket.class, 11, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BelieverAbilityPacket::encode)
                .decoder(BelieverAbilityPacket::new)
                .consumerMainThread(BelieverAbilityPacket::handle)
                .add();
        INSTANCE.messageBuilder(VampirebornAbilityPacket.class, 12, NetworkDirection.PLAY_TO_SERVER)
                .encoder(VampirebornAbilityPacket::encode)
                .decoder(VampirebornAbilityPacket::new)
                .consumerMainThread(VampirebornAbilityPacket::handle)
                .add();

        INSTANCE.messageBuilder(DeactivateVampirebornAbilityPacket.class, 13, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeactivateVampirebornAbilityPacket::encode)
                .decoder(DeactivateVampirebornAbilityPacket::new)
                .consumerMainThread(DeactivateVampirebornAbilityPacket::handle)
                .add();
        INSTANCE.messageBuilder(ActivateEtherealAbilityPacket.class, 14, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActivateEtherealAbilityPacket::encode)
                .decoder(ActivateEtherealAbilityPacket::new)
                .consumerMainThread(ActivateEtherealAbilityPacket::handle)
                .add();
        INSTANCE.messageBuilder(UpdateEtherealInputPacket.class, 15, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateEtherealInputPacket::encode)
                .decoder(UpdateEtherealInputPacket::new)
                .consumerMainThread(UpdateEtherealInputPacket::handle)
                .add();
        INSTANCE.messageBuilder(ActivateAngelbornAbilityPacket.class, 16, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActivateAngelbornAbilityPacket::encode)
                .decoder(ActivateAngelbornAbilityPacket::new)
                .consumerMainThread(ActivateAngelbornAbilityPacket::handle)
                .add();
        INSTANCE.messageBuilder(ActivateCelestialAbilityPacket.class, 17, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActivateCelestialAbilityPacket::encode)
                .decoder(ActivateCelestialAbilityPacket::new)
                .consumerMainThread(ActivateCelestialAbilityPacket::handle)
                .add();

        INSTANCE.messageBuilder(CelestialPushPacket.class, 18, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CelestialPushPacket::encode)
                .decoder(CelestialPushPacket::new)
                .consumerMainThread(CelestialPushPacket::handle)
                .add();

        INSTANCE.messageBuilder(CelestialPullPacket.class, 19, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CelestialPullPacket::encode)
                .decoder(CelestialPullPacket::new)
                .consumerMainThread(CelestialPullPacket::handle)
                .add();
        INSTANCE.messageBuilder(DeactivateCelestialAbilityPacket.class, 20, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeactivateCelestialAbilityPacket::encode)
                .decoder(DeactivateCelestialAbilityPacket::new)
                .consumerMainThread(DeactivateCelestialAbilityPacket::handle)
                .add();
    }

    public static <T extends CustomPacketPayload> void sendToServer(T message) {
        INSTANCE.send(message, PacketDistributor.SERVER.noArg());
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static void syncRaceToClient(ServerPlayer player, races.Race race) {
        String raceId = race != null ? race.getId() : "";
        sendToPlayer(new SyncRacePacket(raceId), player);
    }

    public static void syncSizeToClient(ServerPlayer player, float height, float width) {
        INSTANCE.send(new RaceSizeSyncPacket(height, width), PacketDistributor.PLAYER.with(player));
    }
    public static void openSculkStorage() {
        INSTANCE.send(new OpenSculkStoragePacket(), PacketDistributor.SERVER.noArg());
    }
    public static void triggerEnderTeleport() {
        INSTANCE.send(new EnderTeleportPacket(), PacketDistributor.SERVER.noArg());
    }
    public static void triggerBelieverAbility() {
        INSTANCE.send(new BelieverAbilityPacket(), PacketDistributor.SERVER.noArg());
    }
}