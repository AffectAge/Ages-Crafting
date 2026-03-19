package com.agescrafting.agescrafting.barrel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class BarrelScreen extends AbstractContainerScreen<BarrelMenu> {
    private static final int BACKGROUND_COLOR = 0xFF2D2823;
    private static final int PANEL_COLOR = 0xFF3B342E;
    private static final int PANEL_HIGHLIGHT = 0xFF4A4139;
    private static final int SLOT_COLOR = 0xFF1E1A16;
    private static final int TANK_BORDER = 0xFF6B6258;

    private static final int TANK_X = 10;
    private static final int TANK_Y = 18;
    private static final int TANK_WIDTH = 14;
    private static final int TANK_HEIGHT = 54;

    private Button sealButton;

    public BarrelScreen(BarrelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        sealButton = addRenderableWidget(
                Button.builder(getSealButtonLabel(), button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                    }
                }).bounds(leftPos + 150, topPos + 20, 18, 18).build()
        );
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (sealButton != null) {
            sealButton.setMessage(getSealButtonLabel());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderTankTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, BACKGROUND_COLOR);

        guiGraphics.fill(x + 4, y + 4, x + imageWidth - 4, y + 76, PANEL_COLOR);
        guiGraphics.fill(x + 4, y + 78, x + imageWidth - 4, y + imageHeight - 4, PANEL_COLOR);
        guiGraphics.fill(x + 6, y + 6, x + imageWidth - 6, y + 8, PANEL_HIGHLIGHT);

        renderSlotBackground(guiGraphics, x + 26, y + 18);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                renderSlotBackground(guiGraphics, x + 62 + column * 18, y + 18 + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                renderSlotBackground(guiGraphics, x + 8 + column * 18, y + 84 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            renderSlotBackground(guiGraphics, x + 8 + column * 18, y + 142);
        }

        renderTank(guiGraphics, x + TANK_X, y + TANK_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFE5DCCF, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFCFC5B7, false);
        guiGraphics.drawString(font, Component.translatable("gui.agescrafting.barrel.fluid"), 8, 8, 0xFFCFC5B7, false);
        if (menu.isSealed()) {
            guiGraphics.drawString(font, Component.translatable("gui.agescrafting.barrel.sealed"), 126, 8, 0xFFE0B7B7, false);
        }
    }

    private void renderSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF0F0D0B);
        guiGraphics.fill(x, y, x + 16, y + 16, SLOT_COLOR);
    }

    private void renderTank(GuiGraphics guiGraphics, int tankX, int tankY) {
        guiGraphics.fill(tankX - 1, tankY - 1, tankX + TANK_WIDTH + 1, tankY + TANK_HEIGHT + 1, TANK_BORDER);
        guiGraphics.fill(tankX, tankY, tankX + TANK_WIDTH, tankY + TANK_HEIGHT, 0xFF120F0D);

        int fluidAmount = menu.getFluidAmount();
        if (fluidAmount <= 0) {
            return;
        }

        int capacity = menu.getTankCapacity();
        int filledHeight = Math.max(1, Math.round((fluidAmount / (float) capacity) * TANK_HEIGHT));
        FluidStack fluid = menu.getFluid();
        int color = 0xFF3C80EA;
        if (!fluid.isEmpty()) {
            int tint = net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
            color = 0xFF000000 | (tint & 0x00FFFFFF);
        }

        int fillTop = tankY + TANK_HEIGHT - filledHeight;
        guiGraphics.fill(tankX, fillTop, tankX + TANK_WIDTH, tankY + TANK_HEIGHT, color);

        // Soft sheen so liquid does not look flat.
        int sheenColor = (color & 0xFCFCFC) | 0x44000000;
        guiGraphics.fill(tankX, fillTop, tankX + 2, tankY + TANK_HEIGHT, sheenColor);
    }

    private void renderTankTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int tankLeft = leftPos + TANK_X;
        int tankTop = topPos + TANK_Y;
        if (mouseX < tankLeft || mouseX > tankLeft + TANK_WIDTH || mouseY < tankTop || mouseY > tankTop + TANK_HEIGHT) {
            return;
        }

        List<Component> tooltip = new ArrayList<>();
        FluidStack fluid = menu.getFluid();
        if (fluid.isEmpty()) {
            tooltip.add(Component.translatable("gui.agescrafting.barrel.empty"));
        } else {
            tooltip.add(fluid.getDisplayName());
            tooltip.add(Component.literal(menu.getFluidAmount() + " / " + menu.getTankCapacity() + " mB"));
        }
        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private Component getSealButtonLabel() {
        return menu.isSealed()
                ? Component.translatable("gui.agescrafting.barrel.unseal.short")
                : Component.translatable("gui.agescrafting.barrel.seal.short");
    }
}
