package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.AgendamentoPublicoDto;
import com.sistemaagendamento.dto.ComercioPublicoResponseDto;
import com.sistemaagendamento.service.AgendamentoPublicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/v1/publico")
@RequiredArgsConstructor
@Tag(name = "Agendamento Público",
        description = "Endpoints públicos para agendamento sem login")
public class AgendamentoPublicoController {

    private final AgendamentoPublicoService agendamentoPublicoService;

    @GetMapping("/comercio/{comercioId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retorna dados públicos do comércio")
    public ComercioPublicoResponseDto getComercio(
            @PathVariable Integer comercioId
    ) {
        return agendamentoPublicoService.getComercioPublico(comercioId);
    }

    @GetMapping("/comercio/{comercioId}/horarios")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retorna horários disponíveis")
    public List<LocalTime> getHorarios(
            @PathVariable Integer comercioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam Integer jobId,
            @RequestParam(required = false) Integer funcionarioId
    ) {
        return agendamentoPublicoService.getHorariosDisponiveis(
                comercioId, date, jobId, funcionarioId
        );
    }

    @PostMapping("/comercio/{comercioId}/agendar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria agendamento sem login")
    public void agendar(
            @PathVariable Integer comercioId,
            @Valid @RequestBody AgendamentoPublicoDto dto
    ) {
        agendamentoPublicoService.agendar(comercioId, dto);
    }
}