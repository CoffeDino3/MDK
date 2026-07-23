package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.ErinyesItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class ErinyesCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, ErinyesItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.ERINYES_COOLDOWN_END, gameTime + ErinyesItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}
