package com.cestasoft.mobileservices.msp.outbound.component.smpp;

import com.cestasoft.mobileservices.msp.outbound.model.DispatchBulkMessageDTO;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchMessageDTO;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchMultiMessageDTO;
import com.cestasoft.mobileservices.msp.outbound.service.SMPPDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Processes consumed messages before dispatching them
 * @author ezra.k@cestasoft.com
 */
@Component
public class MessageProcessor {

    // TODO - log transaction starts with complete messages

    @Autowired
    private SMPPDispatcher dispatcher;

    public void processMessage(DispatchMessageDTO messageDTO) {
        messageDTO.setTimestamp(System.currentTimeMillis());
        dispatcher.dispatchMessage(messageDTO);
    }

    public void processBulkMessages(DispatchBulkMessageDTO bulkMessageDTO) {
        bulkMessageDTO.setTimestamp(System.currentTimeMillis());
        dispatcher.dispatchBulkMessages(bulkMessageDTO);
    }

    public void processMultiMessages(DispatchMultiMessageDTO multiMessageDTO) {
        multiMessageDTO.setTimestamp(System.currentTimeMillis());
        dispatcher.dispatchMultiMessages(multiMessageDTO);
    }
}
