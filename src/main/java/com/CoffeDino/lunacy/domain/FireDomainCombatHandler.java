package com.CoffeDino.lunacy.domain;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = "lunacy")
public class FireDomainCombatHandler {

    @SubscribeEvent
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        FireDomain domain = FireDomainManager.domainContaining(target, target.getUUID());
        if (domain == null) return;

        if (event.getSource().is(DamageTypes.ON_FIRE)) {
            event.setNewDamage(target.getMaxHealth() * FireDomain.DOMAIN_FIRE_DAMAGE_PERCENT);
        } else if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setNewDamage(event.getNewDamage() * 2.0F);
        }
    }
}