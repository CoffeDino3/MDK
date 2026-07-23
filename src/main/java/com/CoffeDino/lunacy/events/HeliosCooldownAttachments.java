package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.HeliosItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class HeliosCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, HeliosItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.HELIOS_COOLDOWN_END, gameTime + HeliosItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}