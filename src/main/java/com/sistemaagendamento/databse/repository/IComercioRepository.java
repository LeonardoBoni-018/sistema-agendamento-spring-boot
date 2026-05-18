package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.ComercioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IComercioRepository extends JpaRepository<ComercioEntity, Integer> {
}