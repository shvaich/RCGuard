package me.shvaich.rcguard.commands;

import me.shvaich.rcguard.utils.ChatUtil;
import me.shvaich.rcguard.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

import java.util.Collection;
import java.util.List;

abstract class MyAbstractCommand extends CommandBase {

    /**
     * User text is right-trimmed, every space character adds an empty string to args
     * and every other character is added to the current last string in args.
     * For example:
     * <blockquote><pre>
     *     "/command " -> []
     *     "/command" -> []
     *     "/command  Hello " -> ["", "Hello"]
     * </pre></blockquote>
     */
    protected abstract void onCommand(ICommandSender sender, String[] args);

    /**
     * Only called when there is a space character after "/command".
     * Every space character from the user text adds an empty string to args
     * and every other character is added to the current last string in args.
     * For example:
     * <blockquote><pre>
     *    "/command " -> [""]
     *    "/command" -> (NOT CALLED)
     *    "/command  Hello " -> ["", "Hello", ""]
     * </pre></blockquote>
     */
    protected abstract List<String> onTabComplete(ICommandSender sender, String[] args);

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return '/' + this.getCommandName();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        this.onCommand(sender, args);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return this.onTabComplete(sender, args);
    }

    protected final void sendCommand() {
        sendChatMessage('/' + getCommandName());
    }

    protected final void sendCommand(String arg) {
        sendChatMessage('/' + getCommandName() + " " + arg);
    }

    protected final void sendCommand(String[] args) {
        sendChatMessage('/' + getCommandName() + " " + buildString(args, 0));
    }

    protected static void sendChatMessage(String msg) {
        final EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer != null) {
            thePlayer.sendChatMessage(msg);
        }
    }

    protected static String getHelpBar() { return EnumChatFormatting.GREEN + ChatUtil.bar(); }

    protected static String getHelpHeader(String name) {
        return ChatUtil.centerLine(EnumChatFormatting.GOLD + name + "\n\n");
    }

    protected static IChatComponent getHelpLine(String command, String desc, String shortcut) {
        return getHelpLine(command, desc, command, shortcut);
    }

    protected static IChatComponent getHelpLine(String command, String desc, String commandToPutOnClick, String shortcut) {
        final String shortcutStr = StringUtil.isEmpty(shortcut) ? "" : (
                ". " + EnumChatFormatting.DARK_GRAY + "(" + shortcut + ")"
        );
        return new ChatComponentText(EnumChatFormatting.YELLOW + command + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.AQUA + desc + shortcutStr + "\n")
                .setChatStyle(new ChatStyle()
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Click to put the command in chat.")))
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, commandToPutOnClick))
                );
    }

    protected static boolean isHelpSubcommand(String str) {
        return "h".equals(str) || "help".equals(str);
    }

    protected static String getSkippedMessage(Collection<String> skippedNames, String desc) {
        return EnumChatFormatting.RED + "Skipped (" + skippedNames.size() + ") " + desc + ": " + String.join(", ", skippedNames);
    }
}