package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.databse.model.AvaliacaoEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IAppointmentsRepository;
import com.sistemaagendamento.databse.repository.IAvaliacaoRepository;
import com.sistemaagendamento.dto.AvaliacaoDto;
import com.sistemaagendamento.dto.AvaliacaoResponseDto;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
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
public class AvaliacaoService {

    private final IAvaliacaoRepository avaliacaoRepository;
    private final IAppointmentsRepository appointmentsRepository;

    public AvaliacaoResponseDto avaliar(
            Integer appointmentId,
            AvaliacaoDto dto,
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        AppointmentsEntity appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException("Agendamento não encontrado!")
                        );

        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new BadrequestExeption(
                    "Você só pode avaliar seus próprios agendamentos!"
            );
        }

        if (appointment.getStatus() != AppointmentsStatusEnum.FINISHED) {
            throw new BadrequestExeption(
                    "Só é possível avaliar agendamentos finalizados!"
            );
        }

        if (avaliacaoRepository.existsByAppointmentId(appointmentId)) {
            throw new BadrequestExeption(
                    "Este agendamento já foi avaliado!"
            );
        }

        AvaliacaoEntity avaliacao = AvaliacaoEntity.builder()
                .appointment(appointment)
                .user(user)
                .comercio(appointment.getComercio())
                .nota(dto.getNota())
                .comentario(dto.getComentario())
                .build();

        AvaliacaoEntity saved = avaliacaoRepository.save(avaliacao);

        log.info("Avaliação criada: appointmentId={}, nota={}",
                appointmentId, dto.getNota());

        return toDto(saved);
    }

    public List<AvaliacaoResponseDto> findByComercio(
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return avaliacaoRepository
                .findByComercioIdOrderByCreatedAtDesc(user.getComercio().getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public boolean jaAvaliou(Integer appointmentId) {
        return avaliacaoRepository.existsByAppointmentId(appointmentId);
    }

    private AvaliacaoResponseDto toDto(AvaliacaoEntity e) {
        return AvaliacaoResponseDto.builder()
                .id(e.getId())
                .appointmentId(e.getAppointment().getId())
                .userId(e.getUser().getId())
                .userName(e.getUser().getName())
                .jobName(e.getAppointment().getJob().getName())
                .nota(e.getNota())
                .comentario(e.getComentario())
                .createdAt(e.getCreatedAt())
                .build();
    }
}