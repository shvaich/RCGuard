package me.shvaich.rcguard.gui.huds;

import me.shvaich.rcguard.config.RCGuardConfig;
import me.shvaich.rcguard.utils.ColorUtil;
import me.shvaich.rcguard.utils.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ShovelGuardHUD {

    @SubscribeEvent
    public void onRenderGUI(RenderGameOverlayEvent.Post e) {
        if (e.type != ElementType.HOTBAR || !isRenderingShovelGuardHUD()) return;
        final int hotbarX = e.resolution.getScaledWidth() / 2 - 91 + 3;
        final int hotbarY = e.resolution.getScaledHeight() - 16 - 3;
        final boolean renderLineOver = RCGuardConfig.shovelGuardHudStyle == 1;
        final ItemStack[] inventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        if (renderLineOver) { GlStateManager.disableDepth(); }
        for (int i = 0; i < 9; ++i) {
            final ItemStack inventoryItem = inventory[i];
            if (inventoryItem != null && inventoryItem.getItem() instanceof ItemSpade) {
                final int x = hotbarX + i * 20;
                if (renderLineOver) {
                    GlStateManager.pushMatrix();
                    {
                        GlStateManager.translate(x + 8, hotbarY + 8, 0);
                        GlStateManager.rotate(45, 0, 0, 1);
                        GuiUtil.drawHorizontalLine(-7, 7, -1, 2, RCGuardConfig.shovelGuardHudColor);
                    }
                    GlStateManager.popMatrix();
                }
                else {
                    Gui.drawRect(x, hotbarY, x + 16, hotbarY + 16, RCGuardConfig.shovelGuardHudColor);
                }
            }
        }
        if (renderLineOver) { GlStateManager.enableDepth(); }
        GlStateManager.disableBlend();
    }

    private static boolean isRenderingShovelGuardHUD() {
        return RCGuardConfig.isShovelGuarded
                && RCGuardConfig.renderShovelGuardHUD
                && (ColorUtil.getAlpha(RCGuardConfig.shovelGuardHudColor) > 3);
    }
}
