package net.CoffeDino.testmod.datagen;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TestingCoffeDinoMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels(){
        basicItem(ModItems.CUMMINGTONITE.get());
        basicItem(ModItems.CUMMINGTONITE_INGOT.get());

        basicItem(ModItems.SPECIAL_WAND.get());
        basicItem(ModItems.THE_WAND.get());
        basicItem(ModItems.STACK_STAR.get());
        basicItem(ModItems.STORAGE_GEM.get());
        basicItem(ModItems.RACE_RESET_SCROLL.get());
    }
}
