package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ComercioResponseDto {
    private Integer id;
    private String nome;
    private String descricao;
    private String telefone;
    private String endereco;
}