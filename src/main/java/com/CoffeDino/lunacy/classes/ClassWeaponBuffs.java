package com.CoffeDino.lunacy.classes;

import com.CoffeDino.lunacy.classes.PlayerClasses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "lunacy")
public class ClassWeaponBuffs {
    private static final float VIKING_RAGE_HEALTH_THRESHOLD = 0.5f;
    private static final float VIKING_RAGE_BONUS_MULTIPLIER = 0.35f;
    private static final float SWORDSMAN_BONUS_MULTIPLIER = 0.30f;
    private static final float ARCHER_BONUS_MULTIPLIER = 0.25f;
    private static final float ARCHER_FULL_DRAW_BONUS_MULTIPLIER = 0.25f;

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        Entity directEntity = event.getSource().getEntity();

        if (directEntity instanceof Player player) {
            applyMeleeClassBonus(event, player);
        } else if (directEntity instanceof Projectile projectile
                && projectile.getOwner() instanceof Player archer
                && PlayerClasses.getPlayerClass(archer) == PlayerClasses.PlayerClass.ARCHER) {
            applyArcherBonus(event, projectile);
        }
    }

    private static void applyMeleeClassBonus(LivingDamageEvent.Pre event, Player player) {
        PlayerClasses.PlayerClass playerClass = PlayerClasses.getPlayerClass(player);
        if (playerClass == null) return;

        if (playerClass == PlayerClasses.PlayerClass.VIKING
                && player.getMainHandItem().getItem() instanceof AxeItem
                && isEnraged(player)) {
            event.setNewDamage(event.getNewDamage() * (1.0f + VIKING_RAGE_BONUS_MULTIPLIER));
        } else if (playerClass == PlayerClasses.PlayerClass.SWORDSMAN
                && player.getMainHandItem().getItem().getClass() == SwordItem.class) {
            event.setNewDamage(event.getNewDamage() * (1.0f + SWORDSMAN_BONUS_MULTIPLIER));
        }
    }

    private static void applyArcherBonus(LivingDamageEvent.Pre event, Projectile projectile) {
        float multiplier = 1.0f + ARCHER_BONUS_MULTIPLIER;
        if (projectile instanceof AbstractArrow arrow && arrow.isCritArrow()) {
            multiplier += ARCHER_FULL_DRAW_BONUS_MULTIPLIER;
        }
        event.setNewDamage(event.getNewDamage() * multiplier);
    }

    private static boolean isEnraged(Player player) {
        return player.getHealth() / player.getMaxHealth() <= VIKING_RAGE_HEALTH_THRESHOLD;
    }
    private static final float GUARDIAN_RETALIATION_PERCENT = 0.25f;

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (PlayerClasses.getPlayerClass(player) != PlayerClasses.PlayerClass.GUARDIAN) return;

        if (event.getDamageSource().getEntity() instanceof LivingEntity attacker) {
            float retaliation = event.getBlockedDamage() * GUARDIAN_RETALIATION_PERCENT;
            if (retaliation > 0) {
                attacker.hurt(player.damageSources().thorns(player), retaliation);
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (PlayerClasses.getPlayerClass(player) != PlayerClasses.PlayerClass.GUARDIAN) return;

        clearShieldDisable(player, player.getMainHandItem());
        clearShieldDisable(player, player.getOffhandItem());
    }
    private static void clearShieldDisable(Player player, ItemStack stack) {
        if (stack.getItem() instanceof ShieldItem && player.getCooldowns().isOnCooldown(stack.getItem())) {
            player.getCooldowns().removeCooldown(stack.getItem());
        }
    }
}