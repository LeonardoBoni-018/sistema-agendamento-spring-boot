package com.sistemaagendamento.dto;

import com.sistemaagendamento.databse.model.PagamentoEntity.PagamentoStatusEnum;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PagamentoResponseDto {
    private Integer id;
    private Integer appointmentId;
    private BigDecimal valor;
    private PagamentoStatusEnum status;
    private String checkoutUrl;
    private LocalDateTime paidAt;
}