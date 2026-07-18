package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.classes.ChronobreakDataManager;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.particle.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

public class ViridyumGreatswordItem extends GreatswordItem {
    private static final int COOLDOWN_TICKS = 20 * 60 * 10;
    private static long clientCooldownEnd = 0;

    public ViridyumGreatswordItem(Tier tier, Properties properties) {
        super(tier, 15.0f, -3.5f, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            if (!hasChronobladeClass(player)) {
                // Actual removal from inventory is handled centrally by
                // ClassRestrictedWeaponHandler; just skip the chronobreak logic here.
                return;
            }

            ChronobreakDataManager.PlayerChronobreakData data = ChronobreakDataManager.get(player).getOrCreatePlayerData(player.getUUID());
            long currentTime = level.getGameTime();

            if (!data.isOnCooldown() && currentTime - data.getLastSaveTime() >= COOLDOWN_TICKS) {
                saveChronoData(player, currentTime);
                spawnClockParticles(level, player.position().add(0, 1, 0), 15);
                player.playNotifySound(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
            }

            if (data.isOnCooldown() && currentTime >= data.getCooldownEnd()) {
                data.setOnCooldown(false);
                data.setLastSaveTime(currentTime);
                player.displayClientMessage(Component.literal("Chronobreak ready! Position saved.")
                        .withStyle(ChatFormatting.GREEN), true);
            }
        } else if (level.isClientSide && entity instanceof Player) {
            updateClientCooldown(stack, level.getGameTime());
        }
    }

    // onEntitySwing(ItemStack, LivingEntity) is deprecated-for-removal in NeoForge 1.21.1 — removed.
    // Swing particles are handled via PlayerTickEvent.Post watching player.swinging in SwingHandler below.

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!hasChronobladeClass(serverPlayer)) {
                serverPlayer.displayClientMessage(Component.literal("You are not a Chronoblade! The greatsword rejects you.")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }

            ChronobreakDataManager.PlayerChronobreakData data = ChronobreakDataManager.get(serverPlayer).getOrCreatePlayerData(serverPlayer.getUUID());
            long currentTime = level.getGameTime();

            if (data.isOnCooldown()) {
                long cooldownLeft = data.getCooldownEnd() - currentTime;
                int secondsLeft = (int) (cooldownLeft / 20);
                player.displayClientMessage(Component.literal("Chronobreak cooldown: " + secondsLeft + "s")
                        .withStyle(ChatFormatting.YELLOW), true);
            } else if (data.getChronoPos() != null) {
                player.displayClientMessage(Component.literal("Chronobreak ready! Position saved.")
                        .withStyle(ChatFormatting.GOLD), true);
            } else {
                player.displayClientMessage(Component.literal("Chronobreak: No position saved yet")
                        .withStyle(ChatFormatting.YELLOW), true);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Chronobreak: Saves position every 10 minutes").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("On death: Rewinds to saved position & health").withStyle(ChatFormatting.GRAY));

        long currentTime = System.currentTimeMillis() / 50;
        if (clientCooldownEnd > currentTime) {
            tooltip.add(Component.literal("Right click to view the cooldown!").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("Ready to save position").withStyle(ChatFormatting.GREEN));
        }
        tooltip.add(Component.literal("Class: Chronoblade").withStyle(ChatFormatting.DARK_PURPLE));
    }

    private boolean hasChronobladeClass(Player player) {
        return PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.CHRONOBLADE;
    }

    private void saveChronoData(ServerPlayer player, long currentTime) {
        ChronobreakDataManager.PlayerChronobreakData data = ChronobreakDataManager.get(player).getOrCreatePlayerData(player.getUUID());
        CompoundTag posTag = new CompoundTag();
        posTag.putDouble("x", player.getX());
        posTag.putDouble("y", player.getY());
        posTag.putDouble("z", player.getZ());
        data.setChronoPos(posTag);
        data.setChronoHealth(player.getHealth());
        data.setLastSaveTime(currentTime);
        player.displayClientMessage(Component.literal("Chronobreak: Position and health saved!")
                .withStyle(ChatFormatting.AQUA), true);
    }

    public static boolean tryChronobreak(ServerPlayer player) {
        ChronobreakDataManager.PlayerChronobreakData data = ChronobreakDataManager.get(player).getOrCreatePlayerData(player.getUUID());

        if (data.getChronoPos() != null && !data.isOnCooldown()) {
            CompoundTag posTag = data.getChronoPos();
            double x = posTag.getDouble("x");
            double y = posTag.getDouble("y");
            double z = posTag.getDouble("z");
            float savedHealth = data.getChronoHealth();

            player.teleportTo(x, y, z);
            player.setHealth(savedHealth);

            spawnClockParticles(player.serverLevel(), new Vec3(x, y + 1, z), 30);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 1.5F, 0.5F);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.2F);

            player.displayClientMessage(Component.literal("Chronobreak! Rewound to saved position.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            data.setOnCooldown(true);
            data.setCooldownEnd(player.level().getGameTime() + COOLDOWN_TICKS);
            return true;
        }
        return false;
    }

    private void updateClientCooldown(ItemStack stack, long currentTime) {
        if (clientCooldownEnd > 0 && currentTime >= clientCooldownEnd) {
            clientCooldownEnd = 0;
        }
    }

    private static void spawnClockParticles(Level level, Vec3 pos, int count) {
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < count; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
                double offsetY = level.random.nextDouble() * 2.0;
                double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
                serverLevel.sendParticles(ModParticles.CLOCK_PARTICLES.get(),
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        1, 0, 0, 0, 0.1);
            }
        }
    }

    public static void setClientCooldown(long cooldownEnd) {
        clientCooldownEnd = cooldownEnd;
    }

    /**
     * Replaces the deprecated onEntitySwing override.
     * Watches player.swinging each client tick to detect arm swings while holding this sword.
     * player.swinging is true for the duration of the swing animation.
     * We use swingTime == 1 to fire exactly once per swing (the first tick it becomes active).
     */
    @EventBusSubscriber(value = Dist.CLIENT, modid = "lunacy")
    public static class SwingHandler {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) return;
            if (!player.swinging) return;
            if (player.swingTime != 1) return; // fire only on the first tick of the swing

            ItemStack held = player.getMainHandItem();
            if (!(held.getItem() instanceof ViridyumGreatswordItem)) return;

            Level level = player.level();
            Vec3 lookVec = player.getLookAngle();
            Vec3 particlePos = player.getEyePosition().add(lookVec.scale(1.5));

            for (int i = 0; i < 5; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (level.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;
                level.addParticle(ModParticles.CLOCK_PARTICLES.get(),
                        particlePos.x + offsetX,
                        particlePos.y + offsetY,
                        particlePos.z + offsetZ,
                        0, 0, 0);
            }
        }
    }
}