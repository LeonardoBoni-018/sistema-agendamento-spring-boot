package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.JobDto;
import com.sistemaagendamento.dto.JobResponseDto;
import com.sistemaagendamento.dto.JobUpdateDto;
import com.sistemaagendamento.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/job")
@RequiredArgsConstructor
@Validated
public class JobController {

    private final JobService jobService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public void createJob(@Valid @RequestBody JobDto jobDto) {
        jobService.createJob(jobDto);
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public void updateJob(
            @PathVariable Integer jobId,
            @RequestBody JobUpdateDto jobUpdateDto
    ) {
        jobService.updateJob(jobId, jobUpdateDto);
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable Integer jobId) {
        jobService.deleteJob(jobId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<JobResponseDto> getAllJobs() {
        return jobService.findAllJobs();
    }

    @GetMapping("/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public JobResponseDto getJobById(@PathVariable Integer jobId) {
        return jobService.findJobById(jobId);
    }
}