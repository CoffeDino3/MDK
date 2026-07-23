package com.CoffeDino.lunacy.client;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
public final class JoroArmedState {
    private static final Map<Integer, Long> ARMED_UNTIL = new HashMap<>();
    private JoroArmedState() {}

    public static void markArmed(LivingEntity entity, long untilGameTime) {
        ARMED_UNTIL.put(entity.getId(), untilGameTime);
    }

    public static boolean isArmed(LivingEntity entity) {
        if (entity == null) return false;
        Long until = ARMED_UNTIL.get(entity.getId());
        if (until == null) return false;

        if (entity.level().getGameTime() >= until) {
            ARMED_UNTIL.remove(entity.getId());
            return false;
        }
        return true;
    }
}
