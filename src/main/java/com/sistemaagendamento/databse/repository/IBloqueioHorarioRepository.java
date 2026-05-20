package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.BloqueioHorarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IBloqueioHorarioRepository
        extends JpaRepository<BloqueioHorarioEntity, Integer> {

    List<BloqueioHorarioEntity> findByComercioId(Integer comercioId);

    @Query("""
            SELECT b FROM BloqueioHorarioEntity b
            WHERE b.comercio.id = :comercioId
            AND b.dataInicio <= :data
            AND b.dataFim >= :data
            """)
    List<BloqueioHorarioEntity> findByComercioIdAndData(
            @Param("comercioId") Integer comercioId,
            @Param("data") LocalDate data
    );
}