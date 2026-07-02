package me.shvaich.rcguard.config.data;

import me.shvaich.rcguard.gui.data.HUDPosition;

import java.lang.reflect.Field;

public enum PropertyType {

    SWITCH(boolean.class),
    COLOR(int.class),
    SELECTOR(int.class),
    SLIDER(int.class),
    NUMBER(int.class),
    HUD_POSITION(HUDPosition.class),
    CUSTOM {
        @Override
        public boolean isFieldValid(Field field) {
            final ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
            return annotation.customPropertyClass() != CustomPropertyInfo.class;
        }
    };

    private final Class<?> type;

    PropertyType() { this.type = null; }

    PropertyType(Class<?> type) {
        this.type = type;
    }

    public boolean isFieldValid(Field field) {
        return field.getType() == this.type;
    }
}
