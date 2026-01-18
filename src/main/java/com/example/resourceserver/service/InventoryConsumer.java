package com.example.resourceserver.service;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryConsumer {

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void handleOrderEvent(String message) {
        System.out.println("Inventory Service received event: " + message);
    }
}
