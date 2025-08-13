package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import hexlet.code.util.UserUtils;
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
import java.util.Set;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private LabelRepository labelRepository;

    private Task testTask;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        User testUser = userRepository.save(Instancio.of(modelGenerator.getUserModel()).create());
        TaskStatus testStatus = statusRepository.save(Instancio.of(modelGenerator.getStatusModel())
                .set(field(TaskStatus::getName), "test_status_" + UUID.randomUUID()
                        .toString().substring(0, 8)).create());
        Label testLabel = labelRepository.save(Instancio.of(modelGenerator.getLabelModel()).create());
        testTask.setTaskStatus(testStatus);
        testTask.setAssignee(testUser);
        testTask.setLabels(Set.of(testLabel));
        token = jwt().jwt(builder -> builder.subject(userUtils.getTestUser().getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        taskRepository.save(testTask);
        var result = mockMvc.perform(get("/api/tasks").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    void getAllFilteredTasks() throws Exception {
        var assignee = testTask.getAssignee();
        var status = testTask.getTaskStatus();

        var label = labelRepository.save(Instancio.of(modelGenerator.getLabelModel()).create());

        testTask.setLabels(Set.of(label));
        testTask.setName("Fix backend bug");
        taskRepository.save(testTask);

        var otherTask = Instancio.of(modelGenerator.getTaskModel()).create();
        otherTask.setName("Something else");
        otherTask.setAssignee(null);
        otherTask.setLabels(Set.of());
        otherTask.setTaskStatus(status);
        taskRepository.save(otherTask);

        mockMvc.perform(get("/api/tasks")
                        .param("titleCont", "backend")
                        .param("assigneeId", String.valueOf(assignee.getId()))
                        .param("status", status.getSlug())
                        .param("labelId", String.valueOf(label.getId()))
                        .with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Fix backend bug"))
                .andExpect(jsonPath("$[0].assignee_id").value(assignee.getId()))
                .andExpect(jsonPath("$[0].status").value(status.getSlug()));
    }

    @Test
    public void testShow() throws Exception {

        taskRepository.save(testTask);

        var request = get("/api/tasks/{id}", testTask.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var dto = taskMapper.map(testTask);

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByName(testTask.getName());

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo(testTask.getName());
        assertThat(task.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(task.getAssignee()).isEqualTo(testTask.getAssignee());
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getTaskStatus()).isEqualTo(testTask.getTaskStatus());
    }

    @Test
    public void testUpdate() throws Exception {
        taskRepository.save(testTask);

        var data = new TaskUpdateDTO();
        data.setIndex(JsonNullable.of(5));
        data.setContent(JsonNullable.of("new content"));


        var request = put("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var task = taskRepository.findById(testTask.getId()).get();

        assertThat(task.getIndex()).isEqualTo(5);
        assertThat(task.getDescription()).isEqualTo("new content");
    }


    @Test
    public void testDestroy() throws Exception {
        taskRepository.save(testTask);
        var request = delete("/api/tasks/{id}", testTask.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(testTask.getId())).isFalse();
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        taskRepository.save(testTask);
        var request = get("/api/tasks/{id}", testTask.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var dto = taskMapper.map(testTask);

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        taskRepository.save(testTask);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle(JsonNullable.of("updated title"));
        updateDTO.setContent(JsonNullable.of("updated content"));

        var request = put("/api/tasks/{id}", testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
