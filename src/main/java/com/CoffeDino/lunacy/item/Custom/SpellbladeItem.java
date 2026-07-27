package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.classes.ClassDataManager;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.classes.SpellbladeElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class SpellbladeItem extends SwordItem {
    private static final float ARCANE_EDGE_BONUS_MULTIPLIER = 0.2f;

    private static float percentHealthDamage(LivingEntity target, float percent) {
        return Math.max(1.0f, target.getMaxHealth() * percent);
    }

    public SpellbladeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(net.minecraft.world.item.ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof ServerPlayer serverPlayer
                && PlayerClasses.getPlayerClass(serverPlayer) == PlayerClasses.PlayerClass.SPELLBLADE) {
            applyArcaneEdge(serverPlayer, target);

            ClassDataManager dataManager = ClassDataManager.get(serverPlayer);
            String elementId = dataManager.getPlayerElement(serverPlayer.getUUID());
            SpellbladeElement element = SpellbladeElement.fromId(elementId);

            if (element != null) {
                applyElementalEffect(element, target, attacker);
            }
        }

        return result;
    }

    private void applyArcaneEdge(Player player, LivingEntity target) {
        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float bonusDamage = baseDamage * ARCANE_EDGE_BONUS_MULTIPLIER;
        target.hurt(target.damageSources().magic(), bonusDamage);
    }

    private void applyElementalEffect(SpellbladeElement element, LivingEntity target, LivingEntity attacker) {
        switch (element) {
            case FIRE -> {
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 80));
                if (attacker.isOnFire()) {
                    float bonus = percentHealthDamage(target, 0.02f);
                    target.hurt(target.damageSources().magic(), bonus);
                }
            }
            case WATER -> {
                target.setRemainingFireTicks(0);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));

                if (attacker instanceof Player p) {
                    p.setRemainingFireTicks(0);
                }
            }
            case LIGHTNING -> {
                float yourDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float chainDamage = Math.max(1.0f, yourDamage * 0.1f);

                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0));

                target.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0),
                        e -> e != target && e != attacker && e.isAlive()
                ).stream().findFirst().ifPresent(chained -> {
                    chained.hurt(target.damageSources().indirectMagic(attacker, attacker), chainDamage);
                    chained.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0));
                });
            }
            case VOID -> target.hurt(target.damageSources().magic(), percentHealthDamage(target, 0.03f));
            case EARTH -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 4));
            case WIND -> {
                double dx = target.getX() - attacker.getX();
                double dz = target.getZ() - attacker.getZ();
                target.knockback(0.8, -dx, -dz);
                if (attacker instanceof Player p) {
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1));
                }
            }
            case LIGHT -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
            case ETHER -> {
                if (attacker.getRandom().nextFloat() < 0.15f) {
                    target.hurt(target.damageSources().magic(), percentHealthDamage(target, 0.06f));
                }
            }
            case BLOOD -> {
                if (attacker instanceof Player p) {
                    float yourDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    p.heal(yourDamage * 0.5f);
                }
            }
        }
    }
}