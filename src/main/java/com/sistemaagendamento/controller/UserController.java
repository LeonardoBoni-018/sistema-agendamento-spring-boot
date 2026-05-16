package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.UserResponseDto;
import com.sistemaagendamento.dto.UserUpdateDto;
import com.sistemaagendamento.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento do perfil do usuário")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retorna os dados do usuário logado")
    public UserResponseDto getMyProfile(Authentication authentication) {
        return userService.getMyProfile(authentication);
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualiza nome e telefone do usuário logado")
    public void updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UserUpdateDto userUpdateDto
    ) {
        userService.updateMyProfile(authentication, userUpdateDto);
    }
}