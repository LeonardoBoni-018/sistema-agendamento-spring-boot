package com.sistemaagendamento.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita tentativas por IP nos endpoints sensíveis de autenticação (janela fixa),
 * dificultando força bruta e abuso de cadastro.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private record Window(long start, int count) {}

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Value("${security.rate-limit.max-attempts:10}")
    private int maxAttempts;

    @Value("${security.rate-limit.window-ms:60000}")
    private long windowMs;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !"POST".equals(request.getMethod())
                || !(path.equals("/v1/auth/login")
                || path.equals("/v1/auth/register")
                || path.equals("/v1/auth/refresh"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        String key = request.getRequestURI() + "|" + request.getRemoteAddr();

        Window w = windows.compute(key, (k, cur) ->
                (cur == null || now - cur.start() > windowMs)
                        ? new Window(now, 1)
                        : new Window(cur.start(), cur.count() + 1));

        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(e -> now - e.getValue().start() > windowMs);
        }

        if (w.count() > maxAttempts) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Muitas tentativas. Aguarde um instante.\",\"status\":429}");
            return;
        }
        chain.doFilter(request, response);
    }
}
