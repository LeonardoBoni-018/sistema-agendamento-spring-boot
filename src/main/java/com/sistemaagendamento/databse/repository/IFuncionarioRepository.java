package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.FuncionarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IFuncionarioRepository
        extends JpaRepository<FuncionarioEntity, Integer> {

    List<FuncionarioEntity> findByComercioIdAndAtivoTrue(Integer comercioId);

    List<FuncionarioEntity> findByComercioId(Integer comercioId);
}