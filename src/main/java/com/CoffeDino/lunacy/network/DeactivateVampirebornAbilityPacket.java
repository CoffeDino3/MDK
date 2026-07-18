package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.VampirebornAbilityHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeactivateVampirebornAbilityPacket() implements CustomPacketPayload {
    public static final Type<DeactivateVampirebornAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "deactivate_vampireborn_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, DeactivateVampirebornAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new DeactivateVampirebornAbilityPacket());

    public static void handle(DeactivateVampirebornAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                VampirebornAbilityHandler.releaseHeldAbility(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}