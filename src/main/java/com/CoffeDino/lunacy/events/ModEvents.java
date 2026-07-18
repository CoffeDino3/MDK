package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.client.gui.RaceSelectionScreen;
import com.CoffeDino.lunacy.entity.FloatingRapierEntity;
import com.CoffeDino.lunacy.item.Custom.BorontItem;
import com.CoffeDino.lunacy.item.Custom.ObsidiaItem;
import com.CoffeDino.lunacy.item.Custom.ViridyumGreatswordItem;
import com.CoffeDino.lunacy.network.NetworkHandler;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;


@EventBusSubscriber(modid = Lunacy.MODID)
public class ModEvents {
    // Server-side events
    @EventBusSubscriber(modid = Lunacy.MODID)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                System.out.println("DEBUG: ===== SERVER PLAYER LOGIN START =====");
                System.out.println("DEBUG: Player logged in on server - " + serverPlayer.getName().getString() + " UUID: " + serverPlayer.getUUID());

                races.Race race = races.getPlayerRace(serverPlayer);
                System.out.println("DEBUG: Retrieved race for player: " + (race != null ? race.getDisplayName() : "null"));


                if (race != null) {
                    races.onPlayerJoinWorld(serverPlayer);
                    NetworkHandler.syncRaceToClient(serverPlayer, race);
                    NetworkHandler.syncSizeToClient(serverPlayer, race.getHeight(), race.getWidth());
                    System.out.println("DEBUG: Synced race to client on login: " + race.getDisplayName());
                } else {
                    System.out.println("DEBUG: No race found for player, skipping race effects and sync");
                    NetworkHandler.syncRaceToClient(serverPlayer, null);
                }
                PlayerClasses.PlayerClass playerClass = PlayerClasses.getPlayerClass(serverPlayer);
                if (playerClass != null) {
                    NetworkHandler.syncClassToClient(serverPlayer, playerClass);
                    System.out.println("DEBUG: Synced class to client on login: " + playerClass.getDisplayName());
                }
                reapplyPersistedCooldown(serverPlayer, ModAttachments.BORONT_COOLDOWN_END, BorontItem.class);
                reapplyPersistedCooldown(serverPlayer, ModAttachments.OBSIDIA_COOLDOWN_END, ObsidiaItem.class);

                System.out.println("DEBUG: ===== SERVER PLAYER LOGIN END =====");
            }
        }

        /**
         * Vanilla ItemCooldowns live only on the transient ServerPlayer object and are never
         * written to player NBT, so they're wiped on every relog. Item-specific attachments
         * (BorontCooldownAttachments, ObsidiaCooldownAttachments, ...) persist each cooldown's
         * expiry tick separately - this re-applies it here if time is still remaining, and clears
         * the stored value if it already ran out while offline. One generic method instead of a
         * copy-pasted reapply per item; add a new call above whenever another item needs this.
         */
        private static void reapplyPersistedCooldown(ServerPlayer serverPlayer,
                                                     DeferredHolder<AttachmentType<?>, AttachmentType<Long>> cooldownAttachment,
                                                     Class<? extends Item> itemClass) {
            long cooldownEnd = serverPlayer.getData(cooldownAttachment);
            if (cooldownEnd <= 0) return;

            // FIX: Use serverPlayer.serverLevel() instead of serverPlayer.level()
            // serverLevel() is guaranteed to be non-null during login
            ServerLevel level = serverPlayer.serverLevel();
            if (level == null) return; // Safety check

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
                // Instantly clean up entities on the server if the player leaves the game
                List<FloatingRapierEntity> rapiers = player.level().getEntitiesOfClass(FloatingRapierEntity.class,
                        player.getBoundingBox().inflate(100.0),
                        entity -> player.getUUID().equals(entity.getOwner() == null ? null : entity.getOwner().getUUID()));

                for (FloatingRapierEntity rapier : rapiers) {
                    rapier.discard();
                }
                System.out.println("DEBUG: Cleared floating rapiers for logging out player: " + player.getName().getString());
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            Player player = event.getEntity();
            System.out.println("DEBUG: Player respawned on server - " + player.getName().getString());
            races.onPlayerJoinWorld(player);
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            Player player = event.getEntity();
            System.out.println("DEBUG: Player changed dimension - " + player.getName().getString());
            races.onPlayerJoinWorld(player);
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            races.resetClientRace();
            PlayerClasses.resetClientClass();
        }
    }


    // Client-side events
    @EventBusSubscriber(modid = Lunacy.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        private static boolean hasCheckedRace = false;

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            System.out.println("DEBUG: Client received login event for: " + player.getName().getString());
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
                System.out.println("DEBUG: Client entity join world for: " + player.getName().getString());

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

            System.out.println("DEBUG: ===== CLIENT RACE CHECK =====");
            System.out.println("DEBUG: Context: " + context);

            boolean hasChosenRace = races.hasChosenRace(player);
            races.Race currentRace = races.getPlayerRace(player);

            System.out.println("DEBUG: Client - Has chosen race: " + hasChosenRace);
            System.out.println("DEBUG: Client - Current race: " + (currentRace != null ? currentRace.getDisplayName() : "null"));

            if (!hasChosenRace) {
                System.out.println("DEBUG: Showing race selection screen from " + context);
                Minecraft.getInstance().setScreen(new RaceSelectionScreen());
            } else {
                System.out.println("DEBUG: Race already chosen and synced: " + currentRace);
            }
            System.out.println("DEBUG: ===== CLIENT RACE CHECK END =====");
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
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