package com.sistemaagendamento.dto;

import com.sistemaagendamento.databse.model.FilaEsperaEntity.FilaStatusEnum;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class FilaEsperaResponseDto {
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer jobId;
    private String jobName;
    private Integer comercioId;
    private String comercioNome;
    private Integer funcionarioId;
    private String funcionarioNome;
    private LocalDate date;
    private LocalTime horarioPreferido;
    private FilaStatusEnum status;
    private Integer posicao;
}