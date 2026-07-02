package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.config.data.ConfigFieldContainer;
import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.gui.GuiButton;

public class ConfigBooleanButton extends ConfigGuiButton {
    protected GuiButton button;
    protected boolean isOn;

    public ConfigBooleanButton(ConfigScreen screen, ConfigFieldContainer fieldData) throws IllegalAccessException {
        this(screen, fieldData, true);
    }

    protected ConfigBooleanButton(ConfigScreen screen, ConfigFieldContainer fieldData, boolean initialize) throws IllegalAccessException {
        super(screen, fieldData, initialize);
    }

    @Override
    protected void initialize() throws IllegalAccessException {
        super.initialize();
        this.isOn = getBoolean();
        this.button = getMcGuiButton(getMcButtonText(isOn));
    }

    @Override
    protected int getRightSideContentWidth() {
        return button.width + BUTTON_RIGHT_MARGIN;
    }

    @Override
    protected int getRightSideContentHeight() {
        return button.height;
    }

    @Override
    public void onResize(int elementRectWidth) {
        //this.button = getMcGuiButton(getMcButtonText(isOn));
        super.onResize(elementRectWidth);
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        super.draw(x, y, mouseX, mouseY);
        button.xPosition = contentLeft;
        button.yPosition = y + (drawHeight - button.height) / 2;
        button.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT && button.mousePressed(mc, mouseX, mouseY)) {
            toggleBoolean();
            this.isOn = getBoolean();
            this.button.displayString = getMcButtonText(isOn);
            if (fieldData.hasDependants())
                screen.updateDependantsDisplay(fieldData);
            button.playPressSound(mc.getSoundHandler());
            return true;
        }
        return false;
    }

    @Override
    public void updateDisplayFromDependency() {
        button.displayString = getMcButtonText(isOn);
    }

    protected boolean getBoolean() throws IllegalAccessException {
        return fieldData.getBoolean();
    }

    protected void toggleBoolean() throws IllegalAccessException {
        fieldData.setValue(!getBoolean());
    }

    @Override
    protected boolean showDependencyIndicatorFromMouse(int mouseX, int mouseY) {
        return button.isMouseOver();
    }
}
