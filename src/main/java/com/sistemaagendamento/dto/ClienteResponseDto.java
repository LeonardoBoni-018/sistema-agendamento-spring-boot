package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ClienteResponseDto {
    private Integer userId;
    private String nome;
    private String email;
    private String telefone;
    private Integer totalAgendamentos;
    private Integer agendamentosFinalizados;
    private Integer agendamentosCancelados;
    private BigDecimal ticketTotal;
    private BigDecimal ticketMedio;
    private LocalDate primeiroAgendamento;
    private LocalDate ultimoAgendamento;
    private String servicoFavorito;
    private Double mediaAvaliacao;
}