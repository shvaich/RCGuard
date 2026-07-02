package me.shvaich.rcguard.config.gui.screens;

import me.shvaich.rcguard.gui.data.AbstractRenderer;
import me.shvaich.rcguard.gui.data.HUDManager;
import me.shvaich.rcguard.gui.data.HUDPosition;
import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Collections;

public class HUDPositionEditScreen extends GuiScreen {
    private static final int BUTTON_SIZE = 10;

    private final GuiScreen parentScreen;
    private final AbstractRenderer renderer;
    private final HUDPosition hudPosition;
    private final CustomButton[] customButtons;
    private final double originalRelativeX, originalRelativeY;
    private boolean isFirstResize = true;
    private int prevScreenWidth, prevScreenHeight;
    private int prevRendererWidth, prevRendererHeight;
    private boolean isDragging;
    private int prevMouseX, prevMouseY;

    public HUDPositionEditScreen(GuiScreen parentScreen, AbstractRenderer renderer) {
        this.parentScreen = parentScreen;
        this.renderer = renderer;
        this.hudPosition = renderer.getHUDPosition();
        this.originalRelativeX = hudPosition.getRelativeX();
        this.originalRelativeY = hudPosition.getRelativeY();
        this.customButtons = new CustomButton[]{
            new CustomButton(ModResources.RELOAD_ICON, "Reset to Default Position"),
            new CustomButton(ModResources.UNDO_ICON, "Undo Changes")
        };
    }

    @Override
    public void initGui() {
        final int buttonX = this.width - BUTTON_SIZE - 2;
        int buttonY = 2;
        for (final CustomButton button : customButtons) {
            button.onResize(buttonX, buttonY);
            buttonY += BUTTON_SIZE + 2;
        }
        final int rendererWidth = renderer.getDummyWidth();
        final int rendererHeight = renderer.getDummyHeight();
        if (!isFirstResize) {
            hudPosition.savePositionToRelative(prevScreenWidth - prevRendererWidth, prevScreenHeight - prevRendererHeight); // (height) + 1
        }
        hudPosition.updateDrawPositionFromRelative(this.width - rendererWidth, this.height - rendererHeight); // (height) + 1
        this.isFirstResize = false;
        this.prevScreenWidth = this.width;
        this.prevScreenHeight = this.height;
        this.prevRendererWidth = rendererWidth;
        this.prevRendererHeight = rendererHeight;
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        renderCrosshair();
        final boolean prevEnabled = hudPosition.isEnabled();
        hudPosition.setEnabled(false);
        HUDManager.renderAllDummy(this.width, this.height);
        hudPosition.setEnabled(prevEnabled);
        drawDefaultBackground();
        final int dummyWidth = renderer.getDummyWidth();
        final int dummyHeight = renderer.getDummyHeight();
        if (isDragging) {
            final int posX = MathHelper.clamp_int(hudPosition.getDrawX() + mouseX - prevMouseX, 0, this.width - dummyWidth);
            final int posY = MathHelper.clamp_int(hudPosition.getDrawY() + mouseY - prevMouseY, 0, this.height - dummyHeight); // (height) + 1
            hudPosition.setDrawPosition(posX, posY);
        }
        renderer.renderDummy(this.width, this.height, dummyWidth, dummyHeight);
        if (!isDragging) {
            CustomButton hoveredButton = null;
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            for (final CustomButton button : customButtons) {
                final CustomButton buttonIfHovered = button.draw(mc, mouseX, mouseY);
                if (hoveredButton == null) hoveredButton = buttonIfHovered;
            }
            GlStateManager.disableBlend();
            GlStateManager.color(1f, 1f, 1f, 1f);
            if (hoveredButton != null) {
                drawHoveringText(Collections.singletonList(hoveredButton.hoveringText), mouseX, mouseY + fontRendererObj.FONT_HEIGHT + 6);
            }
        }
        this.prevMouseX = mouseX;
        this.prevMouseY = mouseY;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            int clickedButtonIdx = -1;
            for (int i = 0; i < customButtons.length; ++i) {
                if (customButtons[i].isMouseOver(mouseX, mouseY)) {
                    clickedButtonIdx = i;
                    break;
                }
            }
            if (clickedButtonIdx != -1) {
                if (clickedButtonIdx == 0) hudPosition.resetToDefaultPosition();
                else hudPosition.setRelativePosition(originalRelativeX, originalRelativeY);
                hudPosition.updateDrawPositionFromRelative(this.width - renderer.getDummyWidth(), this.height - renderer.getDummyHeight()); // (height) + 1
                return;
            }
            this.isDragging = true;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == GuiUtil.MOUSE_LEFT && isDragging) {
            this.isDragging = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        hudPosition.savePositionToRelative(this.width - renderer.getDummyWidth(), this.height - renderer.getDummyHeight()); // (height) + 1
        super.onGuiClosed();
        GuiUtil.openScreen(parentScreen);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void renderCrosshair() {
        mc.getTextureManager().bindTexture(Gui.icons);
        drawTexturedModalRect(this.width / 2 - 7, this.height / 2 - 7, 0, 0, 16, 16);
    }

    private static class CustomButton {
        private final ResourceLocation icon;
        public final String hoveringText;
        private int xPosition, yPosition;

        public CustomButton(ResourceLocation icon, String hoveringText) {
            this.icon = icon;
            this.hoveringText = hoveringText == null ? "" : hoveringText;
        }

        public void onResize(int x, int y) {
            this.xPosition = x;
            this.yPosition = y;
        }

        public CustomButton draw(Minecraft mc, int mouseX, int mouseY) {
            final boolean isMouseOver = isMouseOver(mouseX, mouseY);
            GlStateManager.color(1f, 1f, 1f, isMouseOver ? 1f : 0.4f);
            GuiUtil.drawRectWithBorder(xPosition, yPosition, xPosition + BUTTON_SIZE, yPosition + BUTTON_SIZE, ModResources.ELEMENT_BACKGROUND, ModResources.ELEMENT_BORDER);
            mc.getTextureManager().bindTexture(icon);
            final int iconSize = 6;
            final int iconOffset = (BUTTON_SIZE - iconSize) / 2;
            GuiUtil.drawFullTextureWithCustomSize(xPosition + iconOffset, yPosition + iconOffset, iconSize, iconSize);
            return (isMouseOver && !hoveringText.isEmpty()) ? this : null;
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return GuiUtil.isMouseInRect(mouseX, mouseY, xPosition, yPosition, xPosition + BUTTON_SIZE, yPosition + BUTTON_SIZE);
        }
    }
}
