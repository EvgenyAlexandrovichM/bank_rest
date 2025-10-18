package com.example.bankcards.dto.mapper;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesNames")
    UserDto toDto(User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", source = "roles", qualifiedByName = "namesToRoles")
    User toEntity(CreateUserRequest req);

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null) return Collections.emptySet();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Named("namesToRoles")
    default Set<Role> namesToRoles(Set<String> names) {
        if (names == null || names.isEmpty()) return Collections.emptySet();
        return names.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(name -> new Role(null, name))
                .collect(Collectors.toSet());
    }
}
