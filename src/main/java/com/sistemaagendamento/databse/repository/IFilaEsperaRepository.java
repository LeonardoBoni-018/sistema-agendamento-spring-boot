package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.FilaEsperaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IFilaEsperaRepository
        extends JpaRepository<FilaEsperaEntity, Integer> {

    List<FilaEsperaEntity> findByComercioIdAndStatusOrderByCreatedAtAsc(
            Integer comercioId,
            FilaEsperaEntity.FilaStatusEnum status
    );

    @Query("""
            SELECT f FROM FilaEsperaEntity f
            WHERE f.comercio.id = :comercioId
            AND f.date = :data
            AND f.status = 'AGUARDANDO'
            ORDER BY f.createdAt ASC
            """)
    List<FilaEsperaEntity> findAguardandoByComercioAndData(
            @Param("comercioId") Integer comercioId,
            @Param("data") LocalDate data
    );

    boolean existsByUserIdAndJobIdAndDateAndStatus(
            Integer userId,
            Integer jobId,
            LocalDate date,
            FilaEsperaEntity.FilaStatusEnum status
    );

    List<FilaEsperaEntity> findByUserIdAndStatus(
            Integer userId,
            FilaEsperaEntity.FilaStatusEnum status
    );
}