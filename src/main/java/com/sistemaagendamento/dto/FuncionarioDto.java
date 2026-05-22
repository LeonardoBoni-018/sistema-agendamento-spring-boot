package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuncionarioDto {

    @NotBlank(message = "Nome obrigatório")
    private String nome;

    private String especialidade;
    private String telefone;
    private String email;
}