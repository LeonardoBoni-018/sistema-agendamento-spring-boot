package com.sistemaagendamento.security;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.databse.model.RoleEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IAppointmentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("appointmentSecurity")
@RequiredArgsConstructor
public class AppointmentSecurity {

    private final IAppointmentsRepository appointmentsRepository;

    public boolean canAccessAppointment(
            Integer appointmentId,
            Authentication authentication
    ){

        UserEntity loggedUser =
                (UserEntity) authentication.getPrincipal();

        boolean isAdmin = loggedUser.getAuthorities()
                .stream()
                .map(role -> ((RoleEntity) role).getAuthority())
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if(isAdmin){
            return true;
        }

        AppointmentsEntity appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElse(null);

        if(appointment == null){
            return false;
        }

        return appointment.getUser()
                .getId()
                .equals(loggedUser.getId());
    }
}