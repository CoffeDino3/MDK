package com.CoffeDino.lunacy.block;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
public class ModWoodTypes {

    public static final BlockSetType MAPLE_SET_TYPE =
            BlockSetType.register(new BlockSetType(Lunacy.MODID + ":maple"));

    public static final WoodType MAPLE_WOOD_TYPE =
            WoodType.register(new WoodType(Lunacy.MODID + ":maple", MAPLE_SET_TYPE));

    public static void init() {
    }
}
