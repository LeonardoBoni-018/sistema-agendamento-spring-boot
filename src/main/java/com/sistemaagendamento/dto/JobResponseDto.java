package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class JobResponseDto {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private Integer comercioId;
    private String comercioNome;
}