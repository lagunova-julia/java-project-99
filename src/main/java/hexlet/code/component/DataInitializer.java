package hexlet.code.component;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        User user = new User();
        user.setEmail("hexlet@example.com");
        user.setPassword("qwerty"); // В production убедитесь, что пароль хэшируется

        userRepository.save(user);
    }
}
