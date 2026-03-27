package com.agescrafting.agescrafting.barrel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class BarrelScreen extends AbstractContainerScreen<BarrelMenu> {
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("agescrafting", "gui/barrel.png");
    private static final int ATLAS_WIDTH = 256;
    private static final int ATLAS_HEIGHT = 256;

    private static final int PANEL_U = 0;
    private static final int PANEL_V = 0;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 178;

    private static final int SEAL_ICON_U = 200;
    private static final int SEAL_ICON_V = 1;
    private static final int UNSEAL_ICON_U = 200;
    private static final int UNSEAL_ICON_V = 21;
    private static final int ICON_W = 18;
    private static final int ICON_H = 19;

    private static final int INPUT_TANK_X = 37;
    private static final int OUTPUT_TANK_X = 126;
    private static final int TANK_Y = 18;
    private static final int TANK_WIDTH = 14;
    private static final int TANK_HEIGHT = 52;

    private static final int SEAL_BUTTON_X = 14;
    private static final int SEAL_BUTTON_Y = 39;

    private Button sealButton;

    public BarrelScreen(BarrelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_W;
        imageHeight = PANEL_H;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        sealButton = addRenderableWidget(new InvisibleSealButton(leftPos + SEAL_BUTTON_X, topPos + SEAL_BUTTON_Y, ICON_W, ICON_H, button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
        }));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderTankTooltip(guiGraphics, mouseX, mouseY);
        renderSealButtonTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && hasShiftDown() && minecraft != null && minecraft.gameMode != null) {
            if (isMouseOverTank((int) mouseX, (int) mouseY, INPUT_TANK_X, TANK_Y)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                return true;
            }
            if (isMouseOverTank((int) mouseX, (int) mouseY, OUTPUT_TANK_X, TANK_Y)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        blitPanel(guiGraphics, x, y);
        renderTank(guiGraphics, x + INPUT_TANK_X, y + TANK_Y, menu.getInputFluid(), menu.getInputFluidAmount(), menu.getInputTankCapacity());
        renderTank(guiGraphics, x + OUTPUT_TANK_X, y + TANK_Y, menu.getOutputFluid(), menu.getOutputFluidAmount(), menu.getOutputTankCapacity());
        int sealX = x + SEAL_BUTTON_X;
        int sealY = y + SEAL_BUTTON_Y;
        blitSealIcon(guiGraphics, sealX, sealY);
        if (isMouseOverSealButton(mouseX, mouseY)) {
            guiGraphics.fill(sealX + 1, sealY + 1, sealX + ICON_W - 1, sealY + ICON_H - 1, 0x66FFFFFF);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        if (menu.isSealed()) {
            Component sealedText = Component.translatable("gui.agescrafting.barrel.sealed");
            int sealedX = 57 + (54 - font.width(sealedText)) / 2;
            int sealedY = 18 + 54 + 3;
            guiGraphics.drawString(font, sealedText, sealedX, sealedY, 0xC02020, false);
        }
    }

    private void blitPanel(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ATLAS, x, y, PANEL_U, PANEL_V, PANEL_W, PANEL_H, ATLAS_WIDTH, ATLAS_HEIGHT);
    }

    private void blitSealIcon(GuiGraphics guiGraphics, int x, int y) {
        int u = menu.isSealed() ? UNSEAL_ICON_U : SEAL_ICON_U;
        int v = menu.isSealed() ? UNSEAL_ICON_V : SEAL_ICON_V;
        guiGraphics.blit(ATLAS, x, y, u, v, ICON_W, ICON_H, ATLAS_WIDTH, ATLAS_HEIGHT);
    }

    private void renderTank(GuiGraphics guiGraphics, int tankX, int tankY, FluidStack fluid, int amount, int capacity) {
        if (fluid.isEmpty() || amount <= 0) {
            return;
        }

        int safeCapacity = Math.max(1, capacity);
        int filledHeight = Math.max(1, Math.round((amount / (float) safeCapacity) * TANK_HEIGHT));
        int fillTop = tankY + TANK_HEIGHT - filledHeight;

        if (minecraft == null) {
            return;
        }

        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation stillTexture = fluidExtensions.getStillTexture(fluid);
        if (stillTexture == null) {
            int tint = fluidExtensions.getTintColor(fluid);
            int color = 0xFF000000 | (tint & 0x00FFFFFF);
            guiGraphics.fill(tankX, fillTop, tankX + TANK_WIDTH, tankY + TANK_HEIGHT, color);
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int tint = fluidExtensions.getTintColor(fluid);
        float alpha = ((tint >> 24) & 0xFF) / 255.0F;
        if (alpha <= 0.0F) {
            alpha = 1.0F;
        }
        float red = ((tint >> 16) & 0xFF) / 255.0F;
        float green = ((tint >> 8) & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;

        renderTiledFluid(guiGraphics, tankX, fillTop, TANK_WIDTH, filledHeight, sprite, red, green, blue, alpha);
    }

    private void renderTiledFluid(GuiGraphics guiGraphics, int x, int y, int width, int height, TextureAtlasSprite sprite,
                                  float red, float green, float blue, float alpha) {
        guiGraphics.setColor(red, green, blue, alpha);
        for (int xOffset = 0; xOffset < width; xOffset += 16) {
            int tileWidth = Math.min(16, width - xOffset);
            for (int yOffset = 0; yOffset < height; yOffset += 16) {
                int tileHeight = Math.min(16, height - yOffset);
                guiGraphics.blit(x + xOffset, y + yOffset, 0, tileWidth, tileHeight, sprite);
            }
        }
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderTankTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isMouseOverTank(mouseX, mouseY, INPUT_TANK_X, TANK_Y)) {
            renderSingleTankTooltip(guiGraphics, mouseX, mouseY, Component.translatable("gui.agescrafting.barrel.input_tank"),
                    menu.getInputFluid(), menu.getInputFluidAmount(), menu.getInputTankCapacity(), true);
            return;
        }

        if (isMouseOverTank(mouseX, mouseY, OUTPUT_TANK_X, TANK_Y)) {
            renderSingleTankTooltip(guiGraphics, mouseX, mouseY, Component.translatable("gui.agescrafting.barrel.output_tank"),
                    menu.getOutputFluid(), menu.getOutputFluidAmount(), menu.getOutputTankCapacity(), false);
        }
    }


    private void renderSealButtonTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isMouseOverSealButton(mouseX, mouseY)) {
            return;
        }

        Component key = menu.isSealed()
                ? Component.translatable("gui.agescrafting.barrel.seal_button.unseal")
                : Component.translatable("gui.agescrafting.barrel.seal_button.seal");
        guiGraphics.renderComponentTooltip(font, List.of(key), mouseX, mouseY);
    }
    private boolean isMouseOverSealButton(int mouseX, int mouseY) {
        int left = leftPos + SEAL_BUTTON_X;
        int top = topPos + SEAL_BUTTON_Y;
        return mouseX >= left && mouseX <= left + ICON_W && mouseY >= top && mouseY <= top + ICON_H;
    }

    private boolean isMouseOverTank(int mouseX, int mouseY, int tankX, int tankY) {
        int left = leftPos + tankX;
        int top = topPos + tankY;
        return mouseX >= left && mouseX <= left + TANK_WIDTH && mouseY >= top && mouseY <= top + TANK_HEIGHT;
    }

    private static final class InvisibleSealButton extends Button {
        private InvisibleSealButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // no-op: visual comes from atlas icon in blitSealIcon
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            defaultButtonNarrationText(narrationElementOutput);
        }
    }

    private void renderSingleTankTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, Component title, FluidStack fluid, int amount, int capacity, boolean isInputTank) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(title);
        if (fluid.isEmpty()) {
            tooltip.add(Component.translatable("gui.agescrafting.barrel.empty"));
            if (isInputTank) {
                tooltip.add(Component.translatable("gui.agescrafting.barrel.rain_fill"));
            }
        } else {
            tooltip.add(fluid.getDisplayName());
            tooltip.add(Component.literal(amount + " / " + Math.max(1, capacity) + " mB"));
        }
        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }
}



