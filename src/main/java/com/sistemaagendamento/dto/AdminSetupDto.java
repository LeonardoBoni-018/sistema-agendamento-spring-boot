package com.sistemaagendamento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSetupDto {

    @NotBlank(message = "Nome do usuário obrigatório")
    private String name;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email obrigatório")
    private String email;

    @NotBlank(message = "Senha obrigatória")
    private String password;

    private String phone;

    @Valid
    private ComercioDto comercio;
}