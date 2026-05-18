package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IAppointmentsRepository extends JpaRepository<AppointmentsEntity, Integer> {

    List<AppointmentsEntity> findByUserId(Integer userId);

    List<AppointmentsEntity> findByComercioId(Integer comercioId);

    List<AppointmentsEntity> findByDateAndStatusInAndComercioId(
            LocalDate date,
            List<AppointmentsStatusEnum> statuses,
            Integer comercioId
    );

    boolean existsByJobIdAndStatusIn(
            Integer jobId,
            List<AppointmentsStatusEnum> statuses
    );
}