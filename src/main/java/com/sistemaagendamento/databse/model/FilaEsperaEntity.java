package com.sistemaagendamento.databse.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "fila_espera")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilaEsperaEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comercio_id", nullable = false)
    private ComercioEntity comercio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private FuncionarioEntity funcionario;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime horarioPreferido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FilaStatusEnum status;

    public enum FilaStatusEnum {
        AGUARDANDO, NOTIFICADO, AGENDADO, EXPIRADO
    }
}