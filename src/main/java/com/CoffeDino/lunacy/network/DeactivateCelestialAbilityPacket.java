package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.CelestialAbilityHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeactivateCelestialAbilityPacket() implements CustomPacketPayload {
    public static final Type<DeactivateCelestialAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "deactivate_celestial_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, DeactivateCelestialAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new DeactivateCelestialAbilityPacket());

    public static void handle(DeactivateCelestialAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                CelestialAbilityHandler.deactivateAbility(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}