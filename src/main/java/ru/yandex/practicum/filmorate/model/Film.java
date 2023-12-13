package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import ru.yandex.practicum.filmorate.annotations.IsBeforeBirthdayMovie;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
