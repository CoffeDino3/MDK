package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.CharybdisItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class CharybdisCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, CharybdisItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.CHARYBDIS_COOLDOWN_END, gameTime + CharybdisItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}