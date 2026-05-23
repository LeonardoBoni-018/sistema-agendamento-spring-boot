package com.sistemaagendamento.databse.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", unique = true)
    private AppointmentsEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comercio_id", nullable = false)
    private ComercioEntity comercio;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PagamentoStatusEnum status;

    private String mpPaymentId;

    private String mpPreferenceId;

    @Column(length = 1000)
    private String checkoutUrl;

    private LocalDateTime paidAt;

    public enum PagamentoStatusEnum {
        PENDENTE, APROVADO, REJEITADO, CANCELADO, REEMBOLSADO
    }
}