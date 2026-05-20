package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.HorarioFuncionamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface IHorarioFuncionamentoRepository
        extends JpaRepository<HorarioFuncionamentoEntity, Integer> {

    List<HorarioFuncionamentoEntity> findByComercioId(Integer comercioId);

    Optional<HorarioFuncionamentoEntity> findByComercioIdAndDiaSemana(
            Integer comercioId, DayOfWeek diaSemana
    );
}