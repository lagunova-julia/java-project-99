package hexlet.code.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import hexlet.code.component.RsaKeyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Configuration class for password encoders.
 *
 * <p>This class is designed for extension. Subclasses should:
 * <ul>
 *   <li>Override {@link #passwordEncoder()} to provide custom encoder implementations</li>
 *   <li>Ensure any overridden methods maintain the same security guarantees</li>
 *   <li>Never weaken access modifiers of security-related methods</li>
 * </ul>
 *
 * @see PasswordEncoder
 */
@Configuration
public class EncodersConfig {
    @Autowired
    // Создается ниже
    private RsaKeyProperties rsaKeys;

    /**
     * Создаёт и возвращает бин {@link PasswordEncoder} для хеширования и проверки паролей.
     *
     * <p>Используется Spring Security для:
     * <ul>
     *   <li>Безопасного хранения паролей в БД (в захешированном виде)</li>
     *   <li>Проверки введённых паролей при аутентификации</li>
     * </ul>
     *
     * <p>В данной реализации используется {@link BCryptPasswordEncoder} — рекомендуемый алгоритм хеширования,
     * который:
     * <ul>
     *   <li>Автоматически добавляет "соль" (salt) к каждому паролю</li>
     *   <li>Поддерживает настраиваемую вычислительную сложность (strength)</li>
     *   <li>Устойчив к атакам перебора (brute-force)</li>
     * </ul>
     * </p>
     *
     * @return бин кодировщика паролей ({@code BCryptPasswordEncoder} по умолчанию)
     * @see BCryptPasswordEncoder
     * @see PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создаёт и возвращает {@link JwtEncoder} для кодирования (подписи) JWT-токенов.
     * Используется в процессах аутентификации (например, при выдаче токена после входа в систему).
     *
     * <p>Конфигурация кодировщика зависит от настроек приложения (алгоритм подписи, секретный ключ и т. д.).</p>
     *
     * @return бин {@link JwtEncoder}, готовый к использованию в Spring Security
     * @see JwtEncoder
     * @see org.springframework.security.oauth2.jwt.JwtEncoder
     */
    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaKeys.getPublicKey()).privateKey(rsaKeys.getPrivateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    /**
     * Создаёт и возвращает {@link JwtDecoder} для декодирования и проверки JWT-токенов.
     * Используется при проверке подлинности запросов (например, в {@link JwtAuthenticationFilter}).
     *
     * <p>Конфигурация декодировщика зависит от:
     * <ul>
     *     <li>Алгоритма подписи (HS256, RS256 и т. д.)</li>
     *     <li>Публичного ключа (для RS*) или секретного ключа (для HS*)</li>
     *     <li>Валидации claims (issuer, expiration и др.)</li>
     * </ul>
     * </p>
     *
     * @return бин {@link JwtDecoder}, готовый к использованию в Spring Security
     * @see JwtDecoder
     * @see org.springframework.security.oauth2.jwt.JwtDecoder
     */
    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeys.getPublicKey()).build();
    }
}
