/**
 * WSWUBlastServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.wswublast;

import javax.xml.namespace.QName;

public class WSWUBlastServiceLocator extends org.apache.axis.client.Service implements uk.ac.ebi.webservices.wswublast.WSWUBlastService {

    public WSWUBlastServiceLocator() {
    }


    public WSWUBlastServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WSWUBlastServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for WSWUBlast
    private java.lang.String WSWUBlast_address = "http://www.ebi.ac.uk/Tools/es/ws-servers/WSWUBlast";

    public java.lang.String getWSWUBlastAddress() {
        return WSWUBlast_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSWUBlastWSDDServiceName = "WSWUBlast";

    public java.lang.String getWSWUBlastWSDDServiceName() {
        return WSWUBlastWSDDServiceName;
    }

    public void setWSWUBlastWSDDServiceName(java.lang.String name) {
        WSWUBlastWSDDServiceName = name;
    }

    public uk.ac.ebi.webservices.wswublast.WSWUBlast getWSWUBlast() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSWUBlast_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWSWUBlast(endpoint);
    }

    public uk.ac.ebi.webservices.wswublast.WSWUBlast getWSWUBlast(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            uk.ac.ebi.webservices.wswublast.WSWUBlastSoapBindingStub _stub = new uk.ac.ebi.webservices.wswublast.WSWUBlastSoapBindingStub(portAddress, this);
            _stub.setPortName(getWSWUBlastWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWSWUBlastEndpointAddress(java.lang.String address) {
        WSWUBlast_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (uk.ac.ebi.webservices.wswublast.WSWUBlast.class.isAssignableFrom(serviceEndpointInterface)) {
                uk.ac.ebi.webservices.wswublast.WSWUBlastSoapBindingStub _stub = new uk.ac.ebi.webservices.wswublast.WSWUBlastSoapBindingStub(new java.net.URL(WSWUBlast_address), this);
                _stub.setPortName(getWSWUBlastWSDDServiceName());
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
        if ("WSWUBlast".equals(inputPortName)) {
            return getWSWUBlast();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast", "WSWUBlastService");
    }

    private java.util.HashSet<QName> ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet<QName>();
            ports.add(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSWUBlast", "WSWUBlast"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("WSWUBlast".equals(portName)) {
            setWSWUBlastEndpointAddress(address);
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
