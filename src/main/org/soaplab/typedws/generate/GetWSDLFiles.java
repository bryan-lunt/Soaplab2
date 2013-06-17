// GetWSDLFiles.java
//
// Created: 2009
//
// Copyright 2010 Mahmut Uludag, Martin Senger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.soaplab.typedws.generate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.soaplab.clients.CmdLineHelper;
import org.soaplab.clients.InputUtils;
import org.soaplab.clients.ServiceLocator;
import org.soaplab.clients.SoaplabBaseClient;
import org.soaplab.services.metadata.AnalysisInstallation;
import org.soaplab.share.SoaplabConstants;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabMap;
import org.tulsoft.shared.FileUtils;
import org.tulsoft.tools.BaseCmdLine;


/**
 * 
 * Generates WSDL and associated XSD files for a given Soaplab installation.
 * For now it uses the local protocol to get analysis list.
 * 
 * @author <A HREF="mailto:uludag@ebi.ac.uk">Mahmut Uludag</A>
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: GetWSDLFiles.java,v 1.6 2012/12/16 18:42:51 mahmutuludag Exp $
 */
public class GetWSDLFiles
    extends CmdLineHelper {

    SoaplabBaseClient client;
    ServiceLocator locator;
    static String wsdloutdir;
    static boolean sawsdl;
    static String inputoutputTypesTemplateFileName;

    // extra inputs added based on outputs processed
    Set<String> extraInputs = new HashSet<String>();

    /*************************************************************************
     * Main worker's constructor
     *
     * @param locator knows about all services whose WSDLs should be
     * generated
     *************************************************************************/
    public GetWSDLFiles (ServiceLocator locator)
	throws SoaplabException {
	this.locator = locator;
	client = new SoaplabBaseClient (locator);
	new File (wsdloutdir).mkdir();
    }

    /*************************************************************************
     *
     *************************************************************************/
    static private void writeStringToFile(String file, String data) {
	try {
	    org.apache.commons.io.FileUtils.writeStringToFile(new File(file), data);
	} catch (IOException e) {
	    processErrorAndExit (e);
	}
    }
	
    /*************************************************************************
     *
     *************************************************************************/
    private void writeXSDFile(String appname, String xsd) {
	String xsdfile = wsdloutdir + appname + (sawsdl? ".sa":"") + ".xsd";
	writeStringToFile(xsdfile, xsd);
    }

    /*************************************************************************
     *
     *************************************************************************/
    private void writeWSDLFile (String analysis, String wsdl) {
	String wsdlfile = wsdloutdir+ analysis + (sawsdl? ".sa":"") + ".wsdl";
	writeStringToFile(wsdlfile, wsdl);
    }

    /*************************************************************************
     *
     *************************************************************************/
    private void getXSDfile (String group, String app)
	throws SoaplabException {

	String template = FileUtils.getFile (inputoutputTypesTemplateFileName + ".xsd");
	String xsd = template.replaceAll("_APP_NAME_", app);
	extraInputs.clear();

	String outputs = getOutputs();
	xsd = xsd.replace("<!-- OUTPUT ELEMENTS -->", outputs.toString());

	String inputs = getInputs();
	xsd = xsd.replace("<!-- INPUT ELEMENTS -->", inputs.toString());

	writeXSDFile(app, xsd);
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String getWSDLfiles (String wsdlTemplate, String endpointTemplate)
	throws SoaplabException {

	StringBuilder endpoints = new StringBuilder();
	String[] analyses = client.getAvailableAnalyses();
	for (String analysis : analyses) {
	    if (analysis.endsWith ("listor"))
		continue;   // TBD: need updated acd2xml
	    locator.setServiceName (analysis);
	    client.setLocator(locator);
						
	    String[] parts =
		AnalysisInstallation.splitName (analysis,
						SoaplabConstants.SOAPLAB_SERVICE_NAME_DELIMITER);
	    String group = parts[0];
	    String app = parts[1];
	    String wsdl = wsdlTemplate.replaceAll("_APP_NAME_", app);
	    wsdl = wsdl.replaceAll("_GROUP_NAME_", group);
	    // replaces the name of the XSD file imported
	    wsdl = wsdl.replaceAll("InputOutputTypesTemplate", app);
	    Map<String, Object> appinfo =
		SoaplabMap.toMap(client.getAnalysisType());
	    Object description = appinfo.get (SoaplabConstants.ANALYSIS_DESCRIPTION);
	    if (description != null)
		wsdl = wsdl.replace("_DESCRIPTION_", description.toString());
	    
	    if (sawsdl) {
		String topic=null, operation=null;
		
		for (String option:appinfo.keySet())
		{
		    // use only ontology options
			if (option.startsWith("EDAM_"))
			{
				String type = option.substring(5, option.indexOf(':'));
				String id = option.substring(option.indexOf(':')+1);
				String value = (String)appinfo.get (option);
				String[] vparts = value.split ("\\s", 3);  // the vparts[2] will not be used here				
				if (vparts == null || vparts.length < 2)
					continue;   // ignore options without decent values
				if (type.equals ("operation")) {
					if (operation==null)
						operation = "/edam/operation/" + id;
					else
						operation += " /edam/operation/" + id;
				}
				else if (type.equals ("topic")) {
					if (topic==null)
						topic = "/edam/topic/" + id;
					else
						topic += " /edam/topic/" + id;
				}
			}
                    
		    if(operation!=null) {
			wsdl = wsdl
			    .replaceAll("_EDAM_OPERATION_",
					operation.replaceAll("/edam/operation/",
							     "http://edamontology.org/operation_"));
		    }
		    if(topic!=null) {
			wsdl = wsdl
			    .replaceAll("_EDAM_TOPIC_",
					topic.replaceAll("/edam/topic/",
							 "http://edamontology.org/topic_"));
		    }
		}
	    }
	
	    writeWSDLFile(analysis, wsdl);
	    if (! sawsdl) {
		// XSD files are the same for both, SA and non-SA, WSDLs, so do them only once
		    getXSDfile (group, app);
		}
	    endpoints.append (endpointTemplate
			      .replaceAll("_APP_NAME_", app+(sawsdl?".sa":""))
			      .replaceAll("_GROUP_NAME_", group));
	}
	return endpoints.toString();
    }
    
    /*************************************************************************
     *
     *************************************************************************/
	String encode(String in) {
		String out;
		out = in.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(
				">", "&gt;");
		return out;
	}

    /*************************************************************************
     *
     *************************************************************************/
	private class XsdElement {
		String maxOccurs = null;
		boolean mandatory = false;
		String type = null;
		String seq_type = null;
		String name;
		String help;
		String annotation;
		String min = null;
		String max = null;
		String default_ = null;
		String[] allowed_values = null;
		// unprocessed entries
		LinkedHashSet<Entry<String, Object>> entrySet = new LinkedHashSet<Entry<String, Object>>();

		public XsdElement(Map<String, Object> m) {
			XsdElement xmle = this;
			for (Entry<String, Object> e : m.entrySet()) {
				String key = e.getKey();
				Object value = e.getValue();
				if (key.equals("mandatory")) {
					if (value.equals("true"))
						xmle.mandatory = true;
				} else if (key.equals("type")) {
					xmle.type = value.toString();
				} else if (key.equals("ordering")) {
				} else if (key.equals("qualifier")) {
				} else if (key.equals("method")) {
				} else if (key.equals("use_defaults")) {
				} else if (key.equals("seq_type")) {
					xmle.seq_type = value.toString();
				} else if (key.equals("server_class")) {
				} else if (key.equals("input_adaptor")) {
				} else if (key.equals("default")) {
					xmle.default_ = value.toString();
				} else if (key.equals("prompt")) {
				} else if (key.equals("extension")) {
				} else if (key.equals("calculated_default")) {
				} else if (key.equals("calculated_type")) {
				} else if (key.equals("calculated_length")) {
				} else if (key.equals("calculated_gytitle")) {
				} else if (key.equals("calculated_gxtitle")) {
				} else if (key.equals("calculated_maximum")) {
				} else if (key.equals("calculated_hardmin")) {
				} else if (key.equals("calculated_hardmax")) {
				} else if (key.equals("calculated_nulldefault")) {
				} else if (key.equals("calculated_size")) {
				} else if (key.equals("calculated_multiple")) {
				} else if (key.equals("calculated_sequence_type")) {
				} else if (key.equals("default_for_direct")) {
				} else if (key.equals("mimetype")) {// TODO
				}
				// only seen in outputs
				else if (key.equals("calculated_minseqs")) {
				} else if (key.equals("calculated_maxseqs")) {
				} else if (key.equals("calculated_features")) {
				} else if (key.equals("calculated_ofname")) {
				} else if (key.equals("calculated_label")) {
				} else if (key.equals("calculated_multiple")) {
				} else if (key.equals("allow_non_existent")) {
				}
				//
				else if (key.equals("envar")) {
				} else if (key.equals("format")) {
				} else if (key.equals("features")) {// TODO: e.g. seqret,
					// process features in input
					// seq and write them out
				} else if (key.equals("readonly")) {
				} else if (key.equals("seq_num")) {// TODO: this should be
					// Soaplab qualifier, no _ in
					// EMBOSS qualifiers
					// example EMBOSS command line... sequence_sformat
				} else if (key.equals("scalemin")) { // TODO: min for integers,
					// and floats(check it)
				} else if (key.equals("scalemax")) {
					// TODO: min means type should be numeric, correction needed
					// on Soaplab side
					// <xs:element default="0" name="identity" maxOccurs="1"
					// minOccurs="0">
					// <xs:simpleType>
					// <xs:restriction base="xs:int">
					// <xs:minExclusive value="0"></xs:minExclusive>
					// </xs:restriction>
					// </xs:simpleType>
					// </xs:element>
				} else if (key.equals("min")) {
					min = value.toString();
				} else if (key.equals("max")) {
					max = value.toString();
				} else if (key.equals("help")) {
				    if (xmle.help == null)
				        xmle.help = value.toString();
				    else
				        xmle.help += "\n"+value.toString();
                } else if (key.startsWith ("EDAM_data:")) {
                    //example:
                    //sawsdl:modelReference="http://purl.org/edam/data/0000848"
                    if(xmle.annotation!=null)
                    {
                        xmle.annotation += " edam/data/" + key.substring(key.indexOf(':')+1);
                    }
                    else
                    {
                        xmle.annotation = "edam/data/" + key.substring(key.indexOf(':')+1);
                    }
                    //xmle.annotation = value.toString();
                } else if (key.equals("relations")) {
                    //TODO: if SAWSDL do it differently
                    //if (sawsdl)
                        xmle.annotation = value.toString();
				} else if (key.equals("allowed_values")) {
					allowed_values = (String[]) value;
				} else if (key.equals("knowntype")) {// TODO:annotation
				} else if (key.equals("datatype")) {// TODO:annotation, this is
					// something in Soaplab not
					// in EMBOSS
				} else if (key.equals("name")) {
					if (value.toString().endsWith("_usa"))
						// || value.toString().endsWith("sequences_usa"))
						name = value.toString().substring(0,
								value.toString().indexOf("_usa"));
					else
						xmle.name = value.toString();
				} else
					xmle.entrySet.add(e);
			}
		}

    /*************************************************************************
     *
     *************************************************************************/
		public String toString() {
			String ret = "<xs:element name =\"" + name + "\" ";
			
			if (annotation !=null)
			{
			    //ret += "sawsdl:modelReference=\"http://embrace/DatatypeOntology#"+annotation+"\" ";
			    
                //example:
                //sawsdl:modelReference="http://purl.org/edam/data/0000848"
			    
			    ret += "sawsdl:modelReference=\""
			    +annotation.replaceAll("edam/data/", "http://edamontology.org/data_")
			    +"\" ";
			    
			}
			if (mandatory) {
				// ret += "minOccurs=\"1\" ";
			} else
				ret += "minOccurs=\"0\" ";

			if (min != null || max != null) {
				if (default_ != null)
					ret += "default=\"" + default_ + "\" ";
			} else if (type != null) {
				if (type.equals("String")) {
					if (seq_type != null) {
						ret += "type=\"emboss:sequenceInput\" ";
					} else {
						if (allowed_values != null) {
							if (default_ != null)
								ret += " default=\"" + default_ + "\"";
							ret += ">\n<xs:simpleType><xs:restriction base=\"xs:string\">";
							for (String a : allowed_values) {
								ret += "\n\t<xs:enumeration value=\"" + a
										+ "\"></xs:enumeration>";
							}
							ret += "\n</xs:restriction></xs:simpleType>\n";
							return ret + "</xs:element>";
						} else {
							ret += "type=\"xs:string\" ";
							if (default_ != null)
								ret += "default=\"" + default_ + "\" ";
							if (maxOccurs != null)
								ret += "maxOccurs=\"" + maxOccurs + "\" ";
						}
					}
				} else if (type.equals("boolean") || type.equals("long")
						|| type.equals("float") || type.equals("base64Binary")) {

					ret += "type=\"xs:" + type + "\" ";
					if (default_ != null)
						ret += "default=\"" + default_ + "\" ";
					if (maxOccurs != null)
						ret += "maxOccurs=\"" + maxOccurs + "\" ";
				}
			}
			if (help != null) {
				ret += ">  " + getAnnotation() + "\n";
			}
			if (min != null || max != null) {
				if (help == null)
					ret += ">\n";

				ret += "<xs:simpleType>\n" + "  <xs:restriction base=\"xs:"
						+ type + "\">\n";
				if (min != null && min.toString().length() > 0)
					ret += "    <xs:minInclusive value=\"" + min
							+ "\"></xs:minInclusive>\n";
				if (max != null && max.toString().length() > 0)
					ret += "    <xs:maxInclusive value=\"" + max
							+ "\"></xs:maxInclusive>\n";
				ret += "  </xs:restriction>\n</xs:simpleType>\n";
			}
			if (help != null || min != null || max != null)
				ret += "\t\t\t\t</xs:element>";
			else
				ret += "/>";

			return ret;
		}

    /*************************************************************************
     *
     *************************************************************************/
		String getAnnotation() {
			if (help == null)
				return "";
			return "<xs:annotation>\n" + "    <xs:documentation>"
					+ encode(help) + "</xs:documentation>\n"
					+ "  </xs:annotation>\n";
		}
	}

    /*************************************************************************
     *
     *************************************************************************/
	private String getInputs() throws SoaplabException {
		SoaplabMap[] inpspecs = client.getInputSpec();
		StringBuilder inputs = new StringBuilder();
		HashMap<String, Integer> chkForLists = new HashMap<String, Integer>();
		HashMap<String, XsdElement> elementsMap = new HashMap<String, XsdElement>();
		List<XsdElement> elements = new ArrayList<XsdElement>();
		
		for (SoaplabMap inpspec : inpspecs)
		{
			Map<String, Object> m = SoaplabMap.toMap(inpspec);
			String name = (String) m.get("name");
			if (name.startsWith("sbegin_") || name.startsWith("send_")
					|| name.startsWith("sprotein_")
					|| name.startsWith("snucleotide_")
					|| name.startsWith("slower_") || name.startsWith("supper_")
					|| name.startsWith("sformat_")
					|| name.startsWith("sreverse_"))
				// || name.endsWith("sequences_direct_data"))
				continue;
			XsdElement xmle = new XsdElement(m);
			
			if (name.endsWith("direct_data") && xmle.seq_type != null)
				continue;
			
			int i = name.indexOf('_');
			
			if (i > 0 && !name.startsWith("offormat"))
			{
			    if (name.endsWith("_direct_data"))
			        i = name.indexOf("_direct_data");
			    if(name.endsWith("_url"))
			        i = name.indexOf("_url");
			        
				String prfx = name.substring(0, i);
				Integer c = chkForLists.get(prfx);
				if (c == null)
					c = Integer.valueOf(1);
				else
					c = Integer.valueOf(c.intValue() + 1);
				chkForLists.put(prfx, c);
				elementsMap.put(prfx, xmle);
			}
			elements.add(xmle);
		}

		boolean nl = false;
		for (XsdElement xmle : elements
				.toArray(new XsdElement[elements.size()])) {
			if (nl)
				inputs.append("\n\t\t\t\t");
			
			String name = xmle.name;
			int i = name.indexOf('_');

			if (name.endsWith("_direct_data"))
                i = name.indexOf("_direct_data");
            if(name.endsWith("_url"))
                i = name.indexOf("_url");
			
			if (i > 0 && !name.startsWith("offormat") && xmle.seq_type == null)
			{

				String prfx = name.substring(0, i);
				Integer j = chkForLists.get(prfx);
				if (j.intValue() == 2
						&& (name.endsWith("_url") || name
								.endsWith("_direct_data"))) {
				    String l = "\n<xs:choice id=\"" + prfx + "\"" +
				    (xmle.mandatory ? "" : " minOccurs=\"0\"") +">";
					    
					l += xmle.getAnnotation() + "\n";
					for (XsdElement xmle_ : elements) {
						if (xmle_.name.startsWith(prfx + "_")) {
							xmle_.help = null;
							// xmle.name = xmle.name.substring(prfx.length()+1);
							l += "\n" + xmle_.toString();
						}
					}
					l += "\n</xs:choice>";
					inputs.append(l);
					chkForLists.put(prfx, Integer.valueOf(-1));
					nl = true;
					continue;
				} else if (j.intValue() > 1
						&& !xmle.type.equals("long") && !xmle.type.equals("float")) {
					String l = "<xs:element name=\""
							+ prfx
							+ "\" minOccurs=\"0\" maxOccurs=\"unbounded\">\n"
							+ "\t<xs:simpleType>\n\t\t<xs:restriction base=\"xs:token\">\n";
					for (XsdElement xmle_ : elements) {
						if (xmle_.name.startsWith(prfx))
							l += "<xs:enumeration value=\""
									+ xmle_.name.substring(prfx.length() + 1)
									+ "\"/>\n";
					}
					l += "\n\t\t</xs:restriction>\n\t</xs:simpleType>\n\t\t\t\t</xs:element>";
					inputs.append(l);
					chkForLists.put(prfx, Integer.valueOf(-1));
					nl = true;
					continue;
				} else if (j.intValue() == -1) {
					nl = false;
					continue;
				}
			}
			inputs.append(xmle.toString());
			nl = true;
		}
		for (String e : extraInputs) {// for now only alignment outputs
			inputs.append("\n\t\t\t\t<xs:element name=\"aformat_" + e
					+ "\" type=\"xs:string\" minOccurs=\"0\"/>");
		}
		return inputs.toString();
	}

    /*************************************************************************
     *
     *************************************************************************/
	private String getOutputs() throws SoaplabException {
		SoaplabMap[] resultSpecs = client.getResultSpec();
		StringBuilder results = new StringBuilder();
		boolean nl = false;
		for (SoaplabMap resultspec : resultSpecs) {
			Map<String, Object> m = SoaplabMap.toMap(resultspec);
			if (nl)
				results.append("\n\t\t\t\t");
			else
				nl = true;

			XsdElement xmle = new XsdElement(m);
			String name = (String) m.get("name");
			String type = (String) m.get("type");
			if (name.equals("Graphics_in_PNG") || type.equals("byte[][]")) {
				xmle.maxOccurs = "unbounded";
				xmle.type = "base64Binary";
			}
			else if (type.equals("byte[]")) {
				xmle.type = "base64Binary";
			}
			else if (type.equals("String[]")) {
				xmle.maxOccurs = "unbounded";
				xmle.type = "String";
			}
			
			if (m.get("datatype") != null
					&& m.get("datatype").equals("alignment")) {
				extraInputs.add((String) m.get("qualifier"));
			}
			results.append(xmle.toString());
		}
		return results.toString();
	}

    /*************************************************************************
     *
     * Entry point...
     *
     *************************************************************************/
    public static void main (String[] args) {
	try {
	    BaseCmdLine cmd = getCmdLine (args, GetWSDLFiles.class);

	    // service location and protocol parameters
	    ServiceLocator locator = InputUtils.getServiceLocator (cmd);

	    // output directory where to create WSDLs
	    wsdloutdir = cmd.getParam ("-wsdlout-dir");
	    if (StringUtils.isEmpty (wsdloutdir)) {
		wsdloutdir = "generated/wsdl/";
	    }
	    if (! wsdloutdir.endsWith ("/")) {
		wsdloutdir += "/";
	    }

	    // template file for SA WSDLs
	    String sawsdlTemplateFileName = cmd.getParam ("-sawsdl-template");
	    if (StringUtils.isEmpty (sawsdlTemplateFileName)) {
		sawsdlTemplateFileName = "src/etc/wsdl/SAWSDLTemplate.wsdl";
	    }

	    // template file for non-SA WSDLs
	    String wsdlTemplateFileName = cmd.getParam ("-wsdl-template");
	    if (StringUtils.isEmpty (wsdlTemplateFileName)) {
		wsdlTemplateFileName = "src/etc/wsdl/WSDLTemplate.wsdl";
	    }

	    // template file for endpoint
	    String endpointTemplateFileName = cmd.getParam ("-wsdl-endpoint");
	    if (StringUtils.isEmpty (endpointTemplateFileName)) {
		endpointTemplateFileName = "src/etc/config/jaxws/typed-interface-endpoint.template";
	    }

	    // template file for input/output types
	    inputoutputTypesTemplateFileName = cmd.getParam ("-iotypes-template");
	    if (StringUtils.isEmpty (inputoutputTypesTemplateFileName)) {
		inputoutputTypesTemplateFileName = "src/etc/wsdl/InputOutputTypesTemplate";
	    }

	    // template file for a list of services ("sun-jaxws.xml")
	    String endpointsTemplateFileName = cmd.getParam ("-endpoints-template");
	    if (StringUtils.isEmpty (endpointsTemplateFileName)) {
		endpointsTemplateFileName = "src/etc/config/jaxws/sun-jaxws.xml.template";
	    }

	    // generate WSDL files (including needed XSD files)
	    GetWSDLFiles worker = new GetWSDLFiles (locator);
	    sawsdl = true;
	    String endpoints = worker.getWSDLfiles (FileUtils.getFile (sawsdlTemplateFileName),
						    FileUtils.getFile (endpointTemplateFileName));
	    sawsdl = false;
	    endpoints += worker.getWSDLfiles (FileUtils.getFile (wsdlTemplateFileName),
					      FileUtils.getFile (endpointTemplateFileName));

	    // update template "endpointsTemplate" by "endpoints" and write it
	    // into the file wsdloutdir + "sun-jaxws.xml.template"
	    String endpointsTemplate = FileUtils.getFile (endpointsTemplateFileName);
	    String endpointsOutputFile = wsdloutdir + "/sun-jaxws.xml.template";
	    writeStringToFile (endpointsOutputFile,
			       endpointsTemplate
			       .replace ("<!-- TYPED_INTERFACE_ENDPOINTS -->", endpoints));

	} catch (Throwable e) {
	    processErrorAndExit (e);
	}
    }
}
