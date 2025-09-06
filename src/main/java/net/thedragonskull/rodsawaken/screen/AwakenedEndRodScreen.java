package net.thedragonskull.rodsawaken.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.SlotItemHandler;
import net.thedragonskull.rodsawaken.RodsAwaken;
import net.thedragonskull.rodsawaken.network.ClearPotionSlotPacket;
import net.thedragonskull.rodsawaken.network.PacketHandler;
import net.thedragonskull.rodsawaken.util.SensorSlotTooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

                // Effect Icon Draw
                List<MobEffectInstance> effects = be.getPotionEffects(i);

                if (effects != null && !effects.isEmpty() && minecraft != null) {
                    MobEffectInstance effectInstance;

                    if (effects.size() == 1) {
                        effectInstance = effects.get(0);
                    } else {
                        int index = (int)((System.currentTimeMillis() / 1000) % effects.size());
                        effectInstance = effects.get(index);
                    }

                    MobEffect mobEffect = effectInstance.getEffect();
                    TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(mobEffect);

                    int atlasSize = 128;

                    int u0 = (int) (sprite.getU0() * atlasSize);
                    int v0 = (int) (sprite.getV0() * atlasSize);
                    int u1 = (int) (sprite.getU1() * atlasSize);
                    int v1 = (int) (sprite.getV1() * atlasSize);

                    guiGraphics.blit(
                            sprite.atlasLocation(),
                            x + barX[i], y + 19,
                            18, 18,
                            u0, v0,
                            u1 - u0,
                            v1 - v0,
                            atlasSize,
                            atlasSize
                    );

                }

            }

            // --- Clear Buttons ---
            int baseX = x + 34;
            int baseY = y + 62;
            int separation = 23 + 11;

            int btnX = baseX + (i * separation);
            int btnY = baseY;

            boolean hovered = mouseX >= btnX && mouseX <= btnX + 10 &&
                    mouseY >= btnY && mouseY <= btnY + 10;

            boolean hasEffect = be.hasEffectInSlot(i);

            int texX;
            if (hasEffect) {
                texX = 0;
                if (hovered) {
                    texX = 22;
                }
            } else {
                texX = 11;
            }

            int texY = 166;
            guiGraphics.blit(TEXTURE, btnX, btnY, texX, texY, 11, 11);

            // Tooltip
            if (hovered) {
                guiGraphics.renderTooltip(
                        this.font,
                        Component.literal("Clear Effect"),
                        mouseX, mouseY
                );
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int baseX = (this.width - this.imageWidth) / 2 + 34;
            int baseY = (this.height - this.imageHeight) / 2 + 62;
            int separation = 23 + 11;

            for (int i = 0; i < 3; i++) {
                int btnX = baseX + (i * separation);
                int btnY = baseY;

                if (mouseX >= btnX && mouseX <= btnX + 11 &&
                        mouseY >= btnY && mouseY <= btnY + 11) {

                    if (menu.getBlockEntity().hasEffectInSlot(i)) {
                        onPotionButtonClicked(i);

                        menu.getBlockEntity().clearPotionSlot(i);
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void onPotionButtonClicked(int slot) {
        BlockPos pos = this.menu.getBlockEntity().getBlockPos();
        PacketHandler.sendToServer(new ClearPotionSlotPacket(slot, pos));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        var be = this.menu.getBlockEntity();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int MAX_BAR_WIDTH = 19;
        int BAR_HEIGHT = 5;

        int[] barX = {29, 63, 97};
        int barY = 9;

        for (int i = 0; i < 3; i++) {
            float progress = be.getPotionProgress(i);

            if (progress > 0f) {
                // --- Timer bar hover ---
                int barStartX = x + barX[i];
                int barStartY = y + barY;
                int barEndX = barStartX + MAX_BAR_WIDTH;
                int barEndY = barStartY + BAR_HEIGHT;

                if (mouseX >= barStartX && mouseX <= barEndX &&
                        mouseY >= barStartY && mouseY <= barEndY) {

                    int ticksLeft = be.getPotionTimeLeft(i);

                    if (ticksLeft > 0) {
                        String formatted = formatTime(ticksLeft);
                        Component tooltip = Component.literal(formatted);
                        guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
                    }
                }

                // --- Effect Icon hover ---
                int iconX = x + barX[i];
                int iconY = y + 18;
                int iconEndX = iconX + 19;
                int iconEndY = iconY + 19;

                if (mouseX >= iconX && mouseX <= iconEndX &&
                        mouseY >= iconY && mouseY <= iconEndY) {

                    List<MobEffectInstance> effects = be.getPotionEffects(i);

                    if (!effects.isEmpty()) {
                        List<Component> tooltips = new ArrayList<>();
                        for (MobEffectInstance effect : effects) {
                            MutableComponent name = Component.translatable(effect.getDescriptionId());

                            int amp = effect.getAmplifier();
                            if (amp > 0) {
                                name = name.append(" ").append(Component.translatable("potion.potency." + amp));
                            }

                            tooltips.add(name);
                        }
                        guiGraphics.renderComponentTooltip(this.font, tooltips, mouseX, mouseY);
                    }
                }
            }
        }
    }

    private String formatTime(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        if (this.hoveredSlot instanceof SlotItemHandler slotHandler &&
                slotHandler.getItemHandler() == this.menu.getBlockEntity().getItems()) {

            int blockSlotIndex = slotHandler.getSlotIndex();
            if (blockSlotIndex >= 0 && blockSlotIndex <= 2) {
                ItemStack carried = this.menu.getCarried();

                if (!carried.isEmpty() && carried.is(Items.POTION)
                        && !this.menu.getBlockEntity().getPotionEffects(blockSlotIndex).isEmpty()) {

                    guiGraphics.renderTooltip(
                            this.font,
                            List.of(Component.literal("Clear the current effect first!").withStyle(ChatFormatting.RED)),
                            Optional.empty(),
                            mouseX,
                            mouseY
                    );
                    return;
                }
            }

            if (blockSlotIndex == 3) {
                if (Screen.hasShiftDown()) {

                    guiGraphics.renderTooltip(
                            this.font,
                            List.of(Component.literal(" ")),
                            Optional.of(new SensorSlotTooltip()),
                            mouseX,
                            mouseY
                    );

                } else {
                    guiGraphics.renderTooltip(
                            this.font,
                            List.of(
                                    Component.literal("Sculk Sensor Slot").withStyle(ChatFormatting.GRAY),
                                    Component.literal("Press Shift for more info").withStyle(ChatFormatting.YELLOW)
                            ),
                            Optional.empty(),
                            mouseX,
                            mouseY
                    );
                }
            }
        }
    }


}
