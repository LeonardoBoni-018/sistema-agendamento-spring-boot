package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ComercioDto {

    @NotBlank(message = "Nome obrigatório")
    private String nome;

    private String descricao;

    private String telefone;

    private String endereco;
}