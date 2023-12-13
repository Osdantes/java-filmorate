package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User {
    private long id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String login;
    private String name;
    @PastOrPresent
    private LocalDate birthday;
}
