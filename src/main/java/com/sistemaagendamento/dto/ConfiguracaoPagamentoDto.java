package com.sistemaagendamento.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfiguracaoPagamentoDto {

    @NotNull
    private Boolean exigirPagamento;

    private Integer percentualAntecipacao;
}