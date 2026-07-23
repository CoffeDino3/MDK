package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.PhaetonItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class PhaetonCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, PhaetonItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.PHAETON_COOLDOWN_END.get(), gameTime + PhaetonItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}