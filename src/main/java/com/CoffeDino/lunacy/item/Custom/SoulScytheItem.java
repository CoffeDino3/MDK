package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.player.ReaperSoulData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber
public class SoulScytheItem extends ScytheItem {
    private static final int MAX_STACKS = 10;
    private static final float ATTACK_DAMAGE = 8.0f;

    public SoulScytheItem(Tier tier, Properties properties) {
        super(tier, ATTACK_DAMAGE, -2.4f, 4.0f, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide() && entity instanceof LivingEntity target) {
            performSweepAttack(stack, player, target);
        }
        return false;
    }

    private void performSweepAttack(ItemStack stack, Player player, LivingEntity primaryTarget) {
        float sweepRange = getSweepRange();
        AABB sweepArea = primaryTarget.getBoundingBox().inflate(sweepRange);
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, sweepArea,
                entity -> entity != player && entity != primaryTarget && entity.isAlive());

        float sweepDamage = getSweepDamage();
        for (LivingEntity entity : nearbyEntities) {
            if (player.hasLineOfSight(entity)) {
                entity.hurt(player.damageSources().playerAttack(player), sweepDamage);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            3, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
    }

    // PlayerTickEvent.Post fires once per player per tick (server-side safe).
    // Use event.getEntity() to get the Player — NeoForge 1.21.1 tick events use getEntity(), not .player field.
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ReaperSoulData data = serverPlayer.getData(ModAttachments.REAPER_SOUL);
        if (!serverPlayer.hasEffect(ModEffects.SOUL_CLAIM)) {
            if (data.getSoulStacks() > 0) {
                data.setSoulStacks(0);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tip, flag);
        tip.add(Component.literal("Wide sweep attack that hits multiple enemies").withStyle(ChatFormatting.BLUE));
        tip.add(Component.literal("Soul Claim: 40% chance when entities die nearby").withStyle(ChatFormatting.DARK_PURPLE));
        tip.add(Component.literal("Max Stacks: " + MAX_STACKS).withStyle(ChatFormatting.DARK_AQUA));
        tip.add(Component.literal("Class: Reaper").withStyle(ChatFormatting.DARK_RED));

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!data.isEmpty()) {
            CompoundTag tag = data.copyTag();
            if (tag.contains("SoulStacks")) {
                int stacks = tag.getInt("SoulStacks");
                tip.add(Component.literal("Soul Stacks: " + stacks + "/" + MAX_STACKS).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    private float getSweepDamage() {
        return ATTACK_DAMAGE * 0.75f;
    }
}