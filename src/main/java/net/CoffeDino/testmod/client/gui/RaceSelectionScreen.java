package net.CoffeDino.testmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.network.NetworkHandler;
import net.CoffeDino.testmod.network.RaceSelectionPacket;
import net.CoffeDino.testmod.races.races;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.PostChain;


import java.util.List;

public class RaceSelectionScreen extends Screen {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "textures/gui/race_selection_bg2.png");
    private int currentRaceIndex = 0;
    private List<races.Race> raceList;
    private Button selectButton;
    private Button leftArrow;
    private Button rightArrow;

    public RaceSelectionScreen(){
        super(Component.literal("Choose your Race!"));
        this.raceList = List.of(
                races.Race.SCULK,
                races.Race.WARDER,
                races.Race.ENDER,
                races.Race.PHANTOM
        );
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int uiCardWidth = 200;
        int uiCardHeight = 220;

        int cardLeft = centerX - uiCardWidth / 2;
        int cardRight = centerX + uiCardWidth / 2;
        int cardTop = centerY - uiCardHeight / 2;
        int cardBottom = centerY + uiCardHeight / 2;

        leftArrow = Button.builder(Component.literal("<"), b -> switchRace(-1))
                .bounds(cardLeft -30, centerY - 110, 30, 20)  // 40 pixels left of card
                .build();
        addRenderableWidget(leftArrow);

        rightArrow = Button.builder(Component.literal(">"), b -> switchRace(1))
                .bounds(cardRight , centerY - 110, 30, 20)  // 10 pixels right of card
                .build();
        addRenderableWidget(rightArrow);

        selectButton = Button.builder(Component.literal("Select"), b -> selectRace())
                .bounds(centerX - 95, cardBottom -30, 190, 20)  // 20 pixels below card
                .build();
        addRenderableWidget(selectButton);
    }

    private void switchRace(int direction){
        currentRaceIndex = (currentRaceIndex + direction + raceList.size())% raceList.size();
    }
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        int centerX = this.width/2;
        int centerY = this.height/2;
        int cardWidth = 200;
        int cardHeight = 220;
        guiGraphics.blit(BACKGROUND, centerX - cardWidth /2, centerY - cardHeight/2,
                0, 0, cardWidth, cardHeight, cardWidth, cardHeight);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){

        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        races.Race current = raceList.get(currentRaceIndex);
        guiGraphics.pose().pushPose(); // PUSH
        guiGraphics.pose().translate(0, 0, 100);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY - 95, 0); // Move higher up (from -85 to -110)
        guiGraphics.pose().scale(1.5f, 1.5f, 1.5f); // Make text 50% bigger (1.5x scale)
        guiGraphics.drawCenteredString(this.font, Component.literal(current.getDisplayName()), 0, 0, 0xFFFFFF);
        guiGraphics.pose().popPose();

        int descX = centerX - 7;
        int descY = centerY - 60;
        int descWidth = 100;
        int descHeight = 120;

        guiGraphics.fill(descX, descY, descX + descWidth, descY + descHeight, 0xAA000000);
        guiGraphics.fill(descX - 1, descY - 1, descX + descWidth + 1, descY, 0xFFFFFFFF);
        guiGraphics.fill(descX - 1, descY + descHeight, descX + descWidth + 1, descY + descHeight + 1, 0xFFFFFFFF);
        guiGraphics.fill(descX - 1, descY, descX, descY + descHeight, 0xFFFFFFFF);
        guiGraphics.fill(descX + descWidth, descY, descX + descWidth + 1, descY + descHeight, 0xFFFFFFFF);

        String description = getRaceDescription(current);
        guiGraphics.drawWordWrap(this.font, Component.literal(description),
                descX + 6, descY + 6, descWidth - 10, 0xFFFFFF);
        drawEntity(guiGraphics, centerX - 60, centerY + 50, 55);
        guiGraphics.pose().popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

    }

    private String getRaceDescription(races.Race race) {
        switch (race) {
            case SCULK: return "Connected to the deep end, masters of space and stealth.";
            case WARDER: return "Ancient guardians, strong and resilient protectors.";
            case ENDER: return "Masters of teleportation and the end dimension.";
            case PHANTOM: return "Spectral beings, elusive and mysterious.";
            default: return "A mysterious race with unknown abilities.";
        }
    }


    private void drawEntity(GuiGraphics graphics, int x, int y, int scale) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        double mouseX = Minecraft.getInstance().mouseHandler.xpos();
        double screenWidth = Minecraft.getInstance().getWindow().getScreenWidth();

        float rotation = 90.0f - (float)(mouseX / screenWidth) * 180.0f;

        RenderSystem.enableDepthTest();
        graphics.pose().pushPose();

        graphics.pose().translate(0, 0, 100);
        graphics.pose().translate(x, y, 50);
        graphics.pose().scale((float) scale, (float) scale, (float) scale);
        graphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
        graphics.pose().mulPose(Axis.YP.rotationDegrees(rotation + 180.0f));

        dispatcher.overrideCameraOrientation(Axis.YP.rotationDegrees(180.0F));
        dispatcher.setRenderShadow(false);

        graphics.flush();
        RenderSystem.runAsFancy(() -> {
            dispatcher.render(player, 0, 0, 0, 0F, 1F, graphics.pose(), graphics.bufferSource(), 15728880);
        });
        graphics.bufferSource().endBatch();

        dispatcher.setRenderShadow(true);
        graphics.pose().popPose();
        RenderSystem.disableDepthTest();
    }


    private void selectRace(){
        System.out.println("DEBUG: Select button pressed!");

        races.Race race = raceList.get(currentRaceIndex);
        System.out.println("DEBUG: Selected race: " + race.getId() + " - " + race.getDisplayName());

        // Check if network handler is available
        System.out.println("DEBUG: Sending packet to server...");
        NetworkHandler.sendToServer(new RaceSelectionPacket(race.getId()));

        System.out.println("DEBUG: Closing screen");
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean shouldCloseOnEsc(){
        return false;
    }
    @Override
    public boolean isPauseScreen(){
        return false;
    }
}
