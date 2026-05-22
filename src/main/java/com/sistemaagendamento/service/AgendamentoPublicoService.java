package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.*;
import com.sistemaagendamento.databse.repository.*;
import com.sistemaagendamento.dto.*;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import com.sistemaagendamento.enums.RoleTypeEnum;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgendamentoPublicoService {

    private final IComercioRepository comercioRepository;
    private final IJobRepository jobRepository;
    private final IUserRepository userRepository;
    private final IRolesRepository rolesRepository;
    private final IAppointmentsRepository appointmentsRepository;
    private final IFuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final HorarioFuncionamentoService horarioService;
    private final BloqueioHorarioService bloqueioService;
    private final EmailService emailService;
    private final SseEmitterService sseEmitterService;

    public ComercioPublicoResponseDto getComercioPublico(Integer comercioId) {
        ComercioEntity comercio = comercioRepository.findById(comercioId)
                .orElseThrow(() ->
                        new NotFoundException("Comércio não encontrado!")
                );

        List<JobResponseDto> servicos = jobRepository
                .findByComercioId(comercioId)
                .stream()
                .map(j -> JobResponseDto.builder()
                        .id(j.getId())
                        .name(j.getName())
                        .description(j.getDescription())
                        .price(j.getPrice())
                        .durationMinutes(j.getDurationMinutes())
                        .comercioId(comercioId)
                        .comercioNome(comercio.getNome())
                        .build())
                .toList();

        List<FuncionarioResponseDto> funcionarios = funcionarioRepository
                .findByComercioIdAndAtivoTrue(comercioId)
                .stream()
                .map(f -> FuncionarioResponseDto.builder()
                        .id(f.getId())
                        .nome(f.getNome())
                        .especialidade(f.getEspecialidade())
                        .ativo(f.getAtivo())
                        .build())
                .toList();

        List<HorarioFuncionamentoResponseDto> horarios =
                horarioService.findByComercioId(comercioId);

        return ComercioPublicoResponseDto.builder()
                .id(comercio.getId())
                .nome(comercio.getNome())
                .descricao(comercio.getDescricao())
                .telefone(comercio.getTelefone())
                .endereco(comercio.getEndereco())
                .servicos(servicos)
                .funcionarios(funcionarios)
                .horarios(horarios)
                .build();
    }

    public List<LocalTime> getHorariosDisponiveis(
            Integer comercioId,
            LocalDate date,
            Integer jobId,
            Integer funcionarioId
    ) {
        comercioRepository.findById(comercioId)
                .orElseThrow(() ->
                        new NotFoundException("Comércio não encontrado!")
                );

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new NotFoundException("Serviço não encontrado!")
                );

        DayOfWeek dia = date.getDayOfWeek();

        if (!horarioService.isAberto(comercioId, dia)) {
            return List.of();
        }

        HorarioFuncionamentoEntity horario =
                horarioService.findByComercioAndDia(comercioId, dia);

        List<AppointmentsEntity> booked = funcionarioId != null
                ? appointmentsRepository.findByDateAndStatusInAndFuncionarioId(
                date,
                List.of(AppointmentsStatusEnum.PENDING,
                        AppointmentsStatusEnum.CONFIRMED),
                funcionarioId
        )
                : appointmentsRepository.findByDateAndStatusInAndComercioId(
                date,
                List.of(AppointmentsStatusEnum.PENDING,
                        AppointmentsStatusEnum.CONFIRMED),
                comercioId
        );

        List<LocalTime> available = new ArrayList<>();
        LocalTime slot = horario.getAbertura();

        while (!slot.plusMinutes(job.getDurationMinutes())
                .isAfter(horario.getFechamento())) {
            LocalTime slotEnd = slot.plusMinutes(job.getDurationMinutes());

            boolean bloqueado = bloqueioService.isHorarioBloqueado(
                    comercioId, date, slot, slotEnd
            );

            if (!bloqueado) {
                LocalTime finalSlot = slot;
                boolean conflict = booked.stream().anyMatch(b -> {
                    LocalTime bs = b.getTime();
                    LocalTime be = bs.plusMinutes(
                            b.getJob().getDurationMinutes()
                    );
                    return finalSlot.isBefore(be) && slotEnd.isAfter(bs);
                });

                if (!conflict) available.add(slot);
            }

            slot = slot.plusMinutes(job.getDurationMinutes());
        }

        return available;
    }

    public void agendar(Integer comercioId, AgendamentoPublicoDto dto) {
        ComercioEntity comercio = comercioRepository.findById(comercioId)
                .orElseThrow(() ->
                        new NotFoundException("Comércio não encontrado!")
                );

        JobEntity job = jobRepository.findById(dto.getJobId())
                .orElseThrow(() ->
                        new NotFoundException("Serviço não encontrado!")
                );

        if (!job.getComercio().getId().equals(comercioId)) {
            throw new BadrequestExeption(
                    "Serviço não pertence a este comércio!"
            );
        }

        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseGet(() -> {
                    RoleEntity role = rolesRepository
                            .findByName(RoleTypeEnum.ROLE_USER.name())
                            .orElseGet(() -> rolesRepository.save(
                                    RoleEntity.builder()
                                            .name(RoleTypeEnum.ROLE_USER.name())
                                            .build()
                            ));

                    String senha = dto.getSenha() != null && !dto.getSenha().isBlank()
                            ? dto.getSenha()
                            : gerarSenhaAleatoria();

                    return userRepository.save(UserEntity.builder()
                            .name(dto.getNome())
                            .email(dto.getEmail())
                            .phone(dto.getTelefone())
                            .password(passwordEncoder.encode(senha))
                            .roles(Set.of(role))
                            .comercio(comercio)
                            .build());
                });

        if (user.getComercio() == null ||
                !user.getComercio().getId().equals(comercioId)) {
            user.setComercio(comercio);
            userRepository.save(user);
        }

        DayOfWeek dia = dto.getDate().getDayOfWeek();
        if (!horarioService.isAberto(comercioId, dia)) {
            throw new BadrequestExeption(
                    "O comércio não funciona neste dia!"
            );
        }

        HorarioFuncionamentoEntity horario =
                horarioService.findByComercioAndDia(comercioId, dia);

        LocalTime fim = dto.getTime().plusMinutes(job.getDurationMinutes());

        if (dto.getTime().isBefore(horario.getAbertura()) ||
                fim.isAfter(horario.getFechamento())) {
            throw new BadrequestExeption("Horário fora do funcionamento!");
        }

        if (bloqueioService.isHorarioBloqueado(
                comercioId, dto.getDate(), dto.getTime(), fim)) {
            throw new BadrequestExeption("Horário bloqueado. Escolha outro!");
        }

        FuncionarioEntity funcionario = dto.getFuncionarioId() != null
                ? funcionarioRepository.findById(dto.getFuncionarioId())
                  .orElse(null)
                : null;

        // ✅ Valida conflito — por funcionário se selecionado, por comércio caso contrário
        List<AppointmentsEntity> booked = funcionario != null
                ? appointmentsRepository.findByDateAndStatusInAndFuncionarioId(
                dto.getDate(),
                List.of(AppointmentsStatusEnum.PENDING,
                        AppointmentsStatusEnum.CONFIRMED),
                funcionario.getId()
        )
                : appointmentsRepository.findByDateAndStatusInAndComercioId(
                dto.getDate(),
                List.of(AppointmentsStatusEnum.PENDING,
                        AppointmentsStatusEnum.CONFIRMED),
                comercioId
        );

        boolean conflito = booked.stream().anyMatch(b -> {
            LocalTime bs = b.getTime();
            LocalTime be = bs.plusMinutes(b.getJob().getDurationMinutes());
            return dto.getTime().isBefore(be) && fim.isAfter(bs);
        });

        if (conflito) {
            throw new BadrequestExeption("Horário já ocupado!");
        }

        AppointmentsEntity appointment = AppointmentsEntity.builder()
                .user(user)
                .job(job)
                .comercio(comercio)
                .funcionario(funcionario)
                .date(dto.getDate())
                .time(dto.getTime())
                .status(AppointmentsStatusEnum.PENDING)
                .build();

        AppointmentsEntity saved = appointmentsRepository.save(appointment);

        emailService.enviarConfirmacao(saved);

        sseEmitterService.sendToComercio(
                comercioId,
                "APPOINTMENT_CREATED",
                Map.of(
                        "appointment", toResponseDto(saved),
                        "message", "Novo agendamento via link público: "
                                + dto.getNome()
                )
        );

        log.info("Agendamento público: comercioId={}, user={}, job={}",
                comercioId, dto.getEmail(), job.getName());
    }

    private String gerarSenhaAleatoria() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private AppointmentResponseDto toResponseDto(AppointmentsEntity e) {
        return AppointmentResponseDto.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .userName(e.getUser().getName())
                .jobId(e.getJob().getId())
                .jobName(e.getJob().getName())
                .jobPrice(e.getJob().getPrice())
                .jobDurationMinutes(e.getJob().getDurationMinutes())
                .comercioId(e.getComercio().getId())
                .comercioNome(e.getComercio().getNome())
                .funcionarioId(e.getFuncionario() != null
                        ? e.getFuncionario().getId() : null)
                .funcionarioNome(e.getFuncionario() != null
                        ? e.getFuncionario().getNome() : null)
                .date(e.getDate())
                .time(e.getTime())
                .status(e.getStatus())
                .jaAvaliou(false)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}