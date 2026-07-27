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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SpearItem extends SwordItem {
    private final float attackDamage;
    private final float attackSpeed;
    private final float attackReach;
    private static final float PIERCE_DAMAGE_MULTIPLIER = 0.5f;
    private static final double PIERCE_PROBE_DISTANCE = 1.5;
    private static final double PIERCE_PROBE_RADIUS = 1.0;

    public SpearItem(Tier tier, float attackDamage, float attackSpeed, float attackReach, Properties properties) {
        super(tier, properties);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.attackReach = attackReach;
    }
    public SpearItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        this(tier, attackDamage, attackSpeed, 2.0f, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return createAttributes(attackDamage, attackSpeed, attackReach);
    }

    protected static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed, float attackReach) {
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
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackReach, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player
                && PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.SPEARMAN) {
            applyPierceDamage(player, target);
        }
        return true;
    }

    private void applyPierceDamage(Player player, LivingEntity target) {
        Vec3 look = player.getLookAngle();
        Vec3 piercePos = target.position().add(look.scale(PIERCE_PROBE_DISTANCE));
        AABB pierceBox = new AABB(piercePos, piercePos).inflate(PIERCE_PROBE_RADIUS);

        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float pierceDamage = Math.max(1.0f, baseDamage * PIERCE_DAMAGE_MULTIPLIER);

        player.level().getEntitiesOfClass(LivingEntity.class, pierceBox,
                e -> e != player && e != target && e.isAlive()
        ).forEach(pierced -> pierced.hurt(player.damageSources().playerAttack(player), pierceDamage));
    }
}