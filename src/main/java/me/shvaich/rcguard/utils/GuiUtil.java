package me.shvaich.rcguard.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiUtil {

    public static final int MOUSE_LEFT = 0;
    public static final int DIGIT_AND_SIGN_WIDTH = 6;

    /// Gui.drawRect(left, top, right, bottom) - left & top: inclusive, right & bottom: exclusive;

    public static void drawHorizontalLine(int startX, int endX, int y, int size, int color) {
        Gui.drawRect(startX, y, endX, y + size, color);
    }

    public static void drawHorizontalLine(int startX, int endX, int y, int color) {
        drawHorizontalLine(startX, endX, y, 1 , color);
    }

    public static void drawVerticalLine(int x, int startY, int endY, int size, int color) {
        Gui.drawRect(x, startY, x + size, endY, color);
    }

    public static void drawVerticalLine(int x, int startY, int endY, int color) {
        drawVerticalLine(x, startY, endY, 1, color);
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }

    public static void drawRectBorder(int left, int top, int right, int bottom, int size, int color) {
        drawVerticalLine(left, top, bottom, size, color);
        drawVerticalLine(right-size, top, bottom, size, color);
        drawHorizontalLine(left, right, top, size, color);
        drawHorizontalLine(left, right, bottom - size, size, color);
    }

    public static void drawRectWithBorder(int left, int top, int right, int bottom, int borderSize, int color, int borderColor) {
        Gui.drawRect(left+borderSize, top+borderSize, right-borderSize, bottom-borderSize, color);
        drawRectBorder(left, top, right, bottom, borderSize, borderColor);
    }

    public static void drawRectWithBorder(int left, int top, int right, int bottom, int color, int borderColor) {
        drawRectWithBorder(left, top, right, bottom, 1, color, borderColor);
    }

    public static void drawFullTextureWithCustomSize(int left, int top, int drawWidth, int drawHeight) {
        Gui.drawModalRectWithCustomSizedTexture(left, top, 0, 0, drawWidth, drawHeight, drawWidth, drawHeight);
    }

    public static boolean isMouseInRect(int mouseX, int mouseY, int left, int top, int right, int bottom) {
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    public static void playButtonPressSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public static void openScreen(GuiScreen screen) {
        new DelayedTask(() -> Minecraft.getMinecraft().displayGuiScreen(screen));
    }

    /** Only works correctly for n in range [-999, 999] */
    public static int getNumberWidth(int n, boolean withPositiveSign, boolean withZeroSign) {
        if (n == 0) return ((withPositiveSign && withZeroSign) ? 2 : 1) * DIGIT_AND_SIGN_WIDTH;
        int sign = 0;
        if (n < 0) {
            n = -n;
            sign = 1;
        }
        else if (withPositiveSign) sign = 1;
        return (sign + (n < 10 ? 1 : (n < 100 ? 2 : 3))) * DIGIT_AND_SIGN_WIDTH;
    }

    /**
     * Equivalent to {@code getNumberWidth(n, false, false)}.
     * @see #getNumberWidth(int, boolean, boolean)
     */
    public static int getNumberWidth(int n) {
        return getNumberWidth(n, false, false);
    }

    /**
     * Begins the stencil clear rect.
     * Call {@link #endClearRect()} after rendering if this returned {@code true}.
     * @return {@code true} if this operation worked
     */
    public static boolean beginClearRect(Minecraft mc, int left, int top, int right, int bottom) {
        if (!mc.getFramebuffer().isStencilEnabled() && !mc.getFramebuffer().enableStencil()) return false;
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);

        GlStateManager.colorMask(false, false, false, false);

        Gui.drawRect(left, top, right, bottom, 0xFFFFFFFF);

        GlStateManager.colorMask(true, true, true, true);

        GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF); // skip hole
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

        return true;
        // endClearRect()
    }

    /** See {@link #beginClearRect(Minecraft, int, int, int, int)} */
    public static void endClearRect() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}