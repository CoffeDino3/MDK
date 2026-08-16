package com.CoffeDino.lunacy.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.client.gui.components.ColoredButton;
import com.CoffeDino.lunacy.network.NetworkHandler;
import com.CoffeDino.lunacy.network.RaceSelectionPacket;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RaceSelectionScreen extends Screen {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "textures/gui/race_selection_bg.png");
    private static final int ACCENT = 0xFFBB33FF;

    private int currentRaceIndex = 0;
    private List<races.Race> raceList;
    private Button selectButton;
    private Button randomButton;
    private Button leftArrow;
    private Button rightArrow;
    private int buffScrollOffset = 0;

    public RaceSelectionScreen() {
        super(Component.literal("Choose your Race!"));
        this.raceList = List.of(
                races.Race.SCULK,
                races.Race.WARDER,
                races.Race.ENDER,
                races.Race.PHANTOM,
                races.Race.LOVER,
                races.Race.BELIEVER,
                races.Race.VAMPIREBORN,
                races.Race.ANGELBORN,
                races.Race.ETHEREAL,
                races.Race.CELESTIAL,
                races.Race.GATEKEEPER
        );
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 340;
        int panelHeight = 130;
        int panelLeft = centerX - panelWidth / 2;
        int panelTop = centerY - panelHeight / 2;

        leftArrow = new ColoredButton(
                panelLeft - 35, panelTop + panelHeight / 2 - 12, 25, 25,
                Component.literal("\u25C0"),
                b -> switchRace(-1),
                0xA69678FF,
                0xE07020FF
        );
        addRenderableWidget(leftArrow);

        rightArrow = new ColoredButton(
                panelLeft + panelWidth + 10, panelTop + panelHeight / 2 - 12, 25, 25,
                Component.literal("\u25B6"),
                b -> switchRace(1),
                0xA69678FF,
                0xE07020FF
        );
        addRenderableWidget(rightArrow);

        int buttonGap = 6;
        int randomWidth = 30;
        int selectWidth = 180 - randomWidth - buttonGap;
        int rowY = panelTop + panelHeight + 14;
        int rowLeft = centerX - 90;

        randomButton = new ColoredButton(
                rowLeft, rowY, randomWidth, 27,
                Component.literal(""),
                b -> randomizeAndSelectRace(),
                0xA6A0308A,
                0xCC9933CC
        );
        addRenderableWidget(randomButton);

        selectButton = new ColoredButton(
                rowLeft + randomWidth + buttonGap, rowY, selectWidth, 27,
                Component.literal(""),
                b -> selectRace(),
                0xA60032A0,
                0xCC0066CC
        );
        addRenderableWidget(selectButton);
    }

    private void switchRace(int direction) {
        currentRaceIndex = (currentRaceIndex + direction + raceList.size()) % raceList.size();
        buffScrollOffset = 0;
    }

    private void randomizeAndSelectRace() {
        races.Race race = raceList.get(ThreadLocalRandom.current().nextInt(raceList.size()));
        Lunacy.LOGGER.debug("DEBUG: Random race button pressed!");
        Lunacy.LOGGER.debug("DEBUG: Randomly selected race: " + race.getId() + " - " + race.getDisplayName());
        NetworkHandler.sendToServer(new RaceSelectionPacket(race.getId()));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(BACKGROUND, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
        guiGraphics.fillGradient(0, 0, this.width, 70, 0x99101010, 0x00101010);
        guiGraphics.fillGradient(0, this.height - 70, this.width, this.height, 0x00101010, 0x99101010);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 340;
        int panelHeight = 130;
        int panelLeft = centerX - panelWidth / 2;
        int panelTop = centerY - panelHeight / 2;

        races.Race current = raceList.get(currentRaceIndex);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 100);

        renderTitle(guiGraphics, "CHOOSE YOUR RACE", centerX, panelTop - 32);

        int entityBoxWidth = 100;
        int entityBoxHeight = panelHeight;
        int entityBoxX = panelLeft;
        int entityBoxY = panelTop;

        int gap = 12;
        int infoBoxX = entityBoxX + entityBoxWidth + gap;
        int infoBoxWidth = panelWidth - entityBoxWidth - gap;

        int descHeight = 64;
        int infoGap = 6;
        int buffsHeight = panelHeight - descHeight - infoGap;

        renderGlowPanel(guiGraphics, entityBoxX, entityBoxY, entityBoxWidth, entityBoxHeight);
        drawPlayerModel(guiGraphics, entityBoxX + entityBoxWidth / 2, entityBoxY + entityBoxHeight - 16, 55);

        renderDescriptionPanel(guiGraphics, current, infoBoxX, panelTop, infoBoxWidth, descHeight);
        renderBuffsPanel(guiGraphics, current, infoBoxX, panelTop + descHeight + infoGap, infoBoxWidth, buffsHeight);

        guiGraphics.pose().popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        renderButtonLabel(guiGraphics, "\uD83C\uDFB2", randomButton, 2.0f, -8);
        renderButtonLabel(guiGraphics, "Select Race", selectButton);
    }

    private void renderButtonLabel(GuiGraphics guiGraphics, String text, Button button) {
        renderButtonLabel(guiGraphics, text, button, 1.5f, -4);
    }

    private void renderButtonLabel(GuiGraphics guiGraphics, String text, Button button, float scale, int yOffset) {
        int centerX = button.getX() + button.getWidth() / 2;
        int centerY = button.getY() + button.getHeight() / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY + yOffset, 0);
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.drawCenteredString(this.font, Component.literal(text), 0, 0, 0xFFFFFFFF);
        guiGraphics.pose().popPose();
    }

    private void renderTitle(GuiGraphics guiGraphics, String text, int centerX, int y) {
        Component titleText = Component.literal(text);
        int outlineColor = 0xFF000000 | (ACCENT & 0xFFFFFF);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, y, 0);
        guiGraphics.pose().scale(1.7f, 1.7f, 1.7f);

        int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        for (int[] off : offsets) {
            guiGraphics.drawCenteredString(this.font, titleText, off[0], off[1], outlineColor);
        }
        guiGraphics.drawCenteredString(this.font, titleText, 0, 0, 0xFFFFFFFF);
        guiGraphics.pose().popPose();

        int lineWidth = 140;
        int lineY = y + 16;
        guiGraphics.fill(centerX - lineWidth / 2, lineY, centerX + lineWidth / 2, lineY + 1,
                (0xAA << 24) | (ACCENT & 0xFFFFFF));
        guiGraphics.fill(centerX - lineWidth / 2 - 4, lineY - 2, centerX - lineWidth / 2, lineY + 3,
                (0xFF << 24) | (ACCENT & 0xFFFFFF));
        guiGraphics.fill(centerX + lineWidth / 2, lineY - 2, centerX + lineWidth / 2 + 4, lineY + 3,
                (0xFF << 24) | (ACCENT & 0xFFFFFF));
    }

    private void renderGlowPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xCC000000);

        int borderThickness = 3;
        for (int i = 0; i < borderThickness; i++) {
            int alpha = 200 - (i * 40);
            int color = (alpha << 24) | (ACCENT & 0xFFFFFF);
            guiGraphics.fill(x - i - 1, y - i - 1, x + width + i + 1, y - i, color);
            guiGraphics.fill(x - i - 1, y + height + i, x + width + i + 1, y + height + i + 1, color);
            guiGraphics.fill(x - i - 1, y - i - 1, x - i, y + height + i + 1, color);
            guiGraphics.fill(x + width + i, y - i - 1, x + width + i + 1, y + height + i + 1, color);
        }

        int glowSize = 5;
        for (int i = 0; i < glowSize; i++) {
            int alpha = 70 - (i * 14);
            if (alpha < 0) alpha = 0;
            int glowColor = (alpha << 24) | (ACCENT & 0xFFFFFF);
            guiGraphics.fill(x + i, y, x + i + 1, y + height, glowColor);
            guiGraphics.fill(x + width - i - 1, y, x + width - i, y + height, glowColor);
            guiGraphics.fill(x, y + i, x + width, y + i + 1, glowColor);
            guiGraphics.fill(x, y + height - i - 1, x + width, y + height - i, glowColor);
        }
    }

    private void renderBorderedBox(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xCC000000);
        int borderColor = (0xAA << 24) | (ACCENT & 0xFFFFFF);
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor);
        guiGraphics.fill(x - 1, y, x, y + height, borderColor);
        guiGraphics.fill(x + width, y, x + width + 1, y + height, borderColor);
    }

    private void renderDescriptionPanel(GuiGraphics guiGraphics, races.Race race, int x, int y, int width, int height) {
        renderBorderedBox(guiGraphics, x, y, width, height);

        guiGraphics.drawCenteredString(this.font, Component.literal(race.getDisplayName()),
                x + width / 2, y + 4, 0xFFDD88FF);

        String description = getRaceDescription(race);
        List<Component> lines = splitTextIntoLines(description, width - 14);
        int lineHeight = font.lineHeight + 1;
        int startY = y + 16;
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(this.font, lines.get(i), x + 7, startY + (i * lineHeight), 0xFFDDDDDD, false);
        }
    }

    private void renderBuffsPanel(GuiGraphics guiGraphics, races.Race race, int x, int y, int width, int height) {
        renderBorderedBox(guiGraphics, x, y, width, height);

        guiGraphics.drawCenteredString(this.font, Component.literal("Racial Traits"),
                x + width / 2, y + 4, 0xFFDD88FF);

        List<String> buffs = getRaceBuffs(race);
        int contentTop = y + 16;
        int visibleHeight = height - (contentTop - y) - 2;

        int bulletWidth = font.width("\u2726 ");
        int maxTextWidth = width - 14 - bulletWidth;

        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        List<Boolean> lineHasBullet = new ArrayList<>();
        for (String buff : buffs) {
            List<FormattedCharSequence> split = font.split(Component.literal(buff), maxTextWidth);
            for (int i = 0; i < split.size(); i++) {
                wrappedLines.add(split.get(i));
                lineHasBullet.add(i == 0);
            }
        }

        int fullRow = font.lineHeight + 3;
        int continuationRow = fullRow * 5/6;

        int totalHeight = 0;
        for (boolean hasBullet : lineHasBullet) {
            totalHeight += hasBullet ? fullRow : continuationRow;
        }

        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        if (buffScrollOffset > maxScroll) buffScrollOffset = maxScroll;
        if (buffScrollOffset < 0) buffScrollOffset = 0;

        guiGraphics.enableScissor(x + 1, contentTop, x + width - 1, contentTop + visibleHeight);

        int lineY = contentTop - buffScrollOffset;
        for (int i = 0; i < wrappedLines.size(); i++) {
            if (i > 0) {
                lineY += lineHasBullet.get(i) ? fullRow : continuationRow;
            }
            if (lineHasBullet.get(i)) {
                guiGraphics.drawString(this.font, Component.literal("\u2726 "), x + 7, lineY, 0xFFFFFFFF, false);
            }
            guiGraphics.drawString(this.font, wrappedLines.get(i), x + 7 + bulletWidth, lineY, 0xFFFFFFFF, false);
        }

        guiGraphics.disableScissor();

        if (totalHeight > visibleHeight) {
            int trackWidth = 3;
            int trackX = x + width - trackWidth - 3;
            float scrollRatio = (float) visibleHeight / totalHeight;
            int scrollBarHeight = Math.max(6, (int) (visibleHeight * scrollRatio));
            int scrollBarY = contentTop + (int) ((visibleHeight - scrollBarHeight) * (buffScrollOffset / (float) maxScroll));

            guiGraphics.fill(trackX, contentTop, trackX + trackWidth, contentTop + visibleHeight, 0x33000000);
            guiGraphics.fill(trackX, scrollBarY, trackX + trackWidth, scrollBarY + scrollBarHeight, (0xE0 << 24) | (ACCENT & 0xFFFFFF));
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

    private String getRaceDescription(races.Race race) {
        switch (race) {
            case SCULK: return "Grown in the void, Sculks toughen with every fight and can stash items safely inside themselves.";
            case WARDER: return "Warders bend dimensional energy into raw force, crushing anything caught in their field.";
            case ENDER: return "Enders blink through space, striking before you see them and vanishing before you can react.";
            case PHANTOM: return "Phantoms hunt from above, gliding over the battlefield and closing in with precision.";
            case VAMPIREBORN: return "Vampireborn burn their own life away for power, hunting best under cover of night.";
            case BELIEVER: return "A Believer's faith holds like armor, keeping them steady in the worst of fights.";
            case LOVER: return "Lovers fight for what they cherish, redirecting harm away from themselves and onto their foes.";
            case ANGELBORN: return "Angelborn mend fast and strike faster, mending light into smiting beams.";
            case ETHEREAL: return "Ethereals slip between realities, nearly impossible to hit and quick to punish those who try.";
            case CELESTIAL: return "Celestials command gravity itself, pulling or shoving foes with cosmic force.";
            case GATEKEEPER: return "Gatekeepers tear open portals to their treasury, pulling out a milliard of weapons.";
            default: return "A mysterious race with unknown abilities.";
        }
    }

    private List<String> getRaceBuffs(races.Race race) {
        List<String> buffs = new ArrayList<>();

        switch (race) {
            case SCULK -> {
                buffs.add("Takes reduced damage");
                buffs.add("Ability: Open a death-proof storage chest anytime");
            }
            case WARDER -> {
                buffs.add("Deals bonus melee damage");
                buffs.add("Ability: Unleash a damaging ring that breaks nearby blocks");
            }
            case ENDER -> {
                buffs.add("Sees clearly in the dark");
                buffs.add("Ability: Teleport to where you're looking");
            }
            case PHANTOM -> {
                buffs.add("Jumps higher");
                buffs.add("Ability: Launch upward and glide");
            }
            case LOVER -> {
                buffs.add("Villagers give better trades");
                buffs.add("Ability: Redirect incoming damage to a nearby enemy");
            }
            case BELIEVER -> {
                buffs.add("Better loot & fishing luck");
                buffs.add("Ability: Raise a safe barrier that shields you inside");
            }
            case ANGELBORN -> {
                buffs.add("Slowly regenerates health");
                buffs.add("Ability: Spawn portals that damage nearby hostile mobs");
            }
            case VAMPIREBORN -> {
                buffs.add("Heal when you deal damage");
                buffs.add("Burns in direct sunlight");
                buffs.add("Ability: Charge and fire a blood projectile, stronger the longer you hold");
            }
            case ETHEREAL -> {
                buffs.add("Your skin reflects damage back at attackers");
                buffs.add("Ability: Turn ethereal and phase through blocks");
            }
            case CELESTIAL -> {
                buffs.add("Normal foes can't see you");
                buffs.add("Ability: Create a gravity field, then push or pull everything in it");
            }
            case GATEKEEPER -> {
                buffs.add("Hunger never depletes");
                buffs.add("Ability: Summon portals that fire weapons from your inventory");
            }
        }

        return buffs;
    }

    private void drawPlayerModel(GuiGraphics graphics, int x, int y, int scale) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int centerX = x;
        int centerY = y;

        double mouseX = Minecraft.getInstance().mouseHandler.xpos() * (double) this.width / (double) Minecraft.getInstance().getWindow().getScreenWidth();
        double mouseY = Minecraft.getInstance().mouseHandler.ypos() * (double) this.height / (double) Minecraft.getInstance().getWindow().getScreenHeight();

        float xMouseOffset = (float) (centerX - mouseX);
        float yMouseOffset = (float) (mouseY - (centerY - 30));

        float yBodyRot = player.yBodyRot;
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        float yHeadRotO = player.yHeadRotO;
        float yHeadRot = player.yHeadRot;

        player.yBodyRot = (float) Math.atan(xMouseOffset / 40.0F) * 20.0F;
        player.setYRot((float) Math.atan(xMouseOffset / 40.0F) * 40.0F);
        player.setXRot((float) Math.atan(yMouseOffset / 40.0F) * 20.0F);
        player.yHeadRot = player.getYRot();
        player.yHeadRotO = player.getYRot();

        RenderSystem.enableDepthTest();
        graphics.pose().pushPose();

        graphics.pose().translate(centerX, centerY + 8, 50.0F);
        graphics.pose().scale((float) scale, (float) scale, (float) -scale);

        graphics.pose().mulPose(Axis.ZP.rotationDegrees(180.0F));
        graphics.pose().mulPose(Axis.YP.rotationDegrees(180.0F));

        graphics.pose().mulPose(Axis.XP.rotationDegrees(player.getXRot() * 0.1F));

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.overrideCameraOrientation(Axis.YP.rotationDegrees(180.0F));
        dispatcher.setRenderShadow(false);

        graphics.flush();
        RenderSystem.runAsFancy(() -> {
            dispatcher.render(player, 0, 0, 0, 0.0F, 1.0F, graphics.pose(), graphics.bufferSource(), 15728880);
        });
        graphics.bufferSource().endBatch();

        dispatcher.setRenderShadow(true);
        graphics.pose().popPose();
        RenderSystem.disableDepthTest();

        player.yBodyRot = yBodyRot;
        player.setYRot(yRot);
        player.setXRot(xRot);
        player.yHeadRotO = yHeadRotO;
        player.yHeadRot = yHeadRot;
    }

    private void selectRace() {
        Lunacy.LOGGER.debug("DEBUG: Select button pressed!");
        races.Race race = raceList.get(currentRaceIndex);
        Lunacy.LOGGER.debug("DEBUG: Selected race: " + race.getId() + " - " + race.getDisplayName());
        Lunacy.LOGGER.debug("DEBUG: Sending packet to server...");
        NetworkHandler.sendToServer(new RaceSelectionPacket(race.getId()));
        Lunacy.LOGGER.debug("DEBUG: Closing screen");
        Minecraft.getInstance().setScreen(null);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        buffScrollOffset -= (int) (scrollY * 10);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}