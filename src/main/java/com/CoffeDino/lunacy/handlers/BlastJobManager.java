package com.CoffeDino.lunacy.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

@EventBusSubscriber(modid = "lunacy")
public class BlastJobManager {

    private static final int COLUMNS_PER_TICK = 24;
    private static final float BLAST_DAMAGE_PERCENT = 0.20f;

    private record ColumnTask(int x, int z, int topY, int bottomY, int surfaceY, double dist) {}

    private static class ClearJob {
        final ResourceKey<Level> dimension;
        final Deque<ColumnTask> pending;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        int deepestY = Integer.MAX_VALUE;
        final int surfaceY;
        final int lavaLifetimeTicks;

        ClearJob(ResourceKey<Level> dimension, Deque<ColumnTask> pending, int surfaceY, int lavaLifetimeTicks) {
            this.dimension = dimension;
            this.pending = pending;
            this.surfaceY = surfaceY;
            this.lavaLifetimeTicks = lavaLifetimeTicks;
        }
    }

    private record RevertJob(ResourceKey<Level> dimension, int minX, int maxX, int minZ, int maxZ,
                             int topY, int bottomY, long expireAtTick) {}

    private static final List<ClearJob> ACTIVE_CLEAR_JOBS = new ArrayList<>();
    private static final List<RevertJob> ACTIVE_REVERT_JOBS = new ArrayList<>();

    public static void queueBlast(ServerLevel level, Player player, Vec3 origin, Vec3 forward, double range,
                                  double startOffset, double halfAngleDeg,
                                  int clearAbove, int minDownDepth, int maxDownDepth,
                                  int lavaLifetimeTicks) {

        int minBuild = level.getMinBuildHeight();
        int maxBuild = level.getMaxBuildHeight() - 1;

        int feetY = Mth.floor(origin.y);
        int topClearY = Math.min(feetY + clearAbove, maxBuild);

        int baseX = Mth.floor(origin.x);
        int baseZ = Mth.floor(origin.z);
        int intRange = (int) Math.ceil(range);

        List<ColumnTask> columnList = new ArrayList<>();

        for (int dx = -intRange; dx <= intRange; dx++) {
            for (int dz = -intRange; dz <= intRange; dz++) {
                int worldX = baseX + dx;
                int worldZ = baseZ + dz;

                double toX = (worldX + 0.5) - origin.x;
                double toZ = (worldZ + 0.5) - origin.z;
                double dist = Math.sqrt(toX * toX + toZ * toZ);

                if (dist > range || dist < startOffset) continue;

                double dot = (toX * forward.x + toZ * forward.z) / dist;
                double angleDeg = Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));
                if (angleDeg > halfAngleDeg) continue;

                double depthRatio = Mth.clamp((dist - startOffset) / Math.max(range - startOffset, 0.001), 0.0, 1.0);
                int downDepth = Math.round((float) Mth.lerp(depthRatio, minDownDepth, maxDownDepth));

                int bottomY = Math.max(feetY - downDepth, minBuild);
                columnList.add(new ColumnTask(worldX, worldZ, topClearY, bottomY, feetY, dist));
            }
        }

        columnList.sort(Comparator.comparingDouble(ColumnTask::dist));

        Deque<ColumnTask> columns = new ArrayDeque<>(columnList);
        applyConeDamage(level, player, origin, forward, range, startOffset, halfAngleDeg,
                feetY, clearAbove, maxDownDepth, baseX, baseZ, intRange);

        ClearJob job = new ClearJob(level.dimension(), columns, feetY, lavaLifetimeTicks);
        ACTIVE_CLEAR_JOBS.add(job);
    }

    private static void applyConeDamage(ServerLevel level, Player player, Vec3 origin, Vec3 forward,
                                        double range, double startOffset, double halfAngleDeg,
                                        int feetY, int clearAbove, int maxDownDepth,
                                        int baseX, int baseZ, int intRange) {
        AABB coneBox = new AABB(baseX - intRange, feetY - maxDownDepth, baseZ - intRange,
                baseX + intRange, feetY + clearAbove, baseZ + intRange);

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, coneBox,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            double toX = target.getX() - origin.x;
            double toZ = target.getZ() - origin.z;
            double dist = Math.sqrt(toX * toX + toZ * toZ);
            if (dist > range || dist < startOffset) continue;

            double dot = (toX * forward.x + toZ * forward.z) / dist;
            double angleDeg = Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));
            if (angleDeg > halfAngleDeg) continue;

            float damage = target.getMaxHealth() * BLAST_DAMAGE_PERCENT;
            target.hurt(player.damageSources().playerAttack(player), damage);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        ACTIVE_CLEAR_JOBS.removeIf(job -> processClearJob(server, job));

        long now = server.getTickCount();
        ACTIVE_REVERT_JOBS.removeIf(job -> {
            if (now < job.expireAtTick()) return false;
            sweepRegionClearOfLava(server, job);
            return true;
        });
    }

    private static boolean processClearJob(MinecraftServer server, ClearJob job) {
        ServerLevel level = server.getLevel(job.dimension);
        if (level == null) return true;

        int processed = 0;
        ColumnTask firstThisTick = null;

        while (processed < COLUMNS_PER_TICK && !job.pending.isEmpty()) {
            ColumnTask task = job.pending.poll();
            if (firstThisTick == null) firstThisTick = task;

            clearColumn(level, task);

            job.minX = Math.min(job.minX, task.x());
            job.maxX = Math.max(job.maxX, task.x());
            job.minZ = Math.min(job.minZ, task.z());
            job.maxZ = Math.max(job.maxZ, task.z());
            job.deepestY = Math.min(job.deepestY, task.bottomY());

            level.sendParticles(ParticleTypes.LAVA,
                    task.x() + 0.5, task.surfaceY() + 0.3, task.z() + 0.5,
                    1, 0.15, 0.05, 0.15, 0.0);

            level.sendParticles(ParticleTypes.EXPLOSION,
                    task.x() + 0.5, task.surfaceY() + 0.5, task.z() + 0.5,
                    1, 0.2, 0.2, 0.2, 0.0);

            processed++;
        }

        if (firstThisTick != null) {
            level.playSound(null, firstThisTick.x(), firstThisTick.surfaceY(), firstThisTick.z(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    1.0f, 0.9f + level.random.nextFloat() * 0.2f);

            if (server.getTickCount() % 4 == 0) {
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        firstThisTick.x() + 0.5, firstThisTick.surfaceY() + 0.5, firstThisTick.z() + 0.5,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        if (job.pending.isEmpty()) {
            if (job.minX != Integer.MAX_VALUE) {
                long expireAt = server.getTickCount() + job.lavaLifetimeTicks;
                ACTIVE_REVERT_JOBS.add(new RevertJob(
                        job.dimension, job.minX, job.maxX, job.minZ, job.maxZ,
                        job.surfaceY, job.deepestY, expireAt
                ));
            }
            return true;
        }
        return false;
    }

    private static void clearColumn(ServerLevel level, ColumnTask task) {
        boolean hadBlockAtLavaPos = false;
        BlockPos lavaPos = new BlockPos(task.x(), task.surfaceY() - 1, task.z());
        BlockState lavaPosState = level.getBlockState(lavaPos);

        if (!lavaPosState.isAir() && !lavaPosState.is(Blocks.LAVA)) {
            hadBlockAtLavaPos = true;
        }

        for (int y = task.topY(); y >= task.bottomY(); y--) {
            BlockPos pos = new BlockPos(task.x(), y, task.z());
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            if (state.is(Blocks.BEDROCK)) continue;
            if (state.getDestroySpeed(level, pos) < 0) continue;

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }

        if (hadBlockAtLavaPos) {
            level.setBlock(lavaPos, Blocks.LAVA.defaultBlockState(), 3);
        }
    }

    private static void sweepRegionClearOfLava(MinecraftServer server, RevertJob job) {
        ServerLevel level = server.getLevel(job.dimension());
        if (level == null) return;

        for (int x = job.minX(); x <= job.maxX(); x++) {
            for (int z = job.minZ(); z <= job.maxZ(); z++) {
                for (int y = job.topY(); y >= job.bottomY(); y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(Blocks.LAVA)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}