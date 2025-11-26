// net/CoffeDino/testmod/client/gui/ClassSelectionScreen.java
package net.CoffeDino.testmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.client.gui.components.ColoredButton;
import net.CoffeDino.testmod.classes.PlayerClasses;
import net.CoffeDino.testmod.network.ClassSelectionPacket;
import net.CoffeDino.testmod.network.NetworkHandler;
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
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "textures/gui/class_selection_bg.png");

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

        // Check if player already has a class
        hasExistingClass = PlayerClasses.hasChosenClass(Minecraft.getInstance().player);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Horizontal layout dimensions - smaller overall
        int panelWidth = 350;
        int panelHeight = 200;
        int panelLeft = centerX - panelWidth / 2;
        int panelTop = centerY - panelHeight / 2;

        // Arrow buttons on sides
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

        // Select/Close button at bottom
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

            // Auto-navigate to current class
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
        // Render background image prominently
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Draw background image covering the whole screen
        guiGraphics.blit(BACKGROUND, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        // Semi-dark overlay for readability
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

        // Title - bigger and more prominent
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, panelTop + 10, 0);
        guiGraphics.pose().scale(1.8f, 1.8f, 1.8f);
        guiGraphics.drawCenteredString(this.font, Component.literal("SELECT YOUR CLASS"), 0, 0, 0xFFFFFF);
        guiGraphics.pose().popPose();

        // Calculate class indices for the three panels
        int leftIndex = (currentClassIndex - 1 + classList.size()) % classList.size();
        int centerIndex = currentClassIndex;
        int rightIndex = (currentClassIndex + 1) % classList.size();

        PlayerClasses.PlayerClass leftClass = classList.get(leftIndex);
        PlayerClasses.PlayerClass centerClass = classList.get(centerIndex);
        PlayerClasses.PlayerClass rightClass = classList.get(rightIndex);

        // Panel dimensions - all panels same width now
        int panelWidthAll = 80; // Same width as side panels
        int centerPanelHeight = 110;
        int sidePanelHeight = 90;
        int panelY = panelTop + 45;

        // Spacing between panels
        int spacing = 15;

        // Calculate positions for equal spacing
        int totalWidth = (panelWidthAll * 3) + (spacing * 2);
        int startX = centerX - (totalWidth / 2);

        // Left panel (small, positioned with spacing)
        int leftPanelX = startX;
        renderClassPanel(guiGraphics, leftClass, leftPanelX, panelY + 15, panelWidthAll, sidePanelHeight, 0xAAFFFFFF, true);

        // Center panel (main, with vibrant purple glow effect) - same width as sides
        int centerPanelX = startX + panelWidthAll + spacing;
        renderMainClassPanel(guiGraphics, centerClass, centerPanelX, panelY+6, panelWidthAll, centerPanelHeight);

        // Right panel (small, positioned with spacing)
        int rightPanelX = startX + (panelWidthAll * 2) + (spacing * 2);
        renderClassPanel(guiGraphics, rightClass, rightPanelX, panelY + 15, panelWidthAll, sidePanelHeight, 0xAAFFFFFF, true);

        // Show current class indicator if player already has a class
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
        // Solid panel background (no opacity)
        guiGraphics.fill(x, y, x + width, y + height, 0xFF000000);

        // Simple border for side panels
        int borderColor = 0xFFFFFFFF;
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor);
        guiGraphics.fill(x - 1, y, x, y + height, borderColor);
        guiGraphics.fill(x + width, y, x + width + 1, y + height, borderColor);

        // Class name
        String className = playerClass.getDisplayName();
        if (isSidePanel && className.length() > 10) {
            className = className.substring(0, 10) + "...";
        }

        guiGraphics.drawCenteredString(this.font, Component.literal(className),
                x + width / 2, y + 8, textColor);

        // Description - shorter for side panels
        String description = getClassDescription(playerClass);
        if (description.length() > 60) {
            description = description.substring(0, 60) + "...";
        }

        guiGraphics.drawWordWrap(this.font, Component.literal(description),
                x + 6, y + 25, width - 12, textColor);
    }

    private void renderMainClassPanel(GuiGraphics guiGraphics, PlayerClasses.PlayerClass playerClass,
                                      int x, int y, int width, int height) {
        // Solid panel background (no opacity)
        guiGraphics.fill(x, y, x + width, y + height, 0xFF000000);

        // Very vibrant purple colors
        int vibrantPurple = 0xFFFF00FF; // Bright magenta-purple
        int mediumPurple = 0xFFCC00CC;  // Medium purple
        int darkPurple = 0xFF990099;    // Darker purple

        // Thick vibrant purple border with multiple layers
        int borderThickness = 4;

        // Outer glow layer (most vibrant)
        for (int i = 0; i < borderThickness; i++) {
            int alpha = 200 - (i * 40); // High alpha for vibrancy
            int color = (alpha << 24) | (vibrantPurple & 0xFFFFFF);

            // Top border
            guiGraphics.fill(x - i - 2, y - i - 2, x + width + i + 2, y - i - 2, color);
            // Bottom border
            guiGraphics.fill(x - i - 2, y + height + i + 2, x + width + i + 2, y + height + i + 2, color);
            // Left border
            guiGraphics.fill(x - i - 2, y - i - 2, x - i - 2, y + height + i + 2, color);
            // Right border
            guiGraphics.fill(x + width + i + 2, y - i - 2, x + width + i + 2, y + height + i + 2, color);
        }

        // Main border layer
        for (int i = 0; i < borderThickness; i++) {
            int color = vibrantPurple; // Full vibrancy

            // Top border
            guiGraphics.fill(x - i, y - i, x + width + i, y - i, color);
            // Bottom border
            guiGraphics.fill(x - i, y + height + i, x + width + i, y + height + i, color);
            // Left border
            guiGraphics.fill(x - i, y - i, x - i, y + height + i, color);
            // Right border
            guiGraphics.fill(x + width + i, y - i, x + width + i, y + height + i, color);
        }

        // Inner glow effect
        int glowSize = 6;
        for (int i = 0; i < glowSize; i++) {
            int alpha = 80 - (i * 15); // Bright inner glow
            if (alpha < 0) alpha = 0;
            int glowColor = (alpha << 24) | (vibrantPurple & 0xFFFFFF);

            // Left inner glow
            guiGraphics.fill(x + i, y, x + i + 1, y + height, glowColor);
            // Right inner glow
            guiGraphics.fill(x + width - i - 1, y, x + width - i, y + height, glowColor);
            // Top inner glow
            guiGraphics.fill(x, y + i, x + width, y + i + 1, glowColor);
            // Bottom inner glow
            guiGraphics.fill(x, y + height - i - 1, x + width, y + height - i, glowColor);
        }

        // Class name - bright white for contrast
        guiGraphics.drawCenteredString(this.font, Component.literal(playerClass.getDisplayName()),
                x + width / 2, y + 10, 0xFFFFFFFF);

        // Description

        String description = getClassDescription(playerClass);


        // Split description into multiple lines for centered rendering
        List<Component> lines = splitTextIntoLines(description, width - 16);
        int lineHeight = font.lineHeight;
        int totalTextHeight = lines.size() * lineHeight;
        int startY = y + 30; // Starting Y position for text

        // Render each line centered
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

        System.out.println("DEBUG: Class select button pressed!");
        PlayerClasses.PlayerClass playerClass = classList.get(currentClassIndex);
        System.out.println("DEBUG: Selected class: " + playerClass.getId() + " - " + playerClass.getDisplayName());
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