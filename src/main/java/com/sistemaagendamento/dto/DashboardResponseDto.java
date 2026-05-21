package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashboardResponseDto {

    private Integer totalAgendamentos;
    private Integer agendamentosHoje;
    private Integer agendamentosMes;
    private Integer agendamentosConfirmados;
    private Integer agendamentosPendentes;
    private Integer agendamentosCancelados;
    private Integer agendamentosFinalizados;

    private BigDecimal receitaTotal;
    private BigDecimal receitaMes;
    private BigDecimal receitaHoje;

    private Double mediaAvaliacao;
    private Integer totalAvaliacoes;

    private String servicoMaisAgendado;
    private String horarioPico;

    private List<ReceitaMensalDto> receitaPorMes;
    private List<AgendamentosPorDiaDto> agendamentosPorDia;
    private Map<String, Integer> agendamentosPorStatus;
    private Map<String, Integer> agendamentosPorServico;

    @Getter
    @Builder
    public static class ReceitaMensalDto {
        private String mes;
        private BigDecimal receita;
        private Integer quantidade;
    }

    @Getter
    @Builder
    public static class AgendamentosPorDiaDto {
        private String dia;
        private Integer quantidade;
    }
}