package org.soaplab.emboss;

/**
 * Sequence parameters for EMBOSS applications.
 * 
 * @version $Id: SeqParameter.java,v 1.4 2007/10/26 09:32:17 mahmutuludag Exp $
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.soaplab.services.Job;
import org.soaplab.services.cmdline.IOParameter;
import org.soaplab.services.cmdline.ParameterException;
import org.soaplab.services.metadata.IOParamDef;
import org.soaplab.services.metadata.InputPropertyDef;
import org.soaplab.services.metadata.ParamDef;

public class SeqParameter extends IOParameter {

    public SeqParameter(ParamDef paramDef) {
        super(toIOParamDef(paramDef));
    }

    static private IOParamDef toIOParamDef(ParamDef paramDef) {
        IOParamDef i = new IOParamDef();

        // pure cloning
        i.id = paramDef.id;
        i.type = paramDef.type;
        i.dflt = paramDef.dflt;
        i.conds = paramDef.conds;
        i.options = paramDef.options;
        i.createdFor = paramDef.createdFor;

        // adding IO specifics
        i.ioFormat = IOParamDef.FORMAT_UNSPECIFIED;

        return i;
    }

    /***************************************************************************
     * 
     **************************************************************************/
    public String[] createArg(Map<String, Object> inputs, Job job)
            throws ParameterException {

        List<String> built = build(inputs, job, CMD);
        built.addAll(getAssociatedQualifiersArgs(inputs, job));
        return built.toArray(new String[] {});
    }

    
    // Constructs arguments for inputs not listed as parameters

    private List<String> getAssociatedQualifiersArgs(Map<String, Object> inputs,
            Job job) {
        String id = this.paramDef.id;
        InputPropertyDef[] defs = job.getMetadataAccessor().getInputDefs();
        List<String> saqs = new ArrayList<String>();
        for (InputPropertyDef def : defs) {
            String name = def.name;
            if (name.endsWith("_" + id)) {
                // 'name' should be an associated qualifier
                Object val = inputs.get(name);
                if (val != null) {
                    saqs.add("-" + name + "=" + val);
                }
            }
        }
        return saqs;
    }
}
