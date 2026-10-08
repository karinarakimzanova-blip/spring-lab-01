package kz.iitu.springlab.metrics;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Measured {
    String name() default "";   // необязательное имя метрики
}
