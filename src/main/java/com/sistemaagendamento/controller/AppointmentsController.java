package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.AppointmentDto;
import com.sistemaagendamento.dto.AppointmentResponseDto;
import com.sistemaagendamento.dto.ReagendamentoDto;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import com.sistemaagendamento.service.AppointmentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/v1/appointment")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Gerenciamento de agendamentos")
public class AppointmentsController {

    private final AppointmentsService appointmentsService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista os agendamentos do usuário logado")
    public List<AppointmentResponseDto> myAppointments(
            Authentication authentication,
            @RequestParam(required = false) AppointmentsStatusEnum status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return appointmentsService.myAppointments(authentication, status, date);
    }

    @GetMapping("/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Busca um agendamento por ID")
    public AppointmentResponseDto getAppointmentById(
            @PathVariable Integer appointmentId,
            Authentication authentication
    ) {
        return appointmentsService.findAppointmentById(appointmentId, authentication);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista agendamentos de um usuário (admin)")
    public List<AppointmentResponseDto> appointmentsByUser(
            @PathVariable Integer userId,
            Authentication authentication,
            @RequestParam(required = false) AppointmentsStatusEnum status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return appointmentsService.findAllAppointmentsUser(
                userId, authentication, status, date
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista todos os agendamentos do comércio (admin)")
    public List<AppointmentResponseDto> getAllAppointments(
            Authentication authentication,
            @RequestParam(required = false) AppointmentsStatusEnum status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return appointmentsService.findAllAppointments(authentication, status, date);
    }

    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista horários disponíveis para uma data e serviço")
    public List<LocalTime> getAvailableTimes(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer jobId
    ) {
        return appointmentsService.getAvailableTimes(date, jobId, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo agendamento")
    public void userCreateAppointment(
            Authentication authentication,
            @Valid @RequestBody AppointmentDto appointmentDto
    ) {
        appointmentsService.userCreateAppointment(authentication, appointmentDto);
    }

    @PutMapping("/cancel/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancela um agendamento")
    public void cancelAppointment(
            @PathVariable Integer appointmentId,
            Authentication authentication
    ) {
        appointmentsService.cancelAppointment(appointmentId, authentication);
    }

    @PutMapping("/status/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualiza o status de um agendamento (admin)")
    public void updateStatusAppointment(
            @PathVariable Integer appointmentId,
            @RequestParam AppointmentsStatusEnum status,
            Authentication authentication
    ) {
        appointmentsService.updateStatusAppointment(
                appointmentId, status, authentication
        );
    }

    @PutMapping("/reagendar/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reagenda um agendamento para nova data e horário")
    public void reagendar(
            @PathVariable Integer appointmentId,
            @Valid @RequestBody ReagendamentoDto dto,
            Authentication authentication
    ) {
        appointmentsService.reagendar(appointmentId, dto, authentication);
    }
}