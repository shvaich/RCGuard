package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.config.data.ConfigFieldContainer;
import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.rcguard.config.gui.screens.ColorEditScreen;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.ColorUtil;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.gui.GuiButton;

public class ConfigColorButton extends ConfigGuiButton {

    private final int COLOR_BOX_SIZE;
    private final int defaultColor;
    private final GuiButton button;
    private int color;

    public final boolean allowsTransparency;

    public ConfigColorButton(ConfigScreen screen, ConfigFieldContainer fieldData, int defaultColor) throws IllegalAccessException {
        super(screen, fieldData);
        this.color = (int) fieldData.getValue();
        this.defaultColor = defaultColor;
        this.allowsTransparency = fieldData.getAnnotation().allowsTransparency() || !ColorUtil.isOpaque(this.defaultColor);
        this.button = getMcGuiButton("Change");
        this.COLOR_BOX_SIZE = button.height;
    }

    @Override
    public void onResize(int elementRectWidth) {
        //this.button = getMcGuiButton(getMcButtonText("Change"));
        super.onResize(elementRectWidth);
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        super.draw(x, y, mouseX, mouseY);
        final int drawY = y + (drawHeight - button.height) / 2;
        GuiUtil.drawRectWithBorder(contentLeft, drawY, contentLeft + COLOR_BOX_SIZE, drawY + COLOR_BOX_SIZE, ColorUtil.toOpaque(this.color), ModResources.BLACK);
        button.xPosition = contentLeft + COLOR_BOX_SIZE + 1;
        button.yPosition = drawY;
        button.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public void updateDisplayFromDependency() {
        this.button.displayString = getMcButtonText("Change");
    }

    @Override
    protected int getRightSideContentWidth() {
        return button.width + BUTTON_RIGHT_MARGIN + COLOR_BOX_SIZE + 1;
    }

    @Override
    protected int getRightSideContentHeight() {
        return button.height;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT && button.mousePressed(mc, mouseX, mouseY)) {
            button.playPressSound(mc.getSoundHandler());
            GuiUtil.openScreen(new ColorEditScreen(screen, this));
            return true;
        }
        return false;
    }

    @Override
    protected boolean showDependencyIndicatorFromMouse(int mouseX, int mouseY) {
        return button.isMouseOver();
    }

    public void setColor(int color) { this.color = color; }
    public int getColor() { return this.color; }

    public int getDefaultColor() { return this.defaultColor; }

    public void saveColorToField() throws IllegalAccessException {
        this.fieldData.setValue(this.color);
    }
}
