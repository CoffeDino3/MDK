package net.CoffeDino.testmod.item.Custom;

import net.CoffeDino.testmod.classes.PlayerClasses;
import net.CoffeDino.testmod.entity.FireSpearEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

public class FireSpearItem extends SpearItem {
    private static final int CHARGE_TIME = 60;          // 3 seconds (60 ticks)
    private static final int THROW_TIME = 60;           // 3 seconds for throw
    private static final int SOUL_FIRE_DURATION = 260;  // 13 seconds
    private static final int COOLDOWN_TIME = 200;       // 10 seconds
    private static final int USE_DURATION = 72000;      // Max use time
    private static final int RETURN_TIME = 160;

    public FireSpearItem(Tier tier, Properties properties) {
        super(tier, 8.0f, -2.8f, 3.0f, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();
        long cooldownEnd = tag.contains("CooldownEnd") ? tag.getLong("CooldownEnd") : 0L;
        boolean isThrown = tag.contains("IsThrown") && tag.getBoolean("IsThrown");

        if (!isThrown && cooldownEnd <= level.getGameTime()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else if (isThrown) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Spear is already thrown!").withStyle(ChatFormatting.YELLOW), true);
            }
            return InteractionResultHolder.fail(stack);
        } else {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Spear is on cooldown!").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player) {
            int chargeTime = this.getUseDuration(stack, entity) - timeCharged;

            boolean isShifting = player.isShiftKeyDown();

            if (isShifting && chargeTime >= THROW_TIME) {
                throwSpear(stack, level, player);
            } else if (!isShifting && chargeTime >= CHARGE_TIME) {
                CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();

                if (!tag.contains("Charged") || !tag.getBoolean("Charged")) {
                    tag.putBoolean("Charged", true);
                    tag.putLong("ChargeEndTime", level.getGameTime() + SOUL_FIRE_DURATION);

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
                    spawnChargeParticles(level, player);

                    player.displayClientMessage(Component.literal("Spear charged with Soul Fire!")
                            .withStyle(ChatFormatting.BLUE), true);

                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                }
                player.stopUsingItem();
            }
            else {
                player.stopUsingItem();
            }
        }
    }

    private void throwSpear(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide()) {
            FireSpearEntity spearEntity = new FireSpearEntity(level, player, stack);
            spearEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            level.addFreshEntity(spearEntity);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
            CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();
            boolean wasCharged = tag.contains("Charged") && tag.getBoolean("Charged");
            long chargeEndTime = tag.contains("ChargeEndTime") ? tag.getLong("ChargeEndTime") : 0L;

            tag.putBoolean("IsThrown", true);
            tag.putLong("ReturnTime", level.getGameTime() + RETURN_TIME); // 8 seconds total
            tag.putUUID("ThrowerUUID", player.getUUID());
            tag.putBoolean("WasChargedWhenThrown", wasCharged);
            if (wasCharged) {
                tag.putLong("OriginalChargeEndTime", chargeEndTime);
            }
            tag.remove("ChargeStartTime");
            tag.putBoolean("Charged", false);
            tag.remove("ChargeEndTime");

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            if (!player.getAbilities().instabuild) {
                InteractionHand hand = player.getUsedItemHand();
                if (hand == InteractionHand.MAIN_HAND) {
                    player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                } else {
                    player.getInventory().offhand.set(0, ItemStack.EMPTY);
                }
            }

            player.displayClientMessage(Component.literal("Spear thrown! It will return in 8 seconds.")
                    .withStyle(ChatFormatting.GREEN), true);
            player.stopUsingItem();
        }
    }

    private boolean hasSpearmanClass(Player player) {
        if (player.level().isClientSide()) return true;
        PlayerClasses.PlayerClass cls = PlayerClasses.getPlayerClass(player);
        return cls == PlayerClasses.PlayerClass.SPEARMAN;
    }

    private void handleInvalidClass(ServerPlayer player, ItemStack stack, int slotId) {
        player.drop(stack, false);
        if (slotId >= 0 && slotId < player.getInventory().getContainerSize()) {
            player.getInventory().setItem(slotId, ItemStack.EMPTY);
        }
        player.displayClientMessage(Component.literal("You are not a Spearman! The spear rejects you.")
                .withStyle(ChatFormatting.RED), true);
        player.playSound(SoundEvents.ITEM_BREAK, 1.0F, 1.0F);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof ServerPlayer sp) {
            if (!hasSpearmanClass(sp)) {
                handleInvalidClass(sp, stack, slotId);
                return;
            }

            updateChargeState(stack, sp, level);
        }
        if (level.isClientSide() && entity instanceof Player player) {
            updateClientChargeState(stack, player, level);
        }
    }

    private void updateChargeState(ItemStack stack, Player player, Level level) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();

        long now = level.getGameTime();
        boolean wasCharged = tag.contains("Charged") && tag.getBoolean("Charged");
        long chargeStart = tag.contains("ChargeStartTime") ? tag.getLong("ChargeStartTime") : 0L;
        long cooldownEnd = tag.contains("CooldownEnd") ? tag.getLong("CooldownEnd") : 0L;
        boolean isThrown = tag.contains("IsThrown") && tag.getBoolean("IsThrown");
        long returnTime = tag.contains("ReturnTime") ? tag.getLong("ReturnTime") : 0L;

        if (isThrown && returnTime > 0 && now >= returnTime) {
            returnSpearToPlayer(stack, player, level);
            return;
        }

        boolean isTryingToCharge = player.isUsingItem()
                && player.getUsedItemHand() == InteractionHand.MAIN_HAND
                && player.getUseItem() == stack
                && !isMiningBlockClient(player)
                && !isThrown;

        if (isTryingToCharge) {
            boolean isShifting = player.isShiftKeyDown();

            if (chargeStart == 0L) {
                chargeStart = now;
                tag.putLong("ChargeStartTime", chargeStart);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }

            long progress = now - chargeStart;

            if (isShifting && progress >= THROW_TIME) {
                throwSpear(stack, level, player);
                return;
            }
            else if (!isShifting && progress >= CHARGE_TIME && (!tag.contains("Charged") || !tag.getBoolean("Charged"))) {
                tag.putBoolean("Charged", true);
                tag.putLong("ChargeEndTime", now + SOUL_FIRE_DURATION);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                if (!level.isClientSide()) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
                    spawnChargeParticles(level, player);
                    player.displayClientMessage(Component.literal("Spear charged with Soul Fire!")
                            .withStyle(ChatFormatting.BLUE), true);

                    player.stopUsingItem();
                }
            }
        } else if (chargeStart != 0L) {
            tag.putLong("ChargeStartTime", 0L);
        }

        if (tag.contains("ChargeEndTime")) {
            long chargeEnd = tag.getLong("ChargeEndTime");
            if (wasCharged && now >= chargeEnd) {
                tag.putBoolean("Charged", false);
                tag.putLong("CooldownEnd", now + COOLDOWN_TIME);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.0F);
                player.displayClientMessage(Component.literal("Soul Fire charge expired. Cooldown started.")
                        .withStyle(ChatFormatting.YELLOW), true);
            }
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private void returnSpearToPlayer(ItemStack stack, Player player, Level level) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();

        boolean wasChargedWhenThrown = tag.contains("WasChargedWhenThrown") && tag.getBoolean("WasChargedWhenThrown");
        long originalChargeEndTime = tag.contains("OriginalChargeEndTime") ? tag.getLong("OriginalChargeEndTime") : 0L;

        tag.putBoolean("IsThrown", false);
        tag.remove("ReturnTime");
        tag.remove("ThrowerUUID");
        tag.remove("WasChargedWhenThrown");
        tag.remove("OriginalChargeEndTime");

        if (wasChargedWhenThrown && originalChargeEndTime > level.getGameTime()) {
            tag.putBoolean("Charged", true);
            tag.putLong("ChargeEndTime", originalChargeEndTime);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5F, 1.0F);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        player.getX(), player.getY() + 1, player.getZ(),
                        15, 0.5, 0.5, 0.5, 0.1);
            }

            player.displayClientMessage(Component.literal("Charged spear returned!").withStyle(ChatFormatting.BLUE), true);
        } else {
            tag.putLong("CooldownEnd", level.getGameTime() + COOLDOWN_TIME);
            player.displayClientMessage(Component.literal("Spear returned!").withStyle(ChatFormatting.GREEN), true);

            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        player.getX(), player.getY() + 1, player.getZ(),
                        10, 0.5, 0.5, 0.5, 0.1);
            }
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        boolean hasSpear = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.getItem() instanceof FireSpearItem) {
                hasSpear = true;
                break;
            }
        }

        if (!hasSpear) {
            boolean added = player.getInventory().add(stack);
            if (!added) {
                player.drop(stack, false);
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void updateClientChargeState(ItemStack stack, Player player, Level level) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();

        long now = level.getGameTime();
        long chargeStart = tag.contains("ChargeStartTime") ? tag.getLong("ChargeStartTime") : 0L;
        boolean isThrown = tag.contains("IsThrown") && tag.getBoolean("IsThrown");
        if (!isThrown && chargeStart > 0 && player.isUsingItem() && player.getUseItem() == stack) {
            long progress = now - chargeStart;
            boolean isShifting = player.isShiftKeyDown();

            if (progress < THROW_TIME) {
                if (level.random.nextInt(5) == 0) {
                    level.addParticle(ParticleTypes.FLAME,
                            player.getX() + (level.random.nextDouble() - 0.5),
                            player.getY() + 1.0 + level.random.nextDouble(),
                            player.getZ() + (level.random.nextDouble() - 0.5),
                            0, 0.1, 0);
                }

                if (!isShifting && progress >= CHARGE_TIME - 20) {
                    if (level.random.nextInt(3) == 0) {
                        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                                player.getX() + (level.random.nextDouble() - 0.5),
                                player.getY() + 1.0 + level.random.nextDouble(),
                                player.getZ() + (level.random.nextDouble() - 0.5),
                                0, 0.1, 0);
                    }
                }
                else if (isShifting && progress >= THROW_TIME - 20) {
                    if (level.random.nextInt(3) == 0) {
                        level.addParticle(ParticleTypes.SMOKE,
                                player.getX() + (level.random.nextDouble() - 0.5),
                                player.getY() + 1.0 + level.random.nextDouble(),
                                player.getZ() + (level.random.nextDouble() - 0.5),
                                0, 0.1, 0);
                    }
                }
            }
        }
    }

    private boolean isMiningBlockClient(Player player) {
        if (!player.level().isClientSide()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null) return false;
        return mc.hitResult.getType() == HitResult.Type.BLOCK;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide() && entity instanceof LivingEntity living) {
            CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();
            boolean charged = tag.contains("Charged") && tag.getBoolean("Charged");
            boolean isThrown = tag.contains("IsThrown") && tag.getBoolean("IsThrown");

            if (!isThrown) {
                if (charged) {
                    living.igniteForSeconds(15);

                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                living.getX(), living.getY() + living.getEyeHeight(), living.getZ(),
                                19, 0.5, 0.5, 0.5, 0.1);
                    }
                    player.level().playSound(null, living.getX(), living.getY(), living.getZ(),
                            SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    living.igniteForSeconds(3);
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.FLAME,
                                living.getX(), living.getY() + living.getEyeHeight(), living.getZ(),
                                15, 0.3, 0.3, 0.3, 0.05);
                    }
                }
            }
        }
        return false;
    }

    private void spawnChargeParticles(Level level, Player player) {
        if (level instanceof ServerLevel sl) {
            for (int i = 0; i < 20; i++) {
                double dx = (level.random.nextDouble() - 0.5) * 2.0;
                double dy = level.random.nextDouble() * 2.0;
                double dz = (level.random.nextDouble() - 0.5) * 2.0;
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        player.getX() + dx, player.getY() + dy, player.getZ() + dz,
                        1, 0, 0, 0, 0.1);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tip, flag);
        tip.add(Component.literal("Right-click and hold to charge with Soul Fire").withStyle(ChatFormatting.BLUE));
        tip.add(Component.literal("Hold + Shift for 3 seconds to throw").withStyle(ChatFormatting.DARK_PURPLE));
        tip.add(Component.literal("Sets enemies on fire on hit").withStyle(ChatFormatting.DARK_AQUA));
        tip.add(Component.literal("Class: Spearman").withStyle(ChatFormatting.DARK_PURPLE));

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!data.isEmpty()) {
            CompoundTag tag = data.copyTag();
            if (tag.contains("Charged") && tag.getBoolean("Charged")) {
                tip.add(Component.literal("CHARGED - Soul Fire Active!").withStyle(ChatFormatting.BLUE));
            }
            if (tag.contains("IsThrown") && tag.getBoolean("IsThrown")) {
                tip.add(Component.literal("THROWN - Spear is in flight!").withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    public static boolean isCharged(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return !data.isEmpty() && data.copyTag().contains("Charged") && data.copyTag().getBoolean("Charged");
    }

    public static boolean isThrown(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return !data.isEmpty() && data.copyTag().contains("IsThrown") && data.copyTag().getBoolean("IsThrown");
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "testingcoffedinomod")
    public static class FireSpearOverlay {

        @SubscribeEvent
        public static void onRenderScreen(ScreenEvent.Render.Post event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.screen != null) return;

            ItemStack main = player.getMainHandItem();
            if (!(main.getItem() instanceof FireSpearItem)) return;

            CustomData data = main.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();
            long now = player.level().getGameTime();

            boolean charged = tag.contains("Charged") && tag.getBoolean("Charged");
            boolean isThrown = tag.contains("IsThrown") && tag.getBoolean("IsThrown");
            long chargeEnd = tag.contains("ChargeEndTime") ? tag.getLong("ChargeEndTime") : 0L;
            long cooldownEnd = tag.contains("CooldownEnd") ? tag.getLong("CooldownEnd") : 0L;
            long chargeStart = tag.contains("ChargeStartTime") ? tag.getLong("ChargeStartTime") : 0L;
            long returnTime = tag.contains("ReturnTime") ? tag.getLong("ReturnTime") : 0L;

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            int yPos = screenHeight - 40;

            if (isThrown && returnTime > now) {
                int secondsLeft = (int) ((returnTime - now) / 20);
                Component text = Component.literal("Spear returning in: " + secondsLeft + "s").withStyle(ChatFormatting.YELLOW);
                guiGraphics.drawString(mc.font, text, screenWidth / 2 - mc.font.width(text) / 2, yPos, 0xFFFFFF, false);
            } else if (charged && chargeEnd > now) {
                int secondsLeft = (int) ((chargeEnd - now) / 20);
                Component text = Component.literal("Soul Fire: " + secondsLeft + "s").withStyle(ChatFormatting.BLUE);
                guiGraphics.drawString(mc.font, text, screenWidth / 2 - mc.font.width(text) / 2, yPos, 0xFFFFFF, false);
            } else if (cooldownEnd > now) {
                int secondsLeft = (int) ((cooldownEnd - now) / 20);
                Component text = Component.literal("Cooldown: " + secondsLeft + "s").withStyle(ChatFormatting.RED);
                guiGraphics.drawString(mc.font, text, screenWidth / 2 - mc.font.width(text) / 2, yPos, 0xFFFFFF, false);
            } else if (chargeStart > 0 && player.isUsingItem()) {
                boolean isShifting = player.isShiftKeyDown();
                float chargePercent;
                Component text;

                if (isShifting) {
                    chargePercent = Math.min((now - chargeStart) / (float) THROW_TIME, 1.0f);
                    int chargePercentage = (int) (chargePercent * 100);
                    ChatFormatting color = chargePercent >= 1.0f ? ChatFormatting.DARK_PURPLE : ChatFormatting.YELLOW;
                    text = Component.literal("Throw Charging: " + chargePercentage + "%").withStyle(color);
                    if (chargePercent >= 1.0f) {
                        Component throwText = Component.literal("READY TO THROW!").withStyle(ChatFormatting.DARK_PURPLE);
                        guiGraphics.drawString(mc.font, throwText, screenWidth / 2 - mc.font.width(throwText) / 2, yPos - 12, 0xFFFFFF, false);
                    }
                } else {
                    chargePercent = Math.min((now - chargeStart) / (float) CHARGE_TIME, 1.0f);
                    int chargePercentage = (int) (chargePercent * 100);
                    text = Component.literal("Charging: " + chargePercentage + "%").withStyle(ChatFormatting.YELLOW);
                }

                guiGraphics.drawString(mc.font, text, screenWidth / 2 - mc.font.width(text) / 2, yPos, 0xFFFFFF, false);
            }
        }
    }
}