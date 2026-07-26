package com.CoffeDino.lunacy.domain;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FireDomain {

    public static final double GROWTH_PER_TICK = 0.15;
    public static final double MIN_RADIUS = 2.0;
    public static final double MAX_RADIUS = 20.0;
    public static final int WALL_HEIGHT = 7;
    public static final int MAX_LIFESPAN_TICKS = 20 * 15;
    public static final int LIFESPAN_AFTER_RELEASE_TICKS = 20 * 20;
    public static final float DOMAIN_FIRE_DAMAGE_PERCENT = 0.02F;
    public final UUID owner;
    public final ServerLevel level;
    public final BlockPos center;
    public double radius = MIN_RADIUS;
    public boolean growing = true;
    public int age = 0;
    public int ticksSinceReleased = 0;
    public double growthCap = MAX_RADIUS;

    public final Map<BlockPos, BlockState> originalStates = new HashMap<>();
    public final Set<BlockPos> wallPositions = new HashSet<>();
    public final Set<BlockPos> barrierPositions = new HashSet<>();

    public FireDomain(UUID owner, ServerLevel level, BlockPos center) {
        this.owner = owner;
        this.level = level;
        this.center = center;
    }

    public void release() { this.growing = false; }

    public boolean isExpired() {
        boolean releasedTimeout = !growing && ticksSinceReleased > LIFESPAN_AFTER_RELEASE_TICKS;
        boolean hardTimeout = age > MAX_LIFESPAN_TICKS;
        return releasedTimeout || hardTimeout;
    }
}