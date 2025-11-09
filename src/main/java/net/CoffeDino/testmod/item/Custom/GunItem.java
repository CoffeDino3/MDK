package net.CoffeDino.testmod.item.Custom;

import net.CoffeDino.testmod.classes.PlayerClasses;
import net.CoffeDino.testmod.entity.LamentBulletEntity;
import net.CoffeDino.testmod.handlers.GunUsageHandler;
import net.CoffeDino.testmod.item.ModItems;
import net.CoffeDino.testmod.particle.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GunItem extends Item {

    private static final int SHOT_COOLDOWN = 30;

    public GunItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (PlayerClasses.getPlayerClass(player) != PlayerClasses.PlayerClass.GUNSMITH) {
            player.displayClientMessage(Component.literal("Only Gunsmiths can use this weapon!"), true);
            return InteractionResultHolder.fail(itemstack);
        }

        if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().getItem() instanceof GunItem) {
            return InteractionResultHolder.pass(itemstack);
        }

        if (!hasBullets(player)) {
            return InteractionResultHolder.fail(itemstack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }
    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        if (entity instanceof Player player) {
            return PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.GUNSMITH;
        }
        return false;
    }
    @SubscribeEvent
    public static void onItemPickup(net.minecraftforge.event.entity.player.EntityItemPickupEvent event) {
        ItemStack stack = event.getItem().getItem();
        if (stack.getItem() instanceof GunItem) {
            if (PlayerClasses.getPlayerClass(event.getEntity()) != PlayerClasses.PlayerClass.GUNSMITH) {
                event.setCanceled(true);
                event.getEntity().displayClientMessage(Component.literal("You cannot pick up guns as you are not a Gunsmith!"), true);
            }
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            boolean isAccurate = GunUsageHandler.isFullyAimed(player);
            if (isAccurate || player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                if (hasBullets(player) && !player.getCooldowns().isOnCooldown(this)) {
                    shootBullet(level, player, isAccurate);
                    consumeBullet(player);
                    player.getCooldowns().addCooldown(this, SHOT_COOLDOWN);
                    EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                            ? EquipmentSlot.MAINHAND
                            : EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);

                    player.stopUsingItem();
                }
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    private boolean hasBullets(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ModItems.LAMENT_BULLET.get()) {
                return true;
            }
        }
        return false;
    }

    private void consumeBullet(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == ModItems.LAMENT_BULLET.get()) {
                stack.shrink(1);
                break;
            }
        }
    }

    private void shootBullet(Level level, Player player, boolean isAccurate) {
        if (!level.isClientSide) {
            LamentBulletEntity bullet = new LamentBulletEntity(level, player, isAccurate);

            Vec3 look = player.getLookAngle();
            float speed = isAccurate ? 3.0f : 1.5f;

            bullet.setDeltaMovement(look.x * speed, look.y * speed, look.z * speed);
            level.addFreshEntity(bullet);
            spawnMourningButterflies(level, player);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS,
                    1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
        }
    }
    private void spawnMourningButterflies(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 barrelPos = player.getEyePosition().add(look.x * 0.5, look.y * 0.5 - 0.2, look.z * 0.5);
        int butterflyCount = 3 + level.random.nextInt(3);

        for (int i = 0; i < butterflyCount; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

            Vec3 spawnPos = barrelPos.add(offsetX, offsetY, offsetZ);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        spawnPos.x, spawnPos.y, spawnPos.z,
                        1,
                        0.1, 0.1, 0.1,
                        0.05
                );
                if (level.random.nextBoolean()) {
                    serverLevel.sendParticles(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(),
                            spawnPos.x, spawnPos.y, spawnPos.z,
                            1,
                            0.05, 0.05, 0.05,
                            0.02
                    );
                }
            }
        }

        // Small burst effect at barrel
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    barrelPos.x, barrelPos.y, barrelPos.z,
                    3,
                    0.1, 0.1, 0.1,
                    0.05
            );
        }
    }


    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}