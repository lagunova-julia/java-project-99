package hexlet.code.service;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        var usersDTOs = users.stream()
                .map(userMapper::map)
                .toList();
        return usersDTOs;
    }

    public UserDTO create(UserCreateDTO userData) {
        var user = userMapper.map(userData);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of("ROLE_USER"));
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO show(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found: " + id));

        return userMapper.map(user);
    }

    public UserDTO update(UserUpdateDTO userData, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " not found"));

        userMapper.update(userData, user);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public void delete(Long id) throws ResponseStatusException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " not found"));

        if (taskRepository.existsByAssigneeId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "You cannot delete this user because he connects with tasks");
        }

        userRepository.deleteById(id);
    }
}
