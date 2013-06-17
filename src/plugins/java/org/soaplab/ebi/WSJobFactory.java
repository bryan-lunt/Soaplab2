package org.soaplab.ebi;

import java.util.Map;

import org.soaplab.services.Job;
import org.soaplab.services.JobFactory;
import org.soaplab.services.Reporter;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.services.metadata.MetadataUtils;
import org.soaplab.share.SoaplabException;
import org.soaplab.tools.ICreator;

/**
 * A factory for creating WSJob instances for the EBI webservices jobs. It gets
 * the job class name from the 'launcher' attribute defined in the webservices
 * metadeta description. The attribute name can be configured using the launcher
 * property.
 * <p>
 * 
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @author <A HREF="mailto:uludag@ebi.ac.uk">Mahmut Uludag</A>
 * @version $Id: WSJobFactory.java,v 1.3 2007/04/19 12:54:28 mahmutuludag Exp $
 */

public class WSJobFactory implements JobFactory {

	/***************************************************************************
	 * The default constructor.
	 **************************************************************************/
	public WSJobFactory() {
	}

	private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
			.getLog(WSJobFactory.class);

	/***************************************************************************
	 * FACTORY: Gets a new Job.
	 **************************************************************************/
	public Job newInstance(String jobId, MetadataAccessor metadataAccessor,
			Reporter reporter, Map<String, Object> sharedAttributes,
			boolean jobRecreated) throws SoaplabException {

		String jobClassName =
		    MetadataUtils.parseLauncher (metadataAccessor,
						  metadataAccessor.getServiceName(),
						  this) [1];
		
		if (jobClassName == null || jobClassName.length() == 0) {
			log.error("No job class (launcher) defined for the service: "
					+ metadataAccessor.getServiceName());
			throw new SoaplabException(
					"No job class (launcher) defined for the service: "
							+ metadataAccessor.getServiceName());
		}

		log.debug("Using Job class (launcher): " + jobClassName);
		Class[] clss = new Class[] { String.class, MetadataAccessor.class,
				Reporter.class, Map.class, boolean.class };
		Object[] objs = new Object[] { jobId, metadataAccessor, reporter,
				sharedAttributes, jobRecreated };
		Job job = (Job) ICreator.createInstance(jobClassName, clss, objs);
		return job;
	}

}
