package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.races.races;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record RaceSelectionPacket(String raceId) implements CustomPacketPayload {
    public static final Type<RaceSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "race_selection")
    );

    public RaceSelectionPacket(FriendlyByteBuf buf){
        this(buf.readUtf());
    }
    public void encode(FriendlyByteBuf buf){
        buf.writeUtf(raceId);
    }

    public static void handle(RaceSelectionPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                for (races.Race race : races.Race.values()) {
                    if (race.getId().equals(packet.raceId())) {
                        races.setPlayerRace(player, race);
                        System.out.println("DEBUG: Race set on server for " + player.getName().getString());
                        break;
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}