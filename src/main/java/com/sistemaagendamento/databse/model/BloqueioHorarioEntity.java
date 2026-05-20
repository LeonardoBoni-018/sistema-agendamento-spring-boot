package com.sistemaagendamento.databse.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bloqueios_horario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloqueioHorarioEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comercio_id", nullable = false)
    private ComercioEntity comercio;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    private LocalTime horaInicio;

    private LocalTime horaFim;

    private String motivo;

    // true = dia inteiro bloqueado, false = apenas o intervalo de hora
    @Column(nullable = false)
    private Boolean diaInteiro;
}