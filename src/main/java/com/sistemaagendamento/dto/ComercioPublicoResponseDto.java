package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ComercioPublicoResponseDto {
    private Integer id;
    private String nome;
    private String descricao;
    private String telefone;
    private String endereco;
    private List<JobResponseDto> servicos;
    private List<FuncionarioResponseDto> funcionarios;
    private List<HorarioFuncionamentoResponseDto> horarios;
}