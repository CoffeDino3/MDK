package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.classes.PlayerClasses;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record SyncClassPacket(String classId) implements CustomPacketPayload {
    public static final Type<SyncClassPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "sync_class")
    );

    public SyncClassPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(classId);
    }

    public static void handle(SyncClassPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                PlayerClasses.PlayerClass playerClass = null;
                if (!packet.classId.isEmpty()) {
                    for (PlayerClasses.PlayerClass pc : PlayerClasses.PlayerClass.values()) {
                        if (pc.getId().equals(packet.classId)) {
                            playerClass = pc;
                            break;
                        }
                    }
                }
                PlayerClasses.setClientClass(playerClass);
                System.out.println("DEBUG: Synced class to client: " + (playerClass != null ? playerClass.getDisplayName() : "null"));
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}