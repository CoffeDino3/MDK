package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.PhantomAbilityHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActivatePhantomAbilityPacket() implements CustomPacketPayload {
    public static final Type<ActivatePhantomAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "activate_phantom_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, ActivatePhantomAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new ActivatePhantomAbilityPacket());

    public static void handle(ActivatePhantomAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PhantomAbilityHandler.activateAbility(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}