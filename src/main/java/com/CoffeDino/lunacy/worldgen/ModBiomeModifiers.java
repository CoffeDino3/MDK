package com.CoffeDino.lunacy.worldgen;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;

public class ModBiomeModifiers {

    public static final ResourceKey<BiomeModifier> ADD_MAPLE_KEY = ResourceKey.create(
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "add_maple"));

    public static final ResourceKey<BiomeModifier> ADD_CUMMINGTONITE_ORE_KEY = registerKey("add_cummingtonite_ore");
    public static final ResourceKey<BiomeModifier> ADD_SPRIGOT_ORE_KEY = registerKey("add_sprigot_ore");
    public static final ResourceKey<BiomeModifier> ADD_VIRIDYUM_ORE_KEY = registerKey("add_viridyum_ore");
    public static final ResourceKey<BiomeModifier> ADD_BORONT_ORE_KEY = registerKey("add_boront_ore");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var biomes = context.lookup(Registries.BIOME);
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        context.register(ADD_MAPLE_KEY, new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_FOREST),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.MAPLE_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION
        ));

        context.register(ADD_CUMMINGTONITE_ORE_KEY, new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.CUMMINGTONITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        context.register(ADD_SPRIGOT_ORE_KEY, new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.SPRIGOT_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        context.register(ADD_VIRIDYUM_ORE_KEY, new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.VIRIDYUM_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        context.register(ADD_BORONT_ORE_KEY, new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.BORONT_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(
                net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, name));
    }
}