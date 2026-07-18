package com.CoffeDino.lunacy.item.Custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class RapierItem extends SwordItem {
    private final float attackDamage;
    private final float attackSpeed;

    private static final double LUNGE_DISTANCE = 6.0;
    private static final float LUNGE_BONUS_DAMAGE = 3.0f;
    private static final int LUNGE_COOLDOWN_TICKS = 40;

    public RapierItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    public RapierItem(Tier tier, Properties properties) {
        this(tier, 4.0f, -1.0f, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return createAttributes(attackDamage, attackSpeed);
    }

    protected static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        performLunge(level, player, stack);
        player.getCooldowns().addCooldown(this, LUNGE_COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    protected void performLunge(Level level, Player player, ItemStack stack) {
        Vec3 look = player.getLookAngle();
        Vec3 dashVelocity = look.scale(2.2);

        player.setDeltaMovement(dashVelocity);
        player.hurtMarked = true;

        if (level instanceof ServerLevel serverLevel) {
            Set<UUID> alreadyHit = new HashSet<>();
            checkLungeCollisions(serverLevel, player, stack, alreadyHit, LUNGE_DISTANCE);
        }

        lastLungeTick.put(player.getUUID(), level.getGameTime());
    }
    private static final Map<UUID, Long> lastLungeTick = new HashMap<>();
    private static final int FALL_REDUCTION_WINDOW_TICKS = 100;

    public static boolean isWithinLungeFallWindow(Player player) {
        Long lungeTick = lastLungeTick.get(player.getUUID());
        if (lungeTick == null) return false;
        return player.level().getGameTime() - lungeTick <= FALL_REDUCTION_WINDOW_TICKS;
    }

    private void checkLungeCollisions(ServerLevel level, Player player, ItemStack stack, Set<UUID> alreadyHit, double remainingDistance) {
        AABB path = player.getBoundingBox().inflate(0.5, 0.2, 0.5)
                .expandTowards(player.getLookAngle().scale(remainingDistance));

        float lungeDamage = isDualWieldingRapiers(player) ? LUNGE_BONUS_DAMAGE * 1.5f : LUNGE_BONUS_DAMAGE;

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, path,
                e -> e != player && e.isAlive() && !alreadyHit.contains(e.getUUID()));

        for (LivingEntity target : targets) {
            alreadyHit.add(target.getUUID());
            target.hurt(player.damageSources().playerAttack(player), lungeDamage);
            stack.hurtEnemy(target, player);
            spawnHitParticles(level, target);
        }
    }
    private void spawnHitParticles(ServerLevel level, LivingEntity target) {
        level.sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                8, 0.3, 0.3, 0.3, 0.1);
    }
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && isDualWieldingRapiers(player)) {
            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float bonusDamage = baseDamage * 0.5f;
            target.hurt(target.damageSources().mobAttack(attacker), bonusDamage);
        }
        return true;
    }

    private boolean isDualWieldingRapiers(Player player) {
        return player.getMainHandItem().getItem() instanceof RapierItem
                && player.getOffhandItem().getItem() instanceof RapierItem;
    }
}