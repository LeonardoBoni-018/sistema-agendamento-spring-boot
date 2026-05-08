package service;


import config.TokenProvider;
import databse.model.RoleEntity;
import databse.model.UserEntity;
import databse.repository.IRolesRepository;
import databse.repository.IUserRepository;
import dto.LoginRequestDto;
import dto.TokenResponseDto;
import dto.UserRegisterDto;
import enums.RoleTypeEnum;
import exception.BadrequestExeption;
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
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    @Value("${jwt.expiration}")
    private long expirationTime;

    public void register(UserRegisterDto userRegisterDto) throws BadrequestExeption{
        UserEntity user = userRepository.findByEmail(userRegisterDto.getEmail()).orElse(null);

        if(user != null){
            throw new BadrequestExeption("Usuário já registrado com este email!");
        }

        RoleEntity role = rolesRepository.findByNome(RoleTypeEnum.ROLE_USER.name()).orElseGet(() -> rolesRepository.save(RoleEntity.builder()
                        .name(RoleTypeEnum.ROLE_USER.name())
                .build()));

        userRepository.save(UserEntity.builder()
                .name(userRegisterDto.getName())
                .email(userRegisterDto.getEmail())
                        .phone(String.valueOf(userRegisterDto.getPhone()))
                .roles(Set.of(role))
                .password(passwordEncoder.encode(userRegisterDto.getPassword()))
                .build()
        );
    }

    public void registerAdmin(UserRegisterDto userRegisterDto){
        UserEntity user = userRepository.findByEmail(userRegisterDto.getEmail()).orElse(null);

        if(user != null){
            throw new BadrequestExeption("Usuário já registrado com este email!");
        }

        RoleEntity role = rolesRepository.findByNome(RoleTypeEnum.ROLE_ADMIN.name()).orElseGet(() -> rolesRepository.save(RoleEntity.builder()
                .name(RoleTypeEnum.ROLE_ADMIN.name())
                .build()));

        userRepository.save(UserEntity.builder()
                .name(userRegisterDto.getName())
                .email(userRegisterDto.getEmail())
                .phone(String.valueOf(userRegisterDto.getPhone()))
                .roles(Set.of(role))
                .password(passwordEncoder.encode(userRegisterDto.getPassword()))
                .build()
        );
    }

    public TokenResponseDto login(LoginRequestDto dto) throws BadrequestExeption {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
            String token = tokenProvider.gerarToken(authentication);
            return new TokenResponseDto(token, expirationTime);
        } catch (BadCredentialsException e){
            throw new BadrequestExeption("Credencias inválidas");
        } catch (Exception e){
            throw e;
        }
    }
}
