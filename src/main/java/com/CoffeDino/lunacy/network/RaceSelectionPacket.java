package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RaceSelectionPacket(String raceId) implements CustomPacketPayload {
    public static final Type<RaceSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "race_selection")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, RaceSelectionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RaceSelectionPacket::raceId,
            RaceSelectionPacket::new
    );

    public static void handle(RaceSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                for (races.Race race : races.Race.values()) {
                    if (race.getId().equals(packet.raceId())) {
                        races.setPlayerRace(player, race);
                        Lunacy.LOGGER.debug("DEBUG: Race set on server for " + player.getName().getString());
                        break;
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