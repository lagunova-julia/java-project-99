package hexlet.code.dto.label;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class LabelUpdateDTO {
    @NotNull
    private JsonNullable<String> name;
}
