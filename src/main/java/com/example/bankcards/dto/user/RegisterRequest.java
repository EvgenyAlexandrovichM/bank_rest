package com.example.bankcards.dto.user;

import com.example.bankcards.util.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank
    @ValidPassword
    @Size(min = 6, max = 255)
    private String password;
}
