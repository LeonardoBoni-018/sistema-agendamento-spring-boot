package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.ComercioEntity;
import com.sistemaagendamento.databse.repository.IComercioRepository;
import com.sistemaagendamento.dto.ComercioDto;
import com.sistemaagendamento.dto.ComercioResponseDto;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComercioService {

    private final IComercioRepository comercioRepository;

    public List<ComercioResponseDto> findAll() {
        return comercioRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public ComercioResponseDto findById(Integer id) {
        return comercioRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new NotFoundException("Comércio não encontrado!"));
    }

    public ComercioResponseDto create(ComercioDto dto) {
        ComercioEntity comercio = createEntity(dto);
        log.info("Comércio criado: id={}, nome={}", comercio.getId(), comercio.getNome());
        return toResponseDto(comercio);
    }

    public ComercioEntity createEntity(ComercioDto dto) {
        return comercioRepository.save(
                ComercioEntity.builder()
                        .nome(dto.getNome())
                        .descricao(dto.getDescricao())
                        .telefone(dto.getTelefone())
                        .endereco(dto.getEndereco())
                        .build()
        );
    }

    public void update(Integer id, ComercioDto dto) {
        ComercioEntity comercio = comercioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comércio não encontrado!"));

        if (dto.getNome() != null) comercio.setNome(dto.getNome());
        if (dto.getDescricao() != null) comercio.setDescricao(dto.getDescricao());
        if (dto.getTelefone() != null) comercio.setTelefone(dto.getTelefone());
        if (dto.getEndereco() != null) comercio.setEndereco(dto.getEndereco());

        comercioRepository.save(comercio);
        log.info("Comércio atualizado: id={}", id);
    }

    public ComercioEntity findEntityById(Integer id) {
        return comercioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comércio não encontrado!"));
    }

    public boolean existsAny() {
        return !comercioRepository.findAll().isEmpty();
    }

    public ComercioResponseDto setupPrimeiroComercio(ComercioDto dto) {
        if (existsAny()) {
            throw new RuntimeException("Comércio já existe. Use a rota autenticada para criar novos.");
        }
        return create(dto);
    }

    private ComercioResponseDto toResponseDto(ComercioEntity entity) {
        return ComercioResponseDto.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .telefone(entity.getTelefone())
                .endereco(entity.getEndereco())
                .build();
    }
}