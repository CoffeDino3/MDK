package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.EtherealAbilityHandler;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActivateEtherealAbilityPacket(boolean jumping, boolean shifting) implements CustomPacketPayload {
    public static final Type<ActivateEtherealAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "activate_ethereal_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, ActivateEtherealAbilityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ActivateEtherealAbilityPacket::jumping,
            ByteBufCodecs.BOOL, ActivateEtherealAbilityPacket::shifting,
            ActivateEtherealAbilityPacket::new
    );

    public static void handle(ActivateEtherealAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                EtherealAbilityHandler.activateAbility(player, packet.jumping(), packet.shifting());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}