package com.sistemaagendamento.databse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracao_pagamento")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoPagamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comercio_id", unique = true)
    private ComercioEntity comercio;

    @Column(nullable = false)
    private Boolean exigirPagamento;

    private Integer percentualAntecipacao;
}