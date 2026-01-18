package com.example.resourceserver.service;


import com.example.resourceserver.model.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderEvent(String orderId) {
        OrderEvent event = new OrderEvent(orderId, "New Order created");
        //String message = "New Order: " + orderId;
        kafkaTemplate.send("order-events", event);
        System.out.println("Sent event: " + event);
    }
}
