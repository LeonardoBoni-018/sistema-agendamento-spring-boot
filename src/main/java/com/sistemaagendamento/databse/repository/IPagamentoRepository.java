package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.PagamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPagamentoRepository
        extends JpaRepository<PagamentoEntity, Integer> {

    Optional<PagamentoEntity> findByAppointmentId(Integer appointmentId);

    Optional<PagamentoEntity> findByMpPaymentId(String mpPaymentId);

    Optional<PagamentoEntity> findByMpPreferenceId(String mpPreferenceId);
}