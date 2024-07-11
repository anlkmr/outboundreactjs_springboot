package com.cestasoft.mobileservices.msp.outbound.config;

import com.cestasoft.mobileservices.msp.outbound.config.context.SMPPContext;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for SMPP client transactions
 * @author ezra.k@cestasoft.com
 */
@Configuration
public class SMPPConfig {
    final Logger logger = LoggerFactory.getLogger(SMPPConfig.class);

    @Value(value = "${outbound.smpp.server.host}")
    private String smppProviderHost;

    @Value(value = "${outbound.smpp.server.port}")
    private int smppProviderPort;

    @Value(value = "${outbound.smpp.server.system-id}")
    private String smppProviderSystemId;

    @Value(value = "${outbound.smpp.server.password}")
    private String smppProviderPassword;

    @Value(value = "${outbound.smpp.server.service-type}")
    private String smppProviderServiceType;

    @Value(value = "${outbound.smpp.server.source-address}")
    private String smppProviderSourceAddress;

    @Bean
    public SMPPContext processorContext() {

        Document defaultChannel = new Document()
                .append("host", smppProviderHost)
                .append("port", smppProviderPort)
                .append("systemId", smppProviderSystemId)
                .append("password", smppProviderPassword)
                .append("serviceType", smppProviderServiceType)
                .append("sourceAddress", smppProviderSourceAddress);

        // TODO get other channels from DB

        Document channels = new Document().append("default", defaultChannel);
        Document smppContextDoc = new Document()
                .append("channels",channels);

        return new SMPPContext(new Document().append("smpp", smppContextDoc));
    }
}
