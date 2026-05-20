package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class BloqueioHorarioResponseDto {
    private Integer id;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String motivo;
    private Boolean diaInteiro;
}