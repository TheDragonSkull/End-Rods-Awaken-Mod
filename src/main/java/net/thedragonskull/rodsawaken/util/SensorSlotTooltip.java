package net.thedragonskull.rodsawaken.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.awt.*;

public class SensorSlotTooltip implements TooltipComponent, ClientTooltipComponent {

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        pGuiGraphics.renderFakeItem(new ItemStack(Items.SCULK_SENSOR), pX, pY - 13);
        pGuiGraphics.drawString(pFont, "→ Player detection (only)", pX + 20, pY - 8, 0xFFFFFF);

        pGuiGraphics.renderFakeItem(new ItemStack(Items.CALIBRATED_SCULK_SENSOR), pX, pY + 25);
        pGuiGraphics.drawString(pFont, "→ Entity detection (any)", pX + 20, pY + (25 + 8), 0xFFFFFF);
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public int getWidth(Font pFont) {
        return 150;
    }
}
