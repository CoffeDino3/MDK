package net.CoffeDino.testmod.item.Custom;
import net.CoffeDino.testmod.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpecialWandItem extends Item {
    private static final Random RANDOM = new Random();

    public SpecialWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block currentBlock = level.getBlockState(pos).getBlock();

        if (!level.isClientSide()) {
            List<Block> allBlocks = new ArrayList<>(ForgeRegistries.BLOCKS.getValues());
            allBlocks.remove(currentBlock);

            if (!allBlocks.isEmpty()) {
                Block newBlock = allBlocks.get(RANDOM.nextInt(allBlocks.size()));
                level.setBlockAndUpdate(pos, newBlock.defaultBlockState());
                if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                    context.getItemInHand().hurtAndBreak(1, serverPlayer,
                            EquipmentSlot.MAINHAND);
                }
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        pTooltipComponents.add(Component.translatable("tooltip.lunacy.special_wand.tooltip"));

    }
}




















