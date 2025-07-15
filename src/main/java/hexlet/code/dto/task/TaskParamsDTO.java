package hexlet.code.dto.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class TaskParamsDTO {
    private String titleCont;
    private Long assigneeId;
    private String status;
    private Long labelId;
}
