package ru.yandex.practicum.filmorate.validate;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.List;

public class Validate {
    public static void validate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder message = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                message.append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage());
            }
            throw new ValidationException(message.toString());
        }
    }
}
