package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.*;
import com.sistemaagendamento.databse.repository.*;
import com.sistemaagendamento.dto.*;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentsService {

    private final IAppointmentsRepository appointmentsRepository;
    private final IUserRepository userRepository;
    private final IJobRepository jobRepository;
    private final SseEmitterService sseEmitterService;
    private final HorarioFuncionamentoService horarioService;
    private final BloqueioHorarioService bloqueioService;
    private final IAvaliacaoRepository avaliacaoRepository;

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
                .filter(a -> a.getComercio().getId().equals(
                        loggedUser.getComercio().getId()))
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
        return appointmentsRepository
                .findByComercioId(loggedUser.getComercio().getId())
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

        boolean isAdmin = isAdmin(loggedUser);

        if (!appointment.getComercio().getId()
                .equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption("Sem permissão para visualizar!");
        }

        if (!isAdmin && !appointment.getUser().getId().equals(loggedUser.getId())) {
            throw new BadrequestExeption("Sem permissão para visualizar!");
        }

        return toResponseDto(appointment);
    }

    // ✅ Horários disponíveis agora respeitam horário por dia + bloqueios
    public List<LocalTime> getAvailableTimes(
            LocalDate date,
            Integer jobId,
            Authentication authentication
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        Integer comercioId = loggedUser.getComercio().getId();

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        if (!job.getComercio().getId().equals(comercioId)) {
            throw new BadrequestExeption("Serviço não pertence ao seu comércio!");
        }

        DayOfWeek diaSemana = date.getDayOfWeek();

        // ✅ Verifica se o comércio abre neste dia
        if (!horarioService.isAberto(comercioId, diaSemana)) {
            return List.of();
        }

        HorarioFuncionamentoEntity horario =
                horarioService.findByComercioAndDia(comercioId, diaSemana);

        List<AppointmentsEntity> booked =
                appointmentsRepository.findByDateAndStatusInAndComercioId(
                        date,
                        List.of(AppointmentsStatusEnum.PENDING,
                                AppointmentsStatusEnum.CONFIRMED),
                        comercioId
                );

        List<LocalTime> available = new ArrayList<>();
        LocalTime slot = horario.getAbertura();

        while (!slot.plusMinutes(job.getDurationMinutes())
                .isAfter(horario.getFechamento())) {

            LocalTime slotEnd = slot.plusMinutes(job.getDurationMinutes());

            // ✅ Verifica bloqueios
            boolean bloqueado = bloqueioService.isHorarioBloqueado(
                    comercioId, date, slot, slotEnd
            );

            if (!bloqueado) {
                boolean conflict = false;
                for (AppointmentsEntity b : booked) {
                    LocalTime bs = b.getTime();
                    LocalTime be = bs.plusMinutes(
                            b.getJob().getDurationMinutes()
                    );
                    if (slot.isBefore(be) && slotEnd.isAfter(bs)) {
                        conflict = true;
                        break;
                    }
                }

                if (!conflict) available.add(slot);
            }

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

        validateAppointmentDate(
                appointmentDto.getDate(),
                appointmentDto.getTime(),
                job.getDurationMinutes(),
                user.getComercio().getId()
        );

        validateScheduleConflict(
                appointmentDto.getDate(),
                appointmentDto.getTime(),
                job.getDurationMinutes(),
                user.getComercio().getId(),
                null
        );

        AppointmentsEntity appointment = AppointmentsEntity.builder()
                .user(user).job(job).comercio(user.getComercio())
                .date(appointmentDto.getDate())
                .time(appointmentDto.getTime())
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

        log.info("Agendamento criado: userId={}, jobId={}", user.getId(), job.getId());
    }

    // ✅ Reagendamento — muda data/hora de um agendamento existente
    public void reagendar(
            Integer appointmentId,
            ReagendamentoDto dto,
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
            throw new BadrequestExeption("Sem permissão para reagendar!");
        }

        if (!isAdmin(loggedUser) &&
                !appointment.getUser().getId().equals(loggedUser.getId())) {
            throw new BadrequestExeption("Sem permissão para reagendar!");
        }

        if (appointment.getStatus() == AppointmentsStatusEnum.CANCELED ||
                appointment.getStatus() == AppointmentsStatusEnum.FINISHED) {
            throw new BadrequestExeption(
                    "Não é possível reagendar um agendamento "
                            + appointment.getStatus().name().toLowerCase() + "!"
            );
        }

        Integer comercioId = appointment.getComercio().getId();
        Integer duracao = appointment.getJob().getDurationMinutes();

        validateAppointmentDate(
                dto.getNovaData(),
                dto.getNovoHorario(),
                duracao,
                comercioId
        );

        // ✅ Exclui o próprio agendamento da validação de conflito
        validateScheduleConflict(
                dto.getNovaData(),
                dto.getNovoHorario(),
                duracao,
                comercioId,
                appointmentId
        );

        LocalDate dataAnterior = appointment.getDate();
        LocalTime horaAnterior = appointment.getTime();

        appointment.setDate(dto.getNovaData());
        appointment.setTime(dto.getNovoHorario());
        appointment.setStatus(AppointmentsStatusEnum.PENDING);

        AppointmentsEntity saved = appointmentsRepository.save(appointment);
        AppointmentResponseDto responseDto = toResponseDto(saved);

        sseEmitterService.sendToComercio(
                comercioId,
                "APPOINTMENT_STATUS_UPDATED",
                Map.of(
                        "appointment", responseDto,
                        "message", "Agendamento de " +
                                appointment.getUser().getName() +
                                " reagendado para " +
                                dto.getNovaData() + " às " + dto.getNovoHorario()
                )
        );

        log.info("Agendamento reagendado: id={}, de={}/{} para={}/{}",
                appointmentId, dataAnterior, horaAnterior,
                dto.getNovaData(), dto.getNovoHorario());
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

        if (!appointment.getComercio().getId()
                .equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption("Sem permissão para cancelar!");
        }

        if (!isAdmin(loggedUser) &&
                !appointment.getUser().getId().equals(loggedUser.getId())) {
            throw new BadrequestExeption("Sem permissão para cancelar!");
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
                        "message", "Agendamento de " +
                                appointment.getUser().getName() + " cancelado"
                )
        );

        log.warn("Cancelado: id={}, por={}", appointmentId, loggedUser.getId());
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
            throw new BadrequestExeption("Sem permissão para atualizar!");
        }

        appointment.setStatus(statusEnum);
        AppointmentsEntity saved = appointmentsRepository.save(appointment);
        AppointmentResponseDto dto = toResponseDto(saved);

        String message = switch (statusEnum) {
            case CONFIRMED -> "Seu agendamento de " +
                    appointment.getJob().getName() + " foi confirmado!";
            case CANCELED -> "Seu agendamento de " +
                    appointment.getJob().getName() + " foi cancelado.";
            case FINISHED -> "Agendamento de " +
                    appointment.getJob().getName() + " finalizado.";
            default -> "Status atualizado.";
        };

        sseEmitterService.sendToComercio(
                appointment.getComercio().getId(),
                "APPOINTMENT_STATUS_UPDATED",
                Map.of("appointment", dto, "message", message)
        );

        log.info("Status: id={}, status={}", appointmentId, statusEnum);
    }


    private void validateAppointmentDate(
            LocalDate date,
            LocalTime time,
            Integer duracao,
            Integer comercioId
    ) {
        if (date.isBefore(LocalDate.now())) {
            throw new BadrequestExeption("Não é possível agendar em datas passadas!");
        }

        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new BadrequestExeption("Horário inválido!");
        }

        DayOfWeek diaSemana = date.getDayOfWeek();

        if (!horarioService.isAberto(comercioId, diaSemana)) {
            throw new BadrequestExeption(
                    "O comércio não funciona neste dia da semana!"
            );
        }

        HorarioFuncionamentoEntity horario =
                horarioService.findByComercioAndDia(comercioId, diaSemana);

        LocalTime fim = time.plusMinutes(duracao);

        if (time.isBefore(horario.getAbertura()) ||
                fim.isAfter(horario.getFechamento())) {
            throw new BadrequestExeption(
                    "Horário fora do funcionamento! " +
                            "Atendemos das " + horario.getAbertura() +
                            " às " + horario.getFechamento() + "."
            );
        }

        if (bloqueioService.isHorarioBloqueado(comercioId, date, time, fim)) {
            throw new BadrequestExeption(
                    "Este horário está bloqueado. " +
                            "Escolha outro horário ou data."
            );
        }
    }

    private void validateScheduleConflict(
            LocalDate date,
            LocalTime newStart,
            Integer duration,
            Integer comercioId,
            Integer excludeAppointmentId
    ) {
        LocalTime newEnd = newStart.plusMinutes(duration);

        List<AppointmentsEntity> appointments = excludeAppointmentId != null
                ? appointmentsRepository.findByDateAndStatusInAndComercioIdAndIdNot(
                date,
                List.of(AppointmentsStatusEnum.PENDING,
                        AppointmentsStatusEnum.CONFIRMED),
                comercioId,
                excludeAppointmentId
        )
                : appointmentsRepository.findByDateAndStatusInAndComercioId(
                date,
                List.of(AppointmentsStatusEnum.PENDING,
                        AppointmentsStatusEnum.CONFIRMED),
                comercioId
        );

        for (AppointmentsEntity a : appointments) {
            LocalTime es = a.getTime();
            LocalTime ee = es.plusMinutes(a.getJob().getDurationMinutes());
            if (newStart.isBefore(ee) && newEnd.isAfter(es)) {
                throw new BadrequestExeption(
                        "Já existe um agendamento neste horário!"
                );
            }
        }
    }

    private boolean isAdmin(UserEntity user) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
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
                .jaAvaliou(avaliacaoRepository.existsByAppointmentId(e.getId()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}