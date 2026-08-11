package com.CoffeDino.lunacy.block.Custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

public class DoubleDoorBlock extends DoorBlock {
    public DoubleDoorBlock(
            BlockSetType type,
            Properties properties
    ) {
        super(type, properties);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        boolean wasOpen = state.getValue(BlockStateProperties.OPEN);

        InteractionResult result = super.useWithoutItem(state, level, pos, player, hit);

        if (!level.isClientSide && result.consumesAction()) {
            BlockState newState = level.getBlockState(pos);
            boolean isNowOpen = newState.getValue(BlockStateProperties.OPEN);
            if (wasOpen != isNowOpen) {
                syncAdjacentDoor(level, pos, newState);
            }
        }

        return result;
    }

    private void syncAdjacentDoor(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        for (Direction side : new Direction[]{left, right}) {
            BlockPos neighborPos = pos.relative(side);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.getBlock() instanceof DoubleDoorBlock &&
                    neighborState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == half &&
                    neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing) {
                level.setBlock(
                        neighborPos,
                        neighborState.setValue(BlockStateProperties.OPEN, isOpen),
                        3
                );
                break;
            }
        }
    }
}