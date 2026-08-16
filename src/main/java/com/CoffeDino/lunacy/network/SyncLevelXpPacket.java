package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.leveling.ClientPlayerLevelData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncLevelXpPacket(int level, int xp, int xpToNextLevel) implements CustomPacketPayload {

    public static final Type<SyncLevelXpPacket> TYPE =
            new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    com.CoffeDino.lunacy.Lunacy.MODID, "sync_level_xp"));

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, SyncLevelXpPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SyncLevelXpPacket::level,
                    ByteBufCodecs.VAR_INT, SyncLevelXpPacket::xp,
                    ByteBufCodecs.VAR_INT, SyncLevelXpPacket::xpToNextLevel,
                    SyncLevelXpPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncLevelXpPacket packet, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> ClientPlayerLevelData.update(packet.level(), packet.xp(), packet.xpToNextLevel()));
    }
}