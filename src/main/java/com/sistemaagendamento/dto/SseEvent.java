package com.sistemaagendamento.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SseEvent {
    private String type;
    private Object data;
}