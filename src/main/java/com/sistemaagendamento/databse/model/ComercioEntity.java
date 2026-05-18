package com.sistemaagendamento.databse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comercios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercioEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    private String telefone;

    private String endereco;
}