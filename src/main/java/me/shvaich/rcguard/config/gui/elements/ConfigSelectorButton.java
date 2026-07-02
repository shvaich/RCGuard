package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.config.data.ConfigFieldContainer;
import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;


public class ConfigSelectorButton extends ConfigGuiButton {

    private static final int EXPANDED_BORDER_SIZE = 2;

    private final String[] options;
    private Scrollbar scrollbar;
    private int selectedOptionIndex;
    private int buttonBackgroundColor;
    private boolean isExpanded;

    private int buttonWidth, buttonHeight;
    private int buttonLeft, buttonTop, buttonRight, buttonBottom;
    private int dropdownLeft, dropdownTop, dropdownRight, dropdownBottom;
    private boolean isMouseOverButton;

    public ConfigSelectorButton(ConfigScreen screen, ConfigFieldContainer field) throws IllegalAccessException {
        super(screen, field);
        this.options = field.getAnnotation().options();
        if (options.length == 0)
            throw new IllegalStateException("Config Selector cannot have 0 options");

        final int value = (int) field.getValue();
        if (value < 0 || value >= options.length)
            throw new IndexOutOfBoundsException("Config Selector value is out of options bounds");

        this.selectedOptionIndex = value;
        updateDisplayFromDependency();
    }

    @Override
    public void updateDisplayFromDependency() {
        this.buttonBackgroundColor = showAsInactive() ? 0xFF2A2222 : 0xFF282828;
    }

    @Override
    public void onResize(int elementRectWidth) {
        String longestString = "";
        for (final String option : this.options)
            if (longestString.length() < option.length())
                longestString = option;

        this.buttonWidth = Math.max((int)(mc.fontRendererObj.getStringWidth(longestString) * 0.75f) + 24, getMainButtonWidth());
        this.buttonHeight = mc.fontRendererObj.FONT_HEIGHT + 6;
        super.onResize(elementRectWidth);
    }

    @Override
    protected int getRightSideContentWidth() {
        return BUTTON_RIGHT_MARGIN + this.buttonWidth;
    }

    @Override
    protected int getRightSideContentHeight() {
        return buttonHeight;
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        super.draw(x, y, mouseX, mouseY);
        this.buttonLeft = contentLeft;
        this.buttonTop = y + (drawHeight - buttonHeight) / 2;
        this.buttonRight = this.buttonLeft + this.buttonWidth;
        this.buttonBottom = this.buttonTop + this.buttonHeight;
        this.isMouseOverButton = screen.canMouseVisuallyBeOverElement() && GuiUtil.isMouseInRect(mouseX, mouseY, buttonLeft, buttonTop, buttonRight, buttonBottom);
        final int backgroundColor = (isMouseOverButton || isExpanded) ? 0xFF444444 : buttonBackgroundColor;
        GuiUtil.drawRect(buttonLeft, buttonTop, buttonRight, buttonBottom, backgroundColor);
        final float arrowImageColorScale = isMouseOverButton ? 1 : 0.7f;
        mc.getTextureManager().bindTexture(isExpanded ? ModResources.ARROW_UP : ModResources.ARROW_DOWN);
        GlStateManager.color(arrowImageColorScale, arrowImageColorScale, arrowImageColorScale, 1);
        GuiUtil.drawFullTextureWithCustomSize(buttonRight - 12, buttonTop + (buttonHeight - 5) / 2, 7, 4);
        if (arrowImageColorScale != 1) GlStateManager.color(1, 1, 1, 1);
        final int textLeft = buttonLeft + 4;
        drawScaledText(options[selectedOptionIndex], textLeft, buttonTop, isMouseOverButton ? ModResources.WHITE : ModResources.GRAYISH);
    }

    public void drawDropdown(int mouseX, int mouseY) {
        if (!this.isExpanded || dropdownTop >= dropdownBottom) return;
        this.dropdownLeft = buttonLeft + EXPANDED_BORDER_SIZE;
        this.dropdownRight = buttonRight - EXPANDED_BORDER_SIZE;
        GuiUtil.drawRectWithBorder(buttonLeft, buttonBottom, buttonRight, dropdownBottom + EXPANDED_BORDER_SIZE, EXPANDED_BORDER_SIZE, 0xFF282828, 0xFF444444);
        final boolean hasScrollbar = scrollbar != null;
        if (hasScrollbar) {
            scrollbar.smoothScroll();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(
                (int)(dropdownLeft * screen.SCREEN_WIDTH_SCALE),
                (int)(mc.displayHeight - (dropdownBottom * screen.SCREEN_HEIGHT_SCALE)),
                (int)((dropdownRight - dropdownLeft) * screen.SCREEN_WIDTH_SCALE),
                (int)((dropdownBottom - dropdownTop) * screen.SCREEN_HEIGHT_SCALE)
            );
        }
        final int hoveredIndex = getDropdownIndexFromMouse(mouseX, mouseY);
        int textY = dropdownTop - (hasScrollbar ? scrollbar.getScroll() : 0);
        for (int i = 0; i < options.length; ++i) {
            final int textBottom = textY + buttonHeight;
            if (!hasScrollbar || (textY <= dropdownBottom && textBottom >= dropdownTop)) {
                int color = ModResources.GRAYISH;
                if (i == hoveredIndex) {
                    color = ModResources.WHITE;
                    GuiUtil.drawRect(dropdownLeft, textY, dropdownRight, textBottom, 0xFF343434);
                }
                if (i == selectedOptionIndex) {
                    final float checkmarkColorScale = i == hoveredIndex ? 1 : 0.7f;
                    mc.getTextureManager().bindTexture(ModResources.CHECKMARK);
                    GlStateManager.color(checkmarkColorScale, checkmarkColorScale, checkmarkColorScale, 1);
                    GuiUtil.drawFullTextureWithCustomSize(dropdownRight - 10, textY + (buttonHeight - 5) / 2, 6, 5);
                    if (checkmarkColorScale != 1) GlStateManager.color(1, 1, 1, 1);
                }
                drawScaledText(options[i], buttonLeft + 5, textY, color);
            }
            textY = textBottom;
        }
        if (hasScrollbar) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            scrollbar.draw(dropdownRight, buttonRight, mouseY);
        }
    }

    @Override
    protected boolean showDependencyIndicatorFromMouse(int mouseX, int mouseY) {
        return !this.isExpanded && isMouseOverButton;
    }

    public boolean isMouseOverDropdownRect(int mouseX, int mouseY) {
        return this.isExpanded && GuiUtil.isMouseInRect(mouseX, mouseY, buttonLeft, buttonBottom, buttonRight, dropdownBottom + EXPANDED_BORDER_SIZE);
    }

    public boolean mouseClickedOnDropdown(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            if (isMouseOverDropdownRect(mouseX, mouseY)) {
                final int clickedIndex = getDropdownIndexFromMouse(mouseX, mouseY);
                if (clickedIndex != -1) {
                    final int selectedIndex = (int) this.fieldData.getValue();
                    if (clickedIndex != selectedIndex) {
                        this.fieldData.setValue(clickedIndex);
                        this.selectedOptionIndex = clickedIndex;
                    }
                    close();
                }
                else if (scrollbar == null || !scrollbar.mouseClicked(mouseX, mouseY, mouseButton)) {
                    close();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT && GuiUtil.isMouseInRect(mouseX, mouseY, buttonLeft, buttonTop, buttonRight, buttonBottom)) {
            if (isExpanded) {
                close();
            }
            else {
                this.dropdownTop = buttonBottom + EXPANDED_BORDER_SIZE;
                int maxBottom = screen.height - EXPANDED_BORDER_SIZE;
                // bad fix to a possible issue, im lazy
                if (dropdownTop >= maxBottom) {
                    this.scrollbar = null;
                    this.dropdownBottom = dropdownTop;
                }
                else {  // normal code
                    final int allElementsHeight = options.length * buttonHeight;
                    maxBottom = Math.min(maxBottom, dropdownTop + (5 * buttonHeight));
                    this.scrollbar = Scrollbar.create(allElementsHeight, dropdownTop, maxBottom);
                    this.dropdownBottom = scrollbar == null ? (dropdownTop + allElementsHeight) : scrollbar.getTrackBottom();
                }
                this.isExpanded = true;
            }
            return true;
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (scrollbar != null) scrollbar.mouseReleased(mouseX, mouseY, state);
    }

    public boolean updateScrollbarFromMouseScroll(int eventDWheel) {
        if (scrollbar != null) {
            final int mouseX = (int) (Mouse.getEventX() / screen.SCREEN_WIDTH_SCALE);
            final int mouseY = (int) (screen.height - Mouse.getEventY() / screen.SCREEN_HEIGHT_SCALE - 1);
            if (GuiUtil.isMouseInRect(mouseX, mouseY, dropdownLeft, dropdownTop, dropdownRight, dropdownBottom)) {
                scrollbar.updateFromMouseScroll(eventDWheel);
                return true;
            }
        }
        return false;
    }

    public void close() {
        this.scrollbar = null;
        this.isExpanded = false;
    }

    public boolean isExpanded() { return this.isExpanded; }

    private void drawScaledText(String text, int left, int top, int color) {
        GlStateManager.pushMatrix();
        {
            final float scale = 0.75f;
            GlStateManager.translate(left, top + ((buttonHeight - mc.fontRendererObj.FONT_HEIGHT*scale) / 2f), 0);
            GlStateManager.scale(scale, scale, scale);
            mc.fontRendererObj.drawStringWithShadow(text, 0, 0, color);
        }
        GlStateManager.popMatrix();
    }

    private int getDropdownIndexFromMouse(int mouseX, int mouseY) {
        if (GuiUtil.isMouseInRect(mouseX, mouseY, dropdownLeft, dropdownTop, dropdownRight, dropdownBottom)) {
            final int scrollIn = scrollbar != null ? scrollbar.getScroll() : 0;
            final int clickedOptionIndex = (mouseY - dropdownTop + scrollIn) / buttonHeight;
            if (clickedOptionIndex >= 0 && clickedOptionIndex < options.length)
                return clickedOptionIndex;
        }
        return -1;
    }
}