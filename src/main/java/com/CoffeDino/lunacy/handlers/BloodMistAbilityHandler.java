package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.entity.BloodMistEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

@EventBusSubscriber(modid = "lunacy")
public class BloodMistAbilityHandler {

    private static final double MIST_SEARCH_RADIUS = 12.0;

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)) return;

        LivingEntity target = event.getEntity();

        List<BloodMistEntity> mists = player.level().getEntitiesOfClass(BloodMistEntity.class,
                new AABB(player.getX() - MIST_SEARCH_RADIUS, player.getY() - MIST_SEARCH_RADIUS, player.getZ() - MIST_SEARCH_RADIUS,
                        player.getX() + MIST_SEARCH_RADIUS, player.getY() + MIST_SEARCH_RADIUS, player.getZ() + MIST_SEARCH_RADIUS),
                mist -> player.getUUID().equals(mist.getOwnerUUID()));

        for (BloodMistEntity mist : mists) {
            if (mist.isPositionInMist(target.position())) {
                player.heal(event.getNewDamage() * BloodMistEntity.HEAL_PERCENT_OF_MELEE_DAMAGE);
                break;
            }
        }
    }
}