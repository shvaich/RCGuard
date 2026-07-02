package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.config.data.ConfigFieldContainer;
import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.util.MathHelper;

public class ConfigSliderButton extends ConfigGuiButton {

    private static final int SLIDER_WIDTH = 80;
    private static final int SLIDER_HEIGHT = 6;
    private static final int SLIDER_BUTTON_SIZE = 12;
    private static final int SIDE_BUTTON_SIZE = 9;

    private final int minValue, maxValue;
    private int sliderValue;
    private boolean isDragging;
    private int sliderX, sliderY;
    private int sliderButtonX, sliderButtonY, sliderButtonOffset;
    private int minusButtonX, plusButtonX, sideButtonY;
    private int sliderTrackColor, sliderButtonColor;

    public ConfigSliderButton(ConfigScreen screen, ConfigFieldContainer fieldData) throws IllegalAccessException {
        super(screen, fieldData);
        this.minValue = fieldData.getAnnotation().min();
        this.maxValue = fieldData.getAnnotation().max();
        this.sliderValue = (int) fieldData.getValue();
        this.sliderButtonOffset = MathHelper.clamp_int(SLIDER_WIDTH * (sliderValue - minValue) / (maxValue - minValue), 0, SLIDER_WIDTH);
        updateDisplayFromDependency();
    }

    @Override
    protected int getRightSideContentWidth() {
        return (SIDE_BUTTON_SIZE + SLIDER_BUTTON_SIZE / 2 + 1) * 2 + SLIDER_WIDTH + 6;
    }

    @Override
    protected int getRightSideContentHeight() {
        return SLIDER_BUTTON_SIZE + mc.fontRendererObj.FONT_HEIGHT + 5;
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        super.draw(x, y, mouseX, mouseY);
        final int halfSliderButtonSize = SLIDER_BUTTON_SIZE / 2;
        this.minusButtonX = contentLeft;
        this.sliderX = minusButtonX + SIDE_BUTTON_SIZE + halfSliderButtonSize + 1;
        this.plusButtonX = sliderX + SLIDER_WIDTH + halfSliderButtonSize + 1;
        this.sliderY = y + (drawHeight - SLIDER_HEIGHT) / 2;
        this.sliderButtonY = sliderY + (SLIDER_HEIGHT - SLIDER_BUTTON_SIZE) / 2;
        this.sideButtonY = sliderY + (SLIDER_HEIGHT - SIDE_BUTTON_SIZE) / 2;
        if (isDragging) {
            updateSliderFromPosition(mouseX - sliderX);
        }
        this.sliderButtonX = sliderX + sliderButtonOffset - halfSliderButtonSize;
        GuiUtil.drawRectBorder(sliderX, sliderY, sliderX + SLIDER_WIDTH, sliderY + SLIDER_HEIGHT, 1, 0xFF3E3E3E);
        if (sliderButtonX > (sliderX + 1)) {
            final int top = sliderY + 1;
            GuiUtil.drawRect(sliderX + 1, top, sliderButtonX, top + SLIDER_HEIGHT - 2, sliderTrackColor);
        }
        GuiUtil.drawRectWithBorder(sliderButtonX, sliderButtonY, sliderButtonX + SLIDER_BUTTON_SIZE, sliderButtonY + SLIDER_BUTTON_SIZE, sliderButtonColor, ModResources.WHITE);
//        GuiUtil.drawRectWithBorder(sliderX, sliderY, sliderX + SLIDER_WIDTH, sliderY + SLIDER_HEIGHT, 0xFF595959, 0xFFBFBFBF);
//        GuiUtil.drawRectWithBorder(sliderButtonX, sliderButtonY, sliderButtonX + SLIDER_BUTTON_SIZE, sliderButtonY + SLIDER_BUTTON_SIZE, sliderButtonColor, 0xFFE6E6E6);
        GuiUtil.drawRectWithBorder(minusButtonX, sideButtonY, minusButtonX + SIDE_BUTTON_SIZE, sideButtonY + SIDE_BUTTON_SIZE, 0xFF707070, 0xFF606060);
        GuiUtil.drawRectWithBorder(plusButtonX, sideButtonY, plusButtonX + SIDE_BUTTON_SIZE, sideButtonY + SIDE_BUTTON_SIZE, 0xFF707070, 0xFF606060);
        final int signOffset = (SIDE_BUTTON_SIZE - GuiUtil.DIGIT_AND_SIGN_WIDTH + 1) / 2;
        mc.fontRendererObj.drawString("-", minusButtonX + signOffset, sideButtonY + 1, ModResources.WHITE);
        mc.fontRendererObj.drawString("+", plusButtonX + signOffset, sideButtonY + 1, ModResources.WHITE);
        final int sliderValueX = sliderButtonX + (SLIDER_BUTTON_SIZE - GuiUtil.getNumberWidth(sliderValue)) / 2;
        final int sliderValueY = sliderButtonY - mc.fontRendererObj.FONT_HEIGHT - 1;
        mc.fontRendererObj.drawStringWithShadow(String.valueOf(sliderValue), sliderValueX, sliderValueY, ModResources.WHITE);
    }

    @Override
    public void updateDisplayFromDependency() {
        if (showAsInactive()) {
            this.sliderTrackColor = 0xFFA14B4B; // 0xFF8A4A4A, 0xFF993333
            this.sliderButtonColor = 0xFFC45C5C; // 0xFFA85A5A, 0xFFCC4444
        }
        else {
            this.sliderTrackColor = 0xFF1F86D6;  // 0xFF1874BD
            this.sliderButtonColor = 0xFF2FA8FF;
        }
    }

    @Override
    protected boolean showDependencyIndicatorFromMouse(int mouseX, int mouseY) {
        return !isDragging && (
            GuiUtil.isMouseInRect(mouseX, mouseY, sliderX, sliderY, sliderX + SLIDER_WIDTH, sliderY + SLIDER_HEIGHT)
            || GuiUtil.isMouseInRect(mouseX, mouseY, sliderButtonX, sliderButtonY, sliderButtonX + SLIDER_BUTTON_SIZE, sliderButtonY + SLIDER_BUTTON_SIZE)
        );
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            if (GuiUtil.isMouseInRect(mouseX, mouseY, sliderButtonX, sliderButtonY, sliderButtonX + SLIDER_BUTTON_SIZE, sliderButtonY + SLIDER_BUTTON_SIZE)
                || GuiUtil.isMouseInRect(mouseX, mouseY, sliderX, sliderY, sliderX + SLIDER_WIDTH, sliderY + SLIDER_HEIGHT)
            ) {
                updateSliderFromPosition(mouseX - sliderX);
                fieldData.setValue(sliderValue);
                this.isDragging = true;
                GuiUtil.playButtonPressSound();
                return true;
            }
            final boolean clickedOnMinus = GuiUtil.isMouseInRect(mouseX, mouseY, minusButtonX, sideButtonY, minusButtonX + SIDE_BUTTON_SIZE, sideButtonY + SIDE_BUTTON_SIZE);
            if (clickedOnMinus || GuiUtil.isMouseInRect(mouseX, mouseY, plusButtonX, sideButtonY, plusButtonX + SIDE_BUTTON_SIZE, sideButtonY + SIDE_BUTTON_SIZE)) {
                updateSliderFromIncrement(clickedOnMinus ? -1 : 1);
                GuiUtil.playButtonPressSound();
                return true;
            }
        }
        return false;
    }

    public boolean release() {
        if (isDragging) {
            this.isDragging = false;
            try { fieldData.setValue(sliderValue); }
            catch (Exception e) { throw new RuntimeException("Failed to save slider value of field: " + fieldData.getFieldName(), e); }
            return true;
        }
        return false;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == GuiUtil.MOUSE_LEFT) return release();
        return false;
    }

    private void updateSliderFromPosition(int sliderButtonOffsetIn) {
        this.sliderButtonOffset = MathHelper.clamp_int(sliderButtonOffsetIn, 0, SLIDER_WIDTH);
        this.sliderValue = MathHelper.clamp_int(minValue + (int)((sliderButtonOffset * (maxValue - minValue)) / (float)(SLIDER_WIDTH)), minValue, maxValue);
    }

    public void updateSliderFromIncrement(int increment) {
        this.sliderValue = MathHelper.clamp_int(sliderValue + increment, minValue, maxValue);
        this.sliderButtonOffset = MathHelper.clamp_int(SLIDER_WIDTH * (sliderValue - minValue) / (maxValue - minValue), 0, SLIDER_WIDTH);
        try { fieldData.setValue(sliderValue); }
        catch (Exception e) { throw new RuntimeException("Failed to save slider value of field: " + fieldData.getFieldName(), e); }
    }
}
