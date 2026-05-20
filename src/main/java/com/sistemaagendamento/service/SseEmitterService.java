package com.sistemaagendamento.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterService {

    private final ObjectMapper objectMapper;

    private final Map<Integer, List<SseEmitter>> emitters =
            new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Integer comercioId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.computeIfAbsent(comercioId, k ->
                new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(comercioId, emitter));
        emitter.onTimeout(() -> remove(comercioId, emitter));
        emitter.onError(e -> remove(comercioId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("ok"));
        } catch (IOException e) {
            remove(comercioId, emitter);
        }

        log.info("SSE conectado: comercioId={}, total emissores={}",
                comercioId, emitters.getOrDefault(comercioId, List.of()).size());

        return emitter;
    }

    public void sendToComercio(Integer comercioId, String eventType, Object data) {
        log.info("[SSE] Enviando evento: eventType={}, comercioId={}, total emissores={}",
                eventType, comercioId, emitters.getOrDefault(comercioId, List.of()).size());

        List<SseEmitter> comercioEmitters =
                emitters.getOrDefault(comercioId, List.of());

        if (comercioEmitters.isEmpty()) {
            log.warn("[SSE] Nenhum emissor encontrado para comercioId={}", comercioId);
            return;
        }

        List<SseEmitter> dead = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : comercioEmitters) {
            try {
                String json = objectMapper.writeValueAsString(
                        Map.of("type", eventType, "data", data)
                );
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(json));
                log.info("[SSE] Evento enviado com sucesso para comercioId={}", comercioId);
            } catch (IOException e) {
                log.error("[SSE] Erro ao enviar para comercioId={}", comercioId, e);
                dead.add(emitter);
            }
        }

        dead.forEach(e -> remove(comercioId, e));
    }

    private void remove(Integer comercioId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(comercioId);
        if (list != null) {
            list.remove(emitter);
            log.info("SSE desconectado: comercioId={}, restantes={}",
                    comercioId, list.size());
        }
    }
}