package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.VampirebornAbilityHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VampirebornAbilityPacket(boolean isHoldStart) implements CustomPacketPayload {
    public static final Type<VampirebornAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "vampireborn_ability")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, VampirebornAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    VampirebornAbilityPacket::isHoldStart,
                    VampirebornAbilityPacket::new
            );

    public static void handle(VampirebornAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (packet.isHoldStart()) {
                    VampirebornAbilityHandler.startHoldingAbility(player);
                } else {
                    VampirebornAbilityHandler.fireSingleShot(player);
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}