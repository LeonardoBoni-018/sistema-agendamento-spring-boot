package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FuncionarioResponseDto {
    private Integer id;
    private String nome;
    private String especialidade;
    private String telefone;
    private String email;
    private Boolean ativo;
    private Integer comercioId;
    private String comercioNome;
}