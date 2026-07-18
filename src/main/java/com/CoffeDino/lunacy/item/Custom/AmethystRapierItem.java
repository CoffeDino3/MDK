package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.entity.FloatingRapierEntity;
import com.CoffeDino.lunacy.entity.ThrownRapierEntity;
import com.CoffeDino.lunacy.events.RapierThrowCooldown;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.CoffeDino.lunacy.effects.EchoingEffect.tryApply;

public class AmethystRapierItem extends RapierItem {

    private static final int FLOATING_RAPIERS = 7;
    private static final int SUMMON_COOLDOWN_TICKS = 600;
    private static final int THROW_COOLDOWN_TICKS = 40;

    private static final int HOMING_MAX_FLIGHT_TICKS = 12;
    private static final float STRAIGHT_LINE_SPEED = 2.4F;
    private static boolean isResolvingProjectileHit = false;

    public AmethystRapierItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    public AmethystRapierItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            List<FloatingRapierEntity> existing = getOwnedFloatingRapiers(player);

            if (!existing.isEmpty()) {
                tryThrowStraight(level, player, stack, existing);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            summonFloatingRapiers(level, player, stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (attacker instanceof Player player && !player.level().isClientSide) {
            tryApply(target);

            if (!isResolvingProjectileHit) {
                List<FloatingRapierEntity> existing = getOwnedFloatingRapiers(player);
                if (!existing.isEmpty()) {
                    launchHomingRapier(player, existing, target, stack);
                }
            }
        }

        return result;
    }

    public static void setResolvingProjectileHit(boolean value) {
        isResolvingProjectileHit = value;
    }

    private void summonFloatingRapiers(Level level, Player player, ItemStack stack) {
        for (int i = 0; i < FLOATING_RAPIERS; i++) {
            FloatingRapierEntity rapier = new FloatingRapierEntity(level, player, stack, i);
            level.addFreshEntity(rapier);
        }
    }

    private Optional<FloatingRapierEntity> pickRapierToDetach(List<FloatingRapierEntity> existing) {
        return existing.stream().min(Comparator.comparingInt(FloatingRapierEntity::getOrbitIndex));
    }

    private void launchHomingRapier(Player player, List<FloatingRapierEntity> existing,
                                    LivingEntity target, ItemStack rapierStack) {
        Optional<FloatingRapierEntity> chosen = pickRapierToDetach(existing);
        if (chosen.isEmpty()) return;

        FloatingRapierEntity detached = chosen.get();
        Level level = player.level();

        ThrownRapierEntity thrown = new ThrownRapierEntity(level, player, rapierStack);
        thrown.setPos(detached.position());
        thrown.setupHoming(target, HOMING_MAX_FLIGHT_TICKS);

        level.addFreshEntity(thrown);
        detached.discard();

        FloatingRapierEntity.reflowFormation(player);
        triggerCooldownIfDepleted(player, existing.size() - 1);
    }

    private void tryThrowStraight(Level level, Player player, ItemStack stack, List<FloatingRapierEntity> existing) {
        if (!RapierThrowCooldown.isReady(player)) {
            return;
        }

        Optional<FloatingRapierEntity> chosen = pickRapierToDetach(existing);
        if (chosen.isEmpty()) return;

        FloatingRapierEntity detached = chosen.get();

        ThrownRapierEntity thrown = new ThrownRapierEntity(level, player, stack);
        thrown.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        thrown.setupStraightLine(player.getLookAngle(), STRAIGHT_LINE_SPEED);

        level.addFreshEntity(thrown);
        detached.discard();

        FloatingRapierEntity.reflowFormation(player);
        RapierThrowCooldown.trigger(player, THROW_COOLDOWN_TICKS);
        triggerCooldownIfDepleted(player, existing.size() - 1);
    }

    private void triggerCooldownIfDepleted(Player player, int remainingAfterConsumption) {
        if (remainingAfterConsumption <= 0) {
            player.getCooldowns().addCooldown(this, SUMMON_COOLDOWN_TICKS);
        }
    }

    private List<FloatingRapierEntity> getOwnedFloatingRapiers(Player player) {
        return player.level().getEntitiesOfClass(FloatingRapierEntity.class,
                player.getBoundingBox().inflate(50.0),
                entity -> player.getUUID().equals(entity.getOwner() == null ? null : entity.getOwner().getUUID()));
    }
}