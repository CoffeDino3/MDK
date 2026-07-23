package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.AmphitriteItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class AmphitriteCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, AmphitriteItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
        player.setData(ModAttachments.AMPHITRITE_COOLDOWN_END, gameTime + AmphitriteItem.SPECIAL_ABILITY_COOLDOWN_TICKS);
    }
}
