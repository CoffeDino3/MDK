package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncClassPacket(String classId) implements CustomPacketPayload {
    public static final Type<SyncClassPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "sync_class")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, SyncClassPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncClassPacket::classId,
            SyncClassPacket::new
    );

    public static void handle(SyncClassPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                PlayerClasses.PlayerClass playerClass = null;
                if (!packet.classId().isEmpty()) {
                    for (PlayerClasses.PlayerClass pc : PlayerClasses.PlayerClass.values()) {
                        if (pc.getId().equals(packet.classId())) {
                            playerClass = pc;
                            break;
                        }
                    }
                }
                PlayerClasses.setClientClass(playerClass);
                Lunacy.LOGGER.debug("DEBUG: Synced class to client: " + (playerClass != null ? playerClass.getDisplayName() : "null"));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}