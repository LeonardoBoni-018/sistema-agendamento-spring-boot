package com.sistemaagendamento.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserUpdateDto {

    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @Size(min = 8, max = 11, message = "Telefone inválido")
    private String phone;
}