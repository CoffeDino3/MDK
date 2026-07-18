package com.CoffeDino.lunacy.events;

import net.minecraft.world.entity.player.Player;

public final class RapierThrowCooldown {

    private static final String TAG_KEY = "testmod_rapier_next_throw_tick";

    private RapierThrowCooldown() {
    }

    public static boolean isReady(Player player) {
        long nextAllowedTick = player.getPersistentData().getLong(TAG_KEY);
        return player.level().getGameTime() >= nextAllowedTick;
    }

    public static void trigger(Player player, int cooldownTicks) {
        long nextAllowedTick = player.level().getGameTime() + cooldownTicks;
        player.getPersistentData().putLong(TAG_KEY, nextAllowedTick);
    }

    public static int ticksRemaining(Player player) {
        long nextAllowedTick = player.getPersistentData().getLong(TAG_KEY);
        long remaining = nextAllowedTick - player.level().getGameTime();
        return (int) Math.max(0, remaining);
    }
}