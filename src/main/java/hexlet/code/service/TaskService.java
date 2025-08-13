package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import hexlet.code.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Сервис для управления задачами.
 */
@Slf4j
@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TaskStatusRepository statusRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserUtils userUtils;

    @Autowired
    private TaskSpecification specBuilder;

    /**
     * Возвращает список задач с пагинацией и фильтрацией.
     * @param params параметры фильтрации задач (может быть null)
     * @param page номер страницы (начинается с 1)
     * @return страница списка задач с учетом фильтрации
     */
    public List<TaskDTO> getAll(TaskParamsDTO params, @RequestParam(defaultValue = "1") int page) {
        log.info("Fetching all tasks");
        var specification = specBuilder.build(params);
        var tasks = taskRepository.findAll(specification, PageRequest.of(page - 1, 10));
        var result = tasks.stream()
                .map(taskMapper::map)
                .toList();
        log.info("Found {} tasks", result.size());
        return result;
    }

    /**
     * Возвращает общее количество задач с учетом параметров фильтрации.
     * @param params параметры фильтрации задач (может быть null)
     * @return общее количество задач, удовлетворяющих критериям фильтрации
     */
    public Long getTotalCount(TaskParamsDTO params) {
        var spec = specBuilder.build(params);
        return taskRepository.count(spec);
    }

    /**
     * Создает новую задачу.
     * @param taskData данные задачи
     * @return созданная задача
     */
    public TaskDTO create(TaskCreateDTO taskData) {
        log.info("Create called with data={}", taskData);
        TaskStatus status = statusRepository.findBySlug(taskData.getStatus())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task status not found: " + taskData.getStatus()));
        Set<Label> labels = new HashSet<>();
        if (taskData.getLabelIds() != null && !taskData.getLabelIds().isEmpty()) {
            labels = new HashSet<>(labelRepository.findAllById(taskData.getLabelIds()));
            // Проверка: все ли переданные ID найдены
            if (labels.size() != taskData.getLabelIds().size()) {
                throw new ResponseStatusException(BAD_REQUEST, "Some label IDs not found");
            }
        }

        User assignee = null;
        if (taskData.getAssigneeId() != null) {
            assignee = userRepository.findById(taskData.getAssigneeId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found: " + taskData.getAssigneeId()));
        }

        Task task = taskMapper.map(taskData);
        task.setTaskStatus(status);
        task.setAssignee(assignee);
        task.setLabels(labels);
//        task.setRoles(Set.of("ROLE_USER"));
        Task saved = taskRepository.save(task);
        log.info("Create successful for data={}", taskData);
        return taskMapper.map(saved);
    }

    /**
     * Находит задачу по ID.
     * @param id идентификатор задачи
     * @return найденная задача
     */
    public TaskDTO findById(Long id) {
        log.info("findById called with id={}", id);
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        log.info("findById successful for id={}", id);

        return taskMapper.map(task);
    }

    /**
     * Обновляет задачу.
     * @param id идентификатор задачи
     * @param taskData новые данные
     * @return обновленная задача
     */
    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        log.info("Update called with id={}, data={}", id, taskData);
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task " + id + " not found"));
        if (taskData.getLabelIds() != null && taskData.getLabelIds().isPresent()) {
            var labelIds = taskData.getLabelIds().get();
            var labels = labelRepository.findAllById(labelIds);
            task.setLabels(new HashSet<>(labels));
        }

        taskMapper.update(taskData, task);
        taskRepository.save(task);
        log.info("Update successful for id={}", id);

        return taskMapper.map(task);
    }

    /**
     * Удаляет задачу.
     * @param id идентификатор задачи
     */
    public void delete(Long id) throws ResponseStatusException {
        log.info("Delete called with id={}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task " + id + " not found"));

        taskRepository.deleteById(id);
        log.info("Delete successful for id={}", id);
    }
}
