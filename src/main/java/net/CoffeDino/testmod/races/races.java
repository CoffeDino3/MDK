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
    public static void resetClientRace() {
        clientRace = null;
        System.out.println("DEBUG: Reset client race for new world");
    }

    public static void setPlayerRace(Player player, Race race) {
        if (player == null) return;

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            RaceDataManager dataManager = RaceDataManager.get(serverPlayer);
            dataManager.setPlayerRace(player.getUUID(), race != null ? race.getId() : "");
            syncRaceToClient(serverPlayer, race);
            System.out.println("DEBUG: Race set on server for " + player.getName().getString() + ": " + (race != null ? race.getDisplayName() : "null"));

            if (race != null) {
                applyRaceEffects(player, race);
            } else {
                clearRaceEffects(player);
            }
        } else {
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
            return clientRace;
        } else if (player instanceof ServerPlayer serverPlayer) {
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
            return clientRace != null;
        } else if (player instanceof ServerPlayer serverPlayer) {
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


    public static void onPlayerJoinWorld(Player player) {
        if (!player.level().isClientSide()) {
            Race race = getPlayerRace(player);
            if (race != null) {
                System.out.println("DEBUG: Applying race effects to " + player.getName().getString() + ": " + race.getDisplayName());
                applyRaceEffects(player, race);
            }
        }
    }


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

        System.out.println("DEBUG: Clearing race effects for " + player.getName().getString());
    }

    private static void applyEnderTraits(Player player) {

    }

    private static void applySculkTraits(Player player) {

    }

    private static void applyWarderTraits(Player player) {

    }

    private static void applyPhantomTraits(Player player) {

    }
}