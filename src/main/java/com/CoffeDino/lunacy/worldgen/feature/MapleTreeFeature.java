package com.CoffeDino.lunacy.worldgen.feature;

import com.CoffeDino.lunacy.block.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MapleTreeFeature extends Feature<NoneFeatureConfiguration> {

    private static final TagKey<Block> NEOFORGE_FLOWERS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("neoforge", "flowers")
    );

    public MapleTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!isValidGround(level, origin)) {
            return false;
        }
        if (!hasEnoughFlowers(level, origin, 50, 10)) {
            return false;
        }
        int trunkHeight = 5 + random.nextInt(3);
        BlockState logState = ModBlocks.MAPLE_LOG.get().defaultBlockState();
        BlockState woodState = ModBlocks.MAPLE_WOOD.get().defaultBlockState();
        BlockState leafState = ModBlocks.MAPLE_LEAVES.get().defaultBlockState();
        for (int y = 0; y < trunkHeight; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos logPos = origin.offset(x, y, z);
                    if (isReplaceable(level, logPos)) {
                        level.setBlock(logPos, logState, 3);
                    }
                }
            }
        }
        generateRoots(level, origin, woodState, random);
        int centerExtraHeight = 2 + random.nextInt(2);
        BlockPos centerPos = origin.offset(0, trunkHeight, 0);
        for (int i = 0; i < centerExtraHeight; i++) {
            BlockPos logPos = centerPos.above(i);
            if (isReplaceable(level, logPos)) {
                level.setBlock(logPos, logState, 3);
            }
        }
        generateLeafBlob(level, centerPos.above(centerExtraHeight), leafState, 2);
        Direction[] branchDirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction dir : branchDirs) {
            int branchStartHeight = 2 + random.nextInt(Math.max(1, trunkHeight - 2));
            BlockPos branchStart = origin.offset(
                    dir == Direction.EAST ? 1 : 0,
                    branchStartHeight,
                    dir == Direction.SOUTH ? 1 : 0
            );

            generateBranch(level, branchStart, dir, woodState, leafState, random);
        }

        return true;
    }

    private boolean isValidGround(WorldGenLevel level, BlockPos origin) {
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                BlockState ground = level.getBlockState(origin.offset(x, -1, z));
                if (!ground.is(BlockTags.DIRT)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasEnoughFlowers(WorldGenLevel level, BlockPos origin, int radius, int requiredCount) {
        int flowerCount = 0;
        int step = 3;

        for (int x = -radius; x <= radius; x += step) {
            for (int z = -radius; z <= radius; z += step) {
                BlockPos checkPos = level.getHeightmapPos(
                        net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
                        origin.offset(x, 0, z)
                );

                if (isFlowerBlock(level.getBlockState(checkPos)) || isFlowerBlock(level.getBlockState(checkPos.below()))) {
                    flowerCount++;
                    if (flowerCount >= requiredCount) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isFlowerBlock(BlockState state) {
        return state.is(BlockTags.FLOWERS)
                || state.is(NEOFORGE_FLOWERS)
                || state.getBlock() instanceof FlowerBlock;
    }

    private void generateRoots(WorldGenLevel level, BlockPos origin, BlockState woodState, RandomSource random) {
        Direction[] rootDirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction dir : rootDirs) {
            if (random.nextBoolean()) {
                BlockPos rootStart = origin.offset(
                        dir == Direction.EAST ? 1 : 0,
                        0,
                        dir == Direction.SOUTH ? 1 : 0
                );

                level.setBlock(rootStart.relative(dir), woodState, 3);
                level.setBlock(rootStart.relative(dir).below(), woodState, 3);
            }
        }
    }

    private void generateBranch(WorldGenLevel level, BlockPos start, Direction dir, BlockState woodState, BlockState leafState, RandomSource random) {
        BlockPos current = start;
        int length = 2 + random.nextInt(2);

        BlockState orientedWood = woodState.hasProperty(RotatedPillarBlock.AXIS) ?
                woodState.setValue(RotatedPillarBlock.AXIS, dir.getAxis()) : woodState;

        for (int i = 0; i < length; i++) {
            current = current.relative(dir).above();
            if (isReplaceable(level, current)) {
                level.setBlock(current, orientedWood, 3);
            }
        }

        generateLeafBlob(level, current, leafState, 2);
    }

    private void generateLeafBlob(WorldGenLevel level, BlockPos center, BlockState leafState, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos leafPos = center.offset(x, y, z);
                    if (isReplaceable(level, leafPos) && (x * x + y * y + z * z <= radius * radius + 1)) {
                        level.setBlock(leafPos, leafState, 3);
                    }
                }
            }
        }
    }

    private boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.is(ModBlocks.MAPLE_SAPLING.get());
    }
}