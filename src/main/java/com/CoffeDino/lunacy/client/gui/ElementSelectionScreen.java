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
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "textures/gui/element_selection.png");

    private static final int BG_NATIVE_WIDTH = 799;
    private static final int BG_NATIVE_HEIGHT = 625;

    // frameWidth at which the original fixed text sizes (1.8f title scale, etc.) looked right.
    // textScale is derived from how far the current frame is from this reference, so text grows
    // and shrinks in step with the panel as the window is resized.
    private static final int TEXT_REFERENCE_FRAME_WIDTH = 650;

    // extra flat multiplier on top of textScale to bump overall text size up
    private static final float TEXT_SIZE_BOOST = 1.3f;
    // additional multipliers layered on top of TEXT_SIZE_BOOST, applied separately
    // so the title and description can be tuned independently
    private static final float TITLE_EXTRA_BOOST = 1.45f;
    private static final float DESC_EXTRA_BOOST = 1.1f;

    private int currentElementIndex = 0;
    private final List<SpellbladeElement> elementList = List.of(SpellbladeElement.values());
    private Button selectButton;
    private Button leftArrow;
    private Button rightArrow;

    // computed once in init(), reused by render()
    private int frameX, frameY, frameWidth, frameHeight;
    private float textScale;

    public ElementSelectionScreen() {
        super(Component.literal("Choose your Element!"));
    }

    @Override
    protected void init() {
        super.init();

        computeFrameBounds();

        textScale = Math.min(1.6f, Math.max(0.6f, (float) frameWidth / TEXT_REFERENCE_FRAME_WIDTH)) * TEXT_SIZE_BOOST;

        int arrowY = frameY + frameHeight / 2 - 12; // vertically centered on the panel

        // outer edges of the arrow buttons now flush with the frame's left/right borders
        leftArrow = new ColoredButton(
                frameX, arrowY, 25, 25,
                Component.literal("◀"),
                b -> switchElement(-1),
                0xA69678FF,
                0xE07020FF
        );
        addRenderableWidget(leftArrow);

        rightArrow = new ColoredButton(
                frameX + frameWidth - 25, arrowY, 25, 25,
                Component.literal("▶"),
                b -> switchElement(1),
                0xA69678FF,
                0xE07020FF
        );
        addRenderableWidget(rightArrow);

        // nudged up a bit (was frameHeight - 60) and recolored to an opaque purple
        selectButton = new ColoredButton(
                this.width / 2 - 60, frameY + frameHeight - 72, 120, 20,
                Component.literal("Select"),
                b -> selectElement(),
                0xE6663399,
                0xF08855BB
        );
        addRenderableWidget(selectButton);
    }

    private void computeFrameBounds() {
        float aspect = (float) BG_NATIVE_WIDTH / BG_NATIVE_HEIGHT;
        int maxWidth = (int) (this.width * 0.85f);
        int maxHeight = (int) (this.height * 0.85f);

        frameWidth = maxWidth;
        frameHeight = (int) (frameWidth / aspect);
        if (frameHeight > maxHeight) {
            frameHeight = maxHeight;
            frameWidth = (int) (frameHeight * aspect);
        }

        frameX = this.width / 2 - frameWidth / 2;
        frameY = this.height / 2 - frameHeight / 2;
    }

    private void switchElement(int direction) {
        currentElementIndex = (currentElementIndex + direction + elementList.size()) % elementList.size();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x80101010, 0xA0101010);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        guiGraphics.blit(BACKGROUND, frameX, frameY, frameWidth, frameHeight,
                0f, 0f, BG_NATIVE_WIDTH, BG_NATIVE_HEIGHT, BG_NATIVE_WIDTH, BG_NATIVE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;
        SpellbladeElement current = elementList.get(currentElementIndex);
        int accentColor = getElementColor(current);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 100);

        // title - name of current element, scaled with the panel, moved up slightly
        float titleScale = 1.8f * textScale * TITLE_EXTRA_BOOST;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, frameY + 65, 0);
        guiGraphics.pose().scale(titleScale, titleScale, titleScale);
        guiGraphics.drawCenteredString(this.font, Component.literal(current.getDisplayName()), 0, 0, accentColor);
        guiGraphics.pose().popPose();

        // description, centered as a wrapped block, also scaled with the panel plus a slight buff.
        // wrap width is computed in "unscaled" font units (i.e. divided by descScale) since the
        // pose scale below stretches whatever we draw at (0,0) by descScale afterward.
        float descScale = textScale * DESC_EXTRA_BOOST;
        int descWidth = (int) (frameWidth * 0.6f);
        int unscaledDescWidth = (int) (descWidth / descScale);
        int descStartY = frameY + frameHeight / 2 - 10;
        int lineHeight = this.font.lineHeight + 2;

        List<String> lines = wrapTextCentered(getElementDescription(current), unscaledDescWidth);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, descStartY, 0);
        guiGraphics.pose().scale(descScale, descScale, descScale);
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawCenteredString(this.font, lines.get(i), 0, i * lineHeight, 0xFFFFFF);
        }
        guiGraphics.pose().popPose();

        guiGraphics.pose().popPose();
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /** Wraps text to fit within maxWidth, returning lines meant to be drawn with drawCenteredString. */
    private List<String> wrapTextCentered(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (this.font.width(testLine) <= maxWidth || currentLine.isEmpty()) {
                currentLine = new StringBuilder(testLine);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
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
            case FIRE -> "Channels raging flame, scorching foes and leaving them to burn long after the blade withdraws.";
            case WATER -> "Bends the tide itself, weighing enemies down and washing away their footing.";
            case LIGHTNING -> "Crackles with stormlight, arcing between foes in a chain of searing current.";
            case VOID -> "Tears at the fabric between worlds, striking through defenses as if they weren't there.";
            case EARTH -> "Grounds every strike with the weight of stone, rooting enemies where they stand.";
            case WIND -> "Rides the gale, striking with a gust that flings enemies back and carries the wielder forward.";
            case LIGHT -> "Radiant and searing, blinding foes while mending the wounds of the righteous.";
            case ETHER -> "Unstable and otherworldly, warping fate itself to slip past an enemy's guard.";
            case BLOOD -> "Feeds on the wounds it opens, trading risk for a surge of stolen vitality.";
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