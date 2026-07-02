package me.shvaich.rcguard.config.data;

import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;

public abstract class CustomPropertyInfo {
    public abstract ConfigGuiElement[] getConfigGuiButtons(ConfigScreen screen, ConfigFieldContainer fieldData);
}
