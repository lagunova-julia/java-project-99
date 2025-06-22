package hexlet.code.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {
    /**
     * Создаёт и настраивает билдер {@link Jackson2ObjectMapperBuilder} для кастомной конфигурации
     * Jackson ObjectMapper, используемого в Spring для сериализации/десериализации JSON.
     *
     * <p>Настройки включают:
     * <ul>
     *     <li>Исключение полей со значением {@code null} при сериализации ({@link JsonInclude.Include#NON_NULL})</li>
     *     <li>Установку модуля {@link JsonNullableModule} для работы с {@code JsonNullable}
     *     (опциональные поля API)</li>
     * </ul>
     * </p>
     *
     * @return настроенный билдер {@link Jackson2ObjectMapperBuilder}
     * @see JsonInclude.Include
     * @see JsonNullableModule
     */
    @Bean
    Jackson2ObjectMapperBuilder objectMapperBuilder() {
        var builder = new Jackson2ObjectMapperBuilder();
        builder.serializationInclusion(JsonInclude.Include.NON_NULL)
                .modulesToInstall(new JsonNullableModule());
        return builder;
    }
}
