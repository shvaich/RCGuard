package me.shvaich.rcguard.config.data;

import me.shvaich.rcguard.RCGuard;
import me.shvaich.rcguard.config.gui.screens.ConfigScreen;
import me.shvaich.rcguard.gui.data.HUDPosition;
import me.shvaich.rcguard.utils.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class AbstractConfig {
    @ConfigProperty(
        type = PropertyType.SELECTOR,
        category = "General",
        subcategory = "User Interface",
        name = "Behaviour when Dependency is Disabled",
        options = { "Normal", "Hidden", "Inactive" },
        comment = "Controls how a property is displayed when a property it depends on is disabled"
    )
    private static int inactiveFieldsDisplayType = 0;

    private final Configuration config;
    private final LinkedHashMap<String, LinkedHashMap<String, List<String>>> configStructure = new LinkedHashMap<>();
    private final List<ConfigCategoryContainer> configCategories = new ArrayList<>();
    private final List<ConfigFieldContainer> configFields = new ArrayList<>();
    private final boolean addedInternalProperties;

    protected AbstractConfig(File file) { this(file, true); }

    protected AbstractConfig(File file, boolean addInternalProperties) {
        this.addedInternalProperties = addInternalProperties;
        this.config = new Configuration(file);
        config.load();
        final Map<String, Set<String>> configFieldsNamesMap = new HashMap<>();
        final ConfigFieldContainer inactiveFieldsDisplayTypeField;
        try {
            for (final Field field : this.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    field.setAccessible(true);
                    configFields.add(new ConfigFieldContainer(field, config, configFieldsNamesMap));
                }
                else if (field.isAnnotationPresent(ConfigCategory.class)) {
                    field.setAccessible(true);
                    configCategories.add(new ConfigCategoryContainer(field));
                }
            }

            if (addInternalProperties) {
                Field field = AbstractConfig.class.getDeclaredField("inactiveFieldsDisplayType");
                field.setAccessible(true);
                inactiveFieldsDisplayTypeField = new ConfigFieldContainer(field, config, configFieldsNamesMap);
                configFields.add(inactiveFieldsDisplayTypeField);
            }
            else { inactiveFieldsDisplayTypeField = null; }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create config", e);
        }
        try {
            readConfigFieldsInDeclarationOrder(configFieldsNamesMap);
            if (inactiveFieldsDisplayTypeField != null)
                addFieldToStructure(inactiveFieldsDisplayTypeField.getCategory(), inactiveFieldsDisplayTypeField.getSubcategory(), inactiveFieldsDisplayTypeField.getName());
        }
        catch (Exception e) {
            configStructure.clear();
            readConfigFieldsInAlphabeticalOrder();
            RCGuard.LOGGER.error("Failed to sort in declaration order", e);
        }
        setConfigPropertiesOrder();
        if (config.hasChanged()) config.save();

        if (inactiveFieldsDisplayTypeField != null) {
            addListener(inactiveFieldsDisplayTypeField, (Object previousValue) -> {
                final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                if (screen instanceof ConfigScreen)
                    ((ConfigScreen) screen).onDependencyDisplayTypeChange(inactiveFieldsDisplayType);
            });

            hidePropertyIf(inactiveFieldsDisplayTypeField, () -> {
                for (final ConfigFieldContainer field : configFields) {
                    if (field != inactiveFieldsDisplayTypeField && !field.isHidden())
                        return false;
                }
                return true;
            });
        }
    }

    public void save() {
        try {
            for (final ConfigFieldContainer field : configFields)
                field.saveFieldValueToConfig();
        }
        catch (Exception e) {
            ChatUtil.addErrorMessage("Failed to save some config!");
            RCGuard.LOGGER.error("Failed in updating config fields", e);
        }
        if (config.hasChanged()) config.save();
    }

    public void saveOnlyOneProperty(String name) {
        final ConfigFieldContainer field = getConfigFieldOrThrow(name);
        try {
            field.saveFieldValueToConfig();
        }
        catch (Exception e) {
            ChatUtil.addErrorMessage("Failed to save config value!");
            RCGuard.LOGGER.error("Failed in updating config field", e);
        }
        if (config.hasChanged()) config.save();
    }

    public GuiScreen getConfigGuiScreen() {
        try {
            return new ConfigScreen(this,
                    configCategories,
                    configFields,
                    configStructure,
                    addedInternalProperties ? inactiveFieldsDisplayType : 0
            );
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create config GUI", e);
        }
    }

    private void readConfigFieldsInDeclarationOrder(Map<String, Set<String>> configFieldsNamesMap) throws IOException {
        final Class<?> clazz = this.getClass();
        final InputStream is = clazz.getResourceAsStream('/' + clazz.getName().replace('.', '/') + ".class");
        if (is == null)
            throw new RuntimeException("failed to find config class resource");
        final ClassReader cr = new ClassReader(is);
        final ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_CODE);
        final String annotationDesc = Type.getDescriptor(ConfigProperty.class);
        final String hudPositionDesc = Type.getDescriptor(HUDPosition.class);
        for (final FieldNode fieldNode : cn.fields) {
            if (fieldNode.visibleAnnotations == null) continue;
            for (final AnnotationNode annotationNode : fieldNode.visibleAnnotations) {
                if (!annotationNode.desc.equals(annotationDesc)) continue;
                String configCategory = null;
                String configSubcategory = "";
                String configName = null;
                for (int i = 0; i < annotationNode.values.size(); i += 2) {
                    final String name = (String) annotationNode.values.get(i);
                    final Object value = annotationNode.values.get(i+1);
                    if ("category".equals(name) && value instanceof String)
                        configCategory = (String) value;
                    else if ("subcategory".equals(name) && value instanceof  String)
                        configSubcategory = (String) value;
                    else if ("name".equals(name) && value instanceof String)
                        configName = (String) value;
                }
                if (configCategory != null && configName != null) {
                    // (!configFieldsNamesMap.containsKey(configCategory) || !configFieldsNamesMap.get(configCategory).contains(configName))
                    if (!configFieldsNamesMap.computeIfAbsent(configCategory, t -> new HashSet<>()).contains(configName))
                        throw new IllegalStateException("config fields declaration order fuckup #1");

                    final List<String> subcategoryFields = configStructure.computeIfAbsent(configCategory, t -> new LinkedHashMap<>())
                        .computeIfAbsent(configSubcategory, t -> new ArrayList<>());

                    if (fieldNode.desc.equals(hudPositionDesc)) {
                        subcategoryFields.add("Show " + configName);
                        subcategoryFields.add(configName + " Xpos");
                        subcategoryFields.add(configName + " Ypos");
                    }
                    subcategoryFields.add(configName);
                }
            }
        }
    }

    private void addFieldToStructure(String category, String subcategory, String name) {
        configStructure.computeIfAbsent(category, t -> new LinkedHashMap<>())
            .computeIfAbsent(subcategory, t -> new ArrayList<>())
                .add(name);
    }

    private void readConfigFieldsInAlphabeticalOrder() {
        final Map<String, Map<String, List<String>>> unsortedStructure = new HashMap<>();
        for (final ConfigFieldContainer fieldData : configFields) {
            final Map<String, List<String>> subcategoriesMap = unsortedStructure.computeIfAbsent(fieldData.getCategory(), t -> new HashMap<>());
            final List<String> subcategoryFields = subcategoriesMap.computeIfAbsent(fieldData.getSubcategory(), t -> new ArrayList<>());
            final String name = fieldData.getName();
            if (fieldData.getType() == PropertyType.HUD_POSITION) {
                subcategoryFields.add("Show " + name);
                subcategoryFields.add(name + " Xpos");
                subcategoryFields.add(name + " Ypos");
            }
            subcategoryFields.add(name);
        }

        for (final String category : getSortedMapKeys(unsortedStructure)) {
            final Map<String, List<String>> subcategoriesMap = unsortedStructure.get(category);
            final LinkedHashMap<String, List<String>> orderedSubcategoriesMap = new LinkedHashMap<>();

            for (final String subcategory : getSortedMapKeys(subcategoriesMap)) {
                final List<String> subcategoryFields = subcategoriesMap.get(subcategory);
                Collections.sort(subcategoryFields);
                orderedSubcategoriesMap.put(subcategory, subcategoryFields);
            }
            configStructure.put(category, orderedSubcategoriesMap);
        }
    }

    private static List<String> getSortedMapKeys(Map<String, ?> map) {
        final List<String> list = new ArrayList<>(map.keySet());
        Collections.sort(list);
        return list;
    }

    private void setConfigPropertiesOrder() {
        for (final Map.Entry<String, LinkedHashMap<String, List<String>>> entry : configStructure.entrySet()) {
            final List<String> propertiesNames = new ArrayList<>();
            for (final List<String> subcategoryFields : entry.getValue().values())
                propertiesNames.addAll(subcategoryFields);

            config.setCategoryPropertyOrder(entry.getKey(), propertiesNames);
        }
    }

    protected void addListener(String name, Consumer<Object> fn) {
        addListener(getConfigFieldOrThrow(name), fn);
    }

    private void addListener(ConfigFieldContainer field, Consumer<Object> fn) {
        field.setAction(fn);
    }

    protected void hidePropertyIf(String name, BooleanSupplier hideCondition) {
        hidePropertyIf(getConfigFieldOrThrow(name), hideCondition);
    }

    private void hidePropertyIf(ConfigFieldContainer field, BooleanSupplier hideCondition) {
        field.setHideCondition(hideCondition);
    }

    protected void addDependency(String dependantName, String dependencyName) {
        getConfigFieldOrThrow(dependantName).setDependsOn(getConfigFieldOrThrow(dependencyName));
    }

    private ConfigFieldContainer getConfigFieldOrThrow(String name) {
        if (name != null) {
            for (final ConfigFieldContainer field : configFields) {
                if (field.getFieldName().equals(name))
                    return field;
            }
        }
        throw new IllegalArgumentException("Field (" + name + ") NOT FOUND!!!");
    }
}
