package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IJobRepository extends JpaRepository<JobEntity, Integer> {

    List<JobEntity> findByComercioId(Integer comercioId);
}