package me.shvaich.rcguard.config.data;

import me.shvaich.rcguard.RCGuard;
import me.shvaich.rcguard.config.gui.elements.*;
import me.shvaich.rcguard.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.gui.data.HUDPosition;
import me.shvaich.rcguard.utils.ColorUtil;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConfigFieldContainer {

    private static final boolean FORCE_SHOW_HIDDEN = Boolean.getBoolean("config.showHidden");

    private final Field field;
    private final ConfigProperty annotation;
    private final Property[] configProperties;
    private Consumer<Object> action;
    private ConfigFieldContainer dependsOn;
    private int dependantsCount = 0;
    private BooleanSupplier hideCondition;

    public ConfigFieldContainer(Field field, Configuration config, Map<String, Set<String>> configFieldsNamesMap) throws IllegalAccessException {
        final ConfigProperty annotation = getConfigProperty(field);

        final String category = annotation.category();
        if (category.isEmpty())
            throw new IllegalArgumentException("Category isn't allowed to be empty. At field: " + annotation.name());

        if (!configFieldsNamesMap.computeIfAbsent(category, t -> new HashSet<>()).add(annotation.name()))
            throw new IllegalStateException("Field " + annotation.name() + " already exists in category " + category);

        this.field = field;
        this.annotation = annotation;
        this.configProperties = loadConfigValueToField(config);
    }

    private static ConfigProperty getConfigProperty(Field field) {
        final ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
        if (!annotation.type().isFieldValid(field))
            throw new IllegalArgumentException("Field " + field.getName() + " is invalid. " + annotation.type() + " is incompatible with " + field.getType());

        switch (annotation.type()) {
            case SELECTOR: {
                if (annotation.options().length == 0)
                    throw new IllegalArgumentException("Field " + field.getName() + " is invalid. SELECTOR must have at least one option");
                break;
            }

            case NUMBER:
            case SLIDER: {
                if (annotation.min() >= annotation.max())
                    throw new IllegalArgumentException("Field " + field.getName() + " is invalid. " + annotation.type().name() + "'s minimum value must be smaller than the maximum value");
                break;
            }
        }
        return annotation;
    }

    private Property[] loadConfigValueToField(Configuration config) throws IllegalAccessException {
        final String category = getCategory();
        final String name = getName();
        if (field.getType() == boolean.class) {
            final Property prop = config.get(category, name, (boolean) getValue());
            field.setBoolean(null, prop.getBoolean());
            return new Property[]{prop};
        }
        else if (field.getType() == int.class) {
            final Property prop = config.get(category, name, (int) getValue());
            field.setInt(null, getValidIntFromConfig(prop));
            return new Property[]{prop};
        }
        else if (field.getType() == HUDPosition.class) {
            final HUDPosition hudPosition = (HUDPosition) getValue();
            final Property isEnabledProp = config.get(category, "Show " + name, hudPosition.isEnabled());
            final Property relativeXProp = config.get(category, name + " Xpos", hudPosition.getRelativeX());
            final Property relativeYProp = config.get(category, name + " Ypos", hudPosition.getRelativeY());
            hudPosition.setEnabled(isEnabledProp.getBoolean());
            hudPosition.setRelativePosition(
                MathHelper.clamp_double(relativeXProp.getDouble(), 0.0D, 1.0D),
                MathHelper.clamp_double(relativeYProp.getDouble(), 0.0D, 1.0D)
            );
            return new Property[]{isEnabledProp, relativeXProp, relativeYProp};
        }
        else if (isFieldStringCollection()) {
            // noinspection unchecked
            final Collection<String> collection = (Collection<String>) getValue();
            final String[] keys = collection.stream().map(Object::toString).toArray(String[]::new);
            final Property prop = config.get(category, name, keys);
            collection.clear();
            collection.addAll(Arrays.asList(prop.getStringList()));
            return new Property[]{prop};
        }
        else throw new IllegalStateException("Field type (" + field.getType() + ") isn't supported");
    }

    private int getValidIntFromConfig(Property prop) {
        final int configValue = prop.getInt();
        switch (getType()) {
            case COLOR: return (
                (!annotation.allowsTransparency() && ColorUtil.isOpaque(Integer.parseInt(prop.getDefault())))
                ? ColorUtil.toOpaque(configValue) : configValue
            );

            case SELECTOR: return MathHelper.clamp_int(configValue, 0, annotation.options().length-1);

            case NUMBER:
            case SLIDER: return MathHelper.clamp_int(configValue, annotation.min(), annotation.max());

            default: return configValue;
        }
    }

    private Property theConfigProperty() { return configProperties[0]; }

    public void saveFieldValueToConfig() throws IllegalAccessException {
        if (field.getType() == boolean.class) {
            theConfigProperty().set((boolean) getValue());
        }
        else if (field.getType() == int.class) {
            theConfigProperty().set((int) getValue());
        }
        else if (field.getType() == HUDPosition.class) {
            final HUDPosition hudPosition = (HUDPosition) getValue();
            configProperties[0].set(hudPosition.isEnabled());
            configProperties[1].set(hudPosition.getRelativeX());
            configProperties[2].set(hudPosition.getRelativeY());
        }
        else if (isFieldStringCollection()) {
            // noinspection unchecked
            final Collection<String> collection = (Collection<String>) getValue();
            theConfigProperty().set(collection.toArray(new String[0]));
        }
        else throw new IllegalStateException("Field type (" + field.getType() + ") isn't supported");
    }

    public ConfigGuiElement[] getConfigButtons(ConfigScreen screen) throws IllegalAccessException {
        if (isHidden()) return null;
        final ConfigGuiElement singleButton;
        switch (getType()) {
            case SWITCH: singleButton = new ConfigBooleanButton(screen, this); break;

            case COLOR: {
                final int defaultColor = Integer.parseInt(theConfigProperty().getDefault());
                singleButton = new ConfigColorButton(screen, this, defaultColor);
            } break;

            case SELECTOR: singleButton = new ConfigSelectorButton(screen, this); break;

            case HUD_POSITION: singleButton = new ConfigHUDButton(screen, this); break;

            case SLIDER: singleButton = new ConfigSliderButton(screen, this); break;

            case NUMBER: singleButton = new ConfigNumberButton(screen, this); break;

            case CUSTOM: {
                try {
                    final Constructor<? extends CustomPropertyInfo> constructor = annotation.customPropertyClass().getDeclaredConstructor();
                    constructor.setAccessible(true);
                    return constructor.newInstance().getConfigGuiButtons(screen, this);
                }
                catch (Exception e) { throw new RuntimeException("Failed to get ConfigButtons from custom property info", e); }
            }

            default: throw new IllegalArgumentException("Config doesn't support GUI for this field. (" + annotation.type() + ")");
        }
        return new ConfigGuiElement[]{ singleButton };
    }

    public void setValue(Object value) throws IllegalAccessException {
        if (value == null) {
            RCGuard.LOGGER.error("Attempted to set field value to null. not allowed");
            return;
        }
        final Object previousValue = action != null ? getValue() : null;
        field.set(null, value);
        invokeAction(previousValue);
    }

    public Object getValue() throws IllegalAccessException {
        return field.get(null);
    }

    public boolean getBoolean() throws IllegalAccessException {
        if (getType() == PropertyType.HUD_POSITION) {
            return ((HUDPosition) getValue()).isEnabled();
        }
        return (boolean) getValue();
    }

    public void invokeAction(Object previousValue) {
        if (action != null) action.accept(previousValue);
    }

    public ConfigProperty getAnnotation() { return annotation; }

    public boolean isHidden() { return !FORCE_SHOW_HIDDEN && (annotation.isHidden() || (hideCondition != null && hideCondition.getAsBoolean())); }

    public PropertyType getType() { return annotation.type(); }
    public String getCategory() { return annotation.category(); }
    public String getName() { return annotation.name(); }
    public String getSubcategory() { return annotation.subcategory(); }

    public void setAction(Consumer<Object> action) {
        if (this.action != null)
            throw new IllegalStateException("Field already has an action!");
        this.action = action;
    }

    public void setHideCondition(BooleanSupplier hideCondition) {
        if (this.hideCondition != null)
            throw new IllegalStateException("Field already has a 'hideCondition'");
        this.hideCondition = hideCondition;
    }

    public void setDependsOn(ConfigFieldContainer dependsOn) {
        // maybe allow null in the future
        if (dependsOn == null || (dependsOn.getType() != PropertyType.SWITCH && dependsOn.getType() != PropertyType.HUD_POSITION))
            throw new IllegalArgumentException("Invalid dependency");

        if (dependsOn == this)
            throw new IllegalArgumentException("Field cannot depend on itself (field is a loser)");

        if (this.dependsOn == dependsOn) return;

        // start at dependsOn's 'father' because we already know: dependsOn != null && dependsOn != this
        ConfigFieldContainer parentDependency = dependsOn.dependsOn;
        while (parentDependency != null) {
            if (parentDependency == this)
                throw new IllegalArgumentException("Dependency cycle detected: " + dependsOn.getFieldName() + " -> ... -> " + this.getFieldName());
            parentDependency = parentDependency.dependsOn;
        }

        if (this.dependsOn != null && ((--this.dependsOn.dependantsCount) < 0))
            this.dependsOn.dependantsCount = 0;

        this.dependsOn = dependsOn;
        this.dependsOn.dependantsCount++;
    }

    public boolean hasDependants() { return dependantsCount != 0; }

    public ConfigFieldContainer getDependsOn() { return dependsOn; }

    public String getFieldName() { return field.getName(); }

    private boolean isFieldStringCollection() {
        if (Collection.class.isAssignableFrom(field.getType())) {
            final Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                final Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
                return args.length == 1 && args[0] == String.class;
            }
        }
        return false;
    }
}