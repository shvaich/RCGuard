package me.shvaich.rcguard.config.gui.elements.base;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigGuiElement {
    protected int elementRectWidth;

    public abstract void draw(int x, int y, int mouseX, int mouseY);

    public void onResize(int elementRectWidth) { this.elementRectWidth = elementRectWidth; }

    public abstract int getHeight();

    public abstract boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException;

    public abstract String getCategory();

    public abstract String getSubcategory();

    public boolean matchSearch(String search) { return false; }

    public List<String> getHoveringTextLines(int mouseX, int mouseY) {
        return null;
    }

    public int getTopMargin() { return 0; }

    public int getBottomMargin() { return 4; }

    protected final List<String> getCommentLines(Minecraft mc, String comment, int wrapWidth) {
        final List<String> commentLines = new ArrayList<>();
        if (wrapWidth > 0) {
            for (final String line : comment.split("\n")) {
                commentLines.addAll(mc.fontRendererObj.listFormattedStringToWidth(line, wrapWidth));
            }
        }
        return commentLines;
    }
}