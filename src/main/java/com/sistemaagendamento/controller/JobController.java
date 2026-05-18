package com.sistemaagendamento.controller;

import com.sistemaagendamento.dto.JobDto;
import com.sistemaagendamento.dto.JobResponseDto;
import com.sistemaagendamento.dto.JobUpdateDto;
import com.sistemaagendamento.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/job")
@RequiredArgsConstructor
@Validated
@Tag(name = "Serviços", description = "Gerenciamento de serviços do comércio")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um serviço no comércio do admin")
    public void createJob(
            @Valid @RequestBody JobDto jobDto,
            Authentication authentication
    ) {
        jobService.createJob(jobDto, authentication);
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualiza um serviço do comércio")
    public void updateJob(
            @PathVariable Integer jobId,
            @RequestBody JobUpdateDto jobUpdateDto,
            Authentication authentication
    ) {
        jobService.updateJob(jobId, jobUpdateDto, authentication);
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um serviço do comércio")
    public void deleteJob(
            @PathVariable Integer jobId,
            Authentication authentication
    ) {
        jobService.deleteJob(jobId, authentication);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista serviços do comércio do usuário logado")
    public List<JobResponseDto> getAllJobs(Authentication authentication) {
        return jobService.findAllJobs(authentication);
    }

    @GetMapping("/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Busca serviço por ID")
    public JobResponseDto getJobById(
            @PathVariable Integer jobId,
            Authentication authentication
    ) {
        return jobService.findJobById(jobId, authentication);
    }
}