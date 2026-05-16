package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private Integer id;
    private String name;
    private String email;
    private String phone;
}