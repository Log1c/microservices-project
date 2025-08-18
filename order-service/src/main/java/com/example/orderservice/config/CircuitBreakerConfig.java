package com.example.orderservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CircuitBreakerConfig {

    @Bean
    public RegistryEventConsumer<CircuitBreaker> myRegistryEventConsumer() {
        return new RegistryEventConsumer<>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                log.info("Circuit breaker added: {}", entryAddedEvent.getAddedEntry().getName());
                entryAddedEvent.getAddedEntry().getEventPublisher()
                        .onSuccess(event -> log.info("Circuit breaker '{}': Successful call", event.getCircuitBreakerName()))
                        .onError(event -> log.error("Circuit breaker '{}': Failed call - {}",
                                event.getCircuitBreakerName(), event.getThrowable().getMessage()))
                        .onStateTransition(event -> log.warn("Circuit breaker '{}': State transition from {} to {}",
                                event.getCircuitBreakerName(), event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
                log.info("Circuit breaker removed: {}", entryRemoveEvent.getRemovedEntry().getName());
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
                log.info("Circuit breaker replaced: {}", entryReplacedEvent.getNewEntry().getName());
            }
        };
    }
}
