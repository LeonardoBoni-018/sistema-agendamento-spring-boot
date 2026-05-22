package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.FuncionarioDto;
import com.sistemaagendamento.dto.FuncionarioResponseDto;
import com.sistemaagendamento.service.FuncionarioService;
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
@RequestMapping("/v1/funcionario")
@RequiredArgsConstructor
@Tag(name = "Funcionários", description = "Gerenciamento de profissionais")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista funcionários ativos do comércio")
    public List<FuncionarioResponseDto> findAtivos(Authentication authentication) {
        return funcionarioService.findAtivos(authentication);
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista todos os funcionários (admin)")
    public List<FuncionarioResponseDto> findAll(Authentication authentication) {
        return funcionarioService.findAll(authentication);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo funcionário")
    public FuncionarioResponseDto criar(
            @Valid @RequestBody FuncionarioDto dto,
            Authentication authentication
    ) {
        return funcionarioService.criar(dto, authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualiza dados do funcionário")
    public FuncionarioResponseDto atualizar(
            @PathVariable Integer id,
            @RequestBody FuncionarioDto dto,
            Authentication authentication
    ) {
        return funcionarioService.atualizar(id, dto, authentication);
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Ativa ou desativa funcionário")
    public void toggleAtivo(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        funcionarioService.toggleAtivo(id, authentication);
    }
}