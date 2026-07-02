package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.GuiUtil;

public class ConfigCategoryMenuEntry {

    private final ConfigScreen screen;
    private final String categoryName;

    private int posX, posY;
    // private int elementRectWidth;

    public ConfigCategoryMenuEntry(ConfigScreen screen, String categoryName) {
        this.screen = screen;
        this.categoryName = categoryName;
    }

    public void onResize(int elementRectWidth) {
        // this.elementRectWidth = elementRectWidth;
    }

    public void draw(int x, int y, int mouseX, int mouseY) {
        this.posX = x;
        this.posY = y;
        final int textColor = categoryName.equals(screen.getSelectedCategory()) ? ModResources.SELECTED_CATEGORY_COLOR : (isMouseOver(mouseX, mouseY) ? ModResources.WHITE : ModResources.GRAYISH);
        screen.mc.fontRendererObj.drawStringWithShadow(categoryName, x, y, textColor);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == GuiUtil.MOUSE_LEFT && isMouseOver(mouseX, mouseY)) {
            this.screen.setSelectedCategory(categoryName);
            GuiUtil.playButtonPressSound();
            return true;
        }
        return false;
    }

    private boolean isMouseOver(int mouseX, int mouseY) {
        return GuiUtil.isMouseInRect(mouseX, mouseY, this.posX, this.posY, this.posX + getWidth(), this.posY + getHeight());
    }

    private int getWidth() { return screen.mc.fontRendererObj.getStringWidth(this.categoryName); }

    public int getHeight() { return screen.mc.fontRendererObj.FONT_HEIGHT; }

    public int getBottomMargin() { return 4; }
}
