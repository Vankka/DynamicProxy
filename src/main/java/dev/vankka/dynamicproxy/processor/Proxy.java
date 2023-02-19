package dev.vankka.dynamicproxy.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Proxy {

    /**
     * The <b>interface</b> to proxy.
     * @return the class to proxy
     */
    Class<?> value();

    /**
     * Specifies the class name of the generated class. This cannot be the same as this or any other existing class.
     * @return the class name
     */
    String className() default "";

    /**
     * Specifies the suffix added to the class name of the generated class. If {@link #className()} is specified this will be ignored.
     * @return the suffix to add to the end of this classes name for the generated class
     */
    String suffix() default "Proxy";
}
