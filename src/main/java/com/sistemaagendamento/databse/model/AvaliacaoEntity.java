package com.sistemaagendamento.databse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "avaliacoes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private AppointmentsEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comercio_id", nullable = false)
    private ComercioEntity comercio;

    @Column(nullable = false)
    private Integer nota;

    @Column(length = 500)
    private String comentario;
}