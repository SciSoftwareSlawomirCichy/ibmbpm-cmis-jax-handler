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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import com.ibm.bpm.integration.client.interfaces.handler.BPMHandler;

public class CustomBPMHandlerPerformanceSample implements BPMHandler,SOAPHandler<SOAPMessageContext> {
	
	public static final String COPYRIGHT = "\n\n(C) Copyright IBM Corporation 2014.\n\n";
	
	public static final String CLASSNAME = CustomBPMHandlerPerformanceSample.class.getName();
	private static final String PERFORMANCE_LOG_PARAMETER = "Logfile";
    private static final Logger logger = Logger.getLogger(CLASSNAME);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    private long starttime = 0;
    private List<String> cmisOperations;
	Properties properties;
	
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
        	starttime = System.currentTimeMillis();
        	cmisOperations = getCMISOperation(messageContext);
            String endpointAdress = (String) messageContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        	log(METHODNAME, "Initiated "+cmisOperations+" on system "+endpointAdress);
        } else{
        	long responseTime = System.currentTimeMillis() - this.starttime;
            String endpointAdress = (String) messageContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        	log(METHODNAME, "Got a response from system "+endpointAdress+" for "+cmisOperations+" in "+responseTime+"ms");
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.exiting(CLASSNAME, METHODNAME);
        }
		return true;
	}

	private List<String> getCMISOperation(SOAPMessageContext messageContext) {
        SOAPMessage message = messageContext.getMessage();
		List<String> operationList = new ArrayList<String>();
        try {
			SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
			SOAPBody body =  envelope.getBody();
			@SuppressWarnings("unchecked")
			Iterator<SOAPElement> childrenIteration = (Iterator<SOAPElement>)body.getChildElements();
			while(childrenIteration.hasNext()){
				SOAPElement operation = childrenIteration.next();
				operationList.add(operation.getLocalName());
			}
		} catch (SOAPException e) {
			throw new RuntimeException(e);
		}
		return operationList;
	}

	private void log(String methodname, String message) {
		if(properties.containsKey(PERFORMANCE_LOG_PARAMETER)){
			File file = new File(properties.getProperty(PERFORMANCE_LOG_PARAMETER));
			try {
				FileWriter fw = new FileWriter(file, true);
	     		BufferedWriter bw = new BufferedWriter(fw);
	     		bw.write("["+dateFormat.format(new Date()) + "] "+Thread.currentThread().getId()+" "+message+"\n");
	     		bw.close();
	     		fw.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}     		
		} else {
			logger.logp(Level.INFO, CLASSNAME, methodname, message);
		}
	}

	@Override
	public void init(Properties properties) {
		this.properties = properties;	
	}

}
