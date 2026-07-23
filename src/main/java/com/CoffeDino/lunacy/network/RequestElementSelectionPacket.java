package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.classes.ClassDataManager;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestElementSelectionPacket() implements CustomPacketPayload {
    public static final Type<RequestElementSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "request_element_selection")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, RequestElementSelectionPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestElementSelectionPacket());

    public static void handle(RequestElementSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (PlayerClasses.getPlayerClass(player) != PlayerClasses.PlayerClass.SPELLBLADE) {
                    Lunacy.LOGGER.debug("DEBUG: Rejected element re-selection request - player is not a Spellblade: " + player.getName().getString());
                    return;
                }

                ClassDataManager dataManager = ClassDataManager.get(player);
                String existingElement = dataManager.getPlayerElement(player.getUUID());
                if (existingElement != null && !existingElement.isEmpty()) {
                    Lunacy.LOGGER.debug("DEBUG: Rejected element re-selection request - player already has an element: " + player.getName().getString());
                    return;
                }

                NetworkHandler.openElementSelectionForPlayer(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}