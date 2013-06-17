package org.soaplab.typedws.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.soaplab.clients.BatchTestClient;
import org.soaplab.clients.ClientConfig;
import org.soaplab.clients.CmdLineHelper;
import org.soaplab.clients.InputUtils;
import org.soaplab.clients.ServiceLocator;
import org.soaplab.clients.SoaplabBaseClient;
import org.soaplab.share.SoaplabException;
import org.soaplab.share.SoaplabMap;
import org.tulsoft.tools.BaseCmdLine;

// Generates and run perl client tests for typed interface
// uses Soaplab batch tests

public class GenerateAndRunPerlClients extends CmdLineHelper {

	static final Logger logger = Logger
			.getLogger(GenerateAndRunPerlClients.class.getName());

	static String testFilesLocation = "/tmp/soaplabtests";
	static String template_ = "src/test/perl-client-templates/typedInterfaceTest.pl";

	private static String getInputFile(String inputFile, String destDir)
			throws IOException {
		File f = new File(inputFile);
		FileUtils.copyFileToDirectory(f, new File(destDir));
		return f.getName();
	}

	static FileWriter testsPassed, testsFailed;

	private static void getServiceTestPerl(String appName, String serviceName,
			String inputs) {
		try {
			File template = new File(template_);
			String perl = FileUtils.readFileToString(template);
			locator.setServiceName(serviceName);
			locator.setUseTypedInterace(true);
			perl = perl.replace("http://www.ebi.ac.uk/soaplab/typed/services/edit.seqret", locator.getServiceEndpoint());

			perl = perl.replaceAll("edit.seqret", serviceName);

			String appspecificdir = testFilesLocation + File.separator
					+ appName + File.separator;
			String inputlines = "";
			if (inputs.trim().length() > 0) {
				String[] inputs_ = new StrTokenizer (inputs,
					       StrMatcher.charSetMatcher (" \t\f"),
					       StrMatcher.quoteMatcher()).getTokenArray();
				for (int i = 0; i < inputs_.length; i += 2) {
					String inputline = null;
					String name = inputs_[i];
					String value = inputs_[i + 1];
					if (value.startsWith(":")) {
						String file = getInputFile(value.substring(1),
								appspecificdir);
						if (name.endsWith("_direct_data")
								&& ( name.contains("sequence") || name.contains("seqfile") 
										|| name.contains("alignfile") || name.contains("dataset")) ) {
							inputline = "'"
									+ name.substring(0, name
											.indexOf("_direct_data"))
									+ "' => { 'direct_data' => &file2String('"
									+ file + "')}";
						} else {
							inputline = "'" + name + "' =>  &file2String('" + file + "')";
						}
					} else if (name.endsWith("_usa")) {
						inputline = "'"
								+ name.substring(0, name.indexOf("_usa"))
								+ "' => { 'usa' => '" + value + "'}";
					} else if (name.startsWith("sbegin_") || name.startsWith("send_") ||
							name.startsWith("sformat_") ) {
						inputline = "'"
								+ name.substring(name.indexOf("_")+1)
								+ "' => { '"+name.substring(0, name.indexOf("_"))+"' => '" + value + "'}";
						//TODO: skip for now as we must put this inside the same sequence bracket 
						continue;						
					} else if (value.startsWith("'")) {
						inputline = "'" + name + "' => " + value;
					} else {
						inputline = "'" + name + "' => '" + value + "'";
					}

					if (inputline != null) {
						if (inputlines.length() != 0)
							inputlines += ",\n\t\t";
						inputlines += inputline;
					}
				}
			}
			// replace the seqret input/output with the actual service
			// input/outputs
			perl = perl.replace("sequence => { usa => 'asis:actttggg' }",
					inputlines);
			String outputlines = getOutputLines(serviceName);

			perl = perl
					.replace("\tmy $outseq = $answer_{'outseq'};", outputlines);
			
			File file = new File(appspecificdir + appName + ".pl");
			FileUtils.writeStringToFile(file, perl);
			//file.setExecutable(true);
			Runtime.getRuntime().exec("chmod 755 " + file.getAbsolutePath());
			file = null;
			int status = testIt(appspecificdir + appName + ".pl",
					appspecificdir);
			if (status == 0) {
				testsPassed.append(appName + " -- " + inputs + "\n");
			} else {
				testsFailed.append(appName + " -- " + inputs + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int testIt(String script, String dir) {
		int status = -1;
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(script);
		pb.directory(new File(dir));
		try {
			Process p = pb.start();
			status = p.waitFor();
			InputStreamReader r = new InputStreamReader(p.getErrorStream());
			StringBuffer b = new StringBuffer();

			try {
				int c;
				while ((c = r.read()) != -1) {
					b.append((char) c);
				}
			} finally {
				if (r != null) {
					r.close();
				}
			}
			System.out.println("\nTest: " + script);
			if (b.length() > 0) {
				b.trimToSize();
				System.err.println("\nError output: " + b);
			}
			System.out.println("Test result: " + status);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(System.getProperty("user.dir"));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return status;
	}

	private static void processTest(String serviceName, String inputs) {

		String fullName = serviceName;
		if (fullName == null)
			return;
		String appName = serviceName.substring(serviceName.indexOf('.') + 1);

		getServiceTestPerl(appName, serviceName, inputs);

	}

	private static ServiceLocator locator = null;

	public static void main(String[] args) {

		try {
			testsPassed = new FileWriter("testsPassed");
			testsFailed = new FileWriter("testsFailed");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		BaseCmdLine cmd = getCmdLine(args, BatchTestClient.class);
		locator = InputUtils.getServiceLocator (cmd);
		// file(s) with the testing data (a list of tested
		// services and their inputs)
		String[] batchFiles = null;
		String batchFile = cmd.getParam("-batchfile");
		if (batchFile != null) {
			// take it from the command-line
			batchFiles = new String[] { batchFile };
		} else {
			// take it from the client configuration file
			batchFiles = ClientConfig.get().getStringArray(
					ClientConfig.PROP_BATCH_TEST_FILE);
		}
		if (batchFiles == null || batchFiles.length == 0) {
			logger
					.severe("A file with a list of services to test must be given. "
							+ "Use '-batchfile' or a property '"
							+ ClientConfig.PROP_BATCH_TEST_FILE + "'.");
			System.exit(1);
		}

		for (String batchfile : batchFiles) {
			logger.info("Using batch file " + batchfile);
			iterateOverTests(batchfile);
		}
		try {
			testsPassed.flush();
			testsFailed.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Program now terminates");
	}

	@SuppressWarnings("unchecked")
	static private void iterateOverTests(String batchfile) {
		// get a list of available services
		// (for validation purposes later)
		Set<String> services = new HashSet<String>();
		try {
			for (String name : new SoaplabBaseClient(locator)
					.getAvailableAnalyses()) {
				services.add(name);
			}
		} catch (SoaplabException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// treat each batch file as a property configuration
		// file - together with a usual Soaplab Client
		// configuration file; this allows handling
		// substitutions of referenced properties, etc.
		CompositeConfiguration cfg = new CompositeConfiguration();
		cfg.addConfiguration(ClientConfig.get());
		try {
			cfg.addConfiguration(new PropertiesConfiguration(batchfile));
		} catch (ConfigurationException e) {
			logger.severe("Loading batch file from '" + batchfile
					+ "' failed: " + e.getMessage());
			return;
		}

		int countNotAvailable = 0;

		// int i = 0;
		for (Iterator<String> it = (Iterator<String>) cfg.getKeys(); it
				.hasNext();) {
			String propertyName = it.next();
			if (!propertyName
					.startsWith(BatchTestClient.PREFIX_SERVICE_TO_TEST))
				continue;
			String serviceName = StringUtils.removeStart(propertyName,
					BatchTestClient.PREFIX_SERVICE_TO_TEST);
			if (!services.contains(serviceName)) {
				logger.warning(serviceName + " is not available for testing");
				countNotAvailable += 1;
				continue;
			}
			String[] inputs = cfg.getStringArray(propertyName);
			if (inputs[0].contains("::"))
				continue;
			processTest(serviceName, inputs[0]);
		}

	}

	static String serviceList = null;

	private static String getOutputLines(String serviceName) {
		String outputlines = "";
		try {
			locator.setUseTypedInterace(false);
			SoaplabBaseClient client = new SoaplabBaseClient(locator);
			SoaplabMap[] outputs_ = client.getResultSpec();
			for (SoaplabMap m : outputs_) {
				String output = (String) m.get("name");
				if (output.equals("report") || output.equals("detailed_status"))
					continue; // already there
				outputlines += "\tmy $" + output + " = $answer_{'" + output
						+ "'};\n";
			}
		} catch (SoaplabException e) {
			e.printStackTrace();
		}
		return outputlines;
	}

}
