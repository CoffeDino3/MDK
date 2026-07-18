package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.AngelbornAbilityHandler;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActivateAngelbornAbilityPacket() implements CustomPacketPayload {
    public static final Type<ActivateAngelbornAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "activate_angelborn_ability")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, ActivateAngelbornAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new ActivateAngelbornAbilityPacket());

    public static void handle(ActivateAngelbornAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AngelbornAbilityHandler.activateAbility(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}