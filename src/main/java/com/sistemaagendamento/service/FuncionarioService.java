package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.ComercioEntity;
import com.sistemaagendamento.databse.model.FuncionarioEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IFuncionarioRepository;
import com.sistemaagendamento.dto.FuncionarioDto;
import com.sistemaagendamento.dto.FuncionarioResponseDto;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final IFuncionarioRepository funcionarioRepository;

    public List<FuncionarioResponseDto> findAtivos(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return funcionarioRepository
                .findByComercioIdAndAtivoTrue(user.getComercio().getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<FuncionarioResponseDto> findAll(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return funcionarioRepository
                .findByComercioId(user.getComercio().getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<FuncionarioResponseDto> findAtivosByComercioId(Integer comercioId) {
        return funcionarioRepository
                .findByComercioIdAndAtivoTrue(comercioId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public FuncionarioResponseDto criar(
            FuncionarioDto dto, Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        ComercioEntity comercio = user.getComercio();

        FuncionarioEntity funcionario = FuncionarioEntity.builder()
                .comercio(comercio)
                .nome(dto.getNome())
                .especialidade(dto.getEspecialidade())
                .telefone(dto.getTelefone())
                .email(dto.getEmail())
                .ativo(true)
                .build();

        FuncionarioEntity saved = funcionarioRepository.save(funcionario);
        log.info("Funcionário criado: id={}, comercioId={}",
                saved.getId(), comercio.getId());
        return toDto(saved);
    }

    public FuncionarioResponseDto atualizar(
            Integer id, FuncionarioDto dto, Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        FuncionarioEntity funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Funcionário não encontrado!")
                );

        if (!funcionario.getComercio().getId().equals(
                user.getComercio().getId())) {
            throw new BadrequestExeption("Sem permissão!");
        }

        if (dto.getNome() != null) funcionario.setNome(dto.getNome());
        if (dto.getEspecialidade() != null)
            funcionario.setEspecialidade(dto.getEspecialidade());
        if (dto.getTelefone() != null) funcionario.setTelefone(dto.getTelefone());
        if (dto.getEmail() != null) funcionario.setEmail(dto.getEmail());

        return toDto(funcionarioRepository.save(funcionario));
    }

    public void toggleAtivo(Integer id, Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        FuncionarioEntity funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Funcionário não encontrado!")
                );

        if (!funcionario.getComercio().getId().equals(
                user.getComercio().getId())) {
            throw new BadrequestExeption("Sem permissão!");
        }

        funcionario.setAtivo(!funcionario.getAtivo());
        funcionarioRepository.save(funcionario);
    }

    public FuncionarioEntity findEntityById(Integer id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Funcionário não encontrado!")
                );
    }

    private FuncionarioResponseDto toDto(FuncionarioEntity e) {
        return FuncionarioResponseDto.builder()
                .id(e.getId())
                .nome(e.getNome())
                .especialidade(e.getEspecialidade())
                .telefone(e.getTelefone())
                .email(e.getEmail())
                .ativo(e.getAtivo())
                .comercioId(e.getComercio().getId())
                .comercioNome(e.getComercio().getNome())
                .build();
    }
}