package com.sistemaagendamento.controller;

import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Server-Sent Events para atualizações em tempo real")
public class SseController {

    private final SseEmitterService sseEmitterService;

    @GetMapping("/stream")
    @Operation(summary = "Abre stream SSE para receber eventos em tempo real")
    public SseEmitter stream(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        Integer comercioId = user.getComercio() != null ? user.getComercio().getId() : null;
        return sseEmitterService.createEmitter(comercioId);
    }
}