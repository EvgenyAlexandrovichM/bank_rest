package com.example.bankcards.service;

import com.example.bankcards.entity.Role;

import java.util.Optional;
import java.util.Set;

public interface RoleService {

    Optional<Role> findByName(String name);

    Set<Role> findRolesOrThrow(Set<String> names);
}
