// BelieverAbilityPacket.java
package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;

import static net.CoffeDino.testmod.abilities.BelieverAbilityHandler.deactivateAbility;

public record BelieverAbilityPacket() implements CustomPacketPayload {
    public static final Type<BelieverAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "believer_ability")
    );

    public BelieverAbilityPacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(BelieverAbilityPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                net.CoffeDino.testmod.abilities.BelieverAbilityHandler.toggleAbility(player);
            }
        });
        context.setPacketHandled(true);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}