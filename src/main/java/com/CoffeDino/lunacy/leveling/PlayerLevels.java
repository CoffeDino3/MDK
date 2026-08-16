package com.CoffeDino.lunacy.leveling;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.network.NetworkHandler;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class PlayerLevels {
    private static final int BASE_XP = 40;
    private static final int XP_PER_LEVEL = 10;

    public static int getXpToNextLevel(int currentLevel) {
        return BASE_XP + (currentLevel * XP_PER_LEVEL);
    }

    public static int getLevel(ServerPlayer player) {
        LevelDataManager dataManager = LevelDataManager.get(player);
        return dataManager.getLevel(player.getUUID());
    }

    public static int getXp(ServerPlayer player) {
        LevelDataManager dataManager = LevelDataManager.get(player);
        return dataManager.getXp(player.getUUID());
    }

    public static void addXp(ServerPlayer player, int amount) {
        if (amount <= 0) return;

        LevelDataManager dataManager = LevelDataManager.get(player);
        UUID id = player.getUUID();

        int level = dataManager.getLevel(id);
        int xp = dataManager.getXp(id) + amount;
        int startingLevel = level;

        int xpNeeded = getXpToNextLevel(level);
        while (xp >= xpNeeded) {
            xp -= xpNeeded;
            level++;
            xpNeeded = getXpToNextLevel(level);
        }

        dataManager.setLevel(id, level);
        dataManager.setXp(id, xp);

        if (level > startingLevel) {
            int levelsGained = level - startingLevel;
            if (levelsGained == 1) {
                player.sendSystemMessage(Component.literal("You leveled up! Now level " + level));
            } else {
                player.sendSystemMessage(Component.literal("You gained " + levelsGained + " levels! Now level " + level));
            }
            Lunacy.LOGGER.debug("DEBUG: {} leveled up to {}", player.getName().getString(), level);
            races.onLevelChanged(player);
        }
        NetworkHandler.syncLevelXpToClient(player, dataManager.getLevel(id), dataManager.getXp(id), getXpToNextLevel(dataManager.getLevel(id)));
    }

    public static void setLevel(ServerPlayer player, int level, boolean resetXp) {
        LevelDataManager dataManager = LevelDataManager.get(player);
        UUID id = player.getUUID();

        dataManager.setLevel(id, level);
        if (resetXp) {
            dataManager.setXp(id, 0);
        }
        races.onLevelChanged(player);
        NetworkHandler.syncLevelXpToClient(player, dataManager.getLevel(id), dataManager.getXp(id), getXpToNextLevel(dataManager.getLevel(id)));
    }

    public static void clear(ServerPlayer player) {
        LevelDataManager dataManager = LevelDataManager.get(player);
        UUID id = player.getUUID();

        dataManager.clear(id);
        Lunacy.LOGGER.debug("DEBUG: Cleared level data for player: " + player.getName().getString());
        NetworkHandler.syncLevelXpToClient(player, dataManager.getLevel(id), dataManager.getXp(id), getXpToNextLevel(dataManager.getLevel(id)));
    }
    public static void resetLevel(ServerPlayer player) {
        setLevel(player, 1, true);
        player.setHealth(player.getMaxHealth());
        Lunacy.LOGGER.debug("DEBUG: Reset level to 1 for player: {}", player.getName().getString());
    }
}
