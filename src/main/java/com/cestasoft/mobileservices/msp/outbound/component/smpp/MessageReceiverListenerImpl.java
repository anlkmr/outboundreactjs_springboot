/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.cestasoft.mobileservices.msp.outbound.component.smpp;

import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message receiver listener for SMPP outbound commands
 * @author uudashr, ezra.k@cestasoft.com
 */
public class MessageReceiverListenerImpl implements MessageReceiverListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageReceiverListenerImpl.class);

    private static final String DATASM_NOT_IMPLEMENTED = "data_sm not implemented";

    public void onAcceptDeliverSm(DeliverSm deliverSm)
            throws ProcessRequestException {
        
        if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
            // this message is delivery receipt
            try {
                DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
                
                // lets cover the id to hex string format
                long id = Long.parseLong(delReceipt.getId()) & 0xffffffff;
                String messageId = Long.toString(id, 16).toUpperCase();
                
                /*
                 * TODO
                 * you can update the status of your submitted message on the
                 * database based on messageId
                 */

                logger.debug("receiving delivery receipt for message '{}' from {} to {}: {}",
                    messageId, deliverSm.getSourceAddr(), deliverSm.getDestAddress(), delReceipt);
            } catch (InvalidDeliveryReceiptException e) {
                logger.error("failed getting delivery receipt", e);
            }
        } else {
            // this message is regular short message
            
            /*
             * TODO - you can save the incoming message to database.
             */

            logger.debug("receiving message : {}", new String(deliverSm.getShortMessage()));
        }
    }
    
    public void onAcceptAlertNotification(AlertNotification alertNotification) {
        logger.info("alertNotification not implemented");
    }
    
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session source)
            throws ProcessRequestException {
        logger.info("the DataSm is not implemented");
        throw new ProcessRequestException(DATASM_NOT_IMPLEMENTED, SMPPConstant.STAT_ESME_RINVCMDID);
    }
}
