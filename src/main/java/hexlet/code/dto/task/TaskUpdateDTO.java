package hexlet.code.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    @NotNull
    private JsonNullable<Integer> index;
    @NotNull
    private JsonNullable<Long> assignee_id;
    @NotNull
    private JsonNullable<String> title;
    @NotNull
    private JsonNullable<String> content;
    @NotNull
    private JsonNullable<String> status;
    @NotNull
    private JsonNullable<Set<Long>> labelIds;
}
