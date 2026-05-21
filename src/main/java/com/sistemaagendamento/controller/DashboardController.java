package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.ClienteResponseDto;
import com.sistemaagendamento.dto.DashboardResponseDto;
import com.sistemaagendamento.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analítico do comércio")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retorna dados analíticos do comércio")
    public DashboardResponseDto getDashboard(Authentication authentication) {
        return dashboardService.getDashboard(authentication);
    }

    @GetMapping("/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retorna painel de clientes do comércio")
    public List<ClienteResponseDto> getClientes(Authentication authentication) {
        return dashboardService.getClientes(authentication);
    }
}