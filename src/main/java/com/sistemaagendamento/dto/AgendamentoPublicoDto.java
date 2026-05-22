package com.sistemaagendamento.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class AgendamentoPublicoDto {

    @NotNull(message = "Serviço obrigatório")
    private Integer jobId;

    @NotNull(message = "Data obrigatória")
    private LocalDate date;

    @NotNull(message = "Horário obrigatório")
    private LocalTime time;

    private Integer funcionarioId;

    @NotBlank(message = "Nome obrigatório")
    private String nome;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email obrigatório")
    private String email;

    @NotBlank(message = "Telefone obrigatório")
    private String telefone;

    private String senha;
}