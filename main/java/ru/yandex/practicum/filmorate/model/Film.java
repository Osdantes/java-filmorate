package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

    private long id;
    @NotNull
    @NotBlank
    private String name;
    @Min(value = 1)
    private int duration;
    private final Set<Long> likes = new HashSet<>();
}
