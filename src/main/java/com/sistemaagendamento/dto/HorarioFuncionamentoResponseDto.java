package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Builder
public class HorarioFuncionamentoResponseDto {
    private Integer id;
    private DayOfWeek diaSemana;
    private String diaSemanaLabel;
    private LocalTime abertura;
    private LocalTime fechamento;
    private Boolean aberto;
}