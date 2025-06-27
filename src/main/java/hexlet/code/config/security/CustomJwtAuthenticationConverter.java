package hexlet.code.config.security;

import hexlet.code.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Кастомный конвертер для преобразования JWT-токена в объект аутентификации Spring Security.
 * Извлекает данные пользователя из JWT (subject и authorities) и создает {@link UsernamePasswordAuthenticationToken}.
 *
 * <p>Используется в цепочке фильтров Spring Security для JWT-аутентификации.</p>
 *
 * @see Converter
 * @see Jwt
 * @see AbstractAuthenticationToken
 */
@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Конвертирует JWT-токен в объект аутентификации.
     *
     * <p>Шаги преобразования:
     * <ol>
     *   <li>Извлекает имя пользователя (subject) из JWT.</li>
     *   <li>Загружает данные пользователя через {@link CustomUserDetailsService}.</li>
     *   <li>Извлекает роли/права (authorities) из JWT (используя {@link JwtGrantedAuthoritiesConverter}).</li>
     *   <li>Создает {@link UsernamePasswordAuthenticationToken} с полученными данными.</li>
     * </ol>
     * </p>
     *
     * @param jwt JWT-токен, полученный из запроса
     * @return объект аутентификации {@link AbstractAuthenticationToken}
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String username = jwt.getSubject();
        UserDetails user = userDetailsService.loadUserByUsername(username);

        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        Collection<? extends GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);
        return new UsernamePasswordAuthenticationToken(user, "n/a", authorities);
    }
}
