package hexlet.code.config;

import hexlet.code.config.security.CustomJwtAuthenticationConverter;
import hexlet.code.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;

    private final PasswordEncoder passwordEncoder;

    private final CustomUserDetailsService userService;

    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    /**
     * Настраивает цепочку безопасности.
     * @param http объект конфигурации
     * @param introspector обработчик путей
     * @return цепочка фильтров
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/welcome").permitAll()
                        .requestMatchers("/error-test").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/index.html").permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/tasks").authenticated()
                        .requestMatchers("/api/tasks/**").authenticated()
                        .requestMatchers("/api/labels").authenticated()
                        .requestMatchers("/api/labels/**").authenticated()
                        .requestMatchers("/api/users").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/task_statuses",
                                "/api/task_statuses/*").permitAll() //for show() and index() for now
                        .requestMatchers(HttpMethod.POST, "/api/task_statuses/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/task_statuses/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/task_statuses/**").authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer((rs) -> rs
                        .jwt((jwt) -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(customJwtAuthenticationConverter)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(((request, response, authException) -> response
                                .sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                        .accessDeniedHandler(((request, response, accessDeniedException) -> response
                                .sendError(HttpServletResponse.SC_FORBIDDEN))))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    /**
     * Создает менеджер аутентификации.
     * @param http конфигурация безопасности
     * @param passwordEncoder кодировщик паролей
     * @param customUserDetailsService сервис пользовательских данных
     * @return менеджер аутентификации
     * @throws Exception при ошибках создания
     */
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            PasswordEncoder passwordEncoder,
            CustomUserDetailsService customUserDetailsService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder
                        .class);
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder);

        return authenticationManagerBuilder.build();
    }
}
