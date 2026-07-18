package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.entity.BorontAvatarEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class BorontCooldownAttachments {
    public static void applyCooldown(Player player, Item item, long gameTime) {
        player.getCooldowns().addCooldown(item, BorontAvatarEntity.SUMMON_COOLDOWN_TICKS);
        player.setData(ModAttachments.BORONT_COOLDOWN_END, gameTime + BorontAvatarEntity.SUMMON_COOLDOWN_TICKS);
    }
}