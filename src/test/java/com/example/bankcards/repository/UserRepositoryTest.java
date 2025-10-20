package com.example.bankcards.repository;

import com.example.bankcards.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("user")
                .password("password")
                .enabled(true)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void findByUsername_returnUser_whenExists() {
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("user");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        Optional<User> found = userRepository.findByUsername("user2");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_returnsTrue_whenExists() {
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("user");

        assertThat(exists).isTrue();
    }
}
