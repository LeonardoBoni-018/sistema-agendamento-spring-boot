package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.ComercioEntity;
import com.sistemaagendamento.databse.model.JobEntity;
import com.sistemaagendamento.databse.model.UserEntity;
import com.sistemaagendamento.databse.repository.IAppointmentsRepository;
import com.sistemaagendamento.databse.repository.IJobRepository;
import com.sistemaagendamento.dto.JobDto;
import com.sistemaagendamento.dto.JobResponseDto;
import com.sistemaagendamento.dto.JobUpdateDto;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final IJobRepository jobRepository;
    private final SseEmitterService sseEmitterService;
    private final IAppointmentsRepository appointmentsRepository;

    public List<JobResponseDto> findAllJobs(Authentication authentication) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        
        if (loggedUser.getComercio() == null) {
            return jobRepository.findAll().stream()
                    .map(this::toResponseDto)
                    .toList();
        }
        
        return jobRepository.findByComercioId(loggedUser.getComercio().getId())
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public JobResponseDto findJobById(Integer jobId, Authentication authentication) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        if (loggedUser.getComercio() != null && 
            job.getComercio() != null && 
            !job.getComercio().getId().equals(loggedUser.getComercio().getId())) {
            throw new BadrequestExeption("Serviço não pertence ao seu comércio!");
        }

        return toResponseDto(job);
    }

    public void createJob(JobDto jobDto, Authentication authentication) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();
        ComercioEntity comercio = loggedUser.getComercio();

        JobEntity job = JobEntity.builder()
                .name(jobDto.getName())
                .description(jobDto.getDescription())
                .price(jobDto.getPrice())
                .durationMinutes(jobDto.getDurationMinutes())
                .build();
        
        if (comercio != null) {
            job.setComercio(comercio);
        }
        
        JobEntity saved = jobRepository.save(job);
        log.info("Serviço criado: id={}, nome={}", saved.getId(), saved.getName());
        
        if (comercio != null) {
            sseEmitterService.sendToComercio(comercio.getId(), "JOB_CREATED", 
                Map.of("job", toResponseDto(saved), "message", "Novo serviço criado: " + saved.getName()));
        }
    }

    public void updateJob(
            Integer jobId,
            JobUpdateDto jobDto,
            Authentication authentication
    ) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        if (loggedUser.getComercio() != null && job.getComercio() != null) {
            if (!job.getComercio().getId().equals(loggedUser.getComercio().getId())) {
                throw new BadrequestExeption("Serviço não pertence ao seu comércio!");
            }
        }

        if (jobDto.getName() != null) job.setName(jobDto.getName());
        if (jobDto.getDescription() != null) job.setDescription(jobDto.getDescription());
        if (jobDto.getPrice() != null) job.setPrice(jobDto.getPrice());
        if (jobDto.getDurationMinutes() != null) job.setDurationMinutes(jobDto.getDurationMinutes());

        jobRepository.save(job);
        log.info("Serviço atualizado: id={}", jobId);
        
        if (job.getComercio() != null) {
            sseEmitterService.sendToComercio(job.getComercio().getId(), "JOB_UPDATED", 
                Map.of("job", toResponseDto(job), "message", "Serviço atualizado: " + job.getName()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Integer jobId, Authentication authentication) {
        UserEntity loggedUser = (UserEntity) authentication.getPrincipal();

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        if (loggedUser.getComercio() != null && job.getComercio() != null) {
            if (!job.getComercio().getId().equals(loggedUser.getComercio().getId())) {
                throw new BadrequestExeption("Serviço não pertence ao seu comércio!");
            }
        }

        boolean hasActiveAppointments = appointmentsRepository
                .existsByJobIdAndStatusIn(
                        jobId,
                        List.of(
                                AppointmentsStatusEnum.PENDING,
                                AppointmentsStatusEnum.CONFIRMED
                        )
                );

        if (hasActiveAppointments) {
            throw new BadrequestExeption(
                    "Não é possível excluir um serviço com agendamentos ativos!"
            );
        }

        jobRepository.deleteById(jobId);
        log.info("Serviço deletado: id={}", jobId);
        
        if (job.getComercio() != null) {
            sseEmitterService.sendToComercio(job.getComercio().getId(), "JOB_DELETED", 
                Map.of("jobId", jobId, "message", "Serviço removido: " + job.getName()));
        }
    }

    private JobResponseDto toResponseDto(JobEntity entity) {
        Integer comercioId = entity.getComercio() != null ? entity.getComercio().getId() : null;
        String comercioNome = entity.getComercio() != null ? entity.getComercio().getNome() : null;
        
        return JobResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .durationMinutes(entity.getDurationMinutes())
                .comercioId(comercioId)
                .comercioNome(comercioNome)
                .build();
    }
}