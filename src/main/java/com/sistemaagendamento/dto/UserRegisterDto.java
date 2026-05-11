package com.sistemaagendamento.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserRegisterDto {
    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^\\d{10,11}$",
            message = "Telefone inválido"
    )
    private String phone;

    @NotBlank
    private String password;
}
