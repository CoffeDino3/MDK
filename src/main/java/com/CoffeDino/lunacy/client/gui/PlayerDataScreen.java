package com.CoffeDino.lunacy.client.gui;

import com.CoffeDino.lunacy.client.KeyBindHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.leveling.ClientPlayerLevelData;
import com.CoffeDino.lunacy.races.RaceLevelScaling;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerDataScreen extends Screen {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "textures/gui/player_data_bg2.png");
    private static final int ACCENT = 0xFFB388E8;
    private static final int ACCENT_DEEP = 0xFF6E2E8F;
    private static final int BOX_FILL_TOP = 0xE01C0F2A;
    private static final int BOX_FILL_BOTTOM = 0xE0120A1C;
    private static final int BOX_BORDER = 0xFF3D1550;
    private static final int BOX_BORDER_BRIGHT = 0xFFD9B3FF;
    private static final int TEXT_LIGHT = 0xFFF3E8FF;
    private static final int TEXT_LABEL = 0xFF9E7EC2;
    private static final int BUFF_RACE_COLOR = 0xFF4A9EFF;
    private static final int BUFF_CLASS_COLOR = 0xFFFF9A3D;
    private static final int BG_TEX_WIDTH = 128;
    private static final int BG_TEX_HEIGHT = 128;
    private static final int BG_PANEL_WIDTH = 190;
    private static final int BG_PANEL_HEIGHT = 150;
    private static final int PANEL_WIDTH = 208;
    private static final int PANEL_HEIGHT = 160;
    private static final int PADDING = 6;
    private static final int XP_AREA_HEIGHT = 16;
    private static final int ROW_HEIGHT = 14;
    private static final int GAP = 4;

    private static final int LEFT_SHIFT = 3;
    private static final int UP_SHIFT = 6;
    private static final int CONTENT_TRIM = 14;
    private static final int BOX_DOWN_SHIFT = 6;
    private static final int BUFFS_EXTRA_GAP = 6;
    private static final int MODEL_BOX_WIDTH = 58;
    private static final int MODEL_SCALE = 34;
    private int buffScrollOffset = 0;
    private static final int BUFF_ROW_HEIGHT = 14;

    private record BuffEntry(String text, boolean fromClass) {}

    public PlayerDataScreen() {
        super(Component.literal("Player Data"));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x66000000);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float widthScale = 1.5f;
        float heightScale = 2.1f;

        int scaledBgWidth = (int) (BG_PANEL_WIDTH * widthScale);
        int scaledBgHeight = (int) (BG_PANEL_HEIGHT * heightScale);

        int bgLeft = centerX - scaledBgWidth / 2;
        int bgTop = centerY - scaledBgHeight / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        RenderSystem.setShaderColor(0.9f, 0.9f, 0.9f, 1.0f);

        guiGraphics.blit(BACKGROUND, bgLeft, bgTop, scaledBgWidth, scaledBgHeight,
                0.0f, 0.0f, BG_TEX_WIDTH, BG_TEX_HEIGHT, BG_TEX_WIDTH, BG_TEX_HEIGHT);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        int centerX = this.width / 2 - LEFT_SHIFT;
        int centerY = this.height / 2 - UP_SHIFT;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = centerY - PANEL_HEIGHT / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 100);

        int contentTop = panelTop + PADDING;
        int contentHeight = PANEL_HEIGHT - PADDING * 2 - XP_AREA_HEIGHT - GAP - CONTENT_TRIM;
        int modelBoxX = panelLeft + PADDING - 10;
        int modelBoxY = contentTop + BOX_DOWN_SHIFT;
        int modelBoxHeight = contentHeight;
        renderBox(guiGraphics, modelBoxX, modelBoxY, MODEL_BOX_WIDTH, modelBoxHeight);
        drawModelFloor(guiGraphics, modelBoxX, modelBoxY, MODEL_BOX_WIDTH, modelBoxHeight);
        drawPlayerModel(guiGraphics, modelBoxX + MODEL_BOX_WIDTH / 2, modelBoxY + modelBoxHeight - 13, MODEL_SCALE);
        int rightColX = modelBoxX + MODEL_BOX_WIDTH + GAP + 10;
        int rightColWidth = PANEL_WIDTH - PADDING * 2 - MODEL_BOX_WIDTH - GAP - 4;
        int row1Y = contentTop + BOX_DOWN_SHIFT;
        int nameBoxWidth = (int) ((rightColWidth - GAP) * 0.62f);
        int levelBoxWidth = rightColWidth - GAP - nameBoxWidth;
        renderBox(guiGraphics, rightColX, row1Y, nameBoxWidth, ROW_HEIGHT);
        renderBox(guiGraphics, rightColX + nameBoxWidth + GAP, row1Y, levelBoxWidth, ROW_HEIGHT);
        Player player = Minecraft.getInstance().player;
        String playerName = player != null ? player.getName().getString() : "Player";
        drawCenteredInBox(guiGraphics, playerName, rightColX, row1Y, nameBoxWidth, ROW_HEIGHT);
        drawCenteredInBox(guiGraphics, "Lvl: " + ClientPlayerLevelData.getLevel(),
                rightColX + nameBoxWidth + GAP, row1Y, levelBoxWidth, ROW_HEIGHT);
        int row2Y = row1Y + ROW_HEIGHT + GAP;
        int availableWidth = rightColWidth - GAP;
        int raceBoxWidth = availableWidth / 2;
        int classBoxWidth = availableWidth - raceBoxWidth;
        int classBoxX = rightColX + raceBoxWidth + GAP;

        renderBox(guiGraphics, rightColX, row2Y, raceBoxWidth, ROW_HEIGHT);
        renderBox(guiGraphics, classBoxX, row2Y, classBoxWidth, ROW_HEIGHT);

        races.Race race = player != null ? races.getPlayerRace(player) : null;
        PlayerClasses.PlayerClass playerClass = player != null ? PlayerClasses.getPlayerClass(player) : null;

        String raceText = (race != null) ? race.getDisplayName() : "No Race";
        String classText = (playerClass != null) ? playerClass.getDisplayName() : "No Class";

        drawCenteredInBox(guiGraphics, raceText, rightColX, row2Y, raceBoxWidth, ROW_HEIGHT);
        drawCenteredInBox(guiGraphics, classText, classBoxX, row2Y, classBoxWidth, ROW_HEIGHT);
        int buffsY = row2Y + ROW_HEIGHT + GAP + BUFFS_EXTRA_GAP;
        int rightColBottom = contentTop + contentHeight + BOX_DOWN_SHIFT;
        int buffsHeight = rightColBottom - buffsY;
        renderBox(guiGraphics, rightColX, buffsY, rightColWidth, buffsHeight);

        List<BuffEntry> allBuffs = getCombinedBuffs(race, playerClass, ClientPlayerLevelData.getLevel());
        renderScrollableBuffs(guiGraphics, allBuffs, rightColX, buffsY, rightColWidth, buffsHeight, mouseX, mouseY);
        int xpBarY = panelTop + PANEL_HEIGHT - PADDING - (XP_AREA_HEIGHT - 4);
        int xpBarX = modelBoxX;
        int xpBarWidth = (rightColX + rightColWidth) - modelBoxX;
        renderXpBar(guiGraphics, xpBarX, xpBarY, xpBarWidth, 14);

        guiGraphics.pose().popPose();
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderBox(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x60000000);
        guiGraphics.fillGradient(x, y, x + width, y + height, BOX_FILL_TOP, BOX_FILL_BOTTOM);

        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, BOX_BORDER);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, BOX_BORDER);
        guiGraphics.fill(x - 1, y, x, y + height, BOX_BORDER);
        guiGraphics.fill(x + width, y, x + width + 1, y + height, BOX_BORDER);

        guiGraphics.fill(x, y, x + width, y + 1, 0x33FFFFFF);

        drawCorner(guiGraphics, x - 1, y - 1, 1, 1);
        drawCorner(guiGraphics, x + width, y - 1, -1, 1);
        drawCorner(guiGraphics, x - 1, y + height, 1, -1);
        drawCorner(guiGraphics, x + width, y + height, -1, -1);
    }

    private void drawCorner(GuiGraphics guiGraphics, int x, int y, int dx, int dy) {
        int len = 4;
        if (dx > 0) guiGraphics.fill(x, y, x + len, y + 1, BOX_BORDER_BRIGHT);
        else guiGraphics.fill(x - len + 1, y, x + 1, y + 1, BOX_BORDER_BRIGHT);
        if (dy > 0) guiGraphics.fill(x, y, x + 1, y + len, BOX_BORDER_BRIGHT);
        else guiGraphics.fill(x, y - len + 1, x + 1, y + 1, BOX_BORDER_BRIGHT);
    }

    private void drawCenteredInBox(GuiGraphics guiGraphics, String text, int boxX, int boxY, int boxWidth, int boxHeight) {
        float scale = 0.65f;
        int textWidth = font.width(text);

        float centerX = boxX + (boxWidth / 2.0f);
        float centerY = boxY + (boxHeight / 2.0f) - ((font.lineHeight * scale) / 2.0f) + 0.5f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);

        int scaledX = Math.round((centerX / scale) - (textWidth / 2.0f));
        int scaledY = Math.round(centerY / scale);

        guiGraphics.drawString(this.font, Component.literal(text), scaledX, scaledY, TEXT_LIGHT, false);
        guiGraphics.pose().popPose();
    }

    private void renderScrollableBuffs(GuiGraphics guiGraphics, List<BuffEntry> buffs, int x, int y, int width, int height, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.75f, 0.75f, 0.75f);
        int scaledX = (int) (x / 0.75f);
        int scaledY = (int) ((y + 5) / 0.75f);
        int scaledWidth = (int) (width / 0.75f);
        String title = "CURRENT EFFECTS";
        int titleWidth = font.width(title);
        guiGraphics.drawString(this.font, Component.literal(title),
                scaledX + scaledWidth / 2 - titleWidth / 2, scaledY, ACCENT, false);
        guiGraphics.pose().popPose();

        guiGraphics.fill(x + 6, y + 15, x + width - 6, y + 16, 0x66B388E8);

        if (buffs.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.65f, 0.65f, 0.65f);
            String none = "None";
            int noneWidth = font.width(none);
            int scaledCenterX = (int) ((x + width / 2) / 0.65f);
            int scaledCenterY = (int) ((y + height / 2) / 0.65f);
            guiGraphics.drawString(this.font, Component.literal(none),
                    scaledCenterX - noneWidth / 2, scaledCenterY, TEXT_LABEL, false);
            guiGraphics.pose().popPose();
            return;
        }

        int contentTop = y + 22;
        int innerPaddingBottom = 2;
        int visibleHeight = height - (contentTop - y) - innerPaddingBottom;

        float textScale = 0.65f;
        int bulletWidth = font.width("\u2726");
        int scrollbarReserve = 8;
        int maxTextWidth = Math.max(10, (int) ((width - 6 - scrollbarReserve) / textScale) - bulletWidth);
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        List<Integer> lineBulletColors = new ArrayList<>();
        for (BuffEntry entry : buffs) {
            List<FormattedCharSequence> split = font.split(Component.literal(entry.text()), maxTextWidth);
            int color = entry.fromClass() ? BUFF_CLASS_COLOR : BUFF_RACE_COLOR;
            for (int i = 0; i < split.size(); i++) {
                wrappedLines.add(split.get(i));
                lineBulletColors.add(i == 0 ? color : null);
            }
        }

        int continuationRowHeight = BUFF_ROW_HEIGHT / 2;
        int totalBuffHeight = 0;
        for (Integer color : lineBulletColors) {
            totalBuffHeight += (color != null) ? BUFF_ROW_HEIGHT : continuationRowHeight;
        }

        int maxScroll = Math.max(0, totalBuffHeight - visibleHeight);
        if (buffScrollOffset > maxScroll) buffScrollOffset = maxScroll;
        if (buffScrollOffset < 0) buffScrollOffset = 0;

        guiGraphics.enableScissor(x + 1, contentTop, x + width - 1, contentTop + visibleHeight);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, textScale);
        int buffX = (int) ((x + 6) / textScale);
        int rowStepFull = (int) (BUFF_ROW_HEIGHT / textScale);
        int rowStepHalf = (int) (continuationRowHeight / textScale);

        int lineY = (int) ((contentTop + 2 - buffScrollOffset) / textScale);
        for (int i = 0; i < wrappedLines.size(); i++) {
            if (i > 0) {
                lineY += (lineBulletColors.get(i) != null) ? rowStepFull : rowStepHalf;
            }
            Integer bulletColor = lineBulletColors.get(i);
            if (bulletColor != null) {
                guiGraphics.drawString(this.font, Component.literal("\u2726"), buffX, lineY, bulletColor, false);
                guiGraphics.drawString(this.font, wrappedLines.get(i),
                        buffX + bulletWidth + font.width(" "), lineY, TEXT_LIGHT, false);
            } else {
                guiGraphics.drawString(this.font, wrappedLines.get(i),
                        buffX + bulletWidth + font.width(" "), lineY, TEXT_LIGHT, false);
            }
        }
        guiGraphics.pose().popPose();

        guiGraphics.disableScissor();

        if (totalBuffHeight > visibleHeight) {
            int trackWidth = 3;
            int trackX = x + width - trackWidth - 3;

            float scrollRatio = (float) visibleHeight / totalBuffHeight;
            int scrollBarHeight = Math.max(6, (int) (visibleHeight * scrollRatio));

            int scrollBarY = contentTop + (int) ((visibleHeight - scrollBarHeight) * (buffScrollOffset / (float) maxScroll));

            guiGraphics.fill(trackX, contentTop, trackX + trackWidth, contentTop + visibleHeight, 0x33000000);
            guiGraphics.fill(trackX, scrollBarY, trackX + trackWidth, scrollBarY + scrollBarHeight, 0xE0B388E8);
        }
    }

    private void drawModelFloor(GuiGraphics guiGraphics, int boxX, int boxY, int boxWidth, int boxHeight) {
        int floorY = boxY + boxHeight - 12;
        int centerX = boxX + boxWidth / 2;
        int ovalWidth = boxWidth - 18;
        int halfW = ovalWidth / 2;

        int[] widths = {halfW, (int) (halfW * 0.75f), (int) (halfW * 0.45f)};
        int[] alphas = {0x30, 0x40, 0x55};
        for (int i = 0; i < widths.length; i++) {
            int w = widths[i];
            int a = alphas[i] << 24;
            guiGraphics.fill(centerX - w, floorY - 1 + i, centerX + w, floorY + i, a | 0x2A0F38);
        }
    }

    private void renderXpBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int currentXp = ClientPlayerLevelData.getXp();
        int xpNeeded = Math.max(1, ClientPlayerLevelData.getXpToNextLevel());
        float ratio = Math.min(1.0f, currentXp / (float) xpNeeded);

        guiGraphics.fill(x + 1, y + 2, x + width + 1, y + height + 2, 0x40000000);

        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, BOX_BORDER);
        guiGraphics.fillGradient(x, y, x + width, y + height, 0xFF120819, 0xFF1D0A28);

        int filledWidth = (int) (width * ratio);
        if (filledWidth > 0) {
            guiGraphics.fillGradient(x, y, x + filledWidth, y + height, 0xFF9C4FD1, ACCENT_DEEP);
            guiGraphics.fill(x, y, x + filledWidth, y + 1, 0x66FFFFFF);
            guiGraphics.fill(x + filledWidth - 1, y, x + filledWidth, y + height, 0xFFE3B8FF);
        }

        float textScale = 0.85f;
        int labelY = y - (int) (font.lineHeight * textScale) - 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, textScale);

        int scaledX = (int) (x / textScale);
        int scaledWidth = (int) (width / textScale);
        int scaledLabelY = (int) (labelY / textScale);
        boolean nearStart = ratio <= 0.15f;
        boolean nearEnd = ratio >= 0.85f;

        if (!nearStart) {
            guiGraphics.drawString(this.font, Component.literal("0"), scaledX, scaledLabelY, TEXT_LIGHT, false);
        }

        String maxLabel = String.valueOf(xpNeeded);
        if (!nearEnd) {
            guiGraphics.drawString(this.font, Component.literal(maxLabel),
                    scaledX + scaledWidth - font.width(maxLabel), scaledLabelY, TEXT_LIGHT, false);
        }
        String currentLabel = String.valueOf(currentXp);
        int currentLabelWidth = font.width(currentLabel);

        int scaledFilledEdge = (int) (filledWidth / textScale);
        int markerX = scaledX + scaledFilledEdge - currentLabelWidth / 2;

        int minX = scaledX;
        int maxX = scaledX + scaledWidth - currentLabelWidth;
        markerX = Math.max(minX, Math.min(maxX, markerX));

        guiGraphics.drawString(this.font, Component.literal(currentLabel), markerX, scaledLabelY, TEXT_LIGHT, false);

        guiGraphics.pose().popPose();
    }

    private List<BuffEntry> getCombinedBuffs(races.Race race, PlayerClasses.PlayerClass playerClass, int level) {
        List<BuffEntry> buffs = new ArrayList<>();
        if (race != null) for (String s : getRaceBuffs(race, level)) buffs.add(new BuffEntry(s, false));
        if (playerClass != null) for (String s : getClassBuffs(playerClass, level)) buffs.add(new BuffEntry(s, true));
        return buffs;
    }

    private List<String> getRaceBuffs(races.Race race, int level) {
        List<String> buffs = new ArrayList<>();
        float baseHealth = races.getHealthBonus(race);
        float levelHealth = RaceLevelScaling.levelHealthBonus(race, level);
        float totalHealth = baseHealth + levelHealth;
        if (totalHealth != 0f) {
            String sign = totalHealth > 0 ? "+" : "";
            buffs.add(sign + formatFloat(totalHealth) + " max health");
        }
        float armorBonus = RaceLevelScaling.levelArmorBonus(race, level);
        if (armorBonus > 0f) {
            buffs.add("+" + formatFloat(armorBonus) + " armor");

            float toughnessBonus = RaceLevelScaling.levelToughnessBonus(race, level);
            if (toughnessBonus > 0f) {
                buffs.add("+" + formatFloat(toughnessBonus) + " armor toughness");
            }
        }

        float attackBonus = RaceLevelScaling.levelAttackDamageBonus(race, level);
        if (attackBonus > 0f) {
            buffs.add("+" + formatFloat(attackBonus) + " attack damage");
        }
        List<String> raceEffectLines = new ArrayList<>();
        switch (race) {
            case SCULK -> {
                raceEffectLines.add("Takes reduced damage");
                raceEffectLines.add("Ability: Open a death-proof storage chest anytime");
            }
            case WARDER -> {
                raceEffectLines.add("Deals bonus melee damage");
                raceEffectLines.add("Ability: Unleash a damaging ring that breaks nearby blocks");
            }
            case ENDER -> {
                raceEffectLines.add("Sees clearly in the dark");
                raceEffectLines.add("Ability: Teleport to where you're looking");
            }
            case PHANTOM -> {
                raceEffectLines.add("Jumps higher");
                raceEffectLines.add("Ability: Launch upward and glide");
            }
            case LOVER -> {
                raceEffectLines.add("Villagers treat you better");
                raceEffectLines.add("Ability: Redirect incoming damage to a nearby enemy");
            }
            case BELIEVER -> {
                raceEffectLines.add("Better loot & fishing luck");
                raceEffectLines.add("Ability: Raise a safe barrier that shields you inside");
            }
            case ANGELBORN -> {
                raceEffectLines.add("Slowly regenerates health");
                raceEffectLines.add("Ability: Spawn portals that damage nearby hostile mobs");
            }
            case VAMPIREBORN -> {
                raceEffectLines.add("Heal when you deal damage");
                raceEffectLines.add("Burns in direct sunlight");
                raceEffectLines.add("Ability: Charge and fire a blood projectile, stronger the longer you hold");
            }
            case ETHEREAL -> {
                raceEffectLines.add("Your skin reflects damage back at attackers");
                raceEffectLines.add("Ability: Turn ethereal and phase through blocks");
            }
            case CELESTIAL -> {
                raceEffectLines.add("Normal foes can't see you");
                raceEffectLines.add("Ability: Create a gravity field, then push or pull everything in it");
            }
            case GATEKEEPER -> {
                raceEffectLines.add("Hunger never depletes");
                raceEffectLines.add("Ability: Summon portals that fire weapons from your inventory");
            }
        }
        if (RaceLevelScaling.hasReachedLevel25(level)) {
            int insertIndex = Math.min(1, raceEffectLines.size());
            raceEffectLines.add(insertIndex, getLevel25BuffDescription(race));
        }

        buffs.addAll(raceEffectLines);

        return buffs;
    }

    private String getLevel25BuffDescription(races.Race race) {
        return switch (race) {
            case SCULK -> "Move noticeably faster";
            case WARDER -> "Immune to fire and lava";
            case ENDER -> "Move noticeably faster";
            case PHANTOM -> "Move noticeably faster";
            case LOVER -> "Gains extra absorption hearts";
            case BELIEVER -> "Regenerate health over time";
            case ANGELBORN -> "Villagers treat you better";
            case VAMPIREBORN -> "Regenerate health over time";
            case ETHEREAL -> "Move noticeably faster";
            case CELESTIAL -> "Deal bonus damage";
            case GATEKEEPER -> "Resist more incoming damage";
        };
    }

    private static String formatFloat(float value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }
        return String.format("%.1f", value);
    }

    private static final int SPECIAL_WEAPON_LEVEL = 30;

    private List<String> getClassBuffs(PlayerClasses.PlayerClass playerClass, int level) {
        List<String> buffs = new ArrayList<>();
        switch (playerClass) {
            case ASSASSIN -> {
                buffs.add("Bonus damage attacking from behind");
                buffs.add("Invisible while sneaking with a dagger");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Obsidia");
            }
            case FENCER -> {
                buffs.add("Rapier lunge dash attack");
                buffs.add("Bonus damage on riposte after a lunge");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Amethyst Rapier");
            }
            case GUNSMITH -> {
                buffs.add("Can modify bullets with enhancements");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Lament");
            }
            case SPELLBLADE -> {
                buffs.add("Bonus magic damage on hit");
                buffs.add("Elemental effect on hit");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield an elemental Spellblade");
            }
            case REAPER -> {
                buffs.add("Bonus damage + lifesteal vs low-health foes");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Soul Scythe");
            }
            case SPEARMAN -> {
                buffs.add("Hits pierce through to enemies behind");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Fire Spear");
            }
            case HEAVY_KNIGHT -> {
                buffs.add("Chained hits deal increasing damage");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Viridyum Greatsword");
            }
            case VIKING -> {
                buffs.add("Bonus axe damage below half health");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Boront");
            }
            case GUARDIAN -> {
                buffs.add("Reflects damage when blocking");
                buffs.add("Shield never gets disabled");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Gruck");
            }
            case SWORDSMAN -> {
                buffs.add("Bonus damage with swords");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Roca");
            }
            case ARCHER -> {
                buffs.add("Bonus arrow damage");
                buffs.add("Extra damage on fully-drawn crit shots");
                if (level >= SPECIAL_WEAPON_LEVEL) buffs.add("You are able to wield Shi Bow");
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        buffScrollOffset -= (int) (scrollY * 10);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyBindHandler.OPEN_PLAYER_DATA_KEY.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}