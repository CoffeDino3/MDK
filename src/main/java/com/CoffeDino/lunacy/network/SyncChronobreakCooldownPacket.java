package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.item.Custom.ViridyumGreatswordItem;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncChronobreakCooldownPacket(long cooldownEnd) implements CustomPacketPayload {
    public static final Type<SyncChronobreakCooldownPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "sync_chronobreak_cooldown")
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, SyncChronobreakCooldownPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SyncChronobreakCooldownPacket::cooldownEnd,
            SyncChronobreakCooldownPacket::new
    );

    public static void handle(SyncChronobreakCooldownPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ViridyumGreatswordItem.setClientCooldown(packet.cooldownEnd());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}