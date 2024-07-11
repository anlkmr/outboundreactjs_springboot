package com.cestasoft.mobileservices.msp.outbound.controller;

import com.cestasoft.mobileservices.msp.outbound.component.amqp.MessageSender;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchBulkMessageDTO;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchMessageDTO;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchMultiMessageDTO;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller with endpoints for sending mes single message, bulk messages and multi messages
 * @author ezra.k@cestasoft.com
 */
@RestController
@RequestMapping("/api/v1/sms")
public class DispatchController {

    final Logger logger = LoggerFactory.getLogger(DispatchController.class);

    @Autowired
    private MessageSender messageSender;

    @PostMapping("/dispatch/message")
    Document dispatchMessage(@RequestBody DispatchMessageDTO messageDTO) {
        boolean status = false;
        try {
            messageSender.sendMessage(messageDTO);
            status = true;
            logger.debug("message queued: {}", messageDTO.getTransactionId());
        } catch(Exception ex) {
            logger.error("error queueing message", ex);
        }
        return new Document()
            .append("success", status)
            .append("txid", messageDTO.getTransactionId());
    }

    @PostMapping("/dispatch/bulk/message")
    Document dispatchBulkMessage(@RequestBody DispatchBulkMessageDTO bulkMessageDTO) {
        boolean status = false;
        try {
            messageSender.sendBulkMessages(bulkMessageDTO);
            status = true;
            logger.debug("bulk messages queued: {}", bulkMessageDTO.getTransactionId());
        } catch(Exception ex) {
            logger.error("error queueing bulk messages", ex);
        }
        return new Document()
                .append("success", status)
                .append("txid", bulkMessageDTO.getTransactionId());
    }

    @PostMapping("/dispatch/multi/message")
    Document dispatchMultiMessage(@RequestBody DispatchMultiMessageDTO multiMessageDTO) {
        boolean status = false;
        try {
            messageSender.sendMultiMessages(multiMessageDTO);
            status = true;
            logger.debug("multi messages queued: {}", multiMessageDTO.getTransactionId());
        } catch(Exception ex) {
            logger.error("error queueing multi messages", ex);
        }
        return new Document()
                .append("success", status)
                .append("txid", multiMessageDTO.getTransactionId());
    }
}
