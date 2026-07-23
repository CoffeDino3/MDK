package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.entity.DarkSphereEntity;
import com.CoffeDino.lunacy.events.CharybdisCooldownAttachments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CharybdisItem extends SpellbladeItem {

    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 600;
    private static final double MAX_PLACE_DISTANCE = 20.0;

    public CharybdisItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            Vec3 spawnPos = getTargetPos(level, player);
            DarkSphereEntity sphere = new DarkSphereEntity(level, spawnPos, player);
            level.addFreshEntity(sphere);
            level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.6f, 0.6f);
            CharybdisCooldownAttachments.applyCooldown(player, this, level.getGameTime());
        } else {
            player.getCooldowns().addCooldown(this, SPECIAL_ABILITY_COOLDOWN_TICKS);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    private Vec3 getTargetPos(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 endPos = eyePos.add(look.scale(MAX_PLACE_DISTANCE));
        ClipContext clipContext = new ClipContext(eyePos, endPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        HitResult hitResult = level.clip(clipContext);
        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHit) {
            Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());
            return blockHit.getLocation().add(normal.scale(0.5)).add(0, 1.0, 0);
        }

        return endPos;
    }
}