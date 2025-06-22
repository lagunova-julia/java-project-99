package hexlet.code.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import hexlet.code.repository.UserRepository;

/**
 * Кастомная реализация {@link UserDetailsManager} для работы с пользователями через {@link UserRepository}.
 * Обеспечивает аутентификацию и управление пользователями в системе.
 */
@Service
public class CustomUserDetailsService implements UserDetailsManager {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Загружает пользователя по email (используется как username).
     * @param email email пользователя
     * @return {@link UserDetails} с данными пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Нужно добавить в репозиторий findByEmail
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user;
    }

    /**
     * Создаёт нового пользователя.
     * @param userData данные пользователя
     * @throws IllegalArgumentException если пользователь уже существует
     */
    @Override
    public void createUser(UserDetails userData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    /**
     * Обновляет данные пользователя.
     * @param user объект с новыми данными пользователя
     * @throws UnsupportedOperationException в текущей реализации метод не поддерживается
     * @implNote Заглушка для будущей реализации
     */
    @Override
    public void updateUser(UserDetails user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    /**
     * Удаляет пользователя по username.
     * @param username идентификатор пользователя (email)
     * @throws UnsupportedOperationException в текущей реализации метод не поддерживается
     * @implNote Заглушка для будущей реализации
     */
    @Override
    public void deleteUser(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    /**
     * Изменяет пароль пользователя.
     * @param oldPassword текущий пароль
     * @param newPassword новый пароль
     * @throws UnsupportedOperationException в текущей реализации метод не поддерживается
     * @implNote Заглушка для будущей реализации
     */
    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changePassword'");
    }

    /**
     * Проверяет существование пользователя.
     * @param username идентификатор пользователя (email)
     * @return true если пользователь существует
     * @throws UnsupportedOperationException в текущей реализации метод не поддерживается
     * @implNote Заглушка для будущей реализации
     */
    @Override
    public boolean userExists(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'userExists'");
    }
}
