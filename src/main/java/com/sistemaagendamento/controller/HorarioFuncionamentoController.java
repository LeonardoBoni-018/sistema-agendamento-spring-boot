package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.HorarioFuncionamentoDto;
import com.sistemaagendamento.dto.HorarioFuncionamentoResponseDto;
import com.sistemaagendamento.service.HorarioFuncionamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/horario-funcionamento")
@RequiredArgsConstructor
@Tag(name = "Horário de Funcionamento",
        description = "Configuração de horários por dia da semana")
public class HorarioFuncionamentoController {

    private final HorarioFuncionamentoService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista horários do comércio do usuário logado")
    public List<HorarioFuncionamentoResponseDto> findAll(
            Authentication authentication
    ) {
        return service.findByComercio(authentication);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Salva ou atualiza horário de um dia da semana")
    public HorarioFuncionamentoResponseDto salvar(
            @Valid @RequestBody HorarioFuncionamentoDto dto,
            Authentication authentication
    ) {
        return service.salvar(dto, authentication);
    }
}