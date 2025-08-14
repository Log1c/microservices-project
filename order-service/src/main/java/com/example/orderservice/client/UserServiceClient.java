package com.example.orderservice.client;

import com.example.orderservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    private final WebClient webClient;

    @Value("${user-service.base-url}")
    private String userServiceBaseUrl;

    public UserDto getUserById(Long userId) {
        try {
            return webClient.get()
                    .uri(userServiceBaseUrl + "/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error fetching user with id {}: {}", userId, e.getMessage());
            return null;
        }
    }
}
