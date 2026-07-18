package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.ObsidiaItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class ObsidiaCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, ObsidiaItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.OBSIDIA_COOLDOWN_END, gameTime + ObsidiaItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}