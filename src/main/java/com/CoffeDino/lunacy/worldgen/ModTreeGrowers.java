package com.CoffeDino.lunacy.worldgen;

import net.minecraft.world.level.block.grower.TreeGrower;
import java.util.Optional;

public class ModTreeGrowers {
    public static final TreeGrower MAPLE = new TreeGrower(
            "lunacy:maple",
            Optional.of(ModConfiguredFeatures.MAPLE_KEY),
            Optional.empty(),
            Optional.empty()
    );
}