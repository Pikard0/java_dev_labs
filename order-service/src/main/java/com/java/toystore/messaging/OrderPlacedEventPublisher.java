package com.java.toystore.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class OrderPlacedEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    public OrderPlacedEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${toy-store.messaging.exchange}") String exchangeName,
            @Value("${toy-store.messaging.order-placed-routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(OrderPlacedEvent event) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event, this::makePersistent);
        log.info("Published order placed event {} for order {}", event.eventId(), event.orderId());
    }

    private Message makePersistent(Message message) {
        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        return message;
    }
}
