package hexlet.code.controller;

import io.sentry.Sentry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class TestController {
    @GetMapping(path = "/error-test")
    public String triggerError() {
        try {
            throw new RuntimeException("Тестовая ошибка для Sentry!");
        } catch (Exception e) {
            Sentry.captureException(e);  // Отправка ошибки в Sentry
            return "Произошла ошибка, но она отправлена в Sentry!";
        }
    }
}
