package hexlet.code;

import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
public class AppApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    /**
     * Создает экземпляр Faker для генерации тестовых данных.
     * @return экземпляр Faker
     */
    @Bean
    public Faker faker() {
        return new Faker();
    }

    /**
     * Инициализирует стандартные статусы задач при старте приложения.
     * @param repository репозиторий статусов задач
     * @return CommandLineRunner для выполнения инициализации
     */
    @Bean
    public CommandLineRunner initDefaultStatuses(TaskStatusRepository repository) {
        return args -> {
            List<TaskStatus> defaultStatuses = List.of(
                    new TaskStatus("Draft", "draft"),
                    new TaskStatus("To Review", "to_review"),
                    new TaskStatus("To Be Fixed", "to_be_fixed"),
                    new TaskStatus("To Publish", "to_publish"),
                    new TaskStatus("Published", "published")
            );

            defaultStatuses.forEach(status -> {
                if (!repository.existsBySlug(status.getSlug())) {
                    repository.save(status);
                }
            });
        };
    }
}
