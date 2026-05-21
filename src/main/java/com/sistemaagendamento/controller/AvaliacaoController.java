package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.AvaliacaoDto;
import com.sistemaagendamento.dto.AvaliacaoResponseDto;
import com.sistemaagendamento.service.AvaliacaoService;
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
@RequestMapping("/v1/avaliacao")
@RequiredArgsConstructor
@Tag(name = "Avaliações", description = "Avaliações pós-atendimento")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @PostMapping("/{appointmentId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Avalia um agendamento finalizado")
    public AvaliacaoResponseDto avaliar(
            @PathVariable Integer appointmentId,
            @Valid @RequestBody AvaliacaoDto dto,
            Authentication authentication
    ) {
        return avaliacaoService.avaliar(appointmentId, dto, authentication);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista avaliações do comércio (admin)")
    public List<AvaliacaoResponseDto> findByComercio(
            Authentication authentication
    ) {
        return avaliacaoService.findByComercio(authentication);
    }
}