package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.mapper.UserMapper;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.RoleService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;


    @Override
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(mapper::toDto);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Set<Role> roles = resolveRolesOrDefault(request.getRoles());
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        String newUsername = request.getUsername().trim();
        if (!user.getUsername().equals(newUsername)) {
            if (userRepository.existsByUsername(newUsername)) {
                throw new UsernameAlreadyExistsException(newUsername);
            }
            user.setUsername(newUsername);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = resolveRolesOrDefault(request.getRoles());
            user.setRoles(roles);
        }

        User saved = userRepository.save(user);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public Page<UserDto> listUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page), Math.max(1, size), Sort.by(sortBy == null ? "id" : sortBy)
        );
        return userRepository.findAll(pageable).map(mapper::toDto);
    }

    private Set<Role> resolveRolesOrDefault(Set<String> inputRoles) {
        if (inputRoles == null || inputRoles.isEmpty()) {
            return roleService.findRolesOrThrow(Set.of("ROLE_USER"));
        }

        return roleService.findRolesOrThrow(inputRoles);
    }
}
