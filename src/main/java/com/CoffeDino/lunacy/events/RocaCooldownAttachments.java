package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.RocaItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class RocaCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, RocaItem.STANCE_COOLDOWN_TICKS);
        player.setData(ModAttachments.ROCA_COOLDOWN_END, gameTime + RocaItem.STANCE_COOLDOWN_TICKS);
    }
}