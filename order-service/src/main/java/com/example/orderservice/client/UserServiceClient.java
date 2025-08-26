package com.example.orderservice.client;

import com.example.orderservice.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    private final WebClient webClient;

    @Value("${user-service.base-url}")
    private String userServiceBaseUrl;

    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "user-service", fallbackMethod = "getUserByIdFallback")
    @TimeLimiter(name = "user-service")
    public CompletableFuture<UserDto> getUserById(Long userId) {
        log.info("Calling User Service to get user with id: {}", userId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return webClient.get()
                        .uri(userServiceBaseUrl + "/api/users/{id}", userId)
                        .retrieve()
                        .bodyToMono(UserDto.class)
                        .block(Duration.ofSeconds(5)); // Це буде контролюватися TimeLimiter
            } catch (WebClientResponseException e) {
                log.error("Error calling User Service for user {}: {} - {}",
                        userId, e.getStatusCode(), e.getMessage());
                throw new RuntimeException("User service call failed", e);
            } catch (Exception e) {
                log.error("Unexpected error calling User Service for user {}: {}",
                        userId, e.getMessage());
                throw new RuntimeException("Unexpected error during user service call", e);
            }
        });
    }

    // Якщо потрібен синхронний варіант (для зворотної сумісності)
    public UserDto getUserByIdSync(Long userId) {
        try {
            return getUserById(userId).get();
        } catch (Exception e) {
            log.error("Error getting user {} synchronously: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user", e);
        }
    }

    // Fallback method для всіх анотацій
    public CompletableFuture<UserDto> getUserByIdFallback(Long userId, Exception ex) {
        log.warn("Fallback activated for user-service. User: {}. Error: {}",
                userId, ex.getMessage());

        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(userId);
        fallbackUser.setName("Unknown User (Service Unavailable)");
        fallbackUser.setEmail("unknown@fallback.com");

        return CompletableFuture.completedFuture(fallbackUser);
    }

    // Fallback для синхронного методу
    public UserDto getUserByIdSyncFallback(Long userId, Exception ex) {
        log.warn("Sync fallback activated for user-service. User: {}. Error: {}",
                userId, ex.getMessage());

        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(userId);
        fallbackUser.setName("Unknown User (Service Unavailable)");
        fallbackUser.setEmail("unknown@fallback.com");

        return fallbackUser;
    }
}
