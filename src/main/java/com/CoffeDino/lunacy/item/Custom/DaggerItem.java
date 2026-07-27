package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.classes.PlayerClasses;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;


public class DaggerItem extends SwordItem {
    private final float attackDamage;
    private final float attackSpeed;
    private static final float BACKSTAB_MULTIPLIER = 2.0f;
    private static final float BACKSTAB_DOT_THRESHOLD = -0.5f;

    private static final String NBT_PASSIVE_INVIS = "DaggerPassiveInvis";

    public DaggerItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    public DaggerItem(Tier tier, Properties properties) {
        this(tier, 3.0f, -1.6f, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return createAttributes(attackDamage, attackSpeed);
    }

    protected static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player
                && PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.ASSASSIN
                && isBackstab(target, attacker)) {
            applyBackstabBonus(player, target);
        }
        return true;
    }

    private void applyBackstabBonus(Player player, LivingEntity target) {
        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float bonusDamage = baseDamage * (BACKSTAB_MULTIPLIER - 1.0f);
        target.hurt(target.damageSources().playerAttack(player), bonusDamage);

        if (target.level() instanceof ServerLevel serverLevel) {
            spawnBackstabParticles(serverLevel, target);
        }
    }

    public boolean isBackstab(LivingEntity target, LivingEntity attacker) {
        Vec3 targetToAttacker = attacker.position().subtract(target.position());
        if (targetToAttacker.lengthSqr() < 1.0E-4) return false;

        Vec3 targetFacing = Vec3.directionFromRotation(0, target.getYRot());
        return targetFacing.normalize().dot(targetToAttacker.normalize()) < getBackstabDotThreshold();
    }

    private void spawnBackstabParticles(ServerLevel level, LivingEntity target) {
        level.sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                10, 0.3, 0.3, 0.3, 0.15);
    }

    public float getBackstabMultiplier() {
        return BACKSTAB_MULTIPLIER;
    }

    protected float getBackstabDotThreshold() {
        return BACKSTAB_DOT_THRESHOLD;
    }

    @EventBusSubscriber(modid = "lunacy")
    public static class PassiveInvisHandler {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            boolean isAssassin = PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.ASSASSIN;

            boolean holdingDagger = player.getMainHandItem().getItem() instanceof DaggerItem
                    || player.getOffhandItem().getItem() instanceof DaggerItem;
            boolean shouldBeInvisible = isAssassin && holdingDagger && player.isShiftKeyDown();

            var data = player.getPersistentData();
            boolean wasGranted = data.getBoolean(NBT_PASSIVE_INVIS);

            if (shouldBeInvisible) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, true, false, false));
                if (!wasGranted) {
                    data.putBoolean(NBT_PASSIVE_INVIS, true);
                }
            } else if (wasGranted) {
                player.removeEffect(MobEffects.INVISIBILITY);
                data.putBoolean(NBT_PASSIVE_INVIS, false);
            }
        }
    }
}