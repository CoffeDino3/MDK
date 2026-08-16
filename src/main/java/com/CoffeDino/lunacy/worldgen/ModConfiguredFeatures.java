package com.CoffeDino.lunacy.worldgen;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> MAPLE_KEY = registerKey("maple");

    public static final ResourceKey<ConfiguredFeature<?, ?>> CUMMINGTONITE_ORE_KEY = registerKey("cummingtonite_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPRIGOT_ORE_KEY = registerKey("sprigot_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VIRIDYUM_ORE_KEY = registerKey("viridyum_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BORONT_ORE_KEY = registerKey("boront_ore");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        register(context, MAPLE_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.MAPLE_LOG.get().defaultBlockState()),
                new StraightTrunkPlacer(5, 2, 0),
                BlockStateProvider.simple(ModBlocks.MAPLE_LEAVES.get().defaultBlockState()),
                new BlobFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 0), 3),
                new TwoLayersFeatureSize(1, 0, 1)
        ).ignoreVines().build());

        register(context, CUMMINGTONITE_ORE_KEY, Feature.ORE, new OreConfiguration(
                List.of(
                        OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), ModBlocks.CUMMINGTONITE_BLOCK.get().defaultBlockState()),
                        OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), ModBlocks.CUMMINGTONITE_BLOCK.get().defaultBlockState())
                ),
                4, 0.0f));

        register(context, SPRIGOT_ORE_KEY, Feature.ORE, new OreConfiguration(
                List.of(
                        OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), ModBlocks.SPRIGOT_ORE_BLOCK.get().defaultBlockState()),
                        OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), ModBlocks.SPRIGOT_ORE_BLOCK.get().defaultBlockState())
                ),
                4, 0.0f));

        register(context, VIRIDYUM_ORE_KEY, Feature.ORE, new OreConfiguration(
                List.of(
                        OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), ModBlocks.VIRIDYUM_ORE_BLOCK.get().defaultBlockState()),
                        OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), ModBlocks.VIRIDYUM_ORE_BLOCK.get().defaultBlockState())
                ),
                4, 0.0f));

        register(context, BORONT_ORE_KEY, Feature.ORE, new OreConfiguration(
                List.of(
                        OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), ModBlocks.BORONT_ORE_BLOCK.get().defaultBlockState()),
                        OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), ModBlocks.BORONT_ORE_BLOCK.get().defaultBlockState())
                ),
                4, 0.0f));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, name));
    }

    private static <FC extends net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration, F extends Feature<FC>>
    void register(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC config) {
        context.register(key, new ConfiguredFeature<>(feature, config));
    }
}