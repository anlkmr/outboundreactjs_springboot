package com.cestasoft.mobileservices.msp.outbound.component.amqp;

import com.cestasoft.mobileservices.msp.outbound.model.DispatchBulkMessageDTO;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchErrorMessageDTO;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchMessageDTO;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchMultiMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publishes messages to RabbitMq queues
 * @author ezra.k@cestasoft.com
 */
@Component
public class MessageSender {

    final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    @Value(value = "${outbound.smpp.messaging.queue}")
    private String amqpQueue;

    @Value(value = "${outbound.smpp.messaging.bulk}")
    private String amqpBulkQueue;

    @Value(value = "${outbound.smpp.messaging.multi}")
    private String amqpMultiQueue;

    @Value(value = "${outbound.processor.error.queue}")
    private String amqpErrorQueue;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(DispatchMessageDTO messageDTO) {
        rabbitTemplate.convertAndSend(amqpQueue, messageDTO);
    }

    public void sendBulkMessages(DispatchBulkMessageDTO bulkMessageDTO) {
        rabbitTemplate.convertAndSend(amqpBulkQueue, bulkMessageDTO);
    }

    public void sendMultiMessages(DispatchMultiMessageDTO multiMessageDTO) {
        rabbitTemplate.convertAndSend(amqpMultiQueue, multiMessageDTO);
    }

    public void sendErrorMessages(DispatchErrorMessageDTO errorMessageDTO) {
        rabbitTemplate.convertAndSend(amqpErrorQueue, errorMessageDTO);
    }
}
