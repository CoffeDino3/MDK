package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.client.gui.RaceSelectionScreen;
import com.CoffeDino.lunacy.domain.FireDomainManager;
import com.CoffeDino.lunacy.entity.FloatingRapierEntity;
import com.CoffeDino.lunacy.item.Custom.*;
import com.CoffeDino.lunacy.item.ModItems;
import com.CoffeDino.lunacy.network.NetworkHandler;
import com.CoffeDino.lunacy.races.races;
import com.CoffeDino.lunacy.renderer.GruckRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

import java.util.List;


@EventBusSubscriber(modid = Lunacy.MODID)
public class ModEvents {
    // Server-side events
    @EventBusSubscriber(modid = Lunacy.MODID)
    public static class ServerEvents {

        @SubscribeEvent
        public static void onShieldBlock(LivingShieldBlockEvent event) {
            LivingEntity blocker = event.getEntity();
            if (blocker.level().isClientSide()) return;
            if (blocker.getUsedItemHand() != InteractionHand.MAIN_HAND) return;

            ItemStack usedStack = blocker.getUseItem();
            if (!(usedStack.getItem() instanceof GruckItem)) return;

            float blocked = event.getBlockedDamage();
            if (blocked <= 0) return;

            GruckItem.fireBeam((ServerLevel) blocker.level(), blocker, blocked * 2.0F);
        }
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                Lunacy.LOGGER.debug("DEBUG: ===== SERVER PLAYER LOGIN START =====");
                Lunacy.LOGGER.debug("DEBUG: Player logged in on server - " + serverPlayer.getName().getString() + " UUID: " + serverPlayer.getUUID());

                races.Race race = races.getPlayerRace(serverPlayer);
                Lunacy.LOGGER.debug("DEBUG: Retrieved race for player: " + (race != null ? race.getDisplayName() : "null"));


                if (race != null) {
                    races.onPlayerJoinWorld(serverPlayer);
                    NetworkHandler.syncRaceToClient(serverPlayer, race);
                    NetworkHandler.syncSizeToClient(serverPlayer, race.getHeight(), race.getWidth());
                    Lunacy.LOGGER.debug("DEBUG: Synced race to client on login: " + race.getDisplayName());
                } else {
                    Lunacy.LOGGER.debug("DEBUG: No race found for player, skipping race effects and sync");
                    NetworkHandler.syncRaceToClient(serverPlayer, null);
                }
                PlayerClasses.PlayerClass playerClass = PlayerClasses.getPlayerClass(serverPlayer);
                if (playerClass != null) {
                    NetworkHandler.syncClassToClient(serverPlayer, playerClass);
                    Lunacy.LOGGER.debug("DEBUG: Synced class to client on login: " + playerClass.getDisplayName());
                }
                reapplyPersistedCooldown(serverPlayer, ModAttachments.BORONT_COOLDOWN_END, BorontItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.OBSIDIA_COOLDOWN_END, ObsidiaItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.ROCA_COOLDOWN_END, RocaItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.CHARYBDIS_COOLDOWN_END, CharybdisItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.HELIOS_COOLDOWN_END, HeliosItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.JORO_COOLDOWN_END, JoroItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.ERINYES_COOLDOWN_END, ErinyesItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.MOIRAI_COOLDOWN_END, MoiraiItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.BOREAS_COOLDOWN_END, BoreasItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.PHAETON_COOLDOWN_END, PhaetonItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.PERUN_COOLDOWN_END, PerunItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.AMPHITRITE_COOLDOWN_END, AmphitriteItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.FIRE_SPEAR_COOLDOWN_END, com.CoffeDino.lunacy.item.Custom.FireSpearItem.class);

                Lunacy.LOGGER.debug("DEBUG: ===== SERVER PLAYER LOGIN END =====");
            }
        }


        private static void reapplyPersistedCooldown(ServerPlayer serverPlayer,
                                                     DeferredHolder<AttachmentType<?>, AttachmentType<Long>> cooldownAttachment,
                                                     Class<? extends Item> itemClass) {
            long cooldownEnd = serverPlayer.getData(cooldownAttachment);
            if (cooldownEnd <= 0) return;
            ServerLevel level = serverPlayer.serverLevel();
            if (level == null) return;

            long now = level.getGameTime();
            long remaining = cooldownEnd - now;

            if (remaining <= 0) {
                serverPlayer.setData(cooldownAttachment, 0L);
                return;
            }

            Item item = BuiltInRegistries.ITEM.stream()
                    .filter(itemClass::isInstance)
                    .findFirst()
                    .orElse(null);
            if (item == null) return;

            serverPlayer.getCooldowns().addCooldown(item, (int) remaining);
        }

        @SubscribeEvent
        public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide) {
                List<FloatingRapierEntity> rapiers = player.level().getEntitiesOfClass(FloatingRapierEntity.class,
                        player.getBoundingBox().inflate(100.0),
                        entity -> player.getUUID().equals(entity.getOwner() == null ? null : entity.getOwner().getUUID()));

                for (FloatingRapierEntity rapier : rapiers) {
                    rapier.discard();
                }
                FireDomainManager.forceDespawnFor(player.getUUID());

                Lunacy.LOGGER.debug("DEBUG: Cleared floating rapiers for logging out player: " + player.getName().getString());
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            Player player = event.getEntity();
            Lunacy.LOGGER.debug("DEBUG: Player respawned on server - " + player.getName().getString());
            races.onPlayerJoinWorld(player);
            resyncClientState(player);
        }


        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            Player player = event.getEntity();
            Lunacy.LOGGER.debug("DEBUG: Player changed dimension - " + player.getName().getString());
            races.onPlayerJoinWorld(player);
            resyncClientState(player);
        }
        private static void resyncClientState(Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                races.Race race = races.getPlayerRace(serverPlayer);
                NetworkHandler.syncRaceToClient(serverPlayer, race);
                if (race != null) {
                    NetworkHandler.syncSizeToClient(serverPlayer, race.getHeight(), race.getWidth());
                }

                PlayerClasses.PlayerClass playerClass = PlayerClasses.getPlayerClass(serverPlayer);
                if (playerClass != null) {
                    NetworkHandler.syncClassToClient(serverPlayer, playerClass);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            races.resetClientRace();
            PlayerClasses.resetClientClass();
        }
    }
    @SubscribeEvent
    public static void onAttackEntity(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        Lunacy.LOGGER.debug("DEBUG: AttackEntityEvent fired, canceled=" + event.isCanceled());
        if (event.getEntity() instanceof ServerPlayer serverPlayer
                && event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target) {
            com.CoffeDino.lunacy.item.Custom.RocaItem.tryTriggerBoulder(serverPlayer, target);
        }
    }


    // Client-side events
    @EventBusSubscriber(modid = Lunacy.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        private static boolean hasCheckedRace = false;
        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(ModModelLayers.GRUCK, ShieldModel::createLayer);
        }
        @SubscribeEvent
        public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerItem(new IClientItemExtensions() {
                private final BlockEntityWithoutLevelRenderer renderer = new GruckRenderer();
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return renderer;
                }
            }, ModItems.GRUCK.get());
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            Lunacy.LOGGER.debug("DEBUG: Client received login event for: " + player.getName().getString());
            hasCheckedRace = false;

            if (player == Minecraft.getInstance().player) {

                Minecraft.getInstance().execute(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Minecraft.getInstance().tell(() -> {
                        checkAndShowRaceScreen(player, "login");
                    });
                });
            }
        }

        @SubscribeEvent
        public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof Player player && player == Minecraft.getInstance().player) {
                Lunacy.LOGGER.debug("DEBUG: Client entity join world for: " + player.getName().getString());

                if (!hasCheckedRace) {
                    Minecraft.getInstance().execute(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Minecraft.getInstance().tell(() -> {
                            checkAndShowRaceScreen(player, "join world");
                        });
                    });
                }
            }
        }

        private static void checkAndShowRaceScreen(Player player, String context) {
            hasCheckedRace = true;

            Lunacy.LOGGER.debug("DEBUG: ===== CLIENT RACE CHECK =====");
            Lunacy.LOGGER.debug("DEBUG: Context: " + context);

            boolean hasChosenRace = races.hasChosenRace(player);
            races.Race currentRace = races.getPlayerRace(player);

            Lunacy.LOGGER.debug("DEBUG: Client - Has chosen race: " + hasChosenRace);
            Lunacy.LOGGER.debug("DEBUG: Client - Current race: " + (currentRace != null ? currentRace.getDisplayName() : "null"));

            if (!hasChosenRace) {
                Lunacy.LOGGER.debug("DEBUG: Showing race selection screen from " + context);
                Minecraft.getInstance().setScreen(new RaceSelectionScreen());
            } else {
                Lunacy.LOGGER.debug("DEBUG: Race already chosen and synced: " + currentRace);
            }
            Lunacy.LOGGER.debug("DEBUG: ===== CLIENT RACE CHECK END =====");
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FireDomainManager.forceDespawnFor(player.getUUID());
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            boolean hasGreatsword = (mainHand.getItem() instanceof ViridyumGreatswordItem) ||
                    (offHand.getItem() instanceof ViridyumGreatswordItem);

            if (hasGreatsword && ViridyumGreatswordItem.tryChronobreak(player)) {
                event.setCanceled(true);
            }
        }
    }
}