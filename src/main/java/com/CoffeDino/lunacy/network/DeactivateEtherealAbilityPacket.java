package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.EtherealAbilityHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeactivateEtherealAbilityPacket() implements CustomPacketPayload {
    public static final Type<DeactivateEtherealAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "deactivate_ethereal_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DeactivateEtherealAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new DeactivateEtherealAbilityPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeactivateEtherealAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                EtherealAbilityHandler.deactivateAbility(serverPlayer);
            }
        });
    }
}