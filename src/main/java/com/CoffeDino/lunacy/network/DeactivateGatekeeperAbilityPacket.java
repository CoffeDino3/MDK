package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.GatekeeperAbilityHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeactivateGatekeeperAbilityPacket() implements CustomPacketPayload {
    public static final Type<DeactivateGatekeeperAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "deactivate_gatekeeper_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, DeactivateGatekeeperAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new DeactivateGatekeeperAbilityPacket());

    public static void handle(DeactivateGatekeeperAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                GatekeeperAbilityHandler.deactivateAbility(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}