package com.CoffeDino.lunacy.datagen;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Lunacy.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels(){
        blockWithItem((DeferredBlock<Block>) ModBlocks.CUMMINGTONITE_BLOCK);
        simpleBlockWithItem(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get(),
                models().cubeTop("cummingtonite_ingot_block",
                        modLoc("block/cummingtonite_ingot_block_side"),
                        modLoc("block/cummingtonite_ingot_block_end")));
    }

    private void blockWithItem(DeferredBlock<Block> blockRegistryObject){
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }
}