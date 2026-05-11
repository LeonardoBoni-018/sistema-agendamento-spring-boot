package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IAppointmentsRepository extends JpaRepository<AppointmentsEntity, Integer> {
    List<AppointmentsEntity> findByDate(LocalDate date);
}
