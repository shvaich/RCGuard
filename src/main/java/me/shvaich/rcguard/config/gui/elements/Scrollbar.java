package me.shvaich.rcguard.config.gui.elements;

import me.shvaich.rcguard.gui.data.ModResources;
import me.shvaich.rcguard.utils.GuiUtil;

public class Scrollbar {

    private final int allElementsHeight;
    private final int trackHeight;
    private final int thumbHeight;
    private final int trackTop, trackBottom;

    private int LEFT, RIGHT, TOP, BOTTOM;

    private int scroll;
    private boolean isDragging;
    private int grabbedAtY;
    private int scrollDirection;
    private int amountToScroll;
    private long lastScrolledAt;

    private Scrollbar(int allElementsHeight, int trackTop, int trackBottom) {
        this.allElementsHeight = allElementsHeight;
        this.trackTop = trackTop;
        this.trackBottom = trackBottom;
        this.trackHeight = trackBottom - trackTop;
        this.thumbHeight = (int) (this.trackHeight * ((float) this.trackHeight / allElementsHeight));
    }

    public static Scrollbar create(int allElementsHeight, int top, int bottom) {
        return create(allElementsHeight, top, bottom, null);
    }

    public static Scrollbar create(int allElementsHeight, int top, int bottom, Scrollbar previous) {
        if (allElementsHeight <= (bottom - top)) return null;
        final Scrollbar created = new Scrollbar(allElementsHeight, top, bottom);
        if (previous != null) {
            created.isDragging = previous.isDragging;
            created.grabbedAtY = previous.grabbedAtY;
            created.lastScrolledAt = previous.lastScrolledAt;
            created.updateScroll(previous.scroll);
        }
        return created;
    }

    public void draw(int left, int right, int mouseY) {
        // final int minY = this.trackTop;
        final int maxY = this.trackBottom - this.thumbHeight;
        if (this.isDragging) {
            final int relativeMouseY = mouseY - this.trackTop - this.grabbedAtY;
            final int newScroll = relativeMouseY * (this.allElementsHeight - this.trackHeight) / (maxY - this.trackTop);
            updateScroll(newScroll);
        }
        this.LEFT = left;
        this.RIGHT = right;
        this.TOP = this.trackTop + (int) ((float)this.scroll / (this.allElementsHeight - this.trackHeight) * (maxY - this.trackTop));
        this.BOTTOM = this.TOP + this.thumbHeight;
        GuiUtil.drawRect(this.LEFT, this.TOP, this.RIGHT, this.BOTTOM, ModResources.SCROLLBAR_COLOR);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == GuiUtil.MOUSE_LEFT && GuiUtil.isMouseInRect(mouseX, mouseY, LEFT, TOP, RIGHT, BOTTOM)) {
            this.isDragging = true;
            this.grabbedAtY = mouseY - TOP;
            return true;
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == GuiUtil.MOUSE_LEFT && this.isDragging)
            this.isDragging = false;
    }

    public void updateFromMouseScroll(int eventDWheel) {
        if (eventDWheel != 0) {
            this.scrollDirection = eventDWheel > 0 ? -1 : 1;
            this.amountToScroll = 100;
        }
    }

    public void smoothScroll() {
        if (this.amountToScroll > 0) {
            final long time = System.currentTimeMillis();
            if (time - this.lastScrolledAt > 1) {
                this.amountToScroll -= 3;
                updateScroll(scroll + (scrollDirection * 3));
                this.lastScrolledAt = time;
            }
        }
    }

    private void updateScroll(int scroll) {
        this.scroll = scroll;
        if (this.scroll > (this.allElementsHeight - this.trackHeight)) {
            this.scroll = this.allElementsHeight - this.trackHeight;
            this.amountToScroll = 0;
        }
        if (this.scroll <= 0) {
            this.scroll = 0;
            this.amountToScroll = 0;
        }
    }

    public int getScroll() { return this.scroll; }

    public int getTrackBottom() { return this.trackBottom; }
}
