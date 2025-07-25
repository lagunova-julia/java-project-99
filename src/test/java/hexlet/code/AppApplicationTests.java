package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static hexlet.code.TestUtils.withMockJwt;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    private User user;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    void setUp() {
        user = Instancio.of(User.class)
                .ignore(field(User::getId))
                .set(field(User::getFirstName), "John")
                .set(field(User::getLastName), "Doe")
                .generate(field(User::getEmail), gen -> gen.net().email())
                .supply(field(User::getPassword), () -> faker.internet().password())
                .create();
    }

    @Test
    public void testWelcomePage() throws Exception {
        var result = mockMvc.perform(get("/welcome").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThat(body).contains("Welcome to Spring");
    }

    @Test
    public void testIndex() throws Exception {
        user.setRoles(Set.of("ADMIN"));
        userRepository.save(user);

        var result = mockMvc.perform(get("/api/users").with(withMockJwt(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

//        var result = mockMvc.perform(get("/api/users").with(jwt()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.password").doesNotExist())
//                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {
        userRepository.save(user);

        var request = get("/api/users/{id}", user.getId()).with(jwt());

//        var result = mockMvc.perform(request)
//                .andExpect(status().isOk())
//                .andReturn();

        var result = mockMvc.perform(get("/api/users/{id}", user.getId())
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("sub", user.getEmail());
                            jwt.claim("authorities", List.of("ROLE_USER"));
                        })))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("id").isNotNull(),
                v -> v.node("email").isEqualTo(user.getEmail()),
                v -> v.node("firstName").isEqualTo(user.getFirstName()),
                v -> v.node("lastName").isEqualTo(user.getLastName()),
                v -> v.node("createdAt").asString().matches("\\d{4}-\\d{2}-\\d{2}"),
                v -> v.node("password").isAbsent()
        );
    }

    @Test
    public void testCreate() throws Exception {
        var request = post("/api/users").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(user));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.createdAt", matchesRegex("\\d{4}-\\d{2}-\\d{2}")))
                .andExpect(jsonPath("$.password").doesNotExist());

        assertNotNull(userRepository.findByEmail(user.getEmail()));
    }

    @Test
    public void testUpdate() throws Exception {
        userRepository.save(user);

        var data = new HashMap<>();
        data.put("email", "john@yahoo.com");
        data.put("password", "new-password");

        var request = put("/api/users/{id}", user.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.email", is("john@yahoo.com")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.createdAt", matchesRegex("\\d{4}-\\d{2}-\\d{2}")))
                .andExpect(jsonPath("$.password").doesNotExist());

        User updatedUser = userRepository.findById(user.getId()).get();

        assertThat(updatedUser.getEmail()).isEqualTo("john@yahoo.com");
    }

    @Test
    public void testDelete() throws Exception {
        userRepository.save(user);

        mockMvc.perform(delete("/api/users/{id}", user.getId()).with(jwt()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    public void testDestroyWithExistingTask() throws Exception {
        User user = userRepository.save(Instancio.of(modelGenerator.getUserModel()).create());

        TaskStatus status = statusRepository.save(Instancio.of(modelGenerator.getStatusModel())
                .supply(Select.field(TaskStatus::getSlug), () -> UUID.randomUUID().toString())
                .create());

        Task task = new Task();
        task.setName("Test task");
        task.setTaskStatus(status);
        task.setAssignee(user);
        taskRepository.save(task);

        var request = delete("/api/users/{id}", user.getId()).with(token);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }
}
