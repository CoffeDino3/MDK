package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClassSelectionPacket(String classId) implements CustomPacketPayload {
    public static final Type<ClassSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "class_selection")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, ClassSelectionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClassSelectionPacket::classId,
            ClassSelectionPacket::new
    );

    public static void handle(ClassSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                for (PlayerClasses.PlayerClass playerClass : PlayerClasses.PlayerClass.values()) {
                    if (playerClass.getId().equals(packet.classId())) {
                        PlayerClasses.setPlayerClass(player, playerClass);
                        System.out.println("DEBUG: Class set on server for " + player.getName().getString());
                        break;
                    }
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}