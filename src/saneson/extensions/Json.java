package saneson.extensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field for JSON deserialization enabling customization.
 * When {@code key} is set, the mapper uses that JSON key instead of the field name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Json {
    /**
     * The JSON key that maps to this field. If empty, the field name is used.
     */
    String value() default "";

    /**
     * When {@code true}, the field is skipped during deserialization even if a matching JSON key is present.
     */
    boolean ignored() default false;
}
