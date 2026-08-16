package com.CoffeDino.lunacy.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.client.gui.components.ColoredButton;
import com.CoffeDino.lunacy.classes.SpellbladeElement;
import com.CoffeDino.lunacy.network.ElementSelectionPacket;
import com.CoffeDino.lunacy.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ElementSelectionScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "textures/gui/class_selection_bg.png");

    private int currentElementIndex = 0;
    private final List<SpellbladeElement> elementList = List.of(SpellbladeElement.values());
    private Button selectButton;
    private Button leftArrow;
    private Button rightArrow;

    public ElementSelectionScreen() {
        super(Component.literal("Choose your Element!"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 350;
        int panelHeight = 200;
        int panelLeft = centerX - panelWidth / 2;
        int panelTop = centerY - panelHeight / 2;

        leftArrow = new ColoredButton(
                panelLeft - 35, centerY - 10, 25, 25,
                Component.literal("◀"),
                b -> switchElement(-1),
                0xA69678FF,
                0xE07020FF
        );
        addRenderableWidget(leftArrow);

        rightArrow = new ColoredButton(
                panelLeft + panelWidth + 10, centerY - 10, 25, 25,
                Component.literal("▶"),
                b -> switchElement(1),
                0xA69678FF,
                0xE07020FF
        );
        addRenderableWidget(rightArrow);

        selectButton = new ColoredButton(
                centerX - 60, panelTop + panelHeight - 30, 120, 20,
                Component.literal("Select Element"),
                b -> selectElement(),
                0xA60032A0,
                0xCC0066CC
        );
        addRenderableWidget(selectButton);
    }

    private void switchElement(int direction) {
        currentElementIndex = (currentElementIndex + direction + elementList.size()) % elementList.size();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(BACKGROUND, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x80101010, 0xA0101010);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 350;
        int panelHeight = 200;
        int panelTop = centerY - panelHeight / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 100);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, panelTop + 10, 0);
        guiGraphics.pose().scale(1.8f, 1.8f, 1.8f);
        guiGraphics.drawCenteredString(this.font, Component.literal("SELECT YOUR ELEMENT"), 0, 0, 0xFFFFFF);
        guiGraphics.pose().popPose();

        int leftIndex = (currentElementIndex - 1 + elementList.size()) % elementList.size();
        int centerIndex = currentElementIndex;
        int rightIndex = (currentElementIndex + 1) % elementList.size();

        SpellbladeElement leftElement = elementList.get(leftIndex);
        SpellbladeElement centerElement = elementList.get(centerIndex);
        SpellbladeElement rightElement = elementList.get(rightIndex);

        int panelWidthAll = 80;
        int centerPanelHeight = 110;
        int sidePanelHeight = 90;
        int panelY = panelTop + 45;
        int spacing = 15;
        int totalWidth = (panelWidthAll * 3) + (spacing * 2);
        int startX = centerX - (totalWidth / 2);

        int leftPanelX = startX;
        renderElementPanel(guiGraphics, leftElement, leftPanelX, panelY + 15, panelWidthAll, sidePanelHeight, 0xAAFFFFFF, true);

        int centerPanelX = startX + panelWidthAll + spacing;
        renderMainElementPanel(guiGraphics, centerElement, centerPanelX, panelY + 6, panelWidthAll, centerPanelHeight);

        int rightPanelX = startX + (panelWidthAll * 2) + (spacing * 2);
        renderElementPanel(guiGraphics, rightElement, rightPanelX, panelY + 15, panelWidthAll, sidePanelHeight, 0xAAFFFFFF, true);

        guiGraphics.pose().popPose();
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderElementPanel(GuiGraphics guiGraphics, SpellbladeElement element,
                                    int x, int y, int width, int height, int textColor, boolean isSidePanel) {
        guiGraphics.fill(x, y, x + width, y + height, 0xFF000000);
        int borderColor = 0xFFFFFFFF;
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor);
        guiGraphics.fill(x - 1, y, x, y + height, borderColor);
        guiGraphics.fill(x + width, y, x + width + 1, y + height, borderColor);

        String name = element.getDisplayName();
        if (isSidePanel && name.length() > 10) {
            name = name.substring(0, 10) + "...";
        }
        guiGraphics.drawCenteredString(this.font, Component.literal(name),
                x + width / 2, y + 8, textColor);

        String description = getElementDescription(element);
        if (description.length() > 60) {
            description = description.substring(0, 60) + "...";
        }
        guiGraphics.drawWordWrap(this.font, Component.literal(description),
                x + 6, y + 25, width - 12, textColor);
    }

    private void renderMainElementPanel(GuiGraphics guiGraphics, SpellbladeElement element,
                                        int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xFF000000);

        int accentColor = getElementColor(element);
        int borderThickness = 4;
        for (int i = 0; i < borderThickness; i++) {
            int alpha = 200 - (i * 40);
            int color = (alpha << 24) | (accentColor & 0xFFFFFF);
            guiGraphics.fill(x - i - 2, y - i - 2, x + width + i + 2, y - i - 2, color);
            guiGraphics.fill(x - i - 2, y + height + i + 2, x + width + i + 2, y + height + i + 2, color);
            guiGraphics.fill(x - i - 2, y - i - 2, x - i - 2, y + height + i + 2, color);
            guiGraphics.fill(x + width + i + 2, y - i - 2, x + width + i + 2, y + height + i + 2, color);
        }

        for (int i = 0; i < borderThickness; i++) {
            guiGraphics.fill(x - i, y - i, x + width + i, y - i, accentColor);
            guiGraphics.fill(x - i, y + height + i, x + width + i, y + height + i, accentColor);
            guiGraphics.fill(x - i, y - i, x - i, y + height + i, accentColor);
            guiGraphics.fill(x + width + i, y - i, x + width + i, y + height + i, accentColor);
        }

        int glowSize = 6;
        for (int i = 0; i < glowSize; i++) {
            int alpha = 80 - (i * 15);
            if (alpha < 0) alpha = 0;
            int glowColor = (alpha << 24) | (accentColor & 0xFFFFFF);
            guiGraphics.fill(x + i, y, x + i + 1, y + height, glowColor);
            guiGraphics.fill(x + width - i - 1, y, x + width - i, y + height, glowColor);
            guiGraphics.fill(x, y + i, x + width, y + i + 1, glowColor);
            guiGraphics.fill(x, y + height - i - 1, x + width, y + height - i, glowColor);
        }

        guiGraphics.drawCenteredString(this.font, Component.literal(element.getDisplayName()),
                x + width / 2, y + 12, 0xFFFFFFFF);

        String description = getElementDescription(element);
        List<Component> lines = splitTextIntoLines(description, width - 16);
        int lineHeight = font.lineHeight;
        int startY = y + 30;
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawCenteredString(this.font, lines.get(i),
                    x + width / 2, startY + (i * lineHeight), 0xFFFFFFFF);
        }
    }

    private List<Component> splitTextIntoLines(String text, int maxWidth) {
        List<Component> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (font.width(testLine) <= maxWidth) {
                currentLine.append(currentLine.isEmpty() ? word : " " + word);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(Component.literal(currentLine.toString()));
                }
                currentLine = new StringBuilder(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(Component.literal(currentLine.toString()));
        }

        return lines;
    }

    private int getElementColor(SpellbladeElement element) {
        return switch (element) {
            case FIRE -> 0xFFFF5522;
            case WATER -> 0xFF33AAFF;
            case LIGHTNING -> 0xFFFFEE55;
            case VOID -> 0xFF9900FF;
            case EARTH -> 0xFF88CC55;
            case WIND -> 0xFFCCFFEE;
            case LIGHT -> 0xFFFFFFCC;
            case ETHER -> 0xFF66FFEE;
            case BLOOD -> 0xFFCC1133;
        };
    }

    private String getElementDescription(SpellbladeElement element) {
        return switch (element) {
            case FIRE -> "Sets foes ablaze, burning them down over time as flames linger.";
            case WATER -> "Drags enemies down, crushing their footing and slowing every step.";
            case LIGHTNING -> "Arcs between foes in a burst of chained current.";
            case VOID -> "Cuts straight through defenses, ignoring guard entirely.";
            case EARTH -> "Roots enemies to the ground the instant it lands, holding them fast.";
            case WIND -> "Sends foes reeling back while you surge forward with the gust.";
            case LIGHT -> "Blinding strikes that sear foes and mend your wounds in kind.";
            case ETHER -> "Warps reality itself to slip past an enemy's guard unseen.";
            case BLOOD -> "Rips vitality from foes with every hit, fueling your own strength.";
        };
    }

    private void selectElement() {
        SpellbladeElement element = elementList.get(currentElementIndex);
        NetworkHandler.sendToServer(new ElementSelectionPacket(element.getId()));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}