package pl.scisoftware.bpm.jaxws.test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;
import pl.scisoftware.bpm.jaxws.SoapHandlerHelper;
import pl.scisoftware.bpm.jaxws.config.ConfigLoader;
import pl.scisoftware.bpm.jaxws.config.api.IConfigLoader;

public class SoapMessageTest extends TestCase {

	public void testCleanMessage() throws IOException, SOAPException {

		InputStream is = new FileInputStream("message-001.xml");
		SOAPMessage message = MessageFactory.newInstance().createMessage(null, is);
		is.close();

		SOAPPart soapPart = message.getSOAPPart();
		SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
		SOAPHeader soapHeader = soapEnvelope.getHeader();
		if (soapHeader != null) {
			System.out.println("I have header");
			NodeList list = soapHeader.getElementsByTagNameNS(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");
			if (list != null && list.getLength() > 0) {
				System.out.println("I have Security tag");
				NamedNodeMap attrMap = list.item(0).getAttributes();
				// attrMap.removeNamedItemNS("http://schemas.xmlsoap.org/soap/envelope/",
				// "mustUnderstand");
				Node attr = attrMap.getNamedItemNS("http://schemas.xmlsoap.org/soap/envelope/", "mustUnderstand");
				attr.setNodeValue("0");
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		message.writeTo(out);
		String strMsg = new String(out.toByteArray());
		System.out.println(strMsg);

		is = new FileInputStream("message-002.xml");
		message = MessageFactory.newInstance().createMessage(null, is);
		is.close();

		soapPart = message.getSOAPPart();
		soapEnvelope = soapPart.getEnvelope();
		soapHeader = soapEnvelope.getHeader();

		if (soapHeader == null) {
			SOAPFactory soapFactory = SOAPFactory.newInstance();
			soapHeader = soapEnvelope.addHeader();
			SOAPElement security = soapHeader.addChildElement("Security", "wsse",
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
			security.addAttribute(soapFactory.createName("soapenv:mustUnderstand"), "1");
			security.addAttribute(new QName("xmlns:wsu"),
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

			SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
			usernameToken.addAttribute(QName.valueOf("wsu:Id"), "UsernameToken-A60CD8632EF2091C82164500708019011");
			SOAPElement username = usernameToken.addChildElement("Username", "wsse");
			username.addTextNode("POKHtest");
			SOAPElement password = usernameToken.addChildElement("Password", "wsse");
			password.setAttribute("Type",
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
			password.addTextNode("POKHtest1234");
			message.saveChanges();
		}

		out = new ByteArrayOutputStream();
		message.writeTo(out);
		strMsg = new String(out.toByteArray());
		System.out.println(strMsg);

	}

	public void testHelper() throws URISyntaxException {
		String expectedValue = "docs.oasis-open.org_80";
		assertEquals(expectedValue, SoapHandlerHelper.generateCacheKey(
				"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"));
		expectedValue = "docs.oasis-open.org_443";
		assertEquals(expectedValue, SoapHandlerHelper.generateCacheKey(
				"https://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"));
		expectedValue = "docs.oasis-open.org_8443";
		assertEquals(expectedValue, SoapHandlerHelper.generateCacheKey(
				"https://docs.oasis-open.org:8443/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"));
	}
	
	
	public void testReadProperty() {
		IConfigLoader config = new ConfigLoader("./src/test/resources");
		config.loadConfiguration();
		String value = config.getValue("ecm-appdev1.cn.in.pekao.com.pl_8080.username", null);
		assertNotNull(value);
		assertEquals("POKHtest", value);
	}

}
