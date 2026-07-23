package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.JoroItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class JoroCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, JoroItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.JORO_COOLDOWN_END, gameTime + JoroItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}
