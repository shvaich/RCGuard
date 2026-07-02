package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.config.data.ConfigFieldContainer;
import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.config.gui.screens.HUDPositionEditScreen;
import me.shvaich.rcguard.gui.data.AbstractRenderer;
import me.shvaich.rcguard.gui.data.HUDManager;
import me.shvaich.rcguard.gui.data.HUDPosition;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.gui.GuiButton;

public class ConfigHUDButton extends ConfigGuiButton {

    private final HUDPosition hudPosition;
    private final GuiButton enabledButton;
    private final GuiButton editButton;

    public ConfigHUDButton(ConfigScreen screen, ConfigFieldContainer fieldData) throws IllegalAccessException {
        super(screen, fieldData);
        this.hudPosition = (HUDPosition) fieldData.getValue();
        this.enabledButton = getMcGuiButton(getMcButtonText(hudPosition.isEnabled()));
        this.editButton = getMcGuiButton(getMcButtonText("Position"));
    }

    @Override
    protected int getRightSideContentWidth() {
        return enabledButton.width + BUTTON_RIGHT_MARGIN;
    }

    @Override
    protected int getRightSideContentHeight() {
        return enabledButton.height + 1 + editButton.height;
    }

    @Override
    public void updateDisplayFromDependency() {
        enabledButton.displayString = getMcButtonText(hudPosition.isEnabled());
        editButton.displayString = getMcButtonText("Position");
    }

    @Override
    protected boolean showDependencyIndicatorFromMouse(int mouseX, int mouseY) {
        return enabledButton.isMouseOver() || editButton.isMouseOver();
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        super.draw(x, y, mouseX, mouseY);
        enabledButton.xPosition = contentLeft;
        enabledButton.yPosition = y + (drawHeight - getRightSideContentHeight()) / 2;
        enabledButton.drawButton(mc, mouseX, mouseY);

        editButton.xPosition = enabledButton.xPosition;
        editButton.yPosition = enabledButton.yPosition + enabledButton.height + 1;
        editButton.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            if (enabledButton.mousePressed(mc, mouseX, mouseY)) {
                final boolean previousValue = hudPosition.isEnabled();
                hudPosition.setEnabled(!previousValue);
                fieldData.invokeAction(previousValue);
                enabledButton.displayString = getMcButtonText(hudPosition.isEnabled());
                if (fieldData.hasDependants())
                    screen.updateDependantsDisplay(fieldData);
                enabledButton.playPressSound(mc.getSoundHandler());
                return true;
            }
            if (editButton.mousePressed(mc, mouseX, mouseY)) {
                editButton.playPressSound(mc.getSoundHandler());
                final AbstractRenderer renderer = HUDManager.getRendererFromPosition(hudPosition);
                if (renderer != null) {
                    GuiUtil.openScreen(new HUDPositionEditScreen(screen, renderer));
                }
                else { throw new RuntimeException("No HUD associated to: " + fieldData.getName()); }
                return true;
            }
        }
        return false;
    }
}
