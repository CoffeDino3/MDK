package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.item.BulletEnhancement;
import com.CoffeDino.lunacy.item.Custom.BulletItem;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import com.CoffeDino.lunacy.network.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class BulletAnvilHandler {

    private static final int BULLETS_PER_MATERIAL = 4;
    private static final int MIN_ENHANCEMENTS = 1;
    private static final int MAX_ENHANCEMENTS = 5;
    private static final int LEVEL_PER_ENHANCEMENT = 10;
    private static final int MAX_ENHANCEMENT_LEVEL = 40;

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (!(left.getItem() instanceof BulletItem)) return;

        Player player = event.getPlayer();
        if (PlayerClasses.getPlayerClass(player) != PlayerClasses.PlayerClass.GUNSMITH) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        BulletEnhancement newTag = resolveEnhancement(right);
        if (newTag == null) return;

        List<BulletEnhancement> current = left.getOrDefault(
                ModDataComponents.BULLET_ENHANCEMENTS.get(), List.of());

        if (current.contains(newTag)) return;

        int maxEnhancements = getMaxEnhancements(PlayerLevels.getLevel(serverPlayer));

        List<BulletEnhancement> updatedTags = new ArrayList<>(current);
        if (updatedTags.size() >= maxEnhancements) {
            updatedTags.remove(0);
        }
        updatedTags.add(newTag);
        int requiredMaterial = (left.getCount() + BULLETS_PER_MATERIAL - 1) / BULLETS_PER_MATERIAL;
        if (right.getCount() < requiredMaterial) return;
        ItemStack output = left.copy();
        output.set(ModDataComponents.BULLET_ENHANCEMENTS.get(), List.copyOf(updatedTags));
        event.setOutput(output);
        event.setCost(newTag.getAnvilCost());
        event.setMaterialCost(requiredMaterial);
    }
    private static int getMaxEnhancements(int level) {
        int scaled = MIN_ENHANCEMENTS + Math.min(level, MAX_ENHANCEMENT_LEVEL) / LEVEL_PER_ENHANCEMENT;
        return Math.min(MAX_ENHANCEMENTS, scaled);
    }

    private static BulletEnhancement resolveEnhancement(ItemStack right) {
        if (right.is(Items.GUNPOWDER)) return BulletEnhancement.EXPLOSIVE;
        if (right.is(Items.IRON_INGOT)) return BulletEnhancement.HEAVY;
        if (right.is(Items.GOLD_INGOT)) return BulletEnhancement.TOXIC;
        if (right.is(Items.DIAMOND)) return BulletEnhancement.PIERCING;
        if (right.is(Items.SLIME_BALL)) return BulletEnhancement.RICOCHET;
        if (right.is(Items.FIRE_CHARGE)) return BulletEnhancement.IGNITE;
        if (right.is(Items.GRASS_BLOCK)) return BulletEnhancement.ROOTED;
        if (right.is(Items.POTION)) return BulletEnhancement.SOAKED;
        if (right.is(Items.WIND_CHARGE)) return BulletEnhancement.KNOCKBACK;
        if (right.is(Items.REDSTONE)) return BulletEnhancement.STATIC;
        if (right.is(Items.AMETHYST_SHARD)) return BulletEnhancement.VOLATILE;
        return null;
    }
}