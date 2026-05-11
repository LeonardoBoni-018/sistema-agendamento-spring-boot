package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IJobRepository extends JpaRepository<JobEntity, Integer> {
}
