package me.shvaich.rcguard.config.gui.elements.custom;

import me.shvaich.rcguard.config.RCGuardConfig;
import me.shvaich.rcguard.config.data.ConfigFieldContainer;
import me.shvaich.rcguard.config.gui.elements.ConfigBooleanButton;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.features.GuardableBlock;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ConfigGuardableBlockButton extends ConfigBooleanButton {

    private final GuardableBlock block;
    private final ItemStack blockItem;

    public ConfigGuardableBlockButton(ConfigScreen screen, ConfigFieldContainer fieldData, GuardableBlock block) throws IllegalAccessException {
        super(screen, fieldData, false);
        this.block = block;
        this.blockItem = new ItemStack(block.mainBlock);
        initialize();
    }

    @Override
    public String getName() {
        return block.displayName;
    }

    @Override
    protected String getComment() {
        return block.comment;
    }

    @Override
    protected int getLeftPadding() {
        return 18 + super.getLeftPadding();
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        super.draw(x, y, mouseX, mouseY);
        final int iconY = y + (drawHeight - 16) / 2;
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(blockItem, x + 4, iconY);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    protected boolean getBoolean() {
        return RCGuardConfig.guardedBlocks.contains(block.key);
    }

    @Override
    protected void toggleBoolean() {
        if (!RCGuardConfig.guardedBlocks.remove(block.key))
            RCGuardConfig.guardedBlocks.add(block.key);
    }
}
