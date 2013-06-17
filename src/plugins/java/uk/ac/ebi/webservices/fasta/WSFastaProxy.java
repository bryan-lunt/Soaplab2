package uk.ac.ebi.webservices.fasta;

public class WSFastaProxy implements uk.ac.ebi.webservices.fasta.WSFasta {
  private String _endpoint = null;
  private uk.ac.ebi.webservices.fasta.WSFasta wSFasta = null;
  
  public WSFastaProxy() {
    _initWSFastaProxy();
  }
  
  private void _initWSFastaProxy() {
    try {
      wSFasta = (new uk.ac.ebi.webservices.fasta.WSFastaServiceLocator()).getWSFasta();
      if (wSFasta != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)wSFasta)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)wSFasta)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (wSFasta != null)
      ((javax.xml.rpc.Stub)wSFasta)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public uk.ac.ebi.webservices.fasta.WSFasta getWSFasta() {
    if (wSFasta == null)
      _initWSFastaProxy();
    return wSFasta;
  }
  
  public java.lang.String runFasta(uk.ac.ebi.webservices.fasta.InputParams params, uk.ac.ebi.webservices.fasta.Data[] content) throws java.rmi.RemoteException{
    if (wSFasta == null)
      _initWSFastaProxy();
    return wSFasta.runFasta(params, content);
  }
  
  public java.lang.String checkStatus(java.lang.String jobid) throws java.rmi.RemoteException{
    if (wSFasta == null)
      _initWSFastaProxy();
    return wSFasta.checkStatus(jobid);
  }
  
  public byte[] poll(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException{
    if (wSFasta == null)
      _initWSFastaProxy();
    return wSFasta.poll(jobid, type);
  }
  
  
  
}