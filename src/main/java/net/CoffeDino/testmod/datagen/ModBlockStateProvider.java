package net.CoffeDino.testmod.datagen;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TestingCoffeDinoMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels(){
        blockWithItem(ModBlocks.CUMMINGTONITE_BLOCK);
        simpleBlockWithItem(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get(),
                models().cubeTop("cummingtonite_ingot_block",
                        modLoc("block/cummingtonite_ingot_block_side"),
                        modLoc("block/cummingtonite_ingot_block_end")));
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject){
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }
}
