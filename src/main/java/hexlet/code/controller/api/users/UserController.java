package hexlet.code.controller.api.users;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

/**
 * Контроллер для управления пользователями.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * Возвращает всех пользователей.
     * @return ResponseEntity со списком UserDTO и X-Total-Count в заголовках
     */
    @GetMapping(path = "")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> index() {
        var users = userService.getAll();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(users.size()));

        return new ResponseEntity<>(users, headers, HttpStatus.OK);
    }

    /**
     * Создает нового пользователя.
     * @param userData данные для создания
     * @return созданный UserDTO
     */
    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO userData) {
        return userService.create(userData);
    }

    /**
     * Возвращает пользователя по ID.
     * @param id ID пользователя
     * @return UserDTO
     */
    @GetMapping(path = "/{id}")
//    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO show(@PathVariable Long id) {
        return userService.show(id);
    }

    /**
     * Обновляет пользователя.
     * @param id ID пользователя
     * @param userData новые данные
     * @return обновленный UserDTO
     */
    @PutMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == @userService.getIdByEmail(principal.claims['sub'])")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@RequestBody @Valid UserUpdateDTO userData, @PathVariable Long id) {
        return userService.update(userData, id);
    }

    /**
     * Удаляет пользователя.
     * @param id ID пользователя
     */
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == @userService.getIdByEmail(principal.claims['sub'])")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws ResponseStatusException {
        userService.delete(id);
    }
}
