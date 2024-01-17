package ru.yandex.practicum.filmorate.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

class IsBeforeBirthdayMovieValidator implements ConstraintValidator<IsBeforeBirthdayMovie, LocalDate> {
    private LocalDate date;

    @Override
    public void initialize(IsBeforeBirthdayMovie constraintAnnotation) {
        date = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value == null || !value.isBefore(date);
    }
}
