package net.CoffeDino.testmod.races;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class races {
    private static Race clientRace = null;

    public enum Race {
        SCULK("sculk", "Sculk"),
        WARDER("warder", "Warder"),
        ENDER("ender", "Ender"),
        PHANTOM("phantom", "Phantom");

        private final String id;
        private final String displayName;

        Race(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName(){
            return displayName;
        }
    }

    // Reset client race when joining new world
    public static void resetClientRace() {
        clientRace = null;
        System.out.println("DEBUG: Reset client race for new world");
    }

    public static void setPlayerRace(Player player, Race race) {
        if (player == null) return;

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // On server: save to world-specific data
            RaceDataManager dataManager = RaceDataManager.get(serverPlayer);
            dataManager.setPlayerRace(player.getUUID(), race != null ? race.getId() : "");

            // Sync to client
            syncRaceToClient(serverPlayer, race);
            System.out.println("DEBUG: Race set on server for " + player.getName().getString() + ": " + (race != null ? race.getDisplayName() : "null"));

            if (race != null) {
                applyRaceEffects(player, race);
            } else {
                clearRaceEffects(player);
            }
        } else {
            // On client: just set locally
            clientRace = race;
            System.out.println("DEBUG: Race set on client: " + (race != null ? race.getDisplayName() : "null"));
        }
    }

    public static void setClientRace(Race race) {
        clientRace = race;
        System.out.println("DEBUG: Set client race: " + (race != null ? race.getDisplayName() : "null"));
    }

    public static Race getPlayerRace(Player player) {
        if (player == null) return null;

        if (player.level().isClientSide()) {
            // On client: use local cache ONLY
            return clientRace;
        } else if (player instanceof ServerPlayer serverPlayer) {
            // On server: get from world-specific data
            RaceDataManager dataManager = RaceDataManager.get(serverPlayer);
            String raceId = dataManager.getPlayerRace(player.getUUID());

            if (raceId == null || raceId.isEmpty()) {
                return null;
            }

            for (Race race : Race.values()) {
                if (race.getId().equals(raceId)) {
                    return race;
                }
            }
        }

        return null;
    }

    public static boolean hasChosenRace(Player player) {
        if (player == null) return false;

        if (player.level().isClientSide()) {
            // On client: check local cache ONLY - don't check player NBT
            return clientRace != null;
        } else if (player instanceof ServerPlayer serverPlayer) {
            // On server: check world-specific data
            RaceDataManager dataManager = RaceDataManager.get(serverPlayer);
            return dataManager.hasRace(player.getUUID());
        }

        return false;
    }

    private static void syncRaceToClient(ServerPlayer player, Race race) {
        net.CoffeDino.testmod.network.NetworkHandler.syncRaceToClient(player, race);
    }

    public static void clearPlayerRace(Player player) {
        setPlayerRace(player, null);
        System.out.println("DEBUG: Cleared race for player: " + player.getName().getString());
    }

    // Apply race effects when player joins world
    public static void onPlayerJoinWorld(Player player) {
        if (!player.level().isClientSide()) {
            Race race = getPlayerRace(player);
            if (race != null) {
                System.out.println("DEBUG: Applying race effects to " + player.getName().getString() + ": " + race.getDisplayName());
                applyRaceEffects(player, race);
            }
        }
    }

    // Clear effects when player leaves world
    public static void onPlayerLeaveWorld(Player player) {
        if (!player.level().isClientSide()) {
            clearRaceEffects(player);
        }
    }


    public static void applyRaceEffects(Player player, Race race) {
        clearRaceEffects(player);
        switch (race) {
            case ENDER -> applyEnderTraits(player);
            case SCULK -> applySculkTraits(player);
            case WARDER -> applyWarderTraits(player);
            case PHANTOM -> applyPhantomTraits(player);
        }
    }

    private static void clearRaceEffects(Player player) {
        // Implement effect clearing logic here
        System.out.println("DEBUG: Clearing race effects for " + player.getName().getString());
    }

    private static void applyEnderTraits(Player player) {
        // Implement Ender traits
    }

    private static void applySculkTraits(Player player) {
        // Implement Sculk traits
    }

    private static void applyWarderTraits(Player player) {
        // Implement Warder traits
    }

    private static void applyPhantomTraits(Player player) {
        // Implement Phantom traits
    }
}