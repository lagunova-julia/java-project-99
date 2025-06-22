package hexlet.code;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPrivateDataWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    @WithMockUser
        // Тест с мокнутой аутентификацией
    void getPrivateDataWithAuth() throws Exception {
        mockMvc.perform(get("/api/users").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void testShowWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    @WithMockUser(roles = "USER") // Обычный пользователь
//    void deleteUser_WithoutAdminRole() throws Exception {
//        mockMvc.perform(delete("/api/users/123"))
//                .andExpect(status().isForbidden()); // 403
//    }

    @Test
    @WithMockUser
    void getUserShouldNotExposePassword() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void accessWithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }
}
