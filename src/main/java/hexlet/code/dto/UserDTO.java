package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class UserDTO {
    private long id;
    private String firstName;

    private String lastName;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
}
