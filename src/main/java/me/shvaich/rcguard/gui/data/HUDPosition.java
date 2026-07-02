package me.shvaich.rcguard.gui.data;

import net.minecraft.util.MathHelper;

public class HUDPosition {
    private boolean isEnabled;

    private final double defaultRelativeX, defaultRelativeY;

    private double relativeX, relativeY;
    private int xPosition, yPosition;

    public HUDPosition(boolean isEnabled, double relativeX, double relativeY) {
        this.isEnabled = isEnabled;
        this.defaultRelativeX = relativeX;
        this.defaultRelativeY = relativeY;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
    }

    public void setEnabled(boolean isEnabled) { this.isEnabled = isEnabled; }

    public void setRelativePosition(double relativeX, double relativeY) {
        this.relativeX = relativeX;
        this.relativeY = relativeY;
    }

    public void resetToDefaultPosition() {
        this.relativeX = defaultRelativeX;
        this.relativeY = defaultRelativeY;
    }

    public void setDrawPosition(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    public void updateDrawPositionFromRelative(int screenWidth, int screenHeight) {
        this.xPosition = (int) (relativeX * screenWidth);
        this.yPosition = (int) (relativeY * screenHeight);
    }

    public void savePositionToRelative(int screenWidth, int screenHeight) {
        this.relativeX = MathHelper.clamp_double(this.xPosition / ((double) screenWidth), 0.0D, 1.0D);
        this.relativeY = MathHelper.clamp_double(this.yPosition / ((double) screenHeight), 0.0D, 1.0D);
    }

    public boolean isEnabled() { return isEnabled; }

    public double getRelativeX() { return relativeX; }
    public double getRelativeY() { return relativeY; }

    public int getDrawX() { return xPosition; }
    public int getDrawY() { return yPosition; }
}
