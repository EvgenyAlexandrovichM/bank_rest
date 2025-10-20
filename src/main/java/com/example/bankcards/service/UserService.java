package com.example.bankcards.service;

import com.example.bankcards.dto.user.UpdateUserRequest;
import com.example.bankcards.dto.user.UserDto;
import org.springframework.data.domain.Page;


public interface UserService {

    UserDto findById(Long id);

    UserDto findByUsername(String username);

    boolean existsByUsername(String username);

    UserDto updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    Page<UserDto> listUsers(int page, int size, String sortBy);
}
