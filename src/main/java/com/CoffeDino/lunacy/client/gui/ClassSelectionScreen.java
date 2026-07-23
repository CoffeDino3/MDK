package com.CoffeDino.lunacy.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.client.gui.components.ColoredButton;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.network.ClassSelectionPacket;
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

public class ClassSelectionScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "textures/gui/class_selection_bg.png");

    private int currentClassIndex = 0;
    private List<PlayerClasses.PlayerClass> classList;
    private Button selectButton;
    private Button leftArrow;
    private Button rightArrow;
    private boolean hasExistingClass = false;

    public ClassSelectionScreen() {
        super(Component.literal("Select your Class!"));
        this.classList = List.of(PlayerClasses.PlayerClass.values());
    }

    @Override
    protected void init() {
        super.init();
        hasExistingClass = PlayerClasses.hasChosenClass(Minecraft.getInstance().player);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 350;
        int panelHeight = 200;
        int panelLeft = centerX - panelWidth / 2;
        int panelTop = centerY - panelHeight / 2;
        leftArrow = new ColoredButton(
                panelLeft - 35, centerY - 10, 25, 25,
                Component.literal("◀"),
                b -> switchClass(-1),
                0xA69678FF,
                0xE07020FF
        );
        addRenderableWidget(leftArrow);

        rightArrow = new ColoredButton(
                panelLeft + panelWidth + 10, centerY - 10, 25, 25,
                Component.literal("▶"),
                b -> switchClass(1),
                0xA69678FF,
                0xE07020FF
        );
        addRenderableWidget(rightArrow);
        if (!hasExistingClass) {
            selectButton = new ColoredButton(
                    centerX - 60, panelTop + panelHeight - 30, 120, 20,
                    Component.literal("Select Class"),
                    b -> selectClass(),
                    0xA60032A0,
                    0xCC0066CC
            );
        } else {
            selectButton = new ColoredButton(
                    centerX - 60, panelTop + panelHeight - 30, 120, 20,
                    Component.literal("Close"),
                    b -> closeScreen(),
                    0xA69678FF,
                    0xE07020FF
            );
            PlayerClasses.PlayerClass currentClass = PlayerClasses.getPlayerClass(Minecraft.getInstance().player);
            if (currentClass != null) {
                for (int i = 0; i < classList.size(); i++) {
                    if (classList.get(i) == currentClass) {
                        currentClassIndex = i;
                        break;
                    }
                }
            }
        }
        addRenderableWidget(selectButton);
    }

    private void switchClass(int direction) {
        currentClassIndex = (currentClassIndex + direction + classList.size()) % classList.size();
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
        int panelLeft = centerX - panelWidth / 2;
        int panelTop = centerY - panelHeight / 2;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 100);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, panelTop + 10, 0);
        guiGraphics.pose().scale(1.8f, 1.8f, 1.8f);
        guiGraphics.drawCenteredString(this.font, Component.literal("SELECT YOUR CLASS"), 0, 0, 0xFFFFFF);
        guiGraphics.pose().popPose();
        int leftIndex = (currentClassIndex - 1 + classList.size()) % classList.size();
        int centerIndex = currentClassIndex;
        int rightIndex = (currentClassIndex + 1) % classList.size();

        PlayerClasses.PlayerClass leftClass = classList.get(leftIndex);
        PlayerClasses.PlayerClass centerClass = classList.get(centerIndex);
        PlayerClasses.PlayerClass rightClass = classList.get(rightIndex);
        int panelWidthAll = 80;
        int centerPanelHeight = 110;
        int sidePanelHeight = 90;
        int panelY = panelTop + 45;
        int spacing = 15;
        int totalWidth = (panelWidthAll * 3) + (spacing * 2);
        int startX = centerX - (totalWidth / 2);
        int leftPanelX = startX;
        renderClassPanel(guiGraphics, leftClass, leftPanelX, panelY + 15, panelWidthAll, sidePanelHeight, 0xAAFFFFFF, true);
        int centerPanelX = startX + panelWidthAll + spacing;
        renderMainClassPanel(guiGraphics, centerClass, centerPanelX, panelY+6, panelWidthAll, centerPanelHeight);
        int rightPanelX = startX + (panelWidthAll * 2) + (spacing * 2);
        renderClassPanel(guiGraphics, rightClass, rightPanelX, panelY + 15, panelWidthAll, sidePanelHeight, 0xAAFFFFFF, true);
        if (hasExistingClass) {
            PlayerClasses.PlayerClass currentClass = PlayerClasses.getPlayerClass(Minecraft.getInstance().player);
            if (currentClass == centerClass) {
                guiGraphics.drawCenteredString(this.font,
                        Component.literal("(Current Class)").withStyle(net.minecraft.ChatFormatting.YELLOW),
                        centerX, panelY + centerPanelHeight + 5, 0xFFFFFF);
            }
        }

        guiGraphics.pose().popPose();
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderClassPanel(GuiGraphics guiGraphics, PlayerClasses.PlayerClass playerClass,
                                  int x, int y, int width, int height, int textColor, boolean isSidePanel) {
        guiGraphics.fill(x, y, x + width, y + height, 0xFF000000);
        int borderColor = 0xFFFFFFFF;
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor);
        guiGraphics.fill(x - 1, y, x, y + height, borderColor);
        guiGraphics.fill(x + width, y, x + width + 1, y + height, borderColor);
        String className = playerClass.getDisplayName();
        if (isSidePanel && className.length() > 10) {
            className = className.substring(0, 10) + "...";
        }
        guiGraphics.drawCenteredString(this.font, Component.literal(className),
                x + width / 2, y + 8, textColor);
        String description = getClassDescription(playerClass);
        if (description.length() > 60) {
            description = description.substring(0, 60) + "...";
        }
        guiGraphics.drawWordWrap(this.font, Component.literal(description),
                x + 6, y + 25, width - 12, textColor);
    }

    private void renderMainClassPanel(GuiGraphics guiGraphics, PlayerClasses.PlayerClass playerClass,
                                      int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xFF000000);
        int vibrantPurple = 0xFFFF00FF;
        int mediumPurple = 0xFFCC00CC;
        int darkPurple = 0xFF990099;
        int borderThickness = 4;
        for (int i = 0; i < borderThickness; i++) {
            int alpha = 200 - (i * 40);
            int color = (alpha << 24) | (vibrantPurple & 0xFFFFFF);
            guiGraphics.fill(x - i - 2, y - i - 2, x + width + i + 2, y - i - 2, color);
            guiGraphics.fill(x - i - 2, y + height + i + 2, x + width + i + 2, y + height + i + 2, color);
            guiGraphics.fill(x - i - 2, y - i - 2, x - i - 2, y + height + i + 2, color);
            guiGraphics.fill(x + width + i + 2, y - i - 2, x + width + i + 2, y + height + i + 2, color);
        }

        for (int i = 0; i < borderThickness; i++) {
            int color = vibrantPurple;
            guiGraphics.fill(x - i, y - i, x + width + i, y - i, color);
            guiGraphics.fill(x - i, y + height + i, x + width + i, y + height + i, color);
            guiGraphics.fill(x - i, y - i, x - i, y + height + i, color);
            guiGraphics.fill(x + width + i, y - i, x + width + i, y + height + i, color);
        }

        int glowSize = 6;
        for (int i = 0; i < glowSize; i++) {
            int alpha = 80 - (i * 15);
            if (alpha < 0) alpha = 0;
            int glowColor = (alpha << 24) | (vibrantPurple & 0xFFFFFF);
            guiGraphics.fill(x + i, y, x + i + 1, y + height, glowColor);
            guiGraphics.fill(x + width - i - 1, y, x + width - i, y + height, glowColor);
            guiGraphics.fill(x, y + i, x + width, y + i + 1, glowColor);
            guiGraphics.fill(x, y + height - i - 1, x + width, y + height - i, glowColor);
        }
        guiGraphics.drawCenteredString(this.font, Component.literal(playerClass.getDisplayName()),
                x + width / 2, y + 10, 0xFFFFFFFF);


        String description = getClassDescription(playerClass);
        List<Component> lines = splitTextIntoLines(description, width - 16);
        int lineHeight = font.lineHeight;
        int totalTextHeight = lines.size() * lineHeight;
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

    private String getClassDescription(PlayerClasses.PlayerClass playerClass) {
        switch (playerClass) {
            case SWORDSMAN: return "Relentless master of steel, each swing honed to lethal perfection.";
            case SPEARMAN: return "Calm precision strikes first, keeping chaos at the edge of reach.";
            case VIKING: return "A storm of fury and strength, breaking foes with roaring defiance.";
            case FENCER: return "Grace and speed entwined, every duel being a dance with death.";
            case ARCHER: return "Eyes sharp as glass, loosing arrows that never miss their mark.";
            case ASSASSIN: return "Shadow incarnate, striking unseen and gone before blood hits ground.";
            case GUARDIAN: return "Unshaken bulwark of resolve, turning enemy fury into wasted effort.";
            case SPELLBLADE: return "A conduit of elements, fusing magic and motion into destruction.";
            case CHRONOBLADE: return "Moves between heartbeats, twisting time itself to undo your strike.";
            case REAPER: return "Silent bringer of endings, harvesting souls with cold, patient grace.";
            case GUNSMITH: return "Master of firearms, delivering precision and power with every shot.";
            default: return "A specialized combat style with unique techniques and abilities.";
        }
    }

    private void selectClass() {
        if (hasExistingClass) {
            closeScreen();
            return;
        }

        Lunacy.LOGGER.debug("DEBUG: Class select button pressed!");
        PlayerClasses.PlayerClass playerClass = classList.get(currentClassIndex);
        Lunacy.LOGGER.debug("DEBUG: Selected class: " + playerClass.getId() + " - " + playerClass.getDisplayName());
        NetworkHandler.sendToServer(new ClassSelectionPacket(playerClass.getId()));
        Minecraft.getInstance().setScreen(null);
    }

    private void closeScreen() {
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