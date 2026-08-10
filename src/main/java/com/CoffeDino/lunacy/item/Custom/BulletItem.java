package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.item.BulletEnhancement;
import com.CoffeDino.lunacy.network.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BulletItem extends Item {
    public BulletItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        List<BulletEnhancement> tags = stack.getOrDefault(ModDataComponents.BULLET_ENHANCEMENTS.get(), List.of());
        for (BulletEnhancement tag : tags) {
            if (tag == BulletEnhancement.NONE) continue;
            tooltipComponents.add(Component.literal("• ")
                    .append(tag.displayName())
                    .withStyle(colorFor(tag)));
        }
    }

    private static ChatFormatting colorFor(BulletEnhancement tag) {
        return switch (tag) {
            case EXPLOSIVE -> ChatFormatting.RED;
            case HEAVY -> ChatFormatting.WHITE;
            case TOXIC -> ChatFormatting.DARK_GREEN;
            case PIERCING -> ChatFormatting.AQUA;
            case RICOCHET -> ChatFormatting.YELLOW;
            case IGNITE -> ChatFormatting.GOLD;
            case ROOTED -> ChatFormatting.DARK_GREEN;
            case SOAKED -> ChatFormatting.BLUE;
            case KNOCKBACK -> ChatFormatting.GRAY;
            case STATIC -> ChatFormatting.LIGHT_PURPLE;
            case VOLATILE -> ChatFormatting.DARK_PURPLE;
            default -> ChatFormatting.GRAY;
        };
    }
}