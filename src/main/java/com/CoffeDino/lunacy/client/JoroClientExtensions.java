package com.CoffeDino.lunacy.client;

import com.CoffeDino.lunacy.item.Custom.JoroItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class JoroClientExtensions implements IClientItemExtensions {
    public static final JoroClientExtensions INSTANCE = new JoroClientExtensions();
    public static final EnumProxy<HumanoidModel.ArmPose> RAISED_POSE = new EnumProxy<>(
            HumanoidModel.ArmPose.class, false,
            (IArmPoseTransformer) (model, entity, arm) -> {
                boolean right = arm == HumanoidArm.RIGHT;
                var limb = right ? model.rightArm : model.leftArm;
                limb.xRot = -(float) Math.PI * 0.95f;
                limb.yRot = 0f;
                limb.zRot = 0f;
            });

    private JoroClientExtensions() {}

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
        if (itemStack.getItem() instanceof JoroItem
                && entityLiving.getUsedItemHand() == hand
                && entityLiving.isUsingItem()) {
            return RAISED_POSE.getValue();
        }
        return HumanoidModel.ArmPose.EMPTY;
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                           ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        return false;
    }
}