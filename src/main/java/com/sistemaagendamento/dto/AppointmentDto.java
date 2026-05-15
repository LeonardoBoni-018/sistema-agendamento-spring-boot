package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class AppointmentDto {

    @NotNull
    private Integer jobId;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;
}