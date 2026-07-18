package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.WarderAbilityHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActivateWarderAbilityPacket() implements CustomPacketPayload {
    public static final Type<ActivateWarderAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "activate_warder_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, ActivateWarderAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new ActivateWarderAbilityPacket());

    public static void handle(ActivateWarderAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                WarderAbilityHandler.activateAbility(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}