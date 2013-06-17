/**
 * WSFastaServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.fasta;

import javax.xml.namespace.QName;

public class WSFastaServiceLocator extends org.apache.axis.client.Service implements uk.ac.ebi.webservices.fasta.WSFastaService {

    public WSFastaServiceLocator() {
    }


    public WSFastaServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WSFastaServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for WSFasta
    private java.lang.String WSFasta_address = "http://www.ebi.ac.uk/Tools/es/ws-servers/WSFasta";

    public java.lang.String getWSFastaAddress() {
        return WSFasta_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSFastaWSDDServiceName = "WSFasta";

    public java.lang.String getWSFastaWSDDServiceName() {
        return WSFastaWSDDServiceName;
    }

    public void setWSFastaWSDDServiceName(java.lang.String name) {
        WSFastaWSDDServiceName = name;
    }

    public uk.ac.ebi.webservices.fasta.WSFasta getWSFasta() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSFasta_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWSFasta(endpoint);
    }

    public uk.ac.ebi.webservices.fasta.WSFasta getWSFasta(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            uk.ac.ebi.webservices.fasta.WSFastaSoapBindingStub _stub = new uk.ac.ebi.webservices.fasta.WSFastaSoapBindingStub(portAddress, this);
            _stub.setPortName(getWSFastaWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWSFastaEndpointAddress(java.lang.String address) {
        WSFasta_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (uk.ac.ebi.webservices.fasta.WSFasta.class.isAssignableFrom(serviceEndpointInterface)) {
                uk.ac.ebi.webservices.fasta.WSFastaSoapBindingStub _stub = new uk.ac.ebi.webservices.fasta.WSFastaSoapBindingStub(new java.net.URL(WSFasta_address), this);
                _stub.setPortName(getWSFastaWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("WSFasta".equals(inputPortName)) {
            return getWSFasta();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSFasta", "WSFastaService");
    }

    private java.util.HashSet<QName> ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet<QName>();
            ports.add(new QName("http://www.ebi.ac.uk/WSFasta", "WSFasta"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("WSFasta".equals(portName)) {
            setWSFastaEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
