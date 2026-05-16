package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.LoginRequestDto;
import com.sistemaagendamento.dto.TokenResponseDto;
import com.sistemaagendamento.dto.UserRegisterDto;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Registro e login de usuários")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra um novo usuário")
    public void register(
            @RequestBody @Valid UserRegisterDto userRegisterDto
    ) throws BadrequestExeption {
        authenticationService.register(userRegisterDto);
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra um novo admin (apenas admin)")
    public void registerAdmin(
            @RequestBody @Valid UserRegisterDto userRegisterDto
    ) throws BadrequestExeption {
        authenticationService.registerAdmin(userRegisterDto);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Realiza login e retorna tokens")
    public TokenResponseDto login(
            @RequestBody @Valid LoginRequestDto loginRequestDto
    ) throws BadrequestExeption {
        return authenticationService.login(loginRequestDto);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Renova o access token via refresh token")
    public TokenResponseDto refresh(
            @RequestParam String refreshToken
    ) {
        return authenticationService.refreshToken(refreshToken);
    }
}