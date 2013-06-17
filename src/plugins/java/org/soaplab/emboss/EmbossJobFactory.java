package org.soaplab.emboss;

import java.util.Map;

import org.soaplab.services.Job;
import org.soaplab.services.JobFactory;
import org.soaplab.services.Reporter;
import org.soaplab.services.metadata.MetadataAccessor;
import org.soaplab.share.SoaplabException;

/**
 * A factory for EmbossJob instances.
 * 
 * @version $Id: EmbossJobFactory.java,v 1.1 2007/10/04 11:21:17 mahmutuludag Exp $
 */

public class EmbossJobFactory implements JobFactory {

	public Job newInstance(String jobId, MetadataAccessor metadataAccessor,
			Reporter reporter, Map<String, Object> sharedAttributes,
			boolean jobRecreated) throws SoaplabException {
		return new EmbossJob(jobId, metadataAccessor, reporter,
				sharedAttributes, jobRecreated);
	}

}
