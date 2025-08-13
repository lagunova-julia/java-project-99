package hexlet.code.specification;

import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.model.Task;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Спецификация для фильтрации задач.
 */
@Component
public class TaskSpecification {
    /**
     * Создает спецификацию для фильтрации.
     * @param params параметры фильтрации
     * @return спецификация
     */
    public Specification<Task> build(TaskParamsDTO params) {
        return withAssigneeId(params.getAssigneeId())
                .and(withTitleCont(params.getTitleCont()))
                .and(withStatus(params.getStatus()))
                .and(withLabelId(params.getLabelId()));
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, cb) -> assigneeId == null
                ? cb.conjunction()
                : cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    private Specification<Task> withTitleCont(String substring) {
        return (root, query, cb) -> substring == null
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + substring + "%");
    }

    private Specification<Task> withStatus(String statusSlug) {
        return (root, query, cb) -> statusSlug == null
                ? cb.conjunction()
                : cb.equal(root.get("taskStatus").get("slug"), statusSlug);
    }

    private Specification<Task> withLabelId(Long labelId) {
        return (root, query, cb) -> {
            if (labelId == null) {
                return null;
            }
            query.distinct(true);
            Join<Task, ?> labels = root.join("labels");
            return cb.equal(labels.get("id"), labelId);
        };
    }
}
