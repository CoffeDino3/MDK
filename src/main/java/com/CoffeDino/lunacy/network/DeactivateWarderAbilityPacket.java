package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.WarderAbilityHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeactivateWarderAbilityPacket() implements CustomPacketPayload {
    public static final Type<DeactivateWarderAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "deactivate_warder_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, DeactivateWarderAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new DeactivateWarderAbilityPacket());

    public static void handle(DeactivateWarderAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                WarderAbilityHandler.deactivateAbility(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}