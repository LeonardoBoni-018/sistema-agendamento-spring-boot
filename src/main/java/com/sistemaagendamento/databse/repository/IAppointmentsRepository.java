package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IAppointmentsRepository extends JpaRepository<AppointmentsEntity, Integer> {
    List<AppointmentsEntity> findByDate(LocalDate date);

    List<AppointmentsEntity> findByUserId(Integer userId);

    boolean existsByDateAndTimeAndStatusIn(
            LocalDate date,
            LocalTime time,
            List<AppointmentsStatusEnum> status
    );

    List<AppointmentsEntity> findByDateAndStatusIn(
            LocalDate date,
            List<AppointmentsStatusEnum> status
    );


}
