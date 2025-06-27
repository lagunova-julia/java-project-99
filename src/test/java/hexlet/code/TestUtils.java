package hexlet.code;

import hexlet.code.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class TestUtils {
    public static RequestPostProcessor withMockJwt(User user) {
        var authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        var token = new Jwt("mock-token", Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS),
                Map.of("alg", "none"),
                Map.of("sub", user.getEmail(), "authorities", List.of(user.getRoles()))
        );
        var auth = new JwtAuthenticationToken(token, authorities, user.getEmail());

        return SecurityMockMvcRequestPostProcessors.authentication(auth);
    }
}