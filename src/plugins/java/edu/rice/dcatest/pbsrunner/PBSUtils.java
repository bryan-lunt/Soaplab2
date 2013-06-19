package edu.rice.dcatest.pbsrunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PBSUtils {

	private static Pattern job_status_pattern;
	
	static{
		job_status_pattern = Pattern.compile("job_state = (.*?)\n");
	}
	
	public static String get_job_status(String jobid){
		String ret = "U";
		String info = runQstat(jobid);
		Matcher myMatcher = job_status_pattern.matcher(info);
		if(myMatcher.find()){
			ret = myMatcher.group(1);
		}
		
		return ret;
	}
	
	public static String runQstat(String jobid){
		try{
		ProcessBuilder pb = new ProcessBuilder();
		pb.command().add("qstat");
		pb.command().add("-f");
		if(jobid != null){
			pb.command().add(jobid);
		}
		Process qstat = pb.start();
		
		int qstatExitCode = -324;
		while (true) {
		    try {
		    	qstatExitCode = qstat.waitFor();//waiting for qsub to finish
		    	break;
		    } catch (InterruptedException e) {/*Don't care*/}
		}
		
		String qstatret = readAll(qstat.getInputStream());
		return qstatret;
		}catch(Exception e){}
		return "";
	}
	
	
	public static String readAll(InputStream s){
		String retstr = "";
		try{
			StringBuilder outBuilder = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(s));
			String tmp = br.readLine();
			while(tmp != null){
				outBuilder.append(tmp.trim());
				tmp = br.readLine();
			}
			retstr = outBuilder.toString();
		}catch(Exception e){e.printStackTrace();}
		return retstr;
	}
}
