package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.BloqueioHorarioEntity;
import com.sistemaagendamento.databse.model.ComercioEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IBloqueioHorarioRepository;
import com.sistemaagendamento.dto.BloqueioHorarioDto;
import com.sistemaagendamento.dto.BloqueioHorarioResponseDto;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloqueioHorarioService {

    private final IBloqueioHorarioRepository repository;

    public List<BloqueioHorarioResponseDto> findByComercio(
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return repository.findByComercioId(user.getComercio().getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public BloqueioHorarioResponseDto criar(
            BloqueioHorarioDto dto,
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        ComercioEntity comercio = user.getComercio();

        if (dto.getDataInicio().isAfter(dto.getDataFim())) {
            throw new BadrequestExeption(
                    "Data início deve ser antes ou igual à data fim!"
            );
        }

        if (!dto.getDiaInteiro() &&
                (dto.getHoraInicio() == null || dto.getHoraFim() == null)) {
            throw new BadrequestExeption(
                    "Informe os horários de início e fim do bloqueio!"
            );
        }

        if (!dto.getDiaInteiro() &&
                dto.getHoraInicio().isAfter(dto.getHoraFim())) {
            throw new BadrequestExeption(
                    "Hora início deve ser antes da hora fim!"
            );
        }

        BloqueioHorarioEntity entity = BloqueioHorarioEntity.builder()
                .comercio(comercio)
                .dataInicio(dto.getDataInicio())
                .dataFim(dto.getDataFim())
                .horaInicio(dto.getDiaInteiro() ? null : dto.getHoraInicio())
                .horaFim(dto.getDiaInteiro() ? null : dto.getHoraFim())
                .motivo(dto.getMotivo())
                .diaInteiro(dto.getDiaInteiro())
                .build();

        BloqueioHorarioEntity saved = repository.save(entity);

        log.info("Bloqueio criado: comercioId={}, de={} até={}",
                comercio.getId(), dto.getDataInicio(), dto.getDataFim());

        return toDto(saved);
    }

    public void deletar(Integer id, Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        BloqueioHorarioEntity bloqueio = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bloqueio não encontrado!"));

        if (!bloqueio.getComercio().getId().equals(user.getComercio().getId())) {
            throw new BadrequestExeption(
                    "Você não tem permissão para remover este bloqueio!"
            );
        }

        repository.deleteById(id);
        log.info("Bloqueio removido: id={}", id);
    }

    public boolean isHorarioBloqueado(
            Integer comercioId,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim
    ) {
        List<BloqueioHorarioEntity> bloqueios =
                repository.findByComercioIdAndData(comercioId, data);

        for (BloqueioHorarioEntity b : bloqueios) {
            if (b.getDiaInteiro()) return true;

            boolean sobreposicao =
                    horaInicio.isBefore(b.getHoraFim()) &&
                            horaFim.isAfter(b.getHoraInicio());

            if (sobreposicao) return true;
        }

        return false;
    }

    private BloqueioHorarioResponseDto toDto(BloqueioHorarioEntity e) {
        return BloqueioHorarioResponseDto.builder()
                .id(e.getId())
                .dataInicio(e.getDataInicio())
                .dataFim(e.getDataFim())
                .horaInicio(e.getHoraInicio())
                .horaFim(e.getHoraFim())
                .motivo(e.getMotivo())
                .diaInteiro(e.getDiaInteiro())
                .build();
    }
}