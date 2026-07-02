package me.shvaich.rcguard;

import me.shvaich.rcguard.commands.CommandRCGuard;
import me.shvaich.rcguard.config.RCGuardConfig;
import me.shvaich.rcguard.events.KeybindingListener;
import me.shvaich.rcguard.events.ModAnnouncement;
import me.shvaich.rcguard.events.RightClickListener;
import me.shvaich.rcguard.gui.data.HUDManager;
import me.shvaich.rcguard.gui.huds.ShovelGuardHUD;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;


@Mod(
    modid = RCGuard.MOD_ID,
    name = RCGuard.NAME,
    version = RCGuard.VERSION,
    acceptedMinecraftVersions = RCGuard.ACCEPTED_MC_VERSIONS,
    clientSideOnly = RCGuard.CLIENT_SIDE_ONLY
)
public class RCGuard {
    public static final String MOD_ID = "@MOD_ID@";
    public static final String NAME = "@MOD_NAME@";
    public static final String VERSION = "@MOD_VERSION@";
    public static final String ACCEPTED_MC_VERSIONS = "@ACCEPTED_MINECRAFT_VERSIONS@";
    public static final boolean CLIENT_SIDE_ONLY = true;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        RCGuardConfig.loadConfig(new File(e.getModConfigurationDirectory(), MOD_ID + ".cfg"));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        registerCommands(
                new CommandRCGuard()
        );

        registerEvents(
                new ModAnnouncement(),
                new KeybindingListener(),
                new RightClickListener(),
                new ShovelGuardHUD()
        );

        HUDManager.register();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {}

    private void registerCommands(ICommand... commands) {
        for (final ICommand command : commands) {
            ClientCommandHandler.instance.registerCommand(command);
        }
    }

    private void registerEvents(Object... events) {
        for (final Object event : events) {
            MinecraftForge.EVENT_BUS.register(event);
        }
    }
}