package hexlet.code.mapper;

import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public class JsonNullableMapper {
    /**
     * Обёртывает переданное значение в {@link JsonNullable}.
     * Используется для явного указания, что значение может быть {@code null}
     * при сериализации/десериализации JSON.
     *
     * @param <T>   тип значения
     * @param entity значение, которое нужно обернуть (может быть {@code null})
     * @return {@link JsonNullable}, содержащее переданное значение
     */
    public <T> JsonNullable<T> wrap(T entity) {
        return JsonNullable.of(entity);
    }

    /**
     * Извлекает значение из {@link JsonNullable}.
     * Если {@link JsonNullable} равно {@code null} или не содержит значения, возвращает {@code null}.
     *
     * @param <T> тип значения
     * @param jsonNullable {@link JsonNullable}, из которого нужно извлечь значение
     * @return извлечённое значение или {@code null}, если {@link JsonNullable} равно {@code null} или пусто
     */
    public <T> T unwrap(JsonNullable<T> jsonNullable) {
        return jsonNullable == null ? null : jsonNullable.orElse(null);
    }

    /**
     * Проверяет, содержит ли {@link JsonNullable} значение (не {@code null} и не пусто).
     * Удобно для использования в условиях (например, в аннотациях или if-проверках).
     *
     * @param <T>      тип значения
     * @param nullable {@link JsonNullable}, которое нужно проверить
     * @return {@code true} — если {@link JsonNullable} не {@code null} и содержит значение,
     *         {@code false} — в противном случае
     */
    @Condition
    public <T> boolean isPresent(JsonNullable<T> nullable) {
        return nullable != null && nullable.isPresent();
    }
}
