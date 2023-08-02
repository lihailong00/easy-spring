package com.lee.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 晓龙coding
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyScope {
    String value() default "singleton";
    enum MyScopeType {
        SINGLETON("singleton"),
        PROTOTYPE("prototype")
        ;
        MyScopeType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private String name;
    }
}
