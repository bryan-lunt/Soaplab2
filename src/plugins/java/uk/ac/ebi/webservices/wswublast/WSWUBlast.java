/**
 * WSWUBlast.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.wswublast;

public interface WSWUBlast extends java.rmi.Remote {
    public java.lang.String blastp(java.lang.String database, java.lang.String sequence, java.lang.String email) throws java.rmi.RemoteException;
    public java.lang.String blastn(java.lang.String database, java.lang.String sequence, java.lang.String email) throws java.rmi.RemoteException;
    public java.lang.String getOutput(java.lang.String jobid) throws java.rmi.RemoteException;
    public java.lang.String getXML(java.lang.String jobid) throws java.rmi.RemoteException;
    public java.lang.String runWUBlast(uk.ac.ebi.webservices.wswublast.InputParams params, uk.ac.ebi.webservices.wswublast.Data[] content) throws java.rmi.RemoteException;
    public java.lang.String checkStatus(java.lang.String jobid) throws java.rmi.RemoteException;
    public byte[] poll(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException;
    public uk.ac.ebi.webservices.wswublast.WSFile[] getResults(java.lang.String jobid) throws java.rmi.RemoteException;
    public java.lang.String[] getIds(java.lang.String jobid) throws java.rmi.RemoteException;
    public byte[] polljob(java.lang.String jobid, java.lang.String outformat) throws java.rmi.RemoteException;
    public byte[] doWUBlast(uk.ac.ebi.webservices.wswublast.InputParams params, byte[] content) throws java.rmi.RemoteException;
}
