package net.CoffeDino.testmod.datagen;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TestingCoffeDinoMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider){
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(ModBlocks.CUMMINGTONITE_BLOCK.get())
                .add(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get());

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
            .add(ModBlocks.CUMMINGTONITE_BLOCK.get())
                .add(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get());
    }
}
