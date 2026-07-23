package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.CoffeDino.lunacy.item.Custom.PhaetonItem.*;

@EventBusSubscriber(modid = "lunacy")
public class PhaetonAbilityHandler {

    private static final int FIRE_PARTICLES_PER_TICK = 4;
    private static final Map<UUID, Vec3> diveDirections = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Level level = player.level();

        long riseEnd = player.getData(ModAttachments.PHAETON_RISE_END.get());
        long launchEnd = player.getData(ModAttachments.PHAETON_LAUNCH_END.get());
        boolean diving = player.getData(ModAttachments.PHAETON_DIVING.get());

        if (riseEnd == 0 && launchEnd == 0 && !diving) return;
        long now = level.getGameTime();
        if (!diving) {
            if (now % 2 == 0) spawnFireParticles(player);
            if (riseEnd != 0 && now < riseEnd) {
                player.fallDistance = 0;
                return;
            }

            if (riseEnd != 0 && now >= riseEnd) {
                player.setNoGravity(true);
                player.setData(ModAttachments.PHAETON_RISE_END.get(), 0L);
                NetworkHandler.syncPhaetonStateToClient(player, 0L, launchEnd, false);
            }

            player.setDeltaMovement(player.getDeltaMovement().x * 0.85, HOVER_Y, player.getDeltaMovement().z * 0.85);
            player.fallDistance = 0;
            if (now % 4 == 0) {
                player.hurtMarked = true;
            }

            if (now >= launchEnd) {
                Vec3 look = player.getLookAngle().normalize();
                diveDirections.put(player.getUUID(), look);
                player.setDeltaMovement(look.scale(DIVE_SPEED));
                player.hurtMarked = true;
                player.setData(ModAttachments.PHAETON_LAUNCH_END.get(), 0L);
                player.setData(ModAttachments.PHAETON_DIVING.get(), true);
                NetworkHandler.syncPhaetonStateToClient(player, 0L, 0L, true);

                level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.9f);
            }
            return;
        }
        if (now % 2 == 0) spawnFireParticles(player);

        Vec3 dir = diveDirections.getOrDefault(player.getUUID(), player.getLookAngle().normalize());
        player.setDeltaMovement(dir.scale(DIVE_SPEED));
        player.fallDistance = 0;
        if (now % 4 == 0) {
            player.hurtMarked = true;
        }
        if (player.onGround() || player.horizontalCollision || player.verticalCollision) {
            player.setData(ModAttachments.PHAETON_DIVING.get(), false);
            player.setNoGravity(false);
            diveDirections.remove(player.getUUID());
            NetworkHandler.syncPhaetonStateToClient(player, 0L, 0L, false);
            if (level instanceof ServerLevel serverLevel) {
                explode(serverLevel, player);
            }
        }
    }

    private static void spawnFireParticles(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(ParticleTypes.FLAME,
                player.getX(), player.getY() + 1.0, player.getZ(),
                FIRE_PARTICLES_PER_TICK, 0.3, 0.5, 0.3, 0.02);
    }

    private static void explode(ServerLevel level, ServerPlayer player) {
        Vec3 pos = player.position();

        level.explode(player, pos.x, pos.y, pos.z, EXPLOSION_POWER,
                true, Level.ExplosionInteraction.MOB);

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 0.5, pos.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.5, pos.z, 80, 2.5, 1.0, 2.5, 0.08);
        level.sendParticles(ParticleTypes.LAVA, pos.x, pos.y + 0.5, pos.z, 20, 1.5, 0.5, 1.5, 0.0);

        igniteFlammableBlocks(level, player.blockPosition());

        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2f, 0.8f);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 0.8f, 0.6f);
    }

    public static void igniteFlammableBlocks(ServerLevel level, BlockPos center) {
        for (int x = -IGNITE_RADIUS; x <= IGNITE_RADIUS; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -IGNITE_RADIUS; z <= IGNITE_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) continue;

                    BlockPos belowPos = pos.below();
                    BlockState below = level.getBlockState(belowPos);
                    if (below.isFlammable(level, belowPos, Direction.UP)) {
                        level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                    }
                }
            }
        }
    }
}