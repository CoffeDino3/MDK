package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.entity.BulletEntity;
import com.CoffeDino.lunacy.item.BulletEnhancement;
import com.CoffeDino.lunacy.item.ModItems;
import com.CoffeDino.lunacy.network.ModDataComponents;
import com.CoffeDino.lunacy.handlers.GunUsageHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GunItem extends Item {

    private static final int SHOT_COOLDOWN = 30;
    private static final int ACCURATE_SHOT_COOLDOWN = 18;
    private static final int SHOTS_BEFORE_OVERHEAT = 6;
    private static final int OVERHEAT_COOLDOWN = 1200;

    private static final Map<UUID, Integer> shotCounters = new HashMap<>();

    private final float baseDamage;

    public GunItem(Properties properties, float baseDamage) {
        super(properties);
        this.baseDamage = baseDamage;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().getItem() instanceof GunItem) {
            return InteractionResultHolder.pass(itemstack);
        }

        if (!hasAmmo(player)) {
            return InteractionResultHolder.fail(itemstack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return true;
    }


    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            boolean isAccurate = GunUsageHandler.isFullyAimed(player);
            if (isAccurate || player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                if (hasAmmo(player) && !player.getCooldowns().isOnCooldown(this)) {
                    fire(level, player, isAccurate);
                    consumeAmmo(player);
                    int cooldown = isAccurate ? ACCURATE_SHOT_COOLDOWN : SHOT_COOLDOWN;
                    player.getCooldowns().addCooldown(this, cooldown);
                    EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                            ? EquipmentSlot.MAINHAND
                            : EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                    player.stopUsingItem();

                    if (isOverheatEligible()) {
                        handleOverheat(player);
                    }
                }
            }
        }
    }

    private void handleOverheat(Player player) {
        int shots = shotCounters.merge(player.getUUID(), 1, Integer::sum);
        if (shots >= SHOTS_BEFORE_OVERHEAT) {
            shotCounters.remove(player.getUUID());
            for (ItemStack invStack : player.getInventory().items) {
                if (invStack.getItem() instanceof GunItem gun && gun.isOverheatEligible()) {
                    player.getCooldowns().addCooldown(invStack.getItem(), OVERHEAT_COOLDOWN);
                }
            }
            for (ItemStack invStack : player.getInventory().offhand) {
                if (invStack.getItem() instanceof GunItem gun && gun.isOverheatEligible()) {
                    player.getCooldowns().addCooldown(invStack.getItem(), OVERHEAT_COOLDOWN);
                }
            }
        }
    }

    /** Override and return false for special/unique guns (e.g. LamentGunItem) that shouldn't overheat. */
    protected boolean isOverheatEligible() {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    protected boolean isValidAmmo(ItemStack stack) {
        return stack.getItem() == ModItems.BULLET.get();
    }

    protected boolean hasAmmo(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (isValidAmmo(stack)) return true;
        }
        return false;
    }

    protected ItemStack findAmmoStack(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isValidAmmo(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    protected void consumeAmmo(Player player) {
        ItemStack ammo = findAmmoStack(player);
        if (!ammo.isEmpty()) ammo.shrink(1);
    }

    protected void fire(Level level, Player player, boolean isAccurate) {
        if (!level.isClientSide) {
            ItemStack ammo = findAmmoStack(player);
            List<BulletEnhancement> enhancements = ammo.isEmpty()
                    ? List.of(BulletEnhancement.NONE)
                    : ammo.getOrDefault(ModDataComponents.BULLET_ENHANCEMENTS.get(), List.of(BulletEnhancement.NONE));

            BulletEntity bullet = new BulletEntity(level, player, enhancements, baseDamage);
            Vec3 look = player.getLookAngle();
            float speed = isAccurate ? 3.0f : 1.5f;
            bullet.setDeltaMovement(look.x * speed, look.y * speed, look.z * speed);
            level.addFreshEntity(bullet);
            playShotSound(level, player);
        }
    }

    protected void playShotSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS,
                1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}