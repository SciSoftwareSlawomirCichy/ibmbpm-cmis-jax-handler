package pl.scisoftware.bpm.jaxws;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.bpm.integration.client.interfaces.handler.BPMHandler;
import com.ibm.websphere.wim.ras.WIMLogger;

import pl.scisoftware.bpm.jaxws.config.ConfigLoader;
import pl.scisoftware.bpm.jaxws.config.api.IConfigLoader;

public class ServerSOAPHandler implements SOAPHandler<SOAPMessageContext>, BPMHandler {

	private static final String NS_SOAPENV = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String NS_WSSE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	public static final String CLASSNAME = ServerSOAPHandler.class.getName();
	private static final Logger logger = WIMLogger.getTraceLogger(CLASSNAME);
	private IConfigLoader configLoader;

	@Override
	public boolean handleMessage(SOAPMessageContext messageContext) {
		final String METHODNAME = "handleMessage";
		if (logger.isLoggable(Level.FINE)) {
			logger.entering(CLASSNAME, METHODNAME, messageContext);
		}

		try {

			Boolean outbound = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			if (outbound != null && outbound) {

				String endpointAdress = (String) messageContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
				String userId = (String) messageContext.get(BindingProvider.USERNAME_PROPERTY);
				String password = (String) messageContext.get(BindingProvider.PASSWORD_PROPERTY);
				if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(password)) {
					Properties props = SoapHandlerHelper.getEndpointProperties(endpointAdress);
					props.put(BindingProvider.USERNAME_PROPERTY, userId);
					props.put(BindingProvider.PASSWORD_PROPERTY, password);
				}

				if (logger.isLoggable(Level.FINE)) {

					String repositoryId = (String) messageContext.get(BPMHandler.REPOSITORY_ID_PROPERTY);
					String uniqueRepositoryId = (String) messageContext.get(BPMHandler.UNIQUE_REPOSITORY_ID_PROPERTY);

					String request = "Outbound message processed:";
					request += "\n\tUser: " + userId + ", ";
					request += "\n\tPassword: " + password + ", ";
					request += "\n\tEndpointAdress: " + endpointAdress + ", ";
					request += "\n\tRepositoryId: " + repositoryId + ", ";
					request += "\n\tUniqueRepositoryId: " + uniqueRepositoryId;

					logger.logp(Level.FINE, CLASSNAME, METHODNAME, request);
				} else {
					logger.logp(Level.INFO, CLASSNAME, METHODNAME, "Outbound message processed.");
				}

				SOAPMessage message = messageContext.getMessage();
				SOAPPart soapPart = message.getSOAPPart();
				SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
				SOAPHeader soapHeader = soapEnvelope.getHeader();
				if (soapHeader != null) {
					NodeList list = soapHeader.getElementsByTagNameNS(NS_WSSE, "Security");
					if (list != null && list.getLength() > 0) {
						NamedNodeMap attrMap = list.item(0).getAttributes();
						Node attr = attrMap.getNamedItemNS(NS_SOAPENV, "mustUnderstand");
						attr.setNodeValue("0");
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		@SuppressWarnings("unused")
		final String METHODNAME = "handleFault";
		return true;
	}

	@Override
	public void close(MessageContext context) {
		@SuppressWarnings("unused")
		final String METHODNAME = "close";
		logger.logp(Level.INFO, CLASSNAME, METHODNAME, "Closeing...");
	}

	@Override
	public Set<QName> getHeaders() {
		@SuppressWarnings("unused")
		final String METHODNAME = "getHeaders";
		return Collections.emptySet();
	}

	@Override
	public void init(Properties arg0) {
		final String METHODNAME = "init";
		String configDir = arg0.getProperty("configDir");
		if (configDir != null) {
			logger.logp(Level.INFO, CLASSNAME, METHODNAME,
					String.format("Config loader initialization. Config from file: %s", configDir));
			this.configLoader = new ConfigLoader(configDir);
			this.configLoader.loadConfiguration();
		}
	}

}
