package com.example.orderservice.controller;

import com.example.orderservice.client.UserServiceClient;
import com.example.orderservice.dto.UserDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
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
    @Operation(summary = "Create new order")
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        // Validate that user exists by calling User Service
        UserDto user = userServiceClient.getUserById(order.getUserId());

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body("User with id " + order.getUserId() + " not found");
        }

        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user details for order validation")
    public ResponseEntity<UserDto> getUserDetails(@PathVariable Long userId) {
        UserDto user = userServiceClient.getUserById(userId);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }
}
