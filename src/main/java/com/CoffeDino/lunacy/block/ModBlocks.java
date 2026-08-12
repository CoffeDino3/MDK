package com.CoffeDino.lunacy.block;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.block.Custom.DoubleDoorBlock;
import com.CoffeDino.lunacy.block.Custom.SimpleSaplingBlock;
import com.CoffeDino.lunacy.item.ModItems;
import com.CoffeDino.lunacy.worldgen.ModTreeGrowers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, Lunacy.MODID);

    public static final DeferredHolder<Block, Block> CUMMINGTONITE_BLOCK = registerBlock("cummingtonite_block",
            () -> new DropExperienceBlock(UniformInt.of(2, 4), BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> CUMMINGTONITE_INGOT_BLOCK = registerBlock("cummingtonite_ingot_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> MAPLE_LOG = registerBlock("maple_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_WOOD = registerBlock("maple_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final DeferredHolder<Block, Block> STRIPPED_MAPLE_LOG = registerBlock("stripped_maple_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_PLANKS = registerBlock("maple_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_LEAVES = registerBlock("maple_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_SAPLING = registerBlock("maple_sapling",
            () -> new SimpleSaplingBlock(ModTreeGrowers.MAPLE,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.GRASS)));

    public static final DeferredHolder<Block, Block> MAPLE_SLAB = registerBlock("maple_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_STAIRS = registerBlock("maple_stairs",
            () -> new StairBlock(
                    MAPLE_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.WOOD)
                            .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_FENCE = registerBlock("maple_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_FENCE_GATE = registerBlock("maple_fence_gate",
            () -> new FenceGateBlock(
                    ModWoodTypes.MAPLE_WOOD_TYPE,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.WOOD)
                            .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_DOOR = registerBlock("maple_door",
            () -> new DoubleDoorBlock(
                    ModWoodTypes.MAPLE_SET_TYPE,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(3.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_TRAPDOOR = registerBlock("maple_trapdoor",
            () -> new TrapDoorBlock(
                    ModWoodTypes.MAPLE_SET_TYPE,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(3.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .ignitedByLava()));

    public static final DeferredHolder<Block, Block> MAPLE_BUTTON = registerBlock("maple_button",
            () -> new ButtonBlock(
                    ModWoodTypes.MAPLE_SET_TYPE,
                    30,
                    BlockBehaviour.Properties.of()
                            .noCollission()
                            .strength(0.5F)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    public static final DeferredHolder<Block, Block> MAPLE_PRESSURE_PLATE = registerBlock("maple_pressure_plate",
            () -> new PressurePlateBlock(
                    ModWoodTypes.MAPLE_SET_TYPE,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .noCollission()
                            .strength(0.5F)
                            .sound(SoundType.WOOD)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                            .ignitedByLava()));
    public static final DeferredHolder<Block, Block> SPRIGOT_ORE_BLOCK = registerBlock("sprigot_ore_block",
            () -> new DropExperienceBlock(UniformInt.of(2, 4), BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> BORONT_ORE_BLOCK = registerBlock("boront_ore_block",
            () -> new DropExperienceBlock(UniformInt.of(2, 4), BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> VIRIDYUM_ORE_BLOCK = registerBlock("viridyum_ore_block",
            () -> new DropExperienceBlock(UniformInt.of(2, 4), BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> LAVA_OBSIDIAN = registerBlock("lava_obsidian",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(70F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> SPRIGOT_INGOT_BLOCK = registerBlock("sprigot_ingot_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> BORONT_INGOT_BLOCK = registerBlock("boront_ingot_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> VIRIDYUM_INGOT_BLOCK = registerBlock("viridyum_ingot_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops()));



    private static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> block) {
        DeferredHolder<Block, T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }


    private static <T extends Block> void registerBlockItem(String name, DeferredHolder<Block, T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}