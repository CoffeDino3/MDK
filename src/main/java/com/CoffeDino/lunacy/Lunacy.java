package com.CoffeDino.lunacy;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.particle.PerunFlashParticle;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.CoffeDino.lunacy.attributes.ModAttributes;
import com.CoffeDino.lunacy.block.ModBlocks;
import com.CoffeDino.lunacy.commands.ClassCommand;
import com.CoffeDino.lunacy.commands.RaceCommand;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.entity.ModEntities;
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
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Lunacy.MODID)
public class Lunacy {
    public static final String MODID = "lunacy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lunacy(IEventBus modEventBus, ModContainer modContainer) {
        ModCreativeModeTabs.register(modEventBus);
        LunacyGameRules.init();
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
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.WOOD_DAGGER.get());
            event.accept(ModItems.STONE_DAGGER.get());
            event.accept(ModItems.IRON_DAGGER.get());
            event.accept(ModItems.GOLD_DAGGER.get());
            event.accept(ModItems.DIAMOND_DAGGER.get());
            event.accept(ModItems.NETHERITE_DAGGER.get());
            event.accept(ModItems.WOOD_RAPIER.get());
            event.accept(ModItems.STONE_RAPIER.get());
            event.accept(ModItems.IRON_RAPIER.get());
            event.accept(ModItems.GOLD_RAPIER.get());
            event.accept(ModItems.DIAMOND_RAPIER.get());
            event.accept(ModItems.NETHERITE_RAPIER.get());
            event.accept(ModItems.WOOD_GREATSWORD.get());
            event.accept(ModItems.STONE_GREATSWORD.get());
            event.accept(ModItems.IRON_GREATSWORD.get());
            event.accept(ModItems.GOLD_GREATSWORD.get());
            event.accept(ModItems.DIAMOND_GREATSWORD.get());
            event.accept(ModItems.NETHERITE_GREATSWORD.get());
            event.accept(ModItems.WOOD_SCYTHE.get());
            event.accept(ModItems.STONE_SCYTHE.get());
            event.accept(ModItems.IRON_SCYTHE.get());
            event.accept(ModItems.GOLD_SCYTHE.get());
            event.accept(ModItems.DIAMOND_SCYTHE.get());
            event.accept(ModItems.NETHERITE_SCYTHE.get());
            event.accept(ModItems.WOOD_SPEAR.get());
            event.accept(ModItems.STONE_SPEAR.get());
            event.accept(ModItems.IRON_SPEAR.get());
            event.accept(ModItems.GOLD_SPEAR.get());
            event.accept(ModItems.DIAMOND_SPEAR.get());
            event.accept(ModItems.NETHERITE_SPEAR.get());
            event.accept(ModItems.WOOD_SPELLBLADE.get());
            event.accept(ModItems.STONE_SPELLBLADE.get());
            event.accept(ModItems.IRON_SPELLBLADE.get());
            event.accept(ModItems.GOLD_SPELLBLADE.get());
            event.accept(ModItems.DIAMOND_SPELLBLADE.get());
            event.accept(ModItems.NETHERITE_SPELLBLADE.get());
            event.accept(ModItems.IRON_GUN.get());
            event.accept(ModItems.GOLD_GUN.get());
            event.accept(ModItems.DIAMOND_GUN.get());
            event.accept(ModItems.NETHERITE_GUN.get());
        }

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
            EntityRenderers.register(ModEntities.FLOATING_RAPIER.get(), FloatingRapierRenderer::new);
            EntityRenderers.register(ModEntities.THROWN_RAPIER.get(), ThrownRapierRenderer::new);
            EntityRenderers.register(ModEntities.ROCA_BOULDER.get(), RocaBoulderRenderer::new);
            EntityRenderers.register(ModEntities.DARK_SPHERE.get(), DarkSphereRenderer::new);
            EntityRenderers.register(ModEntities.HELIOS_SPHERE.get(), HeliosSphereRenderer::new);
            EntityRenderers.register(ModEntities.BLOOD_MIST.get(), BloodMistRenderer::new);
            EntityRenderers.register(ModEntities.BOREAS_STORM.get(), BoreasStormRenderer::new);
            EntityRenderers.register(ModEntities.PERUN_SKY_BEAM.get(), PerunSkyBeamRenderer::new);
            EntityRenderers.register(ModEntities.PERUN_ORBITAL_STRIKE.get(), PerunOrbitalStrikeRenderer::new);
            EntityRenderers.register(ModEntities.AMPHITRITE_ORB.get(), AmphitriteOrbRenderer::new);
            EntityRenderers.register(ModEntities.MOIRAI_PORTAL.get(), MoiraiPortalRenderer::new);
            EntityRenderers.register(ModEntities.MOIRAI_SWEEP.get(), MoiraiSweepRenderer::new);
            event.enqueueWork(() -> {
                ItemProperties.register(ModItems.GRUCK.get(),
                        ResourceLocation.withDefaultNamespace("blocking"),
                        (stack, level, entity, seed) ->
                                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            });
        }
        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            for (PlayerSkin.Model skinModel : event.getSkins()) {
                PlayerRenderer renderer = event.getSkin(skinModel);
                if (renderer != null) {
                    renderer.addLayer(new PhaetonWingsLayer(renderer));
                }
            }
        }

        @SubscribeEvent
        public static void registerParticleProvider(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(), MourningButterflyParticle.Provider::new);
            event.registerSpriteSet(ModParticles.CLOCK_PARTICLES.get(), ClockParticle.Provider::new);
            event.registerSpriteSet(ModParticles.PERUN_FLASH.get(), PerunFlashParticle.Provider::new);

        }
    }
}