package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.ConfiguracaoPagamentoDto;
import com.sistemaagendamento.dto.PagamentoResponseDto;
import com.sistemaagendamento.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/pagamento")
@RequiredArgsConstructor
@Tag(name = "Pagamento", description = "Pagamento antecipado via MercadoPago")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping("/checkout/{appointmentId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gera link de pagamento para um agendamento")
    public PagamentoResponseDto gerarCheckout(
            @PathVariable Integer appointmentId,
            Authentication authentication
    ) {
        return pagamentoService.gerarCheckout(appointmentId, authentication);
    }

    @GetMapping("/appointment/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Busca pagamento de um agendamento")
    public PagamentoResponseDto findByAppointment(
            @PathVariable Integer appointmentId
    ) {
        return pagamentoService.findByAppointment(appointmentId);
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Webhook do MercadoPago")
    public void webhook(@RequestBody Map<String, Object> payload) {
        pagamentoService.processarWebhook(payload);
    }

    @PostMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Salva configuração de pagamento do comércio")
    public void salvarConfig(
            @Valid @RequestBody ConfiguracaoPagamentoDto dto,
            Authentication authentication
    ) {
        pagamentoService.salvarConfig(dto, authentication);
    }
}