package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.BelieverAbilityHandler;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BelieverAbilityPacket(boolean isShiftDown) implements CustomPacketPayload {
    public static final Type<BelieverAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "believer_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, BelieverAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, BelieverAbilityPacket::isShiftDown,
                    BelieverAbilityPacket::new
            );

    public static void handle(BelieverAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BelieverAbilityHandler.toggleAbility(player, packet.isShiftDown());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}