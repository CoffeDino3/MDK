package com.CoffeDino.lunacy.attributes;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, Lunacy.MODID);

    public static final DeferredHolder<Attribute, Attribute> SWEEP_RANGE = ATTRIBUTES.register(
            "sweep_range",
            () -> new RangedAttribute(
                    "attribute.name.lunacy.sweep_range",
                    0.0,
                    0.0,
                    100.0
            ).setSyncable(true)
    );
}