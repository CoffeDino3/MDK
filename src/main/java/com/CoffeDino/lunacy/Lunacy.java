package com.CoffeDino.lunacy;

import com.CoffeDino.lunacy.capability.ModAttachments;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.CoffeDino.lunacy.attributes.ModAttributes;
import com.CoffeDino.lunacy.block.ModBlocks;
import com.CoffeDino.lunacy.capability.ISculkStorage;
import com.CoffeDino.lunacy.commands.ClassCommand;
import com.CoffeDino.lunacy.commands.RaceCommand;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.entity.ModEntities;
import com.CoffeDino.lunacy.entity.abilities.GatekeeperProjectileEntity;
import com.CoffeDino.lunacy.item.Custom.FireSpearItem;
import com.CoffeDino.lunacy.item.ModCreativeModeTabs;
import com.CoffeDino.lunacy.item.ModItems;
import com.CoffeDino.lunacy.menu.ModMenuTypes;
import com.CoffeDino.lunacy.network.NetworkHandler;
import com.CoffeDino.lunacy.particle.ClockParticle;
import com.CoffeDino.lunacy.particle.ModParticles;
import com.CoffeDino.lunacy.particle.MourningButterflyParticle;
import com.CoffeDino.lunacy.renderer.*;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Lunacy.MODID)
public class Lunacy {
    public static final String MODID = "lunacy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lunacy(IEventBus modEventBus, ModContainer modContainer) {
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModParticles.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        ModAttachments.register(modEventBus);
        com.CoffeDino.lunacy.player.ModAttachments.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerPayloads);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Sculk Storage capability initialized");
        });
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        NetworkHandler.register(event);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.CUMMINGTONITE.get());
            event.accept(ModItems.CUMMINGTONITE_INGOT.get());
            event.accept(ModItems.STACK_STAR.get());
            event.accept(ModItems.STORAGE_GEM.get());
        }

        //if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES){
        //   event.accept(ModItems.THE_WAND.get());
        //   event.accept(ModItems.SPECIAL_WAND.get());
        // }

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.CUMMINGTONITE_BLOCK.get());
            event.accept(ModBlocks.CUMMINGTONITE_INGOT_BLOCK.get());
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        RaceCommand.register(event.getServer().getCommands().getDispatcher());
        ClassCommand.register(event.getServer().getCommands().getDispatcher());
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.ANGELBORN_ABILITY.get(), AngelbornAbilityRenderer::new);
            LOGGER.debug("Angelborn ability renderer registered");
            EntityRenderers.register(ModEntities.GATEKEEPER_PORTAL.get(), GatekeeperPortalRenderer::new);
            LOGGER.debug("Gatekeeper portal renderer registered");
            EntityRenderers.register(ModEntities.GATEKEEPER_PROJECTILE.get(), GatekeeperProjectileRenderer::new);
            LOGGER.debug("Gatekeeper projectile renderer registered");
            EntityRenderers.register(ModEntities.LAMENT_BULLET.get(), LamentBulletRenderer::new);
            LOGGER.debug("Lament bullet renderer registered");
            EntityRenderers.register(ModEntities.BORONT_AVATAR.get(), BorontAvatarRenderer::new);
            EntityRenderers.register(ModEntities.FIRE_SPEAR.get(), FireSpearRenderer::new);
            EntityRenderers.register(ModEntities.FLOATING_RAPIER.get(), FloatingRapierRenderer::new);
            EntityRenderers.register(ModEntities.THROWN_RAPIER.get(), ThrownRapierRenderer::new);
            EntityRenderers.register(ModEntities.ROCA_BOULDER.get(), RocaBoulderRenderer::new);
            event.enqueueWork(() -> {
                ItemProperties.register(ModItems.AGNIS_FURY.get(),
                        ResourceLocation.fromNamespaceAndPath(MODID, "charged"),
                        (stack, level, entity, seed) -> FireSpearItem.isCharged(stack) ? 1.0F : 0.0F);
            });
            event.enqueueWork(() -> {
                ItemProperties.register(ModItems.GRUCK.get(),
                        ResourceLocation.withDefaultNamespace("blocking"),
                        (stack, level, entity, seed) ->
                                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            });
        }

        @SubscribeEvent
        public static void registerParticleProvider(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(), MourningButterflyParticle.Provider::new);
            event.registerSpriteSet(ModParticles.CLOCK_PARTICLES.get(), ClockParticle.Provider::new);
        }
    }
}