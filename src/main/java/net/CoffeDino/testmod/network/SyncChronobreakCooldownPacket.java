package net.CoffeDino.testmod.network;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.item.Custom.ViridyumGreatswordItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record SyncChronobreakCooldownPacket(long cooldownEnd) implements CustomPacketPayload {
    public static final Type<SyncChronobreakCooldownPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "sync_chronobreak_cooldown")
    );

    public SyncChronobreakCooldownPacket(FriendlyByteBuf buf) {
        this(buf.readLong());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(cooldownEnd);
    }

    public static void handle(SyncChronobreakCooldownPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ViridyumGreatswordItem.setClientCooldown(packet.cooldownEnd());
        });
        context.setPacketHandled(true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}