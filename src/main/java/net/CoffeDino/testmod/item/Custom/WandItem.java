package net.CoffeDino.testmod.item.Custom;

import net.CoffeDino.testmod.block.ModBlocks;
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
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

public class WandItem extends Item {
    private static final Map<Block, Block> WAND_MAP =
            Map.ofEntries(
                    Map.entry(Blocks.IRON_ORE, Blocks.GOLD_ORE),
                    Map.entry(Blocks.GOLD_ORE, Blocks.IRON_ORE),
                    Map.entry(Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_GOLD_ORE),
                    Map.entry(Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE),
                    Map.entry(Blocks.COPPER_ORE, Blocks.COAL_ORE),
                    Map.entry(Blocks.COAL_ORE, Blocks.COPPER_ORE),
                    Map.entry(Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_COAL_ORE),
                    Map.entry(Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_COPPER_ORE),
                    Map.entry(Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE),
                    Map.entry(Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE),
                    Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
                    Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
                    Map.entry(Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE),
                    Map.entry(Blocks.EMERALD_ORE, Blocks.DIAMOND_ORE),
                    Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
                    Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
                    Map.entry(Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE),
                    Map.entry(Blocks.NETHER_QUARTZ_ORE, Blocks.NETHER_GOLD_ORE),
                    Map.entry(Blocks.IRON_BLOCK, Blocks.DIAMOND_ORE),
                    Map.entry(Blocks.GOLD_BLOCK, Blocks.DEEPSLATE_REDSTONE_ORE),
                    Map.entry(Blocks.COPPER_BLOCK, Blocks.DEEPSLATE_LAPIS_ORE),
                    Map.entry(Blocks.COAL_BLOCK, Blocks.GOLD_ORE),
                    Map.entry(Blocks.REDSTONE_BLOCK, Blocks.LAPIS_BLOCK),
                    Map.entry(Blocks.LAPIS_BLOCK, Blocks.REDSTONE_BLOCK),
                    Map.entry(Blocks.DIAMOND_BLOCK, Blocks.ANCIENT_DEBRIS),
                    Map.entry(Blocks.ANCIENT_DEBRIS, Blocks.DIAMOND_BLOCK),
                    Map.entry(Blocks.EMERALD_BLOCK, Blocks.ANCIENT_DEBRIS),
                    Map.entry(Blocks.QUARTZ_BLOCK, Blocks.RAW_IRON_BLOCK),
                    Map.entry(Blocks.RAW_IRON_BLOCK, Blocks.QUARTZ_BLOCK),
                    Map.entry(Blocks.NETHERITE_BLOCK, ModBlocks.CUMMINGTONITE_BLOCK.get()),
                    Map.entry(ModBlocks.CUMMINGTONITE_BLOCK.get(), Blocks.NETHERITE_BLOCK),
                    Map.entry(Blocks.RAW_GOLD_BLOCK, Blocks.OBSIDIAN),
                    Map.entry(Blocks.OBSIDIAN, Blocks.RAW_GOLD_BLOCK),
                    Map.entry(Blocks.RAW_COPPER_BLOCK, Blocks.AMETHYST_BLOCK),
                    Map.entry(Blocks.AMETHYST_BLOCK, Blocks.RAW_COPPER_BLOCK),
                    Map.entry(Blocks.GILDED_BLACKSTONE, Blocks.CRYING_OBSIDIAN),
                    Map.entry(Blocks.CRYING_OBSIDIAN, Blocks.GILDED_BLACKSTONE)
            );



    public WandItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        Block clickedBlock = level.getBlockState(pContext.getClickedPos()).getBlock();

        if(WAND_MAP.containsKey(clickedBlock)) {
            if(!level.isClientSide()) {
                level.setBlockAndUpdate(pContext.getClickedPos(), WAND_MAP.get(clickedBlock).defaultBlockState());

                pContext.getItemInHand().hurtAndBreak(25,((ServerLevel) level), ((ServerPlayer) pContext.getPlayer()),
                        item -> pContext.getPlayer().onEquippedItemBroken(item, EquipmentSlot.MAINHAND));

                level.playSound(null, pContext.getClickedPos(), SoundEvents.ANCIENT_DEBRIS_FALL, SoundSource.AMBIENT);


            }
        }

        return InteractionResult.SUCCESS;

    }
    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        pTooltipComponents.add(Component.translatable("tooltip.testingcoffedinomod.the_wand.tooltip"));
    }
}
