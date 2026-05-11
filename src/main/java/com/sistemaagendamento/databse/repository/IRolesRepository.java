package com.sistemaagendamento.databse.repository;

import com.sistemaagendamento.databse.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IRolesRepository extends JpaRepository<RoleEntity, Integer> {
    Optional<RoleEntity> findByName(String role);
}
