package com.CoffeDino.lunacy.domain;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = "lunacy")
public class FireDomainManager {

    private static final Map<UUID, FireDomain> ACTIVE = new HashMap<>();
    private static int globalTick = 0;
    public static void beginOrContinue(ServerPlayer player) {
        FireDomain domain = ACTIVE.get(player.getUUID());
        if (domain == null) {
            if (blockedByOtherDomain(player)) return;
            domain = new FireDomain(player.getUUID(), player.serverLevel(), player.blockPosition());
            ACTIVE.put(player.getUUID(), domain);
        }
        domain.growing = true;
        domain.ticksSinceReleased = 0;
    }

    public static void release(ServerPlayer player) {
        FireDomain d = ACTIVE.get(player.getUUID());
        if (d != null) d.release();
    }
    public static void interruptCharge(ServerPlayer player) {
        FireDomain d = ACTIVE.get(player.getUUID());
        if (d != null) forceRelease(d);
    }
    public static void forceRelease(FireDomain d) {
        if (!d.growing) return;
        d.release();
        if (d.level.getPlayerByUUID(d.owner) instanceof ServerPlayer player && player.isUsingItem()) {
            player.releaseUsingItem();
        }
    }
    public static void forceDespawnFor(UUID playerUUID) {
        FireDomain d = ACTIVE.remove(playerUUID);
        if (d != null) {
            restoreAll(d);
        }
    }

    public static boolean hasActiveDomain(UUID uuid) { return ACTIVE.containsKey(uuid); }

    public static Collection<FireDomain> allDomains() { return ACTIVE.values(); }

    public static boolean isInsideOwnDomain(LivingEntity entity, UUID ownerUuid) {
        FireDomain d = ACTIVE.get(ownerUuid);
        return d != null && isInsideEffectZone(d, entity.blockPosition());
    }

    public static FireDomain domainContaining(LivingEntity entity, UUID excludeOwner) {
        for (FireDomain d : ACTIVE.values()) {
            if (d.owner.equals(excludeOwner)) continue;
            if (d.level == entity.level() && isInsideEffectZone(d, entity.blockPosition())) return d;
        }
        return null;
    }
    private static boolean isInsideEffectZone(FireDomain d, BlockPos pos) {
        double dx = pos.getX() - d.center.getX();
        double dz = pos.getZ() - d.center.getZ();
        return (dx * dx + dz * dz) <= d.radius * d.radius;
    }
    private static boolean blockedByOtherDomain(ServerPlayer player) {
        for (FireDomain d : ACTIVE.values()) {
            if (d.owner.equals(player.getUUID()) || d.level != player.serverLevel()) continue;
            double dist = Math.sqrt(player.blockPosition().distSqr(d.center));
            if (dist <= d.radius) return true;
        }
        return false;
    }
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        globalTick++;
        Iterator<FireDomain> it = ACTIVE.values().iterator();
        while (it.hasNext()) {
            FireDomain d = it.next();
            d.age++;
            if (!d.growing) d.ticksSinceReleased++;

            if (d.isExpired()) {
                restoreAll(d);
                it.remove();
                continue;
            }

            expand(d);
            if (globalTick % 4 == 0) applyEffectsAndIgnite(d);
            if (globalTick % 20 == 0) applyFireImmuneDamage(d);
            if (globalTick % 10 == 0) extinguishWater(d);
        }
    }

    private static void expand(FireDomain d) {
        if (d.growing) {
            double cap = FireDomain.MAX_RADIUS;
            for (FireDomain other : ACTIVE.values()) {
                if (other == d || other.level != d.level) continue;
                double dist = Math.sqrt(d.center.distSqr(other.center));
                cap = Math.min(cap, Math.max(FireDomain.MIN_RADIUS, dist - other.radius));
            }
            d.growthCap = cap;
            if (d.radius < d.growthCap) {
                d.radius = Math.min(d.growthCap, d.radius + FireDomain.GROWTH_PER_TICK);
            }
            if (d.radius >= d.growthCap - 1.0E-6) {
                forceRelease(d);
            }
        }

        RingResult ring = computeRing(d);
        Iterator<Map.Entry<BlockPos, BlockState>> it = d.originalStates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, BlockState> e = it.next();
            boolean stillLava = ring.lava.contains(e.getKey());
            boolean stillBarrier = ring.barrier.contains(e.getKey());
            if (!stillLava && !stillBarrier) {
                d.level.setBlock(e.getKey(), e.getValue(), 3);
                d.wallPositions.remove(e.getKey());
                d.barrierPositions.remove(e.getKey());
                it.remove();
            }
        }
        for (BlockPos pos : ring.barrier) {
            if (!d.barrierPositions.contains(pos)) {
                d.originalStates.putIfAbsent(pos, d.level.getBlockState(pos));
                d.level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                d.barrierPositions.add(pos);
            }
        }
        for (BlockPos pos : ring.lava) {
            if (!d.wallPositions.contains(pos)) {
                d.originalStates.putIfAbsent(pos, d.level.getBlockState(pos));
                d.level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
                d.wallPositions.add(pos);
            }
        }
    }

    private record RingResult(Set<BlockPos> lava, Set<BlockPos> barrier) {}

    private static RingResult computeRing(FireDomain d) {
        Set<BlockPos> lava = new HashSet<>();

        double r = d.radius;
        int steps = (int) Math.max(48, Math.ceil((2 * Math.PI * r) / 0.4));
        int baseY = d.center.getY();

        for (int i = 0; i < steps; i++) {
            double angle = (2 * Math.PI * i) / steps;
            int lx = d.center.getX() + (int) Math.round(Math.cos(angle) * r);
            int lz = d.center.getZ() + (int) Math.round(Math.sin(angle) * r);
            for (int h = 0; h < FireDomain.WALL_HEIGHT; h++) {
                lava.add(new BlockPos(lx, baseY + h, lz));
            }
        }
        Set<BlockPos> barrier = new HashSet<>();
        for (BlockPos lavaPos : lava) {
            for (BlockPos neighbor : neighbors6(lavaPos)) {
                if (!lava.contains(neighbor)) {
                    barrier.add(neighbor);
                }
            }
        }

        return new RingResult(lava, barrier);
    }

    private static BlockPos[] neighbors6(BlockPos pos) {
        return new BlockPos[] {
                pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below()
        };
    }

    private static void restoreAll(FireDomain d) {
        for (Map.Entry<BlockPos, BlockState> e : d.originalStates.entrySet()) {
            d.level.setBlock(e.getKey(), e.getValue(), 3);
        }
        d.originalStates.clear();
        d.wallPositions.clear();
        d.barrierPositions.clear();
    }

    private static void applyEffectsAndIgnite(FireDomain d) {
        AABB box = fullColumnBox(d);
        for (LivingEntity entity : d.level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!isInsideEffectZone(d, entity.blockPosition())) continue;
            boolean isOwner = entity.getUUID().equals(d.owner);

            if (isOwner) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, false, false, true));
            } else {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false, true));
                entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 60));
            }
        }
    }
    private static void applyFireImmuneDamage(FireDomain d) {
        AABB box = fullColumnBox(d);
        Player owner = d.level.getPlayerByUUID(d.owner);

        for (LivingEntity entity : d.level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.getUUID().equals(d.owner) || !isInsideEffectZone(d, entity.blockPosition())) continue;
            if (!entity.fireImmune()) continue;

            float tickDamage = entity.getMaxHealth() * FireDomain.DOMAIN_FIRE_DAMAGE_PERCENT;
            DamageSource bypass = owner != null
                    ? d.level.damageSources().indirectMagic(owner, owner)
                    : d.level.damageSources().magic();
            entity.hurt(bypass, tickDamage);
        }
    }
    private static AABB fullColumnBox(FireDomain d) {
        return new AABB(
                d.center.getX() - d.radius, d.level.getMinBuildHeight(), d.center.getZ() - d.radius,
                d.center.getX() + d.radius, d.level.getMaxBuildHeight(), d.center.getZ() + d.radius);
    }

    private static void extinguishWater(FireDomain d) {
        int r = (int) Math.ceil(d.radius);
        int baseY = d.center.getY();
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                if (x * x + z * z > r * r) continue;
                int wx = d.center.getX() + x, wz = d.center.getZ() + z;
                for (int y = baseY - 3; y <= baseY + FireDomain.WALL_HEIGHT + 3; y++) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (d.wallPositions.contains(pos) || d.barrierPositions.contains(pos)) continue;
                    if (d.level.getBlockState(pos).getFluidState().is(FluidTags.WATER)) {
                        d.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}