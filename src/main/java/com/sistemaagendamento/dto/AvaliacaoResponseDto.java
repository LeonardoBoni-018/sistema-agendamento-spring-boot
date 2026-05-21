package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AvaliacaoResponseDto {
    private Integer id;
    private Integer appointmentId;
    private Integer userId;
    private String userName;
    private String jobName;
    private Integer nota;
    private String comentario;
    private LocalDateTime createdAt;
}