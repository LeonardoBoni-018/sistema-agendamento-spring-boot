package com.sistemaagendamento.controller;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.dto.AppointmentDto;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import com.sistemaagendamento.service.AppointmentsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/appointment")
@RequiredArgsConstructor
public class AppointmentsController {

    private final AppointmentsService appointmentsService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public List<AppointmentsEntity> myAppointments(
            Authentication authentication
    ){
        return appointmentsService.myAppointments(authentication);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public List<AppointmentsEntity> appointmentsByUser(
            @PathVariable Integer userId
    ){
        return appointmentsService.findAllAppointmentsUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void userCreateAppointment(
            Authentication authentication,
            @Valid @RequestBody AppointmentDto appointmentDto
    ){
        appointmentsService.userCreateAppointment(
                authentication,
                appointmentDto
        );
    }

    @PutMapping("/cancel/{appointmentId}")
    @PreAuthorize(
            "@appointmentSecurity.canAccessAppointment(#appointmentId, authentication)"
    )
    @ResponseStatus(HttpStatus.OK)
    public void cancelAppointment(
            @PathVariable Integer appointmentId
    ){
        appointmentsService.cancelAppointment(appointmentId);
    }

    @PutMapping("/status/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void updateStatusAppointment(
            @PathVariable Integer appointmentId,
            @RequestParam AppointmentsStatusEnum status
    ){
        appointmentsService.updateStatusAppointment(
                appointmentId,
                status
        );
    }
}