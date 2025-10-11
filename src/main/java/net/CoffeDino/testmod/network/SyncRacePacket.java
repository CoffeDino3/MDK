package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.races.races;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record SyncRacePacket(String raceId) implements CustomPacketPayload {
    public static final Type<SyncRacePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "sync_race")
    );

    public SyncRacePacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(raceId);
    }

    public static void handle(SyncRacePacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                String raceId = packet.raceId();
                if (raceId.isEmpty()) {
                    // Clear the race if empty string
                    races.setClientRace(null);
                    System.out.println("DEBUG: Cleared race on client");
                } else {
                    for (races.Race race : races.Race.values()) {
                        if (race.getId().equals(raceId)) {
                            races.setClientRace(race);
                            System.out.println("DEBUG: Synced race to client: " + race.getDisplayName());
                            break;
                        }
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