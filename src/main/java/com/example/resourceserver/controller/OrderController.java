package com.example.resourceserver.controller;

import com.example.resourceserver.service.OrderEventProducer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderEventProducer producer;

    public OrderController(OrderEventProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/{orderId}")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    public String createOrder(@PathVariable String orderId) {
        producer.sendOrderEvent(orderId);
        return "Order event published for " + orderId;
    }
}
