package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.BloqueioHorarioDto;
import com.sistemaagendamento.dto.BloqueioHorarioResponseDto;
import com.sistemaagendamento.service.BloqueioHorarioService;
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
@RequestMapping("/v1/bloqueio")
@RequiredArgsConstructor
@Tag(name = "Bloqueios de Horário",
        description = "Feriados, folgas e horários bloqueados")
public class BloqueioHorarioController {

    private final BloqueioHorarioService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista bloqueios do comércio")
    public List<BloqueioHorarioResponseDto> findAll(
            Authentication authentication
    ) {
        return service.findByComercio(authentication);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo bloqueio de horário")
    public BloqueioHorarioResponseDto criar(
            @Valid @RequestBody BloqueioHorarioDto dto,
            Authentication authentication
    ) {
        return service.criar(dto, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um bloqueio")
    public void deletar(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        service.deletar(id, authentication);
    }
}