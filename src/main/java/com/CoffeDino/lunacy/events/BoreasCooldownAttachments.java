package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.BoreasItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class BoreasCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, BoreasItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.BOREAS_COOLDOWN_END, gameTime + BoreasItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}
