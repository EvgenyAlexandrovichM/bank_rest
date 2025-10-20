package com.example.bankcards.service;

import com.example.bankcards.dto.mapper.UserMapper;
import com.example.bankcards.dto.user.UpdateUserRequest;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;
    private UserDto existingUserDto;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .username("user")
                .password("password")
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        existingUserDto = UserDto.builder()
                .id(1L)
                .username("user")
                .enabled(true)
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    @Test
    void findById_returnsUserDto_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userMapper.toDto(existingUser)).thenReturn(existingUserDto);

        UserDto result = userService.findById(1L);

        assertThat(result.getUsername()).isEqualTo("user");
    }

    @Test
    void findById_throwsException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_changesUsernameAndPassword_whenValid() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newuser")
                .password("newpass")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(
                UserDto.builder()
                        .id(1L)
                        .username("newuser")
                        .build()
        );

        UserDto result = userService.updateUser(1L, request);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(existingUser.getPassword()).isEqualTo("ENCODED");

    }

    @Test
    void updateUser_doesNotUpdatePassword_whenPasswordIsNull() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newuser")
                .password(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(
                UserDto.builder()
                        .id(1L)
                        .username("newuser")
                        .build()
        );

        UserDto result = userService.updateUser(1L, request);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(existingUser.getPassword()).isEqualTo("password");
    }

    @Test
    void updateUser_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateUserRequest request = new UpdateUserRequest("user2", "password2", Set.of("ROLE_USER"));

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_throwsException_whenUsernameAlreadyExists() {
        UpdateUserRequest request = new UpdateUserRequest("user2", "password", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("user2")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void deleteUser_removesUser_whenExists() {
        User existing = User
                .builder()
                .id(1L)
                .username("user")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.deleteUser(1L);

        verify(userRepository).delete(existing);
    }

    @Test
    void deleteUser_throwsException_whenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void listUsers_returnsPagedDtos() {
        User user = User.builder()
                .id(1L)
                .username("user")
                .build();
        UserDto dto = UserDto.builder()
                .id(1L)
                .username("user").build();

        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(userMapper.toDto(user)).thenReturn(dto);

        Page<UserDto> result = userService.listUsers(0, 10, "id");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("user");
    }
}
