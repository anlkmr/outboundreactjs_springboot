package com.cestasoft.mobileservices.msp.outbound.component.error;

import com.cestasoft.mobileservices.msp.outbound.component.amqp.MessageSender;
import com.cestasoft.mobileservices.msp.outbound.model.ChannelDTO;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchErrorMessageDTO;
import com.cestasoft.mobileservices.msp.outbound.model.DispatchTransactionDTO;
import com.cestasoft.mobileservices.msp.outbound.service.SMPPDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorManager {

    final Logger logger = LoggerFactory.getLogger(ErrorManager.class);
    @Autowired
    MessageSender _messageSender;

    public void processError(final ChannelDTO channel, DispatchTransactionDTO dispatchMessage, final Exception x, final Logger srcLogger, String description) {
        srcLogger.error(description, x);
        DispatchErrorMessageDTO errorMessage = new DispatchErrorMessageDTO();
        errorMessage.setTimestamp(System.currentTimeMillis());
        errorMessage.setErrorMessage(x.getMessage() + ": " + description);
        errorMessage.setErrorType(x.getClass().getTypeName());
        errorMessage.setTransactionId(dispatchMessage.getTransactionId());
        errorMessage.setErrorPayload(dispatchMessage);
        errorMessage.setChannel(channel);
        errorMessage.setChannelName(channel.getName());
        logger.debug("dispatching error [{}]: {}", errorMessage.getErrorType(), errorMessage.getTransactionId());
        _messageSender.sendErrorMessages(errorMessage);
    }
}
