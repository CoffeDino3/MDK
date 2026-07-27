package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.classes.PlayerClasses;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ScytheItem extends SwordItem {
    private final float attackDamage;
    private final float attackSpeed;
    private final float sweepRange;
    private static final float EXECUTE_HEALTH_THRESHOLD = 0.25f;
    private static final float EXECUTE_BONUS_MULTIPLIER = 0.5f;
    private static final float SOUL_HARVEST_HEAL_PERCENT = 0.15f;

    public ScytheItem(Tier tier, float attackDamage, float attackSpeed, float sweepRange, Properties properties) {
        super(tier, properties);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.sweepRange = sweepRange;
    }

    public ScytheItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        this(tier, attackDamage, attackSpeed, 4.0f, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return createAttributes(attackDamage, attackSpeed, sweepRange);
    }

    protected static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed, float sweepRange) {
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
                .add(
                        Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, sweepRange, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    public float getSweepRange() {
        return sweepRange;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player
                && PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.REAPER
                && isLowHealth(target)) {
            applySoulHarvest(player, target);
        }
        return true;
    }

    private boolean isLowHealth(LivingEntity target) {
        return target.getHealth() / target.getMaxHealth() <= EXECUTE_HEALTH_THRESHOLD;
    }

    private void applySoulHarvest(Player player, LivingEntity target) {
        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float executeBonus = baseDamage * EXECUTE_BONUS_MULTIPLIER;
        target.hurt(target.damageSources().playerAttack(player), executeBonus);
        player.heal(executeBonus * SOUL_HARVEST_HEAL_PERCENT);
    }
}