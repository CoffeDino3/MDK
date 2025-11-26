package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.classes.PlayerClasses;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record ClassSelectionPacket(String classId) implements CustomPacketPayload {
    public static final Type<ClassSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "class_selection")
    );

    public ClassSelectionPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(classId);
    }

    public static void handle(ClassSelectionPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                for (PlayerClasses.PlayerClass playerClass : PlayerClasses.PlayerClass.values()) {
                    if (playerClass.getId().equals(packet.classId())) {
                        PlayerClasses.setPlayerClass(player, playerClass);
                        System.out.println("DEBUG: Class set on server for " + player.getName().getString());
                        break;
                    }
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