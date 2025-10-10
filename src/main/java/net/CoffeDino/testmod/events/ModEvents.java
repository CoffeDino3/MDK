package net.CoffeDino.testmod.events;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.client.gui.RaceSelectionScreen;
import net.CoffeDino.testmod.races.races;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        System.out.println("DEBUG: Player logged in event - " + player.getName().getString());

        // Only check on client side
        if (player.level().isClientSide()) {
            System.out.println("DEBUG: Client side, checking race...");

            // Small delay to ensure everything is loaded
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().tell(() -> {
                    checkAndShowRaceScreen(player, "login");
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        System.out.println("DEBUG: Player respawn event - " + player.getName().getString());

        // Only check on client side
        if (player.level().isClientSide()) {
            System.out.println("DEBUG: Client side respawn, checking race...");

            // Longer delay for respawn
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().tell(() -> {
                    Minecraft.getInstance().tell(() -> {
                        checkAndShowRaceScreen(player, "respawn");
                    });
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && event.getLevel().isClientSide()) {
            System.out.println("DEBUG: Player joined world - " + player.getName().getString());

            // Only show if this is the main player (not other players)
            if (player == Minecraft.getInstance().player) {
                System.out.println("DEBUG: Main player joined world, checking race...");

                Minecraft.getInstance().execute(() -> {
                    checkAndShowRaceScreen(player, "join world");
                });
            }
        }
    }

    private static void checkAndShowRaceScreen(Player player, String context) {
        boolean hasChosenRace = races.hasChosenRace(player);
        System.out.println("DEBUG: " + context + " - Has chosen race: " + hasChosenRace);
        System.out.println("DEBUG: " + context + " - Player persistent data: " + player.getPersistentData());

        if (!hasChosenRace) {
            System.out.println("DEBUG: Showing race selection screen from " + context);
            Minecraft.getInstance().setScreen(new RaceSelectionScreen());
        } else {
            System.out.println("DEBUG: Race already chosen: " + races.getPlayerRace(player));
        }
    }
}