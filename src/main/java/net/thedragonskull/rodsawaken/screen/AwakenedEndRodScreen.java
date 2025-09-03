package net.thedragonskull.rodsawaken.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.thedragonskull.rodsawaken.RodsAwaken;

public class AwakenedEndRodScreen extends AbstractContainerScreen<AwakenedEndRodMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RodsAwaken.MOD_ID, "textures/gui/awakened_end_rod_screen.png");

    public AwakenedEndRodScreen(AwakenedEndRodMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = 100000;
        this.inventoryLabelY = 100000;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // BG
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        var be = this.menu.getBlockEntity();

        int MAX_BAR_WIDTH = 18;
        int BAR_HEIGHT = 4;

        int[] barX = {30, 64, 98};
        int barY = 10;

        for (int i = 0; i < 3; i++) {
            float progress = be.getPotionProgress(i);
            int width = (int)(progress * MAX_BAR_WIDTH);

            if (width > 0) {
                int color = be.getPotionColor(i) | 0xFF000000;

                guiGraphics.fill(
                        x + barX[i],
                        y + barY,
                        x + barX[i] + width,
                        y + barY + BAR_HEIGHT,
                        color
                );
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
