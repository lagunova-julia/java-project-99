package hexlet.code.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {
    private int index;
    private Long assignee_id;
    private String title;
    private String content;
    private String status;
    private Set<Long> labelIds;
}
