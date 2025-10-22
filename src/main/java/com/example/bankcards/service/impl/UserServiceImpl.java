package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.UpdateUserRequest;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.dto.mapper.UserMapper;
import com.example.bankcards.entity.role.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;


    @Override
    public UserDto findById(Long id) {
        User user = getUserOrThrow(id);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto findByUsername(String username) {
        User user = getUserOrThrow(username);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = getUserOrThrow(id);

        String newUsername = request.getUsername();
        if (!user.getUsername().equals(newUsername)) {
            if (userRepository.existsByUsername(newUsername)) {
                log.warn("Username={} already exists", newUsername);
                throw new UsernameAlreadyExistsException(newUsername);
            }
            user.setUsername(newUsername);
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoles()));
        }

        User saved = userRepository.save(user);
        log.info("User with id={} updated successfully", saved.getId());
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserOrThrow(id);
        userRepository.delete(user);
        log.info("User={} deleted successfully", id);
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        List<Role> foundRoles = roleRepository.findAllByNameIn(roleNames);
        Set<String> foundNames = foundRoles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> missing = new HashSet<>(roleNames);
        missing.removeAll(foundNames);

        if (!missing.isEmpty()) {
            log.warn("Roles not found={}", String.join(", ", missing));
            throw new RoleNotFoundException("Roles not found: " + String.join(", ", missing));
        }

        return new HashSet<>(foundRoles);
    }

    @Override
    public Page<UserDto> listUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserId={} not found", id);
                    return new UserNotFoundException(id);
                });
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Username={} not found", username);
                    return new UserNotFoundException(username);
                });
    }
}
