/**
 * WSFasta.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.fasta;

public interface WSFasta extends java.rmi.Remote {
    public java.lang.String runFasta(uk.ac.ebi.webservices.fasta.InputParams params, uk.ac.ebi.webservices.fasta.Data[] content) throws java.rmi.RemoteException;
    public java.lang.String checkStatus(java.lang.String jobid) throws java.rmi.RemoteException;
    public byte[] poll(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException;
}
