package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.FilaEsperaDto;
import com.sistemaagendamento.dto.FilaEsperaResponseDto;
import com.sistemaagendamento.service.FilaEsperaService;
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
@RequestMapping("/v1/fila")
@RequiredArgsConstructor
@Tag(name = "Fila de Espera", description = "Gerenciamento da fila de espera")
public class FilaEsperaController {

    private final FilaEsperaService filaEsperaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Entra na fila de espera")
    public FilaEsperaResponseDto entrar(
            @Valid @RequestBody FilaEsperaDto dto,
            Authentication authentication
    ) {
        return filaEsperaService.entrarNaFila(dto, authentication);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Minha posição na fila")
    public List<FilaEsperaResponseDto> minhaFila(Authentication authentication) {
        return filaEsperaService.minhaFila(authentication);
    }

    @DeleteMapping("/{filaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Sair da fila de espera")
    public void sair(
            @PathVariable Integer filaId,
            Authentication authentication
    ) {
        filaEsperaService.sairDaFila(filaId, authentication);
    }

    @GetMapping("/comercio")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Fila de espera do comércio (admin)")
    public List<FilaEsperaResponseDto> filaDoComercio(
            Authentication authentication
    ) {
        return filaEsperaService.filaDoComercio(authentication);
    }
}