package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.IsBeforeBirthdayMovie;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Film {
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    @Size(max = 200)
    private String description;
    @IsBeforeBirthdayMovie
    private LocalDate releaseDate;
    @Min(value = 1)
    private int duration;
    private List<Genre> genres;
    private Mpa mpa;
}
