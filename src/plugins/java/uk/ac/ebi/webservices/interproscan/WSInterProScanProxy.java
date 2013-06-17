package uk.ac.ebi.webservices.interproscan;

public class WSInterProScanProxy implements uk.ac.ebi.webservices.interproscan.WSInterProScan {
  private String _endpoint = null;
  private uk.ac.ebi.webservices.interproscan.WSInterProScan wSInterProScan = null;
  
  public WSInterProScanProxy() {
    _initWSInterProScanProxy();
  }
  
  private void _initWSInterProScanProxy() {
    try {
      wSInterProScan = (new uk.ac.ebi.webservices.interproscan.WSInterProScanServiceLocator()).getWSInterProScan();
      if (wSInterProScan != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)wSInterProScan)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)wSInterProScan)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (wSInterProScan != null)
      ((javax.xml.rpc.Stub)wSInterProScan)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public uk.ac.ebi.webservices.interproscan.WSInterProScan getWSInterProScan() {
    if (wSInterProScan == null)
      _initWSInterProScanProxy();
    return wSInterProScan;
  }
  
  public java.lang.String runInterProScan(uk.ac.ebi.webservices.interproscan.InputParams params, uk.ac.ebi.webservices.interproscan.Data[] content) throws java.rmi.RemoteException{
    if (wSInterProScan == null)
      _initWSInterProScanProxy();
    return wSInterProScan.runInterProScan(params, content);
  }
  
  public java.lang.String checkStatus(java.lang.String jobid) throws java.rmi.RemoteException{
    if (wSInterProScan == null)
      _initWSInterProScanProxy();
    return wSInterProScan.checkStatus(jobid);
  }
  
  public byte[] poll(java.lang.String jobid, java.lang.String type) throws java.rmi.RemoteException{
    if (wSInterProScan == null)
      _initWSInterProScanProxy();
    return wSInterProScan.poll(jobid, type);
  }
  
  
}