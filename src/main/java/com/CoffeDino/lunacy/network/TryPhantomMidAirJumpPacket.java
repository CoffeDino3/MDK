package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.PhantomAbilityHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TryPhantomMidAirJumpPacket() implements CustomPacketPayload {
    public static final Type<TryPhantomMidAirJumpPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "try_phantom_mid_air_jump")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, TryPhantomMidAirJumpPacket> STREAM_CODEC =
            StreamCodec.unit(new TryPhantomMidAirJumpPacket());

    public static void handle(TryPhantomMidAirJumpPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PhantomAbilityHandler.tryMidAirJump(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
