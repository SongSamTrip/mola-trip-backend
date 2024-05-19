package com.mola.global.sse;

import com.mola.domain.trip.dto.TripListHtmlDto;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class TripPlanSseRegistry {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitterForTrip(Long tripId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.computeIfAbsent(tripId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(tripId, emitter));

        emitter.onTimeout(() -> {
            emitter.complete();
            removeEmitter(tripId, emitter);
        });
        return emitter;
    }

    private void removeEmitter(Long tripId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitterList = emitters.get(tripId);
        if (emitterList != null) {
            emitterList.remove(emitter);
            if (emitterList.isEmpty()) {
                emitters.remove(tripId);
            }
        }
    }

    public void sendUpdate(Long tripId, TripListHtmlDto data) {
        List<SseEmitter> tripEmitters = emitters.get(tripId);

        if (tripEmitters != null) {
            for (SseEmitter emitter : tripEmitters) {
                try {
                    emitter.send(SseEmitter.event().data(data));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        }
    }
}




