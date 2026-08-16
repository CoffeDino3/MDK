package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.block.ModBlocks;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = Lunacy.MODID)
public class LavaObsidianFormationHandler {

    private static final float LAVA_OBSIDIAN_CHANCE = 0.05f;

    @SubscribeEvent
    public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!event.getNewState().is(Blocks.OBSIDIAN)) {
            return;
        }

        if (ThreadLocalRandom.current().nextFloat() < LAVA_OBSIDIAN_CHANCE) {
            event.setNewState(ModBlocks.LAVA_OBSIDIAN.get().defaultBlockState());
        }
    }
}