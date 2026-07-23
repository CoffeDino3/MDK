package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.entity.ModEntities;
import com.CoffeDino.lunacy.entity.PerunOrbitalStrikeEntity;
import com.CoffeDino.lunacy.entity.PerunSkyBeamEntity;
import com.CoffeDino.lunacy.events.PerunCooldownAttachments;
import com.CoffeDino.lunacy.renderer.PerunChargeRingRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PerunItem extends SpellbladeItem {

    public static final int SPECIAL_ABILITY_COOLDOWN_TICKS = 1200;
    public static final int RING_COUNT = 3;
    public static final int TICKS_PER_RING = 15;
    public static final int CHARGE_TICKS = RING_COUNT * TICKS_PER_RING;
    public static final double STRIKE_RANGE = 80.0;

    public PerunItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CHARGE_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return stack;
        }

        PerunCooldownAttachments.applyCooldown(player, this, level.getGameTime());
        Vec3 headPos = player.getEyePosition(1.0f);
        Vec3 beamSpawnPos = headPos.add(0, PerunChargeRingRenderer.topRingHeightAboveHead(), 0);

        PerunSkyBeamEntity skyBeam = new PerunSkyBeamEntity(ModEntities.PERUN_SKY_BEAM.get(), level);
        skyBeam.setPos(beamSpawnPos.x, beamSpawnPos.y, beamSpawnPos.z);
        ((ServerLevel) level).addFreshEntity(skyBeam);

        Vec3 impact = findImpactPoint(player);

        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(),
                SoundSource.PLAYERS, 1.0f, 1.0f);

        PerunOrbitalStrikeEntity strike =
                new PerunOrbitalStrikeEntity(ModEntities.PERUN_ORBITAL_STRIKE.get(), level);
        strike.setPos(impact.x, impact.y, impact.z);
        strike.setOwner(player);
        ((ServerLevel) level).addFreshEntity(strike);

        return stack;
    }
    private Vec3 findImpactPoint(Player player) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(STRIKE_RANGE));
        BlockHitResult hit = player.level().clip(new ClipContext(
                eyePos, endPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getLocation();
        }
        return endPos;
    }
}