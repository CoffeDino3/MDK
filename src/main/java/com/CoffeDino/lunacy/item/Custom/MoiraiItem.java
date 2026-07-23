package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.entity.MoiraiPortalEntity;
import com.CoffeDino.lunacy.entity.MoiraiSweepEntity;
import com.CoffeDino.lunacy.events.MoiraiCooldownAttachments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MoiraiItem extends SpellbladeItem {

    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 2000;
    private static final int CHAIN_GAP_TICKS = 6;
    public static final int MAX_CHARGES = 5;
    private static final double TARGET_SEARCH_RADIUS = 20.0;
    private static final double ENTRY_PORTAL_DISTANCE = 2.2;
    private static final double ENTRY_PORTAL_HEIGHT = 1.2;
    private static final double EXIT_PORTAL_PAST_TARGET = 2.0;

    public MoiraiItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            int charges = player.getData(ModAttachments.MOIRAI_CHARGES.get());
            if (charges <= 0) charges = MAX_CHARGES;
            cutThroughPortal(level, player);
            charges--;
            player.setData(ModAttachments.MOIRAI_CHARGES.get(), charges);
            if (charges <= 0) {
                MoiraiCooldownAttachments.applyCooldown(player, this, level.getGameTime());
            } else {
                player.getCooldowns().addCooldown(this, CHAIN_GAP_TICKS);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void cutThroughPortal(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 entryPos = player.position().add(look.scale(ENTRY_PORTAL_DISTANCE)).add(0, ENTRY_PORTAL_HEIGHT, 0);
        level.addFreshEntity(new MoiraiPortalEntity(level, entryPos, player.getYRot(),
                MoiraiPortalEntity.CAST_PORTAL_LIFETIME));

        LivingEntity target = findRandomTarget(level, player);
        if (target == null) {
            level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_WORK_FLETCHER,
                    SoundSource.PLAYERS, 0.8f, 0.6f); // placeholder "nothing to cut" sfx
            return;
        }
        Vec3 throughLine = target.position().subtract(player.position());
        if (throughLine.lengthSqr() < 1.0E-4) {
            throughLine = look;
        }
        throughLine = throughLine.normalize();

        Vec3 exitPos = target.position()
                .add(throughLine.scale(EXIT_PORTAL_PAST_TARGET))
                .add(0, target.getBbHeight() * 0.5, 0);
        float exitYaw = (float) Math.toDegrees(Math.atan2(-throughLine.x, throughLine.z));

        level.addFreshEntity(new MoiraiPortalEntity(level, exitPos, exitYaw, MoiraiPortalEntity.EXIT_PORTAL_LIFETIME));
        level.addFreshEntity(new MoiraiSweepEntity(level, exitPos, exitYaw, player, throughLine));

        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.0f, 0.7f);
    }

    private LivingEntity findRandomTarget(Level level, Player player) {
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(TARGET_SEARCH_RADIUS),
                e -> e.isAlive() && e != player);
        if (candidates.isEmpty()) return null;
        return candidates.get(level.random.nextInt(candidates.size()));
    }
}