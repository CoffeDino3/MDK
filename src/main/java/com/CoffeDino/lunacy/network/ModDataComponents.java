package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.item.BulletEnhancement;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Lunacy.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<BulletEnhancement>>> BULLET_ENHANCEMENTS =
            DATA_COMPONENTS.register("bullet_enhancements", () -> DataComponentType.<List<BulletEnhancement>>builder()
                    .persistent(BulletEnhancement.CODEC.listOf())
                    .networkSynchronized(BulletEnhancement.STREAM_CODEC.apply(ByteBufCodecs.list(2)))
                    .build());
}