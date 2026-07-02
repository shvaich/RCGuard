package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.config.data.ConfigCategoryContainer;
import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.rcguard.gui.data.ModResources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class ConfigCategoryHeader extends ConfigGuiElement {
    private final String categoryName;
    private final String comment;
    private final boolean hasComment;
    private final List<String> commentLines = new ArrayList<>();

    public ConfigCategoryHeader(String categoryName) {
        this.categoryName = categoryName;
        this.comment = "";
        this.hasComment = false;
    }

    public ConfigCategoryHeader(ConfigCategoryContainer configCategoryContainer) {
        this.categoryName = configCategoryContainer.getCategoryName();
        this.comment = configCategoryContainer.getAnnotation().comment();
        this.hasComment = !comment.isEmpty();
    }

    @Override
    public void onResize(int elementRectWidth) {
        super.onResize(elementRectWidth);
        if (hasComment) {
            commentLines.clear();
            commentLines.addAll(getCommentLines(Minecraft.getMinecraft(), comment, elementRectWidth * 4 / 5));
        }
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        final Minecraft mc = Minecraft.getMinecraft();
        final int centerX = x + elementRectWidth / 2;
        GlStateManager.pushMatrix();
        {
            final int drawX = centerX - mc.fontRendererObj.getStringWidth(this.categoryName);
            GlStateManager.translate(drawX, y, 0);
            GlStateManager.scale(2, 2, 2);
            mc.fontRendererObj.drawStringWithShadow(this.categoryName, 0, 0, ModResources.WHITE);
        }
        GlStateManager.popMatrix();
        if (hasComment) {
            int drawY = y + mc.fontRendererObj.FONT_HEIGHT * 2 + 4;
            for (final String commentLine : commentLines) {
                final int drawX = centerX - mc.fontRendererObj.getStringWidth(commentLine) / 2;
                mc.fontRendererObj.drawStringWithShadow(commentLine, drawX, drawY, ModResources.COMMENT_COLOR);
                drawY += mc.fontRendererObj.FONT_HEIGHT;
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        return false;
    }

    @Override
    public String getSubcategory() {
        return "";
    }

    @Override
    public String getCategory() {
        return categoryName;
    }

    @Override
    public int getHeight() {
        final int fontHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        if (hasComment)
            return (fontHeight * 2 + getBottomMargin()) + commentLines.size() * fontHeight;
        return fontHeight * 2;
    }

    @Override
    public int getBottomMargin() {
        return 8;
    }
}
