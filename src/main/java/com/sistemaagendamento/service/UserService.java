package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IUserRepository;
import com.sistemaagendamento.dto.UserResponseDto;
import com.sistemaagendamento.dto.UserUpdateDto;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;

    public UserResponseDto getMyProfile(Authentication authentication) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        return toResponseDto(loggedUser);
    }

    public void updateMyProfile(
            Authentication authentication,
            UserUpdateDto userUpdateDto
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();

        UserEntity user = userRepository.findById(loggedUser.getId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado!"));

        if (userUpdateDto.getName() != null) user.setName(userUpdateDto.getName());
        if (userUpdateDto.getPhone() != null) user.setPhone(userUpdateDto.getPhone());

        userRepository.save(user);
        log.info("Perfil atualizado: userId={}", user.getId());
    }

    private UserResponseDto toResponseDto(UserEntity entity) {
        return UserResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .comercioId(entity.getComercio() != null
                        ? entity.getComercio().getId() : null)
                .comercioNome(entity.getComercio() != null
                        ? entity.getComercio().getNome() : null)
                .build();
    }
}