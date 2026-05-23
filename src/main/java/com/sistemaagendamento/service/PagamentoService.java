package com.sistemaagendamento.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.sistemaagendamento.databse.model.*;
import com.sistemaagendamento.databse.model.PagamentoEntity.PagamentoStatusEnum;
import com.sistemaagendamento.databse.repository.*;
import com.sistemaagendamento.dto.ConfiguracaoPagamentoDto;
import com.sistemaagendamento.dto.PagamentoResponseDto;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final IPagamentoRepository pagamentoRepository;
    private final IAppointmentsRepository appointmentsRepository;
    private final IConfiguracaoPagamentoRepository configRepository;
    private final SseEmitterService sseEmitterService;
    private final EmailService emailService;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${app.url}")
    private String appUrl;

    public PagamentoResponseDto gerarCheckout(
            Integer appointmentId,
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        AppointmentsEntity appointment =
                appointmentsRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new NotFoundException("Agendamento não encontrado!")
                        );

        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new BadrequestExeption("Sem permissão!");
        }

        if (pagamentoRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BadrequestExeption(
                    "Já existe um pagamento para este agendamento!"
            );
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            BigDecimal valor = appointment.getJob().getPrice();
            String titulo = appointment.getJob().getName()
                    + " — " + appointment.getComercio().getNome();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(titulo)
                    .quantity(1)
                    .unitPrice(valor)
                    .currencyId("BRL")
                    .build();

            PreferenceBackUrlsRequest backUrls =
                    PreferenceBackUrlsRequest.builder()
                            .success(appUrl + "/pagamento/sucesso?appointmentId="
                                    + appointmentId)
                            .failure(appUrl + "/pagamento/erro?appointmentId="
                                    + appointmentId)
                            .pending(appUrl + "/pagamento/pendente?appointmentId="
                                    + appointmentId)
                            .build();

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .payer(payer)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(String.valueOf(appointmentId))
                    .notificationUrl(appUrl.replace("5173", "8080")
                            + "/v1/pagamento/webhook")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);

            PagamentoEntity pagamento = PagamentoEntity.builder()
                    .appointment(appointment)
                    .comercio(appointment.getComercio())
                    .valor(valor)
                    .status(PagamentoStatusEnum.PENDENTE)
                    .mpPreferenceId(preference.getId())
                    .checkoutUrl(preference.getInitPoint())
                    .build();

            PagamentoEntity saved = pagamentoRepository.save(pagamento);

            log.info("Checkout gerado: appointmentId={}, preferenceId={}",
                    appointmentId, preference.getId());

            return toDto(saved);

        } catch (Exception e) {
            log.error("Erro ao gerar checkout MP: {}", e.getMessage());
            throw new BadrequestExeption("Erro ao gerar link de pagamento!");
        }
    }

    public PagamentoResponseDto findByAppointment(Integer appointmentId) {
        return pagamentoRepository.findByAppointmentId(appointmentId)
                .map(this::toDto)
                .orElse(null);
    }

    public void processarWebhook(Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type");
            if (!"payment".equals(type)) return;

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String mpPaymentId = String.valueOf(data.get("id"));

            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client =
                    new PaymentClient();
            Payment payment =
                    client.get(Long.parseLong(mpPaymentId));

            String preferenceId = payment.getCurrencyId();
            String status = payment.getStatus();

            PagamentoEntity pagamento = pagamentoRepository
                    .findByMpPreferenceId(preferenceId)
                    .orElse(null);

            if (pagamento == null) {
                log.warn("Pagamento não encontrado para preferenceId={}",
                        preferenceId);
                return;
            }

            pagamento.setMpPaymentId(mpPaymentId);

            switch (status) {
                case "approved" -> {
                    pagamento.setStatus(PagamentoStatusEnum.APROVADO);
                    pagamento.setPaidAt(LocalDateTime.now());

                    AppointmentsEntity appointment = pagamento.getAppointment();
                    appointment.setStatus(AppointmentsStatusEnum.CONFIRMED);
                    appointmentsRepository.save(appointment);

                    sseEmitterService.sendToComercio(
                            pagamento.getComercio().getId(),
                            "PAGAMENTO_APROVADO",
                            Map.of(
                                    "appointmentId", appointment.getId(),
                                    "message", "Pagamento aprovado — "
                                            + appointment.getUser().getName()
                            )
                    );

                    emailService.enviarConfirmacaoPagamento(appointment, pagamento);
                    log.info("Pagamento aprovado: appointmentId={}",
                            appointment.getId());
                }
                case "rejected", "cancelled" -> {
                    pagamento.setStatus(PagamentoStatusEnum.REJEITADO);
                    log.info("Pagamento rejeitado: preferenceId={}", preferenceId);
                }
                case "refunded" -> {
                    pagamento.setStatus(PagamentoStatusEnum.REEMBOLSADO);
                    log.info("Pagamento reembolsado: preferenceId={}", preferenceId);
                }
                default -> log.info("Status MP ignorado: {}", status);
            }

            pagamentoRepository.save(pagamento);

        } catch (Exception e) {
            log.error("Erro ao processar webhook MP: {}", e.getMessage());
        }
    }

    public ConfiguracaoPagamentoEntity getConfig(Integer comercioId) {
        return configRepository.findByComercioId(comercioId)
                .orElse(null);
    }

    public void salvarConfig(
            ConfiguracaoPagamentoDto dto,
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        ComercioEntity comercio = user.getComercio();

        ConfiguracaoPagamentoEntity config =
                configRepository.findByComercioId(comercio.getId())
                        .orElse(ConfiguracaoPagamentoEntity.builder()
                                .comercio(comercio)
                                .build());

        config.setExigirPagamento(dto.getExigirPagamento());
        config.setPercentualAntecipacao(dto.getPercentualAntecipacao());

        configRepository.save(config);
        log.info("Config pagamento salva: comercioId={}", comercio.getId());
    }

    private PagamentoResponseDto toDto(PagamentoEntity e) {
        return PagamentoResponseDto.builder()
                .id(e.getId())
                .appointmentId(e.getAppointment().getId())
                .valor(e.getValor())
                .status(e.getStatus())
                .checkoutUrl(e.getCheckoutUrl())
                .paidAt(e.getPaidAt())
                .build();
    }
}