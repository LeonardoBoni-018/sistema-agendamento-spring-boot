package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.ConfiguracaoPagamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IConfiguracaoPagamentoRepository
        extends JpaRepository<ConfiguracaoPagamentoEntity, Integer> {

    Optional<ConfiguracaoPagamentoEntity> findByComercioId(Integer comercioId);
}