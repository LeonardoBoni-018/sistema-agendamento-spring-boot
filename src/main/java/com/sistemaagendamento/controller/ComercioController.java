package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.ComercioDto;
import com.sistemaagendamento.dto.ComercioResponseDto;
import com.sistemaagendamento.service.ComercioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/comercio")
@RequiredArgsConstructor
@Tag(name = "Comércio", description = "Gerenciamento de comércios")
public class ComercioController {

    private final ComercioService comercioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista todos os comércios")
    public List<ComercioResponseDto> findAll() {
        return comercioService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Busca comércio por ID")
    public ComercioResponseDto findById(@PathVariable Integer id) {
        return comercioService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um novo comércio")
    public ComercioResponseDto create(@Valid @RequestBody ComercioDto dto) {
        return comercioService.create(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um comércio")
    public void update(
            @PathVariable Integer id,
            @RequestBody ComercioDto dto
    ) {
        comercioService.update(id, dto);
    }
}