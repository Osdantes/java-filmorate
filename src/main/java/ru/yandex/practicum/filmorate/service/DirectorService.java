package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.dao.DirectorDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorDbStorage directorDbStorage;

    public List<Director> getAllDirectors() {
        return directorDbStorage.getAllDirectors();
    }

    public Director getDirectorById(int id) {
        Director director = directorDbStorage.getDirectorById(id);
        if (director != null) {
            log.info("Найден режиссер: {} {}", director.getId(), director.getName());
        } else {
            log.info("Режиссер с id {} не найден.", id);
            throw new DataNotFoundException(String.format("Режиссер с id: %d не найден.", id));
        }
        return director;
    }

    public Director addDirector(Director director) {
        return directorDbStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        Director updatedDirector;
        if (directorDbStorage.getDirectorById(director.getId()) == null) {
            log.warn("Попытка обновить несуществующего режиссера с id: {}", director.getId());
            throw new DataNotFoundException(String.format("Режиссер с id: %d не найден.", director.getId()));
        }
        updatedDirector = directorDbStorage.updateDirector(director);
        log.info("Режиссер с id: {} был обновлен.", director.getId());
        return updatedDirector;
    }

    public boolean deleteDirector(int id) {
        return directorDbStorage.deleteDirector(id);
    }
}
