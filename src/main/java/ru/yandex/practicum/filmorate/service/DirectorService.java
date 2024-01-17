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
        log.info("Получение списка режиссеров.");
        return directorDbStorage.getAllDirectors();
    }

    public Director getDirectorById(int id) {
        if (!isDirectorPresent(id)) {
            throw new DataNotFoundException(String.format("Режиссер с id: %d не найден.", id));
        }
        log.info("Получение режиссера с id: {} .", id);
        return directorDbStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        log.info("Добавление режиссера : {} .", director.getName());
        return directorDbStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        Director updatedDirector;
        if (!isDirectorPresent(director.getId())) {
            log.warn("Попытка обновить несуществующего режиссера с id: {}", director.getId());
            throw new DataNotFoundException(String.format("Режиссер с id: %d не найден.", director.getId()));
        }
        updatedDirector = directorDbStorage.updateDirector(director);
        log.info("Режиссер с id: {} был обновлен.", director.getId());
        return updatedDirector;
    }

    public void deleteDirector(int id) {
        if (!isDirectorPresent(id)) {
            throw new DataNotFoundException(String.format("Режиссер с id: %d не найден.", id));
        }
        log.info("Удаление режиссера с id: {} .", id);
        directorDbStorage.deleteDirector(id);
    }

    public boolean isDirectorPresent(int id) {
        boolean isPresent = directorDbStorage.existsById(id);
        if (isPresent) {
            log.info("Найден режиссер с id: {}", id);
        } else {
            log.info("Режиссер с id {} не найден.", id);
        }
        return isPresent;
    }
}
