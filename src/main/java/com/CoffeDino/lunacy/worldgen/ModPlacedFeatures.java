package com.CoffeDino.lunacy.worldgen;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;

import java.util.List;

public class ModPlacedFeatures {

    public static final ResourceKey<PlacedFeature> MAPLE_PLACED_KEY = registerKey("maple_placed");

    public static final ResourceKey<PlacedFeature> CUMMINGTONITE_ORE_PLACED_KEY = registerKey("cummingtonite_ore_placed");
    public static final ResourceKey<PlacedFeature> SPRIGOT_ORE_PLACED_KEY = registerKey("sprigot_ore_placed");
    public static final ResourceKey<PlacedFeature> VIRIDYUM_ORE_PLACED_KEY = registerKey("viridyum_ore_placed");
    public static final ResourceKey<PlacedFeature> BORONT_ORE_PLACED_KEY = registerKey("boront_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        var maple = configuredFeatures.getOrThrow(ModConfiguredFeatures.MAPLE_KEY);

        register(context, MAPLE_PLACED_KEY, maple,
                List.of(
                        RarityFilter.onAverageOnceEvery(10),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                        net.minecraft.world.level.levelgen.placement.BiomeFilter.biome()
                ));
        var cummingtonite = configuredFeatures.getOrThrow(ModConfiguredFeatures.CUMMINGTONITE_ORE_KEY);
        register(context, CUMMINGTONITE_ORE_PLACED_KEY, cummingtonite, orePlacement(1));

        var sprigot = configuredFeatures.getOrThrow(ModConfiguredFeatures.SPRIGOT_ORE_KEY);
        register(context, SPRIGOT_ORE_PLACED_KEY, sprigot, orePlacement(1));

        var viridyum = configuredFeatures.getOrThrow(ModConfiguredFeatures.VIRIDYUM_ORE_KEY);
        register(context, VIRIDYUM_ORE_PLACED_KEY, viridyum, orePlacement(1));

        var boront = configuredFeatures.getOrThrow(ModConfiguredFeatures.BORONT_ORE_KEY);
        register(context, BORONT_ORE_PLACED_KEY, boront, orePlacement(1));
    }

    private static List<PlacementModifier> orePlacement(int veinsPerChunk) {
        return List.of(
                CountPlacement.of(veinsPerChunk),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32)),
                BiomeFilter.biome()
        );
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 net.minecraft.core.Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}