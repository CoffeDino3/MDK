package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.classes.ClassDataManager;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.classes.SpellbladeElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ElementSelectionPacket(String elementId) implements CustomPacketPayload {
    public static final Type<ElementSelectionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "element_selection")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, ElementSelectionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ElementSelectionPacket::elementId,
            ElementSelectionPacket::new
    );

    public static void handle(ElementSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SpellbladeElement element = SpellbladeElement.fromId(packet.elementId());
                if (element == null) return;

                if (PlayerClasses.getPlayerClass(player) != PlayerClasses.PlayerClass.SPELLBLADE) {
                    Lunacy.LOGGER.debug("DEBUG: Rejected element selection - player is not a Spellblade: " + player.getName().getString());
                    return;
                }

                ClassDataManager dataManager = ClassDataManager.get(player);
                dataManager.setPlayerElement(player.getUUID(), element.getId());
                Lunacy.LOGGER.debug("DEBUG: Element set on server for " + player.getName().getString() + ": " + element.getDisplayName());
                player.sendSystemMessage(Component.literal("Your element is: " + element.getDisplayName()));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}