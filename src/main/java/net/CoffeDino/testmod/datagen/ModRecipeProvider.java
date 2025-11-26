package net.CoffeDino.testmod.datagen;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.block.ModBlocks;
import net.CoffeDino.testmod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }



    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput){
        List<ItemLike> CUMMINGTONITE_SMELTABLES = List.of(ModItems.CUMMINGTONITE.get(),
                ModBlocks.CUMMINGTONITE_BLOCK.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.CUMMINGTONITE_INGOT.get())
                .unlockedBy(getHasName(ModItems.CUMMINGTONITE_INGOT.get()), has(ModItems.CUMMINGTONITE_INGOT.get())).save(pRecipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CUMMINGTONITE_INGOT.get(), 9)
                .requires(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get()), has(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get())).save(pRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.THE_WAND.get())
                .pattern("AAC")
                .pattern("ASA")
                .pattern("NAA")
                .define('C', ModItems.CUMMINGTONITE_INGOT.get())
                .define('S', Items.STICK)
                .define('A', Items.GOLD_INGOT)
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy(getHasName(ModItems.CUMMINGTONITE_INGOT.get()), has(ModItems.CUMMINGTONITE_INGOT.get())).save(pRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPECIAL_WAND.get())
                .pattern("NNC")
                .pattern("NSN")
                .pattern("ANN")
                .define('C', ModItems.CUMMINGTONITE_INGOT.get())
                .define('S', Items.STICK)
                .define('A', Items.GOLD_INGOT)
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy(getHasName(ModItems.CUMMINGTONITE_INGOT.get()), has(ModItems.CUMMINGTONITE_INGOT.get())).save(pRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STORAGE_GEM.get())
                .pattern(" C ")
                .pattern("NSA")
                .pattern("   ")
                .define('C', ModItems.CUMMINGTONITE_INGOT.get())
                .define('S', Items.DIAMOND)
                .define('A', Items.GOLD_INGOT)
                .define('N', Items.EMERALD)
                .unlockedBy(getHasName(ModItems.CUMMINGTONITE_INGOT.get()), has(ModItems.CUMMINGTONITE_INGOT.get())).save(pRecipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STACK_STAR.get())
                .requires(Items.NETHER_STAR)
                .requires(ModItems.CUMMINGTONITE_INGOT.get())
                .unlockedBy(getHasName(ModItems.CUMMINGTONITE_INGOT.get()), has(ModItems.CUMMINGTONITE_INGOT.get()))
                .save(pRecipeOutput);


        oreSmelting(pRecipeOutput, CUMMINGTONITE_SMELTABLES, RecipeCategory.MISC, ModItems.CUMMINGTONITE_INGOT.get(), 0.40f, 250, "cummingtonite_ingot");
        oreBlasting(pRecipeOutput, CUMMINGTONITE_SMELTABLES, RecipeCategory.MISC, ModItems.CUMMINGTONITE_INGOT.get(), 0.60f, 150, "cummingtonite_ingot");
    }
    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, Lunacy.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
