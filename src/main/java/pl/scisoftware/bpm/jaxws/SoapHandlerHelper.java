package pl.scisoftware.bpm.jaxws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.lang.StringUtils;

/**
 * Klasa pomocnicza
 * 
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl>
 *
 */
public class SoapHandlerHelper {

	private SoapHandlerHelper() {
	}

	/**
	 * Pamięć podręczna parametrów punktów końcowych.
	 */
	private static final Map<String, Properties> endpointsProperties = new HashMap<String, Properties>();

	/**
	 * Metoda budująca klucz mapy pełniącej rolę pamięci podręcznej do
	 * przechowywania danych uwierzytelniających
	 * 
	 * @param endpointAdress adres endpoint'a
	 * @return wygenerowana wartość klucza
	 * @throws URISyntaxException
	 */
	public static String generateCacheKey(String endpointAdress) throws URISyntaxException {
		if (StringUtils.isBlank(endpointAdress)) {
			return null;
		}
		URI endpointAdressURI = new URI(endpointAdress);
		String hostName = endpointAdressURI.getHost();
		int port = endpointAdressURI.getPort();
		if (port == -1) {
			if (endpointAdressURI.getScheme().equalsIgnoreCase("http")) {
				port = 80;
			} else if (endpointAdressURI.getScheme().equalsIgnoreCase("https")) {
				port = 443;
			}
		}
		return String.format("%s_%d", hostName, port);
	}

	/**
	 * Pobranie parametrów punktu końcowego na podstawie jego adresu
	 * 
	 * @param endpointAdress adres endpoint'a
	 * @return parametry przechowywane w pamięci podręcznej
	 * @throws URISyntaxException
	 */
	public static final Properties getEndpointProperties(String endpointAdress) throws URISyntaxException {
		String endpointKey = generateCacheKey(endpointAdress);
		Properties props = endpointsProperties.get(endpointKey);
		if (props == null) {
			props = new Properties();
			endpointsProperties.put(endpointKey, props);
		}
		return props;
	}

	/**
	 * Pobieranie listy operacji z wiadomości SOAP
	 * 
	 * @param messageContext wiadomość SOAP
	 * @return lista nazw operacji odseparowanych przecinkami.
	 */
	public static final String getCMISOperation(SOAPMessageContext messageContext) {
		SOAPMessage message = messageContext.getMessage();
		StringBuilder operationList = new StringBuilder();
		try {
			SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
			SOAPBody body = envelope.getBody();
			@SuppressWarnings("unchecked")
			Iterator<SOAPElement> childrenIteration = (Iterator<SOAPElement>) body.getChildElements();
			int i = 0;
			while (childrenIteration.hasNext()) {
				SOAPElement operation = childrenIteration.next();
				if (i > 0) {
					operationList.append(", ");
				}
				operationList.append(operation.getLocalName());
				i++;
			}
		} catch (SOAPException e) {
			throw new IllegalArgumentException(e);
		}
		return operationList.toString();
	}

}
