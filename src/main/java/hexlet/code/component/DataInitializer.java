package hexlet.code.component;

import hexlet.code.model.User;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public final class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LabelRepository labelRepository;

    private static final List<String> DEFAULT_LABELS = List.of("feature", "bug");

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initAdmin();
        initLabels();
    }

    private void initAdmin() {
        if(userRepository.findByEmail("hexlet@example.com").isEmpty()) {
            User user = new User();
            user.setEmail("hexlet@example.com");
            user.setPassword(passwordEncoder.encode("qwerty"));
            user.setRoles(Set.of("ROLE_ADMIN"));

            userRepository.save(user);
        }
    }

    private void initLabels() {
        for(var name : DEFAULT_LABELS) {
            if(labelRepository.findByName(name).isEmpty()) {
                var label = new Label();
                label.setName(name);
                labelRepository.save(label);
            }
        }
    }
}
