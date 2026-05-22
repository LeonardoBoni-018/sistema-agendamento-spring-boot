package com.sistemaagendamento.databse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "funcionarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuncionarioEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comercio_id", nullable = false)
    private ComercioEntity comercio;

    @Column(nullable = false)
    private String nome;

    private String especialidade;

    private String telefone;

    private String email;

    @Column(nullable = false)
    private Boolean ativo;
}