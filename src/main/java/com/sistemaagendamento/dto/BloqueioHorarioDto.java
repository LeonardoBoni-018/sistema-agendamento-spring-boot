package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class BloqueioHorarioDto {

    @NotNull(message = "Data início obrigatória")
    private LocalDate dataInicio;

    @NotNull(message = "Data fim obrigatória")
    private LocalDate dataFim;

    private LocalTime horaInicio;

    private LocalTime horaFim;

    private String motivo;

    @NotNull(message = "Informe se é dia inteiro")
    private Boolean diaInteiro;
}