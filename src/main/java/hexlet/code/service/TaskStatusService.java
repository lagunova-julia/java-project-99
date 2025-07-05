package hexlet.code.service;

import hexlet.code.dto.status.TaskStatusCreateDTO;
import hexlet.code.dto.status.TaskStatusDTO;
import hexlet.code.dto.status.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class TaskStatusService {
    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskStatusMapper statusMapper;
    @Autowired
    private TaskRepository taskRepository;

    public List<TaskStatusDTO> getAll() {
        log.info("Fetching all task statuses");
        var statuses = statusRepository.findAll();
        var result = statuses.stream()
                .map(statusMapper::map)
                .toList();
        log.info("Found {} statuses", result.size());
        return result;
    }

    public TaskStatusDTO create(TaskStatusCreateDTO statusData) {
        log.info("Create called with data={}", statusData);
        var taskStatus = statusMapper.map(statusData);
//        taskStatus.setRoles(Set.of("ROLE_USER"));
        statusRepository.save(taskStatus);
        log.info("Create successful for data={}", statusData);
        return statusMapper.map(taskStatus);
    }

    public TaskStatusDTO findById(Long id) {
        log.info("findById called with id={}", id);
        var status = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        log.info("findById successful for id={}", id);

        return statusMapper.map(status);
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO statusData, Long id) {
        log.info("Update called with id={}, data={}", id, statusData);
        var status = statusRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task status " + id + " not found"));

        statusMapper.update(statusData, status);
        statusRepository.save(status);
        log.info("Update successful for id={}", id);

        return statusMapper.map(status);
    }

    public void delete(Long id) throws ResponseStatusException {
        log.info("Delete called with id={}", id);
        TaskStatus taskStatus = statusRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task status " + id + " not found"));

        if (taskRepository.existsByTaskStatusId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "You cannot delete this status because it connects with tasks");
        }

        statusRepository.deleteById(id);
        log.info("Delete successful for id={}", id);
    }
}
