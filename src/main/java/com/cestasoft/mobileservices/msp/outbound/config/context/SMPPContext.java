package com.cestasoft.mobileservices.msp.outbound.config.context;

import org.bson.Document;

/**
 * Provides configuration context for SMPP client transactions
 * @author ezra.k@cestasoft.com
 */
public class SMPPContext {
    private final Document _context;

    public SMPPContext(Document context) {
        this._context = context;
    }

    public Document context(String reference) {
        if (_context.containsKey(reference))
            return (Document) _context.get(reference);
        return null;
    }
}
