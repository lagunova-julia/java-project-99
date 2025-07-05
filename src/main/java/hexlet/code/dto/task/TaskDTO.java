package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class TaskDTO {
    private long id;
    private int index;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    private Long assignee_id;
    private String title;
    private String content;
    private String status;
}
