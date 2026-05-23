package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReagendamentoDto {

    @NotNull(message = "Nova data obrigatória")
    private LocalDate novaData;

    @NotNull(message = "Novo horário obrigatório")
    private LocalTime novoHorario;
}