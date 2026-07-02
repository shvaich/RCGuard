package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.Minecraft;

public class ConfigSubcategoryHeader extends ConfigGuiElement {
    private final String categoryName;
    private final String subcategoryName;

    public ConfigSubcategoryHeader(String categoryName, String subcategoryName) {
        this.categoryName = categoryName;
        this.subcategoryName = subcategoryName;
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        final Minecraft mc = Minecraft.getMinecraft();
        final int textWidth = mc.fontRendererObj.getStringWidth(subcategoryName);
        final int textX = x + (elementRectWidth - textWidth) / 2;
        final int lineY = y + mc.fontRendererObj.FONT_HEIGHT / 2;
        GuiUtil.drawHorizontalLine(x, textX - 4, lineY, ModResources.ELEMENT_BORDER);
        GuiUtil.drawHorizontalLine(textX + textWidth + 4, x + elementRectWidth, lineY, ModResources.ELEMENT_BORDER);
        mc.fontRendererObj.drawStringWithShadow(subcategoryName, textX, y, ModResources.GRAYISH);
    }

    @Override
    public int getHeight() {
        return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        return false;
    }

    @Override
    public String getCategory() {
        return categoryName;
    }

    @Override
    public String getSubcategory() {
        return subcategoryName;
    }

    @Override
    public int getTopMargin() {
        return 6;
    }
}
