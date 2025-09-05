package net.thedragonskull.rodsawaken.util;

import com.ibm.icu.impl.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;


public class SensorSlotTooltip implements TooltipComponent, ClientTooltipComponent {

    List<Pair<ItemStack, String>> entries = List.of(
            Pair.of(new ItemStack(Items.SCULK_SENSOR), "→ Player detection (only)"),
            Pair.of(new ItemStack(Items.CALIBRATED_SCULK_SENSOR), "→ Entity detection (any)")
    );

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        String title = "Enable AUTO Mode";
        int titleWidth = pFont.width(title);
        int centerX = pX + (getWidth(pFont) - titleWidth) / 2;
        pGuiGraphics.drawString(pFont, title, centerX, pY - 8, 0x00cbd9);

        int lineHeight = 20;
        int startY = pY + 5;

        for (int i = 0; i < entries.size(); i++) {
            int lineY = startY + i * lineHeight;

            ItemStack icon = entries.get(i).first;
            String text = entries.get(i).second;

            pGuiGraphics.renderFakeItem(icon, pX, lineY);
            pGuiGraphics.drawString(pFont, text, pX + 20, lineY + 5, 0xFFFFFF);
        }
    }

    @Override
    public int getHeight() {
        int lineHeight = 20;
        int titleHeight = 10;
        return titleHeight + (entries.size() * lineHeight);
    }

    @Override
    public int getWidth(Font pFont) {
        int maxWidth = pFont.width("Enable AUTO Mode");
        for (var entry : entries) {
            int w = 20 + pFont.width(entry.second);
            if (w > maxWidth) maxWidth = w;
        }
        return maxWidth;
    }
}
