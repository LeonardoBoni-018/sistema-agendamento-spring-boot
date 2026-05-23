package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComercioDto {

    @NotBlank(message = "Nome obrigatório")
    private String nome;

    private String descricao;

    private String telefone;

    private String endereco;
}