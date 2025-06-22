package hexlet.code;

import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AppApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    /**
     * Создаёт и возвращает бин {@link Faker} — библиотеку для генерации фейковых данных
     * (например, имён, адресов, телефонных номеров).
     *
     * <p>Использование:
     * <pre>{@code
     * faker.name().fullName(); // "John Doe"
     * faker.address().city();  // "New York"
     * }</pre>
     * </p>
     *
     * @return экземпляр {@link Faker}, готовый к использованию
     * @see Faker
     */
    @Bean
    public Faker faker() {
        return new Faker();
    }
}
