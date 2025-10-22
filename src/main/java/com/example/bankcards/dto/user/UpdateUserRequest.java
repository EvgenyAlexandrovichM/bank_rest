package com.example.bankcards.dto.user;

import com.example.bankcards.util.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    @ValidPassword
    private String password;

    private Set<String> roles;
}
