package me.shvaich.rcguard.utils;

import me.shvaich.rcguard.RCGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import static net.minecraft.util.EnumChatFormatting.*;

public class ChatUtil {
    public static String getModTag() {
        return DARK_GRAY + "[" + YELLOW + RCGuard.NAME + DARK_GRAY + "] " + RESET;
    }

    public static String getBooleanMsg(boolean bool) {
        return bool ? GREEN + "Enabled" : RED + "Disabled";
    }

    public static String getBooleanMsg(boolean bool, boolean isActive) {
        final String msg = getBooleanMsg(bool);
        if (isActive) return msg;
        final char[] chars = msg.toCharArray();
        chars[1] = '7';
        return new String(chars);
    }

    public static void addChatMessage(String msg) {
        addChatMessage(new ChatComponentText(msg));
    }

    public static void addChatMessage(IChatComponent msg) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(msg);
        }
        else {
            RCGuard.LOGGER.info("Failed to add chat message: {}", msg.getUnformattedText());
        }
    }

    public static void addErrorMessage(String msg) {
        addErrorMessage(msg, true);
    }

    public static void addErrorMessage(String msg, boolean withModTag) {
        addChatMessage((withModTag ? getModTag() : "") + RED + msg);
    }

    public static String bar() {
        return STRIKETHROUGH + repeatToChatWidth('-');
    }

    public static String centerLine(String msg) {
        return getSpaceToCenter(msg) + msg;
    }

    public static String getSpaceToCenter(String msg) {
        final Minecraft mc = Minecraft.getMinecraft();
        final int chatWidth = mc.ingameGUI.getChatGUI().getChatWidth();
        final int msgWidth = mc.fontRendererObj.getStringWidth(msg);
        return msgWidth >= chatWidth ? "" : getSpaceOfLength((chatWidth - msgWidth) / 2);
    }

    public static String getSpaceOfLength(int len) {
        final char space = ' ';
        final int n = len / Minecraft.getMinecraft().fontRendererObj.getCharWidth(space);
        return n < 1 ? "" : StringUtil.repeat(space, n);
    }

    public static String repeatToChatWidth(char ch) {
        final Minecraft mc = Minecraft.getMinecraft();
        return StringUtil.repeat(ch, mc.ingameGUI.getChatGUI().getChatWidth() / mc.fontRendererObj.getCharWidth(ch));
    }
}
