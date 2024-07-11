package com.cestasoft.mobileservices.msp.outbound.service;

import com.cestasoft.mobileservices.msp.outbound.component.error.ErrorManager;
import com.cestasoft.mobileservices.msp.outbound.component.smpp.MessageReceiverListenerImpl;
import com.cestasoft.mobileservices.msp.outbound.config.context.SMPPContext;
import com.cestasoft.mobileservices.msp.outbound.model.*;
import org.bson.Document;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SubmitMultiResult;
import org.jsmpp.session.SubmitSmResult;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Dispatches single, bulk and multi messages to an SMS server using SMPP protocol
 * @author ezra.k@cestasoft.com
 */
@Service
public class SMPPDispatcher {

    final Logger logger = LoggerFactory.getLogger(SMPPDispatcher.class);

    @Autowired
    private SMPPContext _context;

    @Autowired
    private ErrorManager _errorManager;

    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();

    private ChannelDTO getSMPPChannelConfig(String channelName) {
        Document configDoc = _context.context("smpp");
        Document channels = configDoc.get("channels", Document.class);
        Document channelConfig = channels.get(channelName, Document.class);
        ChannelDTO channel = new ChannelDTO();
        channel.setHost(channelConfig.get("host").toString());
        channel.setPort(Integer.parseInt(channelConfig.get("port").toString()));
        channel.setSystemId(channelConfig.get("systemId").toString());
        channel.setServiceType(channelConfig.get("serviceType").toString());
        channel.setPassword(channelConfig.get("password").toString());
        channel.setSourceAddress(channelConfig.get("sourceAddress").toString());
        return channel;
    }

    private String determineChannel(DispatchTransactionDTO dispatchMessage) {
        // TODO - this should end up in the databse as config w/ rules
        // if (dispatchMessage.getChannelName() != null && !dispatchMessage.getChannelName().isEmpty()) return dispatchMessage.getChannelName();
        return "default";
    }

    public void dispatchMessage(DispatchMessageDTO messageDTO) {
        ChannelDTO channel = getSMPPChannelConfig(determineChannel(messageDTO));
        SMPPSession session = new SMPPSession();
        BindParameter bindParam = new BindParameter(
                BindType.BIND_TX,
                channel.getSystemId(),
                channel.getPassword(),
                "",
                TypeOfNumber.UNKNOWN,
                NumberingPlanIndicator.UNKNOWN,
                null);
        try {
            session.setMessageReceiverListener(new MessageReceiverListenerImpl());
            String systemConnectId = session.connectAndBind(channel.getHost(), channel.getPort(), bindParam);
            logger.debug("connected with SMSC, system id: {}", systemConnectId);
            try {
                SubmitSmResult submitSmResult = session.submitShortMessage(channel.getServiceType(),
                        TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, messageDTO.getSource(),
                        TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, messageDTO.getRecipient(),
                        new ESMClass(), (byte)0, (byte)1,  TIME_FORMATTER.format(new Date()), null,
                        new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), (byte)0, new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false), (byte)0,
                        messageDTO.getValue().getBytes());
                String messageId = submitSmResult.getMessageId();
                logger.debug("message submitted, message_id: {}", messageId);
            } catch (PDUException e) {
                _errorManager.processError(channel, messageDTO, e, logger, "invalid PDU parameter");
            } catch (ResponseTimeoutException e) {
                _errorManager.processError(channel, messageDTO, e, logger, "response timeout");
            } catch (InvalidResponseException e) {
                _errorManager.processError(channel, messageDTO, e, logger, "received invalid response");
            } catch (NegativeResponseException e) {
                _errorManager.processError(channel, messageDTO, e, logger, "receive negative response");
            }
            session.unbindAndClose();
        }  catch (IOException e) {
            _errorManager.processError(channel, messageDTO, e, logger, "failed connect and bind to host");
        }
    }

    private Address [] buildAddressList(DispatchBulkMessageDTO bulkMessageDTO) {
        return Arrays.stream(bulkMessageDTO.getRecipients()).toList()
            .stream()
            .map(recipient -> {
                return new Address(TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, recipient);
            }).toList().toArray(new Address[0]);
    }

    public void dispatchBulkMessages(DispatchBulkMessageDTO bulkMessageDTO) {
        ChannelDTO channel = getSMPPChannelConfig(determineChannel(bulkMessageDTO));
        SMPPSession session = new SMPPSession();
        try {
            session.setMessageReceiverListener(new MessageReceiverListenerImpl());
            String systemConnectId = session.connectAndBind(channel.getHost(), channel.getPort(),
                    new BindParameter(BindType.BIND_TRX, channel.getSystemId(),
                            channel.getPassword(), channel.getServiceType(),
                            TypeOfNumber.UNKNOWN,
                            NumberingPlanIndicator.UNKNOWN,
                            null));
            logger.debug("connected with SMSC with system id {}", systemConnectId);
            try {
                Address[] addresses = buildAddressList(bulkMessageDTO);
                SubmitMultiResult result = session.submitMultiple(channel.getServiceType(), TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, channel.getSourceAddress(),
                        addresses, new ESMClass(), (byte)0, (byte)1, TIME_FORMATTER.format(new Date()), null,
                        new RegisteredDelivery(SMSCDeliveryReceipt.FAILURE), ReplaceIfPresentFlag.REPLACE,
                        new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false), (byte)0,
                        bulkMessageDTO.getValue().getBytes());
                logger.debug("{} messages submitted, result message id {}", addresses.length, result.getMessageId());
                for (UnsuccessDelivery unsuccessDelivery: result.getUnsuccessDeliveries()){
                    logger.debug("unsuccessful delivery to {}: {}", unsuccessDelivery.getDestinationAddress(), unsuccessDelivery.getErrorStatusCode());
                }
            } catch (PDUException e) {
                _errorManager.processError(channel, bulkMessageDTO, e, logger, "invalid PDU parameter");
            } catch (ResponseTimeoutException e) {
                _errorManager.processError(channel, bulkMessageDTO, e, logger, "response timeout");
            } catch (InvalidResponseException e) {
                _errorManager.processError(channel, bulkMessageDTO, e, logger, "receive invalid response");
            } catch (NegativeResponseException e) {
                _errorManager.processError(channel, bulkMessageDTO, e, logger, "receive negative response");
            } catch (IOException e) {
                _errorManager.processError(channel, bulkMessageDTO, e,logger, "I/O error occurred");
            }
            session.unbindAndClose();
        } catch (IOException e) {
            _errorManager.processError(channel, bulkMessageDTO, e, logger, "failed connect and bind to host");
        }
    }

    public void dispatchMultiMessages(DispatchMultiMessageDTO multiMessageDTO) {
        ChannelDTO channel = getSMPPChannelConfig(determineChannel(multiMessageDTO));
        SMPPSession session = new SMPPSession();
        try {
            session.setMessageReceiverListener(new MessageReceiverListenerImpl());
            String systemConnectId = session.connectAndBind(channel.getHost(), channel.getPort(),
                    new BindParameter(BindType.BIND_TRX, channel.getSystemId(),
                            channel.getPassword(), channel.getServiceType(),
                            TypeOfNumber.UNKNOWN,
                            NumberingPlanIndicator.UNKNOWN,
                            null));
            logger.debug("connected with SMSC with system id {}", systemConnectId);
            for (DispatchMessageDTO messageDTO : multiMessageDTO.getMessages()) {
                try {
                    SubmitSmResult submitSmResult = session.submitShortMessage(channel.getServiceType(),
                            TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, messageDTO.getSource(),
                            TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, messageDTO.getRecipient(),
                            new ESMClass(), (byte)0, (byte)1,  TIME_FORMATTER.format(new Date()), null,
                            new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), (byte)0, new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false), (byte)0,
                            messageDTO.getValue().getBytes());
                    String messageId = submitSmResult.getMessageId();
                    logger.debug("message submitted, message_id: {}", messageId);
                } catch (PDUException e) {
                    _errorManager.processError(channel, messageDTO, e, logger, "invalid PDU parameter");
                } catch (ResponseTimeoutException e) {
                    _errorManager.processError(channel, messageDTO, e, logger, "response timeout");
                } catch (InvalidResponseException e) {
                    _errorManager.processError(channel, messageDTO, e, logger, "received invalid response");
                } catch (NegativeResponseException e) {
                    _errorManager.processError(channel, messageDTO, e, logger, "receive negative response");
                }
            }
            session.unbindAndClose();
        } catch (IOException e) {
            _errorManager.processError(channel, multiMessageDTO, e, logger, "failed connect and bind to host");
        }
    }
}
