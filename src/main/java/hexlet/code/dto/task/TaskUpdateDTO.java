package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;
    @NotNull
    private JsonNullable<String> title;
    @NotNull
    private JsonNullable<String> content;
    @NotNull
    private JsonNullable<String> status;
    @NotNull
    private JsonNullable<Set<Long>> labelIds;
}
