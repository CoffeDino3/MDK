package com.CoffeDino.lunacy;

import net.minecraft.world.level.GameRules;

public class LunacyGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> RANDOM_SPELLBLADE_ELEMENT =
            GameRules.register(
                    "lunacyRandomSpellbladeElement",
                    GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(false)
            );
    public static void init() {
    }
}