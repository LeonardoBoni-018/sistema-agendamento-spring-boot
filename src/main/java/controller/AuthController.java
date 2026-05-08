package controller;

import dto.LoginRequestDto;
import dto.TokenResponseDto;
import dto.UserRegisterDto;
import exception.BadrequestExeption;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.AuthenticationService;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    @PostMapping("/register")
    public void register(@RequestBody @Valid UserRegisterDto userRegisterDto) throws BadrequestExeption {
        authenticationService.register(userRegisterDto);
    }

    @PostMapping("/register/admin")
    public void registerAdmin(@RequestBody @Valid UserRegisterDto userRegisterDto) throws BadrequestExeption {
        authenticationService.registerAdmin(userRegisterDto);
    }

    @PostMapping("/login")
    public TokenResponseDto register(@RequestBody @Valid LoginRequestDto loginRequestDto) throws BadrequestExeption {
        return authenticationService.login(loginRequestDto);
    }
}
