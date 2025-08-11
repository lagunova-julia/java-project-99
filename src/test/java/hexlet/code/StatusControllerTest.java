package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.status.TaskStatusUpdateDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.ModelGenerator;
import hexlet.code.util.UserUtils;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StatusControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Faker faker;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusMapper statusMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private UserUtils userUtils;

    private TaskStatus testStatus;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        testStatus = Instancio.of(modelGenerator.getStatusModel()).create();
        token = jwt().jwt(builder -> builder.subject(userUtils.getTestUser().getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        statusRepository.save(testStatus);
        var result = mockMvc.perform(get("/api/task_statuses").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {

        statusRepository.save(testStatus);

        var request = get("/api/task_statuses/{id}", testStatus.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testStatus.getName()),
                v -> v.node("slug").isEqualTo(testStatus.getSlug())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var dto = statusMapper.map(testStatus);

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var status = statusRepository.findBySlug(testStatus.getSlug()).get();

        assertThat(status).isNotNull();
        assertThat(status.getName()).isEqualTo(testStatus.getName());
        assertThat(status.getSlug()).isEqualTo(testStatus.getSlug());
    }

    @Test
    public void testUpdate() throws Exception {
        statusRepository.save(testStatus);

        var data = new TaskStatusUpdateDTO();
        data.setName(JsonNullable.of("new name"));


        var request = put("/api/task_statuses/{id}", testStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var status = statusRepository.findById(testStatus.getId()).get();

        assertThat(status.getName()).isEqualTo(data.getName().get());
        assertThat(status.getSlug()).isEqualTo(testStatus.getSlug());
    }


    @Test
    public void testDestroy() throws Exception {
        statusRepository.save(testStatus);
        var request = delete("/api/task_statuses/{id}", testStatus.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(statusRepository.existsById(testStatus.getId())).isFalse();
    }

    @Test
    public void testDestroyWithExistingTask() throws Exception {
        statusRepository.save(testStatus);

        Task task = new Task();
        task.setName(faker.lorem().word());
        task.setTaskStatus(testStatus);
        taskRepository.save(task);

        var request = delete("/api/task_statuses/{id}", testStatus.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        assertThat(statusRepository.existsById(testStatus.getId())).isTrue();
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {

        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isOk());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        statusRepository.save(testStatus);
        var request = get("/api/task_statuses/{id}", testStatus.getId());
        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var dto = statusMapper.map(testStatus);

        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
