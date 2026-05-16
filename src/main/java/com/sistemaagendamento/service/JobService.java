package com.sistemaagendamento.service;

import com.sistemaagendamento.databse.model.JobEntity;
import com.sistemaagendamento.databse.repository.IAppointmentsRepository;
import com.sistemaagendamento.databse.repository.IJobRepository;
import com.sistemaagendamento.dto.JobDto;
import com.sistemaagendamento.dto.JobResponseDto;
import com.sistemaagendamento.dto.JobUpdateDto;
import com.sistemaagendamento.enums.AppointmentsStatusEnum;
import com.sistemaagendamento.exception.BadrequestExeption;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final IJobRepository jobRepository;
    private final IAppointmentsRepository appointmentsRepository;

    public List<JobResponseDto> findAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public JobResponseDto findJobById(Integer jobId) {
        return jobRepository.findById(jobId)
                .map(this::toResponseDto)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));
    }

    public void createJob(JobDto jobDto) {
        jobRepository.save(JobEntity.builder()
                .name(jobDto.getName())
                .description(jobDto.getDescription())
                .price(jobDto.getPrice())
                .durationMinutes(jobDto.getDurationMinutes())
                .build()
        );
    }

    public void updateJob(Integer jobId, JobUpdateDto jobDto) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        if (jobDto.getName() != null) {
            job.setName(jobDto.getName());
        }

        if (jobDto.getDescription() != null) {
            job.setDescription(jobDto.getDescription());
        }

        if (jobDto.getPrice() != null) {
            job.setPrice(jobDto.getPrice());
        }

        if (jobDto.getDurationMinutes() != null) {
            job.setDurationMinutes(jobDto.getDurationMinutes());
        }

        jobRepository.save(job);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Integer jobId) {
        jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        //impede exclusão com agendamentos ativos
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
    }

    private JobResponseDto toResponseDto(JobEntity entity) {
        return JobResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .durationMinutes(entity.getDurationMinutes())
                .build();
    }
}