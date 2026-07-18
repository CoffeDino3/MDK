package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.classes.PlayerClasses;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class ShiBowItem extends BowItem {

    private static final float FLAT_ATTACK_DAMAGE = 7.0F;
    private static final float ATTACK_SPEED = -2.4F;

    private static final float ARROW_CHARGE_GAIN = 0.005F;
    private static final float MELEE_CHARGE_GAIN = 0.003F;
    private static final float MAX_CHARGE = 0.50F;

    public ShiBowItem(Properties properties) {
        super(properties.attributes(createAttributes()));
    }

    private static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath("lunacy", "shi_bow_attack_damage"),
                                FLAT_ATTACK_DAMAGE,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath("lunacy", "shi_bow_attack_speed"),
                                ATTACK_SPEED,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        float charge = getCharge(stack, "NextMeleeBonus");

        if (charge > 0f && attacker.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            float bonusDamage = charge * target.getMaxHealth();

            bonusDamage = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyDamage(
                    serverLevel, stack, target, target.damageSources().mobAttack(attacker), bonusDamage);

            target.hurt(target.damageSources().mobAttack(attacker), bonusDamage);
            setCharge(stack, "NextMeleeBonus", 0f);
        }

        addCharge(stack, MELEE_CHARGE_GAIN, "NextArrowBonus");
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }

    public void onArrowHit(ItemStack bowStack, LivingEntity target, LivingEntity shooter) {
        float charge = getCharge(bowStack, "NextArrowBonus");

        if (charge > 0f && shooter.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            float bonusDamage = charge * target.getMaxHealth();

            bonusDamage = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyDamage(
                    serverLevel, bowStack, target, target.damageSources().mobAttack(shooter), bonusDamage);

            target.hurt(target.damageSources().mobAttack(shooter), bonusDamage);
            setCharge(bowStack, "NextArrowBonus", 0f);
        }

        addCharge(bowStack, ARROW_CHARGE_GAIN, "NextMeleeBonus");
    }

    private float getCharge(ItemStack stack, String key) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.contains(key) ? tag.getFloat(key) : 0f;
    }

    private void setCharge(ItemStack stack, String key, float value) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putFloat(key, Math.min(value, MAX_CHARGE));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private void addCharge(ItemStack stack, float amount, String key) {
        setCharge(stack, key, getCharge(stack, key) + amount);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tip, flag);

        float meleeBonus = getCharge(stack, "NextMeleeBonus") * 100f;
        float arrowBonus = getCharge(stack, "NextArrowBonus") * 100f;

        if (meleeBonus > 0f) {
            tip.add(Component.literal(String.format("Your next melee will do %.1f%% health damage", meleeBonus))
                    .withStyle(net.minecraft.ChatFormatting.RED));
        }
        if (arrowBonus > 0f) {
            tip.add(Component.literal(String.format("Your next shot will do %.1f%% health damage", arrowBonus))
                    .withStyle(net.minecraft.ChatFormatting.GREEN));
        }
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> stack.is(net.minecraft.tags.ItemTags.ARROWS) && !stack.is(net.minecraft.world.item.Items.ARROW);
    }
}