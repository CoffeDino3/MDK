package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.PerunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class PerunCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, PerunItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.PERUN_COOLDOWN_END, gameTime + PerunItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}
