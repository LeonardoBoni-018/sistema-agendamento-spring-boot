package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class JobUpdateDto {
    private String name;

    private String description;

    private BigDecimal price;

    private Integer durationMinutes;
}
