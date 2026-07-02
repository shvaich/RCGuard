package me.shvaich.rcguard.config.gui.screens;

import me.shvaich.rcguard.config.gui.elements.ConfigColorButton;
import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.ColorUtil;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorEditScreen extends GuiScreen implements GuiSlider.ISlider {

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTONS_GAP = 4;
    private static final int TOP_BOTTOM_MARGIN = 16;

    private final GuiScreen parentScreen;
    private final ConfigColorButton colorButton;
    private final int originalColor;

    private final List<GuiSlider> colorSliders = new ArrayList<>();
    private int selectedSliderIndex = -1;

    private int allButtonsHeight;

    public ColorEditScreen(GuiScreen parentScreen, ConfigColorButton colorButton) {
        this.parentScreen = parentScreen;
        this.colorButton = colorButton;
        this.originalColor = colorButton.getColor();
    }

    @Override
    public void initGui() {
        final int color = colorButton.getColor();
        final int centerX = this.width / 2;
        final int slidersX = centerX - BUTTON_WIDTH;
        final List<String> colorChannels = new ArrayList<>(Arrays.asList("Red", "Green", "Blue"));
        if (colorButton.allowsTransparency) colorChannels.add("Alpha");
        this.allButtonsHeight = ((colorChannels.size() + 2) * (BUTTON_HEIGHT + BUTTONS_GAP) - BUTTONS_GAP) + TOP_BOTTOM_MARGIN + BUTTON_HEIGHT;
        int drawY = (this.height - allButtonsHeight) / 2;
        colorSliders.clear();
        for (int i = 0; i < colorChannels.size(); ++i) {
            final int value = ColorUtil.getColorChannelValue(color, i);
            final GuiSlider slider = new GuiSlider(i + 1, slidersX, drawY, BUTTON_WIDTH, BUTTON_HEIGHT, colorChannels.get(i) + ": ", "", 0, 255, value, false, true, this);
            this.colorSliders.add(slider);
            this.buttonList.add(slider);
            drawY += BUTTON_HEIGHT + BUTTONS_GAP;
        }
        this.buttonList.add(new GuiButton(5, slidersX, drawY, BUTTON_WIDTH, BUTTON_HEIGHT, "Undo Changes"));
        drawY += BUTTON_HEIGHT + BUTTONS_GAP;
        this.buttonList.add(new GuiButton(6, slidersX, drawY, BUTTON_WIDTH, BUTTON_HEIGHT, "Reset to default"));
        drawY += BUTTON_HEIGHT + TOP_BOTTOM_MARGIN;
        this.buttonList.add(new GuiButton(7, centerX - BUTTON_WIDTH / 2, drawY, BUTTON_WIDTH, BUTTON_HEIGHT, "Done"));
        super.initGui();
        this.selectedSliderIndex = -1;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final int firstSliderY = colorSliders.get(0).yPosition;
        final int lineLength = allButtonsHeight - BUTTON_HEIGHT - TOP_BOTTOM_MARGIN;
        final int colorBoxLeft = this.width / 2 + 10;
        final int colorBoxTop = firstSliderY;
        final int colorBoxRight = colorBoxLeft + lineLength;
        final int colorBoxBottom = colorBoxTop + lineLength;
        if (GuiUtil.beginClearRect(mc, colorBoxLeft, colorBoxTop, colorBoxRight, colorBoxBottom)) {
            drawDefaultBackground();
            GuiUtil.endClearRect();
        }
        drawCenteredString(fontRendererObj, colorButton.getName(), this.width / 2, firstSliderY - TOP_BOTTOM_MARGIN - fontRendererObj.FONT_HEIGHT / 2, ModResources.WHITE);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.selectedSliderIndex != -1) {
            final GuiSlider slider = colorSliders.get(this.selectedSliderIndex);
            GuiUtil.drawRectBorder(slider.xPosition-1, slider.yPosition-1, slider.xPosition+slider.width+1, slider.yPosition+slider.height+1, 1, 0xFF3C6EFF);
        }
        GuiUtil.drawRectWithBorder(colorBoxLeft, colorBoxTop, colorBoxRight, colorBoxBottom, colorButton.getColor(), ModResources.BLACK);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.selectedSliderIndex = -1;
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 1 && button.id <= 4)
            this.selectedSliderIndex = button.id-1;
        else {
            switch (button.id) {
                case 5: updateToColor(originalColor); break;
                case 6: updateToColor(colorButton.getDefaultColor()); break;
                case 7: GuiUtil.openScreen(parentScreen); break;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_TAB) {
            if ((++this.selectedSliderIndex) >= this.colorSliders.size())
                this.selectedSliderIndex = 0;  // change to -1 if you want a "none selected buffer" between cycles
            return;
        }
        else if (this.selectedSliderIndex != -1 && (keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT)) {
            final GuiSlider slider = colorSliders.get(this.selectedSliderIndex);
            final int value = keyCode == Keyboard.KEY_LEFT ? -1 : 1;
            slider.setValue(slider.getValueInt()+value);
            slider.updateSlider();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        try {
            colorButton.saveColorToField();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to save: " + colorButton.getName(), e);
        }
        super.onGuiClosed();
        GuiUtil.openScreen(parentScreen);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        colorButton.setColor(ColorUtil.getColorWithUpdatedChannel(colorButton.getColor(), slider.id-1, slider.getValueInt()));
    }

    private void updateToColor(int newColor) {
        for (final GuiSlider slider : colorSliders) {
            slider.setValue(ColorUtil.getColorChannelValue(newColor, slider.id-1));
            slider.updateSlider();
        }
    }
}
