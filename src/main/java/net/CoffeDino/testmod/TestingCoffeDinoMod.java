package net.CoffeDino.testmod;

import com.mojang.logging.LogUtils;
import net.CoffeDino.testmod.block.ModBlocks;
import net.CoffeDino.testmod.capability.IRaceSize;
import net.CoffeDino.testmod.commands.RaceCommand;
import net.CoffeDino.testmod.item.ModCreativeModeTabs;
import net.CoffeDino.testmod.item.ModItems;
import net.CoffeDino.testmod.network.NetworkHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixin;


@Mod(TestingCoffeDinoMod.MOD_ID)
public class TestingCoffeDinoMod
{
    public static final String MOD_ID = "testingcoffedinomod";
    public static final Logger LOGGER = LogUtils.getLogger();


    public TestingCoffeDinoMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        MixinBootstrap.init();

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);



        modEventBus.addListener(this::addCreative);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(NetworkHandler::register);
    }




    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS){
            event.accept(ModItems.CUMMINGTONITE);
            event.accept(ModItems.CUMMINGTONITE_INGOT);
            event.accept(ModItems.STACK_STAR);
            event.accept(ModItems.STORAGE_GEM);
        }

        //if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES){
         //   event.accept(ModItems.THE_WAND);
         //   event.accept(ModItems.SPECIAL_WAND);
       // }

        if(event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS){
            event.accept(ModBlocks.CUMMINGTONITE_BLOCK);
            event.accept(ModBlocks.CUMMINGTONITE_INGOT_BLOCK);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        RaceCommand.register(event.getServer().getCommands().getDispatcher());
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }
    }

}
