package com.cestasoft.mobileservices.msp.outbound.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for error messages
 * @author ezra.k@cestasoft.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchErrorMessageDTO extends DispatchTransactionDTO implements Serializable {
    String errorMessage;
    String errorType;
    DispatchTransactionDTO errorPayload;
    ChannelDTO channel;
}
