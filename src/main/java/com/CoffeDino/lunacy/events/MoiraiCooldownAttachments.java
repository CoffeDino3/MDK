package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.MoiraiItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class MoiraiCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, MoiraiItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.MOIRAI_COOLDOWN_END, gameTime + MoiraiItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}
