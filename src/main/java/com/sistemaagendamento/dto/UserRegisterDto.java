package com.sistemaagendamento.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserRegisterDto {

    @NotBlank(message = "Nome obrigatório")
    private String name;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email obrigatório")
    private String email;

    @NotBlank(message = "Senha obrigatória")
    private String password;

    private String phone;

    // ✅ ID do comércio ao qual o usuário pertence
    @NotNull(message = "Comércio obrigatório")
    private Integer comercioId;
}