package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record ActivatePhantomAbilityPacket() implements CustomPacketPayload {
    public static final Type<ActivatePhantomAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "activate_phantom_ability")
    );

    public ActivatePhantomAbilityPacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(ActivatePhantomAbilityPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                net.CoffeDino.testmod.abilities.PhantomAbilityHandler.activateAbility(player);
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}