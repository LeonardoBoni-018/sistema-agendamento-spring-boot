package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class AppointmentDto {

    @NotNull(message = "Serviço obrigatório")
    private Integer jobId;

    @NotNull(message = "Data obrigatória")
    private LocalDate date;

    @NotNull(message = "Horário obrigatório")
    private LocalTime time;
}