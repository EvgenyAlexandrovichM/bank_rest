package com.example.bankcards.dto;

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
public class CreateUserRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank
    @Size(min = 6, max = 255)
    @ValidPassword
    private String password;

    private Set<String> roles;

}
