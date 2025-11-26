package net.CoffeDino.testmod;

import com.mojang.logging.LogUtils;
import net.CoffeDino.testmod.block.ModBlocks;
import net.CoffeDino.testmod.capability.ISculkStorage;
import net.CoffeDino.testmod.commands.ClassCommand;
import net.CoffeDino.testmod.commands.RaceCommand;
import net.CoffeDino.testmod.effects.ModEffects;
import net.CoffeDino.testmod.entity.ModEntities;
import net.CoffeDino.testmod.item.Custom.FireSpearItem;
import net.CoffeDino.testmod.item.ModCreativeModeTabs;
import net.CoffeDino.testmod.item.ModItems;
import net.CoffeDino.testmod.menu.ModMenuTypes;
import net.CoffeDino.testmod.network.NetworkHandler;
import net.CoffeDino.testmod.particle.ClockParticle;
import net.CoffeDino.testmod.particle.ModParticles;
import net.CoffeDino.testmod.particle.MourningButterflyParticle;
import net.CoffeDino.testmod.renderer.FireSpearRenderer;
import net.CoffeDino.testmod.renderer.LamentBulletRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import net.CoffeDino.testmod.renderer.AngelbornAbilityRenderer;



@Mod(Lunacy.MOD_ID)
public class Lunacy
{
    public static final String MOD_ID = "lunacy";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Capability<ISculkStorage> SCULK_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    public Lunacy(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModParticles.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.register();
            LOGGER.info("Sculk Storage capability initialized");
        });
    }


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
        ClassCommand.register(event.getServer().getCommands().getDispatcher());
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(ModEntities.ANGELBORN_ABILITY.get(), AngelbornAbilityRenderer::new);
            LOGGER.debug("Angelborn ability renderer registered");
            EntityRenderers.register(ModEntities.LAMENT_BULLET.get(), LamentBulletRenderer::new);
            LOGGER.debug("Lament bullet renderer registered");
            EntityRenderers.register(ModEntities.FIRE_SPEAR.get(), FireSpearRenderer::new);
            event.enqueueWork(() -> {
                ItemProperties.register(ModItems.AGNIS_FURY.get(),
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "charged"),
                        (stack, level, entity, seed) -> FireSpearItem.isCharged(stack) ? 1.0F : 0.0F);
            });
        }

        @SubscribeEvent
        public static void registerParticleProvider(RegisterParticleProvidersEvent event){
            event.registerSpriteSet(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(), MourningButterflyParticle.Provider::new);
            event.registerSpriteSet(ModParticles.CLOCK_PARTICLES.get(), ClockParticle.Provider::new);
        }




    }

}
