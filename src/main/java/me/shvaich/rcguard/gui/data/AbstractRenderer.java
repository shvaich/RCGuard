package me.shvaich.rcguard.gui.data;

public abstract class AbstractRenderer {
    protected final HUDPosition hudPosition;

    public AbstractRenderer(HUDPosition hudPosition) {
        this.hudPosition = hudPosition;
    }

    public HUDPosition getHUDPosition() {
        return hudPosition;
    }

    public abstract boolean isEnabled(long currentTimeMillis);

    public abstract void render(int screenWidth, int screenHeight, int thisWidth, int thisHeight);

    public abstract int getWidth();

    public abstract int getHeight();

    public boolean shouldRenderDummy() { return hudPosition.isEnabled(); }

    public abstract void renderDummy(int screenWidth, int screenHeight, int thisWidth, int thisHeight);

    public int getDummyWidth() { return getWidth(); }

    public int getDummyHeight() { return getHeight(); }
}
