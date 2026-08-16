package com.CoffeDino.lunacy;

import com.CoffeDino.lunacy.races.races.Race;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.Map;

public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final Map<Race, Double> DEFAULT_HEALTH_BONUS = new EnumMap<>(Race.class);
    private static final Map<Race, Double> DEFAULT_HEALTH_GROWTH_PER_10 = new EnumMap<>(Race.class);
    private static final Map<Race, Double> DEFAULT_ARMOR_PER_TIER_AFTER_20 = new EnumMap<>(Race.class);
    private static final Map<Race, Double> DEFAULT_ATTACK_DAMAGE_PER_10 = new EnumMap<>(Race.class);

    static {
        DEFAULT_HEALTH_BONUS.put(Race.SCULK, 20.0);
        DEFAULT_HEALTH_BONUS.put(Race.WARDER, 10.0);
        DEFAULT_HEALTH_BONUS.put(Race.ENDER, 0.0);
        DEFAULT_HEALTH_BONUS.put(Race.PHANTOM, 5.0);
        DEFAULT_HEALTH_BONUS.put(Race.LOVER, -4.0);
        DEFAULT_HEALTH_BONUS.put(Race.BELIEVER, 5.0);
        DEFAULT_HEALTH_BONUS.put(Race.ANGELBORN, 0.0);
        DEFAULT_HEALTH_BONUS.put(Race.VAMPIREBORN, -5.0);
        DEFAULT_HEALTH_BONUS.put(Race.ETHEREAL, 10.0);
        DEFAULT_HEALTH_BONUS.put(Race.CELESTIAL, 5.0);
        DEFAULT_HEALTH_BONUS.put(Race.GATEKEEPER, 10.0);

        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.SCULK, 10.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.WARDER, 8.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.GATEKEEPER, 8.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.ANGELBORN, 6.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.ENDER, 5.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.CELESTIAL, 6.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.BELIEVER, 8.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.ETHEREAL, 6.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.PHANTOM, 5.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.VAMPIREBORN, 6.0);
        DEFAULT_HEALTH_GROWTH_PER_10.put(Race.LOVER, 4.0);

        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.SCULK, 7.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.GATEKEEPER, 5.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.WARDER, 5.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.ANGELBORN, 4.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.ENDER, 3.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.VAMPIREBORN, 2.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.CELESTIAL, 3.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.BELIEVER, 3.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.ETHEREAL, 2.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.PHANTOM, 1.0);
        DEFAULT_ARMOR_PER_TIER_AFTER_20.put(Race.LOVER, 2.0);

        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.SCULK, 9.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.WARDER, 7.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.VAMPIREBORN, 6.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.GATEKEEPER, 5.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.CELESTIAL, 5.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.ANGELBORN, 5.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.PHANTOM, 4.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.ENDER, 4.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.BELIEVER, 3.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.LOVER, 2.0);
        DEFAULT_ATTACK_DAMAGE_PER_10.put(Race.ETHEREAL, 2.0);
    }
    private static final Map<Race, ModConfigSpec.DoubleValue> HEALTH_BONUS = new EnumMap<>(Race.class);
    private static final Map<Race, ModConfigSpec.DoubleValue> HEIGHT = new EnumMap<>(Race.class);
    private static final Map<Race, ModConfigSpec.DoubleValue> WIDTH = new EnumMap<>(Race.class);
    private static final Map<Race, ModConfigSpec.DoubleValue> HEALTH_GROWTH_PER_10 = new EnumMap<>(Race.class);
    private static final Map<Race, ModConfigSpec.DoubleValue> ARMOR_PER_TIER_AFTER_20 = new EnumMap<>(Race.class);
    private static final Map<Race, ModConfigSpec.DoubleValue> ATTACK_DAMAGE_PER_10 = new EnumMap<>(Race.class);

    public static final ModConfigSpec.IntValue AMPLIFIER_LEVEL_CAP;
    public static final ModConfigSpec.BooleanValue LEVEL_25_PERMA_BUFFS_ENABLED;
    public static final ModConfigSpec.IntValue LEVEL_25_BUFF_THRESHOLD;
    public static final ModConfigSpec.IntValue ARMOR_BONUS_START_LEVEL;

    static {
        BUILDER.push("general");
        AMPLIFIER_LEVEL_CAP = BUILDER
                .comment("Max amplifier tier a race's ability effects can reach from leveling (level / 10, capped here).")
                .defineInRange("amplifierLevelCap", 10, 0, 255);
        LEVEL_25_PERMA_BUFFS_ENABLED = BUILDER
                .comment("Whether races get their extra permanent buff at the level-25 milestone.")
                .define("level25PermaBuffsEnabled", true);
        LEVEL_25_BUFF_THRESHOLD = BUILDER
                .comment("Level required to unlock a race's extra permanent buff.")
                .defineInRange("level25BuffThreshold", 25, 1, 1000);
        ARMOR_BONUS_START_LEVEL = BUILDER
                .comment("Level at which races start gaining bonus armor/toughness from leveling.")
                .defineInRange("armorBonusStartLevel", 20, 1, 1000);
        BUILDER.pop();

        for (Race race : Race.values()) {
            BUILDER.push(race.getId());

            HEALTH_BONUS.put(race, BUILDER
                    .comment("Base max-health bonus (can be negative) for " + race.getDisplayName() + ".")
                    .defineInRange("baseHealthBonus", DEFAULT_HEALTH_BONUS.get(race), -19, 1000.0));

            HEALTH_GROWTH_PER_10.put(race, BUILDER
                    .comment("Extra max health gained per 10 levels.")
                    .defineInRange("healthGrowthPerTenLevels", DEFAULT_HEALTH_GROWTH_PER_10.get(race), 0, 1000.0));

            ARMOR_PER_TIER_AFTER_20.put(race, BUILDER
                    .comment("Armor points gained per tier once armorBonusStartLevel is reached (toughness scales at 25% of this).")
                    .defineInRange("armorPerTierAfterThreshold", DEFAULT_ARMOR_PER_TIER_AFTER_20.get(race), 0.0, 1000.0));

            ATTACK_DAMAGE_PER_10.put(race, BUILDER
                    .comment("Extra attack damage gained per 10 levels.")
                    .defineInRange("attackDamagePerTenLevels", DEFAULT_ATTACK_DAMAGE_PER_10.get(race), 0, 1000.0));

            HEIGHT.put(race, BUILDER
                    .comment("Hitbox height (blocks) for " + race.getDisplayName() + ".")
                    .defineInRange("height", (double) race.getHeight(), 0.1, 16.0));

            WIDTH.put(race, BUILDER
                    .comment("Hitbox width (blocks) for " + race.getDisplayName() + ".")
                    .defineInRange("width", (double) race.getWidth(), 0.1, 16.0));

            BUILDER.pop();
        }
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static float healthBonus(Race race) { return HEALTH_BONUS.get(race).get().floatValue(); }
    public static float healthGrowthPerTenLevels(Race race) { return HEALTH_GROWTH_PER_10.get(race).get().floatValue(); }
    public static float armorPerTierAfterThreshold(Race race) { return ARMOR_PER_TIER_AFTER_20.get(race).get().floatValue(); }
    public static float attackDamagePerTenLevels(Race race) { return ATTACK_DAMAGE_PER_10.get(race).get().floatValue(); }
    public static float height(Race race) { return HEIGHT.get(race).get().floatValue(); }
    public static float width(Race race) { return WIDTH.get(race).get().floatValue(); }
}