package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.events.BorontCooldownAttachments;
import com.CoffeDino.lunacy.entity.BorontAvatarEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

public class BorontItem extends AxeItem {

    public BorontItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (attacker instanceof Player player && player.level() instanceof ServerLevel serverLevel) {
            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            BorontAvatarEntity.queueEcho(serverLevel, player, baseDamage * BorontAvatarEntity.DAMAGE_MULTIPLIER);
        }

        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            BorontAvatarEntity existing = BorontAvatarEntity.findAvatarFor(serverLevel, player);
            if (existing != null) {
                existing.discard();
                BorontCooldownAttachments.applyCooldown(player, this, serverLevel.getGameTime());
            } else {
                BorontAvatarEntity avatar = new BorontAvatarEntity(level, player);
                avatar.setSummonItem(this);
                serverLevel.addFreshEntity(avatar);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}