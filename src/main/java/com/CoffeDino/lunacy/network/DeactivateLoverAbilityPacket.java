package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.LoverAbilityHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeactivateLoverAbilityPacket() implements CustomPacketPayload {
    public static final Type<DeactivateLoverAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "deactivate_lover_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, DeactivateLoverAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new DeactivateLoverAbilityPacket());

    public static void handle(DeactivateLoverAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                LoverAbilityHandler.deactivateAbility(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}