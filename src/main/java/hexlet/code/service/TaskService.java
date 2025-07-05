package hexlet.code.service;

import hexlet.code.dto.status.TaskStatusCreateDTO;
import hexlet.code.dto.status.TaskStatusDTO;
import hexlet.code.dto.status.TaskStatusUpdateDTO;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
    private UserRepository userRepository;
    @Autowired
    private UserUtils userUtils;

    public List<TaskDTO> getAll() {
        log.info("Fetching all tasks");
        var tasks = taskRepository.findAll();
        var result = tasks.stream()
                .map(taskMapper::map)
                .toList();
        log.info("Found {} tasks", result.size());
        return result;
    }

    public TaskDTO create(TaskCreateDTO taskData) {
        log.info("Create called with data={}", taskData);
        TaskStatus status = statusRepository.findBySlug(taskData.getStatus())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task status not found: " + taskData.getStatus()));

        User assignee = null;
        if (taskData.getAssignee_id() != null) {
            assignee = userRepository.findById(taskData.getAssignee_id())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found: " + taskData.getAssignee_id()));
        }

        Task task = taskMapper.map(taskData);
        task.setTaskStatus(status);
        task.setAssignee(assignee);
//        task.setRoles(Set.of("ROLE_USER"));
        Task saved = taskRepository.save(task);
        log.info("Create successful for data={}", taskData);
        return taskMapper.map(saved);
    }

    public TaskDTO findById(Long id) {
        log.info("findById called with id={}", id);
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));
        log.info("findById successful for id={}", id);

        return taskMapper.map(task);
    }

    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        log.info("Update called with id={}, data={}", id, taskData);
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task " + id + " not found"));

        taskMapper.update(taskData, task);
        taskRepository.save(task);
        log.info("Update successful for id={}", id);

        return taskMapper.map(task);
    }

    public void delete(Long id) throws ResponseStatusException {
        log.info("Delete called with id={}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task " + id + " not found"));

        taskRepository.deleteById(id);
        log.info("Delete successful for id={}", id);
    }
}
