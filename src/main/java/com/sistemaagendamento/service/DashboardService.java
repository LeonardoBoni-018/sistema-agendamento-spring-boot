package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.AppointmentsEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IAppointmentsRepository;
import com.sistemaagendamento.databse.repository.IAvaliacaoRepository;
import com.sistemaagendamento.dto.ClienteResponseDto;
import com.sistemaagendamento.dto.DashboardResponseDto;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IAppointmentsRepository appointmentsRepository;
    private final IAvaliacaoRepository avaliacaoRepository;

    public DashboardResponseDto getDashboard(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        Integer comercioId = user.getComercio().getId();

        List<AppointmentsEntity> all =
                appointmentsRepository.findByComercioId(comercioId);

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);

        List<AppointmentsEntity> hoje_ = all.stream()
                .filter(a -> a.getDate().equals(hoje))
                .toList();

        List<AppointmentsEntity> mes = all.stream()
                .filter(a -> !a.getDate().isBefore(inicioMes))
                .toList();

        List<AppointmentsEntity> finalizados = all.stream()
                .filter(a -> a.getStatus() == AppointmentsStatusEnum.FINISHED)
                .toList();

        // Receita — soma dos serviços finalizados
        BigDecimal receitaTotal = finalizados.stream()
                .map(a -> a.getJob().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receitaMes = finalizados.stream()
                .filter(a -> !a.getDate().isBefore(inicioMes))
                .map(a -> a.getJob().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receitaHoje = finalizados.stream()
                .filter(a -> a.getDate().equals(hoje))
                .map(a -> a.getJob().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Serviço mais agendado
        String servicoMais = all.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getJob().getName(), Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        // Horário de pico
        String horarioPico = all.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getTime().getHour() + "h", Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        // Agendamentos por status
        Map<String, Integer> porStatus = new LinkedHashMap<>();
        porStatus.put("PENDING", (int) all.stream()
                .filter(a -> a.getStatus() == AppointmentsStatusEnum.PENDING)
                .count());
        porStatus.put("CONFIRMED", (int) all.stream()
                .filter(a -> a.getStatus() == AppointmentsStatusEnum.CONFIRMED)
                .count());
        porStatus.put("CANCELED", (int) all.stream()
                .filter(a -> a.getStatus() == AppointmentsStatusEnum.CANCELED)
                .count());
        porStatus.put("FINISHED", (int) finalizados.size());

        // Agendamentos por serviço
        Map<String, Integer> porServico = all.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getJob().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // Receita por mês (últimos 6 meses)
        List<DashboardResponseDto.ReceitaMensalDto> receitaPorMes =
                buildReceitaPorMes(finalizados);

        // Agendamentos por dia (últimos 7 dias)
        List<DashboardResponseDto.AgendamentosPorDiaDto> porDia =
                buildAgendamentosPorDia(all);

        // Avaliação média
        Double media = avaliacaoRepository
                .findMediaNotaByComercioId(comercioId);
        long totalAval = avaliacaoRepository
                .findByComercioId(comercioId).size();

        return DashboardResponseDto.builder()
                .totalAgendamentos(all.size())
                .agendamentosHoje(hoje_.size())
                .agendamentosMes(mes.size())
                .agendamentosConfirmados(porStatus.get("CONFIRMED"))
                .agendamentosPendentes(porStatus.get("PENDING"))
                .agendamentosCancelados(porStatus.get("CANCELED"))
                .agendamentosFinalizados(finalizados.size())
                .receitaTotal(receitaTotal)
                .receitaMes(receitaMes)
                .receitaHoje(receitaHoje)
                .mediaAvaliacao(media != null
                        ? BigDecimal.valueOf(media)
                          .setScale(1, RoundingMode.HALF_UP).doubleValue()
                        : null)
                .totalAvaliacoes((int) totalAval)
                .servicoMaisAgendado(servicoMais)
                .horarioPico(horarioPico)
                .receitaPorMes(receitaPorMes)
                .agendamentosPorDia(porDia)
                .agendamentosPorStatus(porStatus)
                .agendamentosPorServico(porServico)
                .build();
    }

    public List<ClienteResponseDto> getClientes(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        Integer comercioId = user.getComercio().getId();

        List<AppointmentsEntity> all =
                appointmentsRepository.findByComercioId(comercioId);

        Map<Integer, List<AppointmentsEntity>> porUsuario = all.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        return porUsuario.entrySet().stream()
                .map(entry -> buildClienteDto(entry.getValue(), comercioId))
                .sorted(Comparator.comparing(
                        ClienteResponseDto::getTotalAgendamentos
                ).reversed())
                .toList();
    }

    private ClienteResponseDto buildClienteDto(
            List<AppointmentsEntity> appts,
            Integer comercioId
    ) {
        UserEntity u = appts.get(0).getUser();

        List<AppointmentsEntity> finalizados = appts.stream()
                .filter(a -> a.getStatus() == AppointmentsStatusEnum.FINISHED)
                .toList();

        List<AppointmentsEntity> cancelados = appts.stream()
                .filter(a -> a.getStatus() == AppointmentsStatusEnum.CANCELED)
                .toList();

        BigDecimal ticketTotal = finalizados.stream()
                .map(a -> a.getJob().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketMedio = finalizados.isEmpty()
                ? BigDecimal.ZERO
                : ticketTotal.divide(
                BigDecimal.valueOf(finalizados.size()),
                2, RoundingMode.HALF_UP
        );

        LocalDate primeiro = appts.stream()
                .map(AppointmentsEntity::getDate)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDate ultimo = appts.stream()
                .map(AppointmentsEntity::getDate)
                .max(Comparator.naturalOrder())
                .orElse(null);

        String servicoFavorito = appts.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getJob().getName(), Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        Double mediaAvaliacao = avaliacaoRepository
                .findByComercioId(comercioId)
                .stream()
                .filter(av -> av.getUser().getId().equals(u.getId()))
                .mapToInt(av -> av.getNota())
                .average()
                .orElse(0);

        return ClienteResponseDto.builder()
                .userId(u.getId())
                .nome(u.getName())
                .email(u.getEmail())
                .telefone(u.getPhone())
                .totalAgendamentos(appts.size())
                .agendamentosFinalizados(finalizados.size())
                .agendamentosCancelados(cancelados.size())
                .ticketTotal(ticketTotal)
                .ticketMedio(ticketMedio)
                .primeiroAgendamento(primeiro)
                .ultimoAgendamento(ultimo)
                .servicoFavorito(servicoFavorito)
                .mediaAvaliacao(mediaAvaliacao)
                .build();
    }

    private List<DashboardResponseDto.ReceitaMensalDto> buildReceitaPorMes(
            List<AppointmentsEntity> finalizados
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM/yy",
                new Locale("pt", "BR"));
        LocalDate hoje = LocalDate.now();
        List<DashboardResponseDto.ReceitaMensalDto> result = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate mes = hoje.minusMonths(i);
            LocalDate inicio = mes.withDayOfMonth(1);
            LocalDate fim = mes.withDayOfMonth(mes.lengthOfMonth());

            List<AppointmentsEntity> doMes = finalizados.stream()
                    .filter(a -> !a.getDate().isBefore(inicio)
                            && !a.getDate().isAfter(fim))
                    .toList();

            BigDecimal receita = doMes.stream()
                    .map(a -> a.getJob().getPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(DashboardResponseDto.ReceitaMensalDto.builder()
                    .mes(mes.format(fmt))
                    .receita(receita)
                    .quantidade(doMes.size())
                    .build());
        }

        return result;
    }

    private List<DashboardResponseDto.AgendamentosPorDiaDto> buildAgendamentosPorDia(
            List<AppointmentsEntity> all
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate hoje = LocalDate.now();
        List<DashboardResponseDto.AgendamentosPorDiaDto> result = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            long count = all.stream()
                    .filter(a -> a.getDate().equals(dia))
                    .count();

            result.add(DashboardResponseDto.AgendamentosPorDiaDto.builder()
                    .dia(i == 0 ? "Hoje" : dia.format(fmt))
                    .quantidade((int) count)
                    .build());
        }

        return result;
    }
}