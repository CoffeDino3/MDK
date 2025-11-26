package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.capability.SculkStorageProvider;
import net.CoffeDino.testmod.menu.SculkStorageMenu;
import net.CoffeDino.testmod.menu.SculkStorageMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record OpenSculkStoragePacket() implements CustomPacketPayload {
    public static final Type<OpenSculkStoragePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "open_sculk_storage")
    );

    public OpenSculkStoragePacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(OpenSculkStoragePacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(Lunacy.SCULK_STORAGE).ifPresent(storage -> {
                    if (!storage.isOnCooldown()) {
                        player.openMenu(new SculkStorageMenuProvider(player));
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}