package com.sistemaagendamento.service;


import com.sistemaagendamento.databse.model.JobEntity;
import com.sistemaagendamento.databse.repository.IJobRepository;
import com.sistemaagendamento.dto.JobDto;
import com.sistemaagendamento.dto.JobUpdateDto;
import com.sistemaagendamento.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final IJobRepository jobRepository;

    public List<JobEntity> fildAllJobs(){
        return jobRepository.findAll();
    }

    public JobEntity findJobById(Integer jobId){
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));
        return job;
    }

    public void createJob(JobDto jobDto){
        jobRepository.save(JobEntity.builder()
                .name(jobDto.getName())
                .description(jobDto.getDescription())
                .price(jobDto.getPrice())
                .durationMinutes(jobDto.getDurationMinutes())
                .build()
        );
    }

    public void updateJob(Integer jobId, JobUpdateDto jobDto){
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        if(jobDto.getName() != null){
            job.setName(jobDto.getName());
        }

        if(jobDto.getDescription() != null){
            job.setDescription(jobDto.getDescription());
        }

        if(jobDto.getPrice() != null){
            job.setPrice(jobDto.getPrice());
        }

        if(jobDto.getDurationMinutes() != null){
            job.setDurationMinutes(jobDto.getDurationMinutes());
        }

        jobRepository.save(job);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Serviço não encontrado!"));

        jobRepository.deleteById(jobId);
    }
}
