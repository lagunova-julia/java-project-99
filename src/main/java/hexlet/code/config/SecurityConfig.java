package hexlet.code.config;

import hexlet.code.config.security.CustomJwtAuthenticationConverter;
import hexlet.code.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
public class SecurityConfig {
    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    /**
     * Определяет основную цепочку фильтров безопасности для HTTP-запросов.
     * Настраивает политики аутентификации, авторизации, CORS, CSRF и другие.
     *
     * <p>Примеры настроек:
     * <ul>
     *     <li>Отключение CSRF для REST API</li>
     *     <li>Ограничение доступа к эндпоинтам</li>
     *     <li>Настройка JWT-аутентификации</li>
     * </ul>
     * </p>
     *
     * @param http          объект {@link HttpSecurity} для конфигурации
     * @param introspector  {@link HandlerMappingIntrospector} для обработки путей запросов
     * @return сконфигурированная цепочка фильтров {@link SecurityFilterChain}
     * @throws Exception при ошибках конфигурации
     * @see HttpSecurity
     * @see SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/welcome").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/index.html").permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        .requestMatchers("/api/tasks").authenticated()
                        .requestMatchers("/api/tasks/**").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/task_statuses",
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
     * Создаёт и возвращает {@link AuthenticationManager}, используемый Spring Security
     * для процесса аутентификации (например, при входе по логину/паролю или JWT).
     *
     * <p>Делегирует работу провайдеру аутентификации ({@link #daoAuthProvider()}).</p>
     *
     * @param http объект {@link HttpSecurity} для получения общего {@link AuthenticationManager}
     * @return бин {@link AuthenticationManager}
     * @see AuthenticationManager
     * @see DaoAuthenticationProvider
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
