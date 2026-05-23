package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.*;
import com.sistemaagendamento.databse.model.FilaEsperaEntity.FilaStatusEnum;
import com.sistemaagendamento.databse.repository.*;
import com.sistemaagendamento.dto.FilaEsperaDto;
import com.sistemaagendamento.dto.FilaEsperaResponseDto;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilaEsperaService {

    private final IFilaEsperaRepository filaRepository;
    private final IJobRepository jobRepository;
    private final IFuncionarioRepository funcionarioRepository;
    private final EmailService emailService;
    private final SseEmitterService sseEmitterService;

    public FilaEsperaResponseDto entrarNaFila(
            FilaEsperaDto dto,
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        if (filaRepository.existsByUserIdAndJobIdAndDateAndStatus(
                user.getId(), dto.getJobId(), dto.getDate(),
                FilaStatusEnum.AGUARDANDO)) {
            throw new BadrequestExeption(
                    "Você já está na fila para este serviço nesta data!"
            );
        }

        JobEntity job = jobRepository.findById(dto.getJobId())
                .orElseThrow(() ->
                        new NotFoundException("Serviço não encontrado!")
                );

        if (!job.getComercio().getId().equals(user.getComercio().getId())) {
            throw new BadrequestExeption("Serviço não pertence ao seu comércio!");
        }

        FuncionarioEntity funcionario = dto.getFuncionarioId() != null
                ? funcionarioRepository.findById(dto.getFuncionarioId())
                  .orElse(null)
                : null;

        FilaEsperaEntity fila = FilaEsperaEntity.builder()
                .user(user)
                .job(job)
                .comercio(user.getComercio())
                .funcionario(funcionario)
                .date(dto.getDate())
                .horarioPreferido(dto.getHorarioPreferido())
                .status(FilaStatusEnum.AGUARDANDO)
                .build();

        FilaEsperaEntity saved = filaRepository.save(fila);

        List<FilaEsperaEntity> fila_ = filaRepository
                .findAguardandoByComercioAndData(
                        user.getComercio().getId(), dto.getDate()
                );
        int posicao = fila_.indexOf(saved) + 1;

        log.info("Usuário {} entrou na fila: jobId={}, data={}, posição={}",
                user.getId(), dto.getJobId(), dto.getDate(), posicao);

        return toDto(saved, posicao);
    }

    public List<FilaEsperaResponseDto> minhaFila(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return filaRepository.findByUserIdAndStatus(user.getId(),
                        FilaStatusEnum.AGUARDANDO)
                .stream()
                .map(f -> toDto(f, calcPosicao(f)))
                .toList();
    }

    public List<FilaEsperaResponseDto> filaDoComercio(
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return filaRepository.findByComercioIdAndStatusOrderByCreatedAtAsc(
                        user.getComercio().getId(), FilaStatusEnum.AGUARDANDO)
                .stream()
                .map(f -> toDto(f, calcPosicao(f)))
                .toList();
    }

    public void sairDaFila(Integer filaId, Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        FilaEsperaEntity fila = filaRepository.findById(filaId)
                .orElseThrow(() ->
                        new NotFoundException("Entrada na fila não encontrada!")
                );

        if (!fila.getUser().getId().equals(user.getId())) {
            throw new BadrequestExeption("Sem permissão!");
        }

        fila.setStatus(FilaStatusEnum.EXPIRADO);
        filaRepository.save(fila);
    }

    public void notificarProximoDaFila(
            Integer comercioId,
            LocalDate date,
            Integer jobId
    ) {
        List<FilaEsperaEntity> fila = filaRepository
                .findAguardandoByComercioAndData(comercioId, date)
                .stream()
                .filter(f -> f.getJob().getId().equals(jobId))
                .toList();

        if (fila.isEmpty()) return;

        FilaEsperaEntity proximo = fila.get(0);
        proximo.setStatus(FilaStatusEnum.NOTIFICADO);
        filaRepository.save(proximo);

        emailService.enviarNotificacaoFila(proximo);

        sseEmitterService.sendToComercio(
                comercioId,
                "FILA_VAGA_DISPONIVEL",
                Map.of(
                        "filaId", proximo.getId(),
                        "userId", proximo.getUser().getId(),
                        "jobName", proximo.getJob().getName(),
                        "date", date.toString(),
                        "message", "Vaga disponível para "
                                + proximo.getJob().getName()
                                + " em " + date
                )
        );

        log.info("Próximo da fila notificado: userId={}, filaId={}",
                proximo.getUser().getId(), proximo.getId());
    }

    private int calcPosicao(FilaEsperaEntity fila) {
        List<FilaEsperaEntity> lista = filaRepository
                .findAguardandoByComercioAndData(
                        fila.getComercio().getId(), fila.getDate()
                );
        AtomicInteger pos = new AtomicInteger(1);
        for (FilaEsperaEntity f : lista) {
            if (f.getId().equals(fila.getId())) return pos.get();
            pos.incrementAndGet();
        }
        return pos.get();
    }

    private FilaEsperaResponseDto toDto(FilaEsperaEntity e, int posicao) {
        return FilaEsperaResponseDto.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .userName(e.getUser().getName())
                .jobId(e.getJob().getId())
                .jobName(e.getJob().getName())
                .comercioId(e.getComercio().getId())
                .comercioNome(e.getComercio().getNome())
                .funcionarioId(e.getFuncionario() != null
                        ? e.getFuncionario().getId() : null)
                .funcionarioNome(e.getFuncionario() != null
                        ? e.getFuncionario().getNome() : null)
                .date(e.getDate())
                .horarioPreferido(e.getHorarioPreferido())
                .status(e.getStatus())
                .posicao(posicao)
                .build();
    }
}