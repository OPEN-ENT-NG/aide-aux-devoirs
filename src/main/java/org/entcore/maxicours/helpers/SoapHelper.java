package org.entcore.maxicours.helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class SoapHelper {

	//Triplet class
	private static class Triplet<T, T2, T3>{
		public T _1; public T2 _2; public T3 _3;
		public Triplet(T a, T2 b, T3 c){
			this._1 = a; this._2 = b; this._3 = c;
		}
	}

	/**
	 * Describes a SOAP RPC message.
	 */
	public static class SoapDescriptor{

		private final String message;
		private final ArrayList<Triplet<String, String, String>> attributes = new ArrayList<>();

		/**
		 * Creates a new descriptor.
		 * @param message RPC method name.
		 */
		public SoapDescriptor(String message){
			this.message = message;
		}

		/**
		 * Add a new attribute to the method call.
		 * @param name Attribute name.
		 * @param type Attribute type.
		 * @param value Attribute value.
		 */
		public void addAttribute(String name, String type, String value){
			attributes.add(new Triplet<String, String, String>(name, type, value));
		}

		/**
		 * Returns the method name.
		 */
		public String getMessage(){
			return this.message;
		}

		/**
		 * Returns the method arguments.
		 */
		public ArrayList<Triplet<String, String, String>> getAttributes(){
			return this.attributes;
		}

	}

	/**
	 * Creates a new SOAP RPC message from a descriptor and returns it as an UTF-8 encoded string.
	 * @param messageDescriptor Descriptor object.
	 * @return An encoded UTF-8 String containing the SOAP message.
	 * @throws SOAPException when the message was badly constructed.
	 * @throws IOException if the String encoding of the message failed.
	 */
	public static String createSoapMessage(SoapDescriptor messageDescriptor) throws SOAPException, IOException{

		MessageFactory 	mf = MessageFactory.newInstance();
		SOAPMessage 	msg = mf.createMessage();
		SOAPPart 		part = msg.getSOAPPart();
		SOAPEnvelope 	env = part.getEnvelope();
		SOAPBody 		body = msg.getSOAPBody();

		//Useless header
		msg.getSOAPHeader().detachNode();

		//Adding envelope namespaces
		env.addNamespaceDeclaration("xsd","http://www.w3.org/2001/XMLSchema");
		env.addNamespaceDeclaration("xsi","http://www.w3.org/2001/XMLSchema-instance");
		env.addNamespaceDeclaration("soapenv","http://schemas.xmlsoap.org/soap/envelop/");
		env.addNamespaceDeclaration("urn","urn:mxc-wsdl");
		env.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");

		//Populating the body
		QName bodyName = new QName("", messageDescriptor.message, "urn");
		SOAPBodyElement messageElement = body.addBodyElement(bodyName);

		//Adding arguments
		for(Triplet<String, String, String> attribute : messageDescriptor.attributes){
			QName attributeName = new QName(attribute._1);
			SOAPElement attributeNode = messageElement.addChildElement(attributeName);
			QName typeName = new QName("xsi:type");
			attributeNode.addAttribute(typeName, "xsd:"+attribute._2);
			attributeNode.setTextContent(attribute._3);
		}

		//Exporting as a String object
		ByteArrayOutputStream byteOutput =  new ByteArrayOutputStream();
		msg.writeTo(byteOutput);
		return new String(byteOutput.toByteArray(), "UTF-8");
	}

}
