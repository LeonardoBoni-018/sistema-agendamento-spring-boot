package com.sistemaagendamento.dto;

import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class AppointmentResponseDto {
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer jobId;
    private String jobName;
    private BigDecimal jobPrice;
    private Integer jobDurationMinutes;
    private LocalDate date;
    private LocalTime time;
    private AppointmentsStatusEnum status;
}