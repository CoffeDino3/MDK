package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.events.PhaetonCooldownAttachments;
import com.CoffeDino.lunacy.network.NetworkHandler;
import com.CoffeDino.lunacy.handlers.PhaetonAbilityHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.CoffeDino.lunacy.handlers.PhaetonAbilityHandler.igniteFlammableBlocks;


public class PhaetonItem extends SpellbladeItem {

    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 1200;
    public static final int RISE_TICKS = 15;
    public static final int HOVER_TICKS = 40;
    public static final double LAUNCH_VELOCITY = 2.3;
    public static final double HOVER_Y = 0.08;
    public static final double DIVE_SPEED = 3.2;
    public static final float EXPLOSION_POWER = 5.0f;
    private static final double DAMAGE_RADIUS = 6.0;
    private static final float DAMAGE_PERCENT_OF_MAX_HEALTH = 0.35f;
    public static final int IGNITE_RADIUS = 4;
    private static long clientRiseEnd = 0L;
    private static long clientLaunchEnd = 0L;
    private static boolean clientDiving = false;

    public PhaetonItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public static void setClientState(long riseEnd, long launchEnd, boolean diving) {
        clientRiseEnd = riseEnd;
        clientLaunchEnd = launchEnd;
        clientDiving = diving;
    }

    public static long getClientRiseEnd() { return clientRiseEnd; }
    public static long getClientLaunchEnd() { return clientLaunchEnd; }
    public static boolean isClientDiving() { return clientDiving; }
    private static void explode(ServerLevel level, ServerPlayer player) {
        Vec3 pos = player.position();

        level.explode(player, pos.x, pos.y, pos.z, EXPLOSION_POWER,
                true, Level.ExplosionInteraction.MOB);
        AABB damageBox = new AABB(pos.subtract(DAMAGE_RADIUS, DAMAGE_RADIUS, DAMAGE_RADIUS),
                pos.add(DAMAGE_RADIUS, DAMAGE_RADIUS, DAMAGE_RADIUS));
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, damageBox, e -> e != player);
        for (LivingEntity target : targets) {
            float damage = target.getMaxHealth() * DAMAGE_PERCENT_OF_MAX_HEALTH;
            target.hurt(level.damageSources().playerAttack(player), damage);
        }

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 0.5, pos.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.5, pos.z, 80, 2.5, 1.0, 2.5, 0.08);
        level.sendParticles(ParticleTypes.LAVA, pos.x, pos.y + 0.5, pos.z, 20, 1.5, 0.5, 1.5, 0.0);
        igniteFlammableBlocks(level, player.blockPosition());
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2f, 0.8f);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 0.8f, 0.6f);
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            player.setDeltaMovement(0, LAUNCH_VELOCITY, 0);
            player.hurtMarked = true;
            player.fallDistance = 0;
            long now = level.getGameTime();
            long riseEnd = now + RISE_TICKS;
            long launchEnd = now + RISE_TICKS + HOVER_TICKS;

            player.setData(ModAttachments.PHAETON_RISE_END.get(), riseEnd);
            player.setData(ModAttachments.PHAETON_LAUNCH_END.get(), launchEnd);
            player.setData(ModAttachments.PHAETON_DIVING.get(), false);
            NetworkHandler.syncPhaetonStateToClient(serverPlayer, riseEnd, launchEnd, false);
            level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);
            PhaetonCooldownAttachments.applyCooldown(serverPlayer, this, now);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}