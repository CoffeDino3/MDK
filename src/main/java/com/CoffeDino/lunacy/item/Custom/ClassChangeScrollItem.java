package com.CoffeDino.lunacy.item.Custom;

import com.CoffeDino.lunacy.client.gui.ClassSelectionScreen;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClassChangeScrollItem extends Item {
    public ClassChangeScrollItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            PlayerClasses.setClientClass(null);
            PlayerClasses.resetClientClass();
            Minecraft.getInstance().setScreen(new ClassSelectionScreen());
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        } else {
            PlayerClasses.clearPlayerClass(player);
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}