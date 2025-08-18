package com.example.orderservice.controller;

import com.example.orderservice.client.UserServiceClient;
import com.example.orderservice.dto.UserDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "Operations related to order management")
public class OrderController {
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;

    @GetMapping
    @Operation(summary = "Get all orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok().body(order))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new order with user validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user or validation failed"),
            @ApiResponse(responseCode = "503", description = "User service unavailable - order created without validation")
    })
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        log.info("Creating order for user: {}", order.getUserId());

        // Try to validate user exists using circuit breaker
        UserDto user = userServiceClient.getUserById(order.getUserId());

        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            // User service is down, but we still create the order
            Order savedOrder = orderRepository.save(order);
            response.put("order", savedOrder);
            response.put("warning", "User service unavailable. Order created without user validation.");
            response.put("userValidated", false);

            log.warn("Order {} created without user validation due to service unavailability",
                    savedOrder.getId());

            return ResponseEntity.status(503).body(response);
        }

        if (user.getEmail().equals("unknown@fallback.com")) {
            // This is a fallback user, service might be degraded
            Order savedOrder = orderRepository.save(order);
            response.put("order", savedOrder);
            response.put("warning", "User service degraded. Order created with fallback validation.");
            response.put("userValidated", false);
            response.put("fallbackUser", user);

            return ResponseEntity.status(503).body(response);
        }

        // Normal flow - user service is working fine
        Order savedOrder = orderRepository.save(order);
        response.put("order", savedOrder);
        response.put("user", user);
        response.put("userValidated", true);

        log.info("Order {} created successfully for user {}", savedOrder.getId(), user.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user details for order validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "503", description = "User service unavailable")
    })
    public ResponseEntity<?> getUserDetails(@PathVariable Long userId) {
        log.info("Fetching user details for user: {}", userId);

        UserDto user = userServiceClient.getUserById(userId);

        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User service unavailable");
            error.put("userId", userId.toString());
            return ResponseEntity.status(503).body(error);
        }

        if (user.getEmail().equals("unknown@fallback.com")) {
            Map<String, Object> response = new HashMap<>();
            response.put("warning", "User service degraded - returning fallback data");
            response.put("user", user);
            return ResponseEntity.status(503).body(response);
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/circuit-breaker/status")
    @Operation(summary = "Get circuit breaker status for user service")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        // This will be handled by actuator endpoints, but we can add custom logic here
        Map<String, Object> status = new HashMap<>();
        status.put("message", "Check /actuator/circuitbreakers for detailed status");
        status.put("healthEndpoint", "/actuator/health");
        status.put("eventsEndpoint", "/actuator/circuitbreakerevents");
        return ResponseEntity.ok(status);
    }
}
