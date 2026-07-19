package com.CoffeDino.lunacy.capability;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.player.ReaperSoulData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.mojang.serialization.Codec; // ADD THIS IMPORT

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Lunacy.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<RaceSizeCapability>> RACE_SIZE =
            ATTACHMENT_TYPES.register("race_size", () ->
                    AttachmentType.builder(RaceSizeCapability::new)
                            .serialize(RaceSizeCapability.CODEC)
                            .build()
            );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SculkStorage>> SCULK_STORAGE =
            ATTACHMENT_TYPES.register("sculk_storage", () ->
                    AttachmentType.builder(SculkStorage::new)
                            .serialize(new IAttachmentSerializer<CompoundTag, SculkStorage>() {
                                @Override
                                public SculkStorage read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                                    SculkStorage storage = new SculkStorage();
                                    storage.loadData(tag, provider);
                                    return storage;
                                }

                                @Override
                                public CompoundTag write(SculkStorage attachment, HolderLookup.Provider provider) {
                                    CompoundTag tag = new CompoundTag();
                                    attachment.saveData(tag, provider);
                                    return tag;
                                }
                            })
                            .build()
            );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ReaperSoulData>> REAPER_SOUL =
            ATTACHMENT_TYPES.register("reaper_soul", () ->
                    AttachmentType.builder(ReaperSoulData::new)
                            .serialize(ReaperSoulData.CODEC)
                            .build()
            );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> BORONT_COOLDOWN_END =
            ATTACHMENT_TYPES.register("boront_cooldown_end",
                    () -> AttachmentType.builder(() -> 0L)
                            .serialize(Codec.LONG)
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> OBSIDIA_COOLDOWN_END =
            ATTACHMENT_TYPES.register("obsidia_cooldown_end",
                    () -> AttachmentType.builder(() -> 0L)
                            .serialize(Codec.LONG)
                            .build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> ROCA_COOLDOWN_END =
            ATTACHMENT_TYPES.register("roca_cooldown_end",
                    () -> AttachmentType.builder(() -> 0L)
                            .serialize(Codec.LONG)
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> ROCA_STANCE_END =
            ATTACHMENT_TYPES.register("roca_stance_end",
                    () -> AttachmentType.builder(() -> 0L)
                            .serialize(Codec.LONG)
                            .build());

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}