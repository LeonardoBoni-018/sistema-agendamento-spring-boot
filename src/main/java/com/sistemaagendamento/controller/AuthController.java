package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.LoginRequestDto;
import com.sistemaagendamento.dto.TokenResponseDto;
import com.sistemaagendamento.dto.UserRegisterDto;
import com.sistemaagendamento.exception.BadrequestExeption;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.sistemaagendamento.service.AuthenticationService;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody @Valid UserRegisterDto userRegisterDto) throws BadrequestExeption {
        authenticationService.register(userRegisterDto);
    }

    @PostMapping("/register/admin")
    @ResponseStatus(HttpStatus.OK)
    public void registerAdmin(@RequestBody @Valid UserRegisterDto userRegisterDto) throws BadrequestExeption {
        authenticationService.registerAdmin(userRegisterDto);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponseDto register(@RequestBody @Valid LoginRequestDto loginRequestDto) throws BadrequestExeption {
        return authenticationService.login(loginRequestDto);
    }
}
