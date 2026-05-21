package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.AvaliacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IAvaliacaoRepository
        extends JpaRepository<AvaliacaoEntity, Integer> {

    List<AvaliacaoEntity> findByComercioId(Integer comercioId);

    Optional<AvaliacaoEntity> findByAppointmentId(Integer appointmentId);

    boolean existsByAppointmentId(Integer appointmentId);

    @Query("""
            SELECT AVG(a.nota)
            FROM AvaliacaoEntity a
            WHERE a.comercio.id = :comercioId
            """)
    Double findMediaNotaByComercioId(@Param("comercioId") Integer comercioId);

    @Query("""
            SELECT a FROM AvaliacaoEntity a
            WHERE a.comercio.id = :comercioId
            ORDER BY a.createdAt DESC
            """)
    List<AvaliacaoEntity> findByComercioIdOrderByCreatedAtDesc(
            @Param("comercioId") Integer comercioId
    );
}