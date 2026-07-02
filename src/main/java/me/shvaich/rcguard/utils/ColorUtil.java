package me.shvaich.rcguard.utils;

public class ColorUtil {

    public static int getAlpha(int color) { return (color >> 24) & 0xFF; }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int toOpaque(int color) {
        return color | 0xFF000000;
    }

    public static boolean isOpaque(int color) {
        return ((color >> 24) & 0xFF) == 0xFF;
    }

    public static int getColorWithUpdatedChannel(int color, int channel, int channelValue) {
        final int shiftBy = getColorChannelBitShift(channel);
        final int mask = 0xFF << shiftBy;
        return (color & ~mask) | ((channelValue & 0xFF) << shiftBy);
    }

    public static int getColorChannelValue(int color, int channel) {
        return (color >> getColorChannelBitShift(channel)) & 0xFF;
    }

    public static int getColorChannelBitShift(int channel) {
        if (channel < 0 || channel > 3)
            throw new IllegalArgumentException("Argument must be one of: (0, 1, 2, 3)");
        return channel == 3 ? 24 : (16 - (channel << 3)); // 8 * (2-channel)
    }
}
