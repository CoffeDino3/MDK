package net.CoffeDino.testmod.events;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.client.gui.RaceSelectionScreen;
import net.CoffeDino.testmod.network.NetworkHandler;
import net.CoffeDino.testmod.races.races;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    // Server-side events
    @Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                System.out.println("DEBUG: ===== SERVER PLAYER LOGIN START =====");
                System.out.println("DEBUG: Player logged in on server - " + serverPlayer.getName().getString() + " UUID: " + serverPlayer.getUUID());
                races.onPlayerJoinWorld(serverPlayer);
                races.Race race = races.getPlayerRace(serverPlayer);
                NetworkHandler.syncRaceToClient(serverPlayer, race);
                System.out.println("DEBUG: Synced race to client on login: " + (race != null ? race.getDisplayName() : "null"));
                System.out.println("DEBUG: ===== SERVER PLAYER LOGIN END =====");
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
        }
    }

    // Client-side events
    @Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
}