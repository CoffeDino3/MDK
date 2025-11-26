package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record ActivateWarderAbilityPacket() implements CustomPacketPayload {
    public static final Type<ActivateWarderAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "activate_warder_ability")
    );

    public ActivateWarderAbilityPacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(ActivateWarderAbilityPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                net.CoffeDino.testmod.abilities.WarderAbilityHandler.activateAbility(player);
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}