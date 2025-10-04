package net.CoffeDino.testmod.item;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TestingCoffeDinoMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CUMMINGTONITE_ITEMS_TAB = CREATIVE_MODE_TABS.register("cummingtonite_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.CUMMINGTONITE.get()))
                    .title(Component.translatable("creativetab.testingcoffedinomod.cummingtonite_items"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.CUMMINGTONITE.get());
                        output.accept(ModItems.CUMMINGTONITE_INGOT.get());
                        output.accept(ModItems.STACK_STAR.get());
                        output.accept(ModItems.STORAGE_GEM.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> SPECIAL_TOOLS_TAB = CREATIVE_MODE_TABS.register("special_tools_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.THE_WAND.get()))
                    .title(Component.translatable("creativetab.testingcoffedinomod.special_tools"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.THE_WAND.get());
                        output.accept(ModItems.SPECIAL_WAND.get());
                    })

                    .build());

    public static final RegistryObject<CreativeModeTab> CUMMINGTONITE_BLOCKS_TAB = CREATIVE_MODE_TABS.register("cummingtonite_blocks_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.CUMMINGTONITE_BLOCK.get().asItem()))
                    .withTabsBefore(CUMMINGTONITE_ITEMS_TAB.getId())
                    .title(Component.translatable("creativetab.testingcoffedinomod.cummingtonite_blocks"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.CUMMINGTONITE_BLOCK.get().asItem());
                        output.accept(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get().asItem());
                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
