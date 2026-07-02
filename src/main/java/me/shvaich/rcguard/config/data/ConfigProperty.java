package me.shvaich.rcguard.config.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperty {

    PropertyType type();

    String name();

    String category();

    String subcategory() default "";

    String comment() default "";

    boolean isHidden() default false;

    /** Reserved for [PropertyType.COLOR] */
    boolean allowsTransparency() default true;

    /** Reserved for [PropertyType.SELECTOR] */
    String[] options() default {};

    /** Reserved for [PropertyType.SLIDER] and [PropertyType.NUMBER] */
    int min() default 0;

    /** Reserved for [PropertyType.SLIDER] and [PropertyType.NUMBER] */
    int max() default 0;

    /** Reserved for [PropertyType.CUSTOM] */
    Class<? extends CustomPropertyInfo> customPropertyClass() default CustomPropertyInfo.class;
}
