package com.CoffeDino.lunacy.item;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Lunacy.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CUMMINGTONITE_ITEMS_TAB = CREATIVE_MODE_TABS.register("cummingtonite_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.CUMMINGTONITE.get()))
                    .title(Component.translatable("creativetab.lunacy.cummingtonite_items"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.CUMMINGTONITE.get());
                        output.accept(ModItems.CUMMINGTONITE_INGOT.get());
                        output.accept(ModItems.STACK_STAR.get());
                        output.accept(ModItems.STORAGE_GEM.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SPECIAL_TOOLS_TAB = CREATIVE_MODE_TABS.register("special_tools_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.THE_WAND.get()))
                    .title(Component.translatable("creativetab.lunacy.special_tools"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.THE_WAND.get());
                        output.accept(ModItems.SPECIAL_WAND.get());
                        output.accept(ModItems.RACE_RESET_SCROLL.get());
                        output.accept(ModItems.LAMENT_BULLET.get());
                        output.accept(ModItems.LAMENT_GUN.get());
                        output.accept(ModItems.VIRIDYUM_GREATSWORD.get());
                        output.accept(ModItems.AGNIS_FURY.get());
                        output.accept(ModItems.SOUL_SCYTHE.get());
                        output.accept(ModItems.SHI_BOW.get());
                        output.accept(ModItems.SHI_ARROW.get());
                        output.accept(ModItems.AMETHYST_RAPIER.get());
                        output.accept(ModItems.OBSIDIA.get());
                        output.accept(ModItems.BORONT_AXE.get());
                        output.accept(ModItems.ROCA.get());
                        output.accept(ModItems.GRUCK.get());
                        output.accept(ModItems.CHARYBDIS.get());
                        output.accept(ModItems.MOIRAI.get());
                        output.accept(ModItems.PHAETON.get());
                        output.accept(ModItems.PERUN.get());
                        output.accept(ModItems.JORO.get());
                        output.accept(ModItems.BOREAS.get());
                        output.accept(ModItems.AMPHITRITE.get());
                        output.accept(ModItems.ERINYES.get());
                        output.accept(ModItems.HELIOS.get());
                        output.accept(ModItems.WOOD_DAGGER.get());
                        output.accept(ModItems.STONE_DAGGER.get());
                        output.accept(ModItems.IRON_DAGGER.get());
                        output.accept(ModItems.GOLD_DAGGER.get());
                        output.accept(ModItems.DIAMOND_DAGGER.get());
                        output.accept(ModItems.NETHERITE_DAGGER.get());
                        output.accept(ModItems.WOOD_RAPIER.get());
                        output.accept(ModItems.STONE_RAPIER.get());
                        output.accept(ModItems.IRON_RAPIER.get());
                        output.accept(ModItems.GOLD_RAPIER.get());
                        output.accept(ModItems.DIAMOND_RAPIER.get());
                        output.accept(ModItems.NETHERITE_RAPIER.get());
                        output.accept(ModItems.WOOD_GREATSWORD.get());
                        output.accept(ModItems.STONE_GREATSWORD.get());
                        output.accept(ModItems.IRON_GREATSWORD.get());
                        output.accept(ModItems.GOLD_GREATSWORD.get());
                        output.accept(ModItems.DIAMOND_GREATSWORD.get());
                        output.accept(ModItems.NETHERITE_GREATSWORD.get());
                        output.accept(ModItems.WOOD_SCYTHE.get());
                        output.accept(ModItems.STONE_SCYTHE.get());
                        output.accept(ModItems.IRON_SCYTHE.get());
                        output.accept(ModItems.GOLD_SCYTHE.get());
                        output.accept(ModItems.DIAMOND_SCYTHE.get());
                        output.accept(ModItems.NETHERITE_SCYTHE.get());
                        output.accept(ModItems.WOOD_SPEAR.get());
                        output.accept(ModItems.STONE_SPEAR.get());
                        output.accept(ModItems.IRON_SPEAR.get());
                        output.accept(ModItems.GOLD_SPEAR.get());
                        output.accept(ModItems.DIAMOND_SPEAR.get());
                        output.accept(ModItems.NETHERITE_SPEAR.get());
                        output.accept(ModItems.WOOD_SPELLBLADE.get());
                        output.accept(ModItems.STONE_SPELLBLADE.get());
                        output.accept(ModItems.IRON_SPELLBLADE.get());
                        output.accept(ModItems.GOLD_SPELLBLADE.get());
                        output.accept(ModItems.DIAMOND_SPELLBLADE.get());
                        output.accept(ModItems.NETHERITE_SPELLBLADE.get());
                        output.accept(ModItems.IRON_GUN.get());
                        output.accept(ModItems.GOLD_GUN.get());
                        output.accept(ModItems.DIAMOND_GUN.get());
                        output.accept(ModItems.NETHERITE_GUN.get());
                        output.accept(ModItems.BULLET.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CUMMINGTONITE_BLOCKS_TAB = CREATIVE_MODE_TABS.register("cummingtonite_blocks_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.CUMMINGTONITE_BLOCK.get().asItem()))
                    .withTabsBefore(CUMMINGTONITE_ITEMS_TAB.getId())
                    .title(Component.translatable("creativetab.lunacy.cummingtonite_blocks"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.CUMMINGTONITE_BLOCK.get().asItem());
                        output.accept(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get().asItem());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}