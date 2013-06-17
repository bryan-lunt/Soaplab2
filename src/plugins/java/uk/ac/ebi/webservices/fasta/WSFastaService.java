/**
 * WSFastaService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.fasta;

public interface WSFastaService extends javax.xml.rpc.Service {
    public java.lang.String getWSFastaAddress();

    public uk.ac.ebi.webservices.fasta.WSFasta getWSFasta() throws javax.xml.rpc.ServiceException;

    public uk.ac.ebi.webservices.fasta.WSFasta getWSFasta(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
