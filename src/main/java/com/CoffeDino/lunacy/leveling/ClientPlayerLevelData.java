package com.CoffeDino.lunacy.leveling;
public class ClientPlayerLevelData {
    private static int level = 1;
    private static int xp = 0;
    private static int xpToNextLevel = PlayerLevels.getXpToNextLevel(1);

    public static void update(int newLevel, int newXp, int newXpToNextLevel) {
        level = newLevel;
        xp = newXp;
        xpToNextLevel = newXpToNextLevel;
    }

    public static int getLevel() {
        return level;
    }

    public static int getXp() {
        return xp;
    }

    public static int getXpToNextLevel() {
        return xpToNextLevel;
    }
}