package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public final class LabelService {

    private final TaskRepository taskRepository;

    private final LabelRepository labelRepository;

    private final LabelMapper labelMapper;

    public List<LabelDTO> getAll() {
        log.info("Fetching all tasks");
        var labels = labelRepository.findAll();
        var result = labels.stream()
                .map(labelMapper::map)
                .toList();
        log.info("Found {} labels", result.size());
        return result;
    }

    public LabelDTO create(LabelCreateDTO labelData) {
        log.info("Create called with data={}", labelData);

        Label label = labelMapper.map(labelData);
        Label saved = labelRepository.save(label);

        log.info("Create successful for data={}", labelData);
        return labelMapper.map(saved);
    }

    public LabelDTO findById(Long id) {
        log.info("findById called with id={}", id);
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        log.info("findById successful for id={}", id);

        return labelMapper.map(label);
    }

    public LabelDTO update(LabelUpdateDTO labelData, Long id) {
        log.info("Update called with id={}, data={}", id, labelData);
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Label " + id + " not found"));

        labelMapper.update(labelData, label);
        labelRepository.save(label);
        log.info("Update successful for id={}", id);

        return labelMapper.map(label);
    }

    public void delete(Long id) throws ResponseStatusException {
        log.info("Delete called with id={}", id);
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Label " + id + " not found"));

        if (taskRepository.existsByLabelsId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "You cannot delete this label because it connects with tasks");
        }

        labelRepository.deleteById(id);
        log.info("Delete successful for id={}", id);
    }
}
