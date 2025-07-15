package hexlet.code.repository;

import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Проверка существования задач с определенным статусом (для валидации при удалении статуса)
    boolean existsByTaskStatusId(Long taskStatusId);

    boolean existsByLabels_Id(Long labelId);

    // Проверка существования задач у исполнителя (для валидации при удалении пользователя)
    boolean existsByAssigneeId(Long assigneeId);

    Task findByName(String name);
}
