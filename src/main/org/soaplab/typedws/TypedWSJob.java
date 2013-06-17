package org.soaplab.typedws;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.soaplab.clients.ClientConfig;
import org.soaplab.clients.ServiceLocator;
import org.soaplab.clients.SoaplabBaseClient;
import org.soaplab.share.Analysis;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabMap;


/**
 * name of the class not yet carefully selected
 * it converts inputs coming from typed interface to the way Soaplab accepts
 * similarly converts outputs retrieved from soaplab to the form typed interface accepts
 *
 */
public class TypedWSJob {

    private static org.apache.commons.logging.Log log =
		org.apache.commons.logging.LogFactory.getLog (TypedWSJob.class);
    
    static int filecount = 0;

    HashMap<String, Object> inputs;

    public TypedWSJob(Class ic, Object i) throws Exception {

        inputs = new HashMap<String, Object>();
        inputsObjectToMap(i, ic, null);
    }

    public TypedWSJob() {
    }

    @SuppressWarnings("unchecked")
	private void inputsObjectToMap(Object i, Class ic, String associatedObjName)
            throws Exception {
        PropertyDescriptor descs[] = Introspector.getBeanInfo(ic)
                .getPropertyDescriptors();
        for (PropertyDescriptor desc : descs) {
            String name = desc.getName();
            Method proprmethod = desc.getReadMethod();
            if (name.equals("class"))
                continue;
            if (proprmethod == null) {
                proprmethod = ic.getMethod("is"
                        + name.substring(0, 1).toUpperCase()
                        + name.substring(1), new Class[0]);
            }
            Object value = proprmethod.invoke(i, new Object[0]);
            if (value == null)
                continue;
            if (value.getClass().getName().equals(
                    "org.emboss.common.SequenceInput")) {
                inputsObjectToMap(value, value.getClass(), name);
            } else {
            	Field field = ic.getDeclaredField(name);
                XmlElement xmle = field.getAnnotation(XmlElement.class);
                if (xmle != null && !xmle.name().equals("##default")) {
                    name = xmle.name();
                }
                XmlJavaTypeAdapter a = field.getAnnotation(XmlJavaTypeAdapter.class);
                if (a != null && a.value() == CollapsedStringAdapter.class) {
                	if (value instanceof List) {
                		for (Object e : (List) value) {
                			String name_ = name + "_" + e.toString();
                			inputs.put(name_, "true");
                		}
                	}
                	continue;
                }
                
                if (value instanceof JAXBElement) {
                    JAXBElement e = (JAXBElement) value;
                    value = e.getValue().toString();
                }
                if (associatedObjName != null) {
                    if (name.equals("usa") || name.equals("direct_data"))
                        name = associatedObjName + "_" + name;
                    else
                        name = name + "_" + associatedObjName;
                }
                // TODO: how to get the actual enumeration value
                // if (value.getClass().isEnum())value.getClass()
                // value = value.toString();
                if (!(value instanceof String) || ((String) value).length() > 0) {
                    if (value instanceof Long || value instanceof Float)
                        value = value.toString();
                    inputs.put(name, value);
                }
            }
        }
    }

    public String run(String fullname, String appname, String remoteIP)
    throws Exception
    {
        Analysis client = getClient(fullname);
        inputs.put(SoaplabConstants.REMOTE_IP, remoteIP);
        inputs.put(SoaplabConstants.INTERFACE, "jaxws-typed");

        String jobId = client.createAndRun(SoaplabMap.fromMap(inputs));
        
        return jobId;
    }

    
    Object getOutputObject(String appname, String packagename)
    throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        String ocName = packagename+"." + appname + ".AppResults";
        Class oc = Class.forName(ocName);
        Object o = oc.newInstance();
        return o;
    }

    public Object runAndWaitFor(String fullname, String appname,
            String packagename, String remoteIP)
            throws Exception {
        Object o = getOutputObject(appname, packagename);
        Analysis client = getClient(fullname);
        
        inputs.put(SoaplabConstants.REMOTE_IP, remoteIP);
        inputs.put(SoaplabConstants.INTERFACE, "jaxws-typed");
        
        Map<String, Object> results = SoaplabMap.toMap(client
                .runAndWaitFor(SoaplabMap.fromMap(inputs)));

        getResultsIntoOutputObj(results, o);
        
        return o;
    }

    public String getStatus(String fullname, String jobId)
            throws Exception {
        Analysis client = getClient(fullname);
        String status = client.getStatus(jobId);
        return status;
    }

    public void terminate(String fullname, String jobId)
    throws Exception {
    	Analysis client = getClient(fullname);
    	client.terminate(jobId);
    	return;
    }

    public void clear(String fullname, String jobId)
    throws Exception {
    	Analysis client = getClient(fullname);
    	client.destroy(jobId);
    	return;
    }

    public String getLastEvent(String fullname, String jobId)
    throws Exception {
    	Analysis client = getClient(fullname);
    	return client.getLastEvent(jobId);
    }

    /*
     * spinet ResultInfo class can be reused here
     */
    public GetResultsInfoResponse getResultsInfo(String fullname, String jobId)
    throws Exception {
    	GetResultsInfoResponse r = new GetResultsInfoResponse();
    	List<ResultInfo> l = r.getResultsInfoLists();
    	Analysis client = getClient(fullname);

    	SoaplabMap[] ria = client.getResultsInfo(jobId);

    	for (SoaplabMap ri: ria){
    		org.soaplab.typedws.ResultInfo i = new org.soaplab.typedws.ResultInfo();
    		i.setName((String)ri.get (SoaplabConstants.RESULT_NAME));
    		i.setType((String)ri.get (SoaplabConstants.RESULT_TYPE));
    		if (ri.get (SoaplabConstants.RESULT_LENGTH) != null)
    			i.setLength(Long.parseLong((String)ri.get (SoaplabConstants.RESULT_LENGTH)));
    		if (ri.get (SoaplabConstants.RESULT_SIZE) != null)
    			i.setSize(Long.parseLong((String)ri.get (SoaplabConstants.RESULT_SIZE)));
    		l.add(i);
    	}
    	return r;
    }

    public String describe(String fullname)
    throws Exception {
    	Analysis client = getClient(fullname);
    	return client.describe();
    }
    
    

    public void waitfor(String fullname, String jobId)
    throws Exception {
    	Analysis client = getClient(fullname);
    	client.waitFor(jobId);
    	return;
    }

    
    public Object getResults(String fullname, String appname, String jobId, String packagename)
            throws Exception {
        Analysis client = getClient(fullname);
        Map<String, Object> results = SoaplabMap
                .toMap(client.getResults(jobId));
        Object o = getOutputObject(appname, packagename);
        getResultsIntoOutputObj(results, o);
        return o;
    }
    
    public Object getSomeResults(String fullname, String appname, String jobId,
            String packagename, String[] resultNames)
    throws Exception
    {
        Analysis client = getClient(fullname);
        Map<String, Object> results = SoaplabMap
        .toMap(client.getSomeResults(jobId, resultNames));
        Object o = getOutputObject(appname, packagename);
        getResultsIntoOutputObj(results, o);
        return o;
    }

    @SuppressWarnings("unchecked")
    private void getResultsIntoOutputObj(Map<String, Object> results, Object o)
            throws Exception {
        Class oc = o.getClass();
        PropertyDescriptor descs[] = Introspector.getBeanInfo(oc)
                .getPropertyDescriptors();
        for (PropertyDescriptor desc : descs) {
            String name = desc.getName();
            if (name.equals("class"))
                continue;
            XmlElement e = null;
            Field field = null;
            try{
                //TBD: to be investigated, i don't know why but for inputs with single letter
            	//description name is capitalized and here we are not able to find relevant
            	//class filed, but it seems we don't need to refer to xml annotation for such
            	//inputs
              field = oc.getDeclaredField(name);
            } catch (NoSuchFieldException nsfe){
            	log.debug("field not found: "+ name);
            }
            if (field !=null){
             e = field.getAnnotation(
                    javax.xml.bind.annotation.XmlElement.class);
            }
            if (e != null && !e.name().equals("##default")) {
                name = e.name();
                //logger.info("PropertyDescriptor - updated name: " + name);
            }
            if (desc.getPropertyType().equals(java.util.List.class)) {
            	Method proprmethod = desc.getReadMethod();
            	//logger.info("found readMethod " + proprmethod.toString());
            	List l = (List) proprmethod.invoke(o, new Object[] {});
            	Object result = results.get(name);
            	if (result instanceof byte[][]){
            		byte[][] aa = (byte[][]) result;
            		if (aa != null)
            			for (byte[] a : aa) {
            				l.add(a);
            			}
            	} else if (result instanceof String[]){
            		String[] a = (String[]) result;
            		if (a != null)
            			for (String s: a) {
            				l.add(s);
            			}
            	}
            } else {
                Method propwrmethod = desc.getWriteMethod();
                //logger.info("found writeMethod " + propwrmethod.toString());
                //logger.info("type: " + desc.getPropertyType());

                Object value = results.get(name);
                if (value == null)
                    continue;
                // else if (desc.getPropertyType() == Segment.class
                // || desc.getPropertyType() == Dasgff.class)
                // value = ServiceUtils.getFeaturesFromEmbossXmlOutput(fname,
                // desc.getPropertyType());

                if (desc.getPropertyType() == Long.class
                        && value instanceof String)
                    value = Long.valueOf(value.toString());
                Object[] a = { value };// get this from the file
                propwrmethod.invoke(o, a);
            }
        }
    }

    protected Analysis getClient(String serviceName) throws SoaplabException {
        ServiceLocator locator = new ServiceLocator(serviceName);
        locator.setProtocol(ClientConfig.PROTOCOL_LOCAL);
        return new SoaplabBaseClient(locator);
    }
}

