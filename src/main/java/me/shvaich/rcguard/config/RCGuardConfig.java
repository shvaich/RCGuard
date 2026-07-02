package me.shvaich.rcguard.config;

import me.shvaich.rcguard.config.data.*;
import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.rcguard.config.gui.elements.custom.ConfigGuardableBlockButton;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.features.GuardableBlock;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RCGuardConfig extends AbstractConfig {

    public static final String BLOCK_GUARD = "BlockGuard";

    @ConfigCategory(
        comment = "Designed for Moleman players in Hypixel Mega Walls"
    )
    public static final String SHOVEL_GUARD = "ShovelGuard";

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Enable Block Guard",
            category = BLOCK_GUARD
    )
    public static boolean areBlocksGuarded = false;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Sneak Guard",
            category = BLOCK_GUARD,
            subcategory = "Behaviour",
            comment = "Prevents interaction with guarded blocks while sneaking with an empty hand"
    )
    public static boolean guardSneakAndEmptyHandClicks = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Allow Pickaxe Override",
            category = BLOCK_GUARD,
            subcategory = "Behaviour",
            comment = "Allows interactions with guarded blocks while holding a pickaxe"
    )
    public static boolean canPickaxeOverrideBlockGuard = false;

    private static class GuardedBlocksInfo extends CustomPropertyInfo {
        @Override
        public ConfigGuiElement[] getConfigGuiButtons(ConfigScreen screen, ConfigFieldContainer fieldData) {
            try {
                final GuardableBlock[] values = GuardableBlock.values();
                final int len = values.length;
                final ConfigGuardableBlockButton[] buttons = new ConfigGuardableBlockButton[len];
                for (int i = 0; i < len; i++)
                    buttons[i] = new ConfigGuardableBlockButton(screen, fieldData, values[i]);
                return buttons;
            }
            catch (Exception e) { throw new RuntimeException("Failed to create GuardableBlock buttons", e); }
        }
    }
    @ConfigProperty(
            type = PropertyType.CUSTOM,
            name = "Guarded Blocks",
            category = BLOCK_GUARD,
            subcategory = "Guardable Blocks",
            customPropertyClass = GuardedBlocksInfo.class
    )
    public static final Set<String> guardedBlocks = new HashSet<>(Arrays.stream(GuardableBlock.values()).map(o -> o.key).collect(Collectors.toSet()));
    //public static final Set<GuardableBlock> guardedBlocks = new HashSet<>(Arrays.asList(GuardableBlock.values()));


    @ConfigProperty(
            type = PropertyType.SWITCH,
            category = SHOVEL_GUARD,
            name = "Enable Shovel Guard"
    )
    public static boolean isShovelGuarded = false;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Entity Right-Click Guard",
            category = SHOVEL_GUARD,
            subcategory = "Behaviour",
            comment = "If enabled, guard all entity right-clicks"
    )
    public static boolean guardShovelEntityInteraction = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Smart Guard",
            category = SHOVEL_GUARD,
            subcategory = "Behaviour",
            comment = "Don't guard right-clicks on unguarded blocks"
                    + "\n§e(disabling this can cause unintended behaviour)"
    )
    public static boolean canOverrideShovelGuard = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show " + SHOVEL_GUARD + " HUD",
            category = SHOVEL_GUARD,
            subcategory = "HUD"
    )
    public static boolean renderShovelGuardHUD = false;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            category = SHOVEL_GUARD,
            subcategory = "HUD",
            name = SHOVEL_GUARD + " HUD Style",
            options = { "Background", "Line-Over" }
    )
    public static int shovelGuardHudStyle = 0;

    @ConfigProperty(
            type = PropertyType.COLOR,
            category = SHOVEL_GUARD,
            subcategory = "HUD",
            name = SHOVEL_GUARD + " HUD Color"
    )
    public static int shovelGuardHudColor = 0x25FF0000;


    private static RCGuardConfig instance;

    public static void loadConfig(File file) {
        if (instance != null)
            throw new IllegalStateException("Config already created");

        instance = new RCGuardConfig(file);
    }

    public static RCGuardConfig instance() { return instance; }

    private RCGuardConfig(File file) {
        super(file);

        Arrays.asList(
                "guardSneakAndEmptyHandClicks",
                "canPickaxeOverrideBlockGuard",
                "guardedBlocks"
        ).forEach(property -> addDependency(property, "areBlocksGuarded"));


        Arrays.asList(
                "guardShovelEntityInteraction",
                "canOverrideShovelGuard",
                "renderShovelGuardHUD"
        ).forEach(property -> addDependency(property, "isShovelGuarded"));

        addDependency("shovelGuardHudStyle", "renderShovelGuardHUD");
        addDependency("shovelGuardHudColor", "renderShovelGuardHUD");
    }
}