package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record VampirebornAbilityPacket(boolean isHoldStart) implements CustomPacketPayload {
    public static final Type<VampirebornAbilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "vampireborn_ability")
    );

    public VampirebornAbilityPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isHoldStart);
    }

    public static void handle(VampirebornAbilityPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (packet.isHoldStart()) {
                    net.CoffeDino.testmod.abilities.VampirebornAbilityHandler.startHoldingAbility(player);
                } else {
                    net.CoffeDino.testmod.abilities.VampirebornAbilityHandler.fireSingleShot(player);
                }
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}