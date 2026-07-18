package com.CoffeDino.lunacy.player;

import com.CoffeDino.lunacy.Lunacy;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Lunacy.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ReaperSoulData>> REAPER_SOUL_DATA =
            ATTACHMENT_TYPES.register("reaper_soul_data", () -> AttachmentType
                    .builder(ReaperSoulData::new)
                    .serialize(ReaperSoulData.CODEC)
                    .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}