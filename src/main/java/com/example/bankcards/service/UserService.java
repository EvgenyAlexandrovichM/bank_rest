package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserService {

    Optional<UserDto> findById(Long id);

    Optional<UserDto> findByUsername(String username);

    boolean existsByUsername(String username);

    UserDto createUser(CreateUserRequest request);

    UserDto updateUser(Long id, CreateUserRequest request);

    void deleteUser(Long id);

    Page<UserDto> listUsers(int page, int size, String sortBy);
}
