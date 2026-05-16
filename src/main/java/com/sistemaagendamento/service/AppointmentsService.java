package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentsService {

    // Horário de funcionamento
    private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(18, 0);

    private final IAppointmentsRepository appointmentsRepository;
    private final IUserRepository userRepository;
    private final IJobRepository jobRepository;

    public List<AppointmentResponseDto> myAppointments(
            Authentication authentication,
            AppointmentsStatusEnum status,
            LocalDate date
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();

        List<AppointmentsEntity> appointments =
                appointmentsRepository.findByUserId(loggedUser.getId());

        return appointments.stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> date == null || a.getDate().equals(date))
                .map(this::toResponseDto)
                .toList();
    }

    public List<AppointmentResponseDto> findAllAppointmentsUser(
            Integer userId,
            AppointmentsStatusEnum status,
            LocalDate date
    ) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado!"));

        List<AppointmentsEntity> appointments =
                appointmentsRepository.findByUserId(userId);

        return appointments.stream()
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

        if (!isAdmin && !appointment.getUser().getId().equals(loggedUser.getId())) {
            throw new BadrequestExeption(
                    "Você não tem permissão para visualizar este agendamento!"
            );
        }

        return toResponseDto(appointment);
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

        validateAppointmentDate(appointmentDto);

        validateScheduleConflict(
                appointmentDto.getDate(),
                appointmentDto.getTime(),
                job.getDurationMinutes()
        );

        AppointmentsEntity appointment = AppointmentsEntity.builder()
                .user(user)
                .job(job)
                .date(appointmentDto.getDate())
                .time(appointmentDto.getTime())
                .status(AppointmentsStatusEnum.PENDING)
                .build();

        appointmentsRepository.save(appointment);
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

        // Validação de horário de funcionamento
        if (appointmentDto.getTime().isBefore(OPEN_TIME) ||
                appointmentDto.getTime().isAfter(CLOSE_TIME)) {
            throw new BadrequestExeption(
                    "Agendamento fora do horário de atendimento! " +
                            "Atendemos das " + OPEN_TIME + " às " + CLOSE_TIME + "."
            );
        }
    }

    private void validateScheduleConflict(
            LocalDate date,
            LocalTime newStartTime,
            Integer durationMinutes
    ) {
        LocalTime newEndTime = newStartTime.plusMinutes(durationMinutes);

        List<AppointmentsEntity> appointments =
                appointmentsRepository.findByDateAndStatusIn(
                        date,
                        List.of(
                                AppointmentsStatusEnum.PENDING,
                                AppointmentsStatusEnum.CONFIRMED
                        )
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

        // Garante que apenas o dono ou admin pode cancelar
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
    }

    public void updateStatusAppointment(
            Integer appointmentId,
            AppointmentsStatusEnum statusEnum
    ) {
        AppointmentsEntity appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException("Agendamento não encontrado!")
                        );

        appointment.setStatus(statusEnum);
        appointmentsRepository.save(appointment);
    }

    // Mapper centralizado - evita repetição
    private AppointmentResponseDto toResponseDto(AppointmentsEntity entity) {
        return AppointmentResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .userName(entity.getUser().getName())
                .jobId(entity.getJob().getId())
                .jobName(entity.getJob().getName())
                .jobPrice(entity.getJob().getPrice())
                .jobDurationMinutes(entity.getJob().getDurationMinutes())
                .date(entity.getDate())
                .time(entity.getTime())
                .status(entity.getStatus())
                .build();
    }
}