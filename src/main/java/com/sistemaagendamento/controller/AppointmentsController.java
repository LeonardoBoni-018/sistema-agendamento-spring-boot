package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.AppointmentDto;
import com.sistemaagendamento.dto.AppointmentResponseDto;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import com.sistemaagendamento.service.AppointmentsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/appointment")
@RequiredArgsConstructor
public class AppointmentsController {

    private final AppointmentsService appointmentsService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
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
    public AppointmentResponseDto getAppointmentById(
            @PathVariable Integer appointmentId,
            Authentication authentication
    ) {
        return appointmentsService.findAppointmentById(appointmentId, authentication);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public List<AppointmentResponseDto> appointmentsByUser(
            @PathVariable Integer userId,
            @RequestParam(required = false) AppointmentsStatusEnum status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return appointmentsService.findAllAppointmentsUser(userId, status, date);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void userCreateAppointment(
            Authentication authentication,
            @Valid @RequestBody AppointmentDto appointmentDto
    ) {
        appointmentsService.userCreateAppointment(authentication, appointmentDto);
    }

    // Removido @PreAuthorize com appointmentSecurity — a verificação foi
    // movida para dentro do service, tornando-a mais segura e testável
    @PutMapping("/cancel/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    public void cancelAppointment(
            @PathVariable Integer appointmentId,
            Authentication authentication
    ) {
        appointmentsService.cancelAppointment(appointmentId, authentication);
    }

    @PutMapping("/status/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void updateStatusAppointment(
            @PathVariable Integer appointmentId,
            @RequestParam AppointmentsStatusEnum status
    ) {
        appointmentsService.updateStatusAppointment(appointmentId, status);
    }
}