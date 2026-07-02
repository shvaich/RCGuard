package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class Searchbar {
    private static final int EXPANDED_WIDTH = 104;
    private static final int MAX_STRING_LENGTH = 32;

    //private final String placeholder;
    private Minecraft mc;
    private FontRenderer fontRenderer;
    private String text = "";

    private boolean isExpanded;
    private boolean isFocused;
    private int cursorPosition, selectionEnd, lineScrollOffset;

    private int top, right;
    private int buttonX;
    private int expandedAreaX;
    private int textAreaX;

//    public Searchbar(String placeholder) {
//        this.placeholder = placeholder == null ? "" : placeholder;
//    }

    public void onResize(Minecraft mc, int top, int right) {
        this.mc = mc;
        this.fontRenderer = mc.fontRendererObj;
        this.right = right;
        this.top = top;
        this.buttonX = right - getHeight();
        this.expandedAreaX = this.buttonX - EXPANDED_WIDTH;
        this.textAreaX = this.expandedAreaX + getHeight();
        this.cursorPosition = this.selectionEnd = this.text.length();
        updateLineScrollOffset();
    }

    public void draw(int mouseX, int mouseY) {
        final int height = getHeight();
        final int bottom = top + height;
        final boolean isMouseHoveringButton = isMouseInButton(mouseX, mouseY);
        final int searchImageSideLength = fontRenderer.FONT_HEIGHT - 3;  // 6;
        if (isExpanded) {
            GuiUtil.drawRect(expandedAreaX, top, right, bottom, 0xFF2D2D2D);
            drawImage(ModResources.SEARCH_ICON, expandedAreaX, searchImageSideLength, 1);

            final int textTop = this.top + (getHeight() - fontRenderer.FONT_HEIGHT) / 2 + 1;
            final String drawnText = (!isFocused && text.isEmpty()) ?
                    /* fontRenderer.trimStringToWidth(placeholder, getTextAreaWidth()) */ "Search..." : getVisibleText();
            int textRight = textAreaX;
            if (!drawnText.isEmpty()) {
                textRight = fontRenderer.drawString(drawnText, textAreaX, textTop, ModResources.WHITE);
            }

            final int cancelImageSideLength = fontRenderer.FONT_HEIGHT-5;  //4;
            float colorScale = 1;
            if (isMouseHoveringButton) GuiUtil.drawRect(buttonX, top, right, bottom, 0xFF383838);
            else colorScale = 0.7f;
            drawImage(ModResources.CANCEL_ICON, buttonX, cancelImageSideLength, colorScale);

            if (isFocused) {
                // drawnText == visibleText
                final int caretTop = textTop - 1;
                final int caretBottom = textTop + fontRenderer.FONT_HEIGHT;
                if (hasSelectedText()) {
                    int visibleSelectionStart = realSelectionStart() - lineScrollOffset;
                    int left = textAreaX;
                    if (visibleSelectionStart > 0) left += fontRenderer.getStringWidth(drawnText.substring(0, visibleSelectionStart));
                    else visibleSelectionStart = 0;

                    final int selectEnd = realSelectionEnd() - lineScrollOffset;
                    final int right = selectEnd >= drawnText.length() ? textRight :
                            (left + fontRenderer.getStringWidth(drawnText.substring(visibleSelectionStart, selectEnd)));

                    GlStateManager.enableColorLogic();
                    GlStateManager.colorLogicOp(5387);
                    GuiUtil.drawRect(left, caretTop, right, caretBottom, 0xFF0000FF);
                    GlStateManager.disableColorLogic();
                }
                else {
                    final int cursorPos = this.cursorPosition - lineScrollOffset;
                    int left = textAreaX;
                    if (cursorPos > 0)
                        left = cursorPos >= drawnText.length() ? textRight :
                                left + fontRenderer.getStringWidth(drawnText.substring(0, cursorPos));

                    final float alphaScale = (float)((Math.sin(System.currentTimeMillis() / 300.0) + 1) * 0.5f);  // 0-1
                    final int alpha = (int)(alphaScale * 255);
                    final int color = (alpha << 24) | 0x00FFFFFF;
                    GuiUtil.drawRect(left, caretTop, left + 1, caretBottom, color);
                }
            }
        }
        else {
            final float colorScale = isMouseHoveringButton ? 1 : 0.7f;
            GuiUtil.drawRect(buttonX, top, right, bottom, isMouseHoveringButton ? 0xFF383838 : 0xFF2D2D2D);
            drawImage(ModResources.SEARCH_ICON, buttonX, searchImageSideLength, colorScale);
        }
        if (!isMouseHoveringButton) GlStateManager.color(1, 1, 1, 1);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            if (isMouseInButton(mouseX, mouseY)) {
                this.isExpanded = !this.isExpanded;
                this.isFocused = this.isExpanded;
                this.cursorPosition = this.selectionEnd = this.lineScrollOffset = 0;
                if (!this.text.isEmpty()) this.text = "";
                return true;
            }
            else if (isExpanded && GuiUtil.isMouseInRect(mouseX, mouseY, expandedAreaX, top, buttonX, top + getHeight())) {
                int newCursorPosition = this.text.length();
                if (mouseX >= this.textAreaX) {
                    final String visibleText = getVisibleText();
                    final int clickX = mouseX - this.textAreaX;
                    int charX = 0;
                    for (int i = 0; i < visibleText.length(); ++i) {
                        final int charWidth = fontRenderer.getCharWidth(visibleText.charAt(i));
                        if (clickX >= charX && clickX < charX + charWidth) {
                            final int clickedIndex = ((clickX - charX) >= (charWidth / 2)) ? i+1 : i;
                            newCursorPosition = lineScrollOffset + clickedIndex;
                            break;
                        }
                        charX += charWidth;
                    }
                }
                this.isFocused = true;
                this.cursorPosition = this.selectionEnd = newCursorPosition;
                updateLineScrollOffset();
                return true;
            }
        }
        return false;
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        if (this.isFocused) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                unfocus();
                return true;
            }
            if (handleKeyCombos(keyCode)) return true;
            switch (keyCode) {
                case Keyboard.KEY_BACK:
                case Keyboard.KEY_DELETE: {
                    if (!this.text.isEmpty()) {
                        if (hasSelectedText()) {
                            writeText("");
                        }
                        else {
                            final boolean isBackspace = keyCode == Keyboard.KEY_BACK;
                            if (this.cursorPosition != (isBackspace ? 0 : this.text.length())) {
                                if (isBackspace) this.selectionEnd = --this.cursorPosition;
                                this.text = this.text.substring(0, this.cursorPosition) + this.text.substring(this.cursorPosition+1);
                                updateLineScrollOffset();
                            }
                        }
                    }
                } return true;

                case Keyboard.KEY_LEFT:
                case Keyboard.KEY_RIGHT: {
                    final boolean isLeftKey = keyCode == Keyboard.KEY_LEFT;
                    final boolean isShifting = GuiScreen.isShiftKeyDown();
                    if (!isShifting && hasSelectedText()) {
                        this.cursorPosition = isLeftKey ? realSelectionStart() : realSelectionEnd();
                        this.selectionEnd = this.cursorPosition;
                        updateLineScrollOffset();
                    }
                    else {
                        final int newCursorPos = this.cursorPosition + (isLeftKey ? -1 : 1);
                        if (newCursorPos >= 0 && newCursorPos <= this.text.length()) {
                            this.cursorPosition = newCursorPos;
                            if (!isShifting) this.selectionEnd = this.cursorPosition;
                            updateLineScrollOffset();
                        }
                    }
                } return true;

                case Keyboard.KEY_HOME:
                case Keyboard.KEY_END: {
                    this.cursorPosition = keyCode == Keyboard.KEY_HOME ? 0 : this.text.length();
                    if (!GuiScreen.isShiftKeyDown()) {
                        this.selectionEnd = cursorPosition;
                    }
                    updateLineScrollOffset();
                } return true;
            }
        }
        if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            this.isExpanded = true;
            if (!isFocused) {
                this.isFocused = true;
                this.cursorPosition = this.selectionEnd = this.text.length();
            }
            writeText(String.valueOf(typedChar));
            return true;
        }
        return false;
    }

    public int getHeight() { return 14; }

    public boolean isFocused() { return this.isFocused; }

    public void unfocus() {
        this.isFocused = false;
    }

//    public void clear() {
//        unfocus();
//        if (!this.text.isEmpty()) this.text = "";
//    }

    public String getText() { return this.text; }

    private boolean handleKeyCombos(int keyCode) {
        if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.cursorPosition = 0;
            this.selectionEnd = this.text.length();
            return true;
        }
        else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            if (hasSelectedText()) {
                GuiScreen.setClipboardString(getSelectedText());
            }
            return true;
        }
        else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            if (hasSelectedText()) {
                GuiScreen.setClipboardString(getSelectedText());
                writeText("");
            }
            return true;
        }
        else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            final String clipboardString = GuiScreen.getClipboardString();
            if (!clipboardString.isEmpty())
                writeText(clipboardString);
            return true;
        }
        return false;
    }

    private void writeText(String input) {
        final int start, end;
        if (hasSelectedText()) {
            start = realSelectionStart();
            end = realSelectionEnd();
        }
        else {
            start = end = this.cursorPosition;
        }

        final int remainingSpace = MAX_STRING_LENGTH - this.text.length() + (end - start);
        if (remainingSpace <= 0) return;

        input = ChatAllowedCharacters.filterAllowedCharacters(input);
        if (input.length() > remainingSpace)
            input = input.substring(0, remainingSpace);

        this.text = this.text.substring(0, start)
                + input
                + this.text.substring(end);

        this.cursorPosition = start + input.length();
        this.selectionEnd = this.cursorPosition;
        updateLineScrollOffset();
    }

    private int realSelectionStart() { return Math.min(this.cursorPosition, this.selectionEnd); }
    private int realSelectionEnd() { return Math.max(this.cursorPosition, this.selectionEnd); }

    private boolean hasSelectedText() { return this.cursorPosition != this.selectionEnd; }

    private String getSelectedText() {
        return this.text.substring(realSelectionStart(), realSelectionEnd());
    }

    private String getVisibleText() {
        return fontRenderer.trimStringToWidth(this.text.substring(lineScrollOffset), getTextAreaWidth());
    }

    private void updateLineScrollOffset() {
        if (this.cursorPosition < this.lineScrollOffset) {
            this.lineScrollOffset = this.cursorPosition;
        }
        else {
            this.lineScrollOffset += Math.max(0, this.cursorPosition - (getVisibleText().length() + lineScrollOffset));
        }

        while (this.lineScrollOffset > 0 &&
                fontRenderer.getStringWidth(this.text.substring(this.lineScrollOffset - 1)) <= getTextAreaWidth()
        ) {
            this.lineScrollOffset--;
        }
    }

    private boolean isMouseInButton(int mouseX, int mouseY) {
        return GuiUtil.isMouseInRect(mouseX, mouseY, buttonX, top, buttonX + getHeight(), top + getHeight());
    }

    private int getTextAreaWidth() {
        return this.buttonX - this.textAreaX;
    }

    private void drawImage(ResourceLocation image, int left, int sideLength, float colorScale) {
        mc.getTextureManager().bindTexture(image);
        final int offset = (getHeight() - sideLength) / 2;
        GlStateManager.color(colorScale, colorScale, colorScale, 1);
        GuiUtil.drawFullTextureWithCustomSize(left + offset, this.top + offset, sideLength, sideLength);
//        Gui.drawScaledCustomSizeModalRect(left + offset, this.top + offset, 0, 0, textureSideLength, textureSideLength, sideLength, sideLength, textureSideLength, textureSideLength);
    }

    public boolean isExpanded() { return isExpanded; }
}

/*
drawing it the minecraft way:

if (isFocused) {
    final String visibleText = getVisibleText();
    boolean drawCaret = true;
    int caretLeft = textAreaX;
    if (!visibleText.isEmpty()) {
        final int start = realSelectionStart() - lineScrollOffset;
        int selectionStartInVisible = 0;
        if (start > 0) {
            selectionStartInVisible = start;
            caretLeft = fontRenderer.drawString(visibleText.substring(0, start), caretLeft, textTop, ColorUtil.WHITE);
            fontRenderer.drawString(visibleText.substring(start), caretLeft, textTop, ColorUtil.WHITE);
        }
        else {
            fontRenderer.drawString(visibleText, caretLeft, textTop, ColorUtil.WHITE);
        }

        if (hasSelectedText()) {
            drawCaret = false;
            final int selectionEndInVisible = Math.min(realSelectionEnd() - lineScrollOffset, visibleText.length());
            final int right = caretLeft + fontRenderer.getStringWidth(visibleText.substring(selectionStartInVisible, selectionEndInVisible));
            Gui.drawRect(caretLeft, textTop, right, textTop + fontRenderer.FONT_HEIGHT, 0x55FF0000);
        }
    }
    if (drawCaret) {
        final float alphaScale = (float)((Math.sin(System.currentTimeMillis() / 300.0) + 1) / 2);  // 0-1
        final int alpha = (int)(alphaScale * 255);
        final int color = (alpha << 24) | 0x00FFFFFF;
        Gui.drawRect(caretLeft, textTop, caretLeft + 1, textTop + fontRenderer.FONT_HEIGHT, color);
    }
}
else {
    final String renderedText = this.text.isEmpty() ? placeholder : getVisibleText();
    fontRenderer.drawString(renderedText, textAreaX, textTop, ColorUtil.WHITE);
}
*/