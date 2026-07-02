package me.shvaich.rcguard.config.data;

import java.lang.reflect.Field;

public class ConfigCategoryContainer {

    private final String categoryName;
    private final ConfigCategory annotation;

    public ConfigCategoryContainer(Field field) throws IllegalAccessException {
        this.categoryName = (String) field.get(null);
        this.annotation = field.getAnnotation(ConfigCategory.class);
    }

    public ConfigCategory getAnnotation() { return annotation; }

    public String getCategoryName() { return categoryName; }
}
