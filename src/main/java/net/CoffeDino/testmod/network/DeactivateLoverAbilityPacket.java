package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record DeactivateLoverAbilityPacket() implements CustomPacketPayload {
    public static final Type<DeactivateLoverAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "deactivate_lover_ability")
    );

    public DeactivateLoverAbilityPacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(DeactivateLoverAbilityPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                net.CoffeDino.testmod.abilities.LoverAbilityHandler.deactivateAbility(player);
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}