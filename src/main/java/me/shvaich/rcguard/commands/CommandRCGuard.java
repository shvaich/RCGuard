package me.shvaich.rcguard.commands;

import me.shvaich.rcguard.RCGuard;
import me.shvaich.rcguard.config.RCGuardConfig;
import me.shvaich.rcguard.utils.ChatUtil;
import me.shvaich.rcguard.utils.GuiUtil;
import me.shvaich.rcguard.utils.StringUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandRCGuard extends MyAbstractCommand {
    @Override
    public String getCommandName() {
        return "rcguard";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("rcg", "rightclickguard");
    }

    @Override
    protected void onCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            final String subcommand = StringUtil.toLowerCase(args[0]);
            if (isHelpSubcommand(subcommand)) {
                final String slashCommand = '/' + getCommandName();
                final IChatComponent msg = new ChatComponentText(getHelpBar() + "\n" + getHelpHeader(RCGuard.NAME + " Help"))
                        .appendSibling(getHelpLine(slashCommand, "Opens the configuration GUI", "rcg"))
                        .appendSibling(getHelpLine(slashCommand + " shovel", "Toggles ShovelGuard", "s"))
                        .appendSibling(getHelpLine(slashCommand + " block", "Toggles BlockGuard", "b"))
                        .appendText("\n" + ChatUtil.centerLine(EnumChatFormatting.GRAY + "Some commands have a shortcut. Shortcuts are shown in parentheses after the description\n") + getHelpBar());

                ChatUtil.addChatMessage(msg);
                return;
            }

            boolean value = false;
            String name = null;
            String guard = null;
            boolean isGuard = true;

            if (subcommand.equals("b") || subcommand.equals("block")) {
                value = RCGuardConfig.areBlocksGuarded = !RCGuardConfig.areBlocksGuarded;
                name = "areBlocksGuarded";
                guard = RCGuardConfig.BLOCK_GUARD;
            }
            else if (subcommand.equals("s") || subcommand.equals("shovel")) {
                value = RCGuardConfig.isShovelGuarded = !RCGuardConfig.isShovelGuarded;
                name = "isShovelGuarded";
                guard = RCGuardConfig.SHOVEL_GUARD;
            }
            else isGuard = false;

            if (isGuard) {
                ChatUtil.addChatMessage(ChatUtil.getModTag() + guard + ": " + ChatUtil.getBooleanMsg(value));
                RCGuardConfig.instance().saveOnlyOneProperty(name);
                return;
            }
        }
        GuiUtil.openScreen(RCGuardConfig.instance().getConfigGuiScreen());
    }

    @Override
    protected List<String> onTabComplete(ICommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
