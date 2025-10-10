package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
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
    }

    public static <T extends CustomPacketPayload> void sendToServer(T message) {
        INSTANCE.send(message, PacketDistributor.SERVER.noArg());
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }
}