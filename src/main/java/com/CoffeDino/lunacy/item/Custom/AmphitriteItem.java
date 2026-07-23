package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.entity.AmphitriteOrbEntity;
import com.CoffeDino.lunacy.events.AmphitriteCooldownAttachments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;

public class AmphitriteItem extends SpellbladeItem {

    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 1600;
    public static final int ORB_COUNT = 10;

    private static final double TARGET_SEARCH_RADIUS = 24.0;
    private static final double ORB_SEARCH_RADIUS = 10.0;

    public AmphitriteItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            boolean summoned = player.getData(ModAttachments.AMPHITRITE_SUMMONED.get());
            if (!summoned) {
                summonConstellation(level, player);
                player.setData(ModAttachments.AMPHITRITE_SUMMONED.get(), true);
                level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_UNDERWATER_ENTER,
                        SoundSource.PLAYERS, 0.8f, 1.3f); // placeholder sfx
            } else {
                launchOrbs(level, player);
                player.setData(ModAttachments.AMPHITRITE_SUMMONED.get(), false);
                AmphitriteCooldownAttachments.applyCooldown(player, this, level.getGameTime());
                level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_3.value(),
                        SoundSource.PLAYERS, 1.0f, 1.1f); // placeholder sfx
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void summonConstellation(Level level, Player player) {
        for (int slot = 0; slot < ORB_COUNT; slot++) {
            level.addFreshEntity(new AmphitriteOrbEntity(level, player, slot));
        }
    }

    private static final int LAUNCH_STAGGER_TICKS = 40;

    private void launchOrbs(Level level, Player player) {
        LivingEntity target = findNearestEnemy(level, player);

        List<AmphitriteOrbEntity> orbs = level.getEntitiesOfClass(AmphitriteOrbEntity.class,
                player.getBoundingBox().inflate(ORB_SEARCH_RADIUS),
                orb -> player.getUUID().equals(orb.getOwnerUUID()) && orb.isFloating());

        orbs.sort(Comparator.comparingInt((AmphitriteOrbEntity orb) -> orb.getSlot()).reversed());

        int delay = 0;
        for (AmphitriteOrbEntity orb : orbs) {
            orb.scheduleLaunch(target, delay);
            delay += LAUNCH_STAGGER_TICKS;
        }
    }

    private LivingEntity findNearestEnemy(Level level, Player player) {
        return level.getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(TARGET_SEARCH_RADIUS),
                        e -> e.isAlive() && e != player)
                .stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);
    }
}