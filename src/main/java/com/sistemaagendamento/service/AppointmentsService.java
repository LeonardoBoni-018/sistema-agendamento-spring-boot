package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.databse.model.JobEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IAppointmentsRepository;
import com.sistemaagendamento.databse.repository.IJobRepository;
import com.sistemaagendamento.databse.repository.IUserRepository;
import com.sistemaagendamento.dto.AppointmentDto;
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

    private final IAppointmentsRepository appointmentsRepository;
    private final IUserRepository userRepository;
    private final IJobRepository jobRepository;

    public List<AppointmentsEntity> myAppointments(
            Authentication authentication
    ){

        UserEntity loggedUser =
                (UserEntity) authentication.getPrincipal();

        return appointmentsRepository.findByUserId(
                loggedUser.getId()
        );
    }

    public List<AppointmentsEntity> findAllAppointmentsUser(
            Integer userId
    ){

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Usuário não encontrado!")
                );

        return appointmentsRepository.findByUserId(userId);
    }

    public void userCreateAppointment(
            Authentication authentication,
            AppointmentDto appointmentDto
    ){

        UserEntity loggedUser =
                (UserEntity) authentication.getPrincipal();

        UserEntity user = userRepository.findById(loggedUser.getId())
                .orElseThrow(() ->
                        new NotFoundException("Usuário não encontrado!")
                );

        JobEntity job = jobRepository.findById(
                        appointmentDto.getJobId()
                )
                .orElseThrow(() ->
                        new NotFoundException("Serviço não encontrado!")
                );

        validateAppointmentDate(appointmentDto);

        validateScheduleConflict(
                appointmentDto.getDate(),
                appointmentDto.getTime(),
                job.getDurationMinutes()
        );

        AppointmentsEntity appointment =
                AppointmentsEntity.builder()
                        .user(user)
                        .job(job)
                        .date(appointmentDto.getDate())
                        .time(appointmentDto.getTime())
                        .status(AppointmentsStatusEnum.PENDING)
                        .build();

        appointmentsRepository.save(appointment);
    }

    private void validateAppointmentDate(
            AppointmentDto appointmentDto
    ){

        if(appointmentDto.getDate().isBefore(LocalDate.now())){
            throw new BadrequestExeption(
                    "Não é possível agendar em datas passadas!"
            );
        }

        if(
                appointmentDto.getDate().equals(LocalDate.now()) &&
                        appointmentDto.getTime().isBefore(LocalTime.now())
        ){
            throw new BadrequestExeption("Horário inválido");
        }
    }

    private void validateScheduleConflict(
            LocalDate date,
            LocalTime newStartTime,
            Integer durationMinutes
    ){

        LocalTime newEndTime =
                newStartTime.plusMinutes(durationMinutes);

        List<AppointmentsEntity> appointments =
                appointmentsRepository.findByDateAndStatusIn(
                        date,
                        List.of(
                                AppointmentsStatusEnum.PENDING,
                                AppointmentsStatusEnum.CONFIRMED
                        )
                );

        for(AppointmentsEntity appointment : appointments){

            LocalTime existingStart =
                    appointment.getTime();

            LocalTime existingEnd =
                    existingStart.plusMinutes(
                            appointment.getJob()
                                    .getDurationMinutes()
                    );

            boolean hasConflict =
                    newStartTime.isBefore(existingEnd) &&
                            newEndTime.isAfter(existingStart);

            if(hasConflict){
                throw new BadrequestExeption(
                        "Já existe um agendamento neste horário"
                );
            }
        }
    }

    public void cancelAppointment(Integer appointmentId){

        AppointmentsEntity appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Agendamento não encontrado!"
                                )
                        );

        if(appointment.getStatus()
                == AppointmentsStatusEnum.CANCELED){
            throw new BadrequestExeption(
                    "Agendamento já cancelado!"
            );
        }

        if(appointment.getStatus()
                == AppointmentsStatusEnum.FINISHED){
            throw new BadrequestExeption(
                    "Agendamento já finalizado!"
            );
        }

        appointment.setStatus(
                AppointmentsStatusEnum.CANCELED
        );

        appointmentsRepository.save(appointment);
    }

    public void updateStatusAppointment(
            Integer appointmentId,
            AppointmentsStatusEnum statusEnum
    ){

        AppointmentsEntity appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Agendamento não encontrado!"
                                )
                        );

        appointment.setStatus(statusEnum);

        appointmentsRepository.save(appointment);
    }
}