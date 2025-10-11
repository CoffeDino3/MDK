package net.CoffeDino.testmod.client.gui.components;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

public class ColoredButton extends Button {
    private final int backgroundColor;
    private final int hoverColor;

    public ColoredButton(int x, int y, int width, int height, Component label,
                         OnPress onPress,
                         int backgroundColor, int hoverColor){
        super(x,y,width,height,label,onPress,DEFAULT_NARRATION);
        this.backgroundColor=backgroundColor;
        this.hoverColor=hoverColor;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int color = this.isHoveredOrFocused() ? hoverColor : backgroundColor;
        RenderSystem.enableBlend();
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color);

        int textColor = 0xFFFFFF;
        guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                this.getMessage(),
                this.getX() + this.width / 2,
                this.getY() + (this.height - 8) / 2,
                textColor
        );
    }
}
