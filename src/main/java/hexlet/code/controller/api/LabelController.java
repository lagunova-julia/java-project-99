package hexlet.code.controller.api;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.service.LabelService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Контроллер для работы с метками.
 */
@Slf4j
@RestController
@RequestMapping("/api/labels")
public class LabelController {
    @Autowired
    private LabelService labelService;

    /**
     * Возвращает список меток.
     * @return список меток
     */
    @GetMapping(path = "")
    public ResponseEntity<List<LabelDTO>> index() {
        var labels = labelService.getAll();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    /**
     * Создает новую метку.
     * @param labelData данные метки
     * @return созданная метка
     */
    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@Valid @RequestBody LabelCreateDTO labelData) {
        log.info("Create request received: {}", labelData);
        return labelService.create(labelData);
    }

    /**
     * Возвращает метку по ID.
     * @param id идентификатор метки
     * @return найденная метка
     */
    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO show(@PathVariable Long id) {
        return labelService.findById(id);
    }

    /**
     * Обновляет метку.
     * @param id идентификатор метки
     * @param labelData новые данные
     * @return обновленная метка
     */
    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO update(@RequestBody @Valid LabelUpdateDTO labelData, @PathVariable Long id) {
        log.info("Update request received: {}, {}", labelData, id);
        return labelService.update(labelData, id);
    }

    /**
     * Удаляет метку.
     * @param id идентификатор метки
     */
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws ResponseStatusException {
        log.info("Delete request received: {}", id);
        labelService.delete(id);
    }
}
