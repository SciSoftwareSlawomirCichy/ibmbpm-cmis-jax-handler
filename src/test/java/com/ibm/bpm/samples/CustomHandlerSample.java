/*
 * Licensed Materials - Property of IBM
 * 5725-C94, 5725-C95, 5725-C96
 * (c) Copyright IBM Corporation 2014. All Rights Reserved.
 * This sample program is provided AS IS and may be used, executed, copied and modified without royalty payment by customer 
 * (a) for its own instruction and study, (b) in order to develop applications designed to run with an IBM product, 
 * either for customer's own internal use or for redistribution by customer, as part of such an application, in customer's 
 * own products.
*/

package com.ibm.bpm.samples;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import com.ibm.bpm.integration.client.interfaces.handler.BPMHandler;

public class CustomHandlerSample implements SOAPHandler<SOAPMessageContext> {
	
	public static final String COPYRIGHT = "\n\n(C) Copyright IBM Corporation 2014.\n\n";

	public static final String CLASSNAME = CustomHandlerSample.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);
	
	@Override
	public void close(MessageContext context) {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<QName> getHeaders() {
		return Collections.EMPTY_SET;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// Do not stop processing in a logging handler
		return true;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext messageContext) {
		final String METHODNAME = "handleMessage";
		if (logger.isLoggable(Level.FINE)) {
            logger.entering(CLASSNAME, METHODNAME, messageContext);
        }
		
        Boolean outbound = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        
        if (outbound) {
        	if (logger.isLoggable(Level.FINE)) {
        		// Read the default properties
                String userId = (String) messageContext.get(BindingProvider.USERNAME_PROPERTY);
                String password = (String) messageContext.get(BindingProvider.PASSWORD_PROPERTY);
                String endpointAdress = (String) messageContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
                String repositoryId = (String) messageContext.get(BPMHandler.REPOSITORY_ID_PROPERTY);
                String uniqueRepositoryId = (String) messageContext.get(BPMHandler.UNIQUE_REPOSITORY_ID_PROPERTY);
                
                String request = "Outbound message processed (";
                request+="User: "+ userId+", ";
                request+="Password: "+ password+", ";
                request+="EndpointAdress: "+ endpointAdress+", ";
                request+="RepositoryId: "+ repositoryId+", ";
                request+="UniqueRepositoryId: "+ uniqueRepositoryId;
                request+= ")";

                logger.logp(Level.FINE,CLASSNAME,METHODNAME,request); 
        	} else {
                logger.logp(Level.INFO,CLASSNAME,METHODNAME,"Outbound message processed.");        		
        	}
        } else{
        	logger.logp(Level.INFO,CLASSNAME,METHODNAME,"Inbound message processed.");
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.exiting(CLASSNAME, METHODNAME);
        }
		return true;
	}

}
