package com.CoffeDino.lunacy.events;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation GRUCK =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "gruck"), "main");
}
