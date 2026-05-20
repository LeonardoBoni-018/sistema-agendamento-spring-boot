package com.sistemaagendamento.service;

import com.sistemaagendamento.config.TokenProvider;
import com.sistemaagendamento.databse.model.ComercioEntity;
import com.sistemaagendamento.databse.model.RoleEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IRolesRepository;
import com.sistemaagendamento.databse.repository.IUserRepository;
import com.sistemaagendamento.dto.AdminSetupDto;
import com.sistemaagendamento.dto.LoginRequestDto;
import com.sistemaagendamento.dto.TokenResponseDto;
import com.sistemaagendamento.dto.UserRegisterDto;
import com.sistemaagendamento.enums.RoleTypeEnum;
import com.sistemaagendamento.exception.BadrequestExeption;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final IUserRepository userRepository;
    private final IRolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final ComercioService comercioService;

    @Value("${jwt.expiration}")
    private long expirationTime;

    public void register(UserRegisterDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BadrequestExeption("Usuário já registrado com este email!");
        }

        RoleEntity role = findOrCreateRole(RoleTypeEnum.ROLE_USER);

        ComercioEntity comercio = null;
        if (dto.getComercioId() != null) {
            comercio = comercioService.findEntityById(dto.getComercioId());
        }

        userRepository.save(UserEntity.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .roles(Set.of(role))
                .password(passwordEncoder.encode(dto.getPassword()))
                .comercio(comercio)
                .build()
        );
    }

    public void registerAdmin(UserRegisterDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BadrequestExeption("Usuário já registrado com este email!");
        }

        RoleEntity role = findOrCreateRole(RoleTypeEnum.ROLE_ADMIN);

        ComercioEntity comercio = null;
        if (dto.getComercioId() != null) {
            comercio = comercioService.findEntityById(dto.getComercioId());
        }

        userRepository.save(UserEntity.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .roles(Set.of(role))
                .password(passwordEncoder.encode(dto.getPassword()))
                .comercio(comercio)
                .build()
        );
    }

    public TokenResponseDto setupInicial(AdminSetupDto dto) {
        if (userRepository.findAll().size() > 0) {
            throw new BadrequestExeption("Setup já foi realizado!");
        }

        if (comercioService.existsAny()) {
            throw new BadrequestExeption("Setup já foi realizado!");
        }

        RoleEntity roleAdmin = findOrCreateRole(RoleTypeEnum.ROLE_ADMIN);

        ComercioEntity comercio = comercioService.createEntity(dto.getComercio());

        UserEntity user = userRepository.save(UserEntity.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .roles(Set.of(roleAdmin))
                .password(passwordEncoder.encode(dto.getPassword()))
                .comercio(comercio)
                .build()
        );


        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );

        String accessToken = tokenProvider.gerarToken(authentication);
        String refreshToken = tokenProvider.gerarRefreshToken(authentication);
        return new TokenResponseDto(accessToken, refreshToken, expirationTime);
    }

    public TokenResponseDto login(LoginRequestDto dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(), dto.getPassword()
                    )
            );
            String accessToken = tokenProvider.gerarToken(authentication);
            String refreshToken = tokenProvider.gerarRefreshToken(authentication);
            return new TokenResponseDto(accessToken, refreshToken, expirationTime);
        } catch (BadCredentialsException e) {
            throw new BadrequestExeption("Credenciais inválidas!");
        }
    }

    public TokenResponseDto refreshToken(String refreshToken) {
        if (!tokenProvider.isValidRefreshToken(refreshToken)) {
            throw new BadrequestExeption("Refresh token inválido ou expirado!");
        }

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new BadrequestExeption("Refresh token inválido ou expirado!");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadrequestExeption("Usuário não encontrado!"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );

        String newAccessToken = tokenProvider.gerarToken(authentication);
        String newRefreshToken = tokenProvider.gerarRefreshToken(authentication);

        tokenBlacklistService.blacklist(refreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken, expirationTime);
    }

    public void logout(String token) {
        if (!tokenProvider.isTokenValid(token)) {
            throw new BadrequestExeption("Token inválido!");
        }
        tokenBlacklistService.blacklist(token);
    }

    private RoleEntity findOrCreateRole(RoleTypeEnum roleType) {
        return rolesRepository.findByName(roleType.name())
                .orElseGet(() -> rolesRepository.save(
                        RoleEntity.builder().name(roleType.name()).build()
                ));
    }
}