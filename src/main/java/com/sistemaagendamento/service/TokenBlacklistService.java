package com.sistemaagendamento.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blacklist em memória, thread-safe, com expiração: o token só precisa ficar
 * listado enquanto ainda for válido, então entradas vencidas são removidas.
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /** Mantém o token bloqueado até o maior TTL possível (refresh token). */
    private static final long MAX_TTL_MS = 8L * 24 * 60 * 60 * 1000;

    public void blacklist(String token) {
        blacklistedTokens.put(token, System.currentTimeMillis() + MAX_TTL_MS);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    @Scheduled(fixedDelay = 3_600_000)
    void purgeExpired() {
        long now = System.currentTimeMillis();
        blacklistedTokens.values().removeIf(exp -> exp < now);
    }
}
