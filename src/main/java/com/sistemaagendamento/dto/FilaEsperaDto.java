package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class FilaEsperaDto {

    @NotNull(message = "Serviço obrigatório")
    private Integer jobId;

    @NotNull(message = "Data obrigatória")
    private LocalDate date;

    private LocalTime horarioPreferido;

    private Integer funcionarioId;
}