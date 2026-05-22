package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.ComercioEntity;
import com.sistemaagendamento.databse.model.HorarioFuncionamentoEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IHorarioFuncionamentoRepository;
import com.sistemaagendamento.dto.HorarioFuncionamentoDto;
import com.sistemaagendamento.dto.HorarioFuncionamentoResponseDto;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HorarioFuncionamentoService {

    private final IHorarioFuncionamentoRepository repository;

    private static final Map<DayOfWeek, String> DIA_LABELS = Map.of(
            DayOfWeek.MONDAY, "Segunda-feira",
            DayOfWeek.TUESDAY, "Terça-feira",
            DayOfWeek.WEDNESDAY, "Quarta-feira",
            DayOfWeek.THURSDAY, "Quinta-feira",
            DayOfWeek.FRIDAY, "Sexta-feira",
            DayOfWeek.SATURDAY, "Sábado",
            DayOfWeek.SUNDAY, "Domingo"
    );

    public List<HorarioFuncionamentoResponseDto> findByComercio(
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return repository.findByComercioId(user.getComercio().getId())
                .stream()
                .map(this::toDto)
                .sorted((a, b) -> a.getDiaSemana().compareTo(b.getDiaSemana()))
                .toList();
    }

    public HorarioFuncionamentoResponseDto salvar(
            HorarioFuncionamentoDto dto,
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        ComercioEntity comercio = user.getComercio();

        if (dto.getAberto() && (dto.getAbertura() == null || dto.getFechamento() == null)) {
            throw new BadrequestExeption(
                    "Informe os horários de abertura e fechamento!"
            );
        }

        if (dto.getAberto() && dto.getAbertura().isAfter(dto.getFechamento())) {
            throw new BadrequestExeption(
                    "Horário de abertura deve ser antes do fechamento!"
            );
        }

        HorarioFuncionamentoEntity entity =
                repository.findByComercioIdAndDiaSemana(
                        comercio.getId(), dto.getDiaSemana()
                ).orElse(
                        HorarioFuncionamentoEntity.builder()
                                .comercio(comercio)
                                .diaSemana(dto.getDiaSemana())
                                .build()
                );

        entity.setAberto(dto.getAberto());
        entity.setAbertura(dto.getAberto() ? dto.getAbertura() : null);
        entity.setFechamento(dto.getAberto() ? dto.getFechamento() : null);

        HorarioFuncionamentoEntity saved = repository.save(entity);

        log.info("Horário salvo: comercioId={}, dia={}, aberto={}",
                comercio.getId(), dto.getDiaSemana(), dto.getAberto());

        return toDto(saved);
    }

    public List<HorarioFuncionamentoResponseDto> findByComercioId(Integer comercioId) {
        return repository.findByComercioId(comercioId)
                .stream()
                .map(this::toDto)
                .sorted((a, b) -> a.getDiaSemana().compareTo(b.getDiaSemana()))
                .toList();
    }

    public HorarioFuncionamentoEntity findByComercioAndDia(
            Integer comercioId, DayOfWeek dia
    ) {
        return repository.findByComercioIdAndDiaSemana(comercioId, dia)
                .orElseThrow(() -> new NotFoundException(
                        "Horário de funcionamento não configurado para este dia!"
                ));
    }

    public boolean isAberto(Integer comercioId, DayOfWeek dia) {
        return repository.findByComercioIdAndDiaSemana(comercioId, dia)
                .map(HorarioFuncionamentoEntity::getAberto)
                .orElse(false);
    }

    private HorarioFuncionamentoResponseDto toDto(HorarioFuncionamentoEntity e) {
        return HorarioFuncionamentoResponseDto.builder()
                .id(e.getId())
                .diaSemana(e.getDiaSemana())
                .diaSemanaLabel(DIA_LABELS.getOrDefault(e.getDiaSemana(), ""))
                .abertura(e.getAbertura())
                .fechamento(e.getFechamento())
                .aberto(e.getAberto())
                .build();
    }
}