// DeactivateCelestialAbilityPacket.java
package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record DeactivateCelestialAbilityPacket() implements CustomPacketPayload {
    public static final Type<DeactivateCelestialAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "deactivate_celestial_ability")
    );

    public DeactivateCelestialAbilityPacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
        // No data needed
    }

    public static void handle(DeactivateCelestialAbilityPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                net.CoffeDino.testmod.abilities.CelestialAbilityHandler.deactivateAbility(player);
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}