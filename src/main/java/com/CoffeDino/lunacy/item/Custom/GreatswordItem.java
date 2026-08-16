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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GreatswordItem extends SwordItem {
    private final float attackDamage;
    private final float attackSpeed;
    private static final int COMBO_WINDOW_TICKS = 60;
    private static final float COMBO_BONUS_PER_STACK = 0.2f;
    private static final int MAX_COMBO_STACKS = 3;
    private static final Map<UUID, Long> lastHitTick = new HashMap<>();
    private static final Map<UUID, Integer> comboStacks = new HashMap<>();

    public GreatswordItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
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
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player
                && PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.HEAVY_KNIGHT) {
            applyChronoRhythm(player, target);
        }
        return true;
    }

    private void applyChronoRhythm(Player player, LivingEntity target) {
        UUID id = player.getUUID();
        long now = player.level().getGameTime();
        Long last = lastHitTick.get(id);

        int stacks = (last != null && now - last <= COMBO_WINDOW_TICKS)
                ? Math.min(MAX_COMBO_STACKS, comboStacks.getOrDefault(id, 0) + 1)
                : 0;

        comboStacks.put(id, stacks);
        lastHitTick.put(id, now);

        if (stacks > 0) {
            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float bonusDamage = baseDamage * COMBO_BONUS_PER_STACK * stacks;
            target.hurt(target.damageSources().playerAttack(player), bonusDamage);
        }
    }
}