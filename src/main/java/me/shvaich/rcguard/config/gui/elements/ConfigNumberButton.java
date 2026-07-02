package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.config.data.ConfigFieldContainer;
import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

public class ConfigNumberButton extends ConfigGuiButton {

    private static final int ICON_WIDTH = 9, ICON_HEIGHT = 5;
    private static final int DISABLED_BORDER_COLOR = 0xFF656565;

    private final int minValue, maxValue;
    private int value;
    private int buttonX, upButtonY, downButtonY;
    private int normalBorderColor;

    public ConfigNumberButton(ConfigScreen screen, ConfigFieldContainer fieldData) throws IllegalAccessException {
        super(screen, fieldData);
        this.minValue = fieldData.getAnnotation().min();
        this.maxValue = fieldData.getAnnotation().max();
        this.value = (int) fieldData.getValue();
        updateDisplayFromDependency();
    }

    @Override
    protected int getRightSideContentWidth() {
        return BUTTON_RIGHT_MARGIN + GuiUtil.getNumberWidth(value) + 4 + ICON_WIDTH + 4;
    }

    @Override
    protected int getRightSideContentHeight() {
        return (ICON_HEIGHT + 4) * 2;
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        super.draw(x, y, mouseX, mouseY);
        int drawX = contentLeft;
        drawX = 4 + mc.fontRendererObj.drawStringWithShadow(String.valueOf(value), drawX, y + (drawHeight - mc.fontRendererObj.FONT_HEIGHT) / 2, ModResources.WHITE);
        this.buttonX = drawX + 1;
        int buttonTop = y + (drawHeight - getRightSideContentHeight()) / 2;
        this.upButtonY = buttonTop + 1;
        final int rectRight = drawX + ICON_WIDTH + 4;
        final int rectHeight = ICON_HEIGHT + 4;
        final int iconX = buttonX + 1;
        GuiUtil.drawRectBorder(drawX, buttonTop, rectRight, buttonTop + rectHeight, 1, value != maxValue ? normalBorderColor : DISABLED_BORDER_COLOR);
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(ModResources.ARROW_UP);
        GuiUtil.drawFullTextureWithCustomSize(iconX, upButtonY + 1, ICON_WIDTH, ICON_HEIGHT);
        buttonTop += rectHeight;
        this.downButtonY = buttonTop + 1;
        GuiUtil.drawRectBorder(drawX, buttonTop, rectRight, buttonTop + rectHeight, 1, value != minValue ? normalBorderColor : DISABLED_BORDER_COLOR);
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(ModResources.ARROW_DOWN);
        GuiUtil.drawFullTextureWithCustomSize(iconX, downButtonY + 1, ICON_WIDTH, ICON_HEIGHT);
    }

    @Override
    public void updateDisplayFromDependency() {
        this.normalBorderColor = showAsInactive() ? 0xFF8A0000 : ModResources.WHITE;
    }

    @Override
    protected boolean showDependencyIndicatorFromMouse(int mouseX, int mouseY) {
        return false;
//        final int left = buttonX - 1;
//        final int top = upButtonY - 1;
//        return GuiUtil.isMouseInRect(mouseX, mouseY, left, top, left + ICON_WIDTH + 4, top + getRightSideContentHeight());
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            final int right = buttonX + ICON_WIDTH + 2;
            final int clickAreaHeight = ICON_HEIGHT + 2;
            final boolean clickedOnUpButton = GuiUtil.isMouseInRect(mouseX, mouseY, buttonX, upButtonY, right, upButtonY + clickAreaHeight);
            if (clickedOnUpButton || GuiUtil.isMouseInRect(mouseX, mouseY, buttonX, downButtonY, right, downButtonY + clickAreaHeight)) {
                this.value = MathHelper.clamp_int(value + (clickedOnUpButton ? 1 : -1), minValue, maxValue);
                fieldData.setValue(value);
                GuiUtil.playButtonPressSound();
                return true;
            }
        }
        return false;
    }
}
