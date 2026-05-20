package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.databse.model.ComercioEntity;
import com.sistemaagendamento.databse.model.JobEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IAppointmentsRepository;
import com.sistemaagendamento.databse.repository.IJobRepository;
import com.sistemaagendamento.databse.repository.IUserRepository;
import com.sistemaagendamento.dto.AppointmentDto;
import com.sistemaagendamento.dto.AppointmentResponseDto;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentsService {

    @Value("${appointment.schedule.open-time}")
    private LocalTime openTime;

    @Value("${appointment.schedule.close-time}")
    private LocalTime closeTime;

    private final IAppointmentsRepository appointmentsRepository;
    private final IUserRepository userRepository;
    private final IJobRepository jobRepository;
    private final SseEmitterService sseEmitterService;

    public List<AppointmentResponseDto> myAppointments(
            Authentication authentication,
            AppointmentsStatusEnum status,
            LocalDate date
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        return appointmentsRepository.findByUserId(loggedUser.getId())
                .stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> date == null || a.getDate().equals(date))
                .map(this::toResponseDto)
                .toList();
    }

    public List<AppointmentResponseDto> findAllAppointmentsUser(
            Integer userId,
            Authentication authentication,
            AppointmentsStatusEnum status,
            LocalDate date
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado!"));
        return appointmentsRepository.findByUserId(userId)
                .stream()
                .filter(a -> a.getComercio().getId().equals(loggedUser.getComercio().getId()))
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> date == null || a.getDate().equals(date))
                .map(this::toResponseDto)
                .toList();
    }

    public List<AppointmentResponseDto> findAllAppointments(
            Authentication authentication,
            AppointmentsStatusEnum status,
            LocalDate date
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        return appointmentsRepository.findByComercioId(loggedUser.getComercio().getId())
                .stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> date == null || a.getDate().equals(date))
                .map(this::toResponseDto)
                .toList();
    }

    public AppointmentResponseDto findAppointmentById(
            Integer appointmentId,
            Authentication authentication
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        AppointmentsEntity appointment = appointmentsRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado!"));

        boolean isAdmin = loggedUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!appointment.getComercio().getId().equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption("Você não tem permissão para visualizar este agendamento!");
        }

        if (!isAdmin && !appointment.getUser().getId().equals(loggedUser.getId())) {
            throw new BadrequestExeption("Você não tem permissão para visualizar este agendamento!");
        }

        return toResponseDto(appointment);
    }

    public List<LocalTime> getAvailableTimes(
            LocalDate date, Integer jobId, Authentication authentication
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        if (!job.getComercio().getId().equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption("Serviço não pertence ao seu comércio!");
        }

        List<AppointmentsEntity> booked = appointmentsRepository
                .findByDateAndStatusInAndComercioId(
                        date,
                        List.of(AppointmentsStatusEnum.PENDING, AppointmentsStatusEnum.CONFIRMED),
                        loggedUser.getComercio().getId()
                );

        List<LocalTime> available = new ArrayList<>();
        LocalTime slot = openTime;

        while (!slot.plusMinutes(job.getDurationMinutes()).isAfter(closeTime)) {
            LocalTime slotEnd = slot.plusMinutes(job.getDurationMinutes());
            boolean conflict = false;

            for (AppointmentsEntity b : booked) {
                LocalTime bs = b.getTime();
                LocalTime be = bs.plusMinutes(b.getJob().getDurationMinutes());
                if (slot.isBefore(be) && slotEnd.isAfter(bs)) { conflict = true; break; }
            }

            if (!conflict) available.add(slot);
            slot = slot.plusMinutes(job.getDurationMinutes());
        }

        return available;
    }

    public void userCreateAppointment(
            Authentication authentication,
            AppointmentDto appointmentDto
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        UserEntity user = userRepository.findById(loggedUser.getId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado!"));
        JobEntity job = jobRepository.findById(appointmentDto.getJobId())
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        if (!job.getComercio().getId().equals(user.getComercio().getId())) {
            throw new BadrequestExeption("Serviço não pertence ao seu comércio!");
        }

        validateAppointmentDate(appointmentDto);
        validateScheduleConflict(
                appointmentDto.getDate(), appointmentDto.getTime(),
                job.getDurationMinutes(), user.getComercio().getId()
        );

        AppointmentsEntity appointment = AppointmentsEntity.builder()
                .user(user).job(job).comercio(user.getComercio())
                .date(appointmentDto.getDate()).time(appointmentDto.getTime())
                .status(AppointmentsStatusEnum.PENDING)
                .build();

        AppointmentsEntity saved = appointmentsRepository.save(appointment);
        AppointmentResponseDto dto = toResponseDto(saved);

        sseEmitterService.sendToComercio(
                user.getComercio().getId(),
                "APPOINTMENT_CREATED",
                Map.of(
                        "appointment", dto,
                        "message", "Novo agendamento de " + user.getName()
                                + " para " + job.getName()
                )
        );

        log.info("Agendamento criado: userId={}, jobId={}, comercioId={}",
                user.getId(), job.getId(), user.getComercio().getId());
    }

    public void cancelAppointment(Integer appointmentId, Authentication authentication) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        AppointmentsEntity appointment = appointmentsRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado!"));

        boolean isAdmin = loggedUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!appointment.getComercio().getId().equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption("Você não tem permissão para cancelar este agendamento!");
        }

        if (!isAdmin && !appointment.getUser().getId().equals(loggedUser.getId())) {
            throw new BadrequestExeption("Você não tem permissão para cancelar este agendamento!");
        }

        if (appointment.getStatus() == AppointmentsStatusEnum.CANCELED) {
            throw new BadrequestExeption("Agendamento já cancelado!");
        }

        if (appointment.getStatus() == AppointmentsStatusEnum.FINISHED) {
            throw new BadrequestExeption("Agendamento já finalizado!");
        }

        appointment.setStatus(AppointmentsStatusEnum.CANCELED);
        AppointmentsEntity saved = appointmentsRepository.save(appointment);
        AppointmentResponseDto dto = toResponseDto(saved);

        sseEmitterService.sendToComercio(
                appointment.getComercio().getId(),
                "APPOINTMENT_CANCELED",
                Map.of(
                        "appointment", dto,
                        "message", "Agendamento de " + appointment.getUser().getName()
                                + " foi cancelado"
                )
        );

        log.warn("Agendamento cancelado: id={}, por={}", appointmentId, loggedUser.getId());
    }

    public void updateStatusAppointment(
            Integer appointmentId,
            AppointmentsStatusEnum statusEnum,
            Authentication authentication
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        AppointmentsEntity appointment = appointmentsRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado!"));

        if (!appointment.getComercio().getId().equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption("Você não tem permissão para atualizar este agendamento!");
        }

        appointment.setStatus(statusEnum);
        AppointmentsEntity saved = appointmentsRepository.save(appointment);
        AppointmentResponseDto dto = toResponseDto(saved);

        String message = switch (statusEnum) {
            case CONFIRMED -> "Seu agendamento de " + appointment.getJob().getName() + " foi confirmado!";
            case CANCELED -> "Seu agendamento de " + appointment.getJob().getName() + " foi cancelado.";
            case FINISHED -> "Agendamento de " + appointment.getJob().getName() + " finalizado.";
            default -> "Status do agendamento atualizado.";
        };

        sseEmitterService.sendToComercio(
                appointment.getComercio().getId(),
                "APPOINTMENT_STATUS_UPDATED",
                Map.of("appointment", dto, "message", message)
        );

        log.info("Status atualizado: id={}, status={}", appointmentId, statusEnum);
    }

    private void validateAppointmentDate(AppointmentDto dto) {
        if (dto.getDate().isBefore(LocalDate.now())) {
            throw new BadrequestExeption("Não é possível agendar em datas passadas!");
        }
        if (dto.getDate().equals(LocalDate.now()) && dto.getTime().isBefore(LocalTime.now())) {
            throw new BadrequestExeption("Horário inválido!");
        }
        if (dto.getTime().isBefore(openTime) || dto.getTime().isAfter(closeTime)) {
            throw new BadrequestExeption(
                    "Fora do horário de atendimento! Das " + openTime + " às " + closeTime);
        }
    }

    private void validateScheduleConflict(
            LocalDate date, LocalTime newStart, Integer duration, Integer comercioId
    ) {
        LocalTime newEnd = newStart.plusMinutes(duration);
        List<AppointmentsEntity> appointments = appointmentsRepository
                .findByDateAndStatusInAndComercioId(
                        date,
                        List.of(AppointmentsStatusEnum.PENDING, AppointmentsStatusEnum.CONFIRMED),
                        comercioId
                );

        for (AppointmentsEntity a : appointments) {
            LocalTime es = a.getTime();
            LocalTime ee = es.plusMinutes(a.getJob().getDurationMinutes());
            if (newStart.isBefore(ee) && newEnd.isAfter(es)) {
                throw new BadrequestExeption("Já existe um agendamento neste horário!");
            }
        }
    }

    private AppointmentResponseDto toResponseDto(AppointmentsEntity e) {
        return AppointmentResponseDto.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .userName(e.getUser().getName())
                .jobId(e.getJob().getId())
                .jobName(e.getJob().getName())
                .jobPrice(e.getJob().getPrice())
                .jobDurationMinutes(e.getJob().getDurationMinutes())
                .comercioId(e.getComercio().getId())
                .comercioNome(e.getComercio().getNome())
                .date(e.getDate())
                .time(e.getTime())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}