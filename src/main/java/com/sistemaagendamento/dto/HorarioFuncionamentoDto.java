package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
public class HorarioFuncionamentoDto {

    @NotNull(message = "Dia da semana obrigatório")
    private DayOfWeek diaSemana;

    private LocalTime abertura;

    private LocalTime fechamento;

    @NotNull(message = "Informe se o comércio está aberto neste dia")
    private Boolean aberto;
}