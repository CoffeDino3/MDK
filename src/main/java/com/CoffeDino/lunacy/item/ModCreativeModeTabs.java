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