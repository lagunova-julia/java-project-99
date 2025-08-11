package hexlet.code.controller.api.users;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import hexlet.code.util.UserUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

/**
 * Контроллер для управления пользователями.
 * Предоставляет REST API для операций CRUD (создание, чтение, обновление, удаление) с пользователями.
 * Доступ к некоторым операциям ограничен в зависимости от роли пользователя.
 *
 * @see UserDTO
 * @see UserCreateDTO
 * @see UserUpdateDTO
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * Возвращает список всех пользователей.
     * Доступно только для пользователей с ролью ADMIN.
     *
     * @return список {@link UserDTO} всех пользователей
     * @throws AccessDeniedException если у текущего пользователя нет прав ADMIN
     */
    @GetMapping(path = "")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> index() {
        var users = userService.getAll();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(users.size()));

        return new ResponseEntity<>(users,headers, HttpStatus.OK);
    }

    /**
     * Создает нового пользователя.
     * Пароль пользователя хэшируется перед сохранением.
     * Новому пользователю автоматически присваивается роль ROLE_USER.
     *
     * @param userData данные для создания пользователя ({@link UserCreateDTO})
     * @return созданный пользователь в формате {@link UserDTO}
     * @throws MethodArgumentNotValidException если данные пользователя не прошли валидацию
     */
    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO userData) {
        return userService.create(userData);
    }

    /**
     * Возвращает информацию о пользователе по его ID.
     * Доступно для ADMIN или для самого пользователя (по principal.id).
     *
     * @param id ID пользователя
     * @return данные пользователя в формате {@link UserDTO}
     * @throws ResourceNotFoundException если пользователь не найден
     * @throws AccessDeniedException если текущий пользователь не имеет доступа
     */
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO show(@PathVariable Long id) {
        return userService.show(id);
    }

    /**
     * Обновляет данные пользователя по его ID.
     * Доступно для ADMIN или для самого пользователя (по principal.id).
     *
     * @param userData новые данные пользователя ({@link UserUpdateDTO})
     * @param id ID пользователя для обновления
     * @return обновленные данные пользователя в формате {@link UserDTO}
     * @throws ResponseStatusException если пользователь не найден (HTTP 404)
     * @throws AccessDeniedException если текущий пользователь не имеет доступа
     * @throws MethodArgumentNotValidException если данные не прошли валидацию
     */
    @PutMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@RequestBody @Valid UserUpdateDTO userData, @PathVariable Long id) {
        return userService.update(userData, id);
    }

    /**
     * Удаляет пользователя по его ID.
     * Доступно для ADMIN или для самого пользователя (по principal.id).
     *
     * @param id ID пользователя для удаления
     * @throws ResponseStatusException если пользователь не найден (HTTP 404)
     * @throws AccessDeniedException если текущий пользователь не имеет доступа
     */
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws ResponseStatusException {
        userService.delete(id);
    }
}
