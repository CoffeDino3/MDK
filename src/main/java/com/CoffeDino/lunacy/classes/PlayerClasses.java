package com.CoffeDino.lunacy.classes;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.LunacyGameRules;
import com.CoffeDino.lunacy.network.NetworkHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerClasses {
    private static PlayerClass clientClass = null;

    public enum PlayerClass {
        SWORDSMAN("swordsman", "Swordsman"),
        SPEARMAN("spearman", "Spearman"),
        VIKING("viking", "Viking"),
        FENCER("fencer", "Fencer"),
        ARCHER("archer", "Archer"),
        ASSASSIN("assassin", "Assassin"),
        GUARDIAN("guardian", "Guardian"),
        SPELLBLADE("spellblade", "Spellblade"),
        CHRONOBLADE("chronoblade", "Chronoblade"),
        REAPER("reaper", "Reaper"),
        GUNSMITH("gunsmith", "Gunsmith");

        private final String id;
        private final String displayName;

        PlayerClass(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static void resetClientClass() {
        clientClass = null;
        Lunacy.LOGGER.debug("DEBUG: Reset client class for new world");
    }

    public static void setPlayerClass(Player player, PlayerClass playerClass) {
        if (player == null) return;

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ClassDataManager dataManager = ClassDataManager.get(serverPlayer);
            dataManager.setPlayerClass(player.getUUID(), playerClass != null ? playerClass.getId() : "");
            syncClassToClient(serverPlayer, playerClass);

            if (playerClass == PlayerClass.SPELLBLADE && dataManager.getPlayerElement(player.getUUID()) == null) {
                handleSpellbladeElementAssignment(serverPlayer, dataManager);
            }

            if (playerClass != null) {
                player.sendSystemMessage(Component.literal("Your class is: " + playerClass.getDisplayName()));
            }
        } else {
            clientClass = playerClass;
        }
    }

    private static void handleSpellbladeElementAssignment(ServerPlayer player, ClassDataManager dataManager) {
        boolean random = player.serverLevel().getGameRules().getBoolean(LunacyGameRules.RANDOM_SPELLBLADE_ELEMENT);

        if (random) {
            SpellbladeElement[] elements = SpellbladeElement.values();
            SpellbladeElement chosen = elements[player.getRandom().nextInt(elements.length)];
            dataManager.setPlayerElement(player.getUUID(), chosen.getId());
            player.sendSystemMessage(Component.literal("The arcane chose your element: " + chosen.getDisplayName()));
        } else {
            NetworkHandler.openElementSelectionForPlayer(player);
        }
    }

    public static void setClientClass(PlayerClass playerClass) {
        clientClass = playerClass;
        Lunacy.LOGGER.debug("DEBUG: Set client class: " + (playerClass != null ? playerClass.getDisplayName() : "null"));
    }

    public static PlayerClass getPlayerClass(Player player) {
        if (player == null) return null;

        if (player.level().isClientSide()) {
            return clientClass;
        } else if (player instanceof ServerPlayer serverPlayer) {
            ClassDataManager dataManager = ClassDataManager.get(serverPlayer);
            String classId = dataManager.getPlayerClass(player.getUUID());

            if (classId == null || classId.isEmpty()) {
                return null;
            }

            for (PlayerClass playerClass : PlayerClass.values()) {
                if (playerClass.getId().equals(classId)) {
                    return playerClass;
                }
            }
        }

        return null;
    }

    public static boolean hasChosenClass(Player player) {
        if (player == null) return false;

        if (player.level().isClientSide()) {
            return clientClass != null;
        } else if (player instanceof ServerPlayer serverPlayer) {
            ClassDataManager dataManager = ClassDataManager.get(serverPlayer);
            return dataManager.hasClass(player.getUUID());
        }

        return false;
    }

    private static void syncClassToClient(ServerPlayer player, PlayerClass playerClass) {
        NetworkHandler.syncClassToClient(player, playerClass);
    }

    public static void clearPlayerClass(Player player) {
        setPlayerClass(player, null);
        Lunacy.LOGGER.debug("DEBUG: Cleared class for player: " + player.getName().getString());
    }
}