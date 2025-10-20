package com.example.bankcards.repository;

import com.example.bankcards.entity.role.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_returnsRole_whenExists() {
        Optional<Role> found = roleRepository.findByName("ROLE_USER");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void findByName_returnsEmpty_whenNotExists() {
        Optional<Role> found = roleRepository.findByName("ROLE_UNKNOWN");

        assertThat(found).isEmpty();
    }
}

