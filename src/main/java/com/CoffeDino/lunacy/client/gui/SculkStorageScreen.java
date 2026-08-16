package com.CoffeDino.lunacy.client.gui;

import com.CoffeDino.lunacy.menu.SculkStorageMenu;
import com.CoffeDino.lunacy.menu.SculkStorageSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
public class SculkStorageScreen extends AbstractContainerScreen<SculkStorageMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int TEXTURE_WIDTH = 176;

    private static final int VISIBLE_ROWS = SculkStorageMenu.VISIBLE_ROWS;
    private static final int SLOT_SIZE = 18;
    private static final int STORAGE_LEFT = 8;
    private static final int STORAGE_TOP = 18;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_MARGIN = 10;
    private static final int SCROLLBAR_HEIGHT = VISIBLE_ROWS * SLOT_SIZE;

    private int scrollRows = -1;
    private boolean draggingScrollbar = false;

    public SculkStorageScreen(SculkStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = TEXTURE_WIDTH + SCROLLBAR_MARGIN + SCROLLBAR_WIDTH;
        this.imageHeight = 114 + VISIBLE_ROWS * SLOT_SIZE;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 8;
    }

    private int getMaxScroll() {
        return Math.max(0, menu.getRowCount() - VISIBLE_ROWS);
    }

    @Override
    protected void init() {
        super.init();
        remapVisibleSlots(0);
    }

    private void remapVisibleSlots(int newScrollRows) {
        if (newScrollRows == scrollRows) return;
        scrollRows = newScrollRows;

        int totalStorageSlots = menu.getRowCount() * 9;
        for (int viewportRow = 0; viewportRow < VISIBLE_ROWS; viewportRow++) {
            for (int col = 0; col < 9; col++) {
                int viewportSlotListIndex = col + viewportRow * 9;
                Slot slot = menu.slots.get(viewportSlotListIndex);
                if (!(slot instanceof SculkStorageSlot sculkSlot)) continue;

                int absoluteRow = viewportRow + scrollRows;
                int absoluteIndex = col + absoluteRow * 9;
                sculkSlot.setSlotIndex(absoluteIndex < totalStorageSlots ? absoluteIndex : -1);
            }
        }
    }

    private void setScroll(int newScrollRows) {
        int maxScroll = getMaxScroll();
        int clamped = Math.max(0, Math.min(maxScroll, newScrollRows));
        if (clamped == scrollRows) return;

        remapVisibleSlots(clamped);
        com.CoffeDino.lunacy.network.NetworkHandler.scrollSculkStorage(clamped);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int topHeight = VISIBLE_ROWS * SLOT_SIZE + 17;
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, TEXTURE_WIDTH, topHeight);
        guiGraphics.blit(TEXTURE, leftPos, topPos + topHeight, 0, 126, TEXTURE_WIDTH, 96);

        int panelX = leftPos + TEXTURE_WIDTH;
        guiGraphics.fill(panelX, topPos + STORAGE_TOP - 1, panelX + SCROLLBAR_MARGIN + SCROLLBAR_WIDTH,
                topPos + STORAGE_TOP + SCROLLBAR_HEIGHT + 1, 0xC0101010);

        renderScrollbar(guiGraphics);
    }

    private int scrollbarTrackX() {
        return leftPos + TEXTURE_WIDTH + (SCROLLBAR_MARGIN - 4);
    }

    private int scrollbarTrackY() {
        return topPos + STORAGE_TOP;
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        int trackX = scrollbarTrackX();
        int trackY = scrollbarTrackY();

        guiGraphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + SCROLLBAR_HEIGHT, 0xFF373737);

        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) return;

        float handleHeightRatio = (float) VISIBLE_ROWS / menu.getRowCount();
        int handleHeight = Math.max(12, Math.round(SCROLLBAR_HEIGHT * handleHeightRatio));
        int scrollRange = SCROLLBAR_HEIGHT - handleHeight;
        int handleY = trackY + Math.round(scrollRange * (scrollRows / (float) maxScroll));

        guiGraphics.fill(trackX, handleY, trackX + SCROLLBAR_WIDTH, handleY + handleHeight, 0xFFAFAFAF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (getMaxScroll() > 0) {
            setScroll(scrollRows - (int) Math.signum(scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int trackX = scrollbarTrackX();
        int trackY = scrollbarTrackY();
        return mouseX >= trackX && mouseX <= trackX + SCROLLBAR_WIDTH
                && mouseY >= trackY && mouseY <= trackY + SCROLLBAR_HEIGHT;
    }

    private void updateScrollFromMouse(double mouseY) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) return;
        int trackY = scrollbarTrackY();
        float ratio = (float) (mouseY - trackY) / SCROLLBAR_HEIGHT;
        setScroll(Math.round(ratio * maxScroll));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
    }
}