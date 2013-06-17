/**
 * WSInterProScanServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.interproscan;

import javax.xml.namespace.QName;

public class WSInterProScanServiceLocator extends org.apache.axis.client.Service implements uk.ac.ebi.webservices.interproscan.WSInterProScanService {

    public WSInterProScanServiceLocator() {
    }


    public WSInterProScanServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WSInterProScanServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for WSInterProScan
    private java.lang.String WSInterProScan_address = "http://www.ebi.ac.uk/Tools/es/ws-servers/WSInterProScan";

    public java.lang.String getWSInterProScanAddress() {
        return WSInterProScan_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WSInterProScanWSDDServiceName = "WSInterProScan";

    public java.lang.String getWSInterProScanWSDDServiceName() {
        return WSInterProScanWSDDServiceName;
    }

    public void setWSInterProScanWSDDServiceName(java.lang.String name) {
        WSInterProScanWSDDServiceName = name;
    }

    public uk.ac.ebi.webservices.interproscan.WSInterProScan getWSInterProScan() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WSInterProScan_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWSInterProScan(endpoint);
    }

    public uk.ac.ebi.webservices.interproscan.WSInterProScan getWSInterProScan(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            uk.ac.ebi.webservices.interproscan.WSInterProScanSoapBindingStub _stub = new uk.ac.ebi.webservices.interproscan.WSInterProScanSoapBindingStub(portAddress, this);
            _stub.setPortName(getWSInterProScanWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWSInterProScanEndpointAddress(java.lang.String address) {
        WSInterProScan_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (uk.ac.ebi.webservices.interproscan.WSInterProScan.class.isAssignableFrom(serviceEndpointInterface)) {
                uk.ac.ebi.webservices.interproscan.WSInterProScanSoapBindingStub _stub = new uk.ac.ebi.webservices.interproscan.WSInterProScanSoapBindingStub(new java.net.URL(WSInterProScan_address), this);
                _stub.setPortName(getWSInterProScanWSDDServiceName());
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
        if ("WSInterProScan".equals(inputPortName)) {
            return getWSInterProScan();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "WSInterProScanService");
    }

    private java.util.HashSet<QName> ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet<QName>();
            ports.add(new QName("http://www.ebi.ac.uk/WSInterProScan", "WSInterProScan"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("WSInterProScan".equals(portName)) {
            setWSInterProScanEndpointAddress(address);
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
