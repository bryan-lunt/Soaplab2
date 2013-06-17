package uk.ac.ebi.webservices.wswublast;

public class WSWUBlastProxy implements uk.ac.ebi.webservices.wswublast.WSWUBlast {
  private String _endpoint = null;
  private uk.ac.ebi.webservices.wswublast.WSWUBlast wSWUBlast = null;
  
  public WSWUBlastProxy() {
    _initWSWUBlastProxy();
  }
  
  public WSWUBlastProxy(String endpoint) {
    _endpoint = endpoint;
    _initWSWUBlastProxy();
  }
  
  private void _initWSWUBlastProxy() {
    try {
      wSWUBlast = (new uk.ac.ebi.webservices.wswublast.WSWUBlastServiceLocator()).getWSWUBlast();
      if (wSWUBlast != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)wSWUBlast)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)wSWUBlast)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (wSWUBlast != null)
      ((javax.xml.rpc.Stub)wSWUBlast)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public uk.ac.ebi.webservices.wswublast.WSWUBlast getWSWUBlast() {
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast;
  }
  
  public java.lang.String blastp(java.lang.String database, java.lang.String sequence, java.lang.String email) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.blastp(database, sequence, email);
  }
  
  public java.lang.String blastn(java.lang.String database, java.lang.String sequence, java.lang.String email) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.blastn(database, sequence, email);
  }
  
  public java.lang.String getOutput(java.lang.String jobid) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.getOutput(jobid);
  }
  
  public java.lang.String getXML(java.lang.String jobid) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.getXML(jobid);
  }
  
  public java.lang.String runWUBlast(uk.ac.ebi.webservices.wswublast.InputParams params, uk.ac.ebi.webservices.wswublast.Data[] content) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.runWUBlast(params, content);
  }
  
  public java.lang.String checkStatus(java.lang.String jobid) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.checkStatus(jobid);
  }
  
  public byte[] poll(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.poll(jobid, type);
  }
  
  public uk.ac.ebi.webservices.wswublast.WSFile[] getResults(java.lang.String jobid) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.getResults(jobid);
  }
  
  public java.lang.String[] getIds(java.lang.String jobid) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.getIds(jobid);
  }
  
  public byte[] polljob(java.lang.String jobid, java.lang.String outformat) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.polljob(jobid, outformat);
  }
  
  public byte[] doWUBlast(uk.ac.ebi.webservices.wswublast.InputParams params, byte[] content) throws java.rmi.RemoteException{
    if (wSWUBlast == null)
      _initWSWUBlastProxy();
    return wSWUBlast.doWUBlast(params, content);
  }
  
  
}