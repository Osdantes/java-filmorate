package ru.yandex.practicum.filmorate.annotations;

import javax.validation.Constraint;
import javax.validation.constraints.Past;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsBeforeBirthdayMovieValidator.class)
@Target(ElementType.FIELD)
public @interface IsBeforeBirthdayMovie {
    String message() default "Дата должна быть не раньше {value}";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};

    String value() default "1895-12-28";
}
