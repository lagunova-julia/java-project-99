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
        String adminEmail = System.getenv("ADMIN_EMAIL");
        String adminPassword = System.getenv("ADMIN_PASSWORD");
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User user = new User();
            user.setEmail(adminEmail);
            user.setPassword(passwordEncoder.encode(adminPassword));
            user.setRoles(Set.of("ROLE_ADMIN"));

            userRepository.save(user);
        }
    }

    private void initLabels() {
        for (var name : DEFAULT_LABELS) {
            if (labelRepository.findByName(name).isEmpty()) {
                var label = new Label();
                label.setName(name);
                labelRepository.save(label);
            }
        }
    }
}
