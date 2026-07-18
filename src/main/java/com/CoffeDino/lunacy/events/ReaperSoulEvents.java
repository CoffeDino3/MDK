package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.item.Custom.SoulScytheItem;
import com.CoffeDino.lunacy.player.ReaperSoulData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
@EventBusSubscriber(modid = Lunacy.MODID)
public class ReaperSoulEvents {

    private static final int MAX_STACKS = 10;

    private static final int COOLDOWN = 40;
    private static final float CHANCE = 0.4f;


    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        LivingEntity dead = event.getEntity();
        for (ServerPlayer player : level.players()) {
            if (player.distanceTo(dead) > 32) continue;
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof SoulScytheItem)) continue;
            ReaperSoulData data = player.getData(ModAttachments.REAPER_SOUL);
            long time = level.getGameTime();
            if (time - data.getLastGainTime() < COOLDOWN) continue;
            if (player.getRandom().nextFloat() > CHANCE) continue;

            data.addStack(MAX_STACKS);
            data.setLastGainTime(time);
            applySoulEffect(player, data.getSoulStacks());
        }
    }

    private static void applySoulEffect(ServerPlayer player, int stacks) {

        int amplifier = Math.min(stacks - 1, 9);

        player.addEffect(new MobEffectInstance(
                ModEffects.SOUL_CLAIM,
                300,
                amplifier,
                false,
                true,
                true
        ));
    }
}