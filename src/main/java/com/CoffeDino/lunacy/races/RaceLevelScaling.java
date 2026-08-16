package com.CoffeDino.lunacy.races;

import com.CoffeDino.lunacy.Config;

public final class RaceLevelScaling {

    private RaceLevelScaling() {}

    public static int genericAmplifierBonus(int level) {
        return Math.min(Config.AMPLIFIER_LEVEL_CAP.get(), level / 10);
    }

    public static float genericDamageRatioMultiplier(int level) {
        return 1.0f + (level / 10) * 0.05f;
    }

    public static float healthGrowthPerTenLevels(races.Race race) {
        return Config.healthGrowthPerTenLevels(race);
    }

    public static float levelHealthBonus(races.Race race, int level) {
        int tens = level / 10;
        return tens * healthGrowthPerTenLevels(race);
    }

    public static float armorPerTierAfter20(races.Race race) {
        return Config.armorPerTierAfterThreshold(race);
    }

    public static float levelArmorBonus(races.Race race, int level) {
        int startLevel = Config.ARMOR_BONUS_START_LEVEL.get();
        if (level < startLevel) return 0f;
        int tiers = ((level - startLevel) / 10) + 1;
        return tiers * armorPerTierAfter20(race);
    }

    public static float levelToughnessBonus(races.Race race, int level) {
        return levelArmorBonus(race, level) * 0.25f;
    }

    public static float attackDamagePerTenLevels(races.Race race) {
        return Config.attackDamagePerTenLevels(race);
    }

    public static float levelAttackDamageBonus(races.Race race, int level) {
        int tens = level / 10;
        return tens * attackDamagePerTenLevels(race);
    }

    public static boolean hasReachedLevel25(int level) {
        return Config.LEVEL_25_PERMA_BUFFS_ENABLED.get() && level >= Config.LEVEL_25_BUFF_THRESHOLD.get();
    }

    public static int tierEvery(int level, int interval) {
        return level / interval;
    }
}