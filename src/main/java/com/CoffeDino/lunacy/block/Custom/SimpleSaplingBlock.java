package com.CoffeDino.lunacy.block.Custom;

import com.CoffeDino.lunacy.worldgen.ModConfiguredFeatures;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleSaplingBlock extends SaplingBlock {

    public static final MapCodec<SimpleSaplingBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    TreeGrower.CODEC.fieldOf("tree").forGetter(block -> block.treeGrower),
                    propertiesCodec()
            ).apply(instance, SimpleSaplingBlock::new)
    );

    public SimpleSaplingBlock(TreeGrower treeGrower, BlockBehaviour.Properties properties) {
        super(treeGrower, properties);
    }

    @Override
    public MapCodec<SimpleSaplingBlock> codec() {
        return CODEC;
    }

    @Override
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), 4);
        } else {
            level.registryAccess()
                    .registry(Registries.CONFIGURED_FEATURE)
                    .flatMap(registry -> registry.getHolder(ModConfiguredFeatures.MAPLE_KEY))
                    .ifPresentOrElse(holder -> {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
                        boolean placed = holder.value().place(
                                level,
                                level.getChunkSource().getGenerator(),
                                random,
                                pos
                        );
                        if (!placed) {
                            level.setBlock(pos, state, 4);
                        }
                    }, () -> {
                        System.err.println("[Lunacy Error] Could not find configured feature: " + ModConfiguredFeatures.MAPLE_KEY.location());
                    });
        }
    }
}