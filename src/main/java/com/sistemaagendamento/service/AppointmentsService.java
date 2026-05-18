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
                .filter(a -> a.getComercio().getId()
                        .equals(loggedUser.getComercio().getId()))
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

        return appointmentsRepository.findByComercioId(
                        loggedUser.getComercio().getId()
                )
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

        AppointmentsEntity appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException("Agendamento não encontrado!")
                        );

        boolean isAdmin = loggedUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!appointment.getComercio().getId()
                .equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption(
                    "Você não tem permissão para visualizar este agendamento!"
            );
        }

        if (!isAdmin && !appointment.getUser().getId().equals(loggedUser.getId())) {
            throw new BadrequestExeption(
                    "Você não tem permissão para visualizar este agendamento!"
            );
        }

        return toResponseDto(appointment);
    }

    public List<LocalTime> getAvailableTimes(
            LocalDate date,
            Integer jobId,
            Authentication authentication
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        if (!job.getComercio().getId().equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption("Serviço não pertence ao seu comércio!");
        }

        List<AppointmentsEntity> bookedAppointments =
                appointmentsRepository.findByDateAndStatusInAndComercioId(
                        date,
                        List.of(
                                AppointmentsStatusEnum.PENDING,
                                AppointmentsStatusEnum.CONFIRMED
                        ),
                        loggedUser.getComercio().getId()
                );

        List<LocalTime> available = new ArrayList<>();
        LocalTime slot = openTime;

        while (!slot.plusMinutes(job.getDurationMinutes()).isAfter(closeTime)) {
            LocalTime slotEnd = slot.plusMinutes(job.getDurationMinutes());
            boolean conflict = false;

            for (AppointmentsEntity booked : bookedAppointments) {
                LocalTime bookedStart = booked.getTime();
                LocalTime bookedEnd = bookedStart.plusMinutes(
                        booked.getJob().getDurationMinutes()
                );
                if (slot.isBefore(bookedEnd) && slotEnd.isAfter(bookedStart)) {
                    conflict = true;
                    break;
                }
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

        // ✅ Valida se o serviço pertence ao comércio do usuário
        if (!job.getComercio().getId().equals(user.getComercio().getId())) {
            throw new BadrequestExeption(
                    "Serviço não pertence ao seu comércio!"
            );
        }

        validateAppointmentDate(appointmentDto);

        validateScheduleConflict(
                appointmentDto.getDate(),
                appointmentDto.getTime(),
                job.getDurationMinutes(),
                user.getComercio().getId()
        );

        AppointmentsEntity appointment = AppointmentsEntity.builder()
                .user(user)
                .job(job)
                .comercio(user.getComercio())
                .date(appointmentDto.getDate())
                .time(appointmentDto.getTime())
                .status(AppointmentsStatusEnum.PENDING)
                .build();

        appointmentsRepository.save(appointment);

        log.info("Agendamento criado: userId={}, jobId={}, comercioId={}, date={}",
                user.getId(), job.getId(),
                user.getComercio().getId(), appointmentDto.getDate());
    }

    private void validateAppointmentDate(AppointmentDto appointmentDto) {
        if (appointmentDto.getDate().isBefore(LocalDate.now())) {
            throw new BadrequestExeption(
                    "Não é possível agendar em datas passadas!"
            );
        }

        if (appointmentDto.getDate().equals(LocalDate.now()) &&
                appointmentDto.getTime().isBefore(LocalTime.now())) {
            throw new BadrequestExeption("Horário inválido!");
        }

        if (appointmentDto.getTime().isBefore(openTime) ||
                appointmentDto.getTime().isAfter(closeTime)) {
            throw new BadrequestExeption(
                    "Agendamento fora do horário de atendimento! " +
                            "Atendemos das " + openTime + " às " + closeTime + "."
            );
        }
    }

    private void validateScheduleConflict(
            LocalDate date,
            LocalTime newStartTime,
            Integer durationMinutes,
            Integer comercioId
    ) {
        LocalTime newEndTime = newStartTime.plusMinutes(durationMinutes);

        // ✅ Verifica conflito apenas dentro do mesmo comércio
        List<AppointmentsEntity> appointments =
                appointmentsRepository.findByDateAndStatusInAndComercioId(
                        date,
                        List.of(
                                AppointmentsStatusEnum.PENDING,
                                AppointmentsStatusEnum.CONFIRMED
                        ),
                        comercioId
                );

        for (AppointmentsEntity appointment : appointments) {
            LocalTime existingStart = appointment.getTime();
            LocalTime existingEnd = existingStart.plusMinutes(
                    appointment.getJob().getDurationMinutes()
            );

            boolean hasConflict =
                    newStartTime.isBefore(existingEnd) &&
                            newEndTime.isAfter(existingStart);

            if (hasConflict) {
                throw new BadrequestExeption(
                        "Já existe um agendamento neste horário!"
                );
            }
        }
    }

    public void cancelAppointment(
            Integer appointmentId,
            Authentication authentication
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();

        AppointmentsEntity appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException("Agendamento não encontrado!")
                        );

        boolean isAdmin = loggedUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // ✅ Valida comércio + dono
        if (!appointment.getComercio().getId()
                .equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption(
                    "Você não tem permissão para cancelar este agendamento!"
            );
        }

        if (!isAdmin && !appointment.getUser().getId().equals(loggedUser.getId())) {
            throw new BadrequestExeption(
                    "Você não tem permissão para cancelar este agendamento!"
            );
        }

        if (appointment.getStatus() == AppointmentsStatusEnum.CANCELED) {
            throw new BadrequestExeption("Agendamento já cancelado!");
        }

        if (appointment.getStatus() == AppointmentsStatusEnum.FINISHED) {
            throw new BadrequestExeption("Agendamento já finalizado!");
        }

        appointment.setStatus(AppointmentsStatusEnum.CANCELED);
        appointmentsRepository.save(appointment);

        log.warn("Agendamento cancelado: id={}, canceladoPor={}",
                appointmentId, loggedUser.getId());
    }

    public void updateStatusAppointment(
            Integer appointmentId,
            AppointmentsStatusEnum statusEnum,
            Authentication authentication
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();

        AppointmentsEntity appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException("Agendamento não encontrado!")
                        );

        if (!appointment.getComercio().getId()
                .equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption(
                    "Você não tem permissão para atualizar este agendamento!"
            );
        }

        appointment.setStatus(statusEnum);
        appointmentsRepository.save(appointment);

        log.info("Status atualizado: id={}, novoStatus={}", appointmentId, statusEnum);
    }

    private AppointmentResponseDto toResponseDto(AppointmentsEntity entity) {
        return AppointmentResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .userName(entity.getUser().getName())
                .jobId(entity.getJob().getId())
                .jobName(entity.getJob().getName())
                .jobPrice(entity.getJob().getPrice())
                .jobDurationMinutes(entity.getJob().getDurationMinutes())
                .comercioId(entity.getComercio().getId())
                .comercioNome(entity.getComercio().getNome())
                .date(entity.getDate())
                .time(entity.getTime())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}