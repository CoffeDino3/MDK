package com.CoffeDino.lunacy.worldgen.feature;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Lunacy.MODID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> MAPLE_TREE_FEATURE =
            FEATURES.register("maple_tree_feature", () -> new MapleTreeFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
