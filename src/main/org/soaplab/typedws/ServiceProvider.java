package org.soaplab.typedws;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;

import org.soaplab.share.SoaplabException;

@ServiceMode(value = Service.Mode.PAYLOAD)
@WebServiceProvider()
public class ServiceProvider implements Provider<Source> {

	private static org.apache.commons.logging.Log log =
		org.apache.commons.logging.LogFactory.getLog (ServiceProvider.class);

	@Resource
	WebServiceContext wsContext;

	ServletContext sc;
	
	public final static String DEFAULT_NAMESPACE = "soaplab.org";
	public final static String DEFAULT_PACKAGE = "org.soaplab";
	
    final static QName _GetStatusResponse_QNAME = new QName(
            "http://soaplab.org/typedws", "getStatusResponse");

	
	@SuppressWarnings("unchecked")
	public Source invoke(Source source)
	{
	    Object i;
	    String fullname;
	    String app;
	    JAXBContext jc;

	    MessageContext mc = wsContext.getMessageContext();

	    log.debug("msgcontext: "+mc);

	    if(mc !=null)
	    {
	        Object headers =  mc.get(MessageContext.HTTP_REQUEST_HEADERS);
	        log.debug("http req headers: "+headers);
	    }

	    //QName on = getOperationName(mc);
	    //String ns = on.getNamespaceURI();

	    String packagename = DEFAULT_PACKAGE;
	    String namespace = DEFAULT_NAMESPACE;

	    //		try {
	    //			URI uri = new URI(ns);
	    //			String host = uri.getHost();
	    //			namespace = host.substring(8);
	    //			packagename = hostnameToPackagename(namespace);
	    //		} catch (URISyntaxException e) {
	    //			SoaplabException.formatAndLog(e, log);
	    //		}
	    //		String operationName = on.getLocalPart();

	    sc = (ServletContext) mc.get(MessageContext.SERVLET_CONTEXT);

	    HttpServletRequest httpReq = (HttpServletRequest)
	    mc.get(MessageContext.SERVLET_REQUEST);

	    // Client IP address...
	    String remoteIP = null;
	    if(httpReq.getRemoteAddr() != null)
	        remoteIP = httpReq.getRemoteAddr();
	    // Handle proxy or load balancer client IP.
	    if(httpReq.getHeader("X-Cluster-Client-Ip") != null) {
	        remoteIP =  httpReq.getHeader("X-Cluster-Client-Ip");
	    }
	    // Default value in case of problems
	    if(remoteIP == null)
	        remoteIP = "unknown";

	    log.debug("remote IP: "+remoteIP);



	    try
	    {
	   	  String path = (String) mc.get(MessageContext.PATH_INFO);

		     fullname = path.substring(1);

		     // if the endpoint was sawsdl url trim it
	        if (fullname.endsWith(".sa"))
	      	  fullname = fullname.substring(0, fullname.length()-3);

	        app = fullname.substring(fullname.indexOf('.') + 1).
	        toLowerCase();
	        

	        jc = JAXBContext
	        .newInstance("org.soaplab.typedws:"+packagename+"." + app);
	        Unmarshaller u = jc.createUnmarshaller();
	        i = u.unmarshal(source);

	        if (i instanceof JAXBElement)
	        {
	            JAXBElement<Object> e = (JAXBElement<Object>) i;
	            i = e.getValue();
	        }

	    } catch (Exception e) {
	        SoaplabException.formatAndLog(e, log);

	        throw new WebServiceException("Soaplab not able to process" +
	                " the input xml please check it against the WSDL/XSD" +
	                " of the webservice: ", e);
	    }

	    try
	    {

	        if (i.getClass().getName().endsWith("AppInputs"))
	        {
	            TypedWSJob job = new TypedWSJob(i.getClass(), i);
	            String jid = job.run(fullname, app,  remoteIP);
	            RunResponse rr = new RunResponse();
	            rr.setJobId(jid);

	            QName _RunResponse_QNAME = new QName(
	                    "http://soaplab.org/typedws", "runResponse");
	            
	            JAXBElement<RunResponse> r =
	                new JAXBElement<RunResponse>(
	                    _RunResponse_QNAME, RunResponse.class, null, rr);
	            
	            return new JAXBSource(jc, r);
	        }
	        else if (i.getClass().getName().endsWith("RunAndWaitFor"))
	        {
	            TypedWSJob job = new TypedWSJob(i.getClass().getSuperclass(),
	                    i);

	            Object o = job.runAndWaitFor(fullname, app, packagename,
	                    remoteIP);

	            JAXBSource ret = getJAXBSourceForAppResults(jc, app, o,
	                    "runAndWaitForResponse", packagename, namespace);
	            return ret;
	        }
	        else if (i.getClass().getName().endsWith("GetStatus"))
	        {
	            TypedWSJob job = new TypedWSJob();
	            String jobId = ((GetStatus) i).getJobId().getJobId();
	            String o = job.getStatus(fullname, jobId);
	            
	            GetStatusResponse r = new GetStatusResponse();
	            r.setJobStatus(o);
	            
	            JAXBElement<GetStatusResponse> s =
	                new JAXBElement<GetStatusResponse>(
	                    _GetStatusResponse_QNAME,
	                    GetStatusResponse.class, null, r);
	            return new JAXBSource(jc, s);
	        }
	        else if (i.getClass().getName().endsWith("Terminate")) {
	            TypedWSJob job = new TypedWSJob();
	            String jobId = ((Terminate) i).getJobId().getJobId();
	            job.terminate(fullname, jobId);
	            return null;
	        } else if (i.getClass().getName().endsWith("Clear")) {
	            TypedWSJob job = new TypedWSJob();
	            String jobId = ((Clear) i).getJobId().getJobId();
	            job.clear(fullname, jobId);
	            return null;
	        } else if (i.getClass().getName().endsWith("Waitfor")) {
	            TypedWSJob job = new TypedWSJob();
	            String jobId= ((Waitfor) i).getJobId().getJobId();
	            job.waitfor(fullname, jobId);
	            return null;
	        } else if (i.getClass().getName().endsWith("GetResults")) {
	            TypedWSJob job = new TypedWSJob();
	            GetResults r = (GetResults) i;
	            Object o = job.getResults(fullname, app,
	                    r.getJobId().getJobId(), packagename);
	            JAXBSource ret = getJAXBSourceForAppResults(jc, app, o,
	                    "getResultsResponse", packagename, namespace);
	            return ret;				
            }
	        else if (i.getClass().getName().endsWith("GetSomeResults"))
	        {
                TypedWSJob job = new TypedWSJob();
                GetSomeResults r = (GetSomeResults) i;
                Object o = job.getSomeResults(fullname, app,
                        r.getJobId().getJobId(), packagename,
                        r.getResultNames().toArray(
                                new String[r.getResultNames().size()]));
                JAXBSource ret = getJAXBSourceForAppResults(jc, app, o,
                        "getResultsResponse", packagename, namespace);
                return ret;             
	        }
	        else if (i.getClass().getName().endsWith("Describe"))
	        {
	            TypedWSJob job = new TypedWSJob();
	            String des = job.describe(fullname);
	            DescribeResponse d = new DescribeResponse();
	            d.setDescription(des);
	            
	            QName _DescribeResponse_QNAME = new QName(
	                    "http://soaplab.org/typedws", "describeResponse");
	            
	            JAXBElement<DescribeResponse> s =
	                new JAXBElement<DescribeResponse>(
	                    _DescribeResponse_QNAME,
	                    DescribeResponse.class, null, d);
	            
	            return new JAXBSource(jc, s);
	        } else if (i.getClass().getName().endsWith("GetLastEvent")) {
	            TypedWSJob job = new TypedWSJob();
	            String jobId = ((GetLastEvent) i).getJobId().getJobId();
	            String o = job.getLastEvent(fullname, jobId);
	            final QName _GetLastEventResponse_QNAME = new QName(
	                    "http://soaplab.org/typedws", "getLastEventResponse");
	            JAXBElement<String> s = new JAXBElement<String>(
	                    _GetLastEventResponse_QNAME, String.class, null, o);
	            return new JAXBSource(jc, s);
	        }
	        else if (i.getClass().getName().endsWith("GetResultsInfo"))
	        {
	            TypedWSJob job = new TypedWSJob();
	            String jobId = ((GetResultsInfo) i).getJobId().getJobId();
	            GetResultsInfoResponse r = job.getResultsInfo(fullname, jobId);
	            
	            QName _GetLastEventResponse_QNAME = new QName(
	                    "http://soaplab.org/typedws", "getResultsInfoResponse");

	            JAXBElement<GetResultsInfoResponse> s =
	                new JAXBElement<GetResultsInfoResponse>(
	                        _GetLastEventResponse_QNAME,
	                        GetResultsInfoResponse.class, null, r);

	            return new JAXBSource(jc, s);
	        }
	    } catch (SoaplabException e) {
	        SoaplabException.formatAndLog(e, log);

	        throw new WebServiceException("Soaplab not able to process" +
	                " the input request: ", e);
	    } catch (Exception e) {
	        SoaplabException.formatAndLog(e, log);

	        throw new WebServiceException("Soaplab not able to process" +
	                " the input request please check your inputs: ", e);
	    }

	    String msg = "Soaplab not able to resolve input type " +
	    i.getClass().getName() +
	    " to any existing methods, please check your request " +
	    " against the WSDL/XSD of the webservice";
	    throw new WebServiceException(msg);


	}

	@SuppressWarnings("unused")
	private QName getOperationName(MessageContext context)
	{
			QName operationName = (QName) context
						.get(MessageContext.WSDL_OPERATION);
			if (operationName==null)
				throw new WebServiceException("Soaplab not able to detect" +
						" operation name for the current request." +
						" Please check/validate your XML request" +
						" against service WSDL/XSD");
			return operationName;
	}

	@SuppressWarnings("unchecked")
	JAXBSource getJAXBSourceForAppResults(JAXBContext jc, String app, Object o,
			String localpart, String packagename, String namespace)
	throws ClassNotFoundException, JAXBException
	{
		String ocName = packagename+"." + app + ".AppResults";
		Class oc = Class.forName(ocName);
		final QName _GetResultsResponse_QNAME = new QName(
				"http://"+namespace+"/" + app, localpart);
		o = new JAXBElement<Object>(_GetResultsResponse_QNAME, oc, null, o);
		return new JAXBSource(jc, o);
	}

	// given a host name returns java package name
	// soaplab.test.org returns org.test
	public static String hostnameToPackagename(String host){
		// assumes all host names starts with prefix "soaplab." as in "soaplab.test.org"
		String[] a = host.split("\\.");
		String p = "";
		for (String s:a){
			boolean l = host.endsWith(s);
			String pp = (l ? "" : ".") + s + p ;
			p = pp;
		}
		return p;
	}
}
