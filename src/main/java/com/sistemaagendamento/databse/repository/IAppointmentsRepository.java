package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IAppointmentsRepository
        extends JpaRepository<AppointmentsEntity, Integer> {

    List<AppointmentsEntity> findByUserId(Integer userId);

    List<AppointmentsEntity> findByComercioId(Integer comercioId);

    List<AppointmentsEntity> findByDateAndStatusInAndComercioId(
            LocalDate date,
            List<AppointmentsStatusEnum> statuses,
            Integer comercioId
    );

    List<AppointmentsEntity> findByDateAndStatusInAndComercioIdAndIdNot(
            LocalDate date,
            List<AppointmentsStatusEnum> statuses,
            Integer comercioId,
            Integer excludeId
    );

    boolean existsByJobIdAndStatusIn(
            Integer jobId,
            List<AppointmentsStatusEnum> statuses
    );

    List<AppointmentsEntity> findByDateAndStatusInAndFuncionarioId(
            LocalDate date,
            List<AppointmentsStatusEnum> statuses,
            Integer funcionarioId
    );

    @Query("""
            SELECT a FROM AppointmentsEntity a
            WHERE a.date = :data
            AND a.status IN ('PENDING', 'CONFIRMED')
            """)
    List<AppointmentsEntity> findByDateForLembrete(
            @Param("data") LocalDate data
    );
}