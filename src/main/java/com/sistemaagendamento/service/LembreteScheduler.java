package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.databse.repository.IAppointmentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LembreteScheduler {

    private final IAppointmentsRepository appointmentsRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 * * *")
    public void enviarLembretes() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        List<AppointmentsEntity> appointments =
                appointmentsRepository.findByDateForLembrete(amanha);

        log.info("Enviando lembretes para {} agendamentos de {}",
                appointments.size(), amanha);

        appointments.forEach(a -> {
            try {
                emailService.enviarLembrete(a);
            } catch (Exception e) {
                log.error("Erro lembrete appointmentId={}: {}",
                        a.getId(), e.getMessage());
            }
        });
    }
}