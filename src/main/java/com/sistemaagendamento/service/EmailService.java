package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.databse.model.FilaEsperaEntity;
import com.sistemaagendamento.databse.model.PagamentoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.url}")
    private String appUrl;

    @Async
    public void enviarConfirmacao(AppointmentsEntity appointment) {
        try {
            String email = appointment.getUser().getEmail();
            String nome = appointment.getUser().getName();
            String servico = appointment.getJob().getName();
            String comercio = appointment.getComercio().getNome();
            String data = appointment.getDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
            String hora = appointment.getTime().toString().substring(0, 5);
            String funcionario = appointment.getFuncionario() != null
                    ? appointment.getFuncionario().getNome() : null;

            String html = buildEmailHtml(
                    "Agendamento confirmado! ✅",
                    nome,
                    comercio,
                    """
                    <p style="color:#374151;font-size:15px;margin:0 0 16px;">
                      Seu agendamento foi <strong>criado com sucesso</strong>. 
                      Veja os detalhes abaixo:
                    </p>
                    """ + buildDetalhesHtml(servico, data, hora, funcionario) +
                            """
                            <p style="color:#6B7280;font-size:13px;margin:16px 0 0;">
                              Você pode gerenciar seus agendamentos acessando o sistema.
                            </p>
                            """,
                    "Ver meus agendamentos",
                    appUrl + "/appointments"
            );

            send(email, "Agendamento confirmado — " + comercio, html);

        } catch (Exception e) {
            log.error("Erro ao enviar email de confirmação: {}", e.getMessage());
        }
    }

    @Async
    public void enviarLembrete(AppointmentsEntity appointment) {
        try {
            String email = appointment.getUser().getEmail();
            String nome = appointment.getUser().getName();
            String servico = appointment.getJob().getName();
            String comercio = appointment.getComercio().getNome();
            String data = appointment.getDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
            String hora = appointment.getTime().toString().substring(0, 5);
            String funcionario = appointment.getFuncionario() != null
                    ? appointment.getFuncionario().getNome() : null;

            String html = buildEmailHtml(
                    "Lembrete de agendamento 🔔",
                    nome,
                    comercio,
                    """
                    <p style="color:#374151;font-size:15px;margin:0 0 16px;">
                      Passando para lembrar que você tem um agendamento 
                      <strong>amanhã</strong>:
                    </p>
                    """ + buildDetalhesHtml(servico, data, hora, funcionario) +
                            """
                            <p style="color:#6B7280;font-size:13px;margin:16px 0 0;">
                              Precisa reagendar ou cancelar? Acesse o sistema com antecedência.
                            </p>
                            """,
                    "Gerenciar agendamento",
                    appUrl + "/appointments"
            );

            send(email, "Lembrete: " + servico + " amanhã — " + comercio, html);

        } catch (Exception e) {
            log.error("Erro ao enviar lembrete: {}", e.getMessage());
        }
    }

    @Async
    public void enviarAtualizacaoStatus(AppointmentsEntity appointment) {
        try {
            String email = appointment.getUser().getEmail();
            String nome = appointment.getUser().getName();
            String servico = appointment.getJob().getName();
            String comercio = appointment.getComercio().getNome();
            String data = appointment.getDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
            String hora = appointment.getTime().toString().substring(0, 5);
            String funcionario = appointment.getFuncionario() != null
                    ? appointment.getFuncionario().getNome() : null;

            String statusLabel = switch (appointment.getStatus()) {
                case CONFIRMED -> "confirmado ✅";
                case CANCELED -> "cancelado ❌";
                case FINISHED -> "finalizado 🎉";
                default -> "atualizado";
            };

            String html = buildEmailHtml(
                    "Agendamento " + statusLabel,
                    nome,
                    comercio,
                    "<p style=\"color:#374151;font-size:15px;margin:0 0 16px;\">" +
                            "Seu agendamento foi <strong>" + statusLabel + "</strong>:</p>" +
                            buildDetalhesHtml(servico, data, hora, funcionario),
                    "Ver meus agendamentos",
                    appUrl + "/appointments"
            );

            send(email, "Agendamento " + statusLabel + " — " + comercio, html);

        } catch (Exception e) {
            log.error("Erro ao enviar email de status: {}", e.getMessage());
        }
    }

    private void send(String to, String subject, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
        log.info("Email enviado para: {}", to);
    }

    private String buildDetalhesHtml(
            String servico, String data, String hora, String funcionario
    ) {
        String funcRow = funcionario != null
                ? "<tr><td style=\"padding:8px 0;color:#6B7280;font-size:13px;\">Profissional</td>" +
                  "<td style=\"padding:8px 0;font-weight:600;font-size:13px;text-align:right;\">" +
                  funcionario + "</td></tr>"
                : "";

        return """
                <table style="width:100%;border-collapse:collapse;
                  background:#F9FAFB;border-radius:8px;padding:4px;
                  margin:0 0 16px;">
                  <tr>
                    <td style="padding:8px 12px;color:#6B7280;font-size:13px;">
                      Serviço
                    </td>
                    <td style="padding:8px 12px;font-weight:600;
                      font-size:13px;text-align:right;">
                """ + servico + """
                    </td>
                  </tr>
                  <tr style="border-top:1px solid #E5E7EB;">
                    <td style="padding:8px 12px;color:#6B7280;font-size:13px;">
                      Data
                    </td>
                    <td style="padding:8px 12px;font-weight:600;
                      font-size:13px;text-align:right;">
                """ + data + """
                    </td>
                  </tr>
                  <tr style="border-top:1px solid #E5E7EB;">
                    <td style="padding:8px 12px;color:#6B7280;font-size:13px;">
                      Horário
                    </td>
                    <td style="padding:8px 12px;font-weight:600;
                      font-size:13px;text-align:right;">
                """ + hora + """
                    </td>
                  </tr>
                """ + (funcionario != null ? """
                  <tr style="border-top:1px solid #E5E7EB;">
                    <td style="padding:8px 12px;color:#6B7280;font-size:13px;">
                      Profissional
                    </td>
                    <td style="padding:8px 12px;font-weight:600;
                      font-size:13px;text-align:right;">
                """ + funcionario + """
                    </td>
                  </tr>
                """ : "") + """
                </table>
                """;
    }

    private String buildEmailHtml(
            String titulo, String nome, String comercio,
            String corpo, String btnLabel, String btnUrl
    ) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:0;background:#F3F4F6;
                  font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                    style="padding:40px 20px;">
                    <tr>
                      <td align="center">
                        <table width="100%%" cellpadding="0" cellspacing="0"
                          style="max-width:520px;background:#FFFFFF;
                          border-radius:12px;overflow:hidden;
                          border:1px solid #E5E7EB;">
                          <tr>
                            <td style="background:#1C1917;padding:24px 32px;">
                              <p style="margin:0;color:#FFFFFF;font-size:18px;
                                font-weight:600;">
                                %s
                              </p>
                              <p style="margin:4px 0 0;color:#9CA3AF;font-size:13px;">
                                %s
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px 32px;">
                              <p style="margin:0 0 20px;color:#374151;font-size:15px;">
                                Olá, <strong>%s</strong>!
                              </p>
                              %s
                              <a href="%s"
                                style="display:inline-block;background:#1C1917;
                                color:#FFFFFF;padding:11px 24px;
                                border-radius:8px;text-decoration:none;
                                font-size:13px;font-weight:600;margin-top:8px;">
                                %s
                              </a>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:16px 32px;border-top:1px solid #F3F4F6;
                              background:#FAFAFA;">
                              <p style="margin:0;color:#9CA3AF;font-size:12px;">
                                Este email foi enviado automaticamente pelo Sistema de Agendamento.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(titulo, comercio, nome, corpo, btnUrl, btnLabel);
    }

    @Async
    public void enviarNotificacaoFila(FilaEsperaEntity fila) {
        try {
            String email = fila.getUser().getEmail();
            String nome = fila.getUser().getName();
            String servico = fila.getJob().getName();
            String comercio = fila.getComercio().getNome();
            String data = fila.getDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );

            String html = buildEmailHtml(
                    "Vaga disponível! 🎉",
                    nome,
                    comercio,
                    """
                    <p style="color:#374151;font-size:15px;margin:0 0 16px;">
                      Uma vaga abriu para o serviço que você estava aguardando!
                      Acesse agora para garantir seu horário.
                    </p>
                    """ + buildDetalhesHtml(servico, data, "—", null) +
                            """
                            <p style="color:#DC2626;font-size:13px;margin:12px 0 0;font-weight:600;">
                              ⚡ Corra! A vaga pode ser ocupada por outro cliente a qualquer momento.
                            </p>
                            """,
                    "Agendar agora",
                    appUrl + "/appointments/new"
            );

            send(email, "Vaga disponível — " + servico + " em " + comercio, html);

        } catch (Exception e) {
            log.error("Erro ao enviar email de fila: {}", e.getMessage());
        }
    }

    @Async
    public void enviarConfirmacaoPagamento(
            AppointmentsEntity appointment,
            PagamentoEntity pagamento
    ) {
        try {
            String email = appointment.getUser().getEmail();
            String nome = appointment.getUser().getName();
            String servico = appointment.getJob().getName();
            String comercio = appointment.getComercio().getNome();
            String data = appointment.getDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
            String hora = appointment.getTime().toString().substring(0, 5);
            String valor = "R$ " + pagamento.getValor().toPlainString();

            String html = buildEmailHtml(
                    "Pagamento confirmado! ✅",
                    nome,
                    comercio,
                    """
                    <p style="color:#374151;font-size:15px;margin:0 0 16px;">
                      Seu pagamento foi <strong>aprovado</strong> e o agendamento confirmado!
                    </p>
                    """ + buildDetalhesHtml(servico, data, hora, null) +
                            "<p style=\"color:#059669;font-size:14px;font-weight:600;margin:12px 0;\">✅ Pago: " + valor + "</p>",
                    "Ver agendamento",
                    appUrl + "/appointments"
            );

            send(email, "Pagamento confirmado — " + comercio, html);

        } catch (Exception e) {
            log.error("Erro ao enviar email de pagamento: {}", e.getMessage());
        }
    }
}