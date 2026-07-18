package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncRacePacket(String raceId) implements CustomPacketPayload {
    public static final Type<SyncRacePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "sync_race")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, SyncRacePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncRacePacket::raceId,
            SyncRacePacket::new
    );

    public static void handle(SyncRacePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                String raceId = packet.raceId();
                if (raceId.isEmpty()) {
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
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}