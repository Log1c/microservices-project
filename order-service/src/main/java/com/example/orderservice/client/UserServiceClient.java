package com.example.orderservice.client;

import com.example.orderservice.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    private final WebClient webClient;

    @Value("${user-service.base-url}")
    private String userServiceBaseUrl;

    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserByIdFallback")
    public UserDto getUserById(Long userId) {
        log.info("Calling User Service to get user with id: {}", userId);

        try {
            return webClient.get()
                    .uri(userServiceBaseUrl + "/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error calling User Service for user {}: {} - {}",
                    userId, e.getStatusCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling User Service for user {}: {}",
                    userId, e.getMessage());
            throw e;
        }
    }

    // Fallback method
    public UserDto getUserByIdFallback(Long userId, Exception ex) {
        log.warn("Circuit breaker activated for user-service. Using fallback for user: {}. Error: {}",
                userId, ex.getMessage());

        // Return a fallback user or null based on business logic
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(userId);
        fallbackUser.setName("Unknown User (Service Unavailable)");
        fallbackUser.setEmail("unknown@fallback.com");

        return fallbackUser;
    }

    // Alternative method that returns null instead of fallback user
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserByIdFallbackNull")
    public UserDto getUserByIdWithNullFallback(Long userId) {
        return getUserById(userId);
    }

    public UserDto getUserByIdFallbackNull(Long userId, Exception ex) {
        log.warn("Circuit breaker activated for user-service. Returning null for user: {}. Error: {}",
                userId, ex.getMessage());
        return null;
    }
}
