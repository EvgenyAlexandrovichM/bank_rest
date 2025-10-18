package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RolesServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public Set<Role> findRolesOrThrow(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> cleaned = names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        List<Role> existing = roleRepository.findAllByNameIn(cleaned);
        Set<String> found = existing.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> missing = new HashSet<>(cleaned);
        missing.removeAll(found);

        if (!missing.isEmpty()){
            throw new RoleNotFoundException(missing.iterator().next());
        }

        return new HashSet<>(existing);
    }
}
